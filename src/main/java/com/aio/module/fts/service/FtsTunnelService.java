package com.aio.module.fts.service;

import com.aio.api.model.FtsTunnelDto;
import com.aio.api.model.FtsTunnelResponse;
import com.aio.module.fts.entity.FtsTunnelEntity;
import java.util.List;

public interface FtsTunnelService {
    void createTunnel(FtsTunnelDto request);
    void updateTunnel(Long id, FtsTunnelDto request);
    void deleteTunnel(Long id);
    List<FtsTunnelEntity> getAllTunnels(String keyword);
    FtsTunnelEntity getTunnelById(Long id);
    List<FtsTunnelResponse> getUserTunnels(Integer userId);
}
