package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_tunnel")
public class FtsTunnelEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * 流量倍率
     */
    @Column(name = "traffic_ratio", precision = 10, scale = 1)
    private BigDecimal trafficRatio = BigDecimal.ONE;

    @Column(name = "in_node_id", nullable = false)
    private Long inNodeId;

    @Column(name = "in_ip")
    private String inIp;

    @Column(name = "out_node_id", nullable = false)
    private Long outNodeId;

    @Column(name = "out_ip")
    private String outIp;

    /**
     * 1:端口转发, 2:隧道转发
     */
    @Column(nullable = false)
    private Integer type;

    @Column(length = 20)
    private String protocol = "tls";

    /**
     * 1:单向计算, 2:双向计算
     */
    @Column(name = "flow_type", nullable = false)
    private Integer flowType;

    @Column(name = "tcp_listen_addr")
    private String tcpListenAddr = "[::]";

    @Column(name = "udp_listen_addr")
    private String udpListenAddr = "[::]";

    @Column(name = "interface_name")
    private String interfaceName;

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
