package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 路由复制，目标路由发布网关/端口信息
 *
 * @author hanjiahao
 */
public class EnvoyCopyRulePortDto {
    /**
     * 目标路由发布网关id
     */
    @JSONField(name = "GwId")
    @Min(1)
    private long gwId;

    /**
     * 目标路由发布网关port
     */
    @JSONField(name = "Port")
    @Max(65536)
    private int port;

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
