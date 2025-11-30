package com.aio.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aio.jwt")
public class JwtProperties {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * 令牌过期天数
     */
    private int expireDays;
}

