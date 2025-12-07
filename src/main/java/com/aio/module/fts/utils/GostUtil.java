package com.aio.module.fts.utils;

import com.aio.module.fts.dto.GostDto;
import com.aio.module.fts.entity.FtsTunnelEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * GOST 配置生成工具类
 * 负责构建符合 GOST 协议标准的 JSON 配置并推送到节点
 */
public class GostUtil {

    private static final String CHAINS_SUFFIX = "_chains";
    private static final String TLS_SUFFIX = "_tls";
    private static final String TCP_SUFFIX = "_tcp";
    private static final String UDP_SUFFIX = "_udp";
    private static final String CHAIN_KEY = "chain";

    // 添加私有构造函数
    private GostUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // ================== 限速器管理 ==================

    public static GostDto addLimiters(Long nodeId, Long limiterId, String speed) {
        JSONObject data = createLimiterData(limiterId, speed);
        return WebSocketServer.sendMsg(nodeId, data, "AddLimiters");
    }

    public static GostDto updateLimiters(Long nodeId, Long limiterId, String speed) {
        JSONObject data = createLimiterData(limiterId, speed);
        JSONObject req = new JSONObject();
        req.put("limiter", limiterId + "");
        req.put("data", data);
        return WebSocketServer.sendMsg(nodeId, req, "UpdateLimiters");
    }

    public static GostDto deleteLimiters(Long nodeId, Long limiterId) {
        JSONObject req = new JSONObject();
        req.put("limiter", limiterId + "");
        return WebSocketServer.sendMsg(nodeId, req, "DeleteLimiters");
    }

    // ================== 本地服务管理 (入口) ==================

    public static GostDto addService(Long nodeId, String name, Integer inPort, Integer limiter,
                                     String remoteAddr, Integer type, FtsTunnelEntity tunnel,
                                     String strategy, String interfaceName) {
        JSONArray services = new JSONArray();
        String[] protocols = {"tcp", "udp"};
        for (String protocol : protocols) {
            JSONObject service = createServiceConfig(name, inPort, limiter, remoteAddr, protocol, type, tunnel, strategy, interfaceName);
            services.add(service);
        }
        return WebSocketServer.sendMsg(nodeId, services, "AddService");
    }

    public static GostDto updateService(Long nodeId, String name, Integer inPort, Integer limiter,
                                        String remoteAddr, Integer type, FtsTunnelEntity tunnel,
                                        String strategy, String interfaceName) {
        JSONArray services = new JSONArray();
        String[] protocols = {"tcp", "udp"};
        for (String protocol : protocols) {
            JSONObject service = createServiceConfig(name, inPort, limiter, remoteAddr, protocol, type, tunnel, strategy, interfaceName);
            services.add(service);
        }
        return WebSocketServer.sendMsg(nodeId, services, "UpdateService");
    }

    public static GostDto deleteService(Long nodeId, String name) {
        JSONObject data = new JSONObject();
        JSONArray services = new JSONArray();
        services.add(name + TCP_SUFFIX);
        services.add(name + UDP_SUFFIX);
        data.put("services", services);
        return WebSocketServer.sendMsg(nodeId, data, "DeleteService");
    }

    // 暂停/恢复服务
    public static GostDto pauseService(Long nodeId, String name) {
        JSONObject data = new JSONObject();
        JSONArray services = new JSONArray();
        services.add(name + TCP_SUFFIX);
        services.add(name + UDP_SUFFIX);
        data.put("services", services);
        return WebSocketServer.sendMsg(nodeId, data, "PauseService");
    }

    public static GostDto resumeService(Long nodeId, String name) {
        JSONObject data = new JSONObject();
        JSONArray services = new JSONArray();
        services.add(name + TCP_SUFFIX);
        services.add(name + UDP_SUFFIX);
        data.put("services", services);
        return WebSocketServer.sendMsg(nodeId, data, "ResumeService");
    }

    // ================== 远程服务管理 (出口) ==================

    public static GostDto addRemoteService(Long nodeId, String name, Integer outPort, String remoteAddr,
                                           String protocol, String strategy, String interfaceName) {
        return sendRemoteService(nodeId, name, outPort, remoteAddr, protocol, strategy, interfaceName, "AddService");
    }

    public static GostDto updateRemoteService(Long nodeId, String name, Integer outPort, String remoteAddr,
                                              String protocol, String strategy, String interfaceName) {
        return sendRemoteService(nodeId, name, outPort, remoteAddr, protocol, strategy, interfaceName, "UpdateService");
    }

    public static GostDto deleteRemoteService(Long nodeId, String name) {
        JSONArray data = new JSONArray();
        data.add(name + TLS_SUFFIX);
        JSONObject req = new JSONObject();
        req.put("services", data);
        return WebSocketServer.sendMsg(nodeId, req, "DeleteService");
    }

    private static GostDto sendRemoteService(Long nodeId, String name, Integer outPort, String remoteAddr,
                                             String protocol, String strategy, String interfaceName, String action) {
        JSONObject data = new JSONObject();
        data.put("name", name + TLS_SUFFIX);
        data.put("addr", ":" + outPort);

        if (StringUtils.isNotBlank(interfaceName)) {
            JSONObject metadata = new JSONObject();
            metadata.put("interface", interfaceName);
            data.put("metadata", metadata);
        }

        JSONObject handler = new JSONObject();
        handler.put("type", "relay");
        data.put("handler", handler);

        JSONObject listener = new JSONObject();
        listener.put("type", protocol);
        data.put("listener", listener);

        JSONObject forwarder = createForwarder(remoteAddr, strategy);
        data.put("forwarder", forwarder);

        JSONArray services = new JSONArray();
        services.add(data);
        return WebSocketServer.sendMsg(nodeId, services, action);
    }

    // ================== 转发链 (Chain) 管理 ==================

    public static GostDto addChains(Long nodeId, String name, String remoteAddr, String protocol, String interfaceName) {
        return sendChainConfig(nodeId, name, remoteAddr, protocol, interfaceName, "AddChains");
    }

    public static GostDto updateChains(Long nodeId, String name, String remoteAddr, String protocol, String interfaceName) {
        JSONObject data = createChainData(name, remoteAddr, protocol, interfaceName);
        JSONObject req = new JSONObject();
        req.put(CHAIN_KEY, name + CHAINS_SUFFIX);
        req.put("data", data);
        return WebSocketServer.sendMsg(nodeId, req, "UpdateChains");
    }

    public static GostDto deleteChains(Long nodeId, String name) {
        JSONObject data = new JSONObject();
        data.put(CHAIN_KEY, name + CHAINS_SUFFIX);
        return WebSocketServer.sendMsg(nodeId, data, "DeleteChains");
    }

    private static GostDto sendChainConfig(Long nodeId, String name, String remoteAddr, String protocol, String interfaceName, String action) {
        JSONObject data = createChainData(name, remoteAddr, protocol, interfaceName);
        return WebSocketServer.sendMsg(nodeId, data, action);
    }

    // ================== 私有辅助方法 ==================

    private static JSONObject createLimiterData(Long id, String speed) {
        JSONObject data = new JSONObject();
        data.put("name", id.toString());
        JSONArray limits = new JSONArray();
        limits.add("$ " + speed + "MB " + speed + "MB");
        data.put("limits", limits);
        return data;
    }

    private static JSONObject createServiceConfig(String name, Integer inPort, Integer limiter, String remoteAddr,
                                                  String protocol, Integer type, FtsTunnelEntity tunnel,
                                                  String strategy, String interfaceName) {
        JSONObject service = new JSONObject();
        service.put("name", name + "_" + protocol);

        if (Objects.equals(protocol, "tcp")) {
            service.put("addr", tunnel.getTcpListenAddr() + ":" + inPort);
        } else {
            service.put("addr", tunnel.getUdpListenAddr() + ":" + inPort);
        }

        if (StringUtils.isNotBlank(interfaceName)) {
            JSONObject metadata = new JSONObject();
            metadata.put("interface", interfaceName);
            service.put("metadata", metadata);
        }

        if (limiter != null) {
            service.put("limiter", limiter.toString());
        }

        // 配置 Handler (处理链)
        JSONObject handler = new JSONObject();
        handler.put("type", protocol);
        if (isTunnelForwarding(type)) {
            handler.put(CHAIN_KEY, name + CHAINS_SUFFIX);
        }
        service.put("handler", handler);

        // 配置 Listener
        JSONObject listener = new JSONObject();
        listener.put("type", protocol);
        if (Objects.equals(protocol, "udp")) {
            JSONObject metadata = new JSONObject();
            metadata.put("keepAlive", true);
            listener.put("metadata", metadata);
        }
        service.put("listener", listener);

        // 配置 Forwarder (仅端口转发需要)
        if (isPortForwarding(type)) {
            JSONObject forwarder = createForwarder(remoteAddr, strategy);
            service.put("forwarder", forwarder);
        }
        return service;
    }

    private static JSONObject createChainData(String name, String remoteAddr, String protocol, String interfaceName) {
        JSONObject dialer = new JSONObject();
        dialer.put("type", protocol);
        if (Objects.equals(protocol, "quic")){
            JSONObject metadata = new JSONObject();
            metadata.put("keepAlive", true);
            metadata.put("ttl", "10s");
            dialer.put("metadata", metadata);
        }

        JSONObject connector = new JSONObject();
        connector.put("type", "relay");

        JSONObject node = new JSONObject();
        node.put("name", "node-" + name);
        node.put("addr", remoteAddr);
        node.put("connector", connector);
        node.put("dialer", dialer);

        if (StringUtils.isNotBlank(interfaceName)) {
            node.put("interface", interfaceName);
        }

        JSONArray nodes = new JSONArray();
        nodes.add(node);

        JSONObject hop = new JSONObject();
        hop.put("name", "hop-" + name);
        hop.put("nodes", nodes);

        JSONArray hops = new JSONArray();
        hops.add(hop);

        JSONObject data = new JSONObject();
        data.put("name", name + CHAINS_SUFFIX);
        data.put("hops", hops);
        return data;
    }

    private static JSONObject createForwarder(String remoteAddr, String strategy) {
        JSONObject forwarder = new JSONObject();
        JSONArray nodes = new JSONArray();

        String[] split = remoteAddr.split(",");
        int num = 1;
        for (String addr : split) {
            JSONObject node = new JSONObject();
            node.put("name", "node_" + num );
            node.put("addr", addr);
            nodes.add(node);
            num++;
        }

        if (strategy == null || strategy.isEmpty()) {
            strategy = "fifo";
        }

        forwarder.put("nodes", nodes);

        JSONObject selector = new JSONObject();
        selector.put("strategy", strategy);
        selector.put("maxFails", 1);
        selector.put("failTimeout", "600s");
        forwarder.put("selector", selector);
        return forwarder;
    }

    private static boolean isPortForwarding(Integer type) {
        return type != null && type == 1;
    }

    private static boolean isTunnelForwarding(Integer type) {
        return type != null && type != 1; // 2 or other
    }
}
