package com.im.imgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.common.result.R;
import com.im.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/login",
            "/user/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单直接放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            // 🚀 核心优化：网关统一为白名单请求打上“信任通行证”，防止下游拦截器误拦
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Is-White", "true")
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }


        // 2. 获取请求头 token
        String token = request.getHeaders().getFirst("token");
        if (token == null || token.trim().isEmpty()) {
            return buildErrorResponse(exchange, 401, "请先登录");
        }

        String redisKey = "auth:token:" + token;

        // 3. 异步校验 Redis 中 token 是否有效
        return redisTemplate.hasKey(redisKey)
                .flatMap(exists -> {
                    if (!exists) {
                        return buildErrorResponse(exchange, 401, "登录凭证已失效，请重新登录");
                    }

                    String userId;
                    String rolesStr = "";
                    String permsStr = "";

                    // 4. 解析 Token 并提取扩展的角色权限数据
                    try {
                        // 建议修改你的 jwtUtil.getUserId(token) 方法，或者让其支持获取完整的 Claims
                        // 假设你的 JWT 载荷中存了 userId, roles, permissions
                        userId = String.valueOf(jwtUtil.getUserId(token));

                        // 提取角色和权限（如果 jwtUtil 暂不支持，可以先只传 userId，后续演进再传权限）
                        // rolesStr = jwtUtil.getRoles(token);
                        // permsStr = jwtUtil.getPermissions(token);
                    } catch (Exception e) {
                        log.error("JWT 解析失败: {}", e.getMessage());
                        return buildErrorResponse(exchange, 401, "登录凭证非法，请重新登录");
                    }

                    // 5. 关键重构：利用响应式 mutate 机制将核心认证数据注入请求头，向后传递
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId)
                            // .header("X-User-Roles", rolesStr)       // 供下游微服务拦截角色
                            // .header("X-User-Perms", permsStr)       // 供下游微服务拦截具体资源
                            .build();

                    // 校验通过，使用带有新 Header 的 exchange 继续向下转发
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    /**
     * 响应式标准回包工具
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, int code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            R<?> result = R.fail(code, msg);
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 🚀 相当于以前的 @Order(Ordered.HIGHEST_PRECEDENCE)
        // 保证网关最先执行鉴权逻辑，阻断非法请求
        return Ordered.HIGHEST_PRECEDENCE;
    }
}