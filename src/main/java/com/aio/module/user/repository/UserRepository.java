package com.aio.module.user.repository;

import com.aio.module.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * 根据用户名查询用户
     */
    Optional<UserEntity> findByUserNameAndStatus(String username, Integer status);

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
    boolean existsByUserName(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查用户名是否已存在（排除指定用户ID）
     */
    boolean existsByUserNameAndUserIdNot(String username, Integer userId);

    /**
     * 检查邮箱是否已存在（排除指定用户ID）
     */
    boolean existsByEmailAndUserIdNot(String email, Integer userId);

    /**
     * 检查手机号是否已存在（排除指定用户ID）
     */
    boolean existsByPhoneAndUserIdNot(String phone, Integer userId);
}
