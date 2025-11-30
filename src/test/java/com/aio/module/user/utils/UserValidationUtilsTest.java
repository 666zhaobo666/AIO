package com.aio.module.user.utils;

import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.exception.UserException;
import com.aio.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户验证工具类测试
 */
@ExtendWith(MockitoExtension.class)
class UserValidationUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidationUtils userValidationUtils;

    @BeforeEach
    void setUp() {
        // 每个测试前重置mock
        reset(userRepository);
    }

    // ==================== 用户名验证测试 ====================
    @Test
    void testValidateUsernameValid() {
        // 测试有效用户名
        assertDoesNotThrow(() -> userValidationUtils.validateUserName("user123"));
        assertDoesNotThrow(() -> userValidationUtils.validateUserName("test_user"));
        assertDoesNotThrow(() -> userValidationUtils.validateUserName("user"));
        assertDoesNotThrow(() -> userValidationUtils.validateUserName("a1b2c3d4e5f6g7h8"));
    }

    @Test
    void testValidateUsernameTooShort() {
        // 测试用户名过短（少于4位）
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("abc"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("ab"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("a"));
    }

    @Test
    void testValidateUsernameTooLong() {
        // 测试用户名过长（超过16位）
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("counterrevolutionary"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("psychoneuroimmunology"));
    }

    @Test
    void testValidateUsernameInvalidCharacters() {
        // 测试用户名包含非法字符
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("user@123"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("user-name"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("user.name"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("user name"));
        assertThrows(UserException.class, () -> userValidationUtils.validateUserName("用户名123"));
    }

    @Test
    void testValidateUsernameNull() {
        // 测试null用户名（不抛出异常）
        assertDoesNotThrow(() -> userValidationUtils.validateUserName(null));
    }

    // ==================== 密码验证测试 ====================
    @Test
    void testValidatePasswordValid() {
        // 测试有效密码
        assertDoesNotThrow(() -> userValidationUtils.validatePassword("pass123"));
        assertDoesNotThrow(() -> userValidationUtils.validatePassword("password"));
        assertDoesNotThrow(() -> userValidationUtils.validatePassword("pwd_123456"));
        assertDoesNotThrow(() -> userValidationUtils.validatePassword("abc123"));
    }

    @Test
    void testValidatePasswordTooShort() {
        // 测试密码过短（少于6位）
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("pass1"));
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("abc12"));
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("12345"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "psychoneuroimmunology", // 20位纯字母
            "password123456789",     // 18位（字母+数字）
            "123456789password",     // 17位（数字+字母）
            "pwd_1234567890123456"   // 18位（含下划线）
    })
    void testValidatePasswordTooLong_Parameterized(String longPassword) {
        assertThrows(UserException.class, () -> validatePasswordHelper(longPassword));
    }
    private void validatePasswordHelper(String password) {
        userValidationUtils.validatePassword(password);
    }

    @Test
    void testValidatePasswordInvalidCharacters() {
        // 测试密码包含非法字符
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("pass@123"));
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("pass-word"));
        assertThrows(UserException.class, () -> userValidationUtils.validatePassword("pass word"));
    }

    @Test
    void testValidatePasswordNull() {
        // 测试null密码（不抛出异常）
        assertDoesNotThrow(() -> userValidationUtils.validatePassword(null));
    }

    // ==================== 邮箱验证测试 ====================
    @Test
    void testValidateEmailValid() {
        // 测试有效邮箱
        assertDoesNotThrow(() -> userValidationUtils.validateEmail("user@example.com"));
        assertDoesNotThrow(() -> userValidationUtils.validateEmail("test.user@example.com"));
        assertDoesNotThrow(() -> userValidationUtils.validateEmail("user+tag@example.co.cn"));
        assertDoesNotThrow(() -> userValidationUtils.validateEmail("user_name@test-domain.com"));
        assertDoesNotThrow(() -> userValidationUtils.validateEmail("a@b.co"));
    }

    @Test
    void testValidateEmailInvalid() {
        // 测试无效邮箱
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("invalid"));
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("@example.com"));
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("user@"));
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("user@.com"));
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("user@example"));
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("user@example.c")); // TLD太短
        assertThrows(UserException.class, () -> userValidationUtils.validateEmail("user example@test.com"));
    }

    @Test
    void testValidateEmailNull() {
        // 测试null邮箱（不抛出异常）
        assertDoesNotThrow(() -> userValidationUtils.validateEmail(null));
    }

    // ==================== 手机号验证测试 ====================
    @Test
    void testValidatePhoneValid() {
        // 测试有效手机号
        assertDoesNotThrow(() -> userValidationUtils.validatePhone("13800138000"));
        assertDoesNotThrow(() -> userValidationUtils.validatePhone("15912345678"));
        assertDoesNotThrow(() -> userValidationUtils.validatePhone("18888888888"));
        assertDoesNotThrow(() -> userValidationUtils.validatePhone("+8613800138000"));
        assertDoesNotThrow(() -> userValidationUtils.validatePhone("013800138000"));
    }

    @Test
    void testValidatePhoneInvalid() {
        // 测试无效手机号
        assertThrows(UserException.class, () -> userValidationUtils.validatePhone("12345678901")); // 错误号段
        assertThrows(UserException.class, () -> userValidationUtils.validatePhone("1380013800")); // 位数不对
        assertThrows(UserException.class, () -> userValidationUtils.validatePhone("138001380000")); // 位数过多
        assertThrows(UserException.class, () -> userValidationUtils.validatePhone("1234567890a"));
        assertThrows(UserException.class, () -> userValidationUtils.validatePhone("138-0013-8000"));
    }

    @Test
    void testValidatePhoneNull() {
        // 测试null手机号（不抛出异常）
        assertDoesNotThrow(() -> userValidationUtils.validatePhone(null));
    }

    // ==================== 生日验证测试 ====================
    @Test
    void testValidateBirthdayValid() {
        // 测试有效生日（过去的日期）
        assertDoesNotThrow(() -> userValidationUtils.validateBirthday(LocalDate.of(1990, 1, 1)));
        assertDoesNotThrow(() -> userValidationUtils.validateBirthday(LocalDate.now().minusDays(1)));
        assertDoesNotThrow(() -> userValidationUtils.validateBirthday(LocalDate.now()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2099-12-31", "2026-01-01"}) // 测试日期字符串
    void testValidateBirthdayFuture_Parameterized(String futureDate) {
        LocalDate birthday = LocalDate.parse(futureDate);
        assertThrows(UserException.class, () -> validateBirthdayHelper(birthday));
    }

    private void validateBirthdayHelper(LocalDate birthday) {
        userValidationUtils.validateBirthday(birthday);
    }

    @Test
    void testValidateBirthdayNull() {
        // 测试null生日（不抛出异常）
        assertDoesNotThrow(() -> userValidationUtils.validateBirthday(null));
    }

    // ==================== 性别验证测试 ====================
    @Test
    void testValidateGenderValid() {
        // 测试有效性别
        assertDoesNotThrow(() -> userValidationUtils.validateGender("M"));
        assertDoesNotThrow(() -> userValidationUtils.validateGender("F"));
        assertDoesNotThrow(() -> userValidationUtils.validateGender("U"));
    }

    @Test
    void testValidateGenderInvalid() {
        // 测试无效性别
        assertThrows(UserException.class, () -> userValidationUtils.validateGender("X"));
        assertThrows(UserException.class, () -> userValidationUtils.validateGender("male"));
        assertThrows(UserException.class, () -> userValidationUtils.validateGender("m"));
        assertThrows(UserException.class, () -> userValidationUtils.validateGender(""));
    }

    // ==================== 角色验证测试 ====================
    @Test
    void testValidateRoleValid() {
        // 测试有效角色
        assertDoesNotThrow(() -> userValidationUtils.validateRole("USER"));
        assertDoesNotThrow(() -> userValidationUtils.validateRole("ADMIN"));
    }

    @Test
    void testValidateRoleInvalid() {
        // 测试无效角色
        assertThrows(UserException.class, () -> userValidationUtils.validateRole("INVALID"));
        assertThrows(UserException.class, () -> userValidationUtils.validateRole("user"));
        assertThrows(UserException.class, () -> userValidationUtils.validateRole(""));
    }

    // ==================== 唯一性验证测试 ====================
    @Test
    void testValidateUserAccountUniquenessAllUnique() {
        // 测试所有字段都唯一
        UserEntity user = new UserEntity();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPhone("13800138000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("13800138000")).thenReturn(false);

        assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniqueness(user));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByPhone("13800138000");
    }

    @Test
    void testValidateUserAccountUniquenessUsernameExists() {
        // 测试用户名已存在
        UserEntity user = new UserEntity();
        user.setUsername("existinguser");
        user.setEmail("new@example.com");
        user.setPhone("13800138000");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(UserException.class, () -> userValidationUtils.validateUserAccountUniqueness(user));

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testValidateUserAccountUniquenessEmailExists() {
        // 测试邮箱已存在
        UserEntity user = new UserEntity();
        user.setUsername("newuser");
        user.setEmail("existing@example.com");
        user.setPhone("13800138000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserException.class, () -> userValidationUtils.validateUserAccountUniqueness(user));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).existsByPhone(anyString());
    }

    @Test
    void testValidateUserAccountUniquenessPhoneExists() {
        // 测试手机号已存在
        UserEntity user = new UserEntity();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPhone("13800138000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("13800138000")).thenReturn(true);

        assertThrows(UserException.class, () -> userValidationUtils.validateUserAccountUniqueness(user));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByPhone("13800138000");
    }

    @Test
    void testValidateUserAccountUniquenessNullPhone() {
        // 测试手机号为null时不检查
        UserEntity user = new UserEntity();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPhone(null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniqueness(user));

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).existsByPhone(anyString());
    }
}

