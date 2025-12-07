package com.aio.module.fts.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.aio.api.model.FtsNodeDto; // 确保引用的是 Dto
import com.aio.common.exception.GlobalException;
import com.aio.module.fts.entity.FtsNodeEntity;
import com.aio.module.fts.repository.FtsNodeRepository;
import com.aio.module.fts.repository.FtsTunnelRepository;
import com.aio.module.fts.service.FtsConfigService;
import com.aio.module.fts.service.FtsNodeService;
import com.aio.module.fts.utils.WebSocketServer;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FtsNodeServiceImpl implements FtsNodeService {

    private final FtsNodeRepository nodeRepository;
    private final FtsTunnelRepository tunnelRepository;
    private final FtsConfigService configService;

    @Override
    @Transactional
    public void createNode(FtsNodeDto req) {
        validatePortRange(req.getPortSta(), req.getPortEnd());

        FtsNodeEntity node = new FtsNodeEntity();
        node.setName(req.getName());
        node.setIp(req.getIp());
        node.setServerIp(req.getServerIp());
        node.setPortStart(req.getPortSta());
        node.setPortEnd(req.getPortEnd());

        // 自动生成密钥
        node.setSecret(IdUtil.simpleUUID());

        // 修复：Integer (0/1) -> Boolean转换
        node.setEnableHttp(isEnable(req.getEnableHttp()));
        node.setEnableTls(isEnable(req.getEnableTls()));
        node.setEnableSocks(isEnable(req.getEnableSocks()));

        node.setStatus(1); // 默认在线状态

        nodeRepository.save(node);
    }

    @Override
    @Transactional
    public void updateNode(Long id, FtsNodeDto req) {
        FtsNodeEntity node = getNodeById(id);

        // 将请求中的 Integer 转换为 Boolean 以便进行比较
        boolean newHttp = isEnable(req.getEnableHttp());
        boolean newTls = isEnable(req.getEnableTls());
        boolean newSocks = isEnable(req.getEnableSocks());

        // 检查是否需要推送更新到节点 (Boolean比较)
        boolean needPush = !node.getEnableHttp().equals(newHttp) ||
            !node.getEnableTls().equals(newTls) ||
            !node.getEnableSocks().equals(newSocks);

        node.setName(req.getName());
        node.setIp(req.getIp());
        node.setServerIp(req.getServerIp());
        node.setPortStart(req.getPortSta());
        node.setPortEnd(req.getPortEnd());

        // 修复：Integer (0/1) -> Boolean转换
        node.setEnableHttp(newHttp);
        node.setEnableTls(newTls);
        node.setEnableSocks(newSocks);

        nodeRepository.save(node);

        // 如果协议开关变更且节点在线，推送配置
        if (needPush && node.getStatus() == 1) {
            JSONObject json = new JSONObject();
            // Boolean -> Integer (0/1) 用于发送给 GOST
            json.put("http", Boolean.TRUE.equals(node.getEnableHttp()) ? 1 : 0);
            json.put("tls", Boolean.TRUE.equals(node.getEnableTls()) ? 1 : 0);
            json.put("socks", Boolean.TRUE.equals(node.getEnableSocks()) ? 1 : 0);
            WebSocketServer.sendMsg(node.getId(), json, "SetProtocol");
        }
    }

    @Override
    @Transactional
    public void deleteNode(Long id) {
        if (tunnelRepository.existsByInNodeIdOrOutNodeId(id, id)) {
            throw new GlobalException("该节点正被隧道使用，无法删除");
        }
        nodeRepository.deleteById(id);
    }

    @Override
    public List<FtsNodeEntity> getAllNodes(String keyword) {
        // 简单实现，暂不支持keyword过滤
        return nodeRepository.findAll();
    }

    @Override
    public FtsNodeEntity getNodeById(Long id) {
        return nodeRepository.findById(id)
            .orElseThrow(() -> new GlobalException("节点不存在: " + id));
    }

    @Override
    public String getInstallCommand(Long id) {
        FtsNodeEntity node = getNodeById(id);
        String panelIp = configService.getConfigValue("site_ip");
        if (CharSequenceUtil.isBlank(panelIp)) {
            throw new GlobalException("请先在系统配置中设置站点IP (site_ip)");
        }

        // 适配 IPv6
        String serverAddr = panelIp;
        if (isIPv6(panelIp) && !panelIp.startsWith("[")) {
            serverAddr = "[" + panelIp + "]";
        }

        // 使用官方脚本
        return String.format(
            "curl -L https://github.com/bqlpfy/flux-panel/releases/download/1.4.3/install.sh -o install.sh && " +
                "chmod +x install.sh && ./install.sh -a %s -s %s",
            serverAddr, node.getSecret()
        );
    }

    // ============ 私有辅助方法 ============

    private void validatePortRange(Integer start, Integer end) {
        if (start == null || end == null || start < 1 || end > 65535 || start > end) {
            throw new GlobalException("端口范围无效 (1-65535)");
        }
    }

    private boolean isIPv6(String ip) {
        return ip != null && ip.contains(":");
    }

    /**
     * 辅助方法：Integer (1/0/null) -> Boolean (true/false)
     */
    private boolean isEnable(Integer value) {
        return value != null && value == 1;
    }
}
