package com.aio.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    SUCCESS(true, 200, "success"),
    UNKNOWN_ERROR(false, 500, "unknown error of User Validation"),

    // User Validation
    USERNAME_VALIDATION_ERROR(false, 400, "用户名格式错误，支持字母、数字、下划线，长度4-16位（例：user123、my_name）"),
    PASSWORD_VALIDATION_ERROR(false, 400, "密码格式错误，支持字母、数字、下划线，长度6-16位（例：pass123、pwd_456）"),
    EMAIL_VALIDATION_ERROR(false, 400, "邮箱格式错误，支持字母、数字、.+-_% 字符，需包含合法域名（例：user@example.com、user.name+tag@example-co.cn）"),
    PHONE_VALIDATION_ERROR(false, 400, "手机号格式错误，支持国内11位手机号（可选前缀+86或0），号段为13/14/15/17/18/19开头（例：13992921222、+8613800138000）"),
    BIRTHDAY_VALIDATION_ERROR(false, 400, "生日不能晚于当前日期"),
    GENDER_VALIDATION_ERROR(false, 400, "性别错误"),
    ROLE_VALIDATION_ERROR(false, 400, "用户角色错误"),

    USERNAME_ALREADY_EXIST(false, 400, "用户名已存在"),
    EMAIL_ALREADY_EXIST(false, 400, "邮箱已存在"),
    PHONE_ALREADY_EXIST(false, 400, "手机号已存在"),

    // Authority validation
    USER_NOT_EXIST(false, 400, "用户不存在"),
    USER_PASSWORD_ERROR(false, 400, "用户密码错误"),
    USER_OLD_PASSWORD_ERROR(false, 400, "用户旧密码错误"),
    USER_PASSWORD_NOT_EXIST(false, 400, "用户密码不存在"),
    USER_DISABLED(false, 400, "用户已禁用"),
    USER_NOT_ADMIN(false, 403, "用户权限不足，非管理员"),
    AUTHORIZATION_FAILED(false, 401, "Authorization Failed!"),

    ;

    private final Boolean status;
    private final Integer code;
    private final String message;

    ExceptionEnum(boolean status, Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
