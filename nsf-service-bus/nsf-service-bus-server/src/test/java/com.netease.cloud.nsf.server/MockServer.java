package com.netease.cloud.nsf.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.netease.cloud.nsf.config.ConfigStore;
import com.netease.cloud.nsf.config.DefaultConfigStore;
import com.netease.cloud.nsf.dao.ResourceDao;
import com.netease.cloud.nsf.dao.StatusDao;
import com.netease.cloud.nsf.dao.impl.ResourceDaoImpl;
import com.netease.cloud.nsf.dao.impl.StatusDaoImpl;
import com.netease.cloud.nsf.dao.meta.ResourceInfo;
import com.netease.cloud.nsf.parser.MapParserContext;
import com.netease.cloud.nsf.parser.ParserContext;
import com.netease.cloud.nsf.parser.ParserOptions;
import com.netease.cloud.nsf.server.resource.DBSnapshotBuilder;
import com.netease.cloud.nsf.server.resource.SnapshotBuilder;
import com.netease.cloud.nsf.service.TranslateService;
import com.netease.cloud.nsf.service.impl.TranslateServiceImpl;
import com.netease.cloud.nsf.status.*;
import com.netease.cloud.nsf.step.Step;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import nsb.route.Service;
import org.apache.camel.util.IOHelper;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeClass;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);

    private Server server;
    private ServerOptions options;
    private ResourceCache resourceCache;
    private ResourceSourceImpl resourceSource;

    private ObjectMapper objectMapper;
    private Map<String, String> routeMap;

    @BeforeClass
    public void init() {
        this.options = options();
        this.resourceCache = resourceCache();
        this.resourceSource = resourceSource();
        this.server = server();
        this.objectMapper = new ObjectMapper();
        this.routeMap = new HashMap<>();
    }

    private ServerOptions options() {
        ServerOptions options = new ServerOptions();
        options.setGrpcPort(8999);
        return options;
    }

    private ResourceCache resourceCache() {
        return new ResourceCache();
    }

    private ResourceSourceImpl resourceSource() {
        return new ResourceSourceImpl(resourceCache, options);
    }

    private Server server() {
        return NettyServerBuilder.forPort(options.getGrpcPort())
                .keepAliveTime(options.getGrpcKeepAliveTime(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(options.getGrpcKeepAliveTimeout(), TimeUnit.MILLISECONDS)
                .maxMessageSize(options.getGrpcMaxMessageSize())
                .addService(this.resourceSource)
                .build();
    }

    private void watch(Path jsonDirectoryPath, Path xmlDirectoryPath) throws Exception {
        ParserContext parserContext = new MapParserContext(new ParserOptions());
        TranslateService translateService = new TranslateServiceImpl(parserContext);
        SnapshotBuilder snapshotBuilder = new MockSnapshotBuilder(objectMapper, translateService, jsonDirectoryPath, xmlDirectoryPath);
        WatchService service = FileSystems.getDefault().newWatchService();
        jsonDirectoryPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        xmlDirectoryPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        this.server.start();
        resourceCache.setSnapshot(snapshotBuilder.build());

        WatchKey watchKey;
        do {
            watchKey = service.take();
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path eventPath = (Path) event.context();
                logger.info("watch file: " + eventPath + ":" + kind);
            }
            Service.Resources snapshot = snapshotBuilder.build();
            resourceCache.setSnapshot(snapshot);
        } while (watchKey.reset());
    }

    public static void main(String[] args) throws Exception {
        MockServer mockServer = new MockServer();
        String jsonDirectoryPath = "nsf-service-bus/nsf-service-bus-server/src/test/resources/json";
        String xmlDirectoryPath = "nsf-service-bus/nsf-service-bus-server/src/test/resources/xml";
        mockServer.init();
        mockServer.watch(FileSystems.getDefault().getPath(jsonDirectoryPath), FileSystems.getDefault().getPath(xmlDirectoryPath));
    }
}
