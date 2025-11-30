package com.aio.module.user.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户异常枚举测试
 */
class UserExceptionEnumTest {

    @Test
    void testSuccessEnum() {
        // 测试成功枚举
        assertEquals(true, UserExceptionEnum.SUCCESS.getStatus());
        assertEquals(200, UserExceptionEnum.SUCCESS.getCode());
        assertEquals("success", UserExceptionEnum.SUCCESS.getMessage());
    }

    @Test
    void testUnknownErrorEnum() {
        // 测试未知错误枚举
        assertEquals(false, UserExceptionEnum.UNKNOWN_ERROR.getStatus());
        assertEquals(500, UserExceptionEnum.UNKNOWN_ERROR.getCode());
        assertTrue(UserExceptionEnum.UNKNOWN_ERROR.getMessage().contains("unknown error"));
    }

    @Test
    void testUsernameValidationError() {
        // 测试用户名验证错误
        assertEquals(false, UserExceptionEnum.USERNAME_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.USERNAME_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.USERNAME_VALIDATION_ERROR.getMessage().contains("用户名格式错误"));
    }

    @Test
    void testPasswordValidationError() {
        // 测试密码验证错误
        assertEquals(false, UserExceptionEnum.PASSWORD_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.PASSWORD_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.PASSWORD_VALIDATION_ERROR.getMessage().contains("密码格式错误"));
    }

    @Test
    void testEmailValidationError() {
        // 测试邮箱验证错误
        assertEquals(false, UserExceptionEnum.EMAIL_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.EMAIL_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.EMAIL_VALIDATION_ERROR.getMessage().contains("邮箱格式错误"));
    }

    @Test
    void testPhoneValidationError() {
        // 测试手机号验证错误
        assertEquals(false, UserExceptionEnum.PHONE_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.PHONE_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.PHONE_VALIDATION_ERROR.getMessage().contains("手机号格式错误"));
    }

    @Test
    void testBirthdayValidationError() {
        // 测试生日验证错误
        assertEquals(false, UserExceptionEnum.BIRTHDAY_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.BIRTHDAY_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.BIRTHDAY_VALIDATION_ERROR.getMessage().contains("生日"));
    }

    @Test
    void testGenderValidationError() {
        // 测试性别验证错误
        assertEquals(false, UserExceptionEnum.GENDER_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.GENDER_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.GENDER_VALIDATION_ERROR.getMessage().contains("性别错误"));
    }

    @Test
    void testRoleValidationError() {
        // 测试角色验证错误
        assertEquals(false, UserExceptionEnum.ROLE_VALIDATION_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.ROLE_VALIDATION_ERROR.getCode());
        assertTrue(UserExceptionEnum.ROLE_VALIDATION_ERROR.getMessage().contains("用户角色错误"));
    }

    @Test
    void testUsernameAlreadyExist() {
        // 测试用户名已存在
        assertEquals(false, UserExceptionEnum.USERNAME_ALREADY_EXIST.getStatus());
        assertEquals(400, UserExceptionEnum.USERNAME_ALREADY_EXIST.getCode());
        assertTrue(UserExceptionEnum.USERNAME_ALREADY_EXIST.getMessage().contains("用户名已存在"));
    }

    @Test
    void testEmailAlreadyExist() {
        // 测试邮箱已存在
        assertEquals(false, UserExceptionEnum.EMAIL_ALREADY_EXIST.getStatus());
        assertEquals(400, UserExceptionEnum.EMAIL_ALREADY_EXIST.getCode());
        assertTrue(UserExceptionEnum.EMAIL_ALREADY_EXIST.getMessage().contains("邮箱已存在"));
    }

    @Test
    void testPhoneAlreadyExist() {
        // 测试手机号已存在
        assertEquals(false, UserExceptionEnum.PHONE_ALREADY_EXIST.getStatus());
        assertEquals(400, UserExceptionEnum.PHONE_ALREADY_EXIST.getCode());
        assertTrue(UserExceptionEnum.PHONE_ALREADY_EXIST.getMessage().contains("手机号已存在"));
    }

    @Test
    void testUserNotExist() {
        // 测试用户不存在
        assertEquals(false, UserExceptionEnum.USER_NOT_EXIST.getStatus());
        assertEquals(400, UserExceptionEnum.USER_NOT_EXIST.getCode());
        assertTrue(UserExceptionEnum.USER_NOT_EXIST.getMessage().contains("用户不存在"));
    }

    @Test
    void testUserPasswordError() {
        // 测试用户密码错误
        assertEquals(false, UserExceptionEnum.USER_PASSWORD_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.USER_PASSWORD_ERROR.getCode());
        assertTrue(UserExceptionEnum.USER_PASSWORD_ERROR.getMessage().contains("密码错误"));
    }

    @Test
    void testUserOldPasswordError() {
        // 测试用户旧密码错误
        assertEquals(false, UserExceptionEnum.USER_OLD_PASSWORD_ERROR.getStatus());
        assertEquals(400, UserExceptionEnum.USER_OLD_PASSWORD_ERROR.getCode());
        assertTrue(UserExceptionEnum.USER_OLD_PASSWORD_ERROR.getMessage().contains("旧密码错误"));
    }

    @Test
    void testUserPasswordNotExist() {
        // 测试用户密码不存在
        assertEquals(false, UserExceptionEnum.USER_PASSWORD_NOT_EXIST.getStatus());
        assertEquals(400, UserExceptionEnum.USER_PASSWORD_NOT_EXIST.getCode());
        assertTrue(UserExceptionEnum.USER_PASSWORD_NOT_EXIST.getMessage().contains("密码不存在"));
    }

    @Test
    void testUserDisabled() {
        // 测试用户已禁用
        assertEquals(false, UserExceptionEnum.USER_DISABLED.getStatus());
        assertEquals(400, UserExceptionEnum.USER_DISABLED.getCode());
        assertTrue(UserExceptionEnum.USER_DISABLED.getMessage().contains("已禁用"));
    }

    @Test
    void testUserNotAdmin() {
        // 测试用户非管理员
        assertEquals(false, UserExceptionEnum.USER_NOT_ADMIN.getStatus());
        assertEquals(403, UserExceptionEnum.USER_NOT_ADMIN.getCode());
        assertTrue(UserExceptionEnum.USER_NOT_ADMIN.getMessage().contains("非管理员"));
    }

    @Test
    void testAuthorizationFailed() {
        // 测试授权失败
        assertEquals(false, UserExceptionEnum.AUTHORIZATION_FAILED.getStatus());
        assertEquals(401, UserExceptionEnum.AUTHORIZATION_FAILED.getCode());
        assertTrue(UserExceptionEnum.AUTHORIZATION_FAILED.getMessage().contains("Authorization Failed"));
    }

    @Test
    void testAllErrorStatusAreFalse() {
        // 测试所有错误状态都是false（除了SUCCESS）
        for (UserExceptionEnum exception : UserExceptionEnum.values()) {
            if (exception != UserExceptionEnum.SUCCESS) {
                assertFalse(exception.getStatus());
            }
        }
    }

    @Test
    void testAllExceptionsHaveValidCodes() {
        // 测试所有异常都有有效的错误码
        for (UserExceptionEnum exception : UserExceptionEnum.values()) {
            assertNotNull(exception.getCode());
            assertTrue(exception.getCode() >= 200 && exception.getCode() <= 599);
        }
    }

    @Test
    void testAllExceptionsHaveMessages() {
        // 测试所有异常都有消息
        for (UserExceptionEnum exception : UserExceptionEnum.values()) {
            assertNotNull(exception.getMessage());
            assertFalse(exception.getMessage().isEmpty());
        }
    }
}

