package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_user_fts")
public class UserFtsEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联主用户表ID (对应 UserEntity.userId)
     * 注意：主表 UserEntity 使用的是 Integer，这里保持一致
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    /**
     * 总流量配额 (Bytes)
     */
    @Column(name = "flow_quota")
    private Long flowQuota = 0L;

    /**
     * 已用入站流量
     */
    @Column(name = "in_flow")
    private Long inFlow = 0L;

    /**
     * 已用出站流量
     */
    @Column(name = "out_flow")
    private Long outFlow = 0L;

    /**
     * 最大转发规则数/端口数
     */
    @Column(name = "max_rules")
    private Integer maxRules = 5;

    /**
     * 过期时间
     */
    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    /**
     * 下次流量重置时间
     */
    @Column(name = "next_reset_time")
    private LocalDateTime nextResetTime;

    /**
     * 状态 1:正常, 0:禁用
     */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
