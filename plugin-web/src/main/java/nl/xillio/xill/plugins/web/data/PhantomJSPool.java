/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.web.data;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the pooling mechanism for Selenium's PhantomJS processes used in Web.loadpage() Xill
 * function.
 *
 * @author Zbynek Hochmann
 */
@Singleton
public class PhantomJSPool implements AutoCloseable {
    private static final Logger LOGGER = Log.get();

    private final int maxPoolSize; // Maximum number of entities in pool (an entity is a PhantomJS process).
    private List<Entity> poolEntities = new ArrayList<>();

    @Inject
    public PhantomJSPool(XillThreadFactory xillThreadFactory) {
        this(10, xillThreadFactory);
    }

    /**
     * Creates PJS pool
     *
     * @param maxPoolSize Maximum amount of entities (PhantomJS processes) that can
     *                    be in the pool at one time
     */
    public PhantomJSPool(final int maxPoolSize, XillThreadFactory xillThreadFactory) {
        this.maxPoolSize = maxPoolSize;
        // A hook disposing PhantomJS processes when the IDE stops
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        // A Thread stopping PhantomJS processes when the XillEnvironment on the server is closed
        Thread disposeThread = xillThreadFactory.create(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    wait();
                } catch (InterruptedException e) {
                    close();
                }
            }
        }, "PhantomJSPool Dispose");
        disposeThread.setDaemon(true);
        disposeThread.start();
    }

    /**
     * This covers the core of entire pool mechanism
     * It looks if the PhantomJS process with required CLI options exists in a pool and it's free
     * to use - then it will return it and reuse, otherwise it creates new one
     *
     * @param id         in fact it is PJS process CLI options
     * @param webService the webService we're using.
     * @return Entity containing a page (i.e. PhantomJS process) with provided options that can be used for web operations
     */
    public Entity get(final Identifier id, final WebService webService) {
        LOGGER.debug("PJSPool: New request for WebDriver...");
        // Try to find a free entity and reuse it.
        Entity entity = findFirstFreeEntityAndReuse(id);

        // No free entity was found.
        if (entity == null) {
            // Check if the pool is full and if there are no unused entities.
            if (poolEntities.size() >= maxPoolSize && !freeUnusedEntity()) {
                LOGGER.error("PJSPool: No free slot for next PhantomJS process!");
                return null;
            }

            LOGGER.debug("PJSPool: Creating new driver...");
            entity = new Entity(webService);
            entity.page = entity.create(id);
            poolEntities.add(entity);
        } else {
            LOGGER.debug("PJSPool: Reusing old driver...");
        }
        return entity;
    }

    /**
     * It ends up one of not currently used PhantomJS process in
     * the pool and removes the entity from the pool
     *
     * @return true if entity has been freed or false if there is no any not
     * used entity in the pool
     */
    private boolean freeUnusedEntity() {
        for (Entity item : poolEntities) {
            if (!item.isUsed()) {
                item.dispose(); // it will end up PJS process
                return poolEntities.remove(item);
            }
        }
        return false;
    }

    /**
     * Creates new Identifier object from Options object
     *
     * @param options PJS CLI options
     * @return created Identifier
     */
    public Identifier createIdentifier(final Options options) {
        return new Identifier(options);
    }

    /**
     * Disposes entire PJS pool - i.e. all pool entities (~all PhantomJS
     * processes in the pool will be terminated)
     */
    public void close() {
        try {
            poolEntities.forEach(PhantomJSPool.Entity::dispose);
            poolEntities.clear();
        } catch (Exception e) {
            LOGGER.error("Error when closing PhantomJS instances! " + e.getMessage(), e);
        }
    }

    /**
     * It tries to find any existing PhantomJS process that has the same
     * CLI options given by 'id' parameter and it's currently not in use.
     * It sets 'used' flag to true immediately on the found item.
     *
     * @param id in fact it is PJS process CLI options
     * @return Found entity or null if not found convenient process
     */
    private Entity findFirstFreeEntityAndReuse(final Identifier id) {
        for (Entity item : poolEntities) {
            if (item.compare(id) && !item.isUsed()) {
                item.reuse(id);
                return item;
            }
        }
        return null;
    }

    /**
     * It represents one item in the pool - i.e. one existing PhantomJS process with other related information
     * Entity consists basically from one pair: PJS process (driver) and
     * the options (id) that have been used when PJS has been started.
     * The PJS CLI options once used for PJS process, they cannot be changed for particular existing PJS anymore.
     * So the CLI options are like identificators when we are looking in the pool for PJS process with given CLI
     * options.
     */
    public class Entity implements AutoCloseable, MetadataExpression {
        private final WebService webService;

        private WebVariable page;
        private Identifier id;
        private boolean used = false; // is this entity in use (true) or free to use (false)?

        /**
         * The constructor for the Entity class.
         *
         * @param webService The webService the class will be using.
         */
        public Entity(final WebService webService) {
            this.webService = webService;
        }


        /**
         * @return true if this PJS process is currently in use; otherwise false which means that this PJS process is
         * free to re/use
         */
        public boolean isUsed() {
            return used;
        }

        /**
         * Creates new PhantomJS pool entity
         * It starts new PJS process with CLI options given by Identifier
         * It sets this PJS process as used automatically
         *
         * @param id in fact it is PJS process CLI options
         * @return Newly created WebDriver instance (i.e. PhantomJS)
         */
        private WebVariable create(final Identifier id) {
            this.id = id;
            PageVariable pageVariable = webService.createPage(id.getOptions());
            pageVariable.setEntity(this);
            used = true;
            return pageVariable;
        }

        /**
         * Determines if the identifier 'id' matches this entity
         * It compares all CLI options if this particular existing PJS process
         * in the pool has been started with same CLI options as we are looking for
         *
         * @param id in fact it is PJS process CLI options
         * @return true if CLI options matches
         */
        public boolean compare(final Identifier id) {
            return this.id.getOptions().compareDCap(id.getOptions());
        }

        /**
         * Reuse PJS process
         * It takes this PJS process (free to use check must be done before this call)
         * and set non-CLI options for this PJS process (CLI options must match) and set as currently in use
         *
         * @param id It is used for passing the options that are to be set
         *           after driver is created (not CLI options)
         */
        private void reuse(final Identifier id) {
            used = true; // set to be used
            // re-set driver (non-CLI) options
            webService.setDriverOptions(page, id.getOptions());
        }

        /**
         * Terminates PhantomJS process of this entity
         * It will end up this PJS process.
         */
        private void dispose() {
            webService.quit(page);
        }

        /**
         * Stops using this entity (PhantomJS process)
         * It sets that this PJS process is free to use now
         */
        public void release() {
            used = false;
        }

        /**
         * @return WebDriver of this entity (PhantomJS process class)
         */
        public WebVariable getPage() {
            return page;
        }

        @Override
        public void close() throws Exception {
            release();
        }
    }

    /**
     * It represents the options that have been used as CLI arguments of
     * PhantomJS process. It helps to identify the proper PhantomJS
     * process - i.e. existing process that has been started with the same CLI
     * parameters as we are looking for.
     * <p>
     * This class could be omitted and used Options class instead but there can be/could have been more various characters identifying particular PJS process.
     */
    public class Identifier {

        private final Options options;

        /**
         * Constructor of Identifier class
         *
         * @param options The CLI options that has been used when given PJS process has been started
         * @throws NullPointerException Throws a NullPointerException when the options were null.
         */
        public Identifier(final Options options) {
            if (options == null) {
                throw new NullPointerException("Options can't be null");
            }
            this.options = options;
        }

        /**
         * @return The CLI options that has been used when given PJS process has been started
         */
        public Options getOptions() {
            return options;
        }
    }
}
