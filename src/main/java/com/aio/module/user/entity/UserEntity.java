package com.aio.module.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Entity
@Table(name = "aio_user")
@ToString
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "displayname", nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    private String gender; // M/F/U

    private LocalDate birthday;

    private String occupation;

    private String signature;

    @Column(nullable = false)
    private String role = "user"; // 默认角色为user，支持任意字符串（如admin、editor等）

    @Column(name = "register_time", nullable = false)
    private LocalDateTime registerTime = LocalDateTime.now();

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    private Integer status = 1; // 1:正常，0:禁用

    @Version
    private Integer version; // 乐观锁
}
