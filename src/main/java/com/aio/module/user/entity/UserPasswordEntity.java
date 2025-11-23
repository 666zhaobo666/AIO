package com.aio.module.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "aio_user_password")
@EqualsAndHashCode(exclude = {"createdTime", "updatedTime", "version"})
public class UserPasswordEntity {
    @Id
    private UUID userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Version
    private Integer version;


    public UserPasswordEntity(UUID userId, String encodePassword) {
        this.userId = userId;
        this.password = encodePassword;
        this.updatedTime = LocalDateTime.now();
    }

    public UserPasswordEntity() {
    }
}
