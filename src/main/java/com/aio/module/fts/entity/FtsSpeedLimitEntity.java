package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_speed_limit")
public class FtsSpeedLimitEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * 限速值 (MB/s)
     */
    @Column(name = "speed_mb", nullable = false)
    private Integer speedMb;

    @Column(name = "tunnel_id", nullable = false)
    private Long tunnelId;

    /**
     * 冗余字段，方便查询显示
     */
    @Column(name = "tunnel_name")
    private String tunnelName;

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
