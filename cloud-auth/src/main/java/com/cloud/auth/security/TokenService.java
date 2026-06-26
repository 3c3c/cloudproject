package com.cloud.auth.security;

import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.common.constant.RedisConstants;
import com.cloud.common.jwt.JwtUtils;
import com.cloud.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Token服务：负责JWT生成和Redis存储
 * 专门为Security层提供，避免循环依赖
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    /**
     * 为认证用户生成token
     */
    public LoginResponse issueToken(LoginUser loginUser) {
        String token = jwtUtils.generateToken(
                loginUser.getUsername(),
                loginUser.getUserId(),
                loginUser.getAuthorities()
        );

        // 写入 Redis：login:token:{username} -> token
        redisTemplate.opsForValue().set(
                RedisConstants.loginTokenKey(loginUser.getUsername()),
                token,
                jwtUtils.getExpiration(),
                TimeUnit.MILLISECONDS
        );

        List<String> authorities = loginUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(
                token,
                jwtUtils.getTokenPrefix(),
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getAvatar(),
                authorities,
                Boolean.TRUE.equals(loginUser.getMustChangePassword())
        );
    }
}