package com.cloud.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 工具（对齐 aaa.md）。无状态身份凭证：携带 username / userId / authorities。
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties properties;

    /** 生成 token */
    public String generateToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities,
                                String nickname, String mobile, String email, String avatar, Boolean mustChangePassword) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + properties.getExpiration());

        String authoritiesStr = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("authorities", authoritiesStr)
                .claim("nickname", nickname)
                .claim("mobile", mobile)
                .claim("email", email)
                .claim("avatar", avatar)
                .claim("mustChangePassword", mustChangePassword)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 token */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 校验 token（签名 + 有效期） */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object id = getClaimsFromToken(token).get("userId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return id != null ? Long.valueOf(id.toString()) : null;
    }

    /** 从 token 中还原权限集合 */
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        String authoritiesStr = getClaimsFromToken(token).get("authorities", String.class);
        if (!StringUtils.hasText(authoritiesStr)) {
            return List.of();
        }
        return Arrays.stream(authoritiesStr.split(","))
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /** 从 Authorization 头中提取纯 token */
    public String extractToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(properties.getTokenPrefix())) {
            return bearerToken.substring(properties.getTokenPrefix().length());
        }
        return bearerToken;
    }

    public String getHeader() {
        return properties.getHeader();
    }

    public String getTokenPrefix() {
        return properties.getTokenPrefix();
    }

    public Long getExpiration() {
        return properties.getExpiration();
    }

    public String getSecret() {
        return properties.getSecret();
    }
}
