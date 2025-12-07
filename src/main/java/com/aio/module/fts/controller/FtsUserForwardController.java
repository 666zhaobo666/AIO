package com.aio.module.fts.controller;

import com.aio.api.FtsUserForwardApi;
import com.aio.api.model.*;
import com.aio.common.security.SecurityContextUtils;
import com.aio.module.fts.entity.FtsForwardEntity;
import com.aio.module.fts.service.FtsForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FtsUserForwardController implements FtsUserForwardApi {

    private final FtsForwardService forwardService;

    @Override
    public ResponseEntity<ModelApiResponseFtsForwardList> getUserForwardList(Integer page, Integer size) {
        Integer userId = SecurityContextUtils.getCurrentUserId(); // 获取当前登录用户ID

        Page<FtsForwardEntity> pageResult = forwardService.getUserForwards(userId, page, size);

        List<FtsForwardResponse> list = pageResult.getContent().stream().map(f -> {
            FtsForwardResponse resp = new FtsForwardResponse();
            BeanUtils.copyProperties(f, resp);
            return resp;
        }).toList();

        ModelApiResponseFtsForwardList response = new ModelApiResponseFtsForwardList();
        response.setCode(200);
        response.setData(list);
        response.setSuccess(true);
        // 这里可以扩展分页信息到 response 中，如果有对应字段的话

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> createForward(FtsForwardDto dto) {
        Integer userId = SecurityContextUtils.getCurrentUserId();
        forwardService.createForward(userId, dto);
        return ResponseEntity.ok(success("创建成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> updateForward(FtsForwardUpdateDto dto) {
        Integer userId = SecurityContextUtils.getCurrentUserId();
        // 复用 DTO，假设 UpdateDto 是 ForwardDto 的子类或包含其字段
        // 如果类型不匹配，需要手动转换
        FtsForwardDto forwardDto = new FtsForwardDto();
        BeanUtils.copyProperties(dto, forwardDto);

        forwardService.updateForward(userId, dto.getId(), forwardDto);
        return ResponseEntity.ok(success("更新成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> deleteForward(Long id) {
        Integer userId = SecurityContextUtils.getCurrentUserId();
        forwardService.deleteForward(userId, id);
        return ResponseEntity.ok(success("删除成功"));
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
