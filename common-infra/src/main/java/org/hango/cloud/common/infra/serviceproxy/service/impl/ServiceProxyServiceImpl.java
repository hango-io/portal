package org.hango.cloud.common.infra.serviceproxy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceConnectionPoolDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceConsistentHashDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceLoadBalancerDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 网关发布服务相关，发布服务至指定网关，即将服务与数据面相关数据进行关联
 *
 * @author hanjiahao
 */
@Service
public class ServiceProxyServiceImpl implements IServiceProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyServiceImpl.class);

    @Autowired
    private IServiceProxyDao serviceProxyDao;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IServiceProxyService serviceProxyService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public long create(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        ServiceDto serviceDto = serviceInfoService.get(serviceProxyDto.getServiceId());
        if (serviceDto == null) {
            logger.error("发布服务至网关存在脏数据，服务不存在，serviceProxyDto ={}", JSON.toJSONString(serviceProxyInfo));
            return BaseConst.ERROR_RESULT;
        }
        serviceProxyInfo.setProjectId(serviceDto.getProjectId());
        serviceProxyInfo.setCreateTime(System.currentTimeMillis());
        serviceProxyInfo.setUpdateTime(System.currentTimeMillis());
        long publishServiceId = serviceProxyDao.add(serviceProxyInfo);
        if (publishServiceId > NumberUtils.INTEGER_ZERO && serviceDto.getStatus() == NumberUtils.INTEGER_ZERO) {
            //更新发布状态,未发布更新为已发布
            serviceDto.setStatus(NumberUtils.INTEGER_ONE);
            serviceInfoService.update(serviceDto);

        }
        serviceProxyDto.setId(publishServiceId);
        return publishServiceId;
    }


    @Override
    public long update(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        serviceProxyInfo.setUpdateTime(System.currentTimeMillis());
        return serviceProxyDao.update(serviceProxyInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        serviceProxyDao.delete(serviceProxyInfo);
        //修改服务发布状态为未发布
        long serviceId = serviceProxyDto.getServiceId();
        if (CollectionUtils.isEmpty(getServiceProxyByServiceId(serviceId))) {
            ServiceDto serviceDto = serviceInfoService.get(serviceId);
            if (serviceDto != null) {
                serviceDto.setStatus(NumberUtils.INTEGER_ZERO);
                serviceInfoService.update(serviceDto);
            }
        }
    }

    @Override
    public List<? extends ServiceProxyDto> findAll() {
        return serviceProxyDao.findAll().stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<? extends ServiceProxyDto> findAll(long offset, long limit) {
        return serviceProxyDao.getRecordsByField(Collections.emptyMap()).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return serviceProxyDao.getCountByFields(Collections.emptyMap());
    }

    @Override
    public ServiceProxyDto get(long id) {
        return toView(serviceProxyDao.get(id));
    }

    @Override
    public ServiceProxyDto toView(ServiceProxyInfo serviceProxyInfo) {
        if(serviceProxyInfo == null){
            return null;
        }
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setId(serviceProxyInfo.getId());
        serviceProxyDto.setServiceId(serviceProxyInfo.getServiceId());
        serviceProxyDto.setCode(serviceProxyInfo.getCode());
        serviceProxyDto.setBackendService(serviceProxyInfo.getBackendService());
        serviceProxyDto.setRegistryCenterType(serviceProxyInfo.getRegistryCenterType());
        serviceProxyDto.setPublishProtocol(serviceProxyInfo.getPublishProtocol());
        serviceProxyDto.setPublishType(serviceProxyInfo.getPublishType());
        serviceProxyDto.setVirtualGwId(serviceProxyInfo.getVirtualGwId());
        serviceProxyDto.setCreateTime(serviceProxyInfo.getCreateTime());
        serviceProxyDto.setUpdateTime(serviceProxyInfo.getUpdateTime());
        serviceProxyDto.setGwType(serviceProxyInfo.getGwType());
        serviceProxyDto.setLoadBalancer(serviceProxyInfo.getLoadBalancer());
        serviceProxyDto.setSubsets(setSubsetForDto(serviceProxyInfo));
        serviceProxyDto.setProjectId(serviceProxyInfo.getProjectId());
        serviceProxyDto.setTrafficPolicy(setTrafficPolicyForDto(serviceProxyInfo));

        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyInfo.getVirtualGwId());
        if (virtualGatewayDto != null) {
            serviceProxyDto.setGwClusterName(virtualGatewayDto.getGwClusterName());
            serviceProxyDto.setVirtualGwName(virtualGatewayDto.getName());
            serviceProxyDto.setVirtualGwCode(virtualGatewayDto.getCode());
            serviceProxyDto.setGwType(virtualGatewayDto.getGwType());
            serviceProxyDto.setGwAddr(virtualGatewayDto.getAddr());
        }

        ServiceDto serviceDto = serviceInfoService.get(serviceProxyInfo.getServiceId());
        if (serviceDto != null) {
            serviceProxyDto.setServiceName(serviceDto.getDisplayName());
            serviceProxyDto.setServiceTag(serviceDto.getServiceName());
            serviceProxyDto.setServiceType(serviceDto.getServiceType());
        }
        return serviceProxyDto;
    }

    /**
     * 为dto增加版本信息，因为db中存储的是字符串，dto中是list，不能直接用BeanUtil.copy来赋值
     * 用于前端展示
     *
     * @param serviceProxyInfo
     * @return
     */
    public static List<SubsetDto> setSubsetForDto(ServiceProxyInfo serviceProxyInfo) {
        //增加版本信息
        if (StringUtils.isBlank(serviceProxyInfo.getSubsets())) {
            return null;
        }
        List<JSONObject> subsetObject = JSON.parseObject(serviceProxyInfo.getSubsets(), List.class);
        List<SubsetDto> subsets = new ArrayList<>();
        for (JSONObject subsetTemp : subsetObject) {
            subsets.add(JSONObject.toJavaObject(subsetTemp, SubsetDto.class));
        }
        return subsets;

    }

    /**
     * 为dto增加负载均衡和连接池信息，因为db中存储的是字符串，dto中是list，不能直接用BeanUtil.copy来赋值
     * 用于前端展示
     *
     * @param serviceProxyInfo
     * @return
     */
    public static ServiceTrafficPolicyDto setTrafficPolicyForDto(ServiceProxyInfo serviceProxyInfo) {
        if (StringUtils.isBlank(serviceProxyInfo.getTrafficPolicy())) {
            return null;
        }
        //增加负载均衡和连接池信息
        ServiceTrafficPolicyDto trafficPolicy = JSON.parseObject(serviceProxyInfo.getTrafficPolicy(), ServiceTrafficPolicyDto.class);
        return trafficPolicy;
    }

    @Override
    public ServiceProxyInfo toMeta(ServiceProxyDto serviceProxyDto) {
        if (serviceProxyDto == null) {
            return null;
        }
        ServiceProxyInfo serviceProxyInfo = new ServiceProxyInfo();
        serviceProxyInfo.setId(serviceProxyDto.getId());
        serviceProxyInfo.setServiceId(serviceProxyDto.getServiceId());
        serviceProxyInfo.setCode(getServiceCode(serviceProxyDto));
        serviceProxyInfo.setBackendService(serviceProxyDto.getBackendService());
        serviceProxyInfo.setPublishProtocol(serviceProxyDto.getPublishProtocol());
        serviceProxyInfo.setPublishType(serviceProxyDto.getPublishType());
        serviceProxyInfo.setRegistryCenterType(serviceProxyDto.getRegistryCenterType());
        serviceProxyInfo.setVirtualGwId(serviceProxyDto.getVirtualGwId());
        serviceProxyInfo.setGwType(serviceProxyDto.getGwType());
        serviceProxyInfo.setCreateTime(serviceProxyDto.getCreateTime());
        serviceProxyInfo.setUpdateTime(serviceProxyDto.getUpdateTime());
        serviceProxyInfo.setLoadBalancer(serviceProxyDto.getLoadBalancer());
        serviceProxyInfo.setTrafficPolicy(serviceProxyDto.getTrafficPolicy() != null ? JSON.toJSONString(serviceProxyDto.getTrafficPolicy()) : null);
        serviceProxyInfo.setSubsets(serviceProxyDto.getSubsets() != null ? JSON.toJSONString(serviceProxyDto.getSubsets()) : null);
        return serviceProxyInfo;
    }

    @Override
    public ErrorCode checkCreateParam(ServiceProxyDto serviceProxyDto) {
        ServiceProxyDto serviceProxyInDb = getServiceProxyByServiceIdAndGwId(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getServiceId());
        if (serviceProxyInDb != null) {
            logger.info("发布服务，服务已发布到当前网关,服务id:{},网关id:{}，不允许再次发布", serviceProxyDto.getServiceId(), serviceProxyDto.getVirtualGwId());
            return CommonErrorCode.SERVICE_ALREADY_PUBLISHED;
        }
        return checkCommonParam(serviceProxyDto);
    }

    /**
     * 校验公共参数
     *
     * @param serviceProxyDto
     * @return
     */
    private ErrorCode checkCommonParam(ServiceProxyDto serviceProxyDto) {
        ServiceDto serviceDto = serviceInfoService.get(serviceProxyDto.getServiceId());
        if (serviceDto == null) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        serviceProxyDto.setCode(serviceDto.getServiceName());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.info("发布服务，指定网关不存在，网关id:{}", serviceProxyDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        //校验注册中心参数
        ErrorCode errorCode = checkRegistryCenterInfo(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        //校验流量策略参数
        errorCode = checkTrafficPolicy(serviceProxyDto.getTrafficPolicy());
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }

        return checkSubsetWhenPublishService(serviceProxyDto);
    }

    @Override
    public ErrorCode checkUpdateParam(ServiceProxyDto serviceProxyDto) {
        ServiceProxyDto serviceProxyInDb = getServiceProxyByServiceIdAndGwId(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getServiceId());
        if (serviceProxyInDb == null) {
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }

        //当有流量镜像指向该服务的版本时，该版本不能被删除
        ErrorCode errorCode = checkRouteMirrorSubset(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        return checkCommonParam(serviceProxyDto);
    }

    /**
     * 当有流量镜像指向该服务的版本时，该版本不能被删除
     *
     * @param serviceProxyDto
     * @return
     */
    ErrorCode checkRouteMirrorSubset(ServiceProxyDto serviceProxyDto) {
        RouteRuleQuery query = RouteRuleQuery.builder().mirrorServiceId(serviceProxyDto.getServiceId()).virtualGwId(serviceProxyDto.getVirtualGwId()).build();
        List<RouteRuleProxyDto> routeProxyList = routeRuleProxyService.getRouteRuleProxyList(query);
        if (CollectionUtils.isEmpty(routeProxyList)) {
            return CommonErrorCode.SUCCESS;
        }
        List<SubsetDto> serviceSubsets = serviceProxyDto.getSubsets();
        Set<String> subsetNameSet = serviceSubsets.stream().map(SubsetDto::getName).collect(Collectors.toSet());
        for (RouteRuleProxyDto routeProxyInfo : routeProxyList) {
            if (routeProxyInfo.getMirrorTraffic() == null || StringUtils.isBlank(routeProxyInfo.getMirrorTraffic().getSubsetName())) {
                continue;
            }
            if (!subsetNameSet.contains(routeProxyInfo.getMirrorTraffic().getSubsetName())) {
                RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.get(routeProxyInfo.getRouteRuleId());
                return CommonErrorCode.subsetUsedByRouteRule(routeRuleProxyDto.getRouteRuleName());
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(ServiceProxyDto serviceProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        ServiceProxyDto servicePublishInfo = getServiceProxyByServiceIdAndGwId(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getServiceId());
        if (servicePublishInfo == null) {
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }
        RouteRuleQuery query = RouteRuleQuery.builder().virtualGwId(serviceProxyDto.getVirtualGwId()).serviceId(serviceProxyDto.getServiceId()).build();
        List<RouteRuleProxyDto> routeRuleList = routeRuleProxyService.getRouteRuleProxyList(query);
        if (!CollectionUtils.isEmpty(routeRuleList)) {
            return CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED;
        }
        //当有流量镜像指向该服务时，该服务不能下线
        RouteRuleQuery routeRuleQuery = RouteRuleQuery.builder().mirrorServiceId(serviceProxyDto.getServiceId()).virtualGwId(serviceProxyDto.getVirtualGwId()).build();
        List<RouteRuleProxyDto> routeProxyList = routeRuleProxyService.getRouteRuleProxyList(routeRuleQuery);
        if (!CollectionUtils.isEmpty(routeProxyList)) {
            return CommonErrorCode.MIRROR_BY_ROUTE_RULE;
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     *
     * @see org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker#doPostGetBackendServicesHook(List)
     * @param virtualGwId                网关id（根据id查询网关所属的数据面地址）
     * @param name                模糊查询服务名
     * @param registryCenterType  注册中心类型
     * @return
     */
    @Override
    public List<BackendServiceWithPortDto> getBackendServicesFromDataPlane(long virtualGwId, String name, String registryCenterType) {
        return Collections.emptyList();
    }


    @Override
    public List<ServiceProxyDto> getServiceProxy(long virtualGwId, long serviceId, long projectId, long offset, long limit) {
        return serviceProxyDao.getServiceProxyByLimit(virtualGwId, serviceId, projectId, offset, limit).stream().map(this::toView).collect(Collectors.toList());
    }


    @Override
    public List<ServiceProxyDto> getServiceProxy(long virtualGwId, String pattern, long projectId, long offset, long limit) {
        List<Long> serviceIds = Collections.EMPTY_LIST;
        if (StringUtils.isNotBlank(pattern)) {
            serviceIds = serviceInfoService.findAllServiceByProjectIdLimit(pattern, NumberUtils.LONG_ZERO, Integer.MAX_VALUE, projectId).stream().map(
                    ServiceDto::getId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(serviceIds)){
                return Collections.emptyList();
            }
        }
        return serviceProxyDao.getServiceProxyByLimit(virtualGwId, serviceIds, projectId, offset, limit).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceProxyDto> getServiceProxyWithPort(long virtualGwId, String pattern, long projectId, long offset, long limit) {
        List<ServiceProxyDto> serviceProxy = getServiceProxy(virtualGwId, pattern, projectId, offset, limit);
        serviceProxy.forEach(this::fillServicePort);
        return serviceProxy;
    }

    public void fillServicePort(ServiceProxyDto serviceProxyDto){
        //静态发布，不获取port信息
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return;
        }
        //dynamic发布，获取port数据
        if (Arrays.asList(RegistryCenterEnum.Eureka.getType(), RegistryCenterEnum.Consul.getType(), RegistryCenterEnum.Zookeeper.getType())
                .contains(serviceProxyDto.getRegistryCenterType())){
            serviceProxyDto.setPort(Collections.singletonList(80));
        }else {
            String backendService = serviceProxyDto.getBackendService();
            List<BackendServiceWithPortDto> backendServiceWithPortDtos = serviceProxyService.getBackendServicesFromDataPlane(serviceProxyDto.getVirtualGwId(), backendService, serviceProxyDto.getRegistryCenterType());
            BackendServiceWithPortDto backendServiceWithPortDto = backendServiceWithPortDtos.stream().filter(o -> o.getName().equals(backendService)).findFirst().orElse(null);
            if (backendServiceWithPortDto == null || CollectionUtils.isEmpty(backendServiceWithPortDto.getPorts())) {
                logger.info("从api-plane获取服务port信息失败，api-plane返回为空");
                return;
            }
            serviceProxyDto.setPort(backendServiceWithPortDto.getPorts());
        }
    }


    @Override
    public long getServiceProxyCount(long virtualGwId, String pattern, long projectId) {
        if (StringUtils.isBlank(pattern)) {
            return getServiceProxyCount(virtualGwId, 0);
        }
        List<Long> serviceIds = serviceInfoService.findAllServiceByProjectIdLimit(pattern, NumberUtils.LONG_ZERO, Integer.MAX_VALUE, projectId).stream()
                .map(ServiceDto::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serviceIds)) {
            return 0;
        }
        return serviceProxyDao.getCount(virtualGwId, serviceIds, projectId);
    }

    @Override
    public long getServiceProxyCount(long virtualGwId, long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        //服务id或网关id有一项等于0
        if (NumberUtils.INTEGER_ZERO == serviceId || NumberUtils.INTEGER_ZERO == virtualGwId) {
            params.put("projectId", ProjectTraceHolder.getProId());
        }
        //查询指定的服务发布数量
        if (NumberUtils.INTEGER_ZERO != serviceId) {
            params.put(BaseConst.SERVICE_ID, serviceId);
        }
        //查询指定的网关发布数量
        if (NumberUtils.INTEGER_ZERO != virtualGwId) {
            params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        }

        return serviceProxyDao.getCountByFields(params);
    }



    @Override
    public ServiceProxyDto getServiceProxyByServiceIdAndGwId(long virtualGwId, long serviceId) {
        ServiceProxyInfo serviceProxyInfo = getServiceProxyInfo(virtualGwId, serviceId);
        return toView(serviceProxyInfo);
    }

    @Override
    public ServiceProxyInfo getServiceProxyInfo(long virtualGwId, long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        params.put(BaseConst.SERVICE_ID, serviceId);
        List<ServiceProxyInfo> serviceProxyInfos = serviceProxyDao.getRecordsByField(params);
        return Iterables.getFirst(serviceProxyInfos, null);
    }

    @Override
    public List<ServiceProxyDto> getServiceProxyByServiceId(long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.SERVICE_ID, serviceId);
        return serviceProxyDao.getRecordsByField(params).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceProxyDto> getServiceProxy(Long virtualGwId, Long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.SERVICE_ID, serviceId);
        if (virtualGwId != null){
            params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        }
        return serviceProxyDao.getRecordsByField(params).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public ErrorCode getRouteRuleNameWithServiceSubset(ServiceProxyDto serviceProxyDto) {
        long serviceId = serviceProxyDto.getServiceId();
        List<SubsetDto> subsetDtos = serviceProxyDto.getSubsets();
        if (subsetDtos == null) {
            return CommonErrorCode.SUCCESS;
        }
        List<String> subsets = subsetDtos.stream().map(SubsetDto::getName).collect(Collectors.toList());
        RouteRuleQuery ruleQuery = RouteRuleQuery.builder().serviceId(serviceId).build();
        List<RouteRuleProxyDto> routeRuleList = routeRuleProxyService.getRouteRuleProxyList(ruleQuery);
        for (RouteRuleProxyDto routeRuleProxyDto : routeRuleList) {
            for (DestinationDto destinationDto : routeRuleProxyDto.getDestinationServices()) {
                if (StringUtils.isNotBlank(destinationDto.getSubsetName()) && !subsets.contains(destinationDto.getSubsetName())) {
                    RouteRuleDto routeRuleInfo = routeRuleInfoService.get(routeRuleProxyDto.getRouteRuleId());
                    return CommonErrorCode.subsetUsedByRouteRule(routeRuleInfo.getRouteRuleName());
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<ServiceProxyDto> getServiceProxyListByVirtualGwId(long virtualGwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        return serviceProxyDao.getRecordsByField(params).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceProxyDto> batchGetServiceProxyList(long virtualGwId, List<Long> serviceIdList) {
        return serviceProxyDao.batchGetServiceProxyList(virtualGwId, serviceIdList).stream().map(this::toView).collect(Collectors.toList());
    }


    @Override
    public List<String> getSubsetsName(ServiceProxyDto serviceProxyInfo) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyInfo.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.error("获取subsetsname存在脏数据，virtualGwId:{}", serviceProxyInfo.getVirtualGwId());
            return Lists.newArrayList();
        }
        List<String> subsetNames = Lists.newArrayList();
        //默认subset
        subsetNames.add((serviceProxyInfo.getCode() + "-" + ServiceProxyConvert.getGateway(virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode())).toLowerCase());
        if (CollectionUtils.isEmpty(serviceProxyInfo.getSubsets())) {
            return subsetNames;
        }

        //用户自定义的版本
        List<SubsetDto> subsetDtos = ServiceProxyConvert.buildSubset(serviceProxyInfo, virtualGatewayDto);
        List<String> subSets = subsetDtos.stream().map(SubsetDto::getName).collect(Collectors.toList());
        subsetNames.addAll(subSets);
        return subsetNames;
    }


    @Override
    public String getServiceCode(ServiceProxyDto serviceProxyDto) {
        if (serviceProxyDto == null) {
            return null;
        }
        return serviceProxyDto.getPublishType() + "-" + serviceProxyDto.getServiceId();
    }

    @Override
    public List<String> getAllServiceTag(long virtualGwId) {
        List<ServiceProxyDto> serviceProxyList = getServiceProxyListByVirtualGwId(virtualGwId);
        if (CollectionUtils.isEmpty(serviceProxyList)) {
            return Collections.emptyList();
        }
        return serviceProxyList.stream().filter(s -> ProjectTraceHolder.getProId() == s.getProjectId())
                .map(ServiceProxyDto::getServiceName).collect(Collectors.toList());
    }


    /**
     * 校验服务发布时，填写的版本信息
     *
     * @param serviceProxyDto
     * @return
     */
    public ErrorCode checkSubsetWhenPublishService(ServiceProxyDto serviceProxyDto) {
        List<SubsetDto> subsetDtoList = serviceProxyDto.getSubsets();
        ErrorCode errorCode = CommonErrorCode.SUCCESS;
        if (subsetDtoList == null || subsetDtoList.size() == 0) {
            return errorCode;
        }

        Set<String> subsetName = new HashSet<>();
        subsetDtoList.forEach(e -> subsetName.add(e.getName()));
        if (subsetName.size() < subsetDtoList.size()) {
            return CommonErrorCode.DUPLICATED_SUBSET_NAME;
        }
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            //静态发布的服务，版本中包含的地址要在发布地址列表中
            List<String> addrList = Arrays.asList(serviceProxyDto.getBackendService().split(","));
            for (SubsetDto subsetDto : subsetDtoList) {
                if (CollectionUtils.isEmpty(subsetDto.getStaticAddrList()) || !addrList.containsAll(subsetDto.getStaticAddrList())) {
                    return CommonErrorCode.INVALID_SUBSET_STATIC_ADDR;
                }
                Set<String> addrSet = new HashSet<>();
                addrSet.addAll(subsetDto.getStaticAddrList());
                if (addrSet.size() < subsetDto.getStaticAddrList().size()) {
                    //同一个版本里配置的静态地址不能重复
                    return CommonErrorCode.DUPLICATED_SUBSET_STATIC_ADDR;
                }
            }
            //每个地址仅能出现在0或1个版本中
            for (String addr : addrList) {
                int count = 0;
                for (SubsetDto subsetDto : subsetDtoList) {
                    if (subsetDto.getStaticAddrList().contains(addr)) {
                        count++;
                    }
                }
                if (count > 1) {
                    return CommonErrorCode.DUPLICATED_STATIC_ADDR;
                }
            }
        }
        for (SubsetDto subsetDto : subsetDtoList) {
            errorCode = checkTrafficPolicy(subsetDto.getTrafficPolicy());
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                return errorCode;
            }
        }
        return errorCode;
    }


    /**
     * 校验负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     *
     * @param serviceTrafficPolicyDto
     * @return
     */
    private ErrorCode checkTrafficPolicy(ServiceTrafficPolicyDto serviceTrafficPolicyDto) {
        if (serviceTrafficPolicyDto == null) {
            return CommonErrorCode.SUCCESS;
        }
        ErrorCode errorCode = checkLoadBalance(serviceTrafficPolicyDto.getLoadBalancer());
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        ServiceConnectionPoolDto serviceConnectionPoolDto = serviceTrafficPolicyDto.getConnectionPoolDto();
        if (serviceConnectionPoolDto != null) {
            ServiceConnectionPoolDto.ServiceHttpConnectionPoolDto serviceHttpConnectionPoolDto = serviceConnectionPoolDto.getServiceHttpConnectionPoolDto();
            ServiceConnectionPoolDto.ServiceTcpConnectionPoolDto serviceTcpConnectionPoolDto = serviceConnectionPoolDto.getServiceTcpConnectionPoolDto();
            if (serviceHttpConnectionPoolDto != null) {
                Integer http1MaxPendingRequests = serviceHttpConnectionPoolDto.getHttp1MaxPendingRequests();
                Integer http2MaxRequests = serviceHttpConnectionPoolDto.getHttp2MaxRequests();
                Integer idleTimeout = serviceHttpConnectionPoolDto.getIdleTimeout();
                Integer maxRequestsPerConnection = serviceHttpConnectionPoolDto.getMaxRequestsPerConnection();
                if (http1MaxPendingRequests < 0) {
                    return CommonErrorCode.INVALID_HTTP_1_MAX_PENDING_REQUESTS;
                }
                if (http2MaxRequests < 0) {
                    return CommonErrorCode.INVALID_HTTP_2_MAX_REQUESTS;
                }
                if (idleTimeout < 0) {
                    return CommonErrorCode.INVALID_IDLE_TIMEOUT;
                }
                if (maxRequestsPerConnection < 0) {
                    return CommonErrorCode.INVALID_MAX_REQUESTS_PER_CONNECTION;
                }
            }
            if (serviceTcpConnectionPoolDto != null) {
                Integer maxConnections = serviceTcpConnectionPoolDto.getMaxConnections();
                Integer connectTimeout = serviceTcpConnectionPoolDto.getConnectTimeout();
                if (maxConnections < 0) {
                    return CommonErrorCode.INVALID_MAX_CONNECTIONS;
                }
                if (connectTimeout < 0) {
                    return CommonErrorCode.INVALID_CONNECT_TIMEOUT;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 校验负载均衡
     *
     * @param serviceLoadBalancerDto
     * @return
     */
    private ErrorCode checkLoadBalance(ServiceLoadBalancerDto serviceLoadBalancerDto) {
        if (serviceLoadBalancerDto == null) {
            return CommonErrorCode.SUCCESS;
        }
        if (BaseConst.SERVICE_LOADBALANCER_SIMPLE.equals(serviceLoadBalancerDto.getType())) {
            return checkSimpleLoadBalance(serviceLoadBalancerDto);
        } else if (BaseConst.SERVICE_LOADBALANCER_HASH.equals(serviceLoadBalancerDto.getType())) {
            return checkHashLoadBalance(serviceLoadBalancerDto);
        }
        // 服务预热时间窗校验，为空则代表不开启功能；时间窗仅支持[1, 3600]区间配置
        if (serviceLoadBalancerDto.getSlowStartWindow() != null &&
                (serviceLoadBalancerDto.getSlowStartWindow() > 3600 || serviceLoadBalancerDto.getSlowStartWindow() < 1)) {
            return CommonErrorCode.INVALID_SLOW_START_WINDOW;
        }
        //type不合法
        return CommonErrorCode.INVALID_LOAD_BALANCE_TYPE;
    }

    /**
     * 校验简单负载均衡
     *
     * @param serviceLoadBalancerDto
     * @return
     */
    private ErrorCode checkSimpleLoadBalance(ServiceLoadBalancerDto serviceLoadBalancerDto) {
        //Simple类型，包含ROUND_ROBIN|LEAST_CONN|RANDOM
        final List<String> simpleList = new ArrayList<>();
        simpleList.add(BaseConst.SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN);
        simpleList.add(BaseConst.SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN);
        simpleList.add(BaseConst.SERVICE_LOADBALANCER_SIMPLE_RANDOM);
        if (StringUtils.isBlank(serviceLoadBalancerDto.getSimple()) ||
                !simpleList.contains(serviceLoadBalancerDto.getSimple())) {
            return CommonErrorCode.INVALID_SIMPLE_LOAD_BALANCE_TYPE;
        }
        //将Cookie相关参数置空
        serviceLoadBalancerDto.setConsistentHash(null);
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 校验哈希负载均衡
     *
     * @param serviceLoadBalancerDto
     * @return
     */
    private ErrorCode checkHashLoadBalance(ServiceLoadBalancerDto serviceLoadBalancerDto) {
        //一致性哈希
        ServiceConsistentHashDto serviceConsistentHashDto = serviceLoadBalancerDto.getConsistentHash();
        if (serviceConsistentHashDto == null) {
            //不能为空
            return CommonErrorCode.INVALID_CONSISTENT_HASH_OBJECT;
        }
        final List<String> hashList = new ArrayList<>();
        hashList.add(BaseConst.SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME);
        hashList.add(BaseConst.SERVICE_LOADBALANCER_HASH_HTTPCOOKIE);
        hashList.add(BaseConst.SERVICE_LOADBALANCER_HASH_USESOURCEIP);

        if (StringUtils.isBlank(serviceConsistentHashDto.getType()) ||
                !hashList.contains(serviceConsistentHashDto.getType())) {
            return CommonErrorCode.INVALID_CONSISTENT_HASH_TYPE;
        }
        if (BaseConst.SERVICE_LOADBALANCER_HASH_HTTPCOOKIE.equals(serviceConsistentHashDto.getType())) {
            ServiceConsistentHashDto.ServiceConsistentHashCookieDto serviceConsistentHashCookieDto =
                    serviceConsistentHashDto.getCookieDto();
            if (serviceConsistentHashCookieDto == null) {
                //cookie不能为空
                return CommonErrorCode.INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT;
            }
            String name = serviceConsistentHashCookieDto.getName();
            if (StringUtils.isBlank(name) || name.length() > 255) {
                return CommonErrorCode.INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME;
            }
            Integer ttl = serviceConsistentHashCookieDto.getTtl();
            if (ttl == null || ttl < 0) {
                return CommonErrorCode.INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL;
            }
            serviceConsistentHashDto.setHttpHeaderName(null);
            serviceConsistentHashDto.setUseSourceIp(null);
        }

        if (BaseConst.SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME.equals(serviceConsistentHashDto.getType())) {
            if (StringUtils.isBlank(serviceConsistentHashDto.getHttpHeaderName())) {
                return CommonErrorCode.INVALID_CONSISTENT_HASH_HTTP_HEADER_NAME;
            }
            serviceConsistentHashDto.setCookieDto(null);
            serviceConsistentHashDto.setUseSourceIp(null);
        }

        if (BaseConst.SERVICE_LOADBALANCER_HASH_USESOURCEIP.equals(serviceConsistentHashDto.getType())) {
            if (serviceConsistentHashDto.getUseSourceIp() == null) {
                return CommonErrorCode.INVALID_CONSISTENT_HASH_SOURCE_IP;
            }
            serviceConsistentHashDto.setHttpHeaderName(null);
            serviceConsistentHashDto.setCookieDto(null);
        }
        //将simple类型相关参数置空
        serviceLoadBalancerDto.setSimple(null);
        return CommonErrorCode.SUCCESS;
    }


    private ErrorCode checkRegistryCenterInfo(ServiceProxyDto serviceProxyDto) {
        if (!BaseConst.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return CommonErrorCode.SUCCESS;
        }
        List<ServiceProxyDto> serviceProxyList = getServiceProxyByServiceId(serviceProxyDto.getServiceId());
        if (!CollectionUtils.isEmpty(serviceProxyList)) {
            List<String> backendServices = serviceProxyList.stream().map(ServiceProxyDto::getBackendService).collect(Collectors.toList());
            if (!backendServices.contains(serviceProxyDto.getBackendService())) {
                logger.info("同一个服务发布/更新到不同的网关，指定后端服务不同，不允许创建");
                return CommonErrorCode.BACKEND_SERVICE_DIFFERENT;
            }
        }
        if (StringUtils.isBlank(serviceProxyDto.getRegistryCenterType()) || RegistryCenterEnum.Kubernetes.getType().equals(serviceProxyDto.getRegistryCenterType())) {
            //默认Kubernetes
            serviceProxyDto.setRegistryCenterType(RegistryCenterEnum.Kubernetes.getType());
            return CommonErrorCode.SUCCESS;
        }
        //校验consul & ZK
        if (StringUtils.equalsAny(serviceProxyDto.getRegistryCenterType()
                , RegistryCenterEnum.Consul.getType(), RegistryCenterEnum.Zookeeper.getType())) {
            RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(serviceProxyDto.getRegistryCenterType());
            if (registryCenterEnum == null) {
                logger.info("错误的注册中心类型，RegistryCenterType = {} ", serviceProxyDto.getRegistryCenterType());
                return CommonErrorCode.invalidParameterRegistryCenterType(serviceProxyDto.getRegistryCenterType());
            }
        }
        return CommonErrorCode.SUCCESS;
    }

}
