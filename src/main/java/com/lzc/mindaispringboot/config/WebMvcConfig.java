package com.lzc.mindaispringboot.config;

import com.lzc.mindaispringboot.Aop.RateLimitInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.upload-dir}")
    private String uploadDir;
    /** 访问前缀，与 FileService 里写的 "/files/" 对应 */
    @Value("${file.access-prefix}")
    private String accessPrefix;
    @Resource
    private RateLimitInterceptor rateLimitInterceptor;
    //addResourceHandlers添加资源处理器
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(accessPrefix)
                .addResourceLocations("file:"+uploadDir.replace("\\", "/") + "/");
    }
    //addInterceptors添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/psychological-chat/**");// 只限流 AI 对话接口
    }
}
