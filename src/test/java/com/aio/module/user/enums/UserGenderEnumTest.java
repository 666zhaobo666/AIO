package com.aio.module.user.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户性别枚举测试
 */
class UserGenderEnumTest {

    @Test
    void testMaleGenderValue() {
        // 测试男性性别值
        assertEquals("M", UserGenderEnum.M.getValue());
        assertEquals("M", UserGenderEnum.M.getGender());
    }

    @Test
    void testFemaleGenderValue() {
        // 测试女性性别值
        assertEquals("F", UserGenderEnum.F.getValue());
        assertEquals("F", UserGenderEnum.F.getGender());
    }

    @Test
    void testUnknownGenderValue() {
        // 测试未知性别值
        assertEquals("U", UserGenderEnum.U.getValue());
        assertEquals("U", UserGenderEnum.U.getGender());
    }

    @Test
    void testGetByGenderForValidGender() {
        // 测试获取有效性别
        assertEquals("M", UserGenderEnum.getByGender("M"));
        assertEquals("F", UserGenderEnum.getByGender("F"));
        assertEquals("U", UserGenderEnum.getByGender("U"));
    }

    @Test
    void testGetByGenderForInvalidGender() {
        // 测试获取无效性别时返回原值
        String invalidGender = "X";
        assertEquals(invalidGender, UserGenderEnum.getByGender(invalidGender));
    }

    @Test
    void testIsValidForValidGenders() {
        // 测试有效性别验证
        assertTrue(UserGenderEnum.isValid("M"));
        assertTrue(UserGenderEnum.isValid("F"));
        assertTrue(UserGenderEnum.isValid("U"));
    }

    @Test
    void testIsValidForInvalidGenders() {
        // 测试无效性别验证
        assertFalse(UserGenderEnum.isValid("X"));
        assertFalse(UserGenderEnum.isValid("male"));
        assertFalse(UserGenderEnum.isValid("m"));
        assertFalse(UserGenderEnum.isValid(""));
        assertFalse(UserGenderEnum.isValid(null));
    }

    @Test
    void testEnumValues() {
        // 测试枚举所有值
        UserGenderEnum[] genders = UserGenderEnum.values();
        assertEquals(3, genders.length);
    }

    @Test
    void testEnumValueOf() {
        // 测试valueOf方法
        assertEquals(UserGenderEnum.M, UserGenderEnum.valueOf("M"));
        assertEquals(UserGenderEnum.F, UserGenderEnum.valueOf("F"));
        assertEquals(UserGenderEnum.U, UserGenderEnum.valueOf("U"));
    }

    @Test
    void testEnumValueOfInvalid() {
        // 测试valueOf方法抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            UserGenderEnum.valueOf("INVALID");
        });
    }

    @Test
    void testCaseSensitivity() {
        // 测试大小写敏感性
        assertFalse(UserGenderEnum.isValid("m"));
        assertFalse(UserGenderEnum.isValid("f"));
        assertFalse(UserGenderEnum.isValid("u"));
    }

    @Test
    void testAllGendersAreValid() {
        // 测试所有枚举值都是有效的
        for (UserGenderEnum gender : UserGenderEnum.values()) {
            assertTrue(UserGenderEnum.isValid(gender.getGender()));
        }
    }
}

