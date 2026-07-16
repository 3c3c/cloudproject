package com.cloud.message.websocket;

import com.cloud.common.jwt.JwtUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 用于验证用户身份和提取用户信息
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("WebSocket 握手开始: URI={}", request.getURI());

        try {
            if (!(request instanceof ServletServerHttpRequest)) {
                return false;
            }
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession();

            // 从请求参数或 Header 中获取 Token
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isEmpty()) {
                token = servletRequest.getServletRequest().getHeader("Authorization");
            }
            // 统一去除 "Bearer " 前缀
            token = jwtUtils.extractToken(token);

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败：缺少 Token");
                return false;
            }

            // 校验签名与有效期
            if (!jwtUtils.validateToken(token)) {
                log.warn("WebSocket 握手失败：Token 无效或已过期");
                return false;
            }

            // 从 Token 中提取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("WebSocket 握手失败：Token 中缺少用户ID");
                return false;
            }

            // 将用户ID存入 attributes，供 Handler 使用
            attributes.put("userId", userId);
            attributes.put("token", token);

            log.info("WebSocket 握手成功: userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("WebSocket 握手异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手后异常", exception);
        }
    }
}
