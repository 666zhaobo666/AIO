package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_user_tunnel")
public class FtsUserTunnelEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "tunnel_id", nullable = false)
    private Long tunnelId;

    @Column(name = "speed_limit_id")
    private Long speedLimitId;

    /**
     * 该隧道允许的最大规则数
     */
    @Column(name = "max_rules")
    private Integer maxRules;

    /**
     * 该隧道的独立流量配额
     */
    @Column(name = "flow_quota")
    private Long flowQuota;

    @Column(name = "in_flow")
    private Long inFlow = 0L;

    @Column(name = "out_flow")
    private Long outFlow = 0L;

    @Column(name = "next_reset_time")
    private LocalDateTime nextResetTime;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    private Integer status = 1;
}
