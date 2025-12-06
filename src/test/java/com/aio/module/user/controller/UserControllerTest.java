package com.aio.module.user.controller;

import com.aio.api.model.*;
import com.aio.common.enums.ExceptionEnum;
import com.aio.common.exception.GlobalException;
import com.aio.common.security.SecurityContextUtils;
import com.aio.common.util.JwtUtils;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.enums.UserRoleEnum;
import com.aio.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserController userController;

    private UserEntity testUser;

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
    }

    @Nested
    @DisplayName("register 注册接口测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void register_Success() {
            // Arrange
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUserName("newuser");
            request.setDisplayName("New User");
            request.setEmail("new@example.com");
            request.setPhone("13900139000");
            request.setPassword("password123");
            request.setGender("M");
            request.setBirthday(LocalDate.of(1995, 5, 15));
            request.setOccupation("Engineer");
            request.setSignature("Hello World");

            when(userService.register(any(UserEntity.class), eq("password123"))).thenReturn(testUser);

            // Act
            ResponseEntity<ModelApiResponse> response = userController.register(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(201, response.getBody().getCode());
            assertEquals("用户注册成功", response.getBody().getMsg());
            assertTrue(response.getBody().getSuccess());
            assertTrue(response.getBody().getData().isPresent());
        }

        @Test
        @DisplayName("注册成功 - 无可选字段")
        void register_SuccessWithoutOptionalFields() {
            // Arrange
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUserName("newuser");
            request.setDisplayName("New User");
            request.setEmail("new@example.com");
            request.setPassword("password123");
            // 不设置可选字段：phone, gender, birthday, occupation, signature

            when(userService.register(any(UserEntity.class), eq("password123"))).thenReturn(testUser);

            // Act
            ResponseEntity<ModelApiResponse> response = userController.register(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(201, response.getBody().getCode());
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void register_UsernameExists() {
            // Arrange
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUserName("existinguser");
            request.setDisplayName("Existing User");
            request.setEmail("existing@example.com");
            request.setPassword("password123");

            when(userService.register(any(UserEntity.class), eq("password123")))
                    .thenThrow(new GlobalException(ExceptionEnum.USERNAME_ALREADY_EXIST));

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userController.register(request));
            assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("注册失败 - 密码格式错误")
        void register_InvalidPassword() {
            // Arrange
            UserRegisterRequest request = new UserRegisterRequest();
            request.setUserName("newuser");
            request.setDisplayName("New User");
            request.setEmail("new@example.com");
            request.setPassword("123");

            when(userService.register(any(UserEntity.class), eq("123")))
                    .thenThrow(new GlobalException(ExceptionEnum.PASSWORD_VALIDATION_ERROR));

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userController.register(request));
            assertEquals(ExceptionEnum.PASSWORD_VALIDATION_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("login 登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void login_Success() {
            // Arrange
            UserLoginRequest request = new UserLoginRequest();
            request.setAccount("testuser");
            request.setPassword("password123");

            String token = "jwt.token.here";

            when(userService.login("testuser", "password123")).thenReturn(testUser);
            when(jwtUtils.generateToken(testUser.getUserId(), testUser.getRole())).thenReturn(token);

            // Act
            ResponseEntity<ModelApiResponse> response = userController.login(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());
            assertEquals("登录成功", response.getBody().getMsg());
            assertTrue(response.getBody().getSuccess());
            assertTrue(response.getBody().getData().isPresent());

            Object data = response.getBody().getData().get();
            assertTrue(data instanceof UserLoginDataResponse);
            UserLoginDataResponse loginData = (UserLoginDataResponse) data;
            assertEquals(token, loginData.getToken());
            assertEquals(testUser.getUserId(), loginData.getUserId());
            assertEquals(testUser.getUserName(), loginData.getUserName());
            assertEquals(testUser.getRole(), loginData.getRole());
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void login_UserNotExist() {
            // Arrange
            UserLoginRequest request = new UserLoginRequest();
            request.setAccount("nonexistent");
            request.setPassword("password123");

            when(userService.login("nonexistent", "password123"))
                    .thenThrow(new GlobalException(ExceptionEnum.USER_NOT_EXIST));

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userController.login(request));
            assertEquals(ExceptionEnum.USER_NOT_EXIST.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void login_PasswordError() {
            // Arrange
            UserLoginRequest request = new UserLoginRequest();
            request.setAccount("testuser");
            request.setPassword("wrongpassword");

            when(userService.login("testuser", "wrongpassword"))
                    .thenThrow(new GlobalException(ExceptionEnum.USER_PASSWORD_ERROR));

            // Act & Assert
            GlobalException exception = assertThrows(GlobalException.class,
                    () -> userController.login(request));
            assertEquals(ExceptionEnum.USER_PASSWORD_ERROR.getMessage(), exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updatePassword 修改密码接口测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void updatePassword_Success() {
            // Arrange
            UpdatePasswordRequest request = new UpdatePasswordRequest();
            request.setOldPassword("oldPassword");
            request.setNewPassword("newPassword");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                doNothing().when(userService).updatePassword(1, "oldPassword", "newPassword");

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updatePassword(request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(200, response.getBody().getCode());
                assertEquals("密码修改成功", response.getBody().getMsg());
                assertTrue(response.getBody().getSuccess());
            }
        }

        @Test
        @DisplayName("修改密码失败 - 未认证")
        void updatePassword_Unauthorized() {
            // Arrange
            UpdatePasswordRequest request = new UpdatePasswordRequest();
            request.setOldPassword("oldPassword");
            request.setNewPassword("newPassword");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(null);

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updatePassword(request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(401, response.getBody().getCode());
                assertEquals("未认证或认证已过期", response.getBody().getMsg());
            }
        }

        @Test
        @DisplayName("修改密码失败 - 旧密码错误")
        void updatePassword_OldPasswordError() {
            // Arrange
            UpdatePasswordRequest request = new UpdatePasswordRequest();
            request.setOldPassword("wrongOldPassword");
            request.setNewPassword("newPassword");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                doThrow(new GlobalException(ExceptionEnum.USER_OLD_PASSWORD_ERROR))
                        .when(userService).updatePassword(1, "wrongOldPassword", "newPassword");

                // Act & Assert
                GlobalException exception = assertThrows(GlobalException.class,
                        () -> userController.updatePassword(request));
                assertEquals(ExceptionEnum.USER_OLD_PASSWORD_ERROR.getMessage(), exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("deleteUser 删除用户接口测试")
    class DeleteUserTests {

        @Test
        @DisplayName("管理员删除用户成功")
        void deleteUser_AdminSuccess() {
            // Arrange
            Integer userId = 2;

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.ADMIN.getValue());
                doNothing().when(userService).deleteUser(userId, 1, UserRoleEnum.ADMIN.getValue());

                // Act
                ResponseEntity<ModelApiResponse> response = userController.deleteUser(userId);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(200, response.getBody().getCode());
                assertEquals("用户删除成功", response.getBody().getMsg());
                assertTrue(response.getBody().getSuccess());
            }
        }

        @Test
        @DisplayName("普通用户删除自己成功")
        void deleteUser_SelfDeleteSuccess() {
            // Arrange
            Integer userId = 1;

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());
                doNothing().when(userService).deleteUser(userId, 1, UserRoleEnum.USER.getValue());

                // Act
                ResponseEntity<ModelApiResponse> response = userController.deleteUser(userId);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(200, response.getBody().getCode());
            }
        }

        @Test
        @DisplayName("删除用户失败 - 未认证（userId为null）")
        void deleteUser_UnauthorizedNullUserId() {
            // Arrange
            Integer userId = 2;

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(null);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());

                // Act
                ResponseEntity<ModelApiResponse> response = userController.deleteUser(userId);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("删除用户失败 - 未认证（role为null）")
        void deleteUser_UnauthorizedNullRole() {
            // Arrange
            Integer userId = 2;

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(null);

                // Act
                ResponseEntity<ModelApiResponse> response = userController.deleteUser(userId);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("删除用户失败 - 权限不足")
        void deleteUser_PermissionDenied() {
            // Arrange
            Integer userId = 2;

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());
                doThrow(new GlobalException(ExceptionEnum.USER_NOT_ADMIN))
                        .when(userService).deleteUser(userId, 1, UserRoleEnum.USER.getValue());

                // Act & Assert
                GlobalException exception = assertThrows(GlobalException.class,
                        () -> userController.deleteUser(userId));
                assertEquals(ExceptionEnum.USER_NOT_ADMIN.getMessage(), exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("updateUserInfo 修改用户信息接口测试")
    class UpdateUserInfoTests {

        @Test
        @DisplayName("修改用户信息成功")
        void updateUserInfo_Success() {
            // Arrange
            Integer userId = 1;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("Updated Name");
            request.setOccupation("Developer");
            request.setSignature("New Signature");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());
                when(userService.updateUserInfo(userId, 1, UserRoleEnum.USER.getValue(), request))
                        .thenReturn(testUser);

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updateUserInfo(userId, request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals(200, response.getBody().getCode());
                assertEquals("用户信息修改成功", response.getBody().getMsg());
                assertTrue(response.getBody().getSuccess());
                assertTrue(response.getBody().getData().isPresent());
            }
        }

        @Test
        @DisplayName("管理员修改其他用户信息成功")
        void updateUserInfo_AdminUpdateOthers() {
            // Arrange
            Integer userId = 2;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setUserName("newUsername");
            request.setEmail("newemail@example.com");

            UserEntity updatedUser = new UserEntity();
            updatedUser.setUserId(2);
            updatedUser.setUserName("newUsername");
            updatedUser.setEmail("newemail@example.com");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.ADMIN.getValue());
                when(userService.updateUserInfo(userId, 1, UserRoleEnum.ADMIN.getValue(), request))
                        .thenReturn(updatedUser);

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updateUserInfo(userId, request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.OK, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("修改用户信息失败 - 未认证（userId为null）")
        void updateUserInfo_UnauthorizedNullUserId() {
            // Arrange
            Integer userId = 1;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("Updated Name");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(null);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updateUserInfo(userId, request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("修改用户信息失败 - 未认证（role为null）")
        void updateUserInfo_UnauthorizedNullRole() {
            // Arrange
            Integer userId = 1;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("Updated Name");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(null);

                // Act
                ResponseEntity<ModelApiResponse> response = userController.updateUserInfo(userId, request);

                // Assert
                assertNotNull(response);
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            }
        }

        @Test
        @DisplayName("修改用户信息失败 - 权限不足")
        void updateUserInfo_PermissionDenied() {
            // Arrange
            Integer userId = 2;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setDisplayName("Updated Name");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());
                when(userService.updateUserInfo(userId, 1, UserRoleEnum.USER.getValue(), request))
                        .thenThrow(new GlobalException(ExceptionEnum.USER_NOT_ADMIN));

                // Act & Assert
                GlobalException exception = assertThrows(GlobalException.class,
                        () -> userController.updateUserInfo(userId, request));
                assertEquals(ExceptionEnum.USER_NOT_ADMIN.getMessage(), exception.getMessage());
            }
        }

        @Test
        @DisplayName("修改用户信息失败 - 用户名已存在")
        void updateUserInfo_UsernameExists() {
            // Arrange
            Integer userId = 1;
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            request.setUserName("existingUser");

            try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserId()).thenReturn(1);
                mockedStatic.when(() -> SecurityContextUtils.getCurrentUserRole()).thenReturn(UserRoleEnum.USER.getValue());
                when(userService.updateUserInfo(userId, 1, UserRoleEnum.USER.getValue(), request))
                        .thenThrow(new GlobalException(ExceptionEnum.USERNAME_ALREADY_EXIST));

                // Act & Assert
                GlobalException exception = assertThrows(GlobalException.class,
                        () -> userController.updateUserInfo(userId, request));
                assertEquals(ExceptionEnum.USERNAME_ALREADY_EXIST.getMessage(), exception.getMessage());
            }
        }
    }
}

