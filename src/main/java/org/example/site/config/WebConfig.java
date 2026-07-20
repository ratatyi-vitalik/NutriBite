package org.example.site.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Связываем виртуальный путь /uploads/ с реальной папкой на диске C:/site-uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/Java/site/uploads/");
    }
}
