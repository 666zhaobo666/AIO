package com.aio.module.user.exception;

import com.aio.module.user.enmus.UserExceptionEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户异常类测试
 */
class UserExceptionTest {

    @Test
    void testDefaultConstructor() {
        // 测试默认构造函数
        UserException exception = new UserException();

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertEquals(UserExceptionEnum.UNKNOWN_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testConstructorWithMessage() {
        // 测试带消息的构造函数
        String message = "Test error message";
        UserException exception = new UserException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(UserExceptionEnum.UNKNOWN_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testConstructorWithEnum() {
        // 测试带枚举的构造函数
        UserException exception = new UserException(UserExceptionEnum.USERNAME_VALIDATION_ERROR);

        assertNotNull(exception);
        assertEquals(UserExceptionEnum.USERNAME_VALIDATION_ERROR.getMessage(), exception.getMessage());
        assertEquals(UserExceptionEnum.USERNAME_VALIDATION_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testConstructorWithCodeAndMessage() {
        // 测试带错误码和消息的构造函数
        Integer errorCode = 404;
        String message = "Not found";
        UserException exception = new UserException(errorCode, message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getCode());
    }

    @Test
    void testDifferentExceptionEnums() {
        // 测试不同的异常枚举
        UserException exception1 = new UserException(UserExceptionEnum.EMAIL_VALIDATION_ERROR);
        UserException exception2 = new UserException(UserExceptionEnum.PHONE_VALIDATION_ERROR);

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals(exception1.getCode(), exception2.getCode()); // 都是400
    }

    @Test
    void testAuthorizationFailedException() {
        // 测试授权失败异常
        UserException exception = new UserException(UserExceptionEnum.AUTHORIZATION_FAILED);

        assertEquals(401, exception.getCode());
        assertTrue(exception.getMessage().contains("Authorization Failed"));
    }

    @Test
    void testUserNotAdminException() {
        // 测试非管理员异常
        UserException exception = new UserException(UserExceptionEnum.USER_NOT_ADMIN);

        assertEquals(403, exception.getCode());
        assertTrue(exception.getMessage().contains("非管理员"));
    }

    @Test
    void testGetCodeNeverReturnsNull() {
        // 测试getCode永远不返回null
        UserException exception1 = new UserException();
        UserException exception2 = new UserException("test");
        UserException exception3 = new UserException(UserExceptionEnum.SUCCESS);
        UserException exception4 = new UserException(500, "error");

        assertNotNull(exception1.getCode());
        assertNotNull(exception2.getCode());
        assertNotNull(exception3.getCode());
        assertNotNull(exception4.getCode());
    }

    @Test
    void testThrowAndCatch() {
        // 测试抛出和捕获异常
        assertThrows(UserException.class, () -> {
            throw new UserException(UserExceptionEnum.USERNAME_VALIDATION_ERROR);
        });
    }

    @Test
    void testExceptionCanBeThrown() {
        // 测试异常可以被抛出
        UserException exception = assertThrows(UserException.class, () -> {
            throw new UserException(UserExceptionEnum.USER_NOT_EXIST);
        });

        assertEquals(UserExceptionEnum.USER_NOT_EXIST.getMessage(), exception.getMessage());
        assertEquals(UserExceptionEnum.USER_NOT_EXIST.getCode(), exception.getCode());
    }
}

