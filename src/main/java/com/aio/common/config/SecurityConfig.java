package com.aio.common.config;

import com.aio.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF：当前系统使用 JWT 无状态认证（无 Session、JWT 放在 Authorization 头）
            // 详细说明：本系统采用基于JWT的无状态认证，所有认证信息通过Authorization头部传递，未使用Session或Cookie存储认证信息。
            // 由于CSRF攻击主要针对基于Cookie的认证机制，而JWT令牌不会被浏览器自动附加到跨域请求，因此CSRF保护在此架构下不是必需的。
            .csrf(AbstractHttpConfigurer::disable)

            // 配置会话管理为无状态（使用JWT不需要session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开接口：注册和登录
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                // Swagger文档相关路径
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                // 错误页面和健康检查
                .requestMatchers("/error", "/actuator/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )

            // 添加JWT过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
