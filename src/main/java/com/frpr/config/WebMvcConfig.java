package com.frpr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("fr.allowed.origins")
    private String allowedOrigins;

    @Value("fr.allowed.headers")
    private String allowedHeaders;

    @Value("fr.allowed.methods")
    private String allowedMethods;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedHeaders(allowedHeaders.split(","))
        .allowedMethods(allowedMethods.split(","));
    }


}
