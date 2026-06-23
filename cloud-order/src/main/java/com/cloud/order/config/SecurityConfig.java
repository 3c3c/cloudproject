package com.cloud.order.config;

import com.cloud.common.result.ResultCode;
import com.cloud.common.web.SecurityResponseUtil;
import com.cloud.common.web.UserContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 订单服务安全配置：与产品服务一致，解析网关透传的 X-User-* 头构建认证上下文。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

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
                .addFilterBefore(new UserContextFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
