package org.hango.cloud.dashboard.apiserver.meta.enums.permission;

import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.BaseSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.GetMethodSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.PluginGetMethodSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.PluginPostMethodSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.PostMethodSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.SyncGetMethodSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.util.Const;

/**
 * action permission enum
 * 需要鉴权的action enum
 *
 * @author hanjiahao
 */
public enum ActionPermissionEnum {
    //服务相关
    CreateService("CreateService", "service", "create", Const.PROJECT_DOMAIN, null),
    UpdateService("UpdateService", "service", "update", Const.PROJECT_DOMAIN, null),
    DeleteService("DeleteService", "service", "delete", Const.PROJECT_DOMAIN, null),
    SyncData("SyncData", "service", "sync", Const.PROJECT_DOMAIN, null),
    //todo 熔断配置需要单独重新判断，服务、网关、API,绑定黑白名单，绑定流控需要重新判断

    //API相关
    CreateApi("CreateApi", "api", "create", Const.PROJECT_DOMAIN, null),
    UpdateApi("UpdateApi", "api", "update", Const.PROJECT_DOMAIN, null),
    DeleteApiById("DeleteApiById", "api", "delete", Const.PROJECT_DOMAIN, null),
    CreateRequestBody("CreateRequestBody", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateQueryString("CreateQueryString", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateStatusCode("CreateStatusCode", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateResponseBody("CreateResponseBody", "api", "create", Const.PROJECT_DOMAIN, null),
    GenerateBodyByJson("GenerateBodyByJson", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateApiExample("CreateApiExample", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateRequestHeader("CreateStatusCode", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateResponseHeader("CreateResponseBody", "api", "create", Const.PROJECT_DOMAIN, null),
    DeleteHeaderByParamId("DeleteHeaderByParamId", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateWebserviceParam("CreateWebserviceParam", "api", "create", Const.PROJECT_DOMAIN, null),
    CreateDubboParam("CreateDubboParam", "api", "create", Const.PROJECT_DOMAIN, null),

    //数据模型相关
    CreateApiModel("CreateApiModel", "model", "create", Const.PROJECT_DOMAIN, null),
    UpdateApiModel("UpdateApiModel", "model", "update", Const.PROJECT_DOMAIN, null),
    DeleteApiModel("DeleteApiModel", "model", "delete", Const.PROJECT_DOMAIN, null),

    //网关相关
    CreateGateway("CreateGateway", "gateway", "create", Const.SYSTEM_DOMAIN, null),
    DeleteGateway("DeleteGateway", "gateway", "delete", Const.SYSTEM_DOMAIN, null),
    UpdateGateway("UpdateGateway", "gateway", "update", Const.SYSTEM_DOMAIN, null),
    CheckAndUpdateGatewayInfo("CheckAndUpdateGatewayInfo", "gateway", "auth", Const.SYSTEM_DOMAIN, null),
    CreateGatewayAuthentication("CreateGatewayAuthentication", "gateway", "auth", Const.SYSTEM_DOMAIN, null),
    DeleteGatewayAuthentication("DeleteGatewayAuthentication", "gateway", "auth", Const.SYSTEM_DOMAIN, null),
    UpdateGatewayWhiteList("UpdateGatewayWhiteListPolicy", "gateway", "whitelist", Const.SYSTEM_DOMAIN, null),

    //策略管理
    //黑白名单
    CreateWhiteList("CreateWhiteList", "strategy", "whitelist", Const.PROJECT_DOMAIN, null),
    UpdateWhiteList("UpdateWhiteList", "strategy", "whitelist", Const.PROJECT_DOMAIN, null),
    DeleteWhiteList("DeleteWhiteList", "strategy", "whitelist", Const.PROJECT_DOMAIN, null),
    BindingWhiteList("BindingWhiteList", "strategy", "whitelist", Const.PROJECT_DOMAIN, null),
    UnbindingPolicy("UnbindingPolicy", "strategy", "whitelist", Const.PROJECT_DOMAIN, null),

    //参数限流（流控）
    CreateTrafficControlPolicy("CreateTrafficControlPolicy", "strategy", "traffic", Const.PROJECT_DOMAIN, null),
    UpdateTrafficControlPolicy("UpdateTrafficControlPolicy", "strategy", "traffic", Const.PROJECT_DOMAIN, null),
    DeleteTrafficControlPolicy("DeleteTrafficControlPolicy", "strategy", "traffic", Const.PROJECT_DOMAIN, null),
    //绑定流控，解绑流控
    BindingTrafficControlPolicy("BindingTrafficControlPolicy", "api", "bindTraffic", Const.PROJECT_DOMAIN, null),
    UnbindingTrafficControlPolicy("UnbindingTrafficControlPolicy", "api", "bindTraffic", Const.PROJECT_DOMAIN, null),
    //熔断策略
    HystrixPolicy("SaveHystrixConfig", "api", "circuit", Const.PROJECT_DOMAIN, null),

    //授权管理
    CreateAuthManage("CreateAuthManage", "strategy", "authz", Const.PROJECT_DOMAIN, null),
    DeleteAuthManage("DeleteAuthManage", "strategy", "authz", Const.PROJECT_DOMAIN, null),

    // Envoy网关路由规则
    CreateRouteRule("CreateRouteRule", "routeRule", "create", Const.PROJECT_DOMAIN, null),
    UpdateRouteRule("UpdateRouteRule", "routeRule", "update", Const.PROJECT_DOMAIN, null),
    DeleteRouteRule("DeleteRouteRule", "routeRule", "delete", Const.PROJECT_DOMAIN, null),
    CopyRouteRule("CopyRouteRule", "routeRule", "copy", Const.PROJECT_DOMAIN, null),

    // Envoy网插件
    BindingPlugin("BindingPlugin", "plugin", "binding", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    UnbindingPlugin("UnbindingPlugin", "plugin", "unbinding", Const.PROJECT_DOMAIN, PluginGetMethodSpecResourceHandler.getInstance()),
    UpdatePluginConfiguration("UpdatePluginConfiguration", "plugin", "updateConfiguration", Const.PROJECT_DOMAIN, PluginPostMethodSpecResourceHandler.getInstance()),
    UpdatePluginBindingStatus("UpdatePluginBindingStatus", "plugin", "updatePluginStatus", Const.PROJECT_DOMAIN, PluginGetMethodSpecResourceHandler.getInstance()),

    //发布相关
    PublishRouteRule("PublishRouteRule", "publish", "routeProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    SyncRouteProxy("SyncRouteProxy", "publish", "routeProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    OfflineRouteRule("DeletePublishedRouteRule", "publish", "routeProxy", Const.PROJECT_DOMAIN, GetMethodSpecResourceHandler.getInstance()),
    UpdatePublishedRouteRuleState("UpdateRouteRuleEnableState", "publish", "routeProxy", Const.PROJECT_DOMAIN, GetMethodSpecResourceHandler.getInstance()),
    UpdatePublishedRouteRule("UpdateRouteRuleProxy", "publish", "routeProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    CreateServiceRoute("CreateServiceRoute", "publish", "serviceProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    DeleteServiceRoute("DeleteServiceRoute", "publish", "serviceProxy", Const.PROJECT_DOMAIN, GetMethodSpecResourceHandler.getInstance()),
    UpdateServiceTimeRange("UpdateServiceTimeRange", "publish", "serviceProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    UpdateServiceRoute("UpdateServiceRoute", "publish", "serviceProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    PublishService2("PublishService", "publish", "serviceProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    UpdatePublishedService2("UpdatePublishService", "publish", "serviceProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    DeleteServiceProxy2("DeleteServiceProxy", "publish", "serviceProxy", Const.PROJECT_DOMAIN, GetMethodSpecResourceHandler.getInstance()),
    PublishApi("PublishApi", "publish", "ApiProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    PublishMultiApi("PublishMultiApi", "publish", "ApiProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    OfflineMultiApi("OfflineMultiApi", "publish", "ApiProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    OfflineApi("OfflineApi", "publish", "ApiProxy", Const.PROJECT_DOMAIN, GetMethodSpecResourceHandler.getInstance()),
    UpdatePublishedApi("UpdatePublishedApi", "publish", "ApiProxy", Const.PROJECT_DOMAIN, PostMethodSpecResourceHandler.getInstance()),
    //同步发布
    CopyServiceProxy("CopyServiceProxy", "publish", "serviceCopyProxy", Const.PROJECT_DOMAIN,
            SyncGetMethodSpecResourceHandler.getInstance()),
    CopyRouteRuleProxy("CopyRouteRuleProxy", "publish", "routeCopyProxy", Const.PROJECT_DOMAIN,
            SyncGetMethodSpecResourceHandler.getInstance()),
    CopyRouteRuleProxyByProxyId("CopyRouteRuleProxyByProxyId", "publish", "routeCopyProxy", Const.PROJECT_DOMAIN,
            SyncGetMethodSpecResourceHandler.getInstance()),

    // Envoy网关服务相关新增逻辑
    UpdateHealthCheckRule("UpdateHealthCheckRule", "service", "healthCheck", Const.PROJECT_DOMAIN, null),

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
     * 作用域
     */
    private String domain;

    /**
     * 自定义资源
     * 如果需要自定义资源，指定自定义资源对应字段
     * 如果不需要自定义资源，该字段为空
     */
    private BaseSpecResourceHandler specResourceHandler;


    ActionPermissionEnum(String action, String resource, String operation, String domain, BaseSpecResourceHandler specResourceHandler) {
        this.action = action;
        this.resource = resource;
        this.operation = operation;
        this.domain = domain;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public BaseSpecResourceHandler getSpecResourceHandler() {
        return specResourceHandler;
    }

    public void setSpecResourceHandler(BaseSpecResourceHandler specResourceHandler) {
        this.specResourceHandler = specResourceHandler;
    }
}
