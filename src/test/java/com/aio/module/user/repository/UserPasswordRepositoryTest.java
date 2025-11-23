package com.aio.module.user.repository;

import com.aio.module.user.entity.UserPasswordEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户密码数据访问层测试
 */
@DataJpaTest
@ActiveProfiles("test")
class UserPasswordRepositoryTest {

    @Autowired
    private UserPasswordRepository passwordRepository;

    private UUID testUserId;
    private UserPasswordEntity testPassword;

    @BeforeEach
    void setUp() {
        // 清空数据库
        passwordRepository.deleteAll();

        // 创建测试密码记录
        testUserId = UUID.randomUUID();
        testPassword = new UserPasswordEntity();
        testPassword.setUserId(testUserId);
        testPassword.setPassword("$2a$10$hashedPassword");
        testPassword.setCreatedTime(LocalDateTime.now());
        testPassword.setUpdatedTime(LocalDateTime.now());

        testPassword = passwordRepository.save(testPassword);
    }

    @Test
    void testSavePassword() {
        // 测试保存密码
        UUID newUserId = UUID.randomUUID();
        UserPasswordEntity newPassword = new UserPasswordEntity();
        newPassword.setUserId(newUserId);
        newPassword.setPassword("$2a$10$newHashedPassword");
        newPassword.setCreatedTime(LocalDateTime.now());

        UserPasswordEntity saved = passwordRepository.save(newPassword);

        assertNotNull(saved);
        assertEquals(newUserId, saved.getUserId());
        assertEquals("$2a$10$newHashedPassword", saved.getPassword());
    }

    @Test
    void testFindByUserId() {
        // 测试根据用户ID查询
        Optional<Object> found = passwordRepository.findByUserId(testUserId);

        assertTrue(found.isPresent());
        assertInstanceOf(UserPasswordEntity.class, found.get());
        UserPasswordEntity entity = (UserPasswordEntity) found.get();
        assertEquals(testUserId, entity.getUserId());
    }

    @Test
    void testFindByUserIdNotFound() {
        // 测试查询不存在的用户ID
        UUID nonExistentId = UUID.randomUUID();
        Optional<Object> found = passwordRepository.findByUserId(nonExistentId);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindPasswordByUserId() {
        // 测试直接查询密码字符串
        String password = passwordRepository.findPasswordByUserId(testUserId);

        assertNotNull(password);
        assertEquals("$2a$10$hashedPassword", password);
    }

    @Test
    void testFindPasswordByUserIdNotFound() {
        // 测试查询不存在的用户密码
        UUID nonExistentId = UUID.randomUUID();
        String password = passwordRepository.findPasswordByUserId(nonExistentId);

        assertNull(password);
    }

    @Test
    void testUpdatePassword() {
        // 测试更新密码
        testPassword.setPassword("$2a$10$newHashedPassword");
        testPassword.setUpdatedTime(LocalDateTime.now());

        UserPasswordEntity updated = passwordRepository.save(testPassword);

        assertEquals("$2a$10$newHashedPassword", updated.getPassword());
        assertNotNull(updated.getUpdatedTime());
    }

    @Test
    void testDeletePassword() {
        // 测试删除密码
        passwordRepository.deleteById(testUserId);

        Optional<Object> found = passwordRepository.findByUserId(testUserId);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindById() {
        // 测试根据ID查询
        Optional<UserPasswordEntity> found = passwordRepository.findById(testUserId);

        assertTrue(found.isPresent());
        assertEquals(testUserId, found.get().getUserId());
    }

    @Test
    void testOptimisticLocking() {
        // 测试乐观锁
        // Note: In JPA, optimistic locking requires fetching the entity twice in separate transactions
        // For this test, we'll modify the password twice and verify the version changes
        UserPasswordEntity pwd1 = passwordRepository.findById(testUserId).get();
        Integer initialVersion = pwd1.getVersion();

        pwd1.setPassword("$2a$10$password1");
        passwordRepository.saveAndFlush(pwd1);

        UserPasswordEntity refreshed = passwordRepository.findById(testUserId).get();
        assertNotEquals(initialVersion, refreshed.getVersion());
    }

    @Test
    void testPasswordEntityWithConstructor() {
        // 测试使用构造函数创建密码实体
        UUID newUserId = UUID.randomUUID();
        String encodedPassword = "$2a$10$constructorPassword";

        UserPasswordEntity entity = new UserPasswordEntity(newUserId, encodedPassword);
        UserPasswordEntity saved = passwordRepository.save(entity);

        assertNotNull(saved);
        assertEquals(newUserId, saved.getUserId());
        assertEquals(encodedPassword, saved.getPassword());
        assertNotNull(saved.getUpdatedTime());
    }

    @Test
    void testCreatedTimeIsSet() {
        // 测试创建时间被设置
        UUID newUserId = UUID.randomUUID();
        UserPasswordEntity entity = new UserPasswordEntity();
        entity.setUserId(newUserId);
        entity.setPassword("password");

        UserPasswordEntity saved = passwordRepository.save(entity);

        assertNotNull(saved.getCreatedTime());
    }

    @Test
    void testMultiplePasswords() {
        // 测试保存多个用户密码
        UUID userId2 = UUID.randomUUID();
        UserPasswordEntity pwd2 = new UserPasswordEntity();
        pwd2.setUserId(userId2);
        pwd2.setPassword("$2a$10$password2");
        pwd2.setCreatedTime(LocalDateTime.now());
        passwordRepository.save(pwd2);

        UUID userId3 = UUID.randomUUID();
        UserPasswordEntity pwd3 = new UserPasswordEntity();
        pwd3.setUserId(userId3);
        pwd3.setPassword("$2a$10$password3");
        pwd3.setCreatedTime(LocalDateTime.now());
        passwordRepository.save(pwd3);

        assertEquals(3, passwordRepository.count());
    }

    @Test
    void testPasswordCanBeUpdatedMultipleTimes() {
        // 测试密码可以被多次更新
        testPassword.setPassword("$2a$10$password1");
        testPassword.setUpdatedTime(LocalDateTime.now());
        passwordRepository.save(testPassword);

        testPassword.setPassword("$2a$10$password2");
        testPassword.setUpdatedTime(LocalDateTime.now());
        passwordRepository.save(testPassword);

        testPassword.setPassword("$2a$10$password3");
        testPassword.setUpdatedTime(LocalDateTime.now());
        UserPasswordEntity updated = passwordRepository.save(testPassword);

        assertEquals("$2a$10$password3", updated.getPassword());
    }
}

