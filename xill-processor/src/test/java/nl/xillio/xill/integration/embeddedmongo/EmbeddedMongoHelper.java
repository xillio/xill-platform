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
package nl.xillio.xill.integration.embeddedmongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoIterable;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProviderException;

/**
 * Created by andrea.parrilli on 2016-04-25.
 */
public enum EmbeddedMongoHelper {
    INSTANCE;

    // The logger cannot be static because it is used in the constructor.
    private final Logger LOGGER = Log.get();
    private MongodStarter starter;
    private MongodExecutable executable;
    private Net net;

    EmbeddedMongoHelper() {
        Command command = Command.MongoD;

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .defaultsWithLogger(command, LOGGER)
                .artifactStore(
                        new ExtractedArtifactStoreBuilder()
                                .defaults(command)
                                .download(
                                        new DownloadConfigBuilder()
                                                .defaultsForCommand(command).build())
                                .executableNaming(new UserTempNaming()))
                .build();

        starter = MongodStarter.getInstance(runtimeConfig);

        executable = deploy();
    }

    private MongodExecutable deploy() {
        try {
            net = new Net();
            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(net)
                    .build();
            System.setProperty("udm.port", Integer.toString(net.getPort()));
            return starter.prepare(mongodConfig);
        } catch (DistributionException e) {
            if (e.getCause() instanceof FileAlreadyExistsException) {
                FileAlreadyExistsException existsException = (FileAlreadyExistsException) e.getCause();
                LOGGER.info("Deployed executable already exists", e);
                try {
                    Files.deleteIfExists(Paths.get(existsException.getFile()));
                    return deploy();
                } catch (IOException e1) {
                    LOGGER.error("Failed to delete executable", e);
                }
            }
            throw e;
        } catch (Exception e) {
            throw new ProviderException("Failed initialization of Embedded Mongo", e);
        }
    }

    public void start() throws IOException {
        if (executable != null) {
            executable.start();
        } else {
            throw new IllegalStateException("The Embedded Mongo instance is not running, probably initialization failed");
        }
    }

    public void stop() throws IOException {
        if (executable != null) {
            executable.stop();
        } else {
            throw new IllegalStateException("The Embedded Mongo instance is not running, probably initialization failed");
        }
    }

    public void cleanupDB() {
        try (MongoClient conn = new MongoClient("localhost", net.getPort())) {
            MongoIterable<String> dbs = conn.listDatabaseNames();
            for (String db : dbs) {
                conn.dropDatabase(db);
            }
        }
    }
}
