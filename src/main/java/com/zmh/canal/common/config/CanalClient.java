package com.zmh.canal.common.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.zmh.canal.common.handler.CanalMessageHandler;
import com.zmh.canal.common.handler.IEsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Description: CanalClient
 * @author: zhou ming hao
 * @date: 2024年08月15日 1:49
 */
@Slf4j
@Component
@EnableConfigurationProperties(CanalClientProperties.class)
public class CanalClient implements DisposableBean {

    /**
     * 获取canal客户端连接
     */
    private CanalConnector canalConnector;


    @Autowired
    private CanalClientProperties canalClientProperties;

    /**
     * 获取canal客户端连接
     */
    @Bean
    public CanalConnector getCanalConnector() {
        canalConnector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalClientProperties.getHost(), canalClientProperties.getPort()),
                canalClientProperties.getDestination(), canalClientProperties.getUsername(), canalClientProperties.getPassword());
        canalConnector.connect();
        // 指定filter，格式 {database}.{table}，这里不做过滤，过滤操作留给用户
        canalConnector.subscribe();
        // 回滚寻找上次中断的位置
        canalConnector.rollback();
        log.info("canal客户端启动成功");
        return canalConnector;
    }


    /**
     * canal消息处理
     */
    @Bean
    public CanalMessageHandler getMessageHandler(List<IEsHandler> entryHandlerList) {
        return new CanalMessageHandler(entryHandlerList);
    }


    @Override
    public void destroy() {
        if (canalConnector != null) {
            canalConnector.disconnect();
        }
    }

}
