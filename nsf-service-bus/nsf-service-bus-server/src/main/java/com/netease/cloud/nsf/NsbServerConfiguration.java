package com.netease.cloud.nsf;

import com.netease.cloud.nsf.config.ConfigStore;
import com.netease.cloud.nsf.config.DefaultConfigStore;
import com.netease.cloud.nsf.dao.ResourceDao;
import com.netease.cloud.nsf.dao.StatusDao;
import com.netease.cloud.nsf.dao.impl.ResourceDaoImpl;
import com.netease.cloud.nsf.dao.impl.StatusDaoImpl;
import com.netease.cloud.nsf.parser.MapParserContext;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.ParserOptions;
import com.netease.cloud.nsf.server.ResourceCache;
import com.netease.cloud.nsf.server.ResourceSourceImpl;
import com.netease.cloud.nsf.server.ServerOptions;
import com.netease.cloud.nsf.server.resource.DBSnapshotBuilder;
import com.netease.cloud.nsf.server.resource.SnapshotBuilder;
import com.netease.cloud.nsf.service.TranslateService;
import com.netease.cloud.nsf.service.impl.TranslateServiceImpl;
import com.netease.cloud.nsf.status.*;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import nsb.route.ResourceSourceGrpc;
import nsb.route.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/24
 **/
@Configuration
@ConditionalOnProperty("enableNsb")
public class NsbServerConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    // grpc端口
    @Value("${nsbSourcePort:8899}")
    private int sourcePort;
    // 默认30s发送一次心跳
    @Value("${nsbKeepAliveTime:30000}")
    private long keepaliveTime;
    // 默认10s心跳请求超时
    @Value("${nsbKeepAliveTimeout:10000}")
    private long keepaliveTimeout;
    // 默认最大message为128M
    @Value("${nsbMaxMessageSize:134217728}")
    private int maxMessageSize;
    // status轮询间隔，要求不能小于Snapshot的构建时间
    @Value("${nsbStatusCheckInterval:1000}")
    private long statusCheckInterval;
    // openApi默认暴露的host
    @Value("${nsbOpenApiHost:0.0.0.0}")
    private String openApiHost;
    // openApi默认暴露的port
    @Value("${nsbOpenApiPort:80}")
    private Integer openApiPort;
    // quartz 任务错过触发的补偿策略
    @Value("${nsbQuartzSimpleTriggerMisfireInstructions:1}")
    private Integer quartzSimpleTriggerMisfireInstructions;
    // quartz 任务错过触发的补偿策略
    @Value("${nsbQuartzCronTriggerMisfireInstructions:1}")
    private Integer quartzCronTriggerMisfireInstructions;

    @Bean
    public ServerOptions options() {
        ServerOptions options = new ServerOptions();
        options.setGrpcPort(sourcePort);
        options.setGrpcKeepAliveTime(keepaliveTime);
        options.setGrpcKeepAliveTimeout(keepaliveTimeout);
        options.setGrpcMaxMessageSize(maxMessageSize);
        options.setStatusCheckInterval(statusCheckInterval);
        return options;
    }

    @Bean
    public ParserOptions parserOptions() {
        ParserOptions options = new ParserOptions();
        options.setOpenApiHost(openApiHost);
        options.setOpenApiPort(openApiPort);
        options.setQuartzSimpleTriggerMisfireInstructions(quartzSimpleTriggerMisfireInstructions);
        options.setQuartzCronTriggerMisfireInstructions(quartzCronTriggerMisfireInstructions);
        return options;
    }

    @Bean
    public ParserContext parserContext(ParserOptions options) {
        return new MapParserContext(options);
    }

    @Bean
    public TranslateService translateService(ParserContext parserContext) {
        return new TranslateServiceImpl(parserContext);
    }

    @Bean
    public ConfigStore configStore(TranslateService translateService, TransactionTemplate template, ResourceDao resourceDao, StatusNotifier notifier) {
        return new DefaultConfigStore(translateService, template, resourceDao, notifier);
    }

    @Bean
    public ResourceCache resourceCache() {
        return new ResourceCache();
    }

    @Bean
    public ResourceSourceGrpc.ResourceSourceImplBase resourceSource(ResourceCache cache, ServerOptions options) {
        return new ResourceSourceImpl(cache, options);
    }

    @Bean
    public Server server(ResourceSourceGrpc.ResourceSourceImplBase resourceSource, ServerOptions options) {
        return NettyServerBuilder.forPort(options.getGrpcPort())
                .keepAliveTime(options.getGrpcKeepAliveTime(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(options.getGrpcKeepAliveTimeout(), TimeUnit.MILLISECONDS)
                .maxMessageSize(options.getGrpcMaxMessageSize())
                .addService(resourceSource)
                .build();
    }

    @Bean
    public SnapshotBuilder snapshotBuilder(ResourceDao resourceDao) {
        return new DBSnapshotBuilder(resourceDao);
    }


    @Bean
    public StatusDao statusDao(NamedParameterJdbcTemplate template) {
        return new StatusDaoImpl("NSB_STATUS", template);
    }

    @Bean
    public ResourceDao resourceDao(NamedParameterJdbcTemplate template) {
        return new ResourceDaoImpl("NSB_RESOURCE", template);
    }

    @Bean
    public StatusProductor productor(StatusDao statusDao) {
        return new StatusProductorImpl(statusDao);
    }

    @Bean
    public StatusNotifier notifier(StatusDao statusDao) {
        return new StatusNotifierImpl(statusDao, key -> String.valueOf(System.currentTimeMillis()));
    }

    @Bean
    public StatusMonitor monitor(StatusProductor productor, SnapshotBuilder builder, TransactionTemplate transactionTemplate, ResourceCache cache, ServerOptions serverOptions) {
        StatusMonitor monitor = new StatusMonitorImpl(serverOptions.getStatusCheckInterval(), productor);
        final Logger logger = LoggerFactory.getLogger(monitor.getClass());
        // 此处初始化BiConsumer handle的行为
        monitor.registerHandler(StatusConst.RESOURCES_VERSION, (((event, property) -> {
            Service.Resources snapshot = transactionTemplate.execute(new TransactionCallback<Service.Resources>() {
                @Override
                public Service.Resources doInTransaction(TransactionStatus status) {
                    String thisVersion = property.value;
                    String dbVersion = productor.product().get(StatusConst.RESOURCES_VERSION);
                    if (Objects.equals(thisVersion, dbVersion)) {
                        long start = System.currentTimeMillis();
                        Service.Resources snapshot = builder.build();
                        logger.info("GRpc: SnapshotBuilder: build snapshot for nonce:[{}], size:[{}], consume:[{}]", snapshot.getNonce(), Objects.isNull(snapshot.getResourcesList()) ? 0 : snapshot.getResourcesList().size(), System.currentTimeMillis() - start + "ms");
                        return snapshot;
                    } else {
                        logger.info("GRpc: SnapshotBuilder: Skip building snapshots for outdated resource versions:[{}], current version:[{}]", thisVersion, dbVersion);
                        return null;
                    }
                }
            });
            if (Objects.nonNull(snapshot)) {
                // 此处遍历所有gRPC连接，下发配置快照
                cache.setSnapshot(snapshot);
            }
        })));
        return monitor;
    }

    /**
     * nsb-server启动初始化，会随着Spring启动触发此流程，该函数是nsb-server的启动入口，主要做以下两件事
     * 1.启动StatusMonitor用于定时检查集成的配置变化（当前默认周期1s），
     *   若有变化则向所有连接了gRPC channel的nsb-worker下发配置快照
     * 2.启动gRPC端口的监听，用于nsb-worker进程来主动连接（当前暴露端口8899）
     *
     * @param contextRefreshedEvent 上下文事件，用于获取Spring上下文
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            if (contextRefreshedEvent.getApplicationContext().getBean(StatusMonitor.class) != null) {
                contextRefreshedEvent.getApplicationContext().getBean(StatusMonitor.class).start();
                contextRefreshedEvent.getApplicationContext().getBean(Server.class).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("camel server failed to start.", e);
        }
    }
}
