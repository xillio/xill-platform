package nl.xillio.xill.webservice;

import nl.xillio.xill.webservice.model.XillRuntime;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    @Primary
    @Bean
    public ProxyFactoryBean xillRuntimeProxy() throws ClassNotFoundException {
        ProxyFactoryBean proxy = new ProxyFactoryBean();
        proxy.setProxyInterfaces(new Class<?>[]{XillRuntime.class});
        proxy.setTargetSource(xillRuntimePool());
        return proxy;
    }

}
