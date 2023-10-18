package org.hango.cloud.envoy.infra.serviceregistry.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceType;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.serviceregistry.service.IEnvoyServiceRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2022/11/14
 */
@Slf4j
@Service
public class EnvoyServiceRegistryServiceImpl implements IEnvoyServiceRegistryService {


    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService iVirtualGatewayInfoService;

    private static final Map<String, Set<RegistryCenterEnum>> SERVICE_TYPE_2_REGISTER_TYPES_MAP = Maps.newHashMap();

    static {
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.dubbo.name(), Sets.newHashSet(RegistryCenterEnum.Zookeeper));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.webservice.name(), Sets.newHashSet(RegistryCenterEnum.Kubernetes));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.tcp.name(), Sets.newHashSet(RegistryCenterEnum.Kubernetes));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.udp.name(), Sets.newHashSet(RegistryCenterEnum.Kubernetes));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.http.name(), Sets.newHashSet(RegistryCenterEnum.Nacos, RegistryCenterEnum.Kubernetes, RegistryCenterEnum.Eureka));
    }


    @Override
    public List<String> getRegistryTypeList(long virtualGwId, String serviceType) {
        List<String> registryList = getRegistry(virtualGwId);
        if (CollectionUtils.isEmpty(registryList)){
            return new ArrayList<>();
        }
        return filterAndSort(serviceType, registryList);
    }

    private static List<String> filterAndSort(String serviceType, List<String> registryList) {
        Set<RegistryCenterEnum> registryTypeSet = registryList.stream()
                .map(RegistryCenterEnum::get)
                .collect(Collectors.toSet());

        return registryTypeSet.stream()
                .filter(registryType -> SERVICE_TYPE_2_REGISTER_TYPES_MAP.containsKey(serviceType)
                        && SERVICE_TYPE_2_REGISTER_TYPES_MAP.get(serviceType).contains(registryType))
                .sorted(Comparator.comparingInt(RegistryCenterEnum::getOrder))
                .map(RegistryCenterEnum::getType)
                .collect(Collectors.toList());
    }


    private List<String> getRegistry(long virtualGwId){
        VirtualGatewayDto virtualGatewayDto = iVirtualGatewayInfoService.get(virtualGwId);
        if(StringUtils.equalsAnyIgnoreCase(virtualGatewayDto.getProtocol(), BaseConst.SCHEME_TCP, BaseConst.SCHEME_UDP)){
            return Lists.newArrayList(RegistryCenterEnum.Kubernetes.getType());
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        Map<String, Object> params = Maps.newHashMap();
        params.put("Action", "GetRegistryList");
        params.put("Version", "2019-07-25");
        HttpClientResponse response = HttpClientUtil.getRequest(gatewayDto.getConfAddr() + "/api", params, EnvoyConst.MODULE_API_PLANE);
        if (!HttpClientUtil.isNormalCode(response.getStatusCode())) {
            log.error("调用api-plane查询注册中心列表，返回http status code非2xx, httpStatusCode:{}, errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return new ArrayList<>();
        }
        JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
        JSONArray registryArray = jsonResult.getJSONArray("Result");
        return JSONObject.parseArray(registryArray.toJSONString(), String.class);
    }
}
