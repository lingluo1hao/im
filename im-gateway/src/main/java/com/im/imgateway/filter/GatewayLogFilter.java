package com.im.imgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 获取请求的核心链路信息
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getRawQuery();
        String clientIp = request.getRemoteAddress() != null ? request.getRemoteAddress().getHostString() : "未知IP";

        // 拼接成一行显眼的日志大旗
        String logMessage = String.format("【网关请求接收】=> 客户端IP: %s | 请求方法: %s | 路径: %s%s",
                clientIp, method, path, (query != null ? "?" + query : ""));

        log.info("=================================================================================");
        log.info(logMessage);
        log.info("【请求头 token】: {}", request.getHeaders().getFirst("token"));
        log.info("=================================================================================");

        // 记录请求耗时并交给下一个过滤器（最终转发给业务微服务）
        long startTime = System.currentTimeMillis();
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long executeTime = System.currentTimeMillis() - startTime;
            log.info("【网关转发响应】=> 路径: {} | 转发处理耗时: {} ms", path, executeTime);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 直接返回最小值，不再做减法
    }

}
