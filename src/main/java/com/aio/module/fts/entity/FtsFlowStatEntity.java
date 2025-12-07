package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_flow_stat")
public class FtsFlowStatEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * 本次统计周期的流量 (Bytes)
     */
    @Column(name = "flow_bytes", nullable = false)
    private Long flowBytes;

    /**
     * 截止当时的累计总流量 (Bytes)
     */
    @Column(name = "total_flow_bytes", nullable = false)
    private Long totalFlowBytes;

    /**
     * 记录日期 (YYYY-MM-DD)
     */
    @Column(name = "record_date", nullable = false, length = 50)
    private String recordDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
