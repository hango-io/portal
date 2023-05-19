package org.hango.cloud.envoy.infra.route.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.convert.RouteRuleConvert;
import org.hango.cloud.common.infra.base.dto.StringMatchDto;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.RouteMapMatchDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceType;
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
public class EnvoyRouteBuilderService {

    @Autowired
    private IDubboBindingService dubboBindingService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    public JSONObject buildRouteInfo(RouteDto routeDto, List<String> pluginConfigurations) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeDto.getVirtualGwId());
        List<ServiceProxyDto> serviceProxyDtoList = serviceProxyService.getServiceByIds(routeDto.getServiceIds());
        //构建基本信息
        JSONObject body = buildBaseInfo(routeDto, pluginConfigurations, virtualGatewayDto);
        //构建header信息
        buildHeaderInfo(body, routeDto);
        //构建服务实例
        List<DestinationDto> destinationDtoList = routeService.genDestinationInfoFromRouteServiceMeta(routeDto).stream()
                .map(RouteRuleConvert::toView)
                .collect(Collectors.toList());
        buildDestinationServices(body, destinationDtoList);
        //构建流量镜像
        buildMirrorTraffic(body, routeDto.getMirrorTraffic(), routeDto.getMirrorSwitch());
        //构建dubbo meta信息
        body.put("MetaMap", processRouteMetadata(virtualGatewayDto, serviceProxyDtoList, routeDto));
        return body;
    }

    private JSONObject buildBaseInfo(RouteDto routeDto, List<String> pluginConfigurations, VirtualGatewayDto virtualGatewayDto) {
        JSONObject body = new JSONObject();
        body.put("Gateway", CommonUtil.genGatewayStrForRoute(virtualGatewayDto));
        body.put("Code", routeDto.getName());
        List<String> hosts = routeDto.getHosts();
        if (CollectionUtils.isEmpty(hosts)){
            hosts = new ArrayList<>(serviceProxyService.getUniqueHostListFromServiceIdList(routeDto.getServiceIds()));
        }
        body.put("Hosts", hosts);
        body.put("RequestUris", routeDto.getUriMatchDto().getValue());
        body.put("UriMatch", routeDto.getUriMatchDto().getType());
        body.put("Plugins", pluginConfigurations);
        body.put("Order", routeDto.getOrders());
        body.put("ProjectId", routeDto.getProjectId());
        body.put("Methods", routeDto.getMethod());
        if (!CollectionUtils.isEmpty(routeDto.getQueryParams())) {
            body.put("QueryParams", toApiPlaneStringMatchDto(routeDto.getQueryParams()));
        }
        body.put("RouteId", routeDto.getId());
        body.put("RouteName", routeDto.getName());
        if (routeDto.getTimeout() > 0) {
            body.put("Timeout", routeDto.getTimeout());
        }
        body.put("HttpRetry", routeDto.getHttpRetryDto());
        return body;
    }

    private void buildHeaderInfo(JSONObject body, RouteDto routeDto) {
        List<RouteMapMatchDto> headers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(routeDto.getHeaders())) {
            headers.addAll(routeDto.getHeaders());

        }
        body.put("Headers", toApiPlaneStringMatchDto(headers));
    }


    private void buildDestinationServices(JSONObject body, List<DestinationDto> destinationDtos) {
        List<JSONObject> proxyServices = destinationDtos.stream().map(destinationInfo -> {
            JSONObject proxyService = new JSONObject();
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(destinationInfo.getServiceId());
            proxyService.put("Code", ServiceProxyConvert.getCode(serviceProxyDto));
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
                proxyService.put("Subset", ServiceProxyConvert.getSubSetName(destinationInfo.getSubsetName(), serviceProxyDto));
            }
            return proxyService;
        }).collect(Collectors.toList());

        body.put("ProxyServices", new JSONArray().fluentAddAll(proxyServices));
    }

    private void buildMirrorTraffic(JSONObject body, DestinationDto mirrorTrafficDto, int mirrorSwitch) {
        if (mirrorTrafficDto == null || mirrorSwitch == 0) {
            return;
        }
        JSONObject mirrorTraffic = new JSONObject();
        ServiceProxyDto serviceProxyDto = serviceProxyService.get(mirrorTrafficDto.getServiceId());
        String backendService;
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            // 静态服务需额外处理，BackendService不为Endpoint IP
            // 案例（ServiceEntry名称）：com.netease.static-3-cuicuisha
            backendService = "com.netease.static-" + ProjectTraceHolder.getProId() + "-" + serviceProxyDto.getName();
        } else {
            backendService = envoyServiceProxyService.getBackendServiceSendToApiPlane(serviceProxyDto);
        }
        mirrorTraffic.put("BackendService", backendService);
        mirrorTraffic.put("MirrorPercent", mirrorTrafficDto.getWeight());
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            mirrorTraffic.put("Port", 80);
        } else {
            mirrorTraffic.put("Port", mirrorTrafficDto.getPort());
        }
        if (StringUtils.isNotBlank(mirrorTrafficDto.getSubsetName())) {
            mirrorTraffic.put("Subset", ServiceProxyConvert.getSubSetName(mirrorTrafficDto.getSubsetName(), serviceProxyDto));
        }
        body.put("MirrorTraffic", mirrorTraffic);
    }


    public static List<StringMatchDto> toApiPlaneStringMatchDto(List<RouteMapMatchDto> matchDtos) {
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
                    values = values.stream().map(EnvoyRouteBuilderService::escapeExprSpecialWord).collect(Collectors.toList());
                }
                if (BaseConst.URI_TYPE_PREFIX.equals(envoyRouteRuleMatchDto.getType())) {
                    values = values.stream().map(EnvoyRouteBuilderService::prefixStringGenerate).collect(Collectors.toList());
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
     */
    private Map<String, String> processRouteMetadata(VirtualGatewayDto virtualGatewayDto,
                                                     List<ServiceProxyDto> serviceDtoList,
                                                     RouteDto routeDto) {
        Map<String, String> metaMap = routeDto.getMetaMap() == null ? Maps.newHashMap() : routeDto.getMetaMap();
        //处理路由指标Meta数据
        processRouteStatsMeta(virtualGatewayDto, routeDto, metaMap);
        //处理路由Metadata数据
        processRouteMetaDataField(virtualGatewayDto, routeDto, metaMap);
        //处理Dubbo Meta相关的数据
        processDubboMeta(serviceDtoList, routeDto, metaMap);
        return metaMap;
    }


    /**
     * 添加路由指标Metadata数据
     */
    private void processRouteMetaDataField(VirtualGatewayDto virtualGatewayDto, RouteDto routeDto, Map<String, String> metaMap) {
        Map<String, Object> stats = Maps.newHashMap();
        stats.put("qz_cluster_name", virtualGatewayDto.getGwClusterName());
        stats.put("qz_virtual_gateway", virtualGatewayDto.getCode());
        stats.put("qz_api_id", routeDto.getName());
        stats.put("qz_api_name", routeDto.getName());
        stats.put("qz_project_id", routeDto.getProjectId());
        metaMap.put("MetadataHub", JSON.toJSONString(stats));
    }


    /**
     * 添加路由指标Meta数据
     */
    private void processRouteStatsMeta(VirtualGatewayDto virtualGatewayDto, RouteDto routeDto, Map<String, String> metaMap) {
        Map<String, Object> stats = Maps.newHashMap();
        stats.put("route_rule_id", routeDto.getName());
        stats.put("route_rule_path", String.join("|", routeDto.getUriMatchDto().getValue()));
        stats.put("virtual_gateway_code", CommonUtil.genGatewayStrForRoute(virtualGatewayDto));
        //此处用服务标识，一方面与服务告警模板保持一致，另一方面，服务标识不存在修改的情况。
        stats.put("project_id", routeDto.getProjectId());
        metaMap.put("StatsMeta", JSON.toJSONString(stats));
    }


    /**
     * 添加Dubbo Meta相关的数据
     */
    private void processDubboMeta(List<ServiceProxyDto> serviceDtoList, RouteDto routeDto, Map<String, String> metaMap) {
        //如果已存在就不进行复写
        //适用于Dubbo在创建、更新、删除的场景
        //@see DubboServiceImpl publishToEnvoy
        String dubboMeta = "DubboMeta";
        if (metaMap.containsKey(dubboMeta)) {
            return;
        }
        if (CollectionUtils.isEmpty(serviceDtoList)) {
            return;
        }
        for (ServiceProxyDto serviceProxyDto : serviceDtoList) {
            if (!ServiceType.dubbo.name().equals(serviceProxyDto.getProtocol())) {
                return;
            }
        }

        if (routeDto.getId() == null) {
            // 路由发布流程中，已发布路由对象未创建从而id为空，无需查询dubbo元数据
            return;
        }
        DubboBindingDto bindingDto = dubboBindingService.getByIdAndType(routeDto.getId(), ApiConst.ROUTE);
        if (bindingDto == null) {
            return;
        }
        dubboBindingService.parseDefaultValue(bindingDto);
        metaMap.put(dubboMeta, JSON.toJSONString(bindingDto));
    }
}
