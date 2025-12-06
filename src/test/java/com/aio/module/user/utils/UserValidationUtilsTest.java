package com.aio.module.user.utils;

import com.aio.common.enums.ExceptionEnum;
import com.aio.common.exception.GlobalException;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidationUtils 单元测试")
class UserValidationUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidationUtils userValidationUtils;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(1);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRole(UserRoleEnum.USER.getValue());
    }

    @Nested
    @DisplayName("validateUserName 用户名校验测试")
    class ValidateUserNameTests {

        @Test
        @DisplayName("用户名为null时不抛异常")
        void validateUserName_NullUsername() {
            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserName(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"user", "test1234", "user_name", "User123", "abcd1234567890xy"})
        @DisplayName("合法用户名校验通过")
        void validateUserName_ValidUsernames(String username) {
            assertDoesNotThrow(() -> userValidationUtils.validateUserName(username));
        }

        @ParameterizedTest(name = "[{index}] 非法用户名: {0}")
        @ValueSource(strings = {
            "abc", "ab", "a",
            "abed12345678901234567",
            "user@name", "user-name", "user.name", "用户名", "user name"
        })
        @DisplayName("非法用户名校验（长度或字符不符）")
        void validateUserName_InvalidInputs(String username) {
            GlobalException exception = assertThrows(GlobalException.class,
                () -> userValidationUtils.validateUserName(username));
            assertEquals(ExceptionEnum.USERNAME_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validatePassword 密码校验测试")
    class ValidatePasswordTests {

        @Test
        @DisplayName("密码为null时不抛异常")
        void validatePassword_NullPassword() {
            assertDoesNotThrow(() -> userValidationUtils.validatePassword(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"pass12", "password", "123456", "pass_123", "Password1234567"})
        @DisplayName("合法密码校验通过")
        void validatePassword_ValidPasswords(String password) {
            assertDoesNotThrow(() -> userValidationUtils.validatePassword(password));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "12345", "abc12",              // Case 1: 太短
            "12345678901234567",           // Case 2: 太长
            "pass@123", "pass-123", "密码"  // Case 3: 非法字符
        })
        @DisplayName("非法密码校验失败（长度不符或包含非法字符）")
        void validatePassword_InvalidInputs(String password) {
            GlobalException exception = assertThrows(GlobalException.class,
                () -> userValidationUtils.validatePassword(password));
            assertEquals(ExceptionEnum.PASSWORD_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateEmail 邮箱校验测试")
    class ValidateEmailTests {

        @Test
        @DisplayName("邮箱为null时不抛异常")
        void validateEmail_NullEmail() {
            assertDoesNotThrow(() -> userValidationUtils.validateEmail(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"test@example.com", "user.name@example.com", "user+tag@example.cn",
                "test@sub.domain.com", "user123@example.io"})
        @DisplayName("合法邮箱校验通过")
        void validateEmail_ValidEmails(String email) {
            assertDoesNotThrow(() -> userValidationUtils.validateEmail(email));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "test@", "@example.com", "test@.com", "test@example",
                "test@example.c", "test@example.toolong"})
        @DisplayName("非法邮箱格式抛异常")
        void validateEmail_InvalidEmails(String email) {
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateEmail(email));
            assertEquals(ExceptionEnum.EMAIL_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validatePhone 手机号校验测试")
    class ValidatePhoneTests {

        @Test
        @DisplayName("手机号为null时不抛异常")
        void validatePhone_NullPhone() {
            assertDoesNotThrow(() -> userValidationUtils.validatePhone(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"13800138000", "15912345678", "18612345678", "17712345678",
                "+8613800138000", "013800138000"})
        @DisplayName("合法手机号校验通过")
        void validatePhone_ValidPhones(String phone) {
            assertDoesNotThrow(() -> userValidationUtils.validatePhone(phone));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678901", "1380013800", "138001380000", "2380013800",
                "1380013800a", "phone123456"})
        @DisplayName("非法手机号格式抛异常")
        void validatePhone_InvalidPhones(String phone) {
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validatePhone(phone));
            assertEquals(ExceptionEnum.PHONE_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateBirthday 生日校验测试")
    class ValidateBirthdayTests {

        @Test
        @DisplayName("生日为null时不抛异常")
        void validateBirthday_NullBirthday() {
            assertDoesNotThrow(() -> userValidationUtils.validateBirthday(null));
        }

        @Test
        @DisplayName("生日为过去日期时校验通过")
        void validateBirthday_PastDate() {
            LocalDate pastDate = LocalDate.of(1990, 1, 1);
            assertDoesNotThrow(() -> userValidationUtils.validateBirthday(pastDate));
        }

        @Test
        @DisplayName("生日为今天时校验通过")
        void validateBirthday_Today() {
            LocalDate today = LocalDate.now();
            assertDoesNotThrow(() -> userValidationUtils.validateBirthday(today));
        }

        @Test
        @DisplayName("生日为未来日期时抛异常")
        void validateBirthday_FutureDate() {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateBirthday(futureDate));
            assertEquals(ExceptionEnum.BIRTHDAY_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateGender 性别校验测试")
    class ValidateGenderTests {

        @Test
        @DisplayName("性别为null时不抛异常")
        void validateGender_NullGender() {
            assertDoesNotThrow(() -> userValidationUtils.validateGender(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"M", "F", "U"})
        @DisplayName("合法性别校验通过")
        void validateGender_ValidGenders(String gender) {
            assertDoesNotThrow(() -> userValidationUtils.validateGender(gender));
        }

        @ParameterizedTest
        @ValueSource(strings = {"m", "f", "u", "male", "female", "unknown", "X", "1"})
        @DisplayName("非法性别抛异常")
        void validateGender_InvalidGenders(String gender) {
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateGender(gender));
            assertEquals(ExceptionEnum.GENDER_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateRole 角色校验测试")
    class ValidateRoleTests {

        @Test
        @DisplayName("角色为null时抛异常")
        void validateRole_NullRole() {
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateRole(null));
            assertEquals(ExceptionEnum.ROLE_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"USER", "ADMIN"})
        @DisplayName("合法角色校验通过")
        void validateRole_ValidRoles(String role) {
            assertDoesNotThrow(() -> userValidationUtils.validateRole(role));
        }

        @ParameterizedTest
        @ValueSource(strings = {"user", "admin", "GUEST", "SUPERADMIN", "role"})
        @DisplayName("非法角色抛异常")
        void validateRole_InvalidRoles(String role) {
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateRole(role));
            assertEquals(ExceptionEnum.ROLE_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateUserAccountUniqueness 唯一性校验测试")
    class ValidateUserAccountUniquenessTests {

        @Test
        @DisplayName("所有字段唯一时校验通过")
        void validateUserAccountUniqueness_AllUnique() {
            // Arrange
            when(userRepository.existsByUserName(testUser.getUserName())).thenReturn(false);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
            when(userRepository.existsByPhone(testUser.getPhone())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniqueness(testUser));
        }

        @Test
        @DisplayName("用户名已存在时抛异常")
        void validateUserAccountUniqueness_UsernameExists() {
            // Arrange
            when(userRepository.existsByUserName(testUser.getUserName())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniqueness(testUser));
            assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("邮箱已存在时抛异常")
        void validateUserAccountUniqueness_EmailExists() {
            // Arrange
            when(userRepository.existsByUserName(testUser.getUserName())).thenReturn(false);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniqueness(testUser));
            assertEquals(ExceptionEnum.EMAIL_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("手机号已存在时抛异常")
        void validateUserAccountUniqueness_PhoneExists() {
            // Arrange
            when(userRepository.existsByUserName(testUser.getUserName())).thenReturn(false);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
            when(userRepository.existsByPhone(testUser.getPhone())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniqueness(testUser));
            assertEquals(ExceptionEnum.PHONE_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("手机号为null时不校验手机号唯一性")
        void validateUserAccountUniqueness_NullPhone() {
            // Arrange
            testUser.setPhone(null);
            when(userRepository.existsByUserName(testUser.getUserName())).thenReturn(false);
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniqueness(testUser));
            verify(userRepository, never()).existsByPhone(any());
        }
    }

    @Nested
    @DisplayName("validateUserAccountUniquenessForUpdate 更新唯一性校验测试")
    class ValidateUserAccountUniquenessForUpdateTests {

        @Test
        @DisplayName("所有字段唯一时校验通过")
        void validateUserAccountUniquenessForUpdate_AllUnique() {
            // Arrange
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByEmailAndUserIdNot(testUser.getEmail(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByPhoneAndUserIdNot(testUser.getPhone(), testUser.getUserId())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
        }

        @Test
        @DisplayName("用户名已被他人使用时抛异常")
        void validateUserAccountUniquenessForUpdate_UsernameExists() {
            // Arrange
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("邮箱已被他人使用时抛异常")
        void validateUserAccountUniquenessForUpdate_EmailExists() {
            // Arrange
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByEmailAndUserIdNot(testUser.getEmail(), testUser.getUserId())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            assertEquals(ExceptionEnum.EMAIL_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("手机号已被他人使用时抛异常")
        void validateUserAccountUniquenessForUpdate_PhoneExists() {
            // Arrange
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByEmailAndUserIdNot(testUser.getEmail(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByPhoneAndUserIdNot(testUser.getPhone(), testUser.getUserId())).thenReturn(true);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            assertEquals(ExceptionEnum.PHONE_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("用户名为null时不校验用户名唯一性")
        void validateUserAccountUniquenessForUpdate_NullUsername() {
            // Arrange
            testUser.setUserName(null);
            when(userRepository.existsByEmailAndUserIdNot(testUser.getEmail(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByPhoneAndUserIdNot(testUser.getPhone(), testUser.getUserId())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            verify(userRepository, never()).existsByUserNameAndUserIdNot(any(), any());
        }

        @Test
        @DisplayName("邮箱为null时不校验邮箱唯一性")
        void validateUserAccountUniquenessForUpdate_NullEmail() {
            // Arrange
            testUser.setEmail(null);
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByPhoneAndUserIdNot(testUser.getPhone(), testUser.getUserId())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            verify(userRepository, never()).existsByEmailAndUserIdNot(any(), any());
        }

        @Test
        @DisplayName("手机号为null时不校验手机号唯一性")
        void validateUserAccountUniquenessForUpdate_NullPhone() {
            // Arrange
            testUser.setPhone(null);
            when(userRepository.existsByUserNameAndUserIdNot(testUser.getUserName(), testUser.getUserId())).thenReturn(false);
            when(userRepository.existsByEmailAndUserIdNot(testUser.getEmail(), testUser.getUserId())).thenReturn(false);

            // Act & Assert
            assertDoesNotThrow(() -> userValidationUtils.validateUserAccountUniquenessForUpdate(testUser));
            verify(userRepository, never()).existsByPhoneAndUserIdNot(any(), any());
        }
    }
}

