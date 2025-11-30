package com.aio.module.user.enmus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户角色枚举测试
 */
class UserRoleEnumTest {

    @Test
    void testUserRoleValue() {
        // 测试USER角色值
        assertEquals("USER", UserRoleEnum.USER.getValue());
        assertEquals("USER", UserRoleEnum.USER.getRole());
    }

    @Test
    void testAdminRoleValue() {
        // 测试ADMIN角色值
        assertEquals("ADMIN", UserRoleEnum.ADMIN.getValue());
        assertEquals("ADMIN", UserRoleEnum.ADMIN.getRole());
    }

    @Test
    void testGetByRoleForValidRole() {
        // 测试获取有效角色
        assertEquals("USER", UserRoleEnum.getByRole("USER"));
        assertEquals("ADMIN", UserRoleEnum.getByRole("ADMIN"));
    }

    @Test
    void testGetByRoleForInvalidRole() {
        // 测试获取无效角色时返回原值
        String invalidRole = "INVALID_ROLE";
        assertEquals(invalidRole, UserRoleEnum.getByRole(invalidRole));
    }

    @Test
    void testIsValidForValidRoles() {
        // 测试有效角色验证
        assertTrue(UserRoleEnum.isValid("USER"));
        assertTrue(UserRoleEnum.isValid("ADMIN"));
    }

    @Test
    void testIsValidForInvalidRoles() {
        // 测试无效角色验证
        assertFalse(UserRoleEnum.isValid("INVALID"));
        assertFalse(UserRoleEnum.isValid("user"));
        assertFalse(UserRoleEnum.isValid("admin"));
        assertFalse(UserRoleEnum.isValid(""));
        assertFalse(UserRoleEnum.isValid(null));
    }

    @Test
    void testEnumValues() {
        // 测试枚举所有值
        UserRoleEnum[] roles = UserRoleEnum.values();
        assertEquals(2, roles.length);
    }

    @Test
    void testEnumValueOf() {
        // 测试valueOf方法
        assertEquals(UserRoleEnum.USER, UserRoleEnum.valueOf("USER"));
        assertEquals(UserRoleEnum.ADMIN, UserRoleEnum.valueOf("ADMIN"));
    }

    @Test
    void testEnumValueOfInvalid() {
        // 测试valueOf方法抛出异常
        assertThrows(IllegalArgumentException.class, () -> UserRoleEnum.valueOf("INVALID"));
    }

    @Test
    void testCaseSensitivity() {
        // 测试大小写敏感性
        assertFalse(UserRoleEnum.isValid("user"));
        assertFalse(UserRoleEnum.isValid("Admin"));
        assertFalse(UserRoleEnum.isValid("UsEr"));
    }
}

