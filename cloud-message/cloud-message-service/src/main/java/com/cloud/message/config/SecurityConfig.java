package com.cloud.message.config;

import com.cloud.common.result.ResultCode;
import com.cloud.common.web.SecurityResponseUtil;
import com.cloud.common.web.UserContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 消息服务安全配置。
 *
 * <p>引入 cloud-common-security 后默认开启 Spring Security 拦截，这里需显式配置：
 * <ul>
 *   <li>{@code /message/send}：业务接口，解析网关透传的 X-User-* 头构建认证上下文（UserContextFilter）；</li>
 *   <li>{@code /ws/**}：WebSocket 握手由 {@code WebSocketInterceptor} 自行用 JwtUtils 校验，
 *       不走 SecurityFilterChain，故直接放行；</li>
 *   <li>actuator：监控端点放行。</li>
 * </ul></p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final StringRedisTemplate stringRedisTemplate;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // WebSocket 握手由 WebSocketInterceptor 用 JwtUtils 自行校验，放行以免被 Security 阻断
                        .requestMatchers("/ws/**").permitAll()
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
