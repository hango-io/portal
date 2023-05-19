package org.hango.cloud.common.infra.virtualgateway.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class PermissionScopeDto implements Serializable {
    private static final long serialVersionUID = 4284492442066101272L;
    @JSONField(name = "id")
    private long id;
    @JSONField(name = "PermissionScopeName")
    private String permissionScopeName;
    @JSONField(name = "PermissionScopeEnName")
    private String permissionScopeEnName;
    @JSONField(name = "ParentId")
    private long parentId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPermissionScopeName() {
        return permissionScopeName;
    }

    public void setPermissionScopeName(String permissionScopeName) {
        this.permissionScopeName = permissionScopeName;
    }

    public String getPermissionScopeEnName() {
        return permissionScopeEnName;
    }

    public void setPermissionScopeEnName(String permissionScopeEnName) {
        this.permissionScopeEnName = permissionScopeEnName;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
}
