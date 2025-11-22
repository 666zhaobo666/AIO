package com.aio.common.enums;

import lombok.Getter;

@Getter
public enum ValidationExceptionEnum {
    SUCCESS(true, 200, "success"),
    UNKNOWN_ERROR(false, 500, "unknown error of User Validation"),

    // User Validation
// 补充格式说明后的枚举值（保持原有结构，仅修改提示文案）
    USERNAME_VALIDATION_ERROR(false, 400, "用户名格式错误，支持字母、数字、下划线，长度4-16位（例：user123、my_name）"),
    PASSWORD_VALIDATION_ERROR(false, 400, "密码格式错误，支持字母、数字、下划线，长度6-16位（例：pass123、pwd_456）"),
    EMAIL_VALIDATION_ERROR(false, 400, "邮箱格式错误，支持字母、数字、.+-_% 字符，需包含合法域名（例：user@example.com、user.name+tag@example-co.cn）"),
    PHONE_VALIDATION_ERROR(false, 400, "手机号格式错误，支持国内11位手机号（可选前缀+86或0），号段为13/14/15/17/18/19开头（例：13992921222、+8613800138000）"),
    BIRTHDAY_VALIDATION_ERROR(false, 400, "生日不能晚于当前日期"),
    GENDER_VALIDATION_ERROR(false, 400, "性别错误"),
    ROLE_VALIDATION_ERROR(false, 400, "用户角色错误"),
    ;

    private final Boolean status;
    private final Integer code;
    private final String message;

    ValidationExceptionEnum(boolean status, Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
