package com.aio.module.fts.service;

import com.aio.api.model.FtsSpeedLimitDto;
import com.aio.module.fts.entity.FtsSpeedLimitEntity;
import java.util.List;

public interface FtsSpeedLimitService {
    void createSpeedLimit(FtsSpeedLimitDto request);
    void deleteSpeedLimit(Long id);
    List<FtsSpeedLimitEntity> getSpeedLimitsByTunnel(Long tunnelId);
    List<FtsSpeedLimitEntity> getAllSpeedLimits();
}
