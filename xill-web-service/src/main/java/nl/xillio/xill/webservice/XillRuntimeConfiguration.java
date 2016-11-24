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

import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for beans relating to the Xill runtime and environment.
 *
 * @author Geert Konijnendijk
 */
@Configuration
public class XillRuntimeConfiguration {

    /**
     * @return A pool for {@link nl.xillio.xill.webservice.xill.XillRuntimeImpl} instances
     */
    @ConfigurationProperties(prefix = "runtimePool")
    @Bean
    public CommonsPool2TargetSource xillRuntimePool() {
        CommonsPool2TargetSource pool = new CommonsPool2TargetSource();
        pool.setTargetBeanName("xillRuntimeImpl");
        return pool;
    }

    /**
     * @return A proxy for directly injecting {@link XillRuntime} instances from the pool without referencing the pool
     * @throws ClassNotFoundException When the {@link XillRuntime} class was not found
     */
    @Primary
    @Bean
    public ProxyFactoryBean xillRuntimeProxy() throws ClassNotFoundException {
        ProxyFactoryBean proxy = new ProxyFactoryBean();
        proxy.setProxyInterfaces(new Class<?>[]{XillRuntime.class});
        proxy.setTargetSource(xillRuntimePool());
        return proxy;
    }

    /**
     * @return A thread pool for asynchronously compiling robots
     */
    @ConfigurationProperties(prefix = "compilePool")
    @Bean
    public ThreadPoolTaskExecutor robotCompileThreadPool() {
        return new ThreadPoolTaskExecutor();
    }

}
