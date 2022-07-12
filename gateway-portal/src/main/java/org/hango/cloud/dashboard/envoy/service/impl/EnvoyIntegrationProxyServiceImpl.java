package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.netease.cloud.nsf.analyser.StepAnalyser;
import com.netease.cloud.nsf.analyser.StepAnalyserImpl;
import com.netease.cloud.nsf.config.ConfigStore;
import com.netease.cloud.nsf.step.Step;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.service.impl.RouteRuleInfoServiceImpl;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationProxyDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationProxyService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteStringMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@ConditionalOnProperty("enableNsb")
public class EnvoyIntegrationProxyServiceImpl implements IEnvoyIntegrationProxyService {

    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoServiceImpl.class);

    @Autowired
    private IEnvoyIntegrationService envoyIntegrationService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;
    @Autowired
    private IEnvoyIntegrationProxyDao envoyIntegrationProxyDao;
    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;
    @Autowired
    private ConfigStore configStore;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ErrorCode checkPublishParam(long integrationId, long gwId) {
        EnvoyIntegrationInfo integrationDbInfo = envoyIntegrationService.getIntegrationInfoById(integrationId);
        if (integrationDbInfo == null) {
            logger.info("发布集成，未找到对应ID的集成，无法发布");
            return CommonErrorCode.NoSuchIntegration;
        }
        if (integrationDbInfo.getPublishStatus() == 1) {
            logger.info("发布集成，集成已经发布");
            return CommonErrorCode.IntegrationAlreadyPublished;
        }
        if (integrationDbInfo.getStep() == null) {
            logger.info("集成step不存在，不能发布");
            return CommonErrorCode.MissingParameter("Step");
        }

        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("发布集成，未找到对应ID的网关，无法发布");
            return CommonErrorCode.NoSuchGateway;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode publishIntegration(long integrationId, long gwId) {
        EnvoyIntegrationInfo integrationDbInfo = envoyIntegrationService.getIntegrationInfoById(integrationId);
        integrationDbInfo.setPublishTime(System.currentTimeMillis());
        integrationDbInfo.setPublishStatus(1);
        //生成集成Proxy
        EnvoyIntegrationProxyInfo envoyIntegrationProxyInfo = new EnvoyIntegrationProxyInfo();
        envoyIntegrationProxyInfo.setIntegrationId(integrationId);
        envoyIntegrationProxyInfo.setGwId(gwId);
        envoyIntegrationProxyInfo.setMetadata(new HashMap<>(Const.DEFAULT_MAP_SIZE));

        //查询触发器类型
        Step step = null;
        try {
            step = objectMapper.readValue(integrationDbInfo.getStep(), Step.class);
        } catch (IOException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        StepAnalyser analyser = new StepAnalyserImpl(step);
        List<Step> steps = analyser.getByKind("openApi");
        if (!steps.isEmpty()) {
            if (!automaticallyPublishServicesAndRoutes(integrationDbInfo, envoyIntegrationProxyInfo, steps.get(0), gwId)) {
                automaticOfflineServiceAndRoutes(envoyIntegrationProxyInfo);
                return CommonErrorCode.FailedToPublishServiceOrRoute;
            }
        }

        try {
            configStore.publish(String.valueOf(integrationId), step);
        } catch (Exception e) {
            logger.error("下发camel配置出现异常，Exception:", e);
            if (!steps.isEmpty()) {
                logger.info("自动回滚发布的服务和路由");
                automaticOfflineServiceAndRoutes(envoyIntegrationProxyInfo);
            }
            return CommonErrorCode.FailedToPublishIntegration;
        }
        //更新集成和集成proxy
        envoyIntegrationService.updateAll(integrationDbInfo);
        envoyIntegrationProxyInfo.setMetadataStr(JSON.toJSONString(envoyIntegrationProxyInfo.getMetadata()));
        envoyIntegrationProxyDao.add(envoyIntegrationProxyInfo);
        return CommonErrorCode.Success;
    }

    @Override
    public boolean automaticallyPublishServicesAndRoutes(EnvoyIntegrationInfo integrationInfo, EnvoyIntegrationProxyInfo integrationProxyInfo, Step step, long gwId) {
        //建立service
        ServiceInfoDto serviceInfoDto = new ServiceInfoDto();
        String name = "Integration_" + integrationInfo.getIntegrationName();
        serviceInfoDto.setDisplayName(name);
        serviceInfoDto.setServiceName("Integration_" + integrationInfo.getId());
        serviceInfoDto.setServiceType("http");
        serviceInfoDto.setDescription(name + "，集成下线时会自动删除");

        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, serviceInfoDto.getId(), serviceInfoDto.getDisplayName());
        AuditResourceHolder.set(resource);
        logger.info("创建服务，serviceInfo:{}", serviceInfoDto);
        ServiceInfo serviceInfo = serviceInfoService.addServiceInfo(serviceInfoDto, ProjectTraceHolder.getProId());
        if (serviceInfo.getId() == -1) {
            return false;
        }
        integrationProxyInfo.getMetadata().put("serviceId", serviceInfo.getId());

        //建立ServiceProxy
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setServiceId(serviceInfo.getId());
        serviceProxyDto.setPublishType("STATIC");
        serviceProxyDto.setGwId(gwId);
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.info("发布集成，未找到对应ID的网关，无法发布");
            return false;
        }
        serviceProxyDto.setBackendService(gatewayInfo.getCamelAddr());

        //操作审计记录资源名称
        resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_SERVICE, serviceProxyDto.getServiceId(), null);
        AuditResourceHolder.set(resource);
        logger.info("发布服务至envoy网关，服务发布信息envoyServiceProxyDto:{}", serviceProxyDto);
        long serviceProxyId = serviceProxyService.publishServiceToGw(serviceProxyDto);
        if (serviceProxyId == -1) {
            return false;
        }
        integrationProxyInfo.getMetadata().put("serviceProxyId", serviceProxyId);

        //配置健康检查
        EnvoyHealthCheckRuleDto checkRuleDto = new EnvoyHealthCheckRuleDto();
        checkRuleDto.setServiceId(serviceInfo.getId());
        checkRuleDto.setGwId(gwId);
        checkRuleDto.setActiveSwitch(1);
        checkRuleDto.setPath("/healthCheck");
        checkRuleDto.setTimeout(30000);
        List<Integer> status = new ArrayList<>();
        status.add(200);
        status.add(201);
        checkRuleDto.setExpectedStatuses(status);
        checkRuleDto.setHealthyInterval(3000);
        checkRuleDto.setHealthyThreshold(3);
        checkRuleDto.setUnhealthyInterval(1000);
        checkRuleDto.setUnhealthyThreshold(3);
        if (!CommonErrorCode.Success.getCode().equals(envoyHealthCheckService.updateHealthCheckRuleParam(EnvoyHealthCheckRuleDto.dtoToMeta(checkRuleDto)).getCode())) {
            return false;
        }

        //建立route
        RouteRuleDto routeRuleDto = new RouteRuleDto();
        routeRuleDto.setServiceId(serviceInfo.getId());
        routeRuleDto.setRouteRuleName(name);
        routeRuleDto.setDescription(name + "，集成下线时会自动删除");
        //配置uri和method
        EnvoyRouteStringMatchDto uriMatchDto = new EnvoyRouteStringMatchDto();
        uriMatchDto.setType("exact");
        uriMatchDto.setValue(new ArrayList<>());
        uriMatchDto.getValue().add((String) step.getProperty().getProperty().get("uri"));
        EnvoyRouteStringMatchDto methodMatchDto = new EnvoyRouteStringMatchDto();
        methodMatchDto.setType("exact");
        methodMatchDto.setValue(new ArrayList<>());
        methodMatchDto.getValue().add((String) step.getProperty().getProperty().get("method"));
        routeRuleDto.setUriMatchDto(uriMatchDto);
        routeRuleDto.setMethodMatchDto(methodMatchDto);

        RouteRuleInfo routeRuleInfo = routeRuleDto.toMeta();
        routeRuleInfo.setProjectId(ProjectTraceHolder.getProId());
        logger.info("创建路由规则，routeRuleInfo:{}", routeRuleInfo);
        //操作审计记录资源名称
        resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, null, routeRuleInfo.getRouteRuleName());
        AuditResourceHolder.set(resource);
        //配置审计,创建路由规则
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(routeRuleInfo))));
        long routeRuleId = envoyRouteRuleInfoService.addRouteRule(routeRuleInfo);
        if (routeRuleId == -1) {
            serviceProxyService.deleteServiceProxy(gwId, serviceProxyId);
            return false;
        }
        integrationProxyInfo.getMetadata().put("routeId", routeRuleId);

        //发布route
        RouteRuleProxyDto routeRuleProxyDto = new RouteRuleProxyDto();
        routeRuleProxyDto.setRouteRuleId(routeRuleId);
        routeRuleProxyDto.setGwId(gwId);
        routeRuleProxyDto.setServiceId(serviceInfo.getId());
        routeRuleProxyDto.setDestinationServices(new ArrayList<>());
        EnvoyDestinationDto envoyDestinationDto = new EnvoyDestinationDto();
        envoyDestinationDto.setServiceId(serviceInfo.getId());
        envoyDestinationDto.setWeight(100);
        envoyDestinationDto.setPort(80);
        routeRuleProxyDto.getDestinationServices().add(envoyDestinationDto);

        logger.info("发布路由规则, routeRuleProxyDto:{}", routeRuleProxyDto);
        //操作审计记录资源名称
        resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, routeRuleProxyDto.getRouteRuleId(), routeRuleInfo.getRouteRuleName());
        AuditResourceHolder.set(resource);
        long routeProxyId = routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRuleProxyDto), Lists.newArrayList(), true);
        if (routeProxyId == -1) {
            serviceProxyService.deleteServiceProxy(gwId, serviceProxyId);
            return false;
        }
        integrationProxyInfo.getMetadata().put("routeProxyId", routeProxyId);
        return true;
    }

    @Override
    public ErrorCode checkDeleteParam(long integrationId) {
        EnvoyIntegrationInfo integrationInfo = envoyIntegrationService.getIntegrationInfoById(integrationId);
        if (integrationInfo == null) {
            logger.info("下线集成，集成不存在，无法下线");
            return CommonErrorCode.NoSuchIntegration;
        }
        if (integrationInfo.getPublishStatus() == 0) {
            logger.info("下线集成，集成未发布，无法下线");
            return CommonErrorCode.IntegrationNotPublished;
        }
        return CommonErrorCode.Success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErrorCode offlineIntegration(long integrationId) {
        EnvoyIntegrationInfo integrationInfo = envoyIntegrationService.getIntegrationInfoById(integrationId);
        EnvoyIntegrationProxyInfo integrationProxy = envoyIntegrationProxyDao.getByIntegrationId(integrationId);

        //查询触发器类型
        Step step = null;
        try {
            step = objectMapper.readValue(integrationInfo.getStep(), Step.class);
        } catch (IOException e) {
            logger.error("Exception:", e);
        }
        StepAnalyser analyser = new StepAnalyserImpl(step);
        List<Step> steps = analyser.getByKind("openApi");
        if (!steps.isEmpty()) {
            if (!automaticOfflineServiceAndRoutes(integrationProxy)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return CommonErrorCode.FailedToOfflineServiceOrRoute;
            }
        }

        configStore.delete(String.valueOf(integrationId));
        //修改集成和删除集成proxy
        envoyIntegrationProxyDao.delete(integrationProxy);
        integrationInfo.setPublishStatus(0);
        integrationInfo.setPublishTime(0);
        envoyIntegrationService.updateAll(integrationInfo);
        return CommonErrorCode.Success;
    }

    @Override
    public boolean automaticOfflineServiceAndRoutes(EnvoyIntegrationProxyInfo integrationProxy) {
        //下线路由
        ResourceDataDto resource = null;
        if (integrationProxy.getMetadata().containsKey("routeProxyId")) {
            logger.info("根据网关id gwId:{},路由规则id:{}下线路由规则,下线serviceId:{}", integrationProxy.getGwId(), integrationProxy.getMetadata().get("routeId"),
                    integrationProxy.getMetadata().get("serviceProxyId"));
            resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, integrationProxy.getMetadata().get("routeId"), null);
            AuditResourceHolder.set(resource);
            if (!routeRuleProxyService.deleteRouteRuleProxy(integrationProxy.getGwId(), Long.parseLong(String.valueOf(integrationProxy.getMetadata().get("routeId"))))) {
                return false;
            }
        }

        if (integrationProxy.getMetadata().containsKey("routeId")) {
            //删除路由
            resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, integrationProxy.getMetadata().get("routeId"), null);
            AuditResourceHolder.set(resource);
            logger.info("根据路由规则id:{}，删除路由规则", integrationProxy.getMetadata().get("routeId"));
            if (!envoyRouteRuleInfoService.deleteRouteRule(Long.parseLong(String.valueOf(integrationProxy.getMetadata().get("routeId"))))) {
                return false;
            }
        }

        if (integrationProxy.getMetadata().containsKey("serviceProxyId")) {
            //下线服务
            logger.info("下线已经关联的服务，gwId:{},serviceId:{}", new Object[]{integrationProxy.getGwId(), integrationProxy.getMetadata().get("serviceProxyId")});
            //操作审计记录资源名称
            resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ENVOY_SERVICE, integrationProxy.getMetadata().get("serviceProxyId"), null);
            AuditResourceHolder.set(resource);
            if (!serviceProxyService.deleteServiceProxy(integrationProxy.getGwId(), Long.parseLong(String.valueOf(integrationProxy.getMetadata().get("serviceId"))))) {
                return false;
            }
        }

        if (integrationProxy.getMetadata().containsKey("serviceId")) {
            //删除服务
            resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_SERVICE, integrationProxy.getMetadata().get("serviceId"), null);
            AuditResourceHolder.set(resource);
            logger.info("删除serviceId：{}下的服务", integrationProxy.getMetadata().get("serviceId"));
            serviceInfoService.delete(Long.parseLong(String.valueOf(integrationProxy.getMetadata().get("serviceId"))));
        }
        return true;
    }
}
