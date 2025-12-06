package com.aio.common.security;

import com.aio.api.model.ModelApiResponse;
import com.aio.common.enums.ExceptionEnum;
import com.aio.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT令牌，验证并设置Spring Security上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    // 无需JWT认证的公开路径
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/user/register",
        "/api/user/login",
        "/error",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        // 如果是公开路径，直接跳过JWT认证
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 从请求头获取JWT令牌
            String token = extractTokenFromHeader(request);

            // 如果令牌无效
            if (token == null || !jwtUtils.validateToken(token)) {
                sendErrorResponse(response, ExceptionEnum.AUTHORIZATION_FAILED);
                return;
            }

            // 从令牌中提取用户信息
            String userId = jwtUtils.getUserIdFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);

            if (userId != null && role != null) {
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("已设置JWT认证，用户ID: {}, 角色: {}", userId, role);
            } else {
                sendErrorResponse(response, ExceptionEnum.AUTHORIZATION_FAILED);
                return;
            }
        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            sendErrorResponse(response, ExceptionEnum.AUTHORIZATION_FAILED);
            return;
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String requestPath) {
        return PUBLIC_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    /**
     * 从请求头中提取JWT令牌
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, ExceptionEnum exceptionEnum) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(exceptionEnum.getCode());

        ModelApiResponse apiResponse = new ModelApiResponse();
        apiResponse.setCode(exceptionEnum.getCode());
        apiResponse.setMsg(exceptionEnum.getMessage());
        apiResponse.setSuccess(false);
        apiResponse.setData(null);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}