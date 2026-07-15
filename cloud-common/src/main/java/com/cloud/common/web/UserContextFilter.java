package com.cloud.common.web;

import com.cloud.common.constant.RedisConstants;
import com.cloud.common.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务服务公共过滤器：解析网关透传的 X-User-* 请求头，构建认证上下文（aaa.md 网关方案二）。
 * <p>
 * 本类不标注 @Component，由各业务服务的 {@code SecurityConfig} 显式注册到过滤器链，
 * 以保证过滤器顺序可控。
 */
public class UserContextFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    public UserContextFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String userId = request.getHeader(SecurityConstants.HEADER_USER_ID);
        String username = request.getHeader(SecurityConstants.HEADER_USERNAME);

        if (StringUtils.hasText(username)) {
            // 从Redis中获取用户权限缓存
            String authorities = redisTemplate.opsForValue().get(RedisConstants.userPermissionKey(username));

            List<SimpleGrantedAuthority> authorityList = StringUtils.hasText(authorities)
                    ? Arrays.stream(authorities.split(","))
                        .filter(StringUtils::hasText)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                    : Collections.emptyList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorityList);
            // userId 通过 details 传递，供 SecurityUtils.getCurrentUserId() 取用
            authentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
