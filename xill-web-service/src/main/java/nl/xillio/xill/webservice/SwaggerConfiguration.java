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
    private String serviceBaseURL;

    @Value("${xws.api.version}")
    private String apiVersion;

    @Bean
    public Docket microserviceApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("xill-web-service-api")
                .apiInfo(apiInfo())
                .select()
                    .paths(path -> !path.endsWith(serviceBaseURL) && // exclude the swagger controller
                            !path.equals("/error")) // and the error controller
                    .build()
                .genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Xill Web Service API")
                .description("Xill robot executor microservice")
                .termsOfServiceUrl("https://support.xillio.com")
                .contact(new Contact("Xillio Support","https://www.xillio.com", "support@xillio.com"))
                .version(apiVersion)
                .build();
    }
}
