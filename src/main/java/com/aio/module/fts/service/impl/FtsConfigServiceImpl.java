package com.aio.module.fts.service.impl;

import com.aio.module.fts.entity.FtsConfigEntity;
import com.aio.module.fts.repository.FtsConfigRepository;
import com.aio.module.fts.service.FtsConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FtsConfigServiceImpl implements FtsConfigService {

    private final FtsConfigRepository configRepository;

    @Override
    public String getConfigValue(String key) {
        return configRepository.findByConfigKey(key)
            .map(FtsConfigEntity::getConfigValue)
            .orElse(null);
    }

    @Override
    public void setConfigValue(String key, String value) {
        FtsConfigEntity config = configRepository.findByConfigKey(key)
            .orElse(new FtsConfigEntity());
        config.setConfigKey(key);
        config.setConfigValue(value);
        configRepository.save(config);
    }
}
