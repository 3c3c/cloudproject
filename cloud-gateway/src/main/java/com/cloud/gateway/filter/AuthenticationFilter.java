package com.cloud.gateway.filter;

import com.cloud.gateway.constant.RedisKeys;
import com.cloud.gateway.properties.GatewayWhiteListProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局鉴权过滤器（aaa.md 网关方案）：
 * 1) 白名单放行
 * 2) 解析 Authorization: Bearer xxx，验签 + 有效期
 * 3) Redis 黑名单校验（响应式）
 * 4) Redis 单点登录校验：login:token:{username} 必须存在且等于当前 token（强制下线 / SSO 真正在网关层生效）
 * 5) 解析 userId / username / authorities，写入 X-User-* 头透传下游
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final GatewayWhiteListProperties gatewayProperties;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String bearer = request.getHeaders().getFirst(AUTH_HEADER);
        if (!StringUtils.hasText(bearer) || !bearer.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "缺少认证信息");
        }
        String token = bearer.substring(BEARER_PREFIX.length());

        final Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return unauthorized(exchange, "Token 无效或已过期");
        }

        final String username = claims.getSubject();

        // 黑名单校验（响应式）—— 命中则直接 401
        return redisTemplate.hasKey(RedisKeys.blacklistKey(token)).flatMap(black -> {
            if (Boolean.TRUE.equals(black)) {
                return unauthorized(exchange, "Token 已失效");
            }
            // 单点登录校验：login:token:{username} 必须存在且等于当前 token。
            // 与 cloud-auth 的 JwtAuthenticationFilter 语义一致：新登录会覆盖该 key，旧 token 即被踢下线；
            // 登出会删除该 key。此前网关不校验，导致已踢下线/已登出的 token 在过期前仍可访问下游服务。
            return redisTemplate.opsForValue().get(RedisKeys.loginTokenKey(username))
                    .defaultIfEmpty("")
                    .flatMap(stored -> {
                        if (!token.equals(stored)) {
                            return unauthorized(exchange, "登录状态已失效，请重新登录");
                        }
                        Object userId = claims.get("userId");
                        // 先移除客户端可能伪造的 X-User-* 头，再由网关写入可信值
                        ServerHttpRequest mutated = request.mutate()
                                .headers(h -> {
                                    h.remove(HEADER_USER_ID);
                                    h.remove(HEADER_USERNAME);
                                })
                                .header(HEADER_USER_ID, userId == null ? "" : String.valueOf(userId))
                                .header(HEADER_USERNAME, username == null ? "" : username)
                                .build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    });
        });
    }

    private boolean isWhiteListed(String path) {
        return gatewayProperties.getWhiteList().stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    @Override
    public int getOrder() {
        // 优先级最高，先于其他过滤器鉴权
        return -100;
    }
}
