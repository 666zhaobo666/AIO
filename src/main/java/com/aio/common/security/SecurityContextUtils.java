package com.aio.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * 安全上下文工具类
 * 用于从Spring Security上下文中获取当前认证用户信息
 */
public class SecurityContextUtils {

    private SecurityContextUtils() {
        // 工具类，隐藏构造函数
    }

    /**
     * 获取当前认证用户的ID
     * @return 用户ID，如果未认证则返回null
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userIdStr = authentication.getPrincipal().toString();
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前认证用户的角色
     * @return 用户角色（不含ROLE_前缀），如果未认证则返回null
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5)) // 移除"ROLE_"前缀
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 检查当前用户是否已认证
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否具有指定角色
     * @param role 角色名称（不含ROLE_前缀）
     * @return 是否具有该角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }
}

