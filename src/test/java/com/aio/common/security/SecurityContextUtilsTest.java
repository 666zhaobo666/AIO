package com.aio.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityContextUtils 单元测试")
class SecurityContextUtilsTest {

    @BeforeEach
    void setUp() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // 测试后清除安全上下文
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Object principal, List<SimpleGrantedAuthority> authorities) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("getCurrentUserId 获取当前用户ID测试")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("成功获取用户ID")
        void getCurrentUserId_Success() {
            // Arrange
            setAuthentication("123", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            Integer userId = SecurityContextUtils.getCurrentUserId();

            // Assert
            assertEquals(123, userId);
        }

        @Test
        @DisplayName("用户未认证时返回null")
        void getCurrentUserId_NotAuthenticated() {
            // 不设置任何认证信息

            // Act
            Integer userId = SecurityContextUtils.getCurrentUserId();

            // Assert
            assertNull(userId);
        }

        @Test
        @DisplayName("匿名用户时返回null")
        void getCurrentUserId_AnonymousUser() {
            // Arrange
            setAuthentication("anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

            // Act
            Integer userId = SecurityContextUtils.getCurrentUserId();

            // Assert
            assertNull(userId);
        }

        @Test
        @DisplayName("principal不是有效数字时返回null")
        void getCurrentUserId_InvalidPrincipal() {
            // Arrange
            setAuthentication("invalidPrincipal", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            Integer userId = SecurityContextUtils.getCurrentUserId();

            // Assert
            assertNull(userId);
        }

        @Test
        @DisplayName("空Authentication时返回null")
        void getCurrentUserId_NullAuthentication() {
            // Arrange - 设置空的SecurityContext
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(null);
            SecurityContextHolder.setContext(context);

            // Act
            Integer userId = SecurityContextUtils.getCurrentUserId();

            // Assert
            assertNull(userId);
        }
    }

    @Nested
    @DisplayName("getCurrentUserRole 获取当前用户角色测试")
    class GetCurrentUserRoleTests {

        @Test
        @DisplayName("成功获取用户角色 - USER")
        void getCurrentUserRole_User() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertEquals("USER", role);
        }

        @Test
        @DisplayName("成功获取用户角色 - ADMIN")
        void getCurrentUserRole_Admin() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertEquals("ADMIN", role);
        }

        @Test
        @DisplayName("用户未认证时返回null")
        void getCurrentUserRole_NotAuthenticated() {
            // 不设置任何认证信息

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertNull(role);
        }

        @Test
        @DisplayName("没有ROLE_前缀的权限返回null")
        void getCurrentUserRole_NoRolePrefix() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("USER")));

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertNull(role);
        }

        @Test
        @DisplayName("多个权限时返回第一个ROLE_角色")
        void getCurrentUserRole_MultipleAuthorities() {
            // Arrange
            setAuthentication("1", List.of(
                    new SimpleGrantedAuthority("READ"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            ));

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertEquals("ADMIN", role); // 返回第一个匹配的ROLE_
        }

        @Test
        @DisplayName("空权限列表时返回null")
        void getCurrentUserRole_EmptyAuthorities() {
            // Arrange
            setAuthentication("1", Collections.emptyList());

            // Act
            String role = SecurityContextUtils.getCurrentUserRole();

            // Assert
            assertNull(role);
        }
    }

    @Nested
    @DisplayName("isAuthenticated 检查认证状态测试")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("已认证用户返回true")
        void isAuthenticated_AuthenticatedUser() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            boolean result = SecurityContextUtils.isAuthenticated();

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("未认证用户返回false")
        void isAuthenticated_NotAuthenticated() {
            // 不设置任何认证信息

            // Act
            boolean result = SecurityContextUtils.isAuthenticated();

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("匿名用户返回false")
        void isAuthenticated_AnonymousUser() {
            // Arrange
            setAuthentication("anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

            // Act
            boolean result = SecurityContextUtils.isAuthenticated();

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("空Authentication返回false")
        void isAuthenticated_NullAuthentication() {
            // Arrange
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(null);
            SecurityContextHolder.setContext(context);

            // Act
            boolean result = SecurityContextUtils.isAuthenticated();

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("hasRole 检查角色权限测试")
    class HasRoleTests {

        @Test
        @DisplayName("用户拥有指定角色返回true")
        void hasRole_HasRole() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            // Act
            boolean result = SecurityContextUtils.hasRole("ADMIN");

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("用户没有指定角色返回false")
        void hasRole_DoesNotHaveRole() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            boolean result = SecurityContextUtils.hasRole("ADMIN");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("未认证用户返回false")
        void hasRole_NotAuthenticated() {
            // 不设置任何认证信息

            // Act
            boolean result = SecurityContextUtils.hasRole("USER");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("多个角色中包含指定角色返回true")
        void hasRole_MultipleRolesContainsTarget() {
            // Arrange
            setAuthentication("1", List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            ));

            // Act
            boolean hasUser = SecurityContextUtils.hasRole("USER");
            boolean hasAdmin = SecurityContextUtils.hasRole("ADMIN");

            // Assert
            assertTrue(hasUser);
            assertTrue(hasAdmin);
        }

        @Test
        @DisplayName("空Authentication返回false")
        void hasRole_NullAuthentication() {
            // Arrange
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(null);
            SecurityContextHolder.setContext(context);

            // Act
            boolean result = SecurityContextUtils.hasRole("USER");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("大小写敏感测试")
        void hasRole_CaseSensitive() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Act
            boolean hasUpperCase = SecurityContextUtils.hasRole("USER");
            boolean hasLowerCase = SecurityContextUtils.hasRole("user");

            // Assert
            assertTrue(hasUpperCase);
            assertFalse(hasLowerCase);
        }
    }

    @Nested
    @DisplayName("综合测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整认证流程 - 普通用户")
        void fullFlow_RegularUser() {
            // Arrange
            setAuthentication("456", List.of(new SimpleGrantedAuthority("ROLE_USER")));

            // Assert
            assertTrue(SecurityContextUtils.isAuthenticated());
            assertEquals(456, SecurityContextUtils.getCurrentUserId());
            assertEquals("USER", SecurityContextUtils.getCurrentUserRole());
            assertTrue(SecurityContextUtils.hasRole("USER"));
            assertFalse(SecurityContextUtils.hasRole("ADMIN"));
        }

        @Test
        @DisplayName("完整认证流程 - 管理员")
        void fullFlow_Admin() {
            // Arrange
            setAuthentication("1", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            // Assert
            assertTrue(SecurityContextUtils.isAuthenticated());
            assertEquals(1, SecurityContextUtils.getCurrentUserId());
            assertEquals("ADMIN", SecurityContextUtils.getCurrentUserRole());
            assertTrue(SecurityContextUtils.hasRole("ADMIN"));
            assertFalse(SecurityContextUtils.hasRole("USER"));
        }

        @Test
        @DisplayName("完整认证流程 - 未认证")
        void fullFlow_NotAuthenticated() {
            // 不设置任何认证信息

            // Assert
            assertFalse(SecurityContextUtils.isAuthenticated());
            assertNull(SecurityContextUtils.getCurrentUserId());
            assertNull(SecurityContextUtils.getCurrentUserRole());
            assertFalse(SecurityContextUtils.hasRole("USER"));
            assertFalse(SecurityContextUtils.hasRole("ADMIN"));
        }
    }
}

