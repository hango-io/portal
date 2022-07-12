package com.netease.cloud.nsf.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckHelper;

public class HealthCheckRoute extends RouteBuilder {

    private final String healthCheckPath;
    private final Integer healthCheckPort;

    public HealthCheckRoute(String healthCheckPath, Integer healthCheckPort) {
        this.healthCheckPath = healthCheckPath;
        this.healthCheckPort = healthCheckPort;
    }

    @Override
    public void configure() throws Exception {
        String healthUri = healthCheckUri();
        from(healthUri)
                // 健康检查不打印trace日志
                .noTracing()
                .process(exchange -> {
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                    HealthCheckHelper.invoke(exchange.getContext()).forEach(item -> {
                        if (!HealthCheck.State.UP.equals(item.getState())) {
                            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
                            item.getMessage().ifPresent(msg -> exchange.getMessage().setBody(msg));
                        }
                    });
                });
    }

    private String healthCheckUri() {
        if (healthCheckPath.startsWith("/")) {
            return String.format("jetty://http://0.0.0.0:%s%s?httpMethodRestrict=GET", healthCheckPort, healthCheckPath);
        } else {
            return String.format("jetty://http://0.0.0.0:%s/%s?httpMethodRestrict=GET", healthCheckPort, healthCheckPath);
        }
    }
}
