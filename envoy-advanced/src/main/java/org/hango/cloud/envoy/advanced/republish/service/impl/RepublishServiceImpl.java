package org.hango.cloud.envoy.advanced.republish.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.advanced.republish.dto.RepublishResult;
import org.hango.cloud.envoy.advanced.republish.service.IRepublishService;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.envoy.infra.route.service.IEnvoyRouteService;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ResultDTO;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IEnvoyVgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author zhufengwei
 * @Date 2023/4/18
 */
@Slf4j
@Service
public class RepublishServiceImpl implements IRepublishService {

    @Autowired
    IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    private IEnvoyRouteService envoyRouteService;

    @Autowired
    private IEnvoyVgService envoyVgService;

    @Autowired
    private IPluginManagerService pluginManagerService;

    @Override
    public ErrorCode checkRepublishParam(List<Long> vgIds) {
        for (Long vgId : vgIds) {
            VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(vgId);
            if (virtualGatewayDto == null){
                return CommonErrorCode.NO_SUCH_GATEWAY;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<RepublishResult> republish(List<Long> vgIds) {
        List<RepublishResult> republishResults = new ArrayList<>();
        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getByIds(vgIds);
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            RepublishResult result;
            try {
                result = doRepublish(virtualGatewayDto);
            }catch (Exception e){
                log.error("republish exception,vgName:{}", virtualGatewayDto.getName(), e);
                result = RepublishResult.ofError(virtualGatewayDto.getName(), "republish exception");
            }
            republishResults.add(result);
        }
        return republishResults;
    }

    private RepublishResult doRepublish(VirtualGatewayDto virtualGatewayDto){
        RepublishResult result = RepublishResult.of(virtualGatewayDto.getName());

        List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceProxyListByVirtualGwId(virtualGatewayDto.getId());
        Set<String> errorName = new HashSet<>();
        for (ServiceProxyDto serviceProxyDto : serviceProxyDtos) {
            //刷新服务和路由
            Boolean res = refreshService(serviceProxyDto);
            if (Boolean.FALSE.equals(res)){
                errorName.add(serviceProxyDto.getName());
            }
        }
        result.setResultDTO(ResultDTO.of(serviceProxyDtos.size(), new ArrayList<>(errorName)));
        //刷新网关
        Boolean res = refreshGateway(virtualGatewayDto);
        if (Boolean.FALSE.equals(res)){
            result.setErrmsg("republish gateway/plm cr error");
        }
        return result;
    }


    private Boolean refreshService(ServiceProxyDto serviceProxyDto){
        RouteQuery query = RouteQuery.builder().serviceId(serviceProxyDto.getId()).build();
        List<RouteDto> routeList = routeService.getRouteList(query);
        for (RouteDto routeDto : routeList) {
            long updateRes = envoyRouteService.updateRoute(routeDto);
            if (BaseConst.ERROR_RESULT == updateRes) {
                log.error("republish route error|{}", routeDto.getName());
                return Boolean.FALSE;
            }
        }
        //发布服务
        Boolean res = envoyServiceProxyService.updateToGateway(serviceProxyDto);
        if (Boolean.FALSE.equals(res)) {
            log.error("republish service error|{}", serviceProxyDto.getName());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    private Boolean refreshGateway(VirtualGatewayDto virtualGatewayDto){
        boolean res = envoyVgService.publishToGateway(virtualGatewayDto);
        if (Boolean.FALSE.equals(res)){
            log.error("republish gateway error|{}", virtualGatewayDto.getName());
            return Boolean.FALSE;
        }
        res = pluginManagerService.publishPluginManager(virtualGatewayDto);
        if (Boolean.FALSE.equals(res)){
            log.error("republish plm error|{}", virtualGatewayDto.getName());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
