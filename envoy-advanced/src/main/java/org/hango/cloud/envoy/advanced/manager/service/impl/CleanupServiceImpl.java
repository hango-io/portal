package org.hango.cloud.envoy.advanced.manager.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.advanced.gateway.dto.GatewayAdvancedDto;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.advanced.manager.service.ICleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/8/31
 */
@Slf4j
@Service
public class CleanupServiceImpl implements ICleanupService {

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Override
    public ErrorCode checkCleanupParam(String name) {
        if (!StringUtils.hasText(name)){
            return CommonErrorCode.invalidParameter("name不能为空");
        }
        List<GatewayDto> gatewayDto = gatewayService.findByName(name, 0, 100);
        if (CollectionUtils.isEmpty(gatewayDto)){
            return CommonErrorCode.invalidParameter(String.format("name:%s不存在", name));
        }
        List<Long> gwIds = gatewayDto.stream().map(GatewayDto::getId).collect(Collectors.toList());
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayInfoService.getVirtualGatewayList(gwIds);
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayList) {
            List<ServiceProxyDto> serviceProxyDtos = serviceProxyService.getServiceProxyListByVirtualGwId(virtualGatewayDto.getId());
            if (!CollectionUtils.isEmpty(serviceProxyDtos)){
                return CommonErrorCode.invalidParameter(String.format("网关%s下存在服务,不允许删除", name));
            }
            PluginBindingInfoQuery query = PluginBindingInfoQuery.builder().virtualGwId(virtualGatewayDto.getId()).build();
            List<PluginBindingDto> pluginBindingDtos = pluginInfoService.getBindingPluginInfoList(query);
            if (!CollectionUtils.isEmpty(pluginBindingDtos)){
                return CommonErrorCode.invalidParameter(String.format("网关%s下存在插件,不允许删除", name));
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public void cleanup(String name) {
        List<GatewayDto> gatewayDtos = gatewayService.findByName(name, 0, 100);
        GatewayDto gatewayDto = gatewayDtos.get(0);
        List<VirtualGatewayDto> virtualGatewayList = virtualGatewayInfoService.getVirtualGatewayList(Collections.singletonList(gatewayDto.getId()));
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayList) {

            List<DomainInfoDTO> domainInfos = virtualGatewayDto.getDomainInfos();
            if (!CollectionUtils.isEmpty(domainInfos)){
                //解绑域名
                List<Long> domainIds = domainInfos.stream().map(DomainInfoDTO::getId).collect(Collectors.toList());
                DomainBindDTO domainBindDTO = new DomainBindDTO();
                domainBindDTO.setDomainIds(domainIds);
                domainBindDTO.setVirtualGwId(virtualGatewayDto.getId());
                virtualGatewayProjectService.unbindDomain(domainBindDTO);
            }
            List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
            if (!CollectionUtils.isEmpty(projectIdList)){
                //解绑项目
                for (Long project : projectIdList) {
                    virtualGatewayProjectService.unbindProject(virtualGatewayDto.getId(), project);
                }
            }
            //删除网关
            virtualGatewayInfoService.delete(virtualGatewayDto);
        }
        //删除物理网关
        GatewayAdvancedDto gatewayAdvancedDto = new GatewayAdvancedDto();
        gatewayAdvancedDto.cast(gatewayDto);
        gatewayService.delete(gatewayAdvancedDto);
    }
}
