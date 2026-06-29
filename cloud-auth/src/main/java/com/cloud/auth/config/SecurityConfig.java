package com.cloud.auth.config;

import com.cloud.auth.dto.login.LoginResponse;
import com.cloud.auth.security.*;
import com.cloud.common.result.Result;
import com.cloud.common.result.ResultCode;
import com.cloud.common.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证服务安全配置（Spring Security 6 lambda DSL）。
 * - STATELESS，关闭 CSRF
 * - 放行登录/注册/发验证码；其余需认证
 * - 接入 JWT 过滤器、手机号验证码登录过滤器
 * - 401/403 统一 JSON 返回
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] WHITELIST = {
            "/auth/login",
            "/auth/public-key",
            "/auth/sms/send",
            "/auth/sms/login",
            "/auth/register",
            "/actuator/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MobileAuthenticationProvider mobileAuthenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 手机号验证码登录 Filter
        AuthenticationConfiguration authConfig = http.getSharedObject(AuthenticationConfiguration.class);
        MobileAuthenticationFilter mobileFilter = new MobileAuthenticationFilter();
        mobileFilter.setAuthenticationManager(authenticationManager(authConfig));
        mobileFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            LoginResponse resp = tokenService.issueToken(loginUser);
            writeJson(response, Result.ok(resp), HttpServletResponse.SC_OK);
        });
        mobileFilter.setAuthenticationFailureHandler((request, response, exception) ->
                writeJson(response, Result.fail(ResultCode.SMS_CODE_ERROR, exception.getMessage()),
                        HttpServletResponse.SC_UNAUTHORIZED));

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authenticationProvider(daoAuthenticationProvider())
                .authenticationProvider(mobileAuthenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(mobileFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void writeJson(HttpServletResponse response, Object body, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
