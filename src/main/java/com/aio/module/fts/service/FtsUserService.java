package com.aio.module.fts.service;

import com.aio.api.model.FtsEnableUserRequest;
import com.aio.api.model.FtsUserPackageInfo;
import com.aio.module.fts.entity.UserFtsEntity;

public interface FtsUserService {
    // 获取用户配额信息 (返回类型修正)
    FtsUserPackageInfo getUserQuota(Integer userId);

    // 开通或更新用户服务
    void enableOrUpdateUser(FtsEnableUserRequest request);

    // 内部方法：获取实体
    UserFtsEntity getUserFtsEntity(Integer userId);

    // 检查配额
    void checkUserQuotaOrThrow(Integer userId);
}
