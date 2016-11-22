package nl.xillio.xill.webservice;

import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
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
    @Bean
    public ThreadPoolTaskExecutor robotCompileThreadPool() {
        ThreadPoolTaskExecutor compileExecutor = new ThreadPoolTaskExecutor();
        compileExecutor.setCorePoolSize(1);
        return compileExecutor;
    }

}
