package com.aio.module.fts.service;

import com.aio.api.model.FtsNodeDto;
import com.aio.module.fts.entity.FtsNodeEntity;

import java.util.List;

public interface FtsNodeService {
    void createNode(FtsNodeDto request);
    void updateNode(Long id, FtsNodeDto request);
    void deleteNode(Long id);
    List<FtsNodeEntity> getAllNodes(String keyword);
    FtsNodeEntity getNodeById(Long id);
    String getInstallCommand(Long id);
}
