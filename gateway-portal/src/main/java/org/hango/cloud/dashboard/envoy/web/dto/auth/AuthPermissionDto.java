package org.hango.cloud.dashboard.envoy.web.dto.auth;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Min;

/**
 * 添加授权Dto
 *
 * @author hanjiahao
 */
public class AuthPermissionDto {

    /**
     * 授权信息主键id
     */
    @JSONField(name = "Id")
    private long id;

    /**
     * 外部认证id
     */
    @JSONField(name = "AuthAccountId")
    private long authAccountId;

    /**
     * 外部认证名称
     */
    @JSONField(name = "AuthAccountName")
    private String authAccountName;

    /**
     * 网关id
     */
    @JSONField(name = "GwId")
    @Min(1)
    private long gwId;

    /**
     * 授权网关集群cluster-name
     */
    @JSONField(name = "AppId")
    private String appId;

    /**
     * 授权可访问对象id
     * routeId, serviceId, gwId
     */
    @JSONField(name = "AuthorizationObjectId")
    private String authorizationObjectId;

    /**
     * 授权可访问对象类型
     * gw_route,gw_service,gw_auth
     */
    @JSONField(name = "AuthorizationObjectType")
    private String authorizationObjectType;

    /**
     * 授权创建时间
     */
    @JSONField(name = "CreateTime")
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAuthAccountId() {
        return authAccountId;
    }

    public void setAuthAccountId(long authAccountId) {
        this.authAccountId = authAccountId;
    }

    public String getAuthAccountName() {
        return authAccountName;
    }

    public void setAuthAccountName(String authAccountName) {
        this.authAccountName = authAccountName;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAuthorizationObjectId() {
        return authorizationObjectId;
    }

    public void setAuthorizationObjectId(String authorizationObjectId) {
        this.authorizationObjectId = authorizationObjectId;
    }

    public String getAuthorizationObjectType() {
        return authorizationObjectType;
    }

    public void setAuthorizationObjectType(String authorizationObjectType) {
        this.authorizationObjectType = authorizationObjectType;
    }


    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
