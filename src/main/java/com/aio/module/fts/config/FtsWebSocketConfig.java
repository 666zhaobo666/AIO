package com.aio.module.fts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class FtsWebSocketConfig {

    /**
     * 扫描并注册所有 @ServerEndpoint 注解的 Bean
     * 注意：如果使用外置 Tomcat 容器，则不需要该 Bean
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
