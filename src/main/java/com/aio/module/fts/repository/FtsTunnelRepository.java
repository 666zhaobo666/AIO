package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsTunnelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FtsTunnelRepository extends JpaRepository<FtsTunnelEntity, Long> {

    /**
     * 根据名称模糊搜索
     */
    Page<FtsTunnelEntity> findByNameContaining(String keyword, Pageable pageable);

    /**
     * 查询所有启用的隧道
     */
    List<FtsTunnelEntity> findByStatus(Integer status);

    /**
     * 检查某个节点是否被隧道使用 (用于节点删除前的检查)
     */
    boolean existsByInNodeIdOrOutNodeId(Long inNodeId, Long outNodeId);
}
