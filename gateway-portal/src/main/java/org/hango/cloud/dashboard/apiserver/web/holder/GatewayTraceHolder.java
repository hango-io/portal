package org.hango.cloud.dashboard.apiserver.web.holder;

/**
 * @author hanjiahao
 * 网关相关trace，主要用于网关审计
 */
public class GatewayTraceHolder {
    public static final String GATEWAY_ID = "X-Gw-Id";
    public static final String DEFAULT_GATEWAY_ID = "0";

    private static ThreadLocal<String> gatewayTraceId = new ThreadLocal<>();

    public static String getGatewayId() {
        return GatewayTraceHolder.gatewayTraceId.get();
    }

    public static void setGatewayId(String gwId) {
        GatewayTraceHolder.gatewayTraceId.set(gwId);
    }

    public static void removeGatewayId() {
        GatewayTraceHolder.gatewayTraceId.remove();
    }
}
