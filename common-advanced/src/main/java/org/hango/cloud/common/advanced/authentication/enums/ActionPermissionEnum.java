package org.hango.cloud.common.advanced.authentication.enums;


import org.hango.cloud.common.advanced.authentication.handler.BaseSpecResourceHandler;
import org.hango.cloud.common.advanced.authentication.handler.PluginGetMethodSpecResourceHandler;
import org.hango.cloud.common.advanced.authentication.handler.PluginPostMethodSpecResourceHandler;
import org.hango.cloud.common.advanced.authentication.handler.PostMethodSpecResourceHandler;

/**
 * action permission enum
 * 需要鉴权的action enum
 *
 * @author hanjiahao
 */
@SuppressWarnings({"java:S115", "java:S1192"})
public enum ActionPermissionEnum {


    //服务相关
    CreateService("CreateService", "service", "create", null),
    UpdateService("UpdateService", "service", "update", null),
    UpdateHealthCheckRule("UpdateHealthCheckRule", "service", "update", null),
    CheckUploadWsdlFile("CheckUploadWsdlFile", "service", "update", null),
    SaveAndPublishedPbFile("SaveAndPublishedPbFile", "service", "update", null),
    OfflinePbFile("OfflinePbFile", "service", "update", null),
    PublicPbService("PublicPbService", "service", "update", null),
    OfflinePbService("OfflinePbService", "service", "update", null),
    DeleteService("DeleteService", "service", "delete", null),
    GetSwaggerDetailsByLocation("GetSwaggerDetailsByLocation", "service", "loadInterfaces", null),

    // 路由规则
    CreateRoute("CreateRoute", "routeRule", "create", null),
    UpdateRoute("UpdateRoute", "routeRule", "update", null),
    PublishRouteMirror("PublishRouteMirror", "routeRule", "update", null),
    PublishDubbo("PublishDubbo", "routeRule", "update", null),
    UpdateRouteWsParam("UpdateRouteWsParam", "routeRule", "update", null),
    RenderWsRequestTemplate("RenderWsRequestTemplate", "routeRule", "update", null),
    DeleteRouteWsParam("DeleteRouteWsParam", "routeRule", "update", null),
    DeleteRoute("DeleteRoute", "routeRule", "delete", null),
    CopyRoute("CopyRoute", "routeRule", "copy", null),
    UpdateRouteEnableState("UpdateRouteEnableState", "routeRule", "updateStatus", null),

    //API相关
    CreateApi("CreateApi", "api", "create", null),
    UpdateApi("UpdateApi", "api", "update", null),
    CreateQueryString("CreateQueryString", "api", "update", null),
    CreateRequestHeader("CreateRequestHeader", "api", "update", null),
    CreateRequestBody("CreateRequestBody", "api", "update", null),
    CreateResponseHeader("CreateResponseHeader", "api", "update", null),
    CreateResponseBody("CreateResponseBody", "api", "update", null),
    CreateStatusCode("CreateStatusCode", "api", "update", null),
    DeleteApiById("DeleteApiById", "api", "delete", null),

    //数据模型相关
    CreateApiModel("CreateApiModel", "model", "create", null),
    UpdateApiModel("UpdateApiModel", "model", "update", null),
    DeleteApiModel("DeleteApiModel", "model", "delete", null),

    // 插件
    BindingPlugin("BindingPlugin", "plugin", "create", PostMethodSpecResourceHandler.getInstance()),
    CopyGlobalPlugin("CopyGlobalPlugin", "plugin", "create", PostMethodSpecResourceHandler.getInstance()),
    UnbindingPlugin("UnbindingPlugin", "plugin", "delete", PluginGetMethodSpecResourceHandler.getInstance()),
    UpdatePluginConfiguration("UpdatePluginConfiguration", "plugin", "updateConfiguration", PluginPostMethodSpecResourceHandler.getInstance()),
    UpdatePluginBindingStatus("UpdatePluginBindingStatus", "plugin", "updatePluginStatus", PluginGetMethodSpecResourceHandler.getInstance()),

    // 模板管理
    CreatePluginTemplate("CreatePluginTemplate", "template", "create", null),
    UpdatePluginTemplate("UpdatePluginTemplate", "template", "update", null),
    SyncPluginTemplates("SyncPluginTemplates", "template", "update", null),
    DeletePluginTemplate("DeletePluginTemplate", "template", "delete", null),
    CreateGlobalPluginTemplate("CreateGlobalPluginTemplate", "template", "create", null),


    //域名管理
    CreateDomain("CreateDomain", "domain", "create", null),
    UpdateDomain("UpdateDomain", "domain", "update", null),
    DeleteDomain("DeleteDomain", "domain", "delete", null),

    //项目网关管理
    RefreshServiceHost("RefreshServiceHost", "projectGateway", "refreshServiceDomain", null),
    UnbindDomainInfo("UnbindDomainInfo", "projectGateway", "removeDomain", null),
    BindDomainInfo("BindDomainInfo", "projectGateway", "addDomain", null),

    //虚拟网关管理
    CreateVirtualGateway("CreateVirtualGateway", "virtualGateway", "create", null),
    UpdateVirtualGateway("UpdateVirtualGateway", "virtualGateway", "update", null),
    UpdateVirtualGatewaySetting("UpdateVirtualGatewaySetting", "virtualGateway", "update", null),
    DeleteVirtualGateway("DeleteVirtualGateway", "virtualGateway", "delete", null),
    UpdateProjectBinding("UpdateProjectBinding", "virtualGateway", "attachProject", null),
    UpdatePluginManager("UpdatePluginManager", "virtualGateway", "plugin", null),
    UnBindProject("UnBindProject", "virtualGateway", "attachProject", null),

    //网关管理
    UpdateGateway("UpdateGateway", "gateway", "update", null),

    //证书管理
    CreateCertificate("CreateCertificate", "ssl", "load", null),
    DeleteCertificate("DeleteCertificate", "ssl", "delete", null),

    //插件市场
    PluginImport("PluginImport", "pluginMarket", "load", null),
    PluginUpdate("PluginUpdate", "pluginMarket", "update", null),
    UpdatePluginStatus("UpdatePluginStatus", "pluginMarket", "update", null),
    DeletePlugin("DeletePlugin", "pluginMarket", "delete", null),
    ;


    /**
     * action
     */
    private String action;
    /**
     * 资源
     */
    private String resource;
    /**
     * 操作
     */
    private String operation;

    /**
     * 自定义资源
     * 如果需要自定义资源，指定自定义资源对应字段
     * 如果不需要自定义资源，该字段为空
     */
    private BaseSpecResourceHandler specResourceHandler;


    ActionPermissionEnum(String action, String resource, String operation, BaseSpecResourceHandler specResourceHandler) {
        this.action = action;
        this.resource = resource;
        this.operation = operation;
        this.specResourceHandler = specResourceHandler;
    }

    public static ActionPermissionEnum getActionPermissionEnum(String action) {
        for (ActionPermissionEnum actionPermissionEnum : ActionPermissionEnum.values()) {
            if (actionPermissionEnum.getAction().equals(action)) {
                return actionPermissionEnum;
            }
        }
        return null;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BaseSpecResourceHandler getSpecResourceHandler() {
        return specResourceHandler;
    }

    public void setSpecResourceHandler(BaseSpecResourceHandler specResourceHandler) {
        this.specResourceHandler = specResourceHandler;
    }
}
