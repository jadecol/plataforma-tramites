package com.gestion.tramites.config;

import com.gestion.tramites.interceptor.EntityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private EntityInterceptor entityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(entityInterceptor).addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/public/**");
    }
}
