package com.roadtech.config;

// Marks this class as a Spring configuration class
import org.springframework.context.annotation.Configuration;

// Used to configure CORS (Cross-Origin Resource Sharing) mappings
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // This method is used to define global CORS configuration for the application
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // Apply CORS configuration to all endpoints in the application
        registry.addMapping("/**")

            // Allow requests from these origins
            // - Any localhost port (useful for local development)
            // - Deployed frontend on Vercel
            // - Any subdomain on Vercel
            .allowedOriginPatterns(
                "http://localhost:*",
                "https://roadtech.vercel.app",
                "https://*.vercel.app"
            )

            // Allow these HTTP methods in cross-origin requests
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

            // Allow all request headers
            .allowedHeaders("*")

            // Allow cookies / authorization headers to be sent with requests
            .allowCredentials(true)

            // Cache the CORS configuration for 1 hour (in seconds)
            .maxAge(3600);
    }
}
