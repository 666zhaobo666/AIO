package com.aio.module.user.enmus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户角色枚举测试
 */
class UserRoleEnmuTest {

    @Test
    void testUserRoleValue() {
        // 测试USER角色值
        assertEquals("USER", UserRoleEnmu.USER.getValue());
        assertEquals("USER", UserRoleEnmu.USER.getRole());
    }

    @Test
    void testAdminRoleValue() {
        // 测试ADMIN角色值
        assertEquals("ADMIN", UserRoleEnmu.ADMIN.getValue());
        assertEquals("ADMIN", UserRoleEnmu.ADMIN.getRole());
    }

    @Test
    void testGetByRoleForValidRole() {
        // 测试获取有效角色
        assertEquals("USER", UserRoleEnmu.getByRole("USER"));
        assertEquals("ADMIN", UserRoleEnmu.getByRole("ADMIN"));
    }

    @Test
    void testGetByRoleForInvalidRole() {
        // 测试获取无效角色时返回原值
        String invalidRole = "INVALID_ROLE";
        assertEquals(invalidRole, UserRoleEnmu.getByRole(invalidRole));
    }

    @Test
    void testIsValidForValidRoles() {
        // 测试有效角色验证
        assertTrue(UserRoleEnmu.isValid("USER"));
        assertTrue(UserRoleEnmu.isValid("ADMIN"));
    }

    @Test
    void testIsValidForInvalidRoles() {
        // 测试无效角色验证
        assertFalse(UserRoleEnmu.isValid("INVALID"));
        assertFalse(UserRoleEnmu.isValid("user"));
        assertFalse(UserRoleEnmu.isValid("admin"));
        assertFalse(UserRoleEnmu.isValid(""));
        assertFalse(UserRoleEnmu.isValid(null));
    }

    @Test
    void testEnumValues() {
        // 测试枚举所有值
        UserRoleEnmu[] roles = UserRoleEnmu.values();
        assertEquals(2, roles.length);
    }

    @Test
    void testEnumValueOf() {
        // 测试valueOf方法
        assertEquals(UserRoleEnmu.USER, UserRoleEnmu.valueOf("USER"));
        assertEquals(UserRoleEnmu.ADMIN, UserRoleEnmu.valueOf("ADMIN"));
    }

    @Test
    void testEnumValueOfInvalid() {
        // 测试valueOf方法抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            UserRoleEnmu.valueOf("INVALID");
        });
    }

    @Test
    void testCaseSensitivity() {
        // 测试大小写敏感性
        assertFalse(UserRoleEnmu.isValid("user"));
        assertFalse(UserRoleEnmu.isValid("Admin"));
        assertFalse(UserRoleEnmu.isValid("UsEr"));
    }
}

