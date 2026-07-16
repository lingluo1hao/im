package com.im.common.config;

import com.im.common.interceptor.AuthInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// ⚡ 核心保护注解：只有当子模块是普通 Web 服务（如 Tomcat）时才生效，完美避开响应式网关（WebFlux）触发崩溃
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ImWebMvcAutoConfiguration implements WebMvcConfigurer {

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局注册拦截器
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/**") // 拦截所有接口
                .excludePathPatterns(  // 放行基础设施接口
                        "/actuator/**",
                        "/error",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }
}
