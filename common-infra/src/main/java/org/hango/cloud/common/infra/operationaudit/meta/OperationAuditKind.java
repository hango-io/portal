package org.hango.cloud.common.infra.operationaudit.meta;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 操作审计元数据存储类
 * @date 2022/4/6
 */
public class OperationAuditKind {

    protected static final Map<String, OperationAuditRule> kindMap = Maps.newHashMap();
    private static final String DEFAULT_RETURN = "$.Result";
    private static final String SERVICE_ID = "ServiceId";

    static {
        /*********************************************** Gateway Start *****************************************************/
        kindMap.put("CreateGateway", OperationAuditRule.get().eventName("CreateGwInfo").description("创建网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).jsonPathForRSNReq("$.GwName").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteGateway", OperationAuditRule.get().eventName("CreateGwInfo").description("删除网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).jsonPathForRSIReq("$.VirtualGwId").jsonPathForRSNReq("$.GwName"));
        kindMap.put("UpdateGateway", OperationAuditRule.get().eventName("UpdateGwInfo").description("更新网关").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_GATEWAY).readRSNFromQuery("VirtualGwId").jsonPathForRSNResp(DEFAULT_RETURN));
        /*********************************************** Gateway End *****************************************************/
        /*********************************************** Service Start *****************************************************/
        kindMap.put("CreateService", OperationAuditRule.get().eventName("CreateService").description("创建服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSNReq("$.Name").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("UpdateService", OperationAuditRule.get().eventName("UpdateService").description("编辑服务基本信息").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).jsonPathForRSIReq("$.Id").jsonPathForRSIReq("$.Name"));
        kindMap.put("DeleteService", OperationAuditRule.get().eventName("DeleteService").description("删除服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_SERVICE).readRSIFromQuery("Id").jsonPathForRSNResp(DEFAULT_RETURN));
        /*********************************************** Service End *****************************************************/
        /*********************************************** Registry Center Start *****************************************************/
        kindMap.put("SaveRegistryCenter", OperationAuditRule.get().eventName("SaveRegistryCenter").description("保存注册中心").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_REGISTRY_CENTER).jsonPathForRSNReq("$.RegistryAddr").jsonPathForRSIResp(DEFAULT_RETURN));
        kindMap.put("DeleteRegistryCenter", OperationAuditRule.get().eventName("DeleteRegistryCenter").description("删除注册中心").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_REGISTRY_CENTER).readRSNFromQuery("Id").jsonPathForRSNResp(DEFAULT_RETURN));
        /*********************************************** Registry Center End *****************************************************/
        /*********************************************** Service Proxy Start *****************************************************/
        kindMap.put("PublishService", OperationAuditRule.get().eventName("PublishService").description("发布服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_REGISTRY_CENTER).jsonPathForRSIReq("$.ServiceId"));
        kindMap.put("DeleteServiceProxy", OperationAuditRule.get().eventName("DeleteServiceProxy").description("下线服务").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_REGISTRY_CENTER).readRSIFromQuery(SERVICE_ID));
        kindMap.put("UpdatePublishService", OperationAuditRule.get().eventName("UpdatePublishService").description("更新服务发布信息").resourceType(AuditConst.AUDIT_RESOURCE_TYPE_REGISTRY_CENTER).jsonPathForRSIReq(SERVICE_ID));

        /*********************************************** Service Proxy End *****************************************************/


    }

    public static Map<String, OperationAuditRule> getMap() {
        return kindMap;
    }

    public static OperationAuditRule get(String eventName) {
        return kindMap.get(eventName);
    }


}
