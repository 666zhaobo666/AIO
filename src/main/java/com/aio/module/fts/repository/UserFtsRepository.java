package com.aio.module.fts.repository;

import com.aio.module.fts.entity.UserFtsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFtsRepository extends JpaRepository<UserFtsEntity, Long> {

    /**
     * 通过主用户表的 userId 查询扩展信息
     * 这是最常用的查询方法
     */
    Optional<UserFtsEntity> findByUserId(Integer userId);

    /**
     * 检查该用户是否已开通服务
     */
    boolean existsByUserId(Integer userId);
}
