package org.hango.cloud.common.infra.route.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.convert.RouteRuleConvert;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.route.dao.IRouteDao;
import org.hango.cloud.common.infra.route.dao.RouteMapper;
import org.hango.cloud.common.infra.route.dto.*;
import org.hango.cloud.common.infra.route.pojo.DestinationInfo;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.URI_TYPE_EXACT;
import static org.hango.cloud.common.infra.base.meta.BaseConst.URI_TYPE_PREFIX;
import static org.hango.cloud.common.infra.base.meta.BaseConst.URI_TYPE_REGEX;
import static org.hango.cloud.common.infra.base.meta.RegexConst.REGEX_MATCH_VALUE;
import static org.hango.cloud.common.infra.base.meta.RegexConst.REGEX_PATH_VALUE;

/**
 * @author xin li
 * @date 2022/9/6 16:59
 */
@Service
public class RouteServiceImpl implements IRouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteServiceImpl.class);
    @Autowired
    private IRouteDao routeDao;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private RouteMapper routeMapper;


    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private ApplicationContext applicationContext;

    private IRouteService routeRuleProxyService;

    @PostConstruct
    public void init() {
        routeRuleProxyService = applicationContext.getBean(RouteServiceImpl.class);
    }

    @Override
    public long create(RouteDto routeDto) {
        //新增路由发布数据
        RoutePO routePO = toMeta(routeDto);
        //h2的情况下需要给id设置未null才能自增
        routePO.setId(null);
        routeMapper.insert(routePO);
        return routePO.getId();
    }

    @Override
    public long update(RouteDto routeDto) {
        RoutePO routePO = toMeta(routeDto);
        return routeMapper.updateById(routePO);
    }

    @Override
    public void delete(RouteDto routeDto) {
        routeMapper.deleteById(routeDto.getId());
    }

    @Override
    public RouteDto get(long id) {
        return toView(routeMapper.selectById(id));
    }

    @Override
    public RouteDto toView(RoutePO routePO) {
        if (routePO == null) {
            return null;
        }
        RouteDto routeDto = new RouteDto();
        routeDto.setCreateTime(routePO.getCreateTime());
        routeDto.setUpdateTime(routePO.getUpdateTime());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routePO.getVirtualGwId());
        routeDto.setVirtualGwId(virtualGatewayDto.getId());
        routeDto.setVirtualGwName(virtualGatewayDto.getName());
        routeDto.setEnvId(virtualGatewayDto.getEnvId());
        routeDto.setGwType(routePO.getGwType());
        routeDto.setId(routePO.getId());
        routeDto.setAlias(routePO.getAlias());
        routeDto.setDescription(routePO.getDescription());
        routeDto.setEnableState(routePO.getEnableState());
        routeDto.setOrders(routePO.getOrders());
        routeDto.setPriority(routePO.getPriority());
        routeDto.setProjectId(routePO.getProjectId());
        routeDto.setName(routePO.getName());
        routeDto.setTimeout(routePO.getTimeout());
        routeDto.setHttpRetryDto(RouteRuleConvert.toView(routePO.getHttpRetry()));

        if (!CollectionUtils.isEmpty(routePO.getDestinationServices())) {
            // 转换路由关联服务信息
            routeDto.setServiceMetaForRouteDtos(getServiceMetaForRouteDtos(routePO));
        }

        // 配置路由关联Hosts（关联服务域名去重）
        routeDto.setHosts(new ArrayList<>(serviceProxyService.getUniqueHostListFromServiceIdList(routeDto.getServiceIds())));

        // 配置流量镜像信息
        configMirrorTraffic(routePO, routeDto);

        // 配置路由匹配信息
        RouteRuleConvert.fillMatchView(routeDto, routePO);

        return routeDto;
    }

    private List<ServiceMetaForRouteDto> getServiceMetaForRouteDtos(RoutePO routePO) {
        List<ServiceMetaForRouteDto> serviceMetaForRouteDtos = new ArrayList<>();

        Map<Long, List<DestinationInfo>> serviceIdToDestinationInfoMap = new HashMap<>();
        for (DestinationInfo destinationInfo : routePO.getDestinationServices()) {
            if (!serviceIdToDestinationInfoMap.containsKey(destinationInfo.getServiceId())) {
                serviceIdToDestinationInfoMap.put(destinationInfo.getServiceId(), new ArrayList<>());
            }
            serviceIdToDestinationInfoMap.get(destinationInfo.getServiceId()).add(destinationInfo);
        }
        serviceIdToDestinationInfoMap.forEach((serviceId, destinationInfoList) ->
                serviceMetaForRouteDtos.add(genServiceMetaFromDestinationAndService(serviceId, destinationInfoList)));
        return serviceMetaForRouteDtos;
    }

    private void configMirrorTraffic(RoutePO routePO, RouteDto routeDto) {
        if (routePO.getMirrorTraffic() != null) {
            ServiceProxyDto serviceDto = serviceProxyService.get(routePO.getMirrorTraffic().getServiceId());
            if (serviceDto != null) {
                routeDto.setMirrorSwitch(1);
                routeDto.setMirrorTraffic(RouteRuleConvert.toView(routePO.getMirrorTraffic()));
                routeDto.getMirrorTraffic().setApplicationName(serviceDto.getBackendService());
            }
        } else {
            routeDto.setMirrorSwitch(0);
        }
    }

    private ServiceMetaForRouteDto genServiceMetaFromDestinationAndService(Long serviceId, List<DestinationInfo> destinationInfoList) {
        ServiceProxyDto serviceDto = serviceProxyService.get(serviceId);
        ServiceMetaForRouteDto serviceMeta = new ServiceMetaForRouteDto();

        DestinationInfo headDestinationInfo = destinationInfoList.get(0);

        // 根据 Destination 对象是否存在 subsetName 判断是主服务还是 subset 服务
        if (StringUtils.isEmpty(headDestinationInfo.getSubsetName())) {
            // 主服务当前版本无subset
            serviceMeta.setDestinationServices(null);
            serviceMeta.setWeight(headDestinationInfo.getWeight());
        } else {
            serviceMeta.setDestinationServices(
                    destinationInfoList.stream().map(RouteRuleConvert::toView).collect(Collectors.toList()));
            // 存在版本配置，则服务本身的权限没有意义，显示100%即可
            serviceMeta.setWeight(100);
        }

        serviceMeta.setServiceName(serviceDto.getName());
        serviceMeta.setServiceSource(serviceDto.getRegistryCenterType());
        serviceMeta.setPort(headDestinationInfo.getPort());
        serviceMeta.setVirtualGateway(serviceDto.getVirtualGwName());
        serviceMeta.setServiceId(serviceId);
        serviceMeta.setProtocol(serviceDto.getProtocol());
        serviceMeta.setBackendService(serviceDto.getBackendService());
        return serviceMeta;
    }

    @Override
    public RoutePO toMeta(RouteDto routeDto) {
        if (routeDto == null) {
            return null;
        }
        RoutePO routePO = new RoutePO();
        routePO.setId(routeDto.getId());
        routePO.setName(routeDto.getName());
        routePO.setAlias(routeDto.getAlias());
        routePO.setDescription(routeDto.getDescription());
        routePO.setCreateTime(routeDto.getCreateTime());
        routePO.setUpdateTime(routeDto.getUpdateTime());
        routePO.setVirtualGwId(routeDto.getVirtualGwId());
        routePO.setGwType(routeDto.getGwType());
        routePO.setProjectId(routeDto.getProjectId());
        routePO.setEnableState(routeDto.getEnableState());
        routePO.setPriority(routeDto.getPriority());
        routePO.setOrders(routeDto.getOrders());
        routePO.setTimeout(routeDto.getTimeout());
        routePO.setServiceIds(routeDto.getServiceIds());
        String gwType = routeDto.getGwType();
        if (StringUtils.isEmpty(gwType)) {
            VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeDto.getVirtualGwId());
            if (virtualGatewayDto != null) {
                gwType = virtualGatewayDto.getGwType();
            }
        }
        routePO.setGwType(gwType);
        //路由重试
        if (routeDto.getHttpRetryDto() != null) {
            routePO.setHttpRetry(RouteRuleConvert.toMeta(routeDto.getHttpRetryDto()));
        }
        //设置流量镜像配置
        if (routeDto.getMirrorSwitch() == 1) {
            routePO.setMirrorServiceId(routeDto.getMirrorTraffic().getServiceId());
            routePO.setMirrorTraffic(RouteRuleConvert.toMeta(routeDto.getMirrorTraffic()));
        } else {
            routePO.setMirrorTraffic(null);
        }
        routePO.setDestinationServices(genDestinationInfoFromRouteServiceMeta(routeDto));
        // 构建匹配信息等其他元数据
        RouteRuleConvert.fillMatchMeta(routePO, routeDto);
        return routePO;
    }

    @Override
    public List<DestinationInfo> genDestinationInfoFromRouteServiceMeta(RouteDto routeDto) {
        List<ServiceMetaForRouteDto> serviceMetaList = routeDto.getServiceMetaForRouteDtos();
        if (CollectionUtils.isEmpty(serviceMetaList)) {
            return new ArrayList<>();
        }
        List<DestinationInfo> destinationInfoList = new ArrayList<>();
        for (ServiceMetaForRouteDto serviceMeta : serviceMetaList) {
            List<DestinationDto> destinationServices = serviceMeta.getDestinationServices();
            // 服务 subset 的内容转换为 Destination
            if (!CollectionUtils.isEmpty(destinationServices)) {
                for (DestinationDto destinationService : destinationServices) {
                    DestinationInfo subsetDestinationInfo = destinationService.toMeta();
                    destinationInfoList.add(subsetDestinationInfo);
                }
            } else {
                // 关联服务的内容转换为 Destination
                DestinationInfo serviceDestinationInfo = new DestinationInfo();
                serviceDestinationInfo.setServiceId(serviceMeta.getServiceId());
                serviceDestinationInfo.setWeight(serviceMeta.getWeight());
                serviceDestinationInfo.setPort(serviceMeta.getPort());
                destinationInfoList.add(serviceDestinationInfo);
            }
        }
        return destinationInfoList;
    }


    @Override
    public ErrorCode checkCreateParam(RouteDto routeDto) {
        if (getRouteByNameInProjectGateway(routeDto.getName(), routeDto.getVirtualGwId(), routeDto.getProjectId()) != null) {
            logger.error("相同项目网关下，已存在同名路由! virtualGwId: {}, projectId: {}, routeName: {}",
                    routeDto.getVirtualGwId(), routeDto.getProjectId(), routeDto.getName());
            return CommonErrorCode.SAME_NAME_ROUTE_EXIST;
        }
        return checkParam(routeDto);
    }

    @Override
    @SuppressWarnings("java:S3776")
    public ErrorCode checkUpdateParam(RouteDto routeDto) {
        if (routeDto.getId() == null) {
            logger.error("校验路由更新规则失败，路由ID为空!");
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }

        RouteDto routeDtoInDb = get(routeDto.getId());
        if (null == routeDtoInDb) {
            logger.error("路由规则更新时指定的路由规则不存在! routeId:{}", routeDto.getId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        List<ServiceMetaForRouteDto> dbServiceMetaForRouteDtos = routeDtoInDb.getServiceMetaForRouteDtos();
        Long serviceId = dbServiceMetaForRouteDtos.get(0).getServiceId();
        ServiceProxyDto dbService = serviceProxyService.get(serviceId);
        if (dbService == null){
            logger.error("路由规则更新时关联服务不存在! routeId:{}, serviceId:{}", routeDto.getId(), serviceId);
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        String dbProtocol = dbService.getProtocol();
        List<ServiceMetaForRouteDto> serviceMetaForRouteDtos = routeDto.getServiceMetaForRouteDtos();
        if (serviceMetaForRouteDtos == null){
            logger.error("路由规则更新时关联服务不存在! routeId:{}", routeDto.getId());
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        for (ServiceMetaForRouteDto serviceMetaForRouteDto : serviceMetaForRouteDtos) {
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(serviceMetaForRouteDto.getServiceId());
            if (serviceProxyDto == null){
                logger.error("路由规则更新时关联服务不存在! routeId:{}, serviceId:{}", routeDto.getId(), serviceMetaForRouteDto.getServiceId());
                return CommonErrorCode.NO_SUCH_SERVICE;
            }
            if (!dbProtocol.equalsIgnoreCase(serviceProxyDto.getProtocol())){
                logger.error("路由规则更新时不允许修改不同协议服务! routeId:{}, serviceId:{}", routeDto.getId(), serviceMetaForRouteDto.getServiceId());
                return CommonErrorCode.ROUTE_SERVICE_PROTOCOL_NOT_SAME;
            }
        }
        return checkParam(routeDto);
    }

    private ErrorCode checkParam(RouteDto routeDto) {
        Long virtualGwId = routeDto.getVirtualGwId();
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGatewayDto) {
            logger.error("动态更新路由时指定的网关不存在! virtualGwId: {}", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        if (CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())) {
            logger.error("发布路由规则时，虚拟网关未绑定项目，不允许发布, virtualGwId: {}", virtualGwId);
            return CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY;
        }
        List<ServiceMetaForRouteDto> serviceMetaList = routeDto.getServiceMetaForRouteDtos();
        if (CollectionUtils.isEmpty(serviceMetaList)) {
            logger.error("路由后端服务信息为空!");
            return CommonErrorCode.MissingParameter("Service");
        }
        ErrorCode matchCheckResult = checkMatchInfo(routeDto);
        if (!matchCheckResult.isSuccess()) {
            logger.error("路由匹配规则不合法!");
            return matchCheckResult;
        }

        boolean sameProtocol = true;
        List<Long> serviceIdList = routeDto.getServiceIds();

        if (serviceIdList.size() != serviceIdList.stream().distinct().count()) {
            // 不允许关联相同的服务
            return CommonErrorCode.ROUTE_SERVICE_SAME;
        }

        String serviceProtocol = serviceProxyService.get(serviceIdList.get(0)).getProtocol();
        // 校验路由关联的所有服务是否存在
        for (Long serviceId : serviceIdList) {
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(serviceId);
            if (!serviceProtocol.equals(serviceProxyDto.getProtocol())) {
                sameProtocol = false;
                break;
            }
            ErrorCode checkResult = checkServiceAndSubsetExist(serviceMetaList, serviceProxyDto);
            if (checkResult == null || !checkResult.isSuccess()) {
                return checkResult;
            }
        }

        // 路由关联服务协议需要一致
        if (!sameProtocol) {
            logger.error("路由关联服务协议不一致!");
            return CommonErrorCode.ROUTE_SERVICE_PROTOCOL_NOT_SAME;
        }

        // 检测关联服务权重合法性
        ErrorCode serviceMetaCheckResult = checkServiceMeta(serviceMetaList);
        if (!serviceMetaCheckResult.isSuccess()) {
            return serviceMetaCheckResult;
        }
        if (existSameRoute(routeDto)) {
            logger.error("发布、更新路由规则，参数完全相同（或关联服务存在交集的域名匹配规则），不允许发布");
            return CommonErrorCode.SAME_PARAM_ROUTE_RULE_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode checkMatchInfo(RouteDto routeDto) {
        if (!checkUri(routeDto.getUriMatchDto())) {
            logger.error("路由path配置不合法!");
            return CommonErrorCode.ROUTE_PATH_INVALID;
        }
        if (!checkRouteMapMatch(routeDto.getHeaders())) {
            logger.error("路由header配置不合法!");
            return CommonErrorCode.ParameterInvalid("header", "header值格式错误");
        }
        if (!checkRouteMapMatch(routeDto.getQueryParams())) {
            logger.error("路由param配置不合法!");
            return CommonErrorCode.ParameterInvalid("param", "param值格式错误");
        }
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 路由path基于不同的匹配类型进行值规范性校验
     *
     * @param uri 路由path匹配组
     * @return 是否合法
     */
    private boolean checkUri(RouteStringMatchDto uri) {
        String type = uri.getType();
        // 元素数量已在入参注解校验，此处校除元素数量外的合法性
        List<String> valueList = uri.getValue();
        if (type.equals(URI_TYPE_EXACT) || type.equals(URI_TYPE_PREFIX)) {
            // 精确和前缀匹配校验规则一致
            for (String value : valueList) {
                if (!value.matches(REGEX_PATH_VALUE)) {
                    logger.error("路由path值校验失败 type: {}, value: {}", type, value);
                    return false;
                }
            }
        } else {
            // 正则类型值校验要求：以"/"开头，长度在100以内
            for (String value : valueList) {
                if (!value.startsWith("/") || value.length() > 100) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验路由请求头和请求参数值格式
     *
     * @param matchDtoList 路由匹配条件组
     * @return 是否符合条件
     */
    private boolean checkRouteMapMatch(List<RouteMapMatchDto> matchDtoList) {
        if (CollectionUtils.isEmpty(matchDtoList)) {
            return true;
        }
        for (RouteMapMatchDto routeMapMatchDto : matchDtoList) {
            String type = routeMapMatchDto.getType();
            List<String> valueList = routeMapMatchDto.getValue();
            switch (type) {
                case URI_TYPE_EXACT:
                case URI_TYPE_PREFIX:
                    // 存在任意一个不符合的匹配值则进行中断返回
                    if (valueList.stream().anyMatch(value -> !value.matches(REGEX_MATCH_VALUE))) {
                        return false;
                    }
                    break;
                case URI_TYPE_REGEX:
                    // 正则场景没有字符约束，长度小于100
                    if (valueList.stream().anyMatch(value -> value.length() > 100)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @SuppressWarnings("java:S3776")
    private ErrorCode checkServiceAndSubsetExist(List<ServiceMetaForRouteDto> serviceMetaList, ServiceProxyDto serviceProxyDto) {
        if (serviceProxyDto == null) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        for (ServiceMetaForRouteDto serviceMeta : serviceMetaList) {
            if (!serviceMeta.getServiceId().equals(serviceProxyDto.getId())) {
                continue;
            }

            // 校验版本信息
            List<DestinationDto> subsetList = serviceMeta.getDestinationServices();
            if (!CollectionUtils.isEmpty(subsetList)) {
                List<String> subsetNameListFromConsole = subsetList.stream().map(DestinationDto::getSubsetName).collect(Collectors.toList());
                if (subsetNameListFromConsole.size() != subsetNameListFromConsole.stream().distinct().count()) {
                    // 不允许关联相同的服务subset
                    return CommonErrorCode.ROUTE_SERVICE_SUBSET_SAME;
                }

                List<SubsetDto> subsets = serviceProxyDto.getSubsets();
                if (CollectionUtils.isEmpty(subsets)) {
                    logger.error("该服务下无可关联的版本信息! serviceId: {}", serviceProxyDto.getId());
                    return CommonErrorCode.NO_SUBSET_OF_SERVICE;
                }
                List<String> subsetNameList = subsets.stream().map(SubsetDto::getName).collect(Collectors.toList());
                for (DestinationDto destinationDto : serviceMeta.getDestinationServices()) {
                    if (StringUtils.isNotBlank(destinationDto.getSubsetName()) && !subsetNameList.contains(destinationDto.getSubsetName())) {
                        return CommonErrorCode.INVALID_SUBSET_NAME;
                    }
                }
            }
            break;
        }
        return CommonErrorCode.SUCCESS;
    }

    private static ErrorCode checkServiceMeta(List<ServiceMetaForRouteDto> serviceMetaList) {
        int serviceTotalWeight = 0;
        for (ServiceMetaForRouteDto serviceMeta : serviceMetaList) {
            ErrorCode subsetCheckResult = checkServiceSubset(serviceMeta);
            if (!subsetCheckResult.isSuccess()) {
                return subsetCheckResult;
            }
            serviceTotalWeight += serviceMeta.getWeight();
            logger.info("[weight] service TotalWeight: {}, serviceId: {}, weight: {}",
                    serviceTotalWeight, serviceMeta.getServiceId(), serviceMeta.getWeight());
        }

        if (serviceTotalWeight != 100) {
            logger.error("创建路由流程，关联服务设置的总权重不为100！");
            return CommonErrorCode.INVALID_TOTAL_WEIGHT;
        }
        return CommonErrorCode.SUCCESS;
    }

    private static ErrorCode checkServiceSubset(ServiceMetaForRouteDto serviceMeta) {
        if (!CollectionUtils.isEmpty(serviceMeta.getDestinationServices())) {
            int subsetTotalWeight = 0;
            for (DestinationDto destinationService : serviceMeta.getDestinationServices()) {
                subsetTotalWeight += destinationService.getWeight();
                logger.info("[weight] subset TotalWeight: {}, serviceId: {}, weight: {}",
                        subsetTotalWeight, serviceMeta.getServiceId(), destinationService.getWeight());
            }

            if (subsetTotalWeight != 100) {
                logger.error("创建路由流程，关联服务版本分流设置的权重总和不为100！");
                return CommonErrorCode.INVALID_TOTAL_WEIGHT;
            }
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    @SuppressWarnings("java:S3776")
    public ErrorCode checkUpdateMirrorTrafficParam(RouteMirrorDto routeMirrorDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeMirrorDto.getVirtualGwId());
        if (null == virtualGatewayDto) {
            logger.info("动态更新路由时指定的网关不存在! virtualGwId:{}", routeMirrorDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }

        RouteDto routeDto = get(routeMirrorDto.getRouteId());
        if (null == routeDto) {
            logger.info("动态更新路由指定的路由规则不存在! routeId:{}", routeMirrorDto.getRouteId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        int mirrorSwitch = routeMirrorDto.getMirrorSwitch();
        // 流量镜像开关: 0: 关闭；1: 开启
        if (mirrorSwitch == 0) {
            // 关闭状态无需校验流量镜像配置
            return CommonErrorCode.SUCCESS;
        }

        return checkMirrorConfig(routeMirrorDto, routeDto);
    }

    @NotNull
    private ErrorCode checkMirrorConfig(RouteMirrorDto routeMirrorDto, RouteDto routeDto) {
        DestinationDto mirrorTraffic = routeMirrorDto.getMirrorTraffic();
        if (mirrorTraffic == null) {
            logger.error("流量镜像配置不存在");
            return CommonErrorCode.MissingParameter("mirrorTraffic");
        }

        if (routeDto.getServiceIds().contains(mirrorTraffic.getServiceId())) {
            logger.error("流量镜像无法配置于本路由已关联的服务");
            return CommonErrorCode.SAME_SERVICE_WHEN_PUBLISH_MIRROR;
        }

        ServiceProxyDto serviceProxyDto = serviceProxyService.get(mirrorTraffic.getServiceId());
        if (null == serviceProxyDto) {
            logger.info("流量镜像指定服务未发布！serviceId:{}, virtualGwId:{}",
                    mirrorTraffic.getServiceId(), routeMirrorDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_SERVICE;
        }

        if (StringUtils.isEmpty(mirrorTraffic.getSubsetName())) {
            return CommonErrorCode.SUCCESS;
        }

        if (!CollectionUtils.isEmpty(serviceProxyDto.getSubsets())) {
            List<SubsetDto> subsetDtoList = serviceProxyDto.getSubsets();
            List<SubsetDto> collect = subsetDtoList.stream()
                    .filter(subsetDto -> subsetDto.getName().equals(mirrorTraffic.getSubsetName()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                logger.error("流量镜像指定服务版本配置错误");
                return CommonErrorCode.INVALID_SUBSET_NAME;
            }
        } else {
            logger.error("流量镜像指定服务版本配置不存在");
            return CommonErrorCode.NO_SUBSET_OF_SERVICE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public long publishMirrorTraffic(RouteMirrorDto routeMirrorDto) {
        RouteDto dbProxyDto = getRoute(routeMirrorDto.getVirtualGwId(), routeMirrorDto.getRouteId());
        dbProxyDto.setMirrorSwitch(routeMirrorDto.getMirrorSwitch());
        dbProxyDto.setMirrorTraffic(routeMirrorDto.getMirrorTraffic());
        return routeRuleProxyService.update(dbProxyDto);
    }


    @Override
    public Page<RoutePO> getRoutePage(RouteQueryDto queryDto) {
        RouteQuery query = RouteRuleConvert.toMeta(queryDto);
        // 没有分页信息进行全量查询
        if (queryDto.getLimit() == null || queryDto.getOffset() == null) {
            List<RoutePO> routeList = routeDao.getRouteList(query);
            Page<RoutePO> routePage = new Page<>();
            routePage.setTotal(routeList.size());
            routePage.setRecords(routeList);
            return routePage;
        }
        return routeDao.getRoutePage(query, PageUtil.of(queryDto.getLimit(), queryDto.getOffset()));
    }

    @Override
    public List<RouteDto> getRouteList(RouteQuery queryDto) {
        List<RoutePO> routeRuleProxyList = routeDao.getRouteList(queryDto);
        return routeRuleProxyList.stream().map(this::toView).collect(Collectors.toList());
    }

    private boolean existSameRoute(RouteDto routeDto) {
        RoutePO routePO = toMeta(routeDto);
        Set<String> hostListOfNewRoute = serviceProxyService.getUniqueHostListFromServiceIdList(routeDto.getServiceIds());
        Set<String> hostListOfDbRoute = new HashSet<>();
        List<RoutePO> routePOList = routeDao.getRuleListByMatchInfo(routePO);
        // 同项目通网关下，除去自身的相同匹配条件的路由集合
        List<RoutePO> routePOListWithSameMatchExceptSelf = new ArrayList<>();
        for (RoutePO route : routePOList) {
            if (route.getId() != null && route.getId().equals(routeDto.getId())) {
                // 跳过自身（创建阶段的路由没有ID，因此需要判空）
                continue;
            }
            hostListOfDbRoute.addAll(serviceProxyService.getUniqueHostListFromServiceIdList(route.getServiceIds()));
            routePOListWithSameMatchExceptSelf.add(route);
        }
        if (!CollectionUtils.isEmpty(routePOListWithSameMatchExceptSelf)) {
            // host集合取交集，若存在交集host，则代表存在相同类型路由（同项目通网关下path\method\query都相同的前提下再进行host比较）
            hostListOfNewRoute.retainAll(hostListOfDbRoute);
            // 此处返回原因：项目网关下存在相同的匹配条件的路由，且他们关联服务的Host存在交集，则判定为相同的路由
            return !hostListOfNewRoute.isEmpty();
        }
        return false;
    }

    @Override
    public RouteDto getRoute(long virtualGwId, long id) {
        RoutePO query = RoutePO.builder()
                .virtualGwId(virtualGwId)
                .id(id).build();
        RoutePO routePO = routeMapper.selectOne(new QueryWrapper<>(query));
        return toView(routePO);
    }

    @Override
    public RouteDto getRouteByNameInProjectGateway(String routeName, long virtualGwId, long projectId) {
        RoutePO query = RoutePO.builder()
                .name(routeName)
                .virtualGwId(virtualGwId)
                .projectId(projectId)
                .build();
        RoutePO routePO = routeMapper.selectOne(new QueryWrapper<>(query));
        return toView(routePO);
    }


    @Override
    public List<RouteDto> getRouteById(long routeRuleId) {
        RouteQuery query = RouteQuery.builder().routeIds(Collections.singletonList(routeRuleId)).build();
        return getRouteList(query);
    }

    @Override
    @SuppressWarnings({"java:S3776"})
    public ErrorCode fillRouteInfo(RouteDto routeDto) {
        routeDto.setProjectId(ProjectTraceHolder.getProId());
        routeDto.setOrders(RouteRuleConvert.calOrders(routeDto));

        List<ServiceMetaForRouteDto> serviceMetaList = routeDto.getServiceMetaForRouteDtos();
        if (CollectionUtils.isEmpty(serviceMetaList)) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }

        for (ServiceMetaForRouteDto serviceMeta : serviceMetaList) {
            // 服务端口配置
            if (serviceMeta.getPort() == null) {
                serviceMeta.setPort(80);
            }
            if (!CommonUtil.isValidPort(serviceMeta.getPort())) {
                return CommonErrorCode.INVALID_SERVICE_PORT;
            }

            // 服务子版本端口配置
            List<DestinationDto> destinationServices = serviceMeta.getDestinationServices();
            if (CollectionUtils.isEmpty(destinationServices)) {
                continue;
            }
            for (DestinationDto subset : destinationServices) {
                if (subset.getPort() == null) {
                    subset.setPort(80);
                }
                if (!CommonUtil.isValidPort(subset.getPort())) {
                    return CommonErrorCode.INVALID_SERVICE_SUBSET_PORT;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public RouteDto fillUpdateInfo(UpdateRouteDto updateRouteDto) {
        RouteDto dbRoute = get(updateRouteDto.getId());
        if (dbRoute == null){
            dbRoute = new RouteDto();
        }
        dbRoute.setId(updateRouteDto.getId());
        dbRoute.setAlias(updateRouteDto.getAlias());
        dbRoute.setServiceMetaForRouteDtos(updateRouteDto.getServiceMetaForRouteDtos());
        dbRoute.setEnableState(updateRouteDto.getEnableState());
        dbRoute.setTimeout(updateRouteDto.getTimeout());
        dbRoute.setHttpRetryDto(updateRouteDto.getHttpRetryDto());
        dbRoute.setDescription(updateRouteDto.getDescription());
        dbRoute.setUriMatchDto(updateRouteDto.getUriMatchDto());
        dbRoute.setMethod(updateRouteDto.getMethod());
        dbRoute.setHeaders(updateRouteDto.getHeaders());
        dbRoute.setQueryParams(updateRouteDto.getQueryParams());
        dbRoute.setPriority(updateRouteDto.getPriority());
        dbRoute.setHosts(null);
        return dbRoute;
    }

    @Override
    public ErrorCode checkDeleteParam(RouteDto routeDto) {
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode fillRouteMirrorDto(RouteMirrorDto routeMirrorDto) {
        DestinationDto mirrorTraffic = routeMirrorDto.getMirrorTraffic();
        if (mirrorTraffic == null) {
            return CommonErrorCode.SUCCESS;
        }

        if (mirrorTraffic.getPort() == null) {
            mirrorTraffic.setPort(80);
        }
        if (!CommonUtil.isValidPort(mirrorTraffic.getPort())) {
            return CommonErrorCode.INVALID_SERVICE_PORT;
        }

        return CommonErrorCode.SUCCESS;
    }
}
