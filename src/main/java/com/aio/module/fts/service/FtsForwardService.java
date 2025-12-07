package com.aio.module.fts.service;

import com.aio.api.model.FtsForwardDto;
import com.aio.module.fts.entity.FtsForwardEntity;
import org.springframework.data.domain.Page;

public interface FtsForwardService {
    // 创建转发
    void createForward(Integer userId, FtsForwardDto request);

    // 更新转发 (这里可以用 FtsForwardDto，因为 UpdateDto 也是继承自它的，包含所需字段)
    void updateForward(Integer userId, Long forwardId, FtsForwardDto request);

    // 删除转发
    void deleteForward(Integer userId, Long forwardId);

    // 获取转发列表
    Page<FtsForwardEntity> getUserForwards(Integer userId, int page, int size);

    // 暂停/恢复
    void toggleForwardStatus(Integer userId, Long forwardId, int status);
}
