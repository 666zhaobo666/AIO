package com.aio.common.service;

import com.aio.api.model.ModelApiResponse;
import com.aio.common.entity.ViteConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *  网站配置服务类
 * </p>
 *
 * @author QAQ
 * @since 2025-07-24
 */
@Service
public interface ViteConfigService extends IService<ViteConfig> {

    /**
     * 获取所有网站配置
     * @return 配置Map，key为配置名，value为配置值
     */
    ResponseEntity<ModelApiResponse> getConfigs();

    /**
     * 根据配置名获取配置值
     * @param name 配置名
     * @return 配置值
     */
    ResponseEntity<ModelApiResponse> getConfigByName(String name);

    /**
     * 批量更新网站配置
     * @param configMap 配置Map，key为配置名，value为新的配置值
     * @return 更新结果
     */
    ResponseEntity<ModelApiResponse> updateConfigs(Map<String, String> configMap);

    /**
     * 更新单个配置项
     * @param name 配置名
     * @param value 配置值
     * @return 更新结果
     */
    ResponseEntity<ModelApiResponse> updateConfig(String name, String value);

}
