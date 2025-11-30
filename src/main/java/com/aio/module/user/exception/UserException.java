package com.aio.module.user.exception;

import com.aio.module.user.enums.UserExceptionEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校验异常类
 */
public class UserException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 20250705L;

    final Integer errorCode;

    public UserException() {
        super();
        this.errorCode = UserExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public Integer getCode() { return this.errorCode; }

    public UserException(String message) {
        super(message);
        this.errorCode = UserExceptionEnum.UNKNOWN_ERROR.getCode();
    }

    public UserException(UserExceptionEnum resultCode) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
    }

    public UserException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
