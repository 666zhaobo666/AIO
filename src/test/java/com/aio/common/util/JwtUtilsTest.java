package com.aio.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils 单元测试")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // 测试用的密钥（至少32字节，256位）
    private static final String TEST_SECRET = "ThisIsAVeryLongSecretKeyForTestingJwtTokenGeneration123456";
    private static final int TEST_EXPIRE_DAYS = 7;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(jwtUtils, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expireDays", TEST_EXPIRE_DAYS);
    }

    @Nested
    @DisplayName("generateToken 生成令牌测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("成功生成令牌")
        void generateToken_Success() {
            // Arrange
            Integer userId = 1;
            String role = "USER";

            // Act
            String token = jwtUtils.generateToken(userId, role);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(3, token.split("\\.").length); // JWT格式：header.payload.signature

        }

        @Test
        @DisplayName("生成令牌包含正确的userId")
        void generateToken_ContainsUserId() {
            // Arrange
            Integer userId = 123;
            String role = "USER";

            // Act
            String token = jwtUtils.generateToken(userId, role);
            String extractedUserId = jwtUtils.getUserIdFromToken(token);

            // Assert
            assertEquals(String.valueOf(userId), extractedUserId);
        }

        @Test
        @DisplayName("生成令牌包含正确的role")
        void generateToken_ContainsRole() {
            // Arrange
            Integer userId = 1;
            String role = "ADMIN";

            // Act
            String token = jwtUtils.generateToken(userId, role);
            String extractedRole = jwtUtils.getRoleFromToken(token);

            // Assert
            assertEquals(role, extractedRole);
        }

        @Test
        @DisplayName("不同用户生成不同令牌")
        void generateToken_DifferentUsersGetDifferentTokens() {
            // Arrange
            Integer userId1 = 1;
            Integer userId2 = 2;
            String role = "USER";

            // Act
            String token1 = jwtUtils.generateToken(userId1, role);
            String token2 = jwtUtils.generateToken(userId2, role);

            // Assert
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken 解析用户ID测试")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("成功解析用户ID")
        void getUserIdFromToken_Success() {
            // Arrange
            Integer userId = 456;
            String role = "USER";
            String token = jwtUtils.generateToken(userId, role);

            // Act
            String extractedUserId = jwtUtils.getUserIdFromToken(token);

            // Assert
            assertEquals(String.valueOf(userId), extractedUserId);
        }

        @Test
        @DisplayName("无效令牌返回null")
        void getUserIdFromToken_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act
            String extractedUserId = jwtUtils.getUserIdFromToken(invalidToken);

            // Assert
            assertNull(extractedUserId);
        }

        @Test
        @DisplayName("空令牌返回null")
        void getUserIdFromToken_EmptyToken() {
            // Arrange
            String emptyToken = "";

            // Act
            String extractedUserId = jwtUtils.getUserIdFromToken(emptyToken);

            // Assert
            assertNull(extractedUserId);
        }
    }

    @Nested
    @DisplayName("getRoleFromToken 解析角色测试")
    class GetRoleFromTokenTests {

        @Test
        @DisplayName("成功解析用户角色")
        void getRoleFromToken_Success() {
            // Arrange
            Integer userId = 1;
            String role = "ADMIN";
            String token = jwtUtils.generateToken(userId, role);

            // Act
            String extractedRole = jwtUtils.getRoleFromToken(token);

            // Assert
            assertEquals(role, extractedRole);
        }

        @Test
        @DisplayName("解析普通用户角色")
        void getRoleFromToken_UserRole() {
            // Arrange
            Integer userId = 1;
            String role = "USER";
            String token = jwtUtils.generateToken(userId, role);

            // Act
            String extractedRole = jwtUtils.getRoleFromToken(token);

            // Assert
            assertEquals(role, extractedRole);
        }

        @Test
        @DisplayName("无效令牌返回null")
        void getRoleFromToken_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act
            String extractedRole = jwtUtils.getRoleFromToken(invalidToken);

            // Assert
            assertNull(extractedRole);
        }
    }

    @Nested
    @DisplayName("validateToken 验证令牌测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("有效令牌验证通过")
        void validateToken_ValidToken() {
            // Arrange
            Integer userId = 1;
            String role = "USER";
            String token = jwtUtils.generateToken(userId, role);

            // Act
            boolean isValid = jwtUtils.validateToken(token);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("无效令牌验证失败")
        void validateToken_InvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.here";

            // Act
            boolean isValid = jwtUtils.validateToken(invalidToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("空令牌验证失败")
        void validateToken_EmptyToken() {
            // Arrange
            String emptyToken = "";

            // Act
            boolean isValid = jwtUtils.validateToken(emptyToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("过期令牌验证失败")
        void validateToken_ExpiredToken() {
            // 创建一个已过期的令牌
            JwtUtils expiredJwtUtils = new JwtUtils();
            ReflectionTestUtils.setField(expiredJwtUtils, "secret", TEST_SECRET);
            ReflectionTestUtils.setField(expiredJwtUtils, "expireDays", -1); // 负数使其立即过期

            // 生成一个"过期"的令牌（通过直接构建）
            SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expiredDate = new Date(now.getTime() - 1000); // 已过期1秒

            String expiredToken = Jwts.builder()
                    .claim("userId", 1)
                    .claim("role", "USER")
                    .subject("1")
                    .issuedAt(new Date(now.getTime() - 10000))
                    .expiration(expiredDate)
                    .signWith(key)
                    .compact();

            // Act
            boolean isValid = jwtUtils.validateToken(expiredToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("篡改的令牌验证失败")
        void validateToken_TamperedToken() {
            // Arrange
            Integer userId = 1;
            String role = "USER";
            String token = jwtUtils.generateToken(userId, role);

            // 篡改令牌（修改payload部分）
            String[] parts = token.split("\\.");
            String tamperedPayload = parts[1] + "xyz";
            String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

            // Act
            boolean isValid = jwtUtils.validateToken(tamperedToken);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("使用错误密钥签名的令牌验证失败")
        void validateToken_WrongSecretToken() {
            // 使用不同密钥生成令牌
            String wrongSecret = "DifferentSecretKeyForTestingPurposes123456789";
            SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

            Date now = new Date();
            Date expiration = new Date(now.getTime() + 3600000); // 1小时后

            String tokenWithWrongSecret = Jwts.builder()
                    .claim("userId", 1)
                    .claim("role", "USER")
                    .subject("1")
                    .issuedAt(now)
                    .expiration(expiration)
                    .signWith(wrongKey)
                    .compact();

            // Act
            boolean isValid = jwtUtils.validateToken(tokenWithWrongSecret);

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("综合测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整流程：生成-解析-验证")
        void fullFlow_GenerateParseValidate() {
            // Arrange
            Integer userId = 999;
            String role = "ADMIN";

            // Act - 生成
            String token = jwtUtils.generateToken(userId, role);

            // Act - 解析
            String extractedUserId = jwtUtils.getUserIdFromToken(token);
            String extractedRole = jwtUtils.getRoleFromToken(token);

            // Act - 验证
            boolean isValid = jwtUtils.validateToken(token);

            // Assert
            assertNotNull(token);
            assertEquals(String.valueOf(userId), extractedUserId);
            assertEquals(role, extractedRole);
            assertTrue(isValid);
        }

        @Test
        @DisplayName("多次生成令牌稳定性")
        void multipleGenerations_Stable() {
            // Arrange
            Integer userId = 1;
            String role = "USER";

            // Act & Assert
            for (int i = 0; i < 10; i++) {
                String token = jwtUtils.generateToken(userId, role);
                assertNotNull(token);
                assertTrue(jwtUtils.validateToken(token));
                assertEquals(String.valueOf(userId), jwtUtils.getUserIdFromToken(token));
                assertEquals(role, jwtUtils.getRoleFromToken(token));
            }
        }

        @Test
        @DisplayName("不同角色令牌正确解析")
        void differentRoles_CorrectlyParsed() {
            // Arrange
            String[] roles = {"USER", "ADMIN"};
            Integer userId = 1;

            // Act & Assert
            for (String role : roles) {
                String token = jwtUtils.generateToken(userId, role);
                assertEquals(role, jwtUtils.getRoleFromToken(token));
            }
        }
    }
}

