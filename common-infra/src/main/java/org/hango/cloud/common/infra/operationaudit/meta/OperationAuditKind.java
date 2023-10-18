package org.hango.cloud.common.infra.operationaudit.meta;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 操作审计元数据存储类
 * @date 2022/4/6
 */
@SuppressWarnings({ "java:S1192"})
public class OperationAuditKind {

    protected static final Map<String, OperationAuditRule> kindMap = Maps.newHashMap();
    private static final String DEFAULT_RETURN = "$.Result";

    static {
        /*********************************************** Gateway Start *****************************************************/
        kindMap.put("CreateGateway", OperationAuditRule.get().eventName("CreateGwInfo").description("创建网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).jsonPathForRSNReq("$.GwName").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteGateway", OperationAuditRule.get().eventName("CreateGwInfo").description("删除网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).jsonPathForRSIReq("$.VirtualGwId").jsonPathForRSNReq("$.GwName"));
        kindMap.put("UpdateGateway", OperationAuditRule.get().eventName("UpdateGwInfo").description("更新网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).readRSNFromQuery("VirtualGwId").jsonPathForRSNResp(DEFAULT_RETURN));
        /*********************************************** Gateway End *****************************************************/

        /*********************************************** VirtualGateway Start *****************************************************/
        kindMap.put("CreateVirtualGateway", OperationAuditRule.get().eventName("CreateVirtualGateway").description("创建虚拟网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSNReq("$.Name").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteVirtualGateway", OperationAuditRule.get().eventName("DeleteVirtualGateway").description("删除虚拟网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSIReq("$.VirtualGwId"));
        kindMap.put("UpdateVirtualGateway", OperationAuditRule.get().eventName("UpdateVirtualGateway").description("更新虚拟网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSNReq("Name"));
        kindMap.put("UpdateProjectBinding", OperationAuditRule.get().eventName("UpdateProjectBinding").description("关联项目").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSIReq("VirtualGwId"));
        kindMap.put("UnBindProject", OperationAuditRule.get().eventName("UnBindProject").description("解绑项目").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).readRSIFromQuery("VirtualGwId"));
        kindMap.put("BindDomainInfo", OperationAuditRule.get().eventName("BindDomainInfo").description("绑定域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSIReq("$.VirtualGwId"));
        kindMap.put("UnbindDomainInfo", OperationAuditRule.get().eventName("UnbindDomainInfo").description("解绑域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSIReq("$.VirtualGwId"));
        kindMap.put("UpdateVirtualGatewaySetting", OperationAuditRule.get().eventName("UpdateVirtualGatewaySetting").description("更新虚拟网关高级配置").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY).jsonPathForRSIReq("$.VirtualGwId"));
        kindMap.put("RefreshKubernetesGateway", OperationAuditRule.get().eventName("RefreshKubernetesGateway").description("刷新虚拟网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_VIRTUAL_GATEWAY));

        /*********************************************** VirtualGateway End *****************************************************/

        /*********************************************** Domain Start *****************************************************/
        kindMap.put("CreateDomain", OperationAuditRule.get().eventName("CreateDomain").description("创建域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DOMAIN).jsonPathForRSNReq("$.Host").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("UpdateDomain", OperationAuditRule.get().eventName("UpdateDomain").description("更新域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DOMAIN).jsonPathForRSNReq("$.Host").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteDomain", OperationAuditRule.get().eventName("DeleteDomain").description("删除域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DOMAIN).readRSIFromQuery("DomainId"));
        /*********************************************** Domain End *****************************************************/

        /*********************************************** Cert Start *****************************************************/
        kindMap.put("CreateCertificate", OperationAuditRule.get().eventName("CreateCertificate").description("创建证书").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_CERT).jsonPathForRSNReq("$.CertificateName").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteCertificate", OperationAuditRule.get().eventName("DeleteCertificate").description("删除证书").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_CERT).readRSIFromQuery("CertificateId"));
        /*********************************************** Cert End *****************************************************/

        /*********************************************** Service Start *****************************************************/
        kindMap.put("CreateService", OperationAuditRule.get().eventName("CreateService").description("创建服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSNReq("$.Name").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("UpdateService", OperationAuditRule.get().eventName("UpdateService").description("更新服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSIReq("$.Id").jsonPathForRSIReq("$.Name"));
        kindMap.put("DeleteService", OperationAuditRule.get().eventName("DeleteService").description("删除服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).readRSIFromQuery("Id").jsonPathForRSNResp(DEFAULT_RETURN));
        kindMap.put("UpdateHealthCheckRule", OperationAuditRule.get().eventName("UpdateHealthCheckRule").description("更新健康检查").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSIReq("$.ServiceId"));
        kindMap.put("RefreshServiceHost", OperationAuditRule.get().eventName("RefreshServiceHost").description("刷新服务域名").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSIReq("$.ServiceIds"));
        /*********************************************** Service End *****************************************************/

        /*********************************************** Route Start *****************************************************/
        kindMap.put("CreateRoute", OperationAuditRule.get().eventName("CreateRoute").description("创建路由").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).jsonPathForRSNReq("$.Name").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("UpdateRoute", OperationAuditRule.get().eventName("UpdateRoute").description("更新路由").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).jsonPathForRSNReq("$.Name").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteRoute", OperationAuditRule.get().eventName("DeleteRoute").description("删除路由").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).readRSIFromQuery("RouteId"));
        kindMap.put("UpdateRouteEnableState", OperationAuditRule.get().eventName("UpdateRouteEnableState").description("更新路由使能状态").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).readRSIFromQuery("RouteId"));
        kindMap.put("CopyRoute", OperationAuditRule.get().eventName("CopyRoute").description("路由复制").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).readRSIFromQuery("RouteId"));
        kindMap.put("PublishRouteMirror", OperationAuditRule.get().eventName("PublishRouteMirror").description("发布流量镜像").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_ROUTE).jsonPathForRSIReq("$.RouteId"));
        /*********************************************** Route End *****************************************************/

        /*********************************************** plugin Start *****************************************************/
        kindMap.put("BindingPlugin", OperationAuditRule.get().eventName("BindingPlugin").description("绑定插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.PluginType"));
        kindMap.put("UnbindingPlugin", OperationAuditRule.get().eventName("UnbindingPlugin").description("解绑插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("PluginBindingInfoId"));
        kindMap.put("UpdatePluginConfiguration", OperationAuditRule.get().eventName("UpdatePluginConfiguration").description("更新插件配置").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSIReq("$.PluginBindingInfoId"));
        kindMap.put("UpdatePluginBindingStatus", OperationAuditRule.get().eventName("UpdatePluginBindingStatus").description("更新插件绑定状态").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("PluginBindingInfoId"));
        kindMap.put("CopyGlobalPlugin", OperationAuditRule.get().eventName("CopyGlobalPlugin").description("拷贝全局插件至目标网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.PluginId"));

        kindMap.put("CreateTrafficColorRule", OperationAuditRule.get().eventName("CreateTrafficColorRule").description("创建流量染色规则").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.TrafficColorName"));
        kindMap.put("UpdateTrafficColorRule", OperationAuditRule.get().eventName("UpdateTrafficColorRule").description("更新流量染色规则").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.TrafficColorName"));
        kindMap.put("DeleteTrafficColorRule", OperationAuditRule.get().eventName("DeleteTrafficColorRule").description("删除流量染色规则").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("TrafficColorRuleId"));
        kindMap.put("ModifyTrafficMarkRuleStatus", OperationAuditRule.get().eventName("ModifyTrafficMarkRuleStatus").description("开启流量染色规则").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("TrafficColorRuleId"));
        kindMap.put("UpdatePluginManager", OperationAuditRule.get().eventName("UpdatePluginManager").description("更新插件全局配置").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("Name"));
        /*********************************************** plugin End *****************************************************/

        /*********************************************** plugin template Start *****************************************************/
        kindMap.put("CreatePluginTemplate", OperationAuditRule.get().eventName("CreatePluginTemplate").description("创建私有模板").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.TemplateName"));
        kindMap.put("CreateGlobalPluginTemplate", OperationAuditRule.get().eventName("CreateGlobalPluginTemplate").description("创建公告模板").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("TemplateName"));
        kindMap.put("UpdatePluginTemplate", OperationAuditRule.get().eventName("UpdatePluginTemplate").description("更新模板配置").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSIReq("$.jsonPathForRSNReq"));
        kindMap.put("DeletePluginTemplate", OperationAuditRule.get().eventName("DeletePluginTemplate").description("删除模板").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("Id"));
        kindMap.put("SyncPluginTemplates", OperationAuditRule.get().eventName("SyncPluginTemplates").description("同步插件配置").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSNReq("$.Id"));

        /*********************************************** plugin template End *****************************************************/


        /*********************************************** plugin market Start *****************************************************/
        kindMap.put("PluginImport", OperationAuditRule.get().eventName("PluginImport").description("上传自定义插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("PluginUpdate", OperationAuditRule.get().eventName("PluginUpdate").description("修改自定义插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("UpdatePluginStatus", OperationAuditRule.get().eventName("UpdatePluginStatus").description("上下架插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).jsonPathForRSIReq("$.Id"));
        kindMap.put("DeletePlugin", OperationAuditRule.get().eventName("DeletePlugin").description("删除自定义插件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_PLUGIN).readRSIFromQuery("Id"));
        /*********************************************** plugin market end *****************************************************/

        /*********************************************** grpc Start *****************************************************/
        kindMap.put("SaveAndPublishedPbFile", OperationAuditRule.get().eventName("SaveAndPublishedPbFile").description("发布pb文件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GRPC).readRSIFromQuery("ServiceId"));
        kindMap.put("OfflinePbFile", OperationAuditRule.get().eventName("OfflinePbFile").description("下线pb文件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GRPC).readRSIFromQuery("ServiceId"));
        kindMap.put("PublicPbService", OperationAuditRule.get().eventName("PublicPbService").description("发布PB服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GRPC).readRSIFromQuery("PbServiceId"));
        kindMap.put("OfflinePbService", OperationAuditRule.get().eventName("OfflinePbService").description("下线PB服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GRPC).readRSIFromQuery("PbServiceId"));
        /*********************************************** grpc end *****************************************************/

        /*********************************************** dubbo Start *****************************************************/
        kindMap.put("PublishDubbo", OperationAuditRule.get().eventName("PublishDubbo").description("发布Dubbo协议转换").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DUBBO).jsonPathForRSNReq("$.interfaceName").jsonPathForRSIReq("$.ObjectId"));
        kindMap.put("OfflineDubbo", OperationAuditRule.get().eventName("OfflineDubbo").description("下线Dubbo协议转换").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DUBBO).readRSIFromQuery("ObjectId"));
        kindMap.put("RefreshDubboMeta", OperationAuditRule.get().eventName("RefreshDubboMeta").description("刷新Dubbo元数据").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_DUBBO).readRSNFromQuery("Igv"));
        /*********************************************** dubbo end *****************************************************/

        /*********************************************** webservice Start *****************************************************/
        kindMap.put("CheckUploadWsdlFile", OperationAuditRule.get().eventName("CheckUploadWsdlFile").description("上传wsdl文件").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_WEB_SERVICE).jsonPathForRSIReq("$.ServiceId"));
        kindMap.put("UpdateRouteWsParam", OperationAuditRule.get().eventName("UpdateRouteWsParam").description("更新webservice").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_WEB_SERVICE).jsonPathForRSIReq("$.ServiceId"));
        kindMap.put("DeleteRouteWsParam", OperationAuditRule.get().eventName("DeleteRouteWsParam").description("下线webservice").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_WEB_SERVICE).readRSIFromQuery("ServiceId"));
        /*********************************************** webservice end *****************************************************/


        /*********************************************** API Start *****************************************************/
        kindMap.put("CreateApi", OperationAuditRule.get().eventName("CreateApi").description("创建API").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSNReq("$.ApiName"));
        kindMap.put("UpdateApi", OperationAuditRule.get().eventName("UpdateApi").description("更新API").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSNReq("ApiName"));
        kindMap.put("DeleteApiById", OperationAuditRule.get().eventName("DeleteApiById").description("删除API").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("ApiId"));

        kindMap.put("CreateRequestBody", OperationAuditRule.get().eventName("CreateRequestBody").description("创建请求体").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("CreateQueryString", OperationAuditRule.get().eventName("CreateQueryString").description("创建请求参数").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("CreateStatusCode", OperationAuditRule.get().eventName("CreateStatusCode").description("创建响应码").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("CreateResponseBody", OperationAuditRule.get().eventName("CreateResponseBody").description("创建响应体").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("GenerateBodyByJson", OperationAuditRule.get().eventName("GenerateBodyByJson").description("json导入").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("DeleteBodyParamId", OperationAuditRule.get().eventName("DeleteBodyParamId").description("删除请求参数").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("$.ParamId"));

        kindMap.put("CreateRequestHeader", OperationAuditRule.get().eventName("CreateRequestHeader").description("创建请求头").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("CreateResponseHeader", OperationAuditRule.get().eventName("CreateResponseHeader").description("创建响应头").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSIReq("$.ApiId"));
        kindMap.put("DeleteHeaderByParamId", OperationAuditRule.get().eventName("DeleteHeaderByParamId").description("删除请求头").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("$.ParamId"));

        kindMap.put("CreateApiModel", OperationAuditRule.get().eventName("CreateApiModel").description("创建模型").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSNReq("$.ModelName"));
        kindMap.put("UpdateApiModel", OperationAuditRule.get().eventName("UpdateApiModel").description("更新模型").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).jsonPathForRSNReq("$.ModelName"));
        kindMap.put("DeleteApiModel", OperationAuditRule.get().eventName("DeleteApiModel").description("删除模型").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("$.ModelId"));

        kindMap.put("ConfirmImportByFile", OperationAuditRule.get().eventName("ConfirmImportByFile").description("导入swagger").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("$.Service-Id"));
        kindMap.put("ConfirmImportByLocation", OperationAuditRule.get().eventName("ConfirmImportByLocation").description("远程导入swagger").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_API).readRSIFromQuery("$.Service-Id"));

        /*********************************************** API end *****************************************************/

    }

    public static void main(String[] args) {
        List<AuditViewInfo> auditViewInfos = new ArrayList<>();

        kindMap.forEach((k, v) -> {
            AuditViewInfo auditViewInfo = new AuditViewInfo();
            auditViewInfo.setEventName(v.getEventName());
            auditViewInfo.setResourceType(v.getResourceType());
            auditViewInfo.setDescription(v.getDescription());
            auditViewInfos.add(auditViewInfo);
        });
        //基于resourceType分组
        Map<String, List<AuditViewInfo>> collect = auditViewInfos.stream().collect(Collectors.groupingBy(AuditViewInfo::getResourceType));
        /**
         * 输出结果，格式为
         * {resourceType}:
         *    {eventName}:{description}
         */
        collect.forEach((k, v) -> {
            System.out.println(k + ":");
            v.forEach(auditViewInfo -> {
                System.out.println("    " + auditViewInfo.getEventName() + ":" + auditViewInfo.getDescription());
            });
        });


    }

    public static Map<String, OperationAuditRule> getMap() {
        return kindMap;
    }

    public static OperationAuditRule get(String eventName) {
        return kindMap.get(eventName);
    }


}
