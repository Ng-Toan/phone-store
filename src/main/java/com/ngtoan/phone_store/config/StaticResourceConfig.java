package com.ngtoan.phone_store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath =
                System.getProperty("user.dir")
                        + File.separator + "uploads"
                        + File.separator + "product"
                        + File.separator;

        registry.addResourceHandler("/img/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }
}