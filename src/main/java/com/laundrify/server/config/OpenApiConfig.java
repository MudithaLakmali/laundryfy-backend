package com.laundrify.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI laundrifyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Laundrify API")
                        .description("On-demand laundry service platform backend APIs")
                        .version("v1")
                        .contact(new Contact()
                                .name("Laundrify Team")
                                .email("support@laundrify.local"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://example.com/license")));
    }
}
