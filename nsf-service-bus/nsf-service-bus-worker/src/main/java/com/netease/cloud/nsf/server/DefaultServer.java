package com.netease.cloud.nsf.server;


import com.netease.cloud.nsf.sink.SinkClient;
import org.apache.camel.model.ModelCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/18
 **/
public class DefaultServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(DefaultServer.class);

    private ModelCamelContext camelContext;
    private SinkClient sinkClient;

    public DefaultServer(ModelCamelContext camelContext, SinkClient sinkClient) {
        this.camelContext = camelContext;
        this.sinkClient = sinkClient;
    }

    @Override
    public void start() {
        logger.info(">>>>>Worker start<<<<<");
        try {
            camelContext.start();
            sinkClient.establish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            camelContext.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info(">>>>>Worker stop<<<<<");
        }
    }
}
