package org.hango.cloud.common.infra.domain.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/11/8
 */
@Getter
@Setter
public class DomainRefreshResult {

    @JSONField(name = "VirtualGwId", ordinal = 1)
    private long virtualGwId;

    @JSONField(name = "VirtualGwName", ordinal = 2)
    private String virtualGwName;

    @JSONField(name = "Env", ordinal = 3)
    private String env;

    @JSONField(name = "RouteProxyNames", ordinal = 4)
    private List<String> routeProxyNames;

    @JSONField(name = "PluginTypes", ordinal = 5)
    private List<String> pluginTypes;

    @JSONField(name = "FailedResult", ordinal = 6)
    private Object failedResult;
}
