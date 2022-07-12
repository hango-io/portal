package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.Valid;
import java.util.List;

/**
 * VirtualCluster Dto
 * 支持用户通过界面输入VirtualCluster信息，包括:path header
 * 实现路由级别监控
 *
 * @author hanjiahao
 */
public class VirtualClusterDto {

    /**
     * Virtual cluster headers
     */
    @JSONField(name = "Headers")
    @Valid
    List<EnvoyRouteRuleMapMatchDto> headers;
    /**
     * 是否开启RouteMetric
     */
    @JSONField(name = "IsRouteMetric")
    private boolean isRouteMetric;
    /**
     * virtual cluster name
     */
    @JSONField(name = "Name")
    private String virtualClusterName;

    public boolean isRouteMetric() {
        return isRouteMetric;
    }

    public void setRouteMetric(boolean routeMetric) {
        isRouteMetric = routeMetric;
    }

    public String getVirtualClusterName() {
        return virtualClusterName;
    }

    public void setVirtualClusterName(String virtualClusterName) {
        this.virtualClusterName = virtualClusterName;
    }

    public List<EnvoyRouteRuleMapMatchDto> getHeaders() {
        return headers;
    }

    public void setHeaders(List<EnvoyRouteRuleMapMatchDto> headers) {
        this.headers = headers;
    }
}
