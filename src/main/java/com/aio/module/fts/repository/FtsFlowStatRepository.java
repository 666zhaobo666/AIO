package com.aio.module.fts.repository;

import com.aio.module.fts.entity.FtsFlowStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FtsFlowStatRepository extends JpaRepository<FtsFlowStatEntity, Long> {

    /**
     * 查询用户某一天的流量记录 (用于更新本日流量)
     */
    Optional<FtsFlowStatEntity> findByUserIdAndRecordDate(Integer userId, String recordDate);

    /**
     * 查询用户最近 N 条流量记录 (用于前端图表展示)
     */
    List<FtsFlowStatEntity> findByUserIdOrderByRecordDateDesc(Integer userId);
}
