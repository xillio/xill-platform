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
package nl.xillio.xill.plugins.jdbc;


import com.google.inject.*;
import com.google.inject.name.Named;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.plugins.jdbc.constructs.*;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactory;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactoryImpl;
import nl.xillio.xill.plugins.jdbc.services.TemporalConversionService;

/**
 * This XillPlugin provides a base implementation for any jdbc driver.
 * By creating a subclass of this class you can access a database system from
 */
public abstract class JDBCXillPlugin extends XillPlugin {
    protected Injector injector;

    @Override
    public void loadConstructs() {
        Injector localInjector = injector.createChildInjector(new InternalConfig(getClass()));
        add(localInjector.getInstance(connectConstruct()));
        add(localInjector.getInstance(queryConstruct()));
        add(localInjector.getInstance(getObjectConstruct()));
        add(localInjector.getInstance(storeObjectConstruct()));
        add(localInjector.getInstance(escapeConstruct()));
        add(localInjector.getInstance(toDateTimeConstruct()));
        add(localInjector.getInstance(toTimestampConstruct()));
    }

    private Class<? extends Construct> queryConstruct() {
        return QueryConstruct.class;
    }

    protected Class<? extends Construct> connectConstruct() {
        return ConnectConstruct.class;
    }

    protected Class<? extends Construct> getObjectConstruct() {
        return GetObjectConstruct.class;
    }

    protected Class<? extends Construct> storeObjectConstruct() {
        return StoreObjectConstruct.class;
    }

    protected Class<? extends Construct> escapeConstruct() {
        return EscapeConstruct.class;
    }

    protected Class<? extends Construct> toDateTimeConstruct() {
        return ToDateConstruct.class;
    }

    protected Class<? extends Construct> toTimestampConstruct() {
        return ToTimestampConstruct.class;
    }

    protected Class<? extends StatementSyntaxFactory> selectStatementFactory() {
        return StatementSyntaxFactoryImpl.class;
    }

    protected abstract Class<? extends ConnectionFactory> connectionFactory();

    protected Class<? extends TemporalConversionService> temporalConversionService() {
        return TemporalConversionService.class;
    }

    @Inject
    void setInjector(Injector injector) {
        this.injector = injector;
    }

    private class InternalConfig extends AbstractModule {

        private final Class pluginPackage;

        private InternalConfig(Class pluginPackage) {
            this.pluginPackage = pluginPackage;
        }

        @Override
        protected void configure() {
            bind(ConnectionFactory.class).to(connectionFactory()).asEagerSingleton();
            bind(StatementSyntaxFactory.class).to(selectStatementFactory());
        }

        @Provides
        @Named("docRoot")
        @Singleton
        String docRoot() {
            return "/" + pluginPackage.getPackage().getName().replace(".", "/") + "/constructs/";
        }
    }

}
