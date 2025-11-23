package com.aio.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT配置属性测试
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void testJwtPropertiesLoaded() {
        // 验证JwtProperties被正确加载
        assertNotNull(jwtProperties);
    }

    @Test
    void testDefaultSecretValue() {
        // 验证默认密钥存在且不为空
        assertNotNull(jwtProperties.getSecret());
        assertFalse(jwtProperties.getSecret().isEmpty());
    }

    @Test
    void testDefaultExpireDaysValue() {
        // 验证默认过期天数大于0
        assertTrue(jwtProperties.getExpireDays() > 0);
    }

    @Test
    void testSetSecret() {
        // 测试设置密钥
        String newSecret = "test-secret-key";
        jwtProperties.setSecret(newSecret);
        assertEquals(newSecret, jwtProperties.getSecret());
    }

    @Test
    void testSetExpireDays() {
        // 测试设置过期天数
        int newExpireDays = 30;
        jwtProperties.setExpireDays(newExpireDays);
        assertEquals(newExpireDays, jwtProperties.getExpireDays());
    }

    @Test
    void testSecretMinimumLength() {
        // 验证密钥长度足够（HMAC-SHA256需要至少256位，即32字节）
        assertTrue(jwtProperties.getSecret().length() >= 32,
                "JWT secret should be at least 32 characters for HMAC-SHA256");
    }
}

