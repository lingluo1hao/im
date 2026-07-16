package com.im.imgateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@ComponentScan(basePackages = {
        "com.im.common",       // 扫描公共模块的配置、工具、异常处理
        "com.im.imgateway" // 扫描当前业务模块自身
})
@SpringBootApplication(
        exclude = {
                // 1. 排除数据源
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,

                // 2. 强行关闭 Spring Security 核心安全自动配置
                org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class,

                // 3. 🚀 关键：强行关闭导致你这次报错的 Actuator 监控安全自动配置
                org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration.class
        }
)
@RestController
public class ImGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImGatewayApplication.class, args);
    }

    @Autowired
    private RouteLocator routeLocator;

    // 访问这个接口，如果是空的 []，说明 Nacos 路由根本没加载进来！
    @GetMapping("/test-routes")
    public Flux<Map<String, Object>> getRoutes() {
        return routeLocator.getRoutes().map(route -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", route.getId());
            map.put("uri", route.getUri().toString());
            map.put("order", route.getOrder());
            return map;
        });
    }
}
