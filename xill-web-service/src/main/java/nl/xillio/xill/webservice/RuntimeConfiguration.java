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
package nl.xillio.xill.webservice;

import nl.xillio.xill.webservice.model.Runtime;
import nl.xillio.xill.webservice.xill.RuntimeImpl;
import nl.xillio.xill.webservice.xill.RuntimePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for beans relating to the Xill runtime and environment.
 *
 * @author Geert Konijnendijk
 */
@Configuration
public class RuntimeConfiguration {

    /**
     * @param runtimeFactory
     * @return a pool for {@link RuntimeImpl} instances
     * @throws Exception
     */
    @ConfigurationProperties(prefix = "runtimePool")
    @Lazy(false)
    @Bean(value = "xillRuntimePool", initMethod = "preparePool")
    public ObjectPool<Runtime> xillRuntimePool(RuntimePooledObjectFactory runtimeFactory) {
        return new GenericObjectPool<>(runtimeFactory);
    }

    /**
     * @return a thread pool for asynchronously compiling robots
     */
    @ConfigurationProperties(prefix = "compilePool")
    @Bean
    public ThreadPoolTaskExecutor robotCompileThreadPool() {
        return new ThreadPoolTaskExecutor();
    }

}
