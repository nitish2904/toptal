package com.toptal.bookshopv3.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** Springdoc OpenAPI configuration exposing JWT Bearer Auth scheme on Swagger UI. */

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Bookshop API v3").version("3.0")
                        .description("Online Bookshop REST API – HTTP Basic Auth, In-Memory Storage"))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .schemaRequirement("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"));
    }
}
