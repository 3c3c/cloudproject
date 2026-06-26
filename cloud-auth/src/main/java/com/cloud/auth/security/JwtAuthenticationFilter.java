package com.cloud.auth.security;

import com.cloud.common.constant.RedisConstants;
import com.cloud.common.jwt.JwtUtils;
import com.cloud.common.security.LoginUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务内部使用的 JWT 过滤器（aaa.md 第一节）：
 * 1) 校验签名 + 有效期
 * 2) 校验黑名单（退出后失效）
 * 3) Redis 双重校验：login:token:{username} 必须与请求 token 一致（单点登录 / 强制下线）
 * 4) 从 JWT Claims 构建认证信息（无额外数据库查询，解决循环依赖）
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String bearerToken = request.getHeader(jwtUtils.getHeader());
        String token = jwtUtils.extractToken(bearerToken);

        if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 黑名单校验
        if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisConstants.blacklistKey(token)))) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.getSubject();

        // Redis 双重校验（单点登录 / 强制下线）
        String stored = redisTemplate.opsForValue().get(RedisConstants.loginTokenKey(username));
        if (stored == null || !stored.equals(token)) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ 从 JWT Claims 构建认证信息（不查询数据库，解决循环依赖）
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            LoginUser loginUser = buildLoginUserFromClaims(claims);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    /**
     * 从 JWT Claims 构建 LoginUser（不查询数据库）
     * 这样可以打破循环依赖：Filter 不再依赖 UserDetailsService
     */
    private LoginUser buildLoginUserFromClaims(Claims claims) {
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        String authorities = claims.get("authorities", String.class);

        // 解析 authorities 字符串为权限列表
        List<org.springframework.security.core.GrantedAuthority> authorityList =
                Arrays.stream(authorities.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 构建轻量级 LoginUser（密码字段可为null，因为已通过JWT验证）
        return new LoginUser(userId, username, null, null, authorityList);
    }
}
