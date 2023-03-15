package org.hango.cloud.common.infra.routeproxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.convert.RouteRuleConvert;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.base.util.ValidityUtil;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.route.common.DestinationInfo;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dao.IRouteRuleProxyDao;
import org.hango.cloud.common.infra.routeproxy.dto.RouteMirrorDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xin li
 * @date 2022/9/6 16:59
 */
@Service
public class RouteRuleProxyServiceImpl implements IRouteRuleProxyService {

    private static final Logger logger = LoggerFactory.getLogger(RouteRuleProxyServiceImpl.class);
    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private RouteRuleProxyMapper routeRuleProxyMapper;


    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private ApplicationContext applicationContext;

    private IRouteRuleProxyService routeRuleProxyService;

    @PostConstruct
    public void init(){
        routeRuleProxyService = applicationContext.getBean(RouteRuleProxyServiceImpl.class);
    }

    @Override
    public long create(RouteRuleProxyDto routeRuleProxyDto) {
        //新增路由发布数据
        RouteRuleProxyPO routeRuleProxyPO = toMeta(routeRuleProxyDto);
        //h2的情况下需要给id设置未null才能自增
        routeRuleProxyPO.setId(null);
        routeRuleProxyMapper.insert(routeRuleProxyPO);
        //更新路由发布状态
        routeRuleInfoService.updatePublishStatus(routeRuleProxyPO.getRouteRuleId(), NumberUtils.INTEGER_ONE);
        return routeRuleProxyPO.getId();
    }

    @Override
    public long update(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleProxyPO routeRuleProxyPO = toMeta(routeRuleProxyDto);
        return routeRuleProxyMapper.updateById(routeRuleProxyPO);
    }

    @Override
    public long updateRouteProxy(RouteRuleProxyDto routeRuleProxyDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeRuleProxyDto.getVirtualGwId());
        List<String> hosts = domainInfoService.getEnableHosts(routeRuleProxyDto.getProjectId(), routeRuleProxyDto.getVirtualGwId());
        routeRuleProxyDto.setHosts(hosts);
        routeRuleProxyDto.setGwType(virtualGatewayDto.getGwType());
        return routeRuleProxyService.update(routeRuleProxyDto);
    }


    @Override
    public void delete(RouteRuleProxyDto routeRuleProxyDto) {
        routeRuleProxyMapper.deleteById(routeRuleProxyDto.getId());
        routeRuleInfoService.updatePublishStatus(routeRuleProxyDto.getRouteRuleId(), NumberUtils.INTEGER_ZERO);
    }

    @Override
    public RouteRuleProxyDto get(long id) {
        return toView(routeRuleProxyMapper.selectById(id));
    }

    @Override
    public RouteRuleProxyDto toView(RouteRuleProxyPO routeRuleProxyInfo) {
        if (routeRuleProxyInfo == null){
            return null;
        }
        RouteRuleProxyDto routeRuleProxyDto = new RouteRuleProxyDto();
        routeRuleProxyDto.setCreateTime(routeRuleProxyInfo.getCreateTime());
        routeRuleProxyDto.setUpdateTime(routeRuleProxyInfo.getUpdateTime());
        routeRuleProxyDto.setVirtualGwId(routeRuleProxyInfo.getVirtualGwId());
        routeRuleProxyDto.setGwType(routeRuleProxyInfo.getGwType());
        routeRuleProxyDto.setId(routeRuleProxyInfo.getId());
        routeRuleProxyDto.setRouteRuleId(routeRuleProxyInfo.getRouteRuleId());
        routeRuleProxyDto.setEnableState(routeRuleProxyInfo.getEnableState());
        routeRuleProxyDto.setProjectId(routeRuleProxyInfo.getProjectId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeRuleProxyInfo.getVirtualGwId());
        if (virtualGatewayDto != null) {
            routeRuleProxyDto.setGwName(virtualGatewayDto.getName());
            routeRuleProxyDto.setGwAddr(virtualGatewayDto.getAddr());
        }
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(routeRuleProxyInfo.getRouteRuleId());
        if (routeRuleDto == null) {
            logger.info("路由元数据不存在，存在脏数据");
            return null;
        }
        ServiceDto serviceDto = serviceInfoService.get(routeRuleProxyInfo.getServiceId());
        ServiceProxyDto proxyByServiceIdAndGwId = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeRuleProxyInfo.getVirtualGwId(), routeRuleProxyInfo.getServiceId());
        routeRuleProxyDto.setRouteRuleName(routeRuleDto.getRouteRuleName());
        routeRuleProxyDto.setServiceId(routeRuleDto.getServiceId());
        routeRuleProxyDto.setServiceName(serviceDto.getDisplayName());
        List<DestinationDto> destinationDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(routeRuleProxyInfo.getDestinationServices())) {
            //修改为当前for循环，减少数据库查询压力
            for (DestinationInfo info : routeRuleProxyInfo.getDestinationServices()) {
                DestinationDto destinationDto = RouteRuleConvert.toView(info);
                destinationDto.setApplicationName(proxyByServiceIdAndGwId.getBackendService());
                destinationDtos.add(destinationDto);
            }
            routeRuleProxyDto.setDestinationServices(destinationDtos);
        }
        routeRuleProxyDto.setServiceType(serviceDto.getServiceType());
        routeRuleProxyDto.setHosts(routeRuleProxyInfo.getHosts());


        routeRuleProxyDto.setTimeout(routeRuleProxyInfo.getTimeout());
        routeRuleProxyDto.setHttpRetryDto(RouteRuleConvert.toView(routeRuleProxyInfo.getHttpRetry()));

        //流量镜像配置
        if (routeRuleProxyInfo.getMirrorTraffic() != null) {
            routeRuleProxyDto.setMirrorSwitch(1);
            ServiceProxyDto proxyService = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeRuleProxyInfo.getVirtualGwId(), routeRuleProxyInfo.getMirrorTraffic().getServiceId());
            if (proxyService != null) {
                routeRuleProxyDto.setMirrorTraffic(RouteRuleConvert.toView(routeRuleProxyInfo.getMirrorTraffic()));
                routeRuleProxyDto.getMirrorTraffic().setApplicationName(proxyService.getBackendService());
            }
        } else {
            routeRuleProxyDto.setMirrorSwitch(0);
        }
        RouteRuleConvert.fillMatchView(routeRuleProxyDto, routeRuleProxyInfo);
        return routeRuleProxyDto;
    }

    @Override
    public RouteRuleProxyPO toMeta(RouteRuleProxyDto routeRuleProxyDto) {
        if (routeRuleProxyDto == null){
            return null;
        }
        RouteRuleProxyPO routeRuleProxyPO = new RouteRuleProxyPO();
        routeRuleProxyPO.setId(routeRuleProxyDto.getId());
        routeRuleProxyPO.setRouteRuleId(routeRuleProxyDto.getRouteRuleId());
        routeRuleProxyPO.setVirtualGwId(routeRuleProxyDto.getVirtualGwId());
        routeRuleProxyPO.setGwType(routeRuleProxyDto.getGwType());
        routeRuleProxyPO.setServiceId(routeRuleProxyDto.getServiceId());
        routeRuleProxyPO.setProjectId(routeRuleProxyDto.getProjectId());
        routeRuleProxyPO.setEnableState(routeRuleProxyDto.getEnableState());
        routeRuleProxyPO.setTimeout(routeRuleProxyDto.getTimeout());
        routeRuleProxyPO.setHosts(routeRuleProxyDto.getHosts());
        String gwType = routeRuleProxyDto.getGwType();
        if (StringUtils.isEmpty(gwType)){
            VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeRuleProxyDto.getVirtualGwId());
            if (virtualGatewayDto != null){
                gwType = virtualGatewayDto.getGwType();
            }
        }
        routeRuleProxyPO.setGwType(gwType);
        //路由重试
        if (routeRuleProxyDto.getHttpRetryDto() != null) {
            routeRuleProxyPO.setHttpRetry(RouteRuleConvert.toMeta(routeRuleProxyDto.getHttpRetryDto()));
        }
        //设置流量镜像配置
        if (routeRuleProxyDto.getMirrorSwitch() == 1) {
            routeRuleProxyPO.setMirrorServiceId(routeRuleProxyDto.getMirrorTraffic().getServiceId());
            routeRuleProxyPO.setMirrorTraffic(RouteRuleConvert.toMeta(routeRuleProxyDto.getMirrorTraffic()));
        }else {
            routeRuleProxyPO.setMirrorTraffic(null);
        }
        //路由发布，构造routeProxy
        ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeRuleProxyDto.getVirtualGwId(), routeRuleProxyDto.getServiceId());
        List<DestinationInfo> destinationInfos = routeRuleProxyDto.getDestinationServices().stream().map(DestinationDto::toMeta).collect(Collectors.toList());
        if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            //静态发布将端口设置为80
            destinationInfos.forEach(destinationInfo -> {
                destinationInfo.setPort(80);
            });
        }
        routeRuleProxyPO.setDestinationServices(destinationInfos);
        /**
         * 构建匹配信息
         */
        RouteRuleConvert.fillMatchMeta(routeRuleProxyPO, routeRuleProxyDto);
        return routeRuleProxyPO;
    }


    @Override
    public ErrorCode checkCreateParam(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleProxyDto dbProxyDto = getRouteRuleProxy(routeRuleProxyDto.getVirtualGwId(), routeRuleProxyDto.getRouteRuleId());
        if (dbProxyDto != null) {
            logger.info("路由规则已经发布到该网关，不允许重复发布");
            return CommonErrorCode.routeRuleAlreadyPublished(routeRuleProxyDto.getRouteRuleName());
        }
        return checkParam(routeRuleProxyDto);
    }

    @Override
    public ErrorCode checkUpdateParam(RouteRuleProxyDto routeRuleProxyDto) {
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(routeRuleProxyDto.getRouteRuleId());
        if (null == routeRuleDto) {
            logger.info("路由规则发布时指定的路由规则不存在! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        return checkParam(routeRuleProxyDto);
    }

    private ErrorCode checkParam(RouteRuleProxyDto routeRuleProxyDto){
        Long virtualGwId = routeRuleProxyDto.getVirtualGwId();
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGatewayDto) {
            logger.info("动态更新路由时指定的网关不存在! virtualGwId:{}", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        if (CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())){
            logger.info("发布路由规则时，虚拟网关未绑定项目，不允许发布, virtualGwId:{}", virtualGwId);
            return CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY;
        }
        ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(virtualGwId, routeRuleProxyDto.getServiceId());
        if (null == serviceProxyDto) {
            logger.info("路由规则发布时所属的服务未发布！serviceId:{}, gwId:{}", routeRuleProxyDto.getServiceId(), virtualGwId);
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }
        List<DestinationDto> destinationServices = routeRuleProxyDto.getDestinationServices();
        if (CollectionUtils.isEmpty(destinationServices)) {
            logger.info("动态更新路由指定的后端服务为空!");
            return CommonErrorCode.MissingParameter("ProxyServices");
        }
        //动态发布，必须填端口
        if (BaseConst.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            int totalweight = 0;
            for (DestinationDto destinationService : destinationServices) {
                //发布路由规则，后端服务id不正确，之后版本等也在这里校验
                if (routeRuleProxyDto.getServiceId() != destinationService.getServiceId()) {
                    logger.info("路由规则发布时，后端服务id不正确");
                    return CommonErrorCode.NO_SUCH_SERVICE;
                }
                if (destinationService.getWeight() < 0 || destinationService.getWeight() > 100) {
                    logger.info("路由规则发布时，设置的权重不合法: {}", destinationService.getWeight());
                    return CommonErrorCode.invalidParameter(String.valueOf(destinationService.getWeight()), "Weight");
                }
                totalweight += destinationService.getWeight();
            }

            if (totalweight != 100) {
                return CommonErrorCode.INVALID_TOTAL_WEIGHT;
            }
        }
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        if (!CollectionUtils.isEmpty(subsets)){
            List<String> subsetNames = subsets.stream().map(SubsetDto::getName).collect(Collectors.toList());
            for (DestinationDto envoyDestinationDto : routeRuleProxyDto.getDestinationServices()) {
                if (StringUtils.isNotBlank(envoyDestinationDto.getSubsetName()) && !subsetNames.contains(envoyDestinationDto.getSubsetName())) {
                    return CommonErrorCode.INVALID_SUBSET_NAME;
                }
            }
        }
        if (existSameRouteRuleProxyInfo(routeRuleProxyDto)) {
            logger.error("发布路由规则，参数完全相同，不允许发布");
            return CommonErrorCode.SAME_PARAM_ROUTE_RULE_EXIST;
        }
        List<String> hosts = domainInfoService.getEnableHosts(routeRuleProxyDto.getProjectId(), routeRuleProxyDto.getVirtualGwId());
        if (CollectionUtils.isEmpty(hosts)) {
            logger.info("发布路由域名为空，禁止发布");
            return CommonErrorCode.NO_SUCH_DOMAIN;
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public ErrorCode checkUpdateMirrorTrafficParam(RouteMirrorDto routeMirrorDto) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(routeMirrorDto.getVirtualGwId());
        if (null == virtualGatewayDto) {
            logger.info("动态更新路由时指定的网关不存在! virtualGwId:{}", routeMirrorDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }

        RouteRuleDto routeRuleDto = routeRuleInfoService.get(routeMirrorDto.getRouteRuleId());
        if (null == routeRuleDto) {
            logger.info("动态更新路由指定的路由规则不存在! routeRuleId:{}", routeMirrorDto.getRouteRuleId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        int mirrorSwitch = routeMirrorDto.getMirrorSwitch();
        if (mirrorSwitch == 1) {
            DestinationDto mirrorTraffic = routeMirrorDto.getMirrorTraffic();
            if (mirrorTraffic == null) {
                logger.info("流量镜像配置不存在");
                return CommonErrorCode.MissingParameter("mirrorTraffic");
            }

            if (!ValidityUtil.vaildPort(mirrorTraffic.getPort())) {
                logger.info("端口设置错误");
                return CommonErrorCode.invalidParameterValue(mirrorTraffic.getPort(), "流量镜像端口");
            }

            ServiceProxyDto serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeMirrorDto.getVirtualGwId(), mirrorTraffic.getServiceId());
            if (null == serviceProxyInfo) {
                logger.info("流量镜像指定服务未发布！serviceId:{}, virtualGwId:{}", mirrorTraffic.getServiceId(), routeMirrorDto.getVirtualGwId());
                return CommonErrorCode.SERVICE_NOT_PUBLISHED;
            }

            if (StringUtils.isNotEmpty(mirrorTraffic.getSubsetName())) {
                if (!CollectionUtils.isEmpty(serviceProxyInfo.getSubsets())) {
                    List<SubsetDto> subsetDtoObjectList = serviceProxyInfo.getSubsets();
                    List<SubsetDto> collect = subsetDtoObjectList.stream().filter(subsetDto -> subsetDto.getName().equals(mirrorTraffic.getSubsetName())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(collect)) {
                        logger.info("流量镜像指定服务版本配置错误");
                        return CommonErrorCode.INVALID_SUBSET_NAME;
                    }
                } else {
                    logger.info("流量镜像指定服务版本配置错误");
                    return CommonErrorCode.INVALID_SUBSET_NAME;
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public long publishMirrorTraffic(RouteMirrorDto routeMirrorDto) {
        RouteRuleProxyDto dbProxyDto = getRouteRuleProxy(routeMirrorDto.getVirtualGwId(), routeMirrorDto.getRouteRuleId());
        dbProxyDto.setMirrorSwitch(routeMirrorDto.getMirrorSwitch());
        dbProxyDto.setMirrorTraffic(routeMirrorDto.getMirrorTraffic());
        return routeRuleProxyService.update(dbProxyDto);
    }



    @Override
    public Page<RouteRuleProxyPO> getRouteRuleProxyPage(RouteRuleQueryDto queryDto) {
        RouteRuleQuery query = RouteRuleConvert.toMeta(queryDto);
        boolean success = preHandle(query);
        if (!success){
            return Page.of(0,0,0);
        }

        return routeRuleProxyDao.getRouteRuleProxyPage(query, PageUtil.of(queryDto.getLimit(), queryDto.getOffset()));
    }

    @Override
    public List<RouteRuleProxyDto> getRouteRuleProxyList(RouteRuleQuery queryDto) {
        boolean success = preHandle(queryDto);
        if (!success){
            return new ArrayList<>();
        }
        List<RouteRuleProxyPO> routeRuleProxyList = routeRuleProxyDao.getRouteRuleProxyList(queryDto);
        return routeRuleProxyList.stream().map(this::toView).collect(Collectors.toList());
    }


    private boolean preHandle(RouteRuleQuery query){
        if (StringUtils.isBlank(query.getPattern())){
            return true;
        }
        List<Long> routeIds = routeRuleInfoService.getRouteRuleList(query).stream().map(RouteRuleInfoPO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(routeIds)){
            return false;
        }
        query.setRouteRuleIds(routeIds);
        return true;
    }

    private boolean existSameRouteRuleProxyInfo(RouteRuleProxyDto proxyDto) {
        RouteRuleProxyPO routeRuleProxyPO = toMeta(proxyDto);
        List<RouteRuleProxyPO> routeRuleProxyPOS = routeRuleProxyDao.getRuleProxyListByMatchInfo(routeRuleProxyPO).stream()
                .filter(o -> !o.getId().equals(proxyDto.getId())).collect(Collectors.toList());
        return !CollectionUtils.isEmpty(routeRuleProxyPOS);
    }


    @Override
    public RouteRuleProxyDto getRouteRuleProxy(long virtualGwId, long routeRuleId) {
        RouteRuleProxyPO query = RouteRuleProxyPO.builder()
                .virtualGwId(virtualGwId)
                .routeRuleId(routeRuleId).build();
        RouteRuleProxyPO routeRuleProxyPO = routeRuleProxyMapper.selectOne(new QueryWrapper<>(query));
        return toView(routeRuleProxyPO);
    }


    @Override
    public ErrorCode checkDeleteParam(RouteRuleProxyDto routeRuleProxyDto) {
        long virtualGwId = routeRuleProxyDto.getVirtualGwId();
        long routeRuleId = routeRuleProxyDto.getRouteRuleId();
        List<Long> serviceIds = (List<Long>) routeRuleProxyDto.getExtension();
        RouteRuleProxyDto dbProxyDto = getRouteRuleProxy(virtualGwId, routeRuleId);
        if (null == dbProxyDto) {
            logger.info("下线路由规则，路由规则未发布");
            return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
        }
        if (!CollectionUtils.isEmpty(serviceIds)) {
            List<DestinationDto> destinationServiceList = dbProxyDto.getDestinationServices();
            List<Long> desServiceIdList = destinationServiceList.stream().map(DestinationDto::getServiceId).collect(Collectors.toList());
            for (Long serviceId : serviceIds) {
                if (!desServiceIdList.contains(serviceId)) {
                    return CommonErrorCode.ROUTE_RULE_SERVICE_NOT_MATCH;
                }
            }
        }

        return CommonErrorCode.SUCCESS;
    }


    @Override
    public List<RouteRuleProxyDto> getRouteRuleProxyByRouteRuleId(long routeRuleId) {
        RouteRuleQuery query = RouteRuleQuery.builder().routeRuleIds(Collections.singletonList(routeRuleId)).build();
        return getRouteRuleProxyList(query);
    }

    @Override
    public ErrorCode fillRouteRuleProxy(RouteRuleProxyDto proxyDto) {
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(proxyDto.getRouteRuleId());
        if (routeRuleDto == null){
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        if (proxyDto.getUriMatchDto() == null){
            proxyDto.setUriMatchDto(routeRuleDto.getUriMatchDto());
        }
        if (proxyDto.getServiceId() == 0){
            proxyDto.setServiceId(routeRuleDto.getServiceId());
        }
        proxyDto.setRouteRuleName(routeRuleDto.getRouteRuleName());
        proxyDto.setProjectId(ProjectTraceHolder.getProId());
        proxyDto.setOrders(RouteRuleConvert.calOrders(proxyDto));
        return CommonErrorCode.SUCCESS;
    }
}
