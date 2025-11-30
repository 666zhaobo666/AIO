package com.aio.common.exception;

import com.aio.api.user.model.UserApiResponse;
import com.aio.module.user.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理UserException异常
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<UserApiResponse> handleUserException(UserException e) {
        log.warn("校验异常: {}", e.getMessage());
        UserApiResponse response = new UserApiResponse();
        response.setCode(e.getCode() != null ? e.getCode() : 400);
        response.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserApiResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);

        UserApiResponse response = new UserApiResponse();
        response.setCode(400);
        response.setMessage("参数校验失败: " + message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<UserApiResponse> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);

        UserApiResponse response = new UserApiResponse();
        response.setCode(400);
        response.setMessage("参数绑定失败: " + message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<UserApiResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束违反: {}", message);

        UserApiResponse response = new UserApiResponse();
        response.setCode(400);
        response.setMessage("参数校验失败: " + message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UserApiResponse> handleException(Exception e) {
        log.error("系统异常", e);
        UserApiResponse response = new UserApiResponse();
        response.setCode(500);
        response.setMessage("系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}