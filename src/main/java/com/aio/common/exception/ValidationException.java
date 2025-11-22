package com.aio.common.exception;

import com.aio.common.enums.ValidationExceptionEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校验异常类
 */
public class ValidationException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 20250705L;

    final Integer errorCode;

    public ValidationException() {
        super();
        this.errorCode = ValidationExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public Integer getCode() { return this.errorCode; }

    public ValidationException(String message) {
        super(message);
        this.errorCode = ValidationExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public ValidationException(ValidationExceptionEnum resultCode) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
    }

    public ValidationException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
