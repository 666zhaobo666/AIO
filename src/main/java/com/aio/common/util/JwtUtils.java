package com.aio.common.util;

import org.springframework.stereotype.Component;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT令牌
     */
    public String generateToken(String userId, String role) {
        // 简单实现，实际应该使用JWT库如jjwt
        return "jwt_token_" + userId + "_" + role;
    }

    /**
     * 解析JWT令牌获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        // 简单实现，实际应该解析JWT
        if (token.startsWith("jwt_token_")) {
            String[] parts = token.split("_");
            if (parts.length >= 3) {
                return parts[2];
            }
        }
        return null;
    }

    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        // 简单实现，实际应该验证JWT签名和过期时间
        return token != null && token.startsWith("jwt_token_");
    }
}
