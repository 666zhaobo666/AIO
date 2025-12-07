package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_forward", indexes = {
    @Index(name = "idx_aio_fts_forward_user", columnList = "user_id"),
    @Index(name = "idx_aio_fts_forward_tunnel", columnList = "tunnel_id")
})
public class FtsForwardEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    private String name;

    @Column(name = "tunnel_id", nullable = false)
    private Long tunnelId;

    /**
     * 入口端口
     */
    @Column(name = "in_port", nullable = false)
    private Integer inPort;

    /**
     * 出口端口 (可选)
     */
    @Column(name = "out_port")
    private Integer outPort;

    /**
     * 目标地址 (IP:Port)
     */
    @Column(name = "remote_addr", nullable = false, columnDefinition = "TEXT")
    private String remoteAddr;

    /**
     * 负载均衡策略
     */
    private String strategy = "fifo";

    @Column(name = "interface_name")
    private String interfaceName;

    @Column(name = "in_flow")
    private Long inFlow = 0L;

    @Column(name = "out_flow")
    private Long outFlow = 0L;

    /**
     * 排序索引
     */
    private Integer idx = 0;

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
