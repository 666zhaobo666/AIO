package com.aio.module.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "aio_user_password")
public class UserPasswordEntity {

    @Id
    private Integer userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    private LocalDateTime updatedTime;

    @Version
    private Integer version;

    public UserPasswordEntity() {}

    public UserPasswordEntity(Integer userId, String password) {
        this.userId = userId;
        this.password = password;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

}