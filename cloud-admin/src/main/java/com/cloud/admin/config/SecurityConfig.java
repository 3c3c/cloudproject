package com.cloud.admin.config;

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
 * 后台管理服务（cloud-admin）安全配置：解析网关透传的 X-User-* 头构建认证上下文。
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
                        .requestMatchers("/actuator/**").permitAll()
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
