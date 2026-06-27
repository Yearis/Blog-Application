package com.yearis.blog_application.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Blog App API",
                description = "Backend API for the Blog Application",
                version = "v1.0",
                contact = @Contact(
                        name = "Yearis",
                        email = "your.email@example.com",
                        url = "https://yourwebsite.com"
                )
        ),
        // This applies security globally to all paths
        security = @SecurityRequirement(name = "bearerAuth")
)
// This defines the Security Scheme (JWT)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
    // No code here
}