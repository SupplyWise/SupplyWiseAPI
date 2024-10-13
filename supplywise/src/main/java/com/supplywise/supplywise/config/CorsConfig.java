package com.supplywise.supplywise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// This class is used to configure the CORS policy for the application
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow all origins to access the API
        registry.addMapping("/**")
                //allow React and Postman to access the API
                //.allowedOrigins("http://localhost:5173", "https://localhost:5173", "https://www.getpostman.com")
                .allowedOrigins("*") // testing only!!
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}