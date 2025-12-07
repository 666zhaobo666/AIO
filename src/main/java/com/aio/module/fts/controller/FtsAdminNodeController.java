package com.aio.module.fts.controller;

import com.aio.api.FtsAdminNodeApi;
import com.aio.api.model.FtsNodeDto;
import com.aio.api.model.FtsNodeUpdateDto;
import com.aio.api.model.FtsNodeResponse;
import com.aio.api.model.ModelApiResponse;
import com.aio.api.model.ModelApiResponseFtsNodeList;
import com.aio.module.fts.entity.FtsNodeEntity;
import com.aio.module.fts.service.FtsNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')") // 建议开启
public class FtsAdminNodeController implements FtsAdminNodeApi {

    private final FtsNodeService nodeService;

    @Override
    public ResponseEntity<ModelApiResponseFtsNodeList> getAdminNodeList(String keyword, Integer page, Integer size) {
        // 这里的 Service 目前是查所有，暂未实现分页，后续可在 Service 层完善 Pageable
        List<FtsNodeEntity> nodes = nodeService.getAllNodes(keyword);

        List<FtsNodeResponse> responseList = nodes.stream().map(node -> {
            FtsNodeResponse resp = new FtsNodeResponse();
            BeanUtils.copyProperties(node, resp);
            // Integer -> Boolean 转换适配
            resp.setEnableHttp(Boolean.TRUE.equals(node.getEnableHttp()) ? 1 : 0);
            resp.setEnableTls(Boolean.TRUE.equals(node.getEnableTls()) ? 1 : 0);
            resp.setEnableSocks(Boolean.TRUE.equals(node.getEnableSocks()) ? 1 : 0);

            return resp;
        }).toList();

        ModelApiResponseFtsNodeList response = new ModelApiResponseFtsNodeList();
        response.setCode(200);
        response.setMsg("查询成功");
        response.setData(responseList);
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> createNode(FtsNodeDto ftsNodeDto) {
        nodeService.createNode(ftsNodeDto);
        return ResponseEntity.ok(success("创建成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> updateNode(FtsNodeUpdateDto updateDto) {
        // 将 UpdateDto 转换为 NodeDto 传给 Service
        FtsNodeDto nodeDto = new FtsNodeDto();
        BeanUtils.copyProperties(updateDto, nodeDto);

        // Service层需要 ID 和 DTO
        nodeService.updateNode(updateDto.getId(), nodeDto);

        return ResponseEntity.ok(success("更新成功"));
    }

    // 修正: 由于生成的接口定义可能有所不同，你需要根据实际生成的 Interface 方法签名来填空
    // 比如 updateNode 可能在 yaml 里定义的是 PUT /node 且 Body 是 UpdateDto

    @Override
    public ResponseEntity<ModelApiResponse> deleteNode(Long id) {
        nodeService.deleteNode(id);
        return ResponseEntity.ok(success("删除成功"));
    }

    private ModelApiResponse success(String msg) {
        ModelApiResponse response = new ModelApiResponse();
        response.setCode(200);
        response.setMsg(msg);
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());
        return response;
    }
}
