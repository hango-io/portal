package org.hango.cloud.common.infra.route.common;

import org.hango.cloud.common.infra.base.meta.CommonExtension;

import java.util.List;

/**
 * 路由match，match info
 *
 * @author hanjiahao
 */
public class RouteRuleMatchInfo extends CommonExtension {
    /**
     * uri list
     */
    RouteStringMatchInfo uriMatchInfo;

    /**
     * 数据库存储uri
     */
    String uri;

    /**
     * method list
     */
    RouteStringMatchInfo methodMatchInfo;

    /**
     * 数据库存储method
     */
    String method;

    /**
     * host list
     */
    RouteStringMatchInfo hostMatchInfo;

    /**
     * 数据库存储host
     */
    String host;

    /**
     * header list
     */
    List<RouteRuleMapMatchInfo> headerList;

    /**
     * 数据库存储header
     */
    String header;

    List<RouteRuleMapMatchInfo> queryParamList;


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


    public RouteStringMatchInfo getUriMatchInfo() {
        return uriMatchInfo;
    }

    public void setUriMatchInfo(RouteStringMatchInfo uriMatchInfo) {
        this.uriMatchInfo = uriMatchInfo;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public RouteStringMatchInfo getMethodMatchInfo() {
        return methodMatchInfo;
    }

    public void setMethodMatchInfo(RouteStringMatchInfo methodMatchInfo) {
        this.methodMatchInfo = methodMatchInfo;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public RouteStringMatchInfo getHostMatchInfo() {
        return hostMatchInfo;
    }

    public void setHostMatchInfo(RouteStringMatchInfo hostMatchInfo) {
        this.hostMatchInfo = hostMatchInfo;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<RouteRuleMapMatchInfo> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(List<RouteRuleMapMatchInfo> headerList) {
        this.headerList = headerList;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<RouteRuleMapMatchInfo> getQueryParamList() {
        return queryParamList;
    }

    public void setQueryParamList(List<RouteRuleMapMatchInfo> queryParamList) {
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


}

