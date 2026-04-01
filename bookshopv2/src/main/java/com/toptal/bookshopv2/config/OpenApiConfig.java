package com.toptal.bookshopv2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 3.0 configuration for the Bookshop API documentation.
 *
 * <p>Configures the auto-generated API documentation available at
 * {@code /swagger-ui.html} with JWT Bearer authentication support,
 * so developers can test protected endpoints directly from the Swagger UI.</p>
 *
 * @author Nitish
 * @version 2.0
 */
@Configuration
public class OpenApiConfig {
    /**
     * Creates the OpenAPI specification bean with API metadata and JWT security scheme.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Bookshop API v2").version("2.0")
                        .description("Bookshop REST API v2 - In-Memory HashMap Storage"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer")));
    }
}
