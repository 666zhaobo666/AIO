package com.aio.common.security;

import com.aio.api.model.ModelApiResponse;
import com.aio.common.enums.ExceptionEnum;
import com.aio.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 单元测试")
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

    private StringWriter currentResponseWriter;

    @BeforeEach
    void setUp() {
        // 清空 SecurityContext
        SecurityContextHolder.clearContext();
        currentResponseWriter = null;
    }

    /**
     * 设置响应 Writer（仅在需要时调用）
     */
    private void setupResponseWriter() throws IOException {
        currentResponseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(currentResponseWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    /**
     * 获取响应内容
     */
    private String getResponseContent() {
        if (currentResponseWriter != null) {
            return currentResponseWriter.toString();
        }
        return "";
    }

    @Nested
    @DisplayName("公开路径测试")
    class PublicPathTests {

        @Test
        @DisplayName("注册路径 - 跳过JWT认证")
        void publicPath_Register_SkipAuthentication() throws ServletException, IOException {
            // Arrange
            when(request.getRequestURI()).thenReturn("/api/user/register");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            verify(jwtUtils, never()).validateToken(any());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("登录路径 - 跳过JWT认证")
        void publicPath_Login_SkipAuthentication() throws ServletException, IOException {
            // Arrange
            when(request.getRequestURI()).thenReturn("/api/user/login");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            verify(jwtUtils, never()).validateToken(any());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @ParameterizedTest(name = "公共路径[{index}] - 跳过JWT认证: {0}")
        @ValueSource(strings = {
            "/error",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/actuator/health"
        })
        @DisplayName("公共/白名单路径 - 跳过JWT认证")
        void publicPaths_SkipAuthentication(String publicPath) throws ServletException, IOException {
            // Arrange
            when(request.getRequestURI()).thenReturn(publicPath);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);
            // 验证确实跳过了 token 校验逻辑
            verify(jwtUtils, never()).validateToken(any());
        }
    }

    @Nested
    @DisplayName("JWT令牌提取测试")
    class TokenExtractionTests {

        @Test
        @DisplayName("缺少Authorization头 - 认证失败")
        void noAuthorizationHeader_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
            verify(response).setContentType("application/json;charset=UTF-8");

            String responseContent = getResponseContent();
            assertTrue(responseContent.contains("\"success\":false"));
            assertTrue(responseContent.contains(ExceptionEnum.AUTHORIZATION_FAILED.getMessage()));
        }

        @Test
        @DisplayName("Authorization头格式错误 - 没有Bearer前缀")
        void invalidAuthorizationHeader_NoBearerPrefix() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("InvalidToken");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("Authorization头格式正确 - 有Bearer前缀")
        void validAuthorizationHeader_BearerPrefix() throws ServletException, IOException {
            // Arrange
            String token = "validToken123";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(token)).thenReturn("USER");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateToken(token);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("JWT令牌验证测试")
    class TokenValidationTests {

        @Test
        @DisplayName("令牌无效 - 认证失败")
        void invalidToken_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            String token = "invalidToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateToken(token);
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("令牌有效但userId为空 - 认证失败")
        void validToken_NullUserId_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            String token = "validToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn(null);
            when(jwtUtils.getRoleFromToken(token)).thenReturn("USER");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("令牌有效但role为空 - 认证失败")
        void validToken_NullRole_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            String token = "validToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(token)).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("JWT工具类抛出异常 - 认证失败")
        void jwtUtilsThrowsException_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            String token = "exceptionToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenThrow(new RuntimeException("JWT parsing error"));

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }
    }

    @Nested
    @DisplayName("认证成功测试")
    class SuccessfulAuthenticationTests {

        @Test
        @DisplayName("普通用户认证成功 - 设置SecurityContext")
        void validToken_UserRole_AuthenticationSuccess() throws ServletException, IOException {
            // Arrange
            String token = "validUserToken";
            String userId = "1";
            String role = "USER";

            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
            when(jwtUtils.getRoleFromToken(token)).thenReturn(role);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertEquals(userId, authentication.getPrincipal());
            assertNull(authentication.getCredentials());
            assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        }

        @Test
        @DisplayName("管理员认证成功 - 设置SecurityContext")
        void validToken_AdminRole_AuthenticationSuccess() throws ServletException, IOException {
            // Arrange
            String token = "validAdminToken";
            String userId = "2";
            String role = "ADMIN";

            when(request.getRequestURI()).thenReturn("/api/admin/users");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
            when(jwtUtils.getRoleFromToken(token)).thenReturn(role);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain).doFilter(request, response);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertEquals(userId, authentication.getPrincipal());
            assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("认证成功 - 验证Authentication Details设置")
        void validToken_AuthenticationDetailsSet() throws ServletException, IOException {
            // Arrange
            String token = "validToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(token)).thenReturn("USER");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertNotNull(authentication.getDetails());
        }
    }

    @Nested
    @DisplayName("错误响应测试")
    class ErrorResponseTests {

        @Test
        @DisplayName("认证失败 - 返回正确的错误响应格式")
        void authenticationFailed_CorrectErrorResponse() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setContentType("application/json;charset=UTF-8");
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());

            String responseContent = getResponseContent();
            assertFalse(responseContent.isEmpty());

            // 验证响应内容包含必要的字段
            assertTrue(responseContent.contains("\"code\":" + ExceptionEnum.AUTHORIZATION_FAILED.getCode()));
            assertTrue(responseContent.contains("\"msg\":\"" + ExceptionEnum.AUTHORIZATION_FAILED.getMessage() + "\""));
            assertTrue(responseContent.contains("\"success\":false"));
        }

        @Test
        @DisplayName("认证失败 - 验证响应可以被解析为ModelApiResponse")
        void authenticationFailed_ResponseParseable() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            String responseContent = getResponseContent();
            ObjectMapper objectMapper = new ObjectMapper();

            // 验证可以解析为 ModelApiResponse
            assertDoesNotThrow(() -> {
                ModelApiResponse apiResponse = objectMapper.readValue(responseContent, ModelApiResponse.class);
                assertEquals(ExceptionEnum.AUTHORIZATION_FAILED.getCode(), apiResponse.getCode());
                assertEquals(ExceptionEnum.AUTHORIZATION_FAILED.getMessage(), apiResponse.getMsg());
                assertFalse(apiResponse.getSuccess());
            });
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("Bearer后面没有空格 - 认证失败")
        void bearerWithoutSpace_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("BearerToken123");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("Bearer前缀大小写敏感 - 小写bearer无效")
        void bearerLowerCase_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("bearer validToken");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("只有Bearer没有令牌 - 认证失败")
        void bearerOnly_AuthenticationFailed() throws ServletException, IOException {
            // Arrange
            setupResponseWriter();
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer ");
            when(jwtUtils.validateToken("")).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, never()).doFilter(request, response);
            verify(response).setStatus(ExceptionEnum.AUTHORIZATION_FAILED.getCode());
        }

        @Test
        @DisplayName("令牌包含特殊字符 - 正常处理")
        void tokenWithSpecialCharacters_Processed() throws ServletException, IOException {
            // Arrange
            String token = "token.with.dots-and_underscores";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(token)).thenReturn("USER");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtUtils).validateToken(token);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("多次请求测试")
    class MultipleRequestTests {

        @Test
        @DisplayName("连续的公开路径请求 - 不影响后续认证")
        void consecutivePublicRequests_NoImpact() throws ServletException, IOException {
            // 第一个请求 - 公开路径
            when(request.getRequestURI()).thenReturn("/api/user/login");
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // 清空SecurityContext
            SecurityContextHolder.clearContext();

            // 第二个请求 - 需要认证
            String token = "validToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtUtils.validateToken(token)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(token)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(token)).thenReturn("USER");

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(authentication);
            assertEquals("1", authentication.getPrincipal());
        }

        @Test
        @DisplayName("认证失败后SecurityContext应该保持清空状态")
        void authenticationFailed_SecurityContextCleared() throws ServletException, IOException {
            // 先设置一个有效的认证
            setupResponseWriter();
            String validToken = "validToken";
            when(request.getRequestURI()).thenReturn("/api/user/profile");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(jwtUtils.validateToken(validToken)).thenReturn(true);
            when(jwtUtils.getUserIdFromToken(validToken)).thenReturn("1");
            when(jwtUtils.getRoleFromToken(validToken)).thenReturn("USER");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());

            // 清空并尝试无效令牌
            SecurityContextHolder.clearContext();
            when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
            when(jwtUtils.validateToken("invalidToken")).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }
}

