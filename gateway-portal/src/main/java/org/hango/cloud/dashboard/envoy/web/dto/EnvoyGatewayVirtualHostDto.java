package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关中virtual host Dto
 *
 * @author hzchenzhongyang 2020-01-10
 */
public class EnvoyGatewayVirtualHostDto {

    @JSONField(name = "GwId")
    private long gwId;

    @JSONField(name = "VirtualHostList")
    private List<EnvoyVirtualHostDto> virtualHostList;

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public List<EnvoyVirtualHostDto> getVirtualHostList() {
        return virtualHostList;
    }

    public void setVirtualHostList(List<EnvoyVirtualHostDto> virtualHostList) {
        this.virtualHostList = virtualHostList;
    }

    public List<EnvoyVirtualHostInfo> toEnvoyVirutalHostList() {
        return this.getVirtualHostList().stream()
                .map(EnvoyVirtualHostDto::toMeta)
                .peek(item -> item.setGwId(this.getGwId())).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
