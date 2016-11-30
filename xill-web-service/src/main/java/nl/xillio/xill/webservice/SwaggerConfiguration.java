package nl.xillio.xill.webservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Application configuration for Swagger.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Value("${xws.api.base.path}")
    private String monitorBaseURL;

    @Bean
    public Docket stateApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("xill-web-service-api")
                .apiInfo(apiInfo())
                .select()
                    .paths(path -> !path.endsWith(monitorBaseURL) && // exclude the swagger controller
                            !path.equals("/error")) // and the error controller
                    .build()
                .genericModelSubstitutes(ResponseEntity.class);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Xill Web Service API")
                .description("Xill robot executor microservice")
                .termsOfServiceUrl("https://support.xillio.com")
                .contact(new Contact("Xillio Support","https://www.xillio.com", "support@xillio.com"))
                .version("${xws.api.version")
                .build();
    }


}
