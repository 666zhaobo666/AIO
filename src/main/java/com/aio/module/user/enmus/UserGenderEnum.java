package com.aio.module.user.enmus;

import lombok.Getter;

/**
 * 用户性别枚举
 */
@Getter
public enum UserGenderEnum {
    M("M"),
    F("F"),
    U("U");

    private final String gender;

    UserGenderEnum(String gender) { this.gender = gender; }

    public String getValue() { return gender; }

    public static String getByGender(String code) {
        for (UserGenderEnum value : values()) {
            if (value.getGender().equals(code)) {
                return value.getGender();
            }
        }
        return code;
    }

    public static boolean isValid(String gender) {
        for (UserGenderEnum value : values()) {
            if (value.getGender().equals(gender)) {
                return true;
            }
        }
        return false;
    }
}
