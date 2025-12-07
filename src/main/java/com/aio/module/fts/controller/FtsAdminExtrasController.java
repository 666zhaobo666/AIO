package com.aio.module.fts.controller;

import com.aio.api.FtsAdminSpeedLimitApi;
import com.aio.api.FtsAdminUserApi; // 确保 YAML 生成了这个接口
import com.aio.api.model.*;
import com.aio.module.fts.entity.FtsSpeedLimitEntity;
import com.aio.module.fts.service.FtsSpeedLimitService;
import com.aio.module.fts.service.FtsUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import java.util.Optional;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FtsAdminExtrasController implements FtsAdminSpeedLimitApi, FtsAdminUserApi {

    private final FtsSpeedLimitService speedLimitService;
    private final FtsUserService userService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return FtsAdminSpeedLimitApi.super.getRequest();
    }

    // --- 限速规则 ---

    @Override
    public ResponseEntity<ModelApiResponseFtsSpeedLimitList> getSpeedLimitList() {
        List<FtsSpeedLimitEntity> limits = speedLimitService.getAllSpeedLimits();

        List<FtsSpeedLimitResponse> list = limits.stream().map(l -> {
            FtsSpeedLimitResponse resp = new FtsSpeedLimitResponse();
            BeanUtils.copyProperties(l, resp);
            return resp;
        }).toList();

        ModelApiResponseFtsSpeedLimitList response = new ModelApiResponseFtsSpeedLimitList();
        response.setCode(200);
        response.setData(list);
        response.setSuccess(true);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> createSpeedLimit(FtsSpeedLimitDto dto) {
        speedLimitService.createSpeedLimit(dto);
        return ResponseEntity.ok(success("创建成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> deleteSpeedLimit(Long id) {
        speedLimitService.deleteSpeedLimit(id);
        return ResponseEntity.ok(success("删除成功"));
    }

    // --- 用户开通 (对应 YAML 补充部分) ---

    @Override
    public ResponseEntity<ModelApiResponse> enableFtsForUser(FtsEnableUserRequest req) {
        userService.enableOrUpdateUser(req);
        return ResponseEntity.ok(success("操作成功"));
    }

    private ModelApiResponse success(String msg) {
        ModelApiResponse r = new ModelApiResponse();
        r.setCode(200);
        r.setMsg(msg);
        r.setSuccess(true);
        r.setTimeStamp(System.currentTimeMillis());
        return r;
    }
}
