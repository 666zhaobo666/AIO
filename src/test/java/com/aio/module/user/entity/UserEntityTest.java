package com.aio.module.user.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户实体测试
 */
class UserEntityTest {

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assertNull(userEntity.getUserId());
        assertEquals("user", userEntity.getRole()); // 默认角色为user
        assertNotNull(userEntity.getRegisterTime()); // 默认注册时间不为空
        assertEquals(1, userEntity.getStatus()); // 默认状态为1（正常）
    }

    @Test
    void testSetAndGetUserId() {
        // 测试设置和获取用户ID
        UUID userId = UUID.randomUUID();
        userEntity.setUserId(userId);
        assertEquals(userId, userEntity.getUserId());
    }

    @Test
    void testSetAndGetUsername() {
        // 测试设置和获取用户名
        String username = "testuser";
        userEntity.setUsername(username);
        assertEquals(username, userEntity.getUsername());
    }

    @Test
    void testSetAndGetName() {
        // 测试设置和获取姓名
        String name = "Test User";
        userEntity.setName(name);
        assertEquals(name, userEntity.getName());
    }

    @Test
    void testSetAndGetEmail() {
        // 测试设置和获取邮箱
        String email = "test@example.com";
        userEntity.setEmail(email);
        assertEquals(email, userEntity.getEmail());
    }

    @Test
    void testSetAndGetPhone() {
        // 测试设置和获取手机号
        String phone = "13800138000";
        userEntity.setPhone(phone);
        assertEquals(phone, userEntity.getPhone());
    }

    @Test
    void testSetAndGetGender() {
        // 测试设置和获取性别
        String gender = "M";
        userEntity.setGender(gender);
        assertEquals(gender, userEntity.getGender());
    }

    @Test
    void testSetAndGetBirthday() {
        // 测试设置和获取生日
        LocalDate birthday = LocalDate.of(1990, 1, 1);
        userEntity.setBirthday(birthday);
        assertEquals(birthday, userEntity.getBirthday());
    }

    @Test
    void testSetAndGetOccupation() {
        // 测试设置和获取职业
        String occupation = "Engineer";
        userEntity.setOccupation(occupation);
        assertEquals(occupation, userEntity.getOccupation());
    }

    @Test
    void testSetAndGetSignature() {
        // 测试设置和获取签名
        String signature = "Hello World";
        userEntity.setSignature(signature);
        assertEquals(signature, userEntity.getSignature());
    }

    @Test
    void testSetAndGetRole() {
        // 测试设置和获取角色
        String role = "ADMIN";
        userEntity.setRole(role);
        assertEquals(role, userEntity.getRole());
    }

    @Test
    void testSetAndGetRegisterTime() {
        // 测试设置和获取注册时间
        LocalDateTime registerTime = LocalDateTime.now();
        userEntity.setRegisterTime(registerTime);
        assertEquals(registerTime, userEntity.getRegisterTime());
    }

    @Test
    void testSetAndGetLastLoginTime() {
        // 测试设置和获取最后登录时间
        LocalDateTime lastLoginTime = LocalDateTime.now();
        userEntity.setLastLoginTime(lastLoginTime);
        assertEquals(lastLoginTime, userEntity.getLastLoginTime());
    }

    @Test
    void testSetAndGetStatus() {
        // 测试设置和获取状态
        Integer status = 0;
        userEntity.setStatus(status);
        assertEquals(status, userEntity.getStatus());
    }

    @Test
    void testSetAndGetVersion() {
        // 测试设置和获取版本号（乐观锁）
        Integer version = 1;
        userEntity.setVersion(version);
        assertEquals(version, userEntity.getVersion());
    }

    @Test
    void testToString() {
        // 测试toString方法
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@example.com");

        String toString = userEntity.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode（Lombok生成）
        UUID userId = UUID.randomUUID();

        UserEntity entity1 = new UserEntity();
        entity1.setUserId(userId);
        entity1.setUsername("user1");

        UserEntity entity2 = new UserEntity();
        entity2.setUserId(userId);
        entity2.setUsername("user1");

        // Note: equals is based on @Data which compares all fields
        // Since registerTime is auto-generated, we need to set it to the same value
        entity1.setRegisterTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        entity2.setRegisterTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void testCompleteUserEntity() {
        // 测试完整的用户实体
        UUID userId = UUID.randomUUID();
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        LocalDateTime registerTime = LocalDateTime.now();
        LocalDateTime lastLoginTime = LocalDateTime.now();

        userEntity.setUserId(userId);
        userEntity.setUsername("johndoe");
        userEntity.setName("John Doe");
        userEntity.setEmail("john@example.com");
        userEntity.setPhone("13800138000");
        userEntity.setGender("M");
        userEntity.setBirthday(birthday);
        userEntity.setOccupation("Software Engineer");
        userEntity.setSignature("Code is poetry");
        userEntity.setRole("USER");
        userEntity.setRegisterTime(registerTime);
        userEntity.setLastLoginTime(lastLoginTime);
        userEntity.setStatus(1);
        userEntity.setVersion(1);

        assertEquals(userId, userEntity.getUserId());
        assertEquals("johndoe", userEntity.getUsername());
        assertEquals("John Doe", userEntity.getName());
        assertEquals("john@example.com", userEntity.getEmail());
        assertEquals("13800138000", userEntity.getPhone());
        assertEquals("M", userEntity.getGender());
        assertEquals(birthday, userEntity.getBirthday());
        assertEquals("Software Engineer", userEntity.getOccupation());
        assertEquals("Code is poetry", userEntity.getSignature());
        assertEquals("USER", userEntity.getRole());
        assertEquals(registerTime, userEntity.getRegisterTime());
        assertEquals(lastLoginTime, userEntity.getLastLoginTime());
        assertEquals(1, userEntity.getStatus());
        assertEquals(1, userEntity.getVersion());
    }

    @Test
    void testNullableFields() {
        // 测试可为空的字段
        userEntity.setPhone(null);
        userEntity.setBirthday(null);
        userEntity.setOccupation(null);
        userEntity.setSignature(null);
        userEntity.setLastLoginTime(null);

        assertNull(userEntity.getPhone());
        assertNull(userEntity.getBirthday());
        assertNull(userEntity.getOccupation());
        assertNull(userEntity.getSignature());
        assertNull(userEntity.getLastLoginTime());
    }
}

