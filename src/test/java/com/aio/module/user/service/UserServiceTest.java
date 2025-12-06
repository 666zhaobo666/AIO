package com.aio.module.user.service;

import com.aio.api.model.ModelApiResponse;
import com.aio.api.model.UpdateUserInfoRequest;
import com.aio.common.enums.ExceptionEnum;
import com.aio.common.exception.GlobalException;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.entity.UserPasswordEntity;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.module.user.repository.UserPasswordRepository;
import com.aio.module.user.repository.UserRepository;
import com.aio.module.user.utils.UserValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService 单元测试")
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
    private UserPasswordEntity testUserPassword;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(1);
        testUser.setUserName("testuser");
        testUser.setDisplayName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setGender("M");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setRole(UserRoleEnum.USER.getValue());
        testUser.setStatus(1);
        testUser.setRegisterTime(LocalDateTime.now());

        testUserPassword = new UserPasswordEntity();
        testUserPassword.setUserId(1);
        testUserPassword.setPassword("encodedPassword");
        testUserPassword.setCreatedTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("register 注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功 - 正常用户信息")
        void register_Success() {
            // Arrange
            String rawPassword = "password123";
            String encodedPassword = "encodedPassword123";

            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validatePassword(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniqueness(any());

            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
                UserEntity user = invocation.getArgument(0);
                user.setUserId(1);
                return user;
            });
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(userPasswordRepository.save(any(UserPasswordEntity.class))).thenReturn(testUserPassword);

            // Act
            UserEntity result = userService.register(testUser, rawPassword);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getUserId());
            verify(userRepository).save(testUser);
            verify(userPasswordRepository).save(any(UserPasswordEntity.class));
            verify(passwordEncoder).encode(rawPassword);
        }

        @Test
        @DisplayName("注册失败 - 用户名格式错误")
        void register_InvalidUsername() {
            // Arrange
            String rawPassword = "password123";
            doThrow(new GlobalException(ExceptionEnum.USERNAME_VALIDATION_ERROR))
                    .when(userValidationUtils).validateUserName(anyString());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.register(testUser, rawPassword));
            assertEquals(ExceptionEnum.USERNAME_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("注册失败 - 密码格式错误")
        void register_InvalidPassword() {
            // Arrange
            String rawPassword = "123";
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doThrow(new GlobalException(ExceptionEnum.PASSWORD_VALIDATION_ERROR))
                    .when(userValidationUtils).validatePassword(anyString());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.register(testUser, rawPassword));
            assertEquals(ExceptionEnum.PASSWORD_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UsernameAlreadyExists() {
            // Arrange
            String rawPassword = "password123";
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validatePassword(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doThrow(new GlobalException(ExceptionEnum.USERNAME_ALREADY_EXIST))
                    .when(userValidationUtils).validateUserAccountUniqueness(any());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.register(testUser, rawPassword));
            assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("login 登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 用户名登录")
        void login_SuccessWithUsername() {
            // Arrange
            String account = "testuser";
            String rawPassword = "password123";

            when(userRepository.findByUserNameAndStatus(account, 1)).thenReturn(Optional.of(testUser));
            when(userPasswordRepository.findPasswordByUserId(testUser.getUserId())).thenReturn("encodedPassword");
            when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // Act
            UserEntity result = userService.login(account, rawPassword);

            // Assert
            assertNotNull(result);
            assertEquals(testUser.getUserId(), result.getUserId());
            assertNotNull(result.getLastLoginTime());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("登录成功 - 邮箱登录")
        void login_SuccessWithEmail() {
            // Arrange
            String account = "test@example.com";
            String rawPassword = "password123";

            when(userRepository.findByUserNameAndStatus(account, 1)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndStatus(account, 1)).thenReturn(Optional.of(testUser));
            when(userPasswordRepository.findPasswordByUserId(testUser.getUserId())).thenReturn("encodedPassword");
            when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // Act
            UserEntity result = userService.login(account, rawPassword);

            // Assert
            assertNotNull(result);
            assertEquals(testUser.getEmail(), result.getEmail());
        }

        @Test
        @DisplayName("登录成功 - 手机号登录")
        void login_SuccessWithPhone() {
            // Arrange
            String account = "13800138000";
            String rawPassword = "password123";

            when(userRepository.findByUserNameAndStatus(account, 1)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndStatus(account, 1)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndStatus(account, 1)).thenReturn(Optional.of(testUser));
            when(userPasswordRepository.findPasswordByUserId(testUser.getUserId())).thenReturn("encodedPassword");
            when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

            // Act
            UserEntity result = userService.login(account, rawPassword);

            // Assert
            assertNotNull(result);
            assertEquals(testUser.getPhone(), result.getPhone());
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void login_UserNotExist() {
            // Arrange
            String account = "nonexistent";
            String rawPassword = "password123";

            when(userRepository.findByUserNameAndStatus(account, 1)).thenReturn(Optional.empty());
            when(userRepository.findByEmailAndStatus(account, 1)).thenReturn(Optional.empty());
            when(userRepository.findByPhoneAndStatus(account, 1)).thenThrow(new GlobalException("用户不存在或已禁用"));

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.login(account, rawPassword));
            assertTrue(exception.getMessage().contains("用户不存在"));
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_PasswordError() {
            // Arrange
            String account = "testuser";
            String rawPassword = "wrongPassword";

            when(userRepository.findByUserNameAndStatus(account, 1)).thenReturn(Optional.of(testUser));
            when(userPasswordRepository.findPasswordByUserId(testUser.getUserId())).thenReturn("encodedPassword");
            when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(false);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.login(account, rawPassword));
            assertEquals(ExceptionEnum.USER_PASSWORD_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updatePassword 修改密码测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void updatePassword_Success() {
            // Arrange
            Integer userId = 1;
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            String encodedNewPassword = "encodedNewPassword";

            when(userPasswordRepository.findByUserId(userId)).thenReturn(Optional.of(testUserPassword));
            when(passwordEncoder.matches(oldPassword, testUserPassword.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
            when(userPasswordRepository.save(any(UserPasswordEntity.class))).thenReturn(testUserPassword);

            // Act
            userService.updatePassword(userId, oldPassword, newPassword);

            // Assert
            ArgumentCaptor<UserPasswordEntity> captor = ArgumentCaptor.forClass(UserPasswordEntity.class);
            verify(userPasswordRepository).save(captor.capture());
            assertEquals(encodedNewPassword, captor.getValue().getPassword());
        }

        @Test
        @DisplayName("修改密码失败 - 用户密码不存在")
        void updatePassword_PasswordNotExist() {
            // Arrange
            Integer userId = 999;
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";

            when(userPasswordRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.updatePassword(userId, oldPassword, newPassword));
            assertEquals(ExceptionEnum.USER_PASSWORD_NOT_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("修改密码失败 - 旧密码错误")
        void updatePassword_OldPasswordError() {
            // Arrange
            Integer userId = 1;
            String oldPassword = "wrongOldPassword";
            String newPassword = "newPassword";

            when(userPasswordRepository.findByUserId(userId)).thenReturn(Optional.of(testUserPassword));
            when(passwordEncoder.matches(oldPassword, testUserPassword.getPassword())).thenReturn(false);

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.updatePassword(userId, oldPassword, newPassword));
            assertEquals(ExceptionEnum.USER_OLD_PASSWORD_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteUser 删除用户测试")
    class DeleteUserTests {

        @Test
        @DisplayName("管理员删除用户成功")
        void deleteUser_AdminSuccess() {
            // Arrange
            Integer targetUserId = 2;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.ADMIN.getValue();

            UserEntity targetUser = new UserEntity();
            targetUser.setUserId(targetUserId);
            targetUser.setUserName("targetUser");

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
            doNothing().when(userRepository).delete(targetUser);

            // Act
            userService.deleteUser(targetUserId, currentUserId, currentRole);

            // Assert
            verify(userRepository).delete(targetUser);
        }

        @Test
        @DisplayName("普通用户删除自己成功")
        void deleteUser_SelfDeleteSuccess() {
            // Arrange
            Integer targetUserId = 1;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).delete(testUser);

            // Act
            userService.deleteUser(targetUserId, currentUserId, currentRole);

            // Assert
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("普通用户删除他人失败 - 权限不足")
        void deleteUser_NonAdminDeleteOthersFailed() {
            // Arrange
            Integer targetUserId = 2;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.deleteUser(targetUserId, currentUserId, currentRole));
            assertEquals(ExceptionEnum.USER_NOT_ADMIN.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("删除用户失败 - 用户不存在")
        void deleteUser_UserNotExist() {
            // Arrange
            Integer targetUserId = 999;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.ADMIN.getValue();

            when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.deleteUser(targetUserId, currentUserId, currentRole));
            assertEquals(ExceptionEnum.USER_NOT_EXIST.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateUserInfo 修改用户信息测试")
    class UpdateUserInfoTests {

        @Test
        @DisplayName("管理员修改其他用户信息成功")
        void updateUserInfo_AdminUpdateOthersSuccess() {
            // Arrange
            Integer targetUserId = 2;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.ADMIN.getValue();

            UserEntity targetUser = new UserEntity();
            targetUser.setUserId(targetUserId);
            targetUser.setUserName("originalUser");
            targetUser.setDisplayName("Original Name");
            targetUser.setEmail("original@example.com");
            targetUser.setPhone("13900139000");
            targetUser.setRole(UserRoleEnum.USER.getValue());
            targetUser.setStatus(1);

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setUserName("newUsername");
            request.setDisplayName("New Name");
            request.setEmail("new@example.com");

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniquenessForUpdate(any());
            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserEntity result = userService.updateUserInfo(targetUserId, currentUserId, currentRole, request);

            // Assert
            assertNotNull(result);
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("普通用户修改自己信息成功")
        void updateUserInfo_SelfUpdateSuccess() {
            // Arrange
            Integer targetUserId = 1;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("Updated Name");
            request.setOccupation("Engineer");
            request.setSignature("Hello World");

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniquenessForUpdate(any());
            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserEntity result = userService.updateUserInfo(targetUserId, currentUserId, currentRole, request);

            // Assert
            assertNotNull(result);
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("普通用户修改他人信息失败 - 权限不足")
        void updateUserInfo_NonAdminUpdateOthersFailed() {
            // Arrange
            Integer targetUserId = 2;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("New Name");

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.updateUserInfo(targetUserId, currentUserId, currentRole, request));
            assertEquals(ExceptionEnum.USER_NOT_ADMIN.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("修改用户信息失败 - 用户不存在")
        void updateUserInfo_UserNotExist() {
            // Arrange
            Integer targetUserId = 999;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.ADMIN.getValue();

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("New Name");

            when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.updateUserInfo(targetUserId, currentUserId, currentRole, request));
            assertEquals(ExceptionEnum.USER_NOT_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("修改用户信息 - 仅更新部分字段")
        void updateUserInfo_PartialUpdate() {
            // Arrange
            Integer targetUserId = 1;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setPhone("13911139111");
            // 其他字段不设置

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniquenessForUpdate(any());
            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserEntity result = userService.updateUserInfo(targetUserId, currentUserId, currentRole, request);

            // Assert
            assertNotNull(result);
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("修改用户信息失败 - 用户名已存在")
        void updateUserInfo_UsernameAlreadyExists() {
            // Arrange
            Integer targetUserId = 1;
            Integer currentUserId = 1;
            String currentRole = UserRoleEnum.USER.getValue();

            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setUserName("existingUser");

            when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doThrow(new GlobalException(ExceptionEnum.USERNAME_ALREADY_EXIST))
                    .when(userValidationUtils).validateUserAccountUniquenessForUpdate(any());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.updateUserInfo(targetUserId, currentUserId, currentRole, request));
            assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getUserInfo 获取用户信息测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("获取用户信息成功")
        void getUserInfo_Success() {
            // Arrange
            Integer userId = 1;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            ModelApiResponse result = userService.getUserInfo(userId);

            // Assert
            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals("查询成功", result.getMsg());
            assertTrue(result.getData().isPresent());
        }

        @Test
        @DisplayName("获取用户信息失败 - 用户不存在")
        void getUserInfo_UserNotExist() {
            // Arrange
            Integer userId = 999;
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userService.getUserInfo(userId));
            assertEquals(ExceptionEnum.USER_NOT_EXIST.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateUser 校验方法测试")
    class ValidateUserTests {

        @Test
        @DisplayName("validateUser 调用所有校验方法")
        void validateUser_CallsAllValidations() {
            // Arrange
            String rawPassword = "password123";
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validatePassword(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniqueness(any());

            // Act
            userService.validateUser(testUser, rawPassword);

            // Assert
            verify(userValidationUtils).validateUserName(testUser.getUserName());
            verify(userValidationUtils).validatePassword(rawPassword);
            verify(userValidationUtils).validateEmail(testUser.getEmail());
            verify(userValidationUtils).validatePhone(testUser.getPhone());
            verify(userValidationUtils).validateBirthday(testUser.getBirthday());
            verify(userValidationUtils).validateGender(testUser.getGender());
            verify(userValidationUtils).validateRole(testUser.getRole());
            verify(userValidationUtils).validateUserAccountUniqueness(testUser);
        }

        @Test
        @DisplayName("validateUserUpdate 调用所有更新校验方法")
        void validateUserUpdate_CallsAllValidations() {
            // Arrange
            doNothing().when(userValidationUtils).validateUserName(anyString());
            doNothing().when(userValidationUtils).validateEmail(anyString());
            doNothing().when(userValidationUtils).validatePhone(anyString());
            doNothing().when(userValidationUtils).validateBirthday(any());
            doNothing().when(userValidationUtils).validateGender(anyString());
            doNothing().when(userValidationUtils).validateRole(anyString());
            doNothing().when(userValidationUtils).validateUserAccountUniquenessForUpdate(any());

            // Act
            userService.validateUserUpdate(testUser);

            // Assert
            verify(userValidationUtils).validateUserName(testUser.getUserName());
            verify(userValidationUtils).validateEmail(testUser.getEmail());
            verify(userValidationUtils).validatePhone(testUser.getPhone());
            verify(userValidationUtils).validateBirthday(testUser.getBirthday());
            verify(userValidationUtils).validateGender(testUser.getGender());
            verify(userValidationUtils).validateRole(testUser.getRole());
            verify(userValidationUtils).validateUserAccountUniquenessForUpdate(testUser);
        }
    }
}

