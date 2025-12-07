package com.aio.module.fts.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aio_fts_node")
public class FtsNodeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * 通信密钥
     */
    @Column(nullable = false)
    private String secret;

    /**
     * 节点入口 IP 列表 (逗号分隔)
     */
    @Column(columnDefinition = "TEXT")
    private String ip;

    /**
     * 节点服务器实际 IP
     */
    @Column(name = "server_ip", nullable = false)
    private String serverIp;

    /**
     * 开放端口起始
     */
    @Column(name = "port_start", nullable = false)
    private Integer portStart;

    /**
     * 开放端口结束
     */
    @Column(name = "port_end", nullable = false)
    private Integer portEnd;

    private String version;

    /**
     * 是否支持 HTTP (SQL中定义为Boolean，映射为Java Boolean)
     */
    @Column(name = "enable_http")
    private Boolean enableHttp = false;

    @Column(name = "enable_tls")
    private Boolean enableTls = false;

    @Column(name = "enable_socks")
    private Boolean enableSocks = false;

    /**
     * 1:在线/正常, 0:离线
     */
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
