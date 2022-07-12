package com.netease.cloud.nsf;

import com.netease.cloud.nsf.server.Server;
import com.netease.cloud.nsf.sink.SinkClient;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/18
 **/
public class ApplicationListener implements org.springframework.context.ApplicationListener<ApplicationContextEvent> {
    private Server server;

    public ApplicationListener(Server server) {
        this.server = server;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        if (applicationContextEvent instanceof ContextRefreshedEvent) {
            server.start();
        }
        if (applicationContextEvent instanceof ContextClosedEvent) {
            server.shutdown();
        }
    }
}
