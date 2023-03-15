package org.hango.cloud.envoy.infra.routeproxy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.dto.StringMatchDto;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.route.common.RouteRuleMapMatchDto;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.meta.ServiceType;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/1/12
 */
@Service
public class EnvoyRouteRuleProxyBuilderService {

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    public JSONObject buildRouteProxyInfo(RouteRuleProxyDto proxyDto, List<String> pluginConfigurations) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(proxyDto.getVirtualGwId());
        ServiceDto serviceDto = serviceInfoService.get(proxyDto.getServiceId());
        //构建基本信息
        JSONObject body = buildBaseInfo(proxyDto, pluginConfigurations, virtualGatewayDto);
        //构建header信息
        buildHeaderInfo(body, proxyDto);
        //构建服务实例
        buildDestinationServices(body, proxyDto.getDestinationServices(), proxyDto.getVirtualGwId());
        //构建流量镜像
        buildMirrorTraffic(body, proxyDto.getMirrorTraffic(), proxyDto.getMirrorSwitch(), proxyDto.getVirtualGwId());
        //构建dubbo meta信息
        body.put("MetaMap", processRouteMetadata(virtualGatewayDto, serviceDto, proxyDto));
        return body;
    }

    private JSONObject buildBaseInfo(RouteRuleProxyDto proxyDto, List<String> pluginConfigurations, VirtualGatewayDto virtualGatewayDto){
        JSONObject body = new JSONObject();
        body.put("Gateway", StringUtils.joinWith(BaseConst.SYMBOL_HYPHEN, virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode()));
        body.put("Code", proxyDto.getRouteRuleId());
        List<String> enableHosts = domainInfoService.getEnableHosts(proxyDto.getProjectId(), proxyDto.getVirtualGwId());
        body.put("Hosts", enableHosts);
        body.put("RequestUris", proxyDto.getUriMatchDto().getValue());
        body.put("UriMatch", proxyDto.getUriMatchDto().getType());
        body.put("Plugins", pluginConfigurations);
        body.put("Order", proxyDto.getOrders());
        body.put("ProjectId", proxyDto.getProjectId());
        //默认为*
        if (proxyDto.getMethodMatchDto() == null) {
            body.put("Methods", new JSONArray().fluentAddAll(Lists.newArrayList("*")));
        } else {
            body.put("Methods", proxyDto.getMethodMatchDto().getValue());
        }
        if (!CollectionUtils.isEmpty(proxyDto.getQueryParams())) {
            body.put("QueryParams", toApiPlaneStringMatchDto(proxyDto.getQueryParams()));
        }
        ServiceDto serviceDto = serviceInfoService.get(proxyDto.getServiceId());
        body.put("ServiceTag", serviceDto.getServiceName());
        body.put("RouteId", proxyDto.getRouteRuleId());
        body.put("RouteName", proxyDto.getRouteRuleName());
        if (proxyDto.getTimeout() > 0) {
            body.put("Timeout", proxyDto.getTimeout());
        }
        body.put("HttpRetry", proxyDto.getHttpRetryDto());
        return body;
    }

    private void buildHeaderInfo(JSONObject body, RouteRuleProxyDto proxyDto){
        List<RouteRuleMapMatchDto> headers = new ArrayList<>();
        // EnvoyRouteRuleDto 中的host实际上是key为 :authority 的header match
        if (null != proxyDto.getHostMatchDto()) {
            RouteRuleMapMatchDto hostHeader = new RouteRuleMapMatchDto();
            hostHeader.setKey(":authority");
            hostHeader.setType(proxyDto.getHostMatchDto().getType());
            hostHeader.setValue(proxyDto.getHostMatchDto().getValue());
            headers.add(hostHeader);
        }
        if (!CollectionUtils.isEmpty(proxyDto.getHeaders())) {
            headers.addAll(proxyDto.getHeaders());

        }
        body.put("Headers", toApiPlaneStringMatchDto(headers));
    }


    private void buildDestinationServices(JSONObject body, List<DestinationDto> destinationDtos, Long vgId){
        List<JSONObject> proxyServices = destinationDtos.stream().map(destinationInfo -> {
            JSONObject proxyService = new JSONObject();
            ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(vgId, destinationInfo.getServiceId());
            proxyService.put("Code", serviceProxyDto.getCode());
            proxyService.put("Weight", destinationInfo.getWeight());
            proxyService.put("Port", destinationInfo.getPort());

            //静态地址发布和注册中心方式发布，其端口对应为80
            if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
                proxyService.put("Port", 80);
            }
            proxyService.put("BackendService", envoyServiceProxyService.getBackendServiceSendToApiPlane(serviceProxyDto));
            proxyService.put("Type", serviceProxyDto.getPublishType());
            //增加版本信息
            if (StringUtils.isNotBlank(destinationInfo.getSubsetName())) {
                VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
                proxyService.put("Subset", ServiceProxyConvert.getName(destinationInfo.getSubsetName(),
                        serviceProxyDto.getServiceId(), virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode()));

            }
            return proxyService;
        }).collect(Collectors.toList());

        body.put("ProxyServices", new JSONArray().fluentAddAll(proxyServices));
    }

    private void buildMirrorTraffic(JSONObject body, DestinationDto mirrorTrafficDto, int mirrorSwitch, Long vgId){
        if (mirrorTrafficDto == null || mirrorSwitch == 0){
            return;
        }
        JSONObject mirrorTraffic = new JSONObject();
        ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(vgId, mirrorTrafficDto.getServiceId());
        mirrorTraffic.put("BackendService", envoyServiceProxyService.getBackendServiceSendToApiPlane(serviceProxyDto));
        mirrorTraffic.put("MirrorPercent", mirrorTrafficDto.getWeight());
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            mirrorTraffic.put("Port", 80);
        } else {
            mirrorTraffic.put("Port", mirrorTrafficDto.getPort());
        }
        if (StringUtils.isNotBlank(mirrorTrafficDto.getSubsetName())) {
            mirrorTraffic.put("Subset", mirrorTrafficDto.getSubsetName());
        }
        body.put("MirrorTraffic", mirrorTraffic);
    }


    public static List<StringMatchDto> toApiPlaneStringMatchDto(List<RouteRuleMapMatchDto> matchDtos) {
        if (CollectionUtils.isEmpty(matchDtos)) {
            return new ArrayList<>();
        }
        List<StringMatchDto> stringMatchDtos = new ArrayList<>();

        matchDtos.forEach(envoyRouteRuleMatchDto -> {
            List<String> values = envoyRouteRuleMatchDto.getValue();
            //多个value以|进行分割
            String value;
            String type = envoyRouteRuleMatchDto.getType();
            if (values.size() == 1) {
                value = values.get(0);
            } else {
                if (BaseConst.URI_TYPE_EXACT.equals(envoyRouteRuleMatchDto.getType())) {
                    values = values.stream().map(EnvoyRouteRuleProxyBuilderService::escapeExprSpecialWord).collect(Collectors.toList());
                }
                if (BaseConst.URI_TYPE_PREFIX.equals(envoyRouteRuleMatchDto.getType())) {
                    values = values.stream().map(EnvoyRouteRuleProxyBuilderService::prefixStringGenerate).collect(Collectors.toList());
                }
                value = String.join("|", values);
                type = BaseConst.URI_TYPE_REGEX;
            }

            StringMatchDto stringMatchDto = new StringMatchDto();
            stringMatchDto.setKey(envoyRouteRuleMatchDto.getKey());
            stringMatchDto.setType(type);
            stringMatchDto.setValue(value);
            stringMatchDtos.add(stringMatchDto);
        });
        return stringMatchDtos;
    }

    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    public static String prefixStringGenerate(String keyword) {
        return escapeExprSpecialWord(keyword) + ".*";
    }


    /**
     * 添加路由 metadata 数据
     *
     */
    private Map<String, String> processRouteMetadata(VirtualGatewayDto virtualGatewayDto, ServiceDto serviceDto, RouteRuleProxyDto routeRuleProxyDto) {
        Map<String, String> metaMap = routeRuleProxyDto.getMetaMap() == null ? Maps.newHashMap() : routeRuleProxyDto.getMetaMap();
        //处理路由指标Meta数据
        processRouteStatsMeta(virtualGatewayDto, serviceDto, routeRuleProxyDto, metaMap);
        //处理路由Metadata数据
        processRouteMetaDataField(virtualGatewayDto, serviceDto, routeRuleProxyDto, metaMap);
        //处理Dubbo Meta相关的数据
        processDubboMeta(virtualGatewayDto, serviceDto, routeRuleProxyDto, metaMap);
        return metaMap;
    }


    /**
     * 添加路由指标Metadata数据
     *
     */
    private void processRouteMetaDataField(VirtualGatewayDto virtualGatewayDto, ServiceDto serviceDto, RouteRuleProxyDto routeRuleProxyInfo, Map<String, String> metaMap) {
        Map<String, Object> stats = Maps.newHashMap();
        stats.put("qz_cluster_name", virtualGatewayDto.getGwClusterName());
        stats.put("qz_virtual_gateway", virtualGatewayDto.getCode());
        stats.put("qz_svc_id",serviceDto.getServiceName());
        stats.put("qz_api_id",routeRuleProxyInfo.getRouteRuleId());
        stats.put("qz_api_name",routeRuleProxyInfo.getRouteRuleName());
        stats.put("qz_project_id",routeRuleProxyInfo.getProjectId());
        metaMap.put("MetadataHub", JSON.toJSONString(stats));
    }


    /**
     * 添加路由指标Meta数据
     *
     */
    private void processRouteStatsMeta(VirtualGatewayDto virtualGatewayDto, ServiceDto serviceDto,RouteRuleProxyDto routeRuleProxyInfo, Map<String, String> metaMap) {
        Map<String, Object> stats = Maps.newHashMap();
        stats.put("route_rule_id", routeRuleProxyInfo.getRouteRuleId());
        stats.put("route_rule_path", String.join("|", routeRuleProxyInfo.getUriMatchDto().getValue()));
        stats.put("virtual_gateway_code",virtualGatewayDto.getCode());
        //此处用服务标识，一方面与服务告警模板保持一致，另一方面，服务标识不存在修改的情况。
        stats.put("service_name",serviceDto.getServiceName());
        stats.put("project_id",serviceDto.getProjectId());
        metaMap.put("StatsMeta", JSON.toJSONString(stats));
    }


    /**
     * 添加Dubbo Meta相关的数据
     *
     */
    private void processDubboMeta(VirtualGatewayDto virtualGatewayDto, ServiceDto serviceDto, RouteRuleProxyDto routeRuleProxyDto, Map<String, String> metaMap) {
        //如果已存在就不进行复写
        //适用于Dubbo在创建、更新、删除的场景
        //@see DubboServiceImpl publishToEnvoy
        String dubboMeta = "DubboMeta";
        if (metaMap.containsKey(dubboMeta)) {
            return;
        }
        if (!ServiceType.dubbo.name().equals(serviceDto.getServiceType())) {
            return;
        }
        if (routeRuleProxyDto.getId() == null) {
            // 路由发布流程中，已发布路由对象未创建从而id为空，无需查询dubbo元数据
            return;
        }
        DubboBindingDto bindingDto = dubboBindingService.getByIdAndType(routeRuleProxyDto.getId(), BaseConst.ROUTE);
        if (bindingDto == null) {
            return;
        }
        dubboBindingService.parseDefaultValue(bindingDto);
        metaMap.put(dubboMeta, JSON.toJSONString(bindingDto));
    }
}
