package com.aio.module.fts.utils;

import com.aio.module.fts.dto.GostDto;
import com.alibaba.fastjson.JSONObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点通信核心服务
 * URL: ws://your-domain/websocket/{nodeId}
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{sid}")
public class WebSocketServer {

    // 存储所有在线节点的连接: NodeId -> Session
    private static final ConcurrentHashMap<String, Session> sessionPools = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        // 要做的: 建议此处增加鉴权逻辑，校验节点携带的 Secret 是否匹配数据库
        sessionPools.put(sid, session);
        log.info("节点上线: ID={}, 当前在线数={}", sid, sessionPools.size());
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        sessionPools.remove(sid);
        log.info("节点离线: ID={}, 当前在线数={}", sid, sessionPools.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: SessionID={}, Cause={}", session.getId(), error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理节点主动上报的心跳或状态信息 (原项目此处逻辑较少，主要用于调试)
        log.debug("收到节点消息: {}", message);
    }

    /**
     * 向指定节点发送指令
     *
     * @param nodeId 节点ID
     * @param data   配置数据 (JSONObject / JSONArray)
     * @param action 操作动作 (e.g. "AddService", "UpdateLimiters")
     */
    public static GostDto sendMsg(Long nodeId, Object data, String action) {
        String sid = String.valueOf(nodeId);
        Session session = sessionPools.get(sid);

        if (session == null || !session.isOpen()) {
            return GostDto.error("节点离线或未连接: " + nodeId);
        }

        try {
            JSONObject request = new JSONObject();
            request.put("action", action);
            request.put("data", data);

            // 发送消息
            synchronized (session) {
                session.getBasicRemote().sendText(request.toJSONString());
            }

            // 简单模式：默认发送成功即视为成功
            // 进阶模式：可以设计 Future 等待节点的 ACK 回复
            return GostDto.success(null);

        } catch (IOException e) {
            log.error("发送指令失败: NodeId={}, Error={}", nodeId, e.getMessage());
            return GostDto.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 检查节点是否在线
     */
    public static boolean isOnline(Long nodeId) {
        return sessionPools.containsKey(String.valueOf(nodeId));
    }
}
