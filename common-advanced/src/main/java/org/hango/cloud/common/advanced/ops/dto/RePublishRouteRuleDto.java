package org.hango.cloud.common.advanced.ops.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 路由重发布Dto
 *
 * @author hzchenzhongyang 2020-01-19
 */
public class RePublishRouteRuleDto {

    @JSONField(name = "VirtualGwId")
    @Min(1)
    private long virtualGwId;

    @JSONField(name = "RePublishAllRouteRule")
    private boolean rePublishAllRouteRule = true;

    @JSONField(name = "ServiceIdList")
    private List<Long> serviceIdList;

    @JSONField(name = "RouteRuleIdList")
    private List<Long> routeRuleIdList;

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public boolean getRePublishAllRouteRule() {
        return rePublishAllRouteRule;
    }

    public void setRePublishAllRouteRule(boolean rePublishAllRouteRule) {
        this.rePublishAllRouteRule = rePublishAllRouteRule;
    }

    public List<Long> getServiceIdList() {
        return serviceIdList;
    }

    public void setServiceIdList(List<Long> serviceIdList) {
        this.serviceIdList = serviceIdList;
    }

    public List<Long> getRouteRuleIdList() {
        return routeRuleIdList;
    }

    public void setRouteRuleIdList(List<Long> routeRuleIdList) {
        this.routeRuleIdList = routeRuleIdList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
