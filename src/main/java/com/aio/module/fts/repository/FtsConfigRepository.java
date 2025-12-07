package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FtsConfigRepository extends JpaRepository<FtsConfigEntity, Long> {

    /**
     * 获取配置项
     */
    Optional<FtsConfigEntity> findByConfigKey(String configKey);
}
