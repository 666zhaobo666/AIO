package com.aio.module.user.entity;

import com.aio.common.exception.GlobalException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "aio_user")
@ToString
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String displayName;

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

    @Column(nullable = false)
    private LocalDateTime registerTime = LocalDateTime.now();

    private LocalDateTime lastLoginTime;

    private Integer status = 1; // 1:正常，0:禁用

    @Version
    private Integer version; // 乐观锁
    
    // 深拷贝
    public UserEntity deepCopy() {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            return (UserEntity) ois.readObject();
        } catch (Exception e) {
            throw new GlobalException("UserEntity Deep copy failed");
        }
    }
}