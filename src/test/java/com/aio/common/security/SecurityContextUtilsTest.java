package com.aio.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全上下文工具类测试
 */
class SecurityContextUtilsTest {

    private UUID testUserId;
    private String testRole;

    @BeforeEach
    void setUp() {
        // 清空安全上下文
        SecurityContextHolder.clearContext();

        testUserId = UUID.randomUUID();
        testRole = "USER";
    }

    @AfterEach
    void tearDown() {
        // 测试后清空安全上下文
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserIdWhenAuthenticated() {
        // 设置认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testRole))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试获取当前用户ID
        UUID userId = SecurityContextUtils.getCurrentUserId();

        assertNotNull(userId);
        assertEquals(testUserId, userId);
    }

    @Test
    void testGetCurrentUserIdWhenNotAuthenticated() {
        // 测试未认证时获取用户ID
        UUID userId = SecurityContextUtils.getCurrentUserId();
        assertNull(userId);
    }

    @Test
    void testGetCurrentUserIdWhenAnonymous() {
        // 设置匿名用户
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "anonymousUser",
                        null,
                        Collections.emptyList()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试匿名用户时获取用户ID
        UUID userId = SecurityContextUtils.getCurrentUserId();
        assertNull(userId);
    }

    @Test
    void testGetCurrentUserIdWithInvalidUUID() {
        // 设置无效的UUID格式
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "invalid-uuid",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试无效UUID时返回null
        UUID userId = SecurityContextUtils.getCurrentUserId();
        assertNull(userId);
    }

    @Test
    void testGetCurrentUserRoleWhenAuthenticated() {
        // 设置认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testRole))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试获取当前用户角色
        String role = SecurityContextUtils.getCurrentUserRole();

        assertNotNull(role);
        assertEquals(testRole, role);
    }

    @Test
    void testGetCurrentUserRoleWhenNotAuthenticated() {
        // 测试未认证时获取角色
        String role = SecurityContextUtils.getCurrentUserRole();
        assertNull(role);
    }

    @Test
    void testGetCurrentUserRoleWithoutRolePrefix() {
        // 测试角色名已移除ROLE_前缀
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityContextUtils.getCurrentUserRole();

        assertEquals("ADMIN", role);
        assertFalse(role.startsWith("ROLE_"));
    }

    @Test
    void testIsAuthenticatedWhenAuthenticated() {
        // 设置认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试是否已认证
        assertTrue(SecurityContextUtils.isAuthenticated());
    }

    @Test
    void testIsAuthenticatedWhenNotAuthenticated() {
        // 测试未认证
        assertFalse(SecurityContextUtils.isAuthenticated());
    }

    @Test
    void testIsAuthenticatedWhenAnonymous() {
        // 设置匿名用户
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "anonymousUser",
                        null,
                        Collections.emptyList()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试匿名用户未认证
        assertFalse(SecurityContextUtils.isAuthenticated());
    }

    @Test
    void testHasRoleWhenUserHasRole() {
        // 设置认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试用户具有指定角色
        assertTrue(SecurityContextUtils.hasRole("ADMIN"));
    }

    @Test
    void testHasRoleWhenUserDoesNotHaveRole() {
        // 设置认证信息
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUserId.toString(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 测试用户不具有指定角色
        assertFalse(SecurityContextUtils.hasRole("ADMIN"));
    }

    @Test
    void testHasRoleWhenNotAuthenticated() {
        // 测试未认证时不具有任何角色
        assertFalse(SecurityContextUtils.hasRole("USER"));
        assertFalse(SecurityContextUtils.hasRole("ADMIN"));
    }
}

