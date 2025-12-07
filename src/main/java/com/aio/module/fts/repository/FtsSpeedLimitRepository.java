package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsSpeedLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FtsSpeedLimitRepository extends JpaRepository<FtsSpeedLimitEntity, Long> {

    List<FtsSpeedLimitEntity> findByTunnelId(Long tunnelId);

    /**
     * 删除隧道时清理关联的限速规则
     */
    void deleteByTunnelId(Long tunnelId);
}
