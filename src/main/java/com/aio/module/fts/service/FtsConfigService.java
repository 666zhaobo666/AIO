package com.aio.module.fts.service;

public interface FtsConfigService {
    String getConfigValue(String key);
    void setConfigValue(String key, String value);
}
