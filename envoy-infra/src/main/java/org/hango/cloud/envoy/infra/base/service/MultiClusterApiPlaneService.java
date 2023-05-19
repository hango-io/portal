package org.hango.cloud.envoy.infra.base.service;

import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/22 16:28
 **/
@Service
public class MultiClusterApiPlaneService {

    @Autowired
    private IServiceProxyDao serviceProxyDao;

    @Autowired
    private IRouteService routeRuleProxyService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    public List<Long> rePublishResource(List<Long> ids, String kind){
        switch (ResourceEnum.getByKind(kind)){
            case Service:
                return publishService(ids);
            case Route:
                return publishRoute(ids);
            case Plugin:
                return publishPlugin(ids);
            default:
                return ids;
        }
    }

    public List<Long> publishService(List<Long> ids){
        if (CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<Long> errorIds = new ArrayList<>();
        for (Long id : ids) {
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(id);
            if (serviceProxyDto == null){
                errorIds.add(id);
                continue;
            }
            long updateResult = serviceProxyService.update(serviceProxyDto);
            if (BaseConst.ERROR_RESULT == updateResult){
                errorIds.add(id);
            }
        }
        return errorIds;
    }


    public List<Long> publishRoute(List<Long> ids){
        if (CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<Long> errorIds = new ArrayList<>();
        for (Long id : ids) {
            RouteDto routeDto = routeRuleProxyService.get(id);
            if (routeDto == null){
                errorIds.add(id);
                continue;
            }
            long updateResult = routeRuleProxyService.update(routeDto);
            if (BaseConst.ERROR_RESULT == updateResult){
                errorIds.add(id);
            }
        }
        return errorIds;
    }

    public List<Long> publishPlugin(List<Long> ids){
        if (CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<Long> errorIds = new ArrayList<>();
        for (Long id : ids) {
            PluginBindingDto pluginInfo = pluginInfoService.get(id);
            long result = pluginInfoService.update(pluginInfo);
            if (BaseConst.ERROR_RESULT == result) {
                errorIds.add(id);
            }
        }
        return errorIds;
    }

}
