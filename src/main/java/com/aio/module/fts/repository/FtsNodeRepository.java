package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsNodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FtsNodeRepository extends JpaRepository<FtsNodeEntity, Long> {

    /**
     * 根据状态查询节点 (用于获取所有在线节点)
     */
    List<FtsNodeEntity> findByStatus(Integer status);

    /**
     * 管理端搜索：根据名称或IP模糊查询
     */
    Page<FtsNodeEntity> findByNameContainingOrServerIpContaining(String name, String serverIp, Pageable pageable);
}
