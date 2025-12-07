package com.aio.module.fts.service.impl;

import com.aio.api.model.FtsSpeedLimitDto;
import com.aio.common.exception.GlobalException;
import com.aio.module.fts.entity.FtsSpeedLimitEntity;
import com.aio.module.fts.repository.FtsSpeedLimitRepository;
import com.aio.module.fts.repository.FtsTunnelRepository;
import com.aio.module.fts.service.FtsSpeedLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FtsSpeedLimitServiceImpl implements FtsSpeedLimitService {

    private final FtsSpeedLimitRepository speedLimitRepository;
    private final FtsTunnelRepository tunnelRepository;

    @Override
    @Transactional
    public void createSpeedLimit(FtsSpeedLimitDto request) {
        String tunnelName = tunnelRepository.findById(request.getTunnelId())
            .map(t -> t.getName())
            .orElseThrow(() -> new GlobalException("隧道不存在"));

        FtsSpeedLimitEntity entity = new FtsSpeedLimitEntity();
        entity.setName(request.getName());
        entity.setSpeedMb(request.getSpeed());
        entity.setTunnelId(request.getTunnelId());
        entity.setTunnelName(tunnelName);

        speedLimitRepository.save(entity);
        // 此处暂不调用 GostUtil.AddLimiters，逻辑较复杂，建议后续完善
    }

    @Override
    public void deleteSpeedLimit(Long id) {
        speedLimitRepository.deleteById(id);
    }

    @Override
    public List<FtsSpeedLimitEntity> getSpeedLimitsByTunnel(Long tunnelId) {
        return speedLimitRepository.findByTunnelId(tunnelId);
    }

    @Override
    public List<FtsSpeedLimitEntity> getAllSpeedLimits() {
        return speedLimitRepository.findAll();
    }
}
