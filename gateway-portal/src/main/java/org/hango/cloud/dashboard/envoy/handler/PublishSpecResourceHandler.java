package org.hango.cloud.dashboard.envoy.handler;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.innerdto.SpecResourceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/7/22
 */
@Component
public class PublishSpecResourceHandler extends AbstractSpecResourceHandler<GatewayInfo> {

    private static final String RESOURCE_TYPE = "publish";

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public List<GatewayInfo> getMetas() {
        return gatewayInfoService.findAll();
    }

    @Override
    public List<SpecResourceDto> toSpecResources(List<GatewayInfo> t) {
        if (CollectionUtils.isEmpty(t)) {
            return Collections.emptyList();
        }
        List<SpecResourceDto> specResources = new ArrayList<>();
        for (GatewayInfo gatewayInfo : t) {
            SpecResourceDto specResourceDto = new SpecResourceDto();
            specResourceDto.setServiceModule(Const.SERVICE_MODULE);
            specResourceDto.setResourceType(RESOURCE_TYPE);
            specResourceDto.setSpecResourceId(String.valueOf(gatewayInfo.getId()));
            specResourceDto.setSpecResourceName(gatewayInfo.getGwName());
            specResources.add(specResourceDto);
        }
        return specResources;
    }


}
