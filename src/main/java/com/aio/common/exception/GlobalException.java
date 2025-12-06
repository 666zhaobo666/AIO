package com.aio.common.exception;

import com.aio.common.enums.ExceptionEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用异常类
 */
public class GlobalException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 20250706L;

    private final Integer errorCode;

    public GlobalException() {
        super();
        this.errorCode = ExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public Integer getCode() {
        return this.errorCode;
    }

    public GlobalException(String message) {
        super(message);
        this.errorCode = ExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public GlobalException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.errorCode = exceptionEnum.getCode();
    }

    public GlobalException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

