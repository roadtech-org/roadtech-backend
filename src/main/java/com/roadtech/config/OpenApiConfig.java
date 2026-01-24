package com.roadtech.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig
 *
 * This class configures Swagger / OpenAPI documentation for the RoadTech application.
 * It defines:
 *  - API metadata (title, version, description, contact, license)
 *  - Server base URL
 *  - Security scheme for JWT-based authentication
 */
@Configuration // Marks this class as a Spring configuration class
@OpenAPIDefinition(
        info = @Info(
                title = "RoadTech API", // Title displayed in Swagger UI
                version = "1.0.0", // Current API version
                description = "Roadside Assistance System API - Connects users with mechanics for vehicle assistance",
                contact = @Contact(
                        name = "RoadTech Support", // Support contact name
                        email = "support@roadtech.com" // Support contact email
                ),
                license = @License(
                        name = "MIT License", // API license name
                        url = "https://opensource.org/licenses/MIT" // License URL
                )
        ),
        servers = {
                // Base URL for all API endpoints in Swagger UI
                @Server(url = "/api", description = "Default Server")
        }
)
@SecurityScheme(
        name = "bearerAuth", // Name used to reference this security scheme
        type = SecuritySchemeType.HTTP, // HTTP-based authentication
        scheme = "bearer", // Bearer token scheme
        bearerFormat = "JWT", // Token format
        description = "JWT authentication. Obtain token from /auth/login endpoint."
)
public class OpenApiConfig {
    // This class is used only for Swagger/OpenAPI configuration
}
