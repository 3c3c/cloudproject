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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证服务内部使用的 JWT 过滤器（aaa.md 第一节）：
 * 1) 校验签名 + 有效期
 * 2) 校验黑名单（退出后失效）
 * 3) Redis 双重校验：login:token:{username} 必须与请求 token 一致（单点登录 / 强制下线）
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final UserDetailsServiceImpl userDetailsService;

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

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
