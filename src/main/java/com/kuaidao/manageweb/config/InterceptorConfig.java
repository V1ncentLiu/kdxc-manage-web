package com.kuaidao.manageweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import com.kuaidao.manageweb.interceptor.RequestCounterInterceptor;
import com.kuaidao.manageweb.interceptor.RequestTimingInterceptor;

@Configuration
public class InterceptorConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestTimingInterceptor());
        registry.addInterceptor(new RequestCounterInterceptor());
    }
}
