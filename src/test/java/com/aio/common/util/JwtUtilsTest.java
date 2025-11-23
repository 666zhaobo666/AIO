package com.aio.common.util;

import com.aio.common.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtProperties jwtProperties;

    private String testUserId;
    private String testRole;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testRole = "USER";
        testToken = jwtUtils.generateToken(testUserId, testRole);
    }

    @Test
    void testGenerateToken() {
        // 测试生成令牌
        String token = jwtUtils.generateToken(testUserId, testRole);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length, "JWT格式应为header.payload.signature");
    }

    @Test
    void testGetUserIdFromToken() {
        // 测试从令牌中提取用户ID
        String userId = jwtUtils.getUserIdFromToken(testToken);

        assertNotNull(userId);
        assertEquals(testUserId, userId);
    }

    @Test
    void testGetRoleFromToken() {
        // 测试从令牌中提取角色
        String role = jwtUtils.getRoleFromToken(testToken);

        assertNotNull(role);
        assertEquals(testRole, role);
    }

    @Test
    void testValidateValidToken() {
        // 测试验证有效令牌
        assertTrue(jwtUtils.validateToken(testToken));
    }

    @Test
    void testValidateInvalidToken() {
        // 测试验证无效令牌
        String invalidToken = "invalid.token.here";
        assertFalse(jwtUtils.validateToken(invalidToken));
    }

    @Test
    void testValidateNullToken() {
        // 测试验证空令牌
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    void testValidateEmptyToken() {
        // 测试验证空字符串令牌
        assertFalse(jwtUtils.validateToken(""));
    }

    @Test
    void testGetUserIdFromInvalidToken() {
        // 测试从无效令牌中提取用户ID
        String userId = jwtUtils.getUserIdFromToken("invalid.token");
        assertNull(userId);
    }

    @Test
    void testGetRoleFromInvalidToken() {
        // 测试从无效令牌中提取角色
        String role = jwtUtils.getRoleFromToken("invalid.token");
        assertNull(role);
    }

    @Test
    void testGenerateTokenWithDifferentRoles() {
        // 测试生成不同角色的令牌
        String adminToken = jwtUtils.generateToken(testUserId, "ADMIN");
        String userToken = jwtUtils.generateToken(testUserId, "USER");

        assertNotEquals(adminToken, userToken);
        assertEquals("ADMIN", jwtUtils.getRoleFromToken(adminToken));
        assertEquals("USER", jwtUtils.getRoleFromToken(userToken));
    }

    @Test
    void testGenerateTokenWithDifferentUserIds() {
        // 测试生成不同用户ID的令牌
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        String token1 = jwtUtils.generateToken(userId1, testRole);
        String token2 = jwtUtils.generateToken(userId2, testRole);

        assertNotEquals(token1, token2);
        assertEquals(userId1, jwtUtils.getUserIdFromToken(token1));
        assertEquals(userId2, jwtUtils.getUserIdFromToken(token2));
    }

    @Test
    void testTokenContainsRequiredParts() {
        // 验证令牌包含必要的部分
        String[] parts = testToken.split("\\.");
        assertEquals(3, parts.length);

        // 验证每部分都不为空
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void testValidateModifiedToken() {
        // 测试验证被篡改的令牌
        String[] parts = testToken.split("\\.");
        String modifiedToken = parts[0] + ".modified." + parts[2];

        assertFalse(jwtUtils.validateToken(modifiedToken));
    }
}

