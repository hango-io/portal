package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Envoy网关vh更新dto
 *
 * @author hzchenzhongyang 2020-03-09
 */
public class EnvoyVirtualHostUpdateDto {
    @JSONField(name = "VirtualHostId")
    private long virtualHostId;

    @JSONField(name = "HostList")
    private List<String> hostList;

    @JSONField(name = "BindType")
    private String bindType = "host";

    @JSONField(name = "ProjectList")
    private List<Long> projectList;

    public long getVirtualHostId() {
        return virtualHostId;
    }

    public void setVirtualHostId(long virtualHostId) {
        this.virtualHostId = virtualHostId;
    }

    public List<String> getHostList() {
        return hostList;
    }

    public void setHostList(List<String> hostList) {
        this.hostList = hostList;
    }

    public String getBindType() {
        return bindType;
    }

    public void setBindType(String bindType) {
        this.bindType = bindType;
    }

    public List<Long> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Long> projectList) {
        this.projectList = projectList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
