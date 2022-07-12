package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关vh Dto
 *
 * @author hzchenzhongyang 2020-01-08
 */
public class EnvoyVirtualHostDto {
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "ProjectId")
    @Min(1)
    private long projectId;

    @JSONField(name = "GwId")
    @Min(1)
    private long gwId;

    @JSONField(name = "HostList")
    private List<String> hostList = Lists.newArrayList();

    @JSONField(name = "VirtualHostCode")
    private String virtualHostCode;

    @JSONField(name = "CreateTime")
    private long createTime;

    @JSONField(name = "UpdateTime")
    private long updateTime;

    @JSONField(name = "BindType")
    @Pattern(regexp = "host|project")
    private String bindType = "host";

    @JSONField(name = "ProjectList")
    private List<Long> projectList = Lists.newArrayList();

    public static EnvoyVirtualHostDto fromMeta(EnvoyVirtualHostInfo info) {
        if (info == null) {
            return null;
        }
        EnvoyVirtualHostDto dto = new EnvoyVirtualHostDto();
        dto.setId(info.getId());
        dto.setGwId(info.getGwId());
        dto.setHostList(info.getHostList());
        dto.setProjectId(info.getProjectId());
        dto.setCreateTime(info.getCreateTime());
        dto.setUpdateTime(info.getUpdateTime());
        dto.setVirtualHostCode(info.getVirtualHostCode());
        dto.setBindType(info.getBindType());
        dto.setProjectList(info.getProjectList());
        return dto;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public List<String> getHostList() {
        return hostList;
    }

    public void setHostList(List<String> hostList) {
        this.hostList = hostList;
    }

    public String getVirtualHostCode() {
        return virtualHostCode;
    }

    public void setVirtualHostCode(String virtualHostCode) {
        this.virtualHostCode = virtualHostCode;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
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

    public EnvoyVirtualHostInfo toMeta() {
        EnvoyVirtualHostInfo info = new EnvoyVirtualHostInfo();
        info.setId(this.id);
        info.setGwId(this.gwId);
        List<String> hostListTrim = hostList.stream().map(String::trim).collect(Collectors.toList());
        info.setHostList(hostListTrim);
        info.setProjectId(this.projectId);
        info.setCreateTime(this.createTime);
        info.setUpdateTime(this.updateTime);
        info.setHosts(JSON.toJSONString(hostListTrim));
        info.setVirtualHostCode(this.virtualHostCode);
        info.setBindType(this.bindType);
        info.setProjectList(this.projectList);
        info.setProjects(JSON.toJSONString(this.projectList));
        return info;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
