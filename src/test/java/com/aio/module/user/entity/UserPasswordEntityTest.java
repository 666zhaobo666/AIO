package com.aio.module.user.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户密码实体测试
 */
class UserPasswordEntityTest {

    private UserPasswordEntity passwordEntity;

    @BeforeEach
    void setUp() {
        passwordEntity = new UserPasswordEntity();
    }

    @Test
    void testDefaultConstructor() {
        // 测试默认构造函数
        assertNotNull(passwordEntity);
        assertNull(passwordEntity.getUserId());
        assertNull(passwordEntity.getPassword());
        assertNotNull(passwordEntity.getCreatedTime()); // 默认创建时间不为空
    }

    @Test
    void testConstructorWithParameters() {
        // 测试带参数的构造函数
        UUID userId = UUID.randomUUID();
        String encodedPassword = "encodedPassword123";

        UserPasswordEntity entity = new UserPasswordEntity(userId, encodedPassword);

        assertNotNull(entity);
        assertEquals(userId, entity.getUserId());
        assertEquals(encodedPassword, entity.getPassword());
        assertNotNull(entity.getUpdatedTime());
    }

    @Test
    void testSetAndGetUserId() {
        // 测试设置和获取用户ID
        UUID userId = UUID.randomUUID();
        passwordEntity.setUserId(userId);
        assertEquals(userId, passwordEntity.getUserId());
    }

    @Test
    void testSetAndGetPassword() {
        // 测试设置和获取密码
        String password = "hashedPassword";
        passwordEntity.setPassword(password);
        assertEquals(password, passwordEntity.getPassword());
    }

    @Test
    void testSetAndGetCreatedTime() {
        // 测试设置和获取创建时间
        LocalDateTime createdTime = LocalDateTime.now();
        passwordEntity.setCreatedTime(createdTime);
        assertEquals(createdTime, passwordEntity.getCreatedTime());
    }

    @Test
    void testSetAndGetUpdatedTime() {
        // 测试设置和获取更新时间
        LocalDateTime updatedTime = LocalDateTime.now();
        passwordEntity.setUpdatedTime(updatedTime);
        assertEquals(updatedTime, passwordEntity.getUpdatedTime());
    }

    @Test
    void testSetAndGetVersion() {
        // 测试设置和获取版本号（乐观锁）
        Integer version = 1;
        passwordEntity.setVersion(version);
        assertEquals(version, passwordEntity.getVersion());
    }

    @Test
    void testCompletePasswordEntity() {
        // 测试完整的密码实体
        UUID userId = UUID.randomUUID();
        String password = "$2a$10$hashedPasswordExample";
        LocalDateTime createdTime = LocalDateTime.now().minusDays(7);
        LocalDateTime updatedTime = LocalDateTime.now();
        Integer version = 2;

        passwordEntity.setUserId(userId);
        passwordEntity.setPassword(password);
        passwordEntity.setCreatedTime(createdTime);
        passwordEntity.setUpdatedTime(updatedTime);
        passwordEntity.setVersion(version);

        assertEquals(userId, passwordEntity.getUserId());
        assertEquals(password, passwordEntity.getPassword());
        assertEquals(createdTime, passwordEntity.getCreatedTime());
        assertEquals(updatedTime, passwordEntity.getUpdatedTime());
        assertEquals(version, passwordEntity.getVersion());
    }

    @Test
    void testToString() {
        // 测试toString方法
        UUID userId = UUID.randomUUID();
        passwordEntity.setUserId(userId);
        passwordEntity.setPassword("hashedPassword");

        String toString = passwordEntity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(userId.toString()));
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode（Lombok生成）
        UUID userId = UUID.randomUUID();

        UserPasswordEntity entity1 = new UserPasswordEntity();
        entity1.setUserId(userId);
        entity1.setPassword("password123");

        UserPasswordEntity entity2 = new UserPasswordEntity();
        entity2.setUserId(userId);
        entity2.setPassword("password123");

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testConstructorSetsUpdatedTime() {
        // 测试构造函数设置更新时间
        UUID userId = UUID.randomUUID();
        String password = "encodedPassword";
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        UserPasswordEntity entity = new UserPasswordEntity(userId, password);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(entity.getUpdatedTime());
        assertTrue(entity.getUpdatedTime().isAfter(before));
        assertTrue(entity.getUpdatedTime().isBefore(after));
    }

    @Test
    void testPasswordCanBeUpdated() {
        // 测试密码可以被更新
        UUID userId = UUID.randomUUID();
        UserPasswordEntity entity = new UserPasswordEntity(userId, "oldPassword");

        String newPassword = "newPassword";
        entity.setPassword(newPassword);
        entity.setUpdatedTime(LocalDateTime.now());

        assertEquals(newPassword, entity.getPassword());
        assertNotNull(entity.getUpdatedTime());
    }

    @Test
    void testVersionForOptimisticLocking() {
        // 测试版本号用于乐观锁
        passwordEntity.setVersion(1);
        assertEquals(1, passwordEntity.getVersion());

        passwordEntity.setVersion(2);
        assertEquals(2, passwordEntity.getVersion());
    }

    @Test
    void testNullableUpdatedTime() {
        // 测试更新时间可以为空
        passwordEntity.setUpdatedTime(null);
        assertNull(passwordEntity.getUpdatedTime());
    }
}

