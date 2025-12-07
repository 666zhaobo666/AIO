package com.aio.module.fts.service.impl;

import com.aio.api.model.FtsEnableUserRequest;
import com.aio.api.model.FtsUserPackageInfo; // 修正引入
import com.aio.common.exception.GlobalException;
import com.aio.module.fts.entity.UserFtsEntity;
import com.aio.module.fts.repository.UserFtsRepository;
import com.aio.module.fts.service.FtsUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FtsUserServiceImpl implements FtsUserService {

    private final UserFtsRepository userFtsRepository;

    @Override
    public FtsUserPackageInfo getUserQuota(Integer userId) { // 返回类型改为 FtsUserPackageInfo
        UserFtsEntity entity = getUserFtsEntity(userId);
        if (entity == null) {
            // 用户未开通服务，返回空对象 (注意：DTO中可能没有 status 字段，根据实际生成代码调整)
            return new FtsUserPackageInfo();
        }

        // 字段映射：Entity -> DTO
        return new FtsUserPackageInfo()
            .userId(Long.valueOf(userId))
            .flow(entity.getFlowQuota())      // flowQuota -> flow
            .inFlow(entity.getInFlow())
            .outFlow(entity.getOutFlow())
            .num(entity.getMaxRules())        // maxRules -> num
            // LocalDateTime -> Long (时间戳)
            .expTime(entity.getExpirationTime() != null ?
                entity.getExpirationTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() : null)
            .flowResetTime(entity.getNextResetTime() != null ?
                entity.getNextResetTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() : null);
    }

    @Override
    @Transactional
    public void enableOrUpdateUser(FtsEnableUserRequest req) {
        UserFtsEntity entity = userFtsRepository.findByUserId(req.getUserId().intValue())
            .orElse(new UserFtsEntity());

        entity.setUserId(req.getUserId().intValue());
        entity.setFlowQuota(req.getFlowQuota());
        entity.setMaxRules(req.getMaxRules());

        if (req.getExpirationTime() != null) {
            // 解析前端传来的时间字符串
            // 建议前端统一传 ISO 格式 (e.g. "2025-12-31T23:59:59")
            try {
                entity.setExpirationTime(LocalDateTime.parse(req.getExpirationTime(), DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                // 如果解析失败，尝试兼容处理或抛出异常
                throw new GlobalException("时间格式错误，请使用 ISO 格式 (yyyy-MM-dd'T'HH:mm:ss)");
            }
        }
        entity.setStatus(1); // 激活

        userFtsRepository.save(entity);
    }

    @Override
    public UserFtsEntity getUserFtsEntity(Integer userId) {
        return userFtsRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public void checkUserQuotaOrThrow(Integer userId) {
        UserFtsEntity user = getUserFtsEntity(userId);
        if (user == null || user.getStatus() != 1) {
            throw new GlobalException("未开通中转服务或已被禁用");
        }
        if (user.getExpirationTime() != null && user.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new GlobalException("服务已过期");
        }
        if (user.getFlowQuota() > 0 && (user.getInFlow() + user.getOutFlow()) >= user.getFlowQuota()) {
            throw new GlobalException("流量已用尽");
        }
    }
}
