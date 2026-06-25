package com.cloud.auth.service.impl;

import com.cloud.auth.dto.login.LoginRequest;
import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.auth.service.AuthService;
import com.cloud.auth.service.UserService;
import com.cloud.common.constant.RedisConstants;
import com.cloud.common.jwt.JwtUtils;
import com.cloud.common.security.LoginUser;
import com.cloud.common.security.SecurityUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;

    @Override
    public LoginResponse login(LoginRequest req) {
        // Spring Security 认证：内部走 UserDetailsServiceImpl + 密码比对
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return issueToken(loginUser);
    }

    @Override
    public LoginResponse issueToken(LoginUser loginUser) {
        String token = jwtUtils.generateToken(loginUser.getUsername(), loginUser.getUserId(), loginUser.getAuthorities());
        // 写入 Redis：login:token:{username} -> token（覆盖旧值即实现单点登录/强制下线）
        redisTemplate.opsForValue().set(
                RedisConstants.loginTokenKey(loginUser.getUsername()),
                token, jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);

        List<String> authorities = loginUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new LoginResponse(token, jwtUtils.getTokenPrefix(),
                loginUser.getUserId(), loginUser.getUsername(), loginUser.getAvatar(),
                authorities, Boolean.TRUE.equals(loginUser.getMustChangePassword()));
    }

    @Override
    public void logout(String bearerToken) {
        String token = jwtUtils.extractToken(bearerToken);
        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.getSubject();
        redisTemplate.delete(RedisConstants.loginTokenKey(username));
        // 加入黑名单，防止该 token 在剩余有效期内继续使用
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(RedisConstants.blacklistKey(token), "1", ttl, TimeUnit.MILLISECONDS);
        }
        log.info("用户 [{}] 退出登录", username);
    }

    @Override
    public LoginResponse refresh() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        return issueToken(loginUser);
    }
}
