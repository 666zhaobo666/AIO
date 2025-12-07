package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsUserTunnelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FtsUserTunnelRepository extends JpaRepository<FtsUserTunnelEntity, Long> {

    /**
     * 查询用户对某条隧道的专属配置
     */
    Optional<FtsUserTunnelEntity> findByUserIdAndTunnelId(Integer userId, Long tunnelId);

    /**
     * 查询用户所有的专属配置
     */
    List<FtsUserTunnelEntity> findByUserId(Integer userId);

    /**
     * 删除隧道时级联删除用户的配置
     */
    void deleteByTunnelId(Long tunnelId);
}
