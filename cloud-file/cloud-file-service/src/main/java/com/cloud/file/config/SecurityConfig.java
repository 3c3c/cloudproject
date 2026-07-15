package com.cloud.file.config;

import com.cloud.common.result.ResultCode;
import com.cloud.common.web.SecurityResponseUtil;
import com.cloud.common.web.UserContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 业务服务安全配置：解析网关透传的 X-User-* 头构建认证上下文。
 * 业务服务不持有 JWT 密钥，鉴权完全依赖网关 + 方法级 @PreAuthorize。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/file/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, resp, e) ->
                                SecurityResponseUtil.write(resp, ResultCode.UNAUTHORIZED))
                        .accessDeniedHandler((req, resp, e) ->
                                SecurityResponseUtil.write(resp, ResultCode.FORBIDDEN))
                )
                .addFilterBefore(new UserContextFilter(stringRedisTemplate), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
