package com.aio.common.config;

import com.aio.common.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全配置测试
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testSecurityConfigLoaded() {
        // 验证SecurityConfig被正确加载
        assertNotNull(securityConfig);
    }

    @Test
    void testPasswordEncoderBean() {
        // 验证PasswordEncoder Bean存在
        assertNotNull(passwordEncoder);
    }

    @Test
    void testPasswordEncoderEncryption() {
        // 测试密码加密功能
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    void testPasswordEncoderDifferentResults() {
        // 验证相同密码两次加密结果不同（BCrypt特性）
        String rawPassword = "testPassword123";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    void testSecurityFilterChainBean() {
        // 验证SecurityFilterChain Bean存在
        assertNotNull(securityFilterChain);
    }

    @Test
    void testJwtAuthenticationFilterInjected() {
        // 验证JwtAuthenticationFilter被正确注入
        assertNotNull(jwtAuthenticationFilter);
    }

    @Test
    void testPasswordEncoderWrongPassword() {
        // 测试错误密码不匹配
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encoded = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encoded));
    }
}

