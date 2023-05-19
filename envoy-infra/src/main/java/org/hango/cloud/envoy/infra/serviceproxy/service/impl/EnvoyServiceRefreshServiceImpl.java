package org.hango.cloud.envoy.infra.serviceproxy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.route.service.IEnvoyRouteService;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ResultDTO;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ServiceRefreshDTO;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceRefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/4/17
 */
@Slf4j
@Service
public class EnvoyServiceRefreshServiceImpl implements IEnvoyServiceRefreshService {

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IEnvoyRouteService envoyRouteService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;



    @Override
    public ResultDTO refreshServiceHost(ServiceRefreshDTO refreshDTO) {
        List<Long> serviceIds = refreshDTO.getServiceIds();

        List<String> successName = new ArrayList<>();
        List<String> errorName = new ArrayList<>();
        List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceByIds(serviceIds);
        for (ServiceProxyDto serviceProxyDto : serviceProxyDtos) {
            try {
                doRefreshServiceHost(serviceProxyDto, refreshDTO, errorName, successName);
            }catch (Exception e){
                log.error("刷新服务异常, serviceName:{}", serviceProxyDto.getName(), e);
                errorName.add(serviceProxyDto.getName());
            }

        }
        return ResultDTO.of(serviceIds.size(), successName.size(), errorName);
    }

    private void doRefreshServiceHost(ServiceProxyDto serviceProxyDto, ServiceRefreshDTO refreshDTO, List<String> errorName, List<String> successName){
        Set<String> hosts = CommonUtil.splitStringToStringSet(serviceProxyDto.getHosts(), ",");
        //域名不能为空，刷新失败
        Set<String> targetHost = getTargetHost(hosts, refreshDTO);
        if (CollectionUtils.isEmpty(targetHost)){
            log.error("refresh service host error, host is null, name:{}", serviceProxyDto.getName());
            errorName.add(serviceProxyDto.getName());
            return;
        }
        //域名相同不需要刷新
        if (CommonUtil.equal(targetHost, hosts)){
            return;
        }
        //刷新路由
        Boolean result = refreshRoute(serviceProxyDto.getVirtualGwId(), serviceProxyDto.getId(), targetHost);
        //路由刷新失败
        if (Boolean.FALSE.equals(result)){
            errorName.add(serviceProxyDto.getName());
            return;
        }
        //更新服务域名
        result = serviceProxyService.updateServiceHost(serviceProxyDto.getId(), String.join(",", targetHost));
        if (Boolean.FALSE.equals(result)){
            errorName.add(serviceProxyDto.getName());
            return;
        }
        successName.add(serviceProxyDto.getName());
    }

    @Override
    public Boolean refreshRoute(Long vgId, Long serviceId, Set<String> hosts){
        RouteQuery query = RouteQuery.builder().virtualGwId(vgId).serviceId(serviceId).build();
        List<RouteDto> routeList = routeService.getRouteList(query);
        for (RouteDto routeDto : routeList) {
            Boolean res = fillHosts(routeDto, serviceId, hosts);
            if (Boolean.FALSE.equals(res)){
                return res;
            }
            long updateRes = envoyRouteService.updateRoute(routeDto);
            if (BaseConst.ERROR_RESULT == updateRes) {
                return res;
            }
        }
        return Boolean.TRUE;
    }

    /**
     *  获取需要修改的目标域名
     *  举例：当前路由关联a,b,c三个服务，服务对应的域名分别为a.com,b.com,c.com，此时c服务的域名修改为c1.com
     *  1.查询路由绑定的所有服务 a,b,c
     *  2.排除需要修改的当前服务 a,b
     *  3.查询并聚合其他服务的域名 a.com,b.com
     *  4.add 当前服务的域名 a.com,b.com, c1.com
     */
    private Boolean fillHosts(RouteDto routeDto, Long serviceId, Set<String> hosts){
        List<Long> serviceIds = routeDto.getServiceIds();
        if (!serviceIds.contains(serviceId)){
            log.error("刷新服务域名失败，服务已失效");
            return false;
        }
        Set<String> targetHosts = new HashSet<>(hosts);
        List<Long> otherServiceIds = serviceIds.stream().filter(o -> !Objects.equals(o, serviceId)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(otherServiceIds)){
            Set<String> otherHosts = serviceProxyService.getUniqueHostListFromServiceIdList(otherServiceIds);
            targetHosts.addAll(otherHosts);
        }
        routeDto.setHosts(new ArrayList<>(targetHosts));
        return true;
    }

    /**
     * 判断是否需要进行刷新
     */


    /**
     * 获取需要刷新的域名列表
     */
    private Set<String> getTargetHost(Set<String> hosts, ServiceRefreshDTO refreshDTO){
        Set<String> targetHost = new HashSet<>(hosts);
        if (!CollectionUtils.isEmpty(refreshDTO.getAddHosts())){
            targetHost.addAll(refreshDTO.getAddHosts());
        }
        if (!CollectionUtils.isEmpty(refreshDTO.getDeleteHosts())){
            refreshDTO.getDeleteHosts().forEach(targetHost::remove);
        }
        return targetHost;
    }

    @Override
    public ErrorCode checkRefrshParam(ServiceRefreshDTO serviceRefreshDTO) {
        List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceByIds(serviceRefreshDTO.getServiceIds());
        if (serviceRefreshDTO.getServiceIds().size() != serviceProxyDtos.size()){
            return CommonErrorCode.invalidParameter("服务已失效，请刷新页面");
        }
        List<Long> vgIds = serviceProxyDtos.stream().map(ServiceProxyDto::getVirtualGwId).distinct().collect(Collectors.toList());
        if (vgIds.size() > 1){
            return CommonErrorCode.invalidParameter("待刷新服务在不同的虚拟网关下，禁止刷新");
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgIds.get(0));
        if (virtualGatewayDto == null){
            return CommonErrorCode.invalidParameter("网关不存在，请刷新页面");
        }
        List<String> addHosts = serviceRefreshDTO.getAddHosts();
        if (!CollectionUtils.isEmpty(addHosts)){
            Set<String> vgHosts = virtualGatewayDto.getDomainInfos().stream().map(DomainInfoDTO::getHost).collect(Collectors.toSet());
            for (String addHost : addHosts) {
                if (!vgHosts.contains(addHost)){
                    return CommonErrorCode.invalidParameter(addHost + "请先绑定虚拟网关");
                }
            }
        }
        return CommonErrorCode.SUCCESS;
    }
}
