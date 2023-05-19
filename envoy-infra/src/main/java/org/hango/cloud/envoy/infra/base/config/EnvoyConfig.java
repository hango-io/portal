package org.hango.cloud.envoy.infra.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/20
 */
@Component
public class EnvoyConfig {


    @Value("${bakApiPlaneAddr:#{null}}")
    private String bakApiPlaneAddr;

    /**
     * Dubbo 元数据刷新时间
     */
    @Value("${metaRefreshInterval:30000}")
    private Integer metaRefreshInterval;

    /**
     * 请求排行榜Top N
     */
    @Value("${topN:10}")
    private Integer topN;

    public Integer getMetaRefreshInterval() {
        return metaRefreshInterval;
    }

    public String getBakApiPlaneAddr() {
        return bakApiPlaneAddr;
    }

    public Integer getTopN() {
        return topN;
    }
}
