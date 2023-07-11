package org.hango.cloud.common.infra.serviceproxy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.PageQuery;
import org.hango.cloud.common.infra.base.meta.RegexConst;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.ServiceMetaForRouteDto;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.serviceproxy.dto.*;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceType;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;
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
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private IApiModelService apiModelService;


    @Override
    public long create(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        serviceProxyDao.add(serviceProxyInfo);
        return serviceProxyInfo.getId();
    }


    @Override
    public long update(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        return serviceProxyDao.update(serviceProxyInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ServiceProxyDto serviceProxyDto) {
        ServiceProxyInfo serviceProxyInfo = toMeta(serviceProxyDto);
        serviceProxyDao.delete(serviceProxyInfo);
    }

    @Override
    public List<? extends ServiceProxyDto> findAll() {
        return serviceProxyDao.findAll().stream().map(this::toView).collect(Collectors.toList());
    }


    @Override
    public Page<ServiceProxyDto> pageAll(PageQuery query) {
        return toPageView(serviceProxyDao.pageRecordsByField(Wrappers.emptyWrapper(), query.of()));
    }

    @Override
    public ServiceProxyDto get(long id) {
        return toView(serviceProxyDao.get(id));
    }

    @Override
    public ServiceProxyDto toView(ServiceProxyInfo serviceProxyInfo) {
        if (serviceProxyInfo == null) {
            return null;
        }
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setId(serviceProxyInfo.getId());
        serviceProxyDto.setName(serviceProxyInfo.getName());
        serviceProxyDto.setAlias(serviceProxyInfo.getAlias());
        serviceProxyDto.setHosts(serviceProxyInfo.getHosts());
        serviceProxyDto.setBackendService(serviceProxyInfo.getBackendService());
        serviceProxyDto.setRegistryCenterType(serviceProxyInfo.getRegistryCenterType());
        serviceProxyDto.setProtocol(serviceProxyInfo.getProtocol());
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
        serviceProxyInfo.setAlias(serviceProxyDto.getAlias());
        serviceProxyInfo.setName(serviceProxyDto.getName());
        serviceProxyInfo.setBackendService(serviceProxyDto.getBackendService());
        serviceProxyInfo.setProtocol(serviceProxyDto.getProtocol());
        serviceProxyInfo.setProjectId(ProjectTraceHolder.getProId());
        serviceProxyInfo.setPublishType(serviceProxyDto.getPublishType());
        serviceProxyInfo.setHosts(serviceProxyDto.getHosts());
        serviceProxyInfo.setRegistryCenterType(serviceProxyDto.getRegistryCenterType());
        serviceProxyInfo.setVirtualGwId(serviceProxyDto.getVirtualGwId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto != null) {
            serviceProxyInfo.setGwType(virtualGatewayDto.getGwType());
        }
        serviceProxyInfo.setCreateTime(serviceProxyDto.getCreateTime());
        serviceProxyInfo.setUpdateTime(serviceProxyDto.getUpdateTime());
        serviceProxyInfo.setLoadBalancer(serviceProxyDto.getLoadBalancer());
        serviceProxyInfo.setTrafficPolicy(serviceProxyDto.getTrafficPolicy() != null ? JSON.toJSONString(serviceProxyDto.getTrafficPolicy()) : null);
        serviceProxyInfo.setSubsets(serviceProxyDto.getSubsets() != null ? JSON.toJSONString(serviceProxyDto.getSubsets()) : null);
        return serviceProxyInfo;
    }

    @Override
    public ErrorCode checkCreateParam(ServiceProxyDto serviceProxyDto) {
       List<ServiceProxyDto> serviceProxyInDb = getServiceProxy(ServiceProxyQuery.builder()
                .pattern(serviceProxyDto.getName())
                .virtualGwId(serviceProxyDto.getVirtualGwId())
                .build());
       serviceProxyDto.setProjectId(ProjectTraceHolder.getProId());
        if (!CollectionUtils.isEmpty(serviceProxyInDb)) {
            logger.info("发布服务，服务已发布到当前网关,服务名称:{},网关id:{}，不允许再次发布", serviceProxyDto.getName(), serviceProxyDto.getVirtualGwId());
            return CommonErrorCode.SERVICE_NAME_ALREADY_EXIST;
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
        if (StringUtils.isBlank(serviceProxyDto.getHosts())){
            return CommonErrorCode.NO_SUCH_DOMAIN;
        }
        if (ServiceType.getServiceTypeByName(serviceProxyDto.getProtocol()) ==null) {
            return CommonErrorCode.SERVICE_TYPE_INVALID;
        }
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())){
            Pattern compile = Pattern.compile(RegexConst.REGEX_DOMAIN);
            String[] split = StringUtils.split(serviceProxyDto.getBackendService(), BaseConst.SYMBOL_COMMA);

            for (String s : split) {
                String[] domain = s.split(BaseConst.SYMBOL_COLON);
                if (!compile.matcher(domain[0]).matches()) {
                    return CommonErrorCode.INVALID_ADDR;
                }
            }

        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(serviceProxyDto.getVirtualGwId());
        if (virtualGatewayDto == null) {
            logger.info("发布服务，指定网关不存在，网关id:{}", serviceProxyDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        List<DomainInfoDTO> domainInfos = virtualGatewayDto.getDomainInfos();
        if (CollectionUtils.isEmpty(domainInfos)){
            return CommonErrorCode.NO_SUCH_DOMAIN;
        }
        Set<String> domainHosts = domainInfos.stream().map(DomainInfoDTO::getHost).collect(Collectors.toSet());
        String[] hosts = serviceProxyDto.getHosts().split(",");
        for (String  host : hosts) {
            if (!domainHosts.contains(host)){
                return CommonErrorCode.NO_SUCH_DOMAIN;
            }
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
        ServiceProxyDto serviceProxyFromDb = get(serviceProxyDto.getId());
        if (serviceProxyFromDb == null) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        if (!serviceProxyFromDb.getName().equals(serviceProxyDto.getName())){
            return CommonErrorCode.CANNOT_UPDATE_SERVICE_NAME;
        }
        //当有流量镜像指向该服务的版本时，该版本不能被删除
        ErrorCode errorCode = checkRouteMirrorSubset(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        errorCode = checkSubset(serviceProxyDto);
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
        RouteQuery query = RouteQuery.builder().mirrorServiceId(serviceProxyDto.getId()).virtualGwId(serviceProxyDto.getVirtualGwId()).build();
        List<RouteDto> routeProxyList = routeService.getRouteList(query);
        if (CollectionUtils.isEmpty(routeProxyList)) {
            return CommonErrorCode.SUCCESS;
        }
        List<SubsetDto> serviceSubsets = serviceProxyDto.getSubsets();
        if (CollectionUtils.isEmpty(serviceSubsets)){
            return CommonErrorCode.SUCCESS;
        }
        Set<String> subsetNameSet = serviceSubsets.stream().map(SubsetDto::getName).collect(Collectors.toSet());
        for (RouteDto routeProxyInfo : routeProxyList) {
            if (routeProxyInfo.getMirrorTraffic() == null || StringUtils.isBlank(routeProxyInfo.getMirrorTraffic().getSubsetName())) {
                continue;
            }
            if (!subsetNameSet.contains(routeProxyInfo.getMirrorTraffic().getSubsetName())) {
                RouteDto routeDto = routeService.get(routeProxyInfo.getId());
                return CommonErrorCode.subsetUsedByRouteRule(routeDto.getName());
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
        long apiCountByServiceId = apiInfoService.getApiCountByServiceId(serviceProxyDto.getId());
        if (NumberUtils.LONG_ZERO != apiCountByServiceId){
            return CommonErrorCode.CANNOT_DELETE_API_SERVICE;
        }
        List<ApiModel> apiModelByServiceId = apiModelService.getApiModelByServiceId(serviceProxyDto.getId());
        if (!CollectionUtils.isEmpty(apiModelByServiceId)){
            return CommonErrorCode.CANNOT_DELETE_API_SERVICE;
        }
        RouteQuery query = RouteQuery.builder().virtualGwId(serviceProxyDto.getVirtualGwId()).serviceId(serviceProxyDto.getId()).build();
        List<RouteDto> routeRuleList = routeService.getRouteList(query);
        if (!CollectionUtils.isEmpty(routeRuleList)) {
            return CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED;
        }
        //当有流量镜像指向该服务时，该服务不能下线
        RouteQuery routeQuery = RouteQuery.builder().mirrorServiceId(serviceProxyDto.getId()).virtualGwId(serviceProxyDto.getVirtualGwId()).build();
        List<RouteDto> routeProxyList = routeService.getRouteList(routeQuery);
        if (!CollectionUtils.isEmpty(routeProxyList)) {
            for (RouteDto routeDto : routeProxyList) {
                if (routeDto.getMirrorSwitch() == 1) {
                    return CommonErrorCode.MIRROR_BY_ROUTE_RULE;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * @param virtualGwId        网关id（根据id查询网关所属的数据面地址）
     * @param name               模糊查询服务名
     * @param registryCenterType 注册中心类型
     * @return
     * @see org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker#doPostGetBackendServicesHook(List)
     */
    @Override
    public List<BackendServiceWithPortDto> getBackendServicesFromDataPlane(long virtualGwId, String name, String registryCenterType) {
        return Collections.emptyList();
    }

    @Override
    public void fillServiceHealthStatus(ServiceProxyDto serviceProxyDto) {
        return;
    }

    @Override
    public List<ServiceProxyDto> getServiceProxy(ServiceProxyQuery query) {
        return serviceProxyDao.getByConditionOptional(query).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceProxyDto> getServiceProxyByHost(String host) {
        if(StringUtils.isBlank(host)){
            return null;
        }
        LambdaQueryWrapper<ServiceProxyInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.apply("find_in_set ('"+ host+"', hosts )");
        return serviceProxyDao.getRecordsByField(wrapper).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long countServiceProxy(ServiceProxyQuery query) {
        LambdaQueryWrapper<ServiceProxyInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ServiceProxyInfo::getProjectId,query.getProjectId());
        if (NumberUtils.LONG_ZERO != query.getVirtualGwId()){
            wrapper.eq(ServiceProxyInfo::getVirtualGwId,query.getVirtualGwId());
        }
        if (StringUtils.isNotBlank(query.getPattern())){
            wrapper.eq(ServiceProxyInfo::getName,query.getPattern());
        }
        return serviceProxyDao.getCountByFields(wrapper);
    }

    @Override
    public Page<ServiceProxyDto> getServiceProxyLimited(ServiceProxyQuery query) {
        return toPageView(serviceProxyDao.getServiceProxyByLimit(query));
    }


    @Override
    public void fillServicePort(ServiceProxyDto serviceProxyDto) {
        //静态发布，不获取port信息
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return;
        }
        //dynamic发布，获取port数据
        if (Arrays.asList(RegistryCenterEnum.Eureka.getType(), RegistryCenterEnum.Consul.getType(), RegistryCenterEnum.Zookeeper.getType())
                .contains(serviceProxyDto.getRegistryCenterType())) {
            serviceProxyDto.setPort(Collections.singletonList(80));
        } else {
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
    public List<ServiceProxyDto> getServiceByIds(List<Long> ids) {
        return serviceProxyDao.getByIds(ids).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public Boolean updateServiceHost(Long id, String host) {
        ServiceProxyInfo serviceProxyInfo = serviceProxyDao.get(id);
        serviceProxyInfo.setHosts(host);
        int update = serviceProxyDao.update(serviceProxyInfo);
        return update > 0;
    }

    @Override
    public ServiceProxyDto fillServiceProxy(ServiceProxyUpdateDto serviceProxyUpdateDto) {
        ServiceProxyDto serviceProxyDto = get(serviceProxyUpdateDto.getId());
        if (serviceProxyDto == null){
            serviceProxyDto =  new ServiceProxyDto();
        }
        serviceProxyDto.setId(serviceProxyUpdateDto.getId());
        serviceProxyDto.setAlias(serviceProxyUpdateDto.getAlias());
        serviceProxyDto.setTrafficPolicy(serviceProxyUpdateDto.getTrafficPolicy());
        serviceProxyDto.setHosts(serviceProxyUpdateDto.getHosts());
        serviceProxyDto.setSubsets(serviceProxyUpdateDto.getSubsets());
        serviceProxyDto.setBackendService(serviceProxyUpdateDto.getBackendService());
        return serviceProxyDto;
    }


    @SuppressWarnings("java:S3776")
    private ErrorCode checkSubset(ServiceProxyDto serviceProxyDto) {
        List<SubsetDto> subsetDtos = serviceProxyDto.getSubsets();
        if (subsetDtos == null) {
            return CommonErrorCode.SUCCESS;
        }
        List<String> subsets = subsetDtos.stream().map(SubsetDto::getName).collect(Collectors.toList());
        RouteQuery ruleQuery = RouteQuery.builder().serviceId(serviceProxyDto.getId()).build();
        List<RouteDto> routeRuleList = routeService.getRouteList(ruleQuery);
        for (RouteDto routeDto : routeRuleList) {
            for (ServiceMetaForRouteDto serviceMeta : routeDto.getServiceMetaForRouteDtos()) {
                if (!CollectionUtils.isEmpty(serviceMeta.getDestinationServices())) {
                    for (DestinationDto destinationDto : serviceMeta.getDestinationServices()) {
                        if (StringUtils.isNotBlank(destinationDto.getSubsetName()) &&
                                !subsets.contains(destinationDto.getSubsetName())) {
                            return CommonErrorCode.subsetUsedByRouteRule(routeDto.getName());
                        }
                    }
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<ServiceProxyDto> getServiceProxyListByVirtualGwId(long virtualGwId) {
        return getServiceProxy(ServiceProxyQuery.builder().virtualGwId(virtualGwId).build());
    }


    @Override
    public List<String> getAllServiceTag(long virtualGwId) {
        List<ServiceProxyDto> serviceProxyList = getServiceProxyListByVirtualGwId(virtualGwId);
        if (CollectionUtils.isEmpty(serviceProxyList)) {
            return Collections.emptyList();
        }
        return serviceProxyList.stream().filter(s -> ProjectTraceHolder.getProId() == s.getProjectId())
                .map(ServiceProxyDto::getName).collect(Collectors.toList());
    }

    @Override
    public Set<String> getUniqueHostListFromServiceList(List<ServiceProxyDto> serviceDtoList) {
        Set<String> uniqueHostSet = new HashSet<>();
        for (ServiceProxyDto serviceProxyDto : serviceDtoList) {
            String hosts = serviceProxyDto.getHosts();
            if (StringUtils.isNotEmpty(hosts)) {
                String[] hostArr = hosts.split(",");
                uniqueHostSet.addAll(Arrays.asList(hostArr));
            }
        }
        return uniqueHostSet;
    }

    @Override
    public Set<String> getUniqueHostListFromServiceIdList(List<Long> serviceIdList) {
        List<ServiceProxyDto> serviceByIds = getServiceByIds(serviceIdList);
        if (CollectionUtils.isEmpty(serviceByIds)) {
            return new HashSet<>();
        }
        return getUniqueHostListFromServiceList(serviceByIds);
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
        if (CollectionUtils.isEmpty(subsetDtoList)) {
            return errorCode;
        }
        Set<String> subsetName = subsetDtoList.stream().map(SubsetDto::getName).collect(Collectors.toSet());
        if (subsetName.size() < subsetDtoList.size()) {
            return CommonErrorCode.DUPLICATED_SUBSET_NAME;
        }
        errorCode = checkStaticSubsetWhenPublishService(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)){
            return errorCode;
        }
        for (SubsetDto subsetDto : subsetDtoList) {
            errorCode = checkLabels(subsetDto.getLabels());
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                return errorCode;
            }
            errorCode = checkTrafficPolicy(subsetDto.getTrafficPolicy());
            if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                return errorCode;
            }
        }
        return errorCode;
    }

    public ErrorCode checkStaticSubsetWhenPublishService(ServiceProxyDto serviceProxyDto){
        if (BaseConst.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())){
            return CommonErrorCode.SUCCESS;
        }
        //静态发布的服务，版本中包含的地址要在发布地址列表中
        List<String> addrList = Arrays.asList(serviceProxyDto.getBackendService().split(","));
        //静态发布的服务，版本中静态地址必填
        //静态发布的服务，版本中包含的地址要在发布地址列表中
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        boolean invalidAddr = subsets.stream().anyMatch(s ->
                CollectionUtils.isEmpty(s.getStaticAddrList()) || !addrList.containsAll(s.getStaticAddrList()));
        if (invalidAddr) {
            return CommonErrorCode.INVALID_SUBSET_STATIC_ADDR;
        }
        List<String> allStaticAddrList = subsets.stream().map(SubsetDto::getStaticAddrList).flatMap(Collection::stream).collect(Collectors.toList());
        Set<String> allStaticAddrSet = subsets.stream().map(SubsetDto::getStaticAddrList).flatMap(Collection::stream).collect(Collectors.toSet());
        // 同一个版本里配置的静态地址不能重复
        // 每个地址仅能出现在0或1个版本中
        if (allStaticAddrList.size() != allStaticAddrSet.size()){
            return CommonErrorCode.DUPLICATED_STATIC_ADDR;
        }
        return CommonErrorCode.SUCCESS;
    }

    private static ErrorCode checkLabels(Map<String, String> labels) {
        Pattern keyPattern = Pattern.compile(RegexConst.REGEX_LABEL_KEY);
        Pattern valuePattern = Pattern.compile(RegexConst.REGEX_LABEL_VALUE);
        if (CollectionUtils.isEmpty(labels)) {
            return CommonErrorCode.SUCCESS;
        }
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if (!keyPattern.matcher(entry.getKey()).matches() || !valuePattern.matcher(entry.getValue()).matches()) {
                return CommonErrorCode.INVALID_LABEL;
            }
        }
        return CommonErrorCode.SUCCESS;
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
        //GRPC服务不支持动态发布
        if (ServiceType.grpc.name().equals(serviceProxyDto.getProtocol())){
            return CommonErrorCode.PUBLISH_TYPE_NOT_SUPPORT;
        }
        //Dubbo服务仅支持Zookeeper注册中心
        if (ServiceType.dubbo.name().equals(serviceProxyDto.getProtocol())
                && !serviceProxyDto.getRegistryCenterType().equals(RegistryCenterEnum.Zookeeper.getType())){
            return CommonErrorCode.NotSupportedRegistryType(serviceProxyDto.getProtocol(), serviceProxyDto.getRegistryCenterType());
        }
        //HTTP服务，当注册中心为空时默认Kubernetes
        if (StringUtils.isBlank(serviceProxyDto.getRegistryCenterType())
                && ServiceType.http.name().equals(serviceProxyDto.getProtocol())) {
            //默认Kubernetes
            serviceProxyDto.setRegistryCenterType(RegistryCenterEnum.Kubernetes.getType());
            return CommonErrorCode.SUCCESS;
        }
        return CommonErrorCode.SUCCESS;
    }

}
