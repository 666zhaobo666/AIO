package com.aio.module.fts.service.impl;

import com.aio.api.model.FtsForwardDto;
import com.aio.common.enums.ExceptionEnum;
import com.aio.common.exception.GlobalException;
import com.aio.module.fts.entity.*;
import com.aio.module.fts.repository.*;
import com.aio.module.fts.service.FtsForwardService;
import com.aio.module.fts.service.FtsUserService;
import com.aio.module.fts.utils.GostUtil; // 下一步
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FtsForwardServiceImpl implements FtsForwardService {

    private final FtsForwardRepository forwardRepository;
    private final FtsTunnelRepository tunnelRepository;
    private final FtsNodeRepository nodeRepository;
    private final FtsUserService userService;
    private final FtsSpeedLimitRepository speedLimitRepository; // 用于获取限速

    @Override
    @Transactional
    public void createForward(Integer userId, FtsForwardDto req) {
        // 1. 检查用户配额 (流量、过期、最大规则数)
        userService.checkUserQuotaOrThrow(userId);

        // 2. 获取隧道
        FtsTunnelEntity tunnel = tunnelRepository.findById(req.getTunnelId())
            .orElseThrow(() -> new GlobalException(ExceptionEnum.TUNNEL_NOT_EXIST));
        if (tunnel.getStatus() != 1) throw new GlobalException("隧道已禁用");

        // 3. 端口分配与检查
        FtsNodeEntity inNode = nodeRepository.findById(tunnel.getInNodeId())
            .orElseThrow(() -> new GlobalException("入口节点异常"));

        Integer inPort = req.getInPort();
        // 检查端口范围
        if (inPort < inNode.getPortStart() || inPort > inNode.getPortEnd()) {
            throw new GlobalException("端口不在节点允许范围内 (" + inNode.getPortStart() + "-" + inNode.getPortEnd() + ")");
        }
        // 检查占用
        if (forwardRepository.existsByInPort(inPort)) {
            throw new GlobalException("端口 " + inPort + " 已被占用");
        }

        // 4. 保存实体
        FtsForwardEntity forward = new FtsForwardEntity();
        forward.setUserId(userId);
        forward.setName(req.getName());
        forward.setTunnelId(req.getTunnelId());
        forward.setInPort(inPort);
        forward.setRemoteAddr(req.getRemoteAddr());
        forward.setStrategy(req.getStrategy());
        forward.setStatus(1);

        // 隧道转发模式下，分配出口端口 (这里简化，复用入口端口或随机)
        if (tunnel.getType() == 2) {
            forward.setOutPort(inPort); // 简单策略：同端口
        }

        forwardRepository.save(forward);

        // 5. 下发 Gost 配置
        try {
            pushGostConfig(forward, tunnel, inNode);
        } catch (Exception e) {
            log.error("Gost配置下发失败", e);
            throw new GlobalException("规则保存成功但节点配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateForward(Integer userId, Long forwardId, FtsForwardDto req) {
        FtsForwardEntity forward = forwardRepository.findById(forwardId)
            .orElseThrow(() -> new GlobalException("规则不存在"));
        if (!forward.getUserId().equals(userId)) {
            throw new GlobalException("无权操作此规则");
        }

        // 检查是否修改了隧道 -> 不支持，建议删除重建
        if (!forward.getTunnelId().equals(req.getTunnelId())) {
            throw new GlobalException("暂不支持直接修改隧道，请删除后重新创建");
        }

        // 更新字段
        forward.setName(req.getName());
        forward.setRemoteAddr(req.getRemoteAddr());
        forward.setStrategy(req.getStrategy());
        // 如果端口变了，需要检查占用
        if (!forward.getInPort().equals(req.getInPort())) {
            if (forwardRepository.existsByInPortAndIdNot(req.getInPort(), forwardId)) {
                throw new GlobalException("新端口已被占用");
            }
            forward.setInPort(req.getInPort());
        }

        forwardRepository.save(forward);

        // 重新下发配置
        FtsTunnelEntity tunnel = tunnelRepository.findById(forward.getTunnelId())
            .orElseThrow(() -> new GlobalException(ExceptionEnum.TUNNEL_NOT_EXIST));
        FtsNodeEntity inNode = nodeRepository.findById(tunnel.getInNodeId())
            .orElseThrow(() -> new GlobalException("入口节点不存在"));

        pushGostConfig(forward, tunnel, inNode);
    }

    @Override
    @Transactional
    public void deleteForward(Integer userId, Long forwardId) {
        FtsForwardEntity forward = forwardRepository.findById(forwardId)
            .orElseThrow(() -> new GlobalException("规则不存在"));

        // 权限检查
        if (!forward.getUserId().equals(userId)) {
            throw new GlobalException("无权操作");
        }

        // 删除 Gost 配置
        try {
            FtsTunnelEntity tunnel = tunnelRepository.findById(forward.getTunnelId()).orElse(null);
            if (tunnel != null) {
                // 构建服务名 (与GostUtil约定一致)
                String serviceName = buildServiceName(forward);
                GostUtil.deleteService(tunnel.getInNodeId(), serviceName);

                if (tunnel.getType() == 2) {
                    GostUtil.deleteChains(tunnel.getInNodeId(), serviceName);
                    // 远程节点清理
                    GostUtil.deleteRemoteService(tunnel.getOutNodeId(), serviceName);
                }
            }
        } catch (Exception e) {
            log.warn("Gost删除失败，可能节点已离线", e);
        }

        forwardRepository.delete(forward);
    }

    @Override
    public Page<FtsForwardEntity> getUserForwards(Integer userId, int page, int size) {
        return forwardRepository.findByUserId(userId, PageRequest.of(page - 1, size));
    }

    @Override
    @Transactional
    public void toggleForwardStatus(Integer userId, Long forwardId, int status) {
        FtsForwardEntity forward = forwardRepository.findById(forwardId).orElseThrow();
        if (!forward.getUserId().equals(userId)) throw new GlobalException("无权操作");

        forward.setStatus(status);
        forwardRepository.save(forward);

        // 触发 Gost 暂停/恢复
        FtsTunnelEntity tunnel = tunnelRepository.findById(forward.getTunnelId())
            .orElseThrow(() -> new GlobalException(ExceptionEnum.TUNNEL_NOT_EXIST));

        String serviceName = buildServiceName(forward);

        if (status == 1) {
            GostUtil.resumeService(tunnel.getInNodeId(), serviceName);
        } else {
            GostUtil.pauseService(tunnel.getInNodeId(), serviceName);
        }
    }

    // ============ Private Helpers ============

    private void pushGostConfig(FtsForwardEntity forward, FtsTunnelEntity tunnel, FtsNodeEntity inNode) {
        String serviceName = buildServiceName(forward);

        // 1. 如果是隧道转发，先配置 Chain 和 Remote Service
        if (tunnel.getType() == 2) {
            // Add Chains (入口节点 -> 出口节点)
            String remoteAddr = tunnel.getOutIp() + ":" + forward.getOutPort(); // 出口节点的监听端口
            GostUtil.addChains(inNode.getId(), serviceName, remoteAddr, tunnel.getProtocol(), tunnel.getInterfaceName());

            // Add Remote Service (出口节点 -> 真实目标)
            GostUtil.addRemoteService(tunnel.getOutNodeId(), serviceName, forward.getOutPort(), forward.getRemoteAddr(), tunnel.getProtocol(), forward.getStrategy(), null);
        }

        // 2. 配置 Main Service (入口监听)
        // 限速器暂未集成 (limiterId 传 null)
        GostUtil.addService(inNode.getId(), serviceName, forward.getInPort(), null,
            forward.getRemoteAddr(), tunnel.getType(), tunnel, forward.getStrategy(), forward.getInterfaceName());
    }

    private String buildServiceName(FtsForwardEntity forward) {
        // 格式: forwardId_userId_0 (最后一位原项目是UserTunnelId，简化为0)
        return forward.getId() + "_" + forward.getUserId() + "_0";
    }
}
