package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsForwardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FtsForwardRepository extends JpaRepository<FtsForwardEntity, Long> {

    /**
     * 查询用户的转发列表
     */
    Page<FtsForwardEntity> findByUserId(Integer userId, Pageable pageable);

    List<FtsForwardEntity> findByUserId(Integer userId);

    /**
     * 检查端口是否被占用 (全局检查，或者根据业务逻辑是否允许不同入口IP复用端口)
     * 原逻辑通常是全局唯一的入口端口
     */
    boolean existsByInPort(Integer inPort);

    /**
     * 排除自身ID的情况下检查端口占用 (用于更新操作)
     */
    boolean existsByInPortAndIdNot(Integer inPort, Long id);

    /**
     * 根据隧道ID查询 (删除隧道前需要检查)
     */
    boolean existsByTunnelId(Long tunnelId);

    List<FtsForwardEntity> findByTunnelId(Long tunnelId);

    /**
     * 核心查询：查找属于某个入口节点的所有转发规则
     * 用于生成 GOST 节点配置文件
     * 逻辑：Forward -> Tunnel (where in_node_id = ?)
     */
    @Query("SELECT f FROM FtsForwardEntity f WHERE f.tunnelId IN " +
        "(SELECT t.id FROM FtsTunnelEntity t WHERE t.inNodeId = :nodeId AND t.status = 1) " +
        "AND f.status = 1")
    List<FtsForwardEntity> findActiveForwardsByNodeId(@Param("nodeId") Long nodeId);
}
