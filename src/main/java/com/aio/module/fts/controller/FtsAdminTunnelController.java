package com.aio.module.fts.controller;

import com.aio.api.FtsAdminTunnelApi;
import com.aio.api.model.*;
import com.aio.module.fts.entity.FtsTunnelEntity;
import com.aio.module.fts.service.FtsTunnelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FtsAdminTunnelController implements FtsAdminTunnelApi {

    private final FtsTunnelService tunnelService;

    @Override
    public ResponseEntity<ModelApiResponseFtsTunnelList> getAdminTunnelList(String keyword) {
        List<FtsTunnelEntity> tunnels = tunnelService.getAllTunnels(keyword);

        List<FtsTunnelResponse> list = tunnels.stream().map(t -> {
            FtsTunnelResponse resp = new FtsTunnelResponse();
            BeanUtils.copyProperties(t, resp);
            // float -> double 转换
            if (t.getTrafficRatio() != null) {
                resp.setTrafficRatio(t.getTrafficRatio().doubleValue());
            }
            return resp;
        }).toList();

        ModelApiResponseFtsTunnelList response = new ModelApiResponseFtsTunnelList();
        response.setCode(200);
        response.setData(list);
        response.setSuccess(true);
        response.setTimeStamp(System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ModelApiResponse> createTunnel(FtsTunnelDto ftsTunnelDto) {
        tunnelService.createTunnel(ftsTunnelDto);
        return ResponseEntity.ok(success("创建成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> updateTunnel(FtsTunnelUpdateDto updateDto) {
        // 转换类型: UpdateDto -> Dto
        FtsTunnelDto dto = new FtsTunnelDto();
        BeanUtils.copyProperties(updateDto, dto);

        // 调用 Service
        tunnelService.updateTunnel(updateDto.getId(), dto);

        return ResponseEntity.ok(success("更新成功"));
    }

    @Override
    public ResponseEntity<ModelApiResponse> deleteTunnel(Long id) {
        tunnelService.deleteTunnel(id);
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
