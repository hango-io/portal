package org.hango.cloud.dashboard.envoy.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 网关vh meta
 *
 * @author hzchenzhongyang 2020-01-09
 */
public class EnvoyVirtualHostInfo {
    /**
     * vh id
     */
    private long id;
    /**
     * 项目id
     */
    private long projectId;
    /**
     * 网关id
     */
    private long gwId;
    /**
     * vh中域名列表
     */
    private String hosts;
    /**
     * vh中域名列表
     */
    private List<String> hostList;
    /**
     * vh唯一标识
     */
    private String virtualHostCode;

    /**
     * 域名关联type
     * host/project
     */
    private String bindType;

    /**
     * 域名关联项目list
     */
    private List<Long> projectList;

    /**
     * 项目关联project
     */
    private String projects;

    private long createTime;

    private long updateTime;

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

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getVirtualHostCode() {
        return virtualHostCode;
    }

    public void setVirtualHostCode(String virtualHostCode) {
        this.virtualHostCode = virtualHostCode;
    }

    public List<String> getHostList() {
        return hostList;
    }

    public void setHostList(List<String> hostList) {
        this.hostList = hostList;
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

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
