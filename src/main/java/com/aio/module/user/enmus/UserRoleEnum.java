package com.aio.module.user.enmus;

import lombok.Getter;

/**
 * 用户身份枚举
 */

@Getter
public enum UserRoleEnum {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;

    UserRoleEnum(String role) { this.role = role; }

    public String getValue() { return role; }

    public static String getByRole(String code) {
        for (UserRoleEnum value : values()) {
            if (value.getValue().equals(code)) {
                return value.getValue();
            }
        }
        return code;
    }

    public static boolean isValid(String role) {
        for (UserRoleEnum value : values()) {
            if (value.getValue().equals(role)) {
                return true;
            }
        }
        return false;
    }

}
