package com.khm.group.center.config.spring.interceptor;

import com.khm.group.center.interceptor.ClientAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ClientAuthInterceptor())
                .addPathPatterns("/api/client/**")
                .excludePathPatterns("/api/client/public/**");
    }
}
