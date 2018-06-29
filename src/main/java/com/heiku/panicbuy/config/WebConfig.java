package com.heiku.panicbuy.config;

import com.heiku.panicbuy.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Autowired
    private AccessInterceptor accessInterceptor;

    // 参数调试器
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 添加user参数判断
        argumentResolvers.add(userArgumentResolver);
    }


    // 拦截器添加
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加访问限制拦截器
        registry.addInterceptor(accessInterceptor);
    }
}
