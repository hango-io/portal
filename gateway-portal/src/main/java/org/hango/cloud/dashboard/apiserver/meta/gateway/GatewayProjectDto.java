package org.hango.cloud.dashboard.apiserver.meta.gateway;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author hanjiahao
 * 网关所属项目、租户dto,主要对齐前端相关
 */
public class GatewayProjectDto {
    @JSONField(name = "TenantId")
    private long tenantId;
    @JSONField(name = "TenantName")
    private String tenantName;
    @JSONField(name = "ProjectId")
    private long projectId;
    @JSONField(name = "ProjectName")
    private String projectName;

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
