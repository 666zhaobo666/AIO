package com.aio.module.user.repository;

import com.aio.module.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * 根据用户名查询用户
     */
    Optional<UserEntity> findByUsernameAndStatus(String username, Integer status);

    /**
     * 根据邮箱查询用户
     */
    Optional<UserEntity> findByEmailAndStatus(String email, Integer status);

    /**
     * 根据手机号查询用户
     */
    Optional<UserEntity> findByPhoneAndStatus(String phone, Integer status);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);
}
