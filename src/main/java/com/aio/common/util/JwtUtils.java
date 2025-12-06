package com.aio.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${aio.jwt.secret}")
    private String secret;

    @Value("${aio.jwt.expire-days}")
    private int expireDays;

    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT令牌
     */
    public String generateToken(Integer userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        Date now = new Date();
        long expireMillis = ((long) expireDays) * 24 * 60 * 60 * 1000L;
        Date expiration = new Date(now.getTime() + expireMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 检查token是否为空或空白
     * @param token JWT令牌
     * @return 是否为空或空白
     */
    private boolean isTokenBlank(String token) {
        return token == null || token.trim().isEmpty();
    }

    /**
     * 从令牌中解析Claims
     * @param token JWT令牌
     * @return Optional包装的Claims，解析失败返回empty
     */
    private Optional<Claims> getClaimsFromToken(String token) {
        // 防御空token
        if (isTokenBlank(token)) {
            log.warn("JWT令牌为空或空白");
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT令牌格式错误: {}", e.getMessage());
            return Optional.empty();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT令牌签名验证失败: {}", e.getMessage());
            return Optional.empty();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            return Optional.empty();
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("不支持的JWT令牌: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.warn("JWT令牌参数非法: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("解析JWT令牌失败: {}", e.getMessage());
            return Optional.empty();
        }
    }


    /**
     * 解析JWT令牌获取用户ID
     * @param token JWT令牌
     * @return 用户ID，解析失败返回null
     */
    public String getUserIdFromToken(String token) {
        if (isTokenBlank(token)) {
            return null;
        }
        return getClaimsFromToken(token)
                .map(Claims::getSubject)
                .orElse(null);
    }

    /**
     * 解析JWT令牌获取用户角色
     * @param token JWT令牌
     * @return 用户角色，解析失败返回null
     */
    public String getRoleFromToken(String token) {
        if (isTokenBlank(token)) {
            return null;
        }
        return getClaimsFromToken(token)
                .map(claims -> claims.get("role", String.class))
                .orElse(null);
    }

    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        // 防御空token
        if (isTokenBlank(token)) {
            return false;
        }

        try {
            Optional<Claims> claimsOpt = getClaimsFromToken(token);
            if (claimsOpt.isEmpty()) {
                return false;
            }
            // 检查是否过期，防御expiration为null的情况
            Date expiration = claimsOpt.get().getExpiration();
            if (expiration == null) {
                log.warn("JWT令牌缺少过期时间");
                return false;
            }
            return expiration.after(new Date());
        } catch (Exception e) {
            log.error("验证JWT令牌失败: {}", e.getMessage());
            return false;
        }
    }
}
