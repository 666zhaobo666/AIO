package com.aio.module.user.service;

import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.entity.UserPasswordEntity;
import com.aio.module.user.enums.UserExceptionEnum;
import com.aio.module.user.exception.UserException;
import com.aio.module.user.repository.UserPasswordRepository;
import com.aio.module.user.repository.UserRepository;
import com.aio.module.user.utils.UserValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPasswordRepository userPasswordRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserValidationUtils userValidationUtils;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setGender("M");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setOccupation("Engineer");
        testUser.setSignature("Test");
        testUser.setRole("USER");
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());

        rawPassword = "password123";
    }

    // ==================== 注册测试 ====================
    @Test
    void testRegisterSuccess() {
        // 测试注册成功
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userPasswordRepository.save(any(UserPasswordEntity.class)))
                .thenReturn(new UserPasswordEntity(testUser.getUserId(), encodedPassword));

        UserEntity result = userService.register(testUser, rawPassword);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        verify(userValidationUtils).validateUserName(testUser.getUsername());
        verify(userValidationUtils).validatePassword(rawPassword);
        verify(userValidationUtils).validateEmail(testUser.getEmail());
        verify(userValidationUtils).validatePhone(testUser.getPhone());
        verify(userValidationUtils).validateBirthday(testUser.getBirthday());
        verify(userValidationUtils).validateGender(testUser.getGender());
        verify(userValidationUtils).validateRole(testUser.getRole());
        verify(userValidationUtils).validateUserAccountUniqueness(testUser);
        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode(rawPassword);
        verify(userPasswordRepository).save(any(UserPasswordEntity.class));
    }

    @Test
    void testRegisterValidationFails() {
        // 测试注册时验证失败
        doThrow(new UserException(UserExceptionEnum.USERNAME_VALIDATION_ERROR))
                .when(userValidationUtils).validateUserName(anyString());

        assertThrows(UserException.class, () -> {
            userService.register(testUser, rawPassword);
        });

        verify(userRepository, never()).save(any());
        verify(userPasswordRepository, never()).save(any());
    }

    // ==================== 登录测试 ====================
    @Test
    void testLoginByUsernameSuccess() {
        // 测试通过用户名登录成功
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.findByUsernameAndStatus("testuser", 1))
                .thenReturn(Optional.of(testUser));
        when(userPasswordRepository.findPasswordByUserId(testUser.getUserId()))
                .thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserEntity result = userService.login("testuser", rawPassword);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNotNull(result.getLastLoginTime());

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testLoginByEmailSuccess() {
        // 测试通过邮箱登录成功
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.findByUsernameAndStatus("test@example.com", 1))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.of(testUser));
        when(userPasswordRepository.findPasswordByUserId(testUser.getUserId()))
                .thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserEntity result = userService.login("test@example.com", rawPassword);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginByPhoneSuccess() {
        // 测试通过手机号登录成功
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.findByUsernameAndStatus("13800138000", 1))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndStatus("13800138000", 1))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneAndStatus("13800138000", 1))
                .thenReturn(Optional.of(testUser));
        when(userPasswordRepository.findPasswordByUserId(testUser.getUserId()))
                .thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        UserEntity result = userService.login("13800138000", rawPassword);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginUserNotExist() {
        // 测试登录时用户不存在
        when(userRepository.findByUsernameAndStatus(anyString(), anyInt()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndStatus(anyString(), anyInt()))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneAndStatus(anyString(), anyInt()))
                .thenThrow(new UserException(UserExceptionEnum.USER_NOT_EXIST));

        assertThrows(UserException.class, () -> {
            userService.login("nonexistent", rawPassword);
        });
    }

    @Test
    void testLoginWrongPassword() {
        // 测试登录时密码错误
        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.findByUsernameAndStatus("testuser", 1))
                .thenReturn(Optional.of(testUser));
        when(userPasswordRepository.findPasswordByUserId(testUser.getUserId()))
                .thenReturn(encodedPassword);
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        assertThrows(UserException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        verify(userRepository, never()).save(any());
    }

    // ==================== 修改密码测试 ====================
    @Test
    void testUpdatePasswordSuccess() {
        // 测试修改密码成功
        UUID userId = testUser.getUserId();
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedOldPassword = "$2a$10$encodedOldPassword";
        String encodedNewPassword = "$2a$10$encodedNewPassword";

        UserPasswordEntity passwordEntity = new UserPasswordEntity();
        passwordEntity.setUserId(userId);
        passwordEntity.setPassword(encodedOldPassword);

        when(userPasswordRepository.findByUserId(userId))
                .thenReturn(Optional.of(passwordEntity));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userPasswordRepository.save(any(UserPasswordEntity.class)))
                .thenReturn(passwordEntity);

        assertDoesNotThrow(() -> {
            userService.updatePassword(userId, oldPassword, newPassword);
        });

        verify(userPasswordRepository).save(any(UserPasswordEntity.class));
    }

    @Test
    void testUpdatePasswordNotExist() {
        // 测试修改不存在的密码
        UUID userId = UUID.randomUUID();

        when(userPasswordRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> {
            userService.updatePassword(userId, "oldPassword", "newPassword");
        });
    }

    @Test
    void testUpdatePasswordOldPasswordWrong() {
        // 测试修改密码时旧密码错误
        UUID userId = testUser.getUserId();
        String encodedPassword = "$2a$10$encodedPassword";

        UserPasswordEntity passwordEntity = new UserPasswordEntity();
        passwordEntity.setUserId(userId);
        passwordEntity.setPassword(encodedPassword);

        when(userPasswordRepository.findByUserId(userId))
                .thenReturn(Optional.of(passwordEntity));
        when(passwordEncoder.matches("wrongOldPassword", encodedPassword)).thenReturn(false);

        assertThrows(UserException.class, () -> {
            userService.updatePassword(userId, "wrongOldPassword", "newPassword");
        });

        verify(userPasswordRepository, never()).save(any());
    }

    // ==================== 删除用户测试 ====================
    @Test
    void testDeleteUserAsAdmin() {
        // 测试管理员删除用户
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = testUser.getUserId();
        String currentRole = "ADMIN";

        assertDoesNotThrow(() -> {
            userService.deleteUser(targetUserId, currentUserId, currentRole);
        });

        verify(userPasswordRepository).deleteById(targetUserId);
        verify(userRepository).deleteById(targetUserId);
    }

    @Test
    void testDeleteSelfAsUser() {
        // 测试普通用户删除自己
        UUID userId = testUser.getUserId();
        String currentRole = "USER";

        assertDoesNotThrow(() -> {
            userService.deleteUser(userId, userId, currentRole);
        });

        verify(userPasswordRepository).deleteById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteOtherAsUser() {
        // 测试普通用户尝试删除其他用户
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = testUser.getUserId();
        String currentRole = "USER";

        assertThrows(UserException.class, () -> {
            userService.deleteUser(targetUserId, currentUserId, currentRole);
        });

        verify(userPasswordRepository, never()).deleteById(any());
        verify(userRepository, never()).deleteById(any());
    }

    // ==================== 获取用户信息测试 ====================
    @Test
    void testGetUserInfoSuccess() {
        // 测试获取用户信息成功
        UUID userId = testUser.getUserId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        var response = userService.getUserInfo(userId);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("查询成功", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testGetUserInfoNotFound() {
        // 测试获取不存在的用户信息
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> {
            userService.getUserInfo(userId);
        });
    }

    // ==================== 验证用户测试 ====================
    @Test
    void testValidateUserSuccess() {
        // 测试验证用户成功
        assertDoesNotThrow(() -> {
            userService.validateUser(testUser, rawPassword);
        });

        verify(userValidationUtils).validateUserName(testUser.getUsername());
        verify(userValidationUtils).validatePassword(rawPassword);
        verify(userValidationUtils).validateEmail(testUser.getEmail());
        verify(userValidationUtils).validatePhone(testUser.getPhone());
        verify(userValidationUtils).validateBirthday(testUser.getBirthday());
        verify(userValidationUtils).validateGender(testUser.getGender());
        verify(userValidationUtils).validateRole(testUser.getRole());
        verify(userValidationUtils).validateUserAccountUniqueness(testUser);
    }

    @Test
    void testValidateUserFails() {
        // 测试验证用户失败
        doThrow(new UserException(UserExceptionEnum.EMAIL_VALIDATION_ERROR))
                .when(userValidationUtils).validateEmail(anyString());

        assertThrows(UserException.class, () -> {
            userService.validateUser(testUser, rawPassword);
        });
    }
}

