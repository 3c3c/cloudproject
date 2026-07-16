package com.cloud.message.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息处理器
 * 管理用户 WebSocket 连接，实现实时推送
 *
 * @author Cloud Team
 * @since 2025-01-15
 */
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    /** 用户ID → WebSocketSession 映射 */
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 session 属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户建立 WebSocket 连接: userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket 连接建立失败：缺少用户ID");
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从 session 属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户断开 WebSocket 连接: userId={}, sessionId={}, status={}",
                userId, session.getId(), status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息（心跳、确认等）
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: sessionId={}, payload={}", session.getId(), payload);

        // 处理心跳消息
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: sessionId={}", session.getId(), exception);
        // 清理断开的连接
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    /**
     * 向指定用户推送消息
     *
     * @param userId  用户ID
     * @param message 消息内容（JSON 字符串）
     * @return 是否推送成功
     */
    public boolean pushToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
                log.info("WebSocket 推送成功: userId={}", userId);
                return true;
            } catch (IOException e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
                // 清理失效的连接
                userSessions.remove(userId);
                return false;
            }
        } else {
            log.warn("用户离线，无法推送: userId={}", userId);
            return false;
        }
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户ID
     *
     * @return 在线用户ID集合
     */
    public Set<Long> getOnlineUsers() {
        return userSessions.keySet();
    }

    /**
     * 向所有在线用户推送消息
     *
     * @param message 消息内容
     */
    public void broadcast(String message) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (IOException e) {
                    log.error("广播消息失败: userId={}", userId, e);
                }
            }
        });
        log.info("广播消息完成: onlineCount={}", userSessions.size());
    }
}
