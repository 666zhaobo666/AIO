package com.aio.module.fts.controller;

import com.aio.api.FtsUserFlowApi;
import com.aio.api.model.FtsUserPackageInfo;
import com.aio.api.model.ModelApiResponse;
import com.aio.common.security.SecurityContextUtils;
import com.aio.module.fts.service.FtsUserService;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FtsUserFlowController implements FtsUserFlowApi {

    private final FtsUserService userService;

    @Override
    public ResponseEntity<ModelApiResponse> getUserPackageInfo() {
        Integer userId = SecurityContextUtils.getCurrentUserId();
        FtsUserPackageInfo info = userService.getUserQuota(userId);

        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg("获取成功");
        response.setData(JsonNullable.of(info));
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
