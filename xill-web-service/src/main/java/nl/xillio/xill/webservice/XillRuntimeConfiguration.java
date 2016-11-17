package nl.xillio.xill.webservice;

import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        pool.setTargetBeanName("xillRuntime");
        return pool;
    }

}
