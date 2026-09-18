package com.lzc.mindaispringboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;
    /** 访问前缀，与 FileService 里写的 "/files/" 对应 */
    @Value("${file.access-prefix}")
    private String accessPrefix;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(accessPrefix)
                .addResourceLocations("file:" + uploadDir);
    }
}
