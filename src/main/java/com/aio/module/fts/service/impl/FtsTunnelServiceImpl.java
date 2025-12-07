package com.aio.module.fts.service.impl;

import com.aio.api.model.FtsTunnelDto;
import com.aio.api.model.FtsTunnelResponse;
import com.aio.common.exception.GlobalException;
import com.aio.module.fts.entity.*;
import com.aio.module.fts.repository.*;
import com.aio.module.fts.service.FtsTunnelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FtsTunnelServiceImpl implements FtsTunnelService {

    private final FtsTunnelRepository tunnelRepository;
    private final FtsNodeRepository nodeRepository;
    private final FtsForwardRepository forwardRepository;
    private final FtsUserTunnelRepository userTunnelRepository;

    @Override
    @Transactional
    public void createTunnel(FtsTunnelDto req) {
        // 校验节点
        FtsNodeEntity inNode = nodeRepository.findById(req.getInNodeId())
            .orElseThrow(() -> new GlobalException("入口节点不存在"));

        FtsTunnelEntity tunnel = new FtsTunnelEntity();
        BeanUtils.copyProperties(req, tunnel);

        // 补全IP快照
        tunnel.setInIp(inNode.getIp());

        // 如果是隧道转发，校验出口节点
        if (req.getType() == 2) {
            if (req.getOutNodeId() == null) throw new GlobalException("隧道转发必须指定出口节点");
            FtsNodeEntity outNode = nodeRepository.findById(req.getOutNodeId())
                .orElseThrow(() -> new GlobalException("出口节点不存在"));
            tunnel.setOutIp(outNode.getServerIp());
        } else {
            // 端口转发，出入口相同
            tunnel.setOutNodeId(tunnel.getInNodeId());
            tunnel.setOutIp(inNode.getServerIp());
        }

        if (tunnel.getTrafficRatio() == null) tunnel.setTrafficRatio(BigDecimal.ONE);

        // flowType处理: 默认为1 (DTO生成时是Integer)
        // tunnel.setFlowType(1); // 默认单向

        tunnelRepository.save(tunnel);
    }

    @Override
    @Transactional
    public void updateTunnel(Long id, FtsTunnelDto req) {
        FtsTunnelEntity tunnel = getTunnelById(id);
        tunnel.setName(req.getName());
        tunnel.setTrafficRatio(BigDecimal.valueOf(req.getTrafficRatio()));
        tunnel.setProtocol(req.getProtocol());
        tunnel.setTcpListenAddr(req.getTcpListenAddr());
        tunnel.setUdpListenAddr(req.getUdpListenAddr());
        // 注意：通常不建议直接修改 Tunnel 的节点 ID，因为会破坏现有的 Forward 规则
        // 这里仅允许修改配置参数

        tunnelRepository.save(tunnel);
        // 要做的: 触发相关 Forward 的配置更新
    }

    @Override
    @Transactional
    public void deleteTunnel(Long id) {
        if (forwardRepository.existsByTunnelId(id)) {
            throw new GlobalException("该隧道下存在转发规则，请先删除规则");
        }
        // 级联删除用户专属配置
        userTunnelRepository.deleteByTunnelId(id);
        tunnelRepository.deleteById(id);
    }

    @Override
    public List<FtsTunnelEntity> getAllTunnels(String keyword) {
        return tunnelRepository.findAll();
    }

    @Override
    public FtsTunnelEntity getTunnelById(Long id) {
        return tunnelRepository.findById(id)
            .orElseThrow(() -> new GlobalException("隧道不存在"));
    }

    @Override
    public List<FtsTunnelResponse> getUserTunnels(Integer userId) {
        // 逻辑：获取所有启用的隧道 (暂时对所有用户开放所有隧道，或者你可以增加逻辑只返回 UserTunnel 表中有的)
        // 原 flux-panel 逻辑：普通用户只能看分配给他的隧道。

        // 1. 获取所有公共启用隧道 (假设所有status=1的都是公共的)
        List<FtsTunnelEntity> tunnels = tunnelRepository.findByStatus(1);

        return tunnels.stream().map(t -> {
            FtsTunnelResponse resp = new FtsTunnelResponse();
            BeanUtils.copyProperties(t, resp);
            return resp;
        }).toList();
    }
}
