package org.hango.cloud.dashboard.envoy.meta;

import java.util.List;
import java.util.Objects;

/**
 * 路由match，match info
 *
 * @author hanjiahao
 */
public class RouteRuleMatchInfo {
    /**
     * uri list
     */
    EnvoyRouteStringMatchInfo uriMatchInfo;

    /**
     * 数据库存储uri
     */
    String uri;

    /**
     * method list
     */
    EnvoyRouteStringMatchInfo methodMatchInfo;

    /**
     * 数据库存储method
     */
    String method;

    /**
     * host list
     */
    EnvoyRouteStringMatchInfo hostMatchInfo;

    /**
     * 数据库存储host
     */
    String host;

    /**
     * header list
     */
    List<EnvoyRouteRuleMapMatchInfo> headerList;

    /**
     * 数据库存储header
     */
    String header;

    List<EnvoyRouteRuleMapMatchInfo> queryParamList;


    String queryParam;

    /**
     * 路由规则优先级
     */
    long priority;

    /**
     * 路由规则orders，发送至api-plane
     * orders = priority * 100000 + isExact * 20000 + pathLength * 20 + routeNumber
     */
    long orders;


    public EnvoyRouteStringMatchInfo getUriMatchInfo() {
        return uriMatchInfo;
    }

    public void setUriMatchInfo(EnvoyRouteStringMatchInfo uriMatchInfo) {
        this.uriMatchInfo = uriMatchInfo;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public EnvoyRouteStringMatchInfo getMethodMatchInfo() {
        return methodMatchInfo;
    }

    public void setMethodMatchInfo(EnvoyRouteStringMatchInfo methodMatchInfo) {
        this.methodMatchInfo = methodMatchInfo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public EnvoyRouteStringMatchInfo getHostMatchInfo() {
        return hostMatchInfo;
    }

    public void setHostMatchInfo(EnvoyRouteStringMatchInfo hostMatchInfo) {
        this.hostMatchInfo = hostMatchInfo;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<EnvoyRouteRuleMapMatchInfo> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<EnvoyRouteRuleMapMatchInfo> headerList) {
        this.headerList = headerList;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<EnvoyRouteRuleMapMatchInfo> getQueryParamList() {
        return queryParamList;
    }

    public void setQueryParamList(List<EnvoyRouteRuleMapMatchInfo> queryParamList) {
        this.queryParamList = queryParamList;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public long getOrders() {
        return orders;
    }

    public void setOrders(long orders) {
        this.orders = orders;
    }

    public boolean isSame(RouteRuleMatchInfo routeRuleInfo) {
        return getPriority() == routeRuleInfo.getPriority() &&
                getOrders() == routeRuleInfo.getOrders() &&
                Objects.equals(getUri(), routeRuleInfo.getUri()) &&
                Objects.equals(getMethod(), routeRuleInfo.getMethod()) &&
                Objects.equals(getHost(), routeRuleInfo.getHost()) &&
                Objects.equals(getHeader(), routeRuleInfo.getHeader()) &&
                Objects.equals(getQueryParam(), routeRuleInfo.getQueryParam());
    }

}

