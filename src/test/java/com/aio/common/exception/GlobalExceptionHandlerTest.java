package com.aio.common.exception;

import com.aio.api.user.model.UserApiResponse;
import com.aio.module.user.exception.UserException;
import com.aio.module.user.enums.UserExceptionEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 全局异常处理器测试
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleUserException() {
        // 测试处理UserException
        UserException exception = new UserException(UserExceptionEnum.USERNAME_VALIDATION_ERROR);

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleUserException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UserExceptionEnum.USERNAME_VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertEquals(UserExceptionEnum.USERNAME_VALIDATION_ERROR.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandleUserExceptionWithCustomMessage() {
        // 测试处理自定义消息的UserException
        String customMessage = "Custom error message";
        UserException exception = new UserException(customMessage);

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleUserException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleUserExceptionWithCode() {
        // 测试处理带错误码的UserException
        Integer errorCode = 403;
        String message = "Forbidden";
        UserException exception = new UserException(errorCode, message);

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleUserException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorCode, response.getBody().getCode());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // 测试处理参数校验异常
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        FieldError fieldError = new FieldError("user", "username", "用户名不能为空");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("参数校验失败"));
        assertTrue(response.getBody().getMessage().contains("用户名不能为空"));
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionMultipleErrors() {
        // 测试处理多个参数校验错误
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        FieldError error1 = new FieldError("user", "username", "用户名不能为空");
        FieldError error2 = new FieldError("user", "email", "邮箱格式错误");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("用户名不能为空"));
        assertTrue(response.getBody().getMessage().contains("邮箱格式错误"));
    }

    @Test
    void testHandleBindException() {
        // 测试处理绑定异常
        BindException exception = mock(BindException.class);

        FieldError fieldError = new FieldError("user", "phone", "手机号格式错误");
        when(exception.getBindingResult()).thenReturn(exception);
        when(exception.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleBindException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("参数绑定失败"));
        assertTrue(response.getBody().getMessage().contains("手机号格式错误"));
    }

    @Test
    void testHandleConstraintViolationException() {
        // 测试处理约束违反异常
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("密码长度必须在6-16之间");
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleConstraintViolationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("参数校验失败"));
        assertTrue(response.getBody().getMessage().contains("密码长度必须在6-16之间"));
    }

    @Test
    void testHandleGeneralException() {
        // 测试处理其他未知异常
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("系统内部错误", response.getBody().getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        // 测试处理运行时异常
        RuntimeException exception = new RuntimeException("Runtime error");

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("系统内部错误", response.getBody().getMessage());
    }

    @Test
    void testHandleNullPointerException() {
        // 测试处理空指针异常
        NullPointerException exception = new NullPointerException("Null pointer");

        ResponseEntity<UserApiResponse> response = globalExceptionHandler.handleException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
    }
}

