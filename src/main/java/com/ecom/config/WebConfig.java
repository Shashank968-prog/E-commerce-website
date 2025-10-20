package com.ecom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** URLs to the actual file system location
        String uploadPath = "file:" + System.getProperty("user.home") + "/ecom-uploads/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}