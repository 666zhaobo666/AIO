package com.aio.module.user.controller;

import com.aio.api.user.model.*;
import com.aio.common.security.SecurityContextUtils;
import com.aio.common.util.JwtUtils;
import com.aio.module.user.entity.UserEntity;
import com.aio.module.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户控制器测试
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserController userController;

    private UserEntity testUser;
    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setGender("M");
        testUser.setRole("USER");
        testUser.setStatus(1);

        registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("13800138000");
        registerRequest.setPassword("password123");
        registerRequest.setGender("M");
        registerRequest.setBirthday(LocalDate.of(1990, 1, 1));
        registerRequest.setOccupation("Engineer");
        registerRequest.setSignature("Test signature");

        loginRequest = new UserLoginRequest();
        loginRequest.setAccount("testuser");
        loginRequest.setPassword("password123");
    }

    // ==================== 注册测试 ====================
    @Test
    void testRegisterSuccess() {
        // 测试注册成功
        when(userService.register(any(UserEntity.class), anyString())).thenReturn(testUser);

        ResponseEntity<UserApiResponse> response = userController.register(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getCode());
        assertEquals("用户注册成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(userService).register(any(UserEntity.class), eq("password123"));
    }

    @Test
    void testRegisterWithMinimalInfo() {
        // 测试最小信息注册
        UserRegisterRequest minimalRequest = new UserRegisterRequest();
        minimalRequest.setUsername("newuser");
        minimalRequest.setName("New User");
        minimalRequest.setEmail("new@example.com");
        minimalRequest.setPassword("password123");

        when(userService.register(any(UserEntity.class), anyString())).thenReturn(testUser);

        ResponseEntity<UserApiResponse> response = userController.register(minimalRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService).register(any(UserEntity.class), eq("password123"));
    }

    @Test
    void testRegisterWithNullGender() {
        // 测试性别为null的注册
        registerRequest.setGender(null);

        when(userService.register(any(UserEntity.class), anyString())).thenReturn(testUser);

        ResponseEntity<UserApiResponse> response = userController.register(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService).register(any(UserEntity.class), anyString());
    }

    // ==================== 登录测试 ====================
    @Test
    void testLoginSuccess() {
        // 测试登录成功
        String token = "jwt.token.here";

        when(userService.login("testuser", "password123")).thenReturn(testUser);
        when(jwtUtils.generateToken(testUser.getUserId().toString(), testUser.getRole()))
                .thenReturn(token);

        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(token, response.getBody().getToken());
        assertEquals(testUser.getUserId(), response.getBody().getUserId());
        assertEquals(testUser.getUsername(), response.getBody().getUsername());
        assertEquals(testUser.getRole(), response.getBody().getRole());

        verify(userService).login("testuser", "password123");
        verify(jwtUtils).generateToken(testUser.getUserId().toString(), testUser.getRole());
    }

    @Test
    void testLoginByEmail() {
        // 测试通过邮箱登录
        loginRequest.setAccount("test@example.com");

        when(userService.login("test@example.com", "password123")).thenReturn(testUser);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("token");

        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).login("test@example.com", "password123");
    }

    @Test
    void testLoginByPhone() {
        // 测试通过手机号登录
        loginRequest.setAccount("13800138000");

        when(userService.login("13800138000", "password123")).thenReturn(testUser);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("token");

        ResponseEntity<LoginResponse> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).login("13800138000", "password123");
    }

    // ==================== 修改密码测试 ====================
    @Test
    void testUpdatePasswordSuccess() {
        // 测试修改密码成功
        UpdatePasswordRequest updateRequest = new UpdatePasswordRequest();
        updateRequest.setOldPassword("oldPassword");
        updateRequest.setNewPassword("newPassword");

        UUID userId = testUser.getUserId();

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(userId);

            doNothing().when(userService).updatePassword(userId, "oldPassword", "newPassword");

            ResponseEntity<UserApiResponse> response = userController.updatePassword(updateRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(200, response.getBody().getCode());
            assertEquals("密码修改成功", response.getBody().getMessage());

            verify(userService).updatePassword(userId, "oldPassword", "newPassword");
        }
    }

    @Test
    void testUpdatePasswordNotAuthenticated() {
        // 测试未认证时修改密码
        UpdatePasswordRequest updateRequest = new UpdatePasswordRequest();
        updateRequest.setOldPassword("oldPassword");
        updateRequest.setNewPassword("newPassword");

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(null);

            ResponseEntity<UserApiResponse> response = userController.updatePassword(updateRequest);

            assertNotNull(response);
            assertEquals(401, response.getStatusCode().value());

            assertNotNull(response.getBody());
            assertEquals(401, response.getBody().getCode());
            assertEquals("未认证或认证已过期", response.getBody().getMessage());

            verify(userService, never()).updatePassword(any(), anyString(), anyString());
        }
    }

    // ==================== 删除用户测试 ====================
    @Test
    void testDeleteUserAsAdmin() {
        // 测试管理员删除用户
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = testUser.getUserId();
        String currentRole = "ADMIN";

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(currentUserId);
            mockedStatic.when(SecurityContextUtils::getCurrentUserRole).thenReturn(currentRole);

            doNothing().when(userService).deleteUser(targetUserId, currentUserId, currentRole);

            ResponseEntity<Void> response = userController.deleteUser(targetUserId);

            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

            verify(userService).deleteUser(targetUserId, currentUserId, currentRole);
        }
    }

    @Test
    void testDeleteSelfAsUser() {
        // 测试普通用户删除自己
        UUID userId = testUser.getUserId();
        String currentRole = "USER";

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(userId);
            mockedStatic.when(SecurityContextUtils::getCurrentUserRole).thenReturn(currentRole);

            doNothing().when(userService).deleteUser(userId, userId, currentRole);

            ResponseEntity<Void> response = userController.deleteUser(userId);

            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

            verify(userService).deleteUser(userId, userId, currentRole);
        }
    }

    @Test
    void testDeleteUserNotAuthenticated() {
        // 测试未认证时删除用户
        UUID targetUserId = UUID.randomUUID();

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(null);
            mockedStatic.when(SecurityContextUtils::getCurrentUserRole).thenReturn(null);

            ResponseEntity<Void> response = userController.deleteUser(targetUserId);

            assertNotNull(response);
            assertEquals(401, response.getStatusCode().value());


            verify(userService, never()).deleteUser(any(), any(), anyString());
        }
    }

    @Test
    void testDeleteUserNoRole() {
        // 测试缺少角色信息时删除用户
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = testUser.getUserId();

        try (MockedStatic<SecurityContextUtils> mockedStatic = mockStatic(SecurityContextUtils.class)) {
            mockedStatic.when(SecurityContextUtils::getCurrentUserId).thenReturn(currentUserId);
            mockedStatic.when(SecurityContextUtils::getCurrentUserRole).thenReturn(null);

            ResponseEntity<Void> response = userController.deleteUser(targetUserId);

            assertNotNull(response);
            assertEquals(401, response.getStatusCode().value());


            verify(userService, never()).deleteUser(any(), any(), anyString());
        }
    }
}

