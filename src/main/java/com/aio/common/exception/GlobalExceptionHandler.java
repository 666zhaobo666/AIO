package com.aio.common.exception;

import com.aio.api.model.ModelApiResponse;
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
    public ResponseEntity<ModelApiResponse> handleUserException(UserException e) {
        log.warn("校验异常: {}", e.getMessage());
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(e.getCode() != null ? e.getCode() : 400);
        response.setMsg(e.getMessage());
        response.setSuccess(false);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ModelApiResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(400);
        response.setMsg("参数校验失败: " + message);
        response.setSuccess(false);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ModelApiResponse> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(400);
        response.setMsg("参数绑定失败: " + message);
        response.setSuccess(false);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ModelApiResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束违反: {}", message);

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(400);
        response.setMsg("参数校验失败: " + message);
        response.setSuccess(false);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ModelApiResponse> handleException(Exception e) {
        log.error("系统异常", e);
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(500);
        response.setMsg("系统内部错误");
        response.setSuccess(false);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}