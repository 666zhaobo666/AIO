package com.aio.common.security;

import com.aio.common.util.JwtUtils;
import com.aio.module.user.enums.UserExceptionEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT认证过滤器测试
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String testToken;
    private String testUserId;
    private String testRole;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        testToken = "valid.jwt.token";
        testUserId = UUID.randomUUID().toString();
        testRole = "USER";
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/api/users/register",
        "/api/users/login",
        "/error",
        "/swagger-ui/index.html",
        "/actuator/health",
        "/v3/api-docs/swagger-config"
    })
    void testPublicPaths(String uri) throws ServletException, IOException {
        // 测试公共路径
        when(request.getRequestURI()).thenReturn(uri);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testValidTokenAuthentication() throws ServletException, IOException {
        // 测试有效令牌认证
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);
        when(jwtUtils.validateToken(testToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(testToken)).thenReturn(testUserId);
        when(jwtUtils.getRoleFromToken(testToken)).thenReturn(testRole);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUserId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void testMissingToken() throws ServletException, IOException {
        // 测试缺少令牌
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidTokenFormat() throws ServletException, IOException {
        // 测试无效的令牌格式（不以Bearer开头）
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + testToken);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidToken() throws ServletException, IOException {
        // 测试无效令牌
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);
        when(jwtUtils.validateToken(testToken)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testTokenWithNullUserId() throws ServletException, IOException {
        // 测试令牌中用户ID为空
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);
        when(jwtUtils.validateToken(testToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(testToken)).thenReturn(null);
        when(jwtUtils.getRoleFromToken(testToken)).thenReturn(testRole);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testTokenWithNullRole() throws ServletException, IOException {
        // 测试令牌中角色为空
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);
        when(jwtUtils.validateToken(testToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(testToken)).thenReturn(testUserId);
        when(jwtUtils.getRoleFromToken(testToken)).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testExceptionDuringValidation() throws ServletException, IOException {
        // 测试验证过程中发生异常
        when(request.getRequestURI()).thenReturn("/api/users/password");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + testToken);
        when(jwtUtils.validateToken(testToken)).thenThrow(new RuntimeException("Validation error"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        verify(filterChain, never()).doFilter(request, response);
    }
}

