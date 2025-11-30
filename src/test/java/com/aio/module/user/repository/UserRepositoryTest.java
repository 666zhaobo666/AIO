package com.aio.module.user.repository;

import com.aio.module.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户数据访问层测试
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // 清空数据库
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setGender("M");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setOccupation("Engineer");
        testUser.setSignature("Test signature");
        testUser.setRole("USER");
        testUser.setRegisterTime(LocalDateTime.now());
        testUser.setStatus(1);

        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveUser() {
        // 测试保存用户
        UserEntity newUser = new UserEntity();
        newUser.setUsername("newuser");
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPhone("13900139000");
        newUser.setRole("USER");
        newUser.setStatus(1);

        UserEntity saved = userRepository.save(newUser);

        assertNotNull(saved);
        assertNotNull(saved.getUserId());
        assertEquals("newuser", saved.getUsername());
    }

    @Test
    void testFindByUsernameAndStatus() {
        // 测试根据用户名和状态查询
        Optional<UserEntity> found = userRepository.findByUsernameAndStatus("testuser", 1);

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsernameAndStatusNotFound() {
        // 测试查询不存在的用户
        Optional<UserEntity> found = userRepository.findByUsernameAndStatus("nonexistent", 1);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUsernameAndStatusDisabled() {
        // 测试查询禁用用户
        testUser.setStatus(0);
        userRepository.save(testUser);

        Optional<UserEntity> found = userRepository.findByUsernameAndStatus("testuser", 1);
        assertFalse(found.isPresent());

        Optional<UserEntity> foundDisabled = userRepository.findByUsernameAndStatus("testuser", 0);
        assertTrue(foundDisabled.isPresent());
    }

    @Test
    void testFindByEmailAndStatus() {
        // 测试根据邮箱和状态查询
        Optional<UserEntity> found = userRepository.findByEmailAndStatus("test@example.com", 1);

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByEmailAndStatusNotFound() {
        // 测试查询不存在的邮箱
        Optional<UserEntity> found = userRepository.findByEmailAndStatus("nonexistent@example.com", 1);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByPhoneAndStatus() {
        // 测试根据手机号和状态查询
        Optional<UserEntity> found = userRepository.findByPhoneAndStatus("13800138000", 1);

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByPhoneAndStatusNotFound() {
        // 测试查询不存在的手机号
        Optional<UserEntity> found = userRepository.findByPhoneAndStatus("19900199000", 1);

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUsername() {
        // 测试检查用户名是否存在
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        // 测试检查邮箱是否存在
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testExistsByPhone() {
        // 测试检查手机号是否存在
        assertTrue(userRepository.existsByPhone("13800138000"));
        assertFalse(userRepository.existsByPhone("19900199000"));
    }

    @Test
    void testUpdateUser() {
        // 测试更新用户
        testUser.setName("Updated Name");
        testUser.setOccupation("Senior Engineer");

        UserEntity updated = userRepository.save(testUser);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Senior Engineer", updated.getOccupation());
    }

    @Test
    void testDeleteUser() {
        // 测试删除用户
        UUID userId = testUser.getUserId();
        userRepository.deleteById(userId);

        Optional<UserEntity> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindById() {
        // 测试根据ID查询
        Optional<UserEntity> found = userRepository.findById(testUser.getUserId());

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testUniqueConstraints() {
        // 测试唯一约束
        UserEntity duplicateUser = new UserEntity();
        duplicateUser.setUsername("testuser"); // 重复用户名
        duplicateUser.setName("Duplicate");
        duplicateUser.setEmail("duplicate@example.com");
        duplicateUser.setRole("USER");
        duplicateUser.setStatus(1);

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void testOptimisticLocking() {
        // 测试乐观锁
        UserEntity user1 = userRepository.findById(testUser.getUserId()).get();
        Integer initialVersion = user1.getVersion();

        user1.setName("Name 1");
        userRepository.saveAndFlush(user1);

        UserEntity refreshed = userRepository.findById(testUser.getUserId()).get();
        assertNotEquals(initialVersion, refreshed.getVersion());
    }

    @Test
    void testUpdateLastLoginTime() {
        // 测试更新最后登录时间
        LocalDateTime loginTime = LocalDateTime.now();
        testUser.setLastLoginTime(loginTime);

        UserEntity updated = userRepository.save(testUser);

        assertNotNull(updated.getLastLoginTime());
        assertEquals(loginTime.withNano(0), updated.getLastLoginTime().withNano(0));
    }

    @Test
    void testMultipleUsers() {
        // 测试保存多个用户
        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setPhone("13900139000");
        user2.setRole("USER");
        user2.setStatus(1);
        userRepository.save(user2);

        UserEntity user3 = new UserEntity();
        user3.setUsername("user3");
        user3.setName("User 3");
        user3.setEmail("user3@example.com");
        user3.setRole("ADMIN");
        user3.setStatus(1);
        userRepository.save(user3);

        assertEquals(3, userRepository.count());
    }
}

