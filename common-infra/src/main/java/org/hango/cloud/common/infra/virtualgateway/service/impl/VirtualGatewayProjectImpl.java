package org.hango.cloud.common.infra.virtualgateway.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.*;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VirtualGatewayProjectImpl implements IVirtualGatewayProjectService {


    private static final Logger logger = LoggerFactory.getLogger(VirtualGatewayProjectImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Override
    public PermissionScopeDto getProjectScope(long projectId) {
        PermissionScopeDto permissionScopeDto = new PermissionScopeDto();
        permissionScopeDto.setId(projectId);
        permissionScopeDto.setPermissionScopeName(BaseConst.DEFAULT);
        permissionScopeDto.setPermissionScopeEnName(BaseConst.DEFAULT);
        permissionScopeDto.setParentId(NumberUtils.LONG_ZERO);
        return permissionScopeDto;
    }

    @Override
    public List<PermissionScopeDto> getProjectScopeList(long tenantId) {
        return Collections.emptyList();
    }


    @Override
    public ErrorCode checkBindProject(VirtualGatewayBindDto virtualGatewayBind) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayBind.getVirtualGwId());
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public long bindProject(VirtualGatewayBindDto virtualGatewayBind) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayBind.getVirtualGwId());
        virtualGatewayDto.setProjectIdList(virtualGatewayBind.getProjectIdList());
        return virtualGatewayInfoService.update(virtualGatewayDto);
    }

    @Override
    public ErrorCode checkUnBindProject(long virtualGwId, long projectId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGatewayDto == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        if (projectId <= 0) {
            return CommonErrorCode.EMPTY_PROJECT_ID;
        }
        List<DomainInfoDTO> domainInfos = virtualGatewayDto.getDomainInfos();
        if (!CollectionUtils.isEmpty(domainInfos)){
            Set<Long> projectIds = domainInfos.stream().map(DomainInfoDTO::getProjectId).collect(Collectors.toSet());
            if (projectIds.contains(projectId)){
                return CommonErrorCode.EXIST_PUBLISHED_DOMAIN;

            }
        }
        long publishedServiced = serviceProxyService.countServiceProxy(ServiceProxyQuery.builder().virtualGwId(virtualGwId).projectId(projectId).build());
        if (publishedServiced > 0) {
            return CommonErrorCode.EXIST_PUBLISHED_SERVICE;
        }
        List<PluginBindingDto> bindingPluginList = pluginInfoService.getPluginBindingList(virtualGwId, String.valueOf(projectId), BindingObjectTypeEnum.GLOBAL.getValue());
        if (!CollectionUtils.isEmpty(bindingPluginList)) {
            return CommonErrorCode.EXIST_PUBLISHED_PLUGIN;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public long unbindProject(long virtualGwId, long projectId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        List<Long> newProjectList = projectIdList.stream().filter(p -> p != projectId).collect(Collectors.toList());
        virtualGatewayDto.setProjectIdList(newProjectList);
        return virtualGatewayInfoService.update(virtualGatewayDto);
    }

    @Override
    public List<SingleVgBindDto> getBindList(QueryVirtualGatewayDto query) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(query.getVirtualGwId());
        if (virtualGatewayDto == null || CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())) {
            return Collections.emptyList();
        }
        //当项目检索条件不为空，但长度为0,认为租户下无项目,返回空数据
        if (query.getProjectIdList() != null && query.getProjectIdList().size() == 0) {
            return Collections.emptyList();
        }

        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();

        if (CollectionUtils.isEmpty(projectIdList) || projectIdList.size() <= query.getOffset()) {
            return Collections.emptyList();
        }

        Stream<SingleVgBindDto> singleVgBindDtoStream = projectIdList.stream().skip(query.getOffset())
                .limit(query.getLimit()).map(p -> getSingleVgBindDto(virtualGatewayDto, p));
        if (CollectionUtils.isEmpty(query.getProjectIdList())) {
            return singleVgBindDtoStream.collect(Collectors.toList());
        }
        return singleVgBindDtoStream.filter(p -> query.getProjectIdList().contains(p.getProjectId())).collect(Collectors.toList());
    }

    private SingleVgBindDto getSingleVgBindDto(VirtualGatewayDto virtualGatewayDto, Long projectId) {
        List<String> hosts = domainInfoService.getHosts(projectId, virtualGatewayDto.getId());
        return new SingleVgBindDto(projectId, virtualGatewayDto.getId(), hosts);
    }

    @Override
    public long countBindList(QueryVirtualGatewayDto query) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(query.getVirtualGwId());
        if (virtualGatewayDto == null) {
            return NumberUtils.LONG_ZERO;
        }
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        if (CollectionUtils.isEmpty(projectIdList)) {
            return NumberUtils.LONG_ZERO;
        }
        //当项目检索条件不为空，但长度为0,认为租户下无项目,返回空数据
        List<Long> queryList = query.getProjectIdList();
        if (queryList != null && queryList.size() == 0) {
            return NumberUtils.LONG_ZERO;
        }
        Stream<Long> stream = projectIdList.stream();
        if (CollectionUtils.isEmpty(queryList)) {
            return stream.count();
        }
        return stream.filter(queryList::contains).count();
    }


    @Override
    public ErrorCode checkBindParam(DomainBindDTO domainBindDTO) {
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());
        if (virtualGateway == null){
            return CommonErrorCode.invalidParameter("虚拟网关为空，绑定失败");
        }
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(domainBindDTO.getDomainIds());
        if (CollectionUtils.isEmpty(domainInfos)){
            return CommonErrorCode.invalidParameter("域名为空，绑定失败");
        }
        if (domainInfos.size() != domainBindDTO.getDomainIds().size()){
            return CommonErrorCode.invalidParameter("域名不存在，绑定失败");
        }
        for (DomainInfoDTO domainInfo : domainInfos) {
            if (!virtualGateway.getProtocol().equalsIgnoreCase(domainInfo.getProtocol())){
                return CommonErrorCode.invalidParameter("域名和网关协议不同，绑定失败");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public void bindDomain(DomainBindDTO domainBindDTO) {
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());
        Set<Long> targetDomainIds = virtualGateway.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toSet());
        targetDomainIds.addAll(domainBindDTO.getDomainIds());
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(new ArrayList<>(targetDomainIds));
        virtualGateway.setDomainInfos(domainInfos);
        virtualGatewayInfoService.update(virtualGateway);
    }



    @Override
    public ErrorCode checkUnbindParam(DomainBindDTO domainBindDTO) {
        ErrorCode errorCode = checkBindParam(domainBindDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());
        Set<Long> targetDomain = virtualGateway.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toSet());
        domainBindDTO.getDomainIds().forEach(targetDomain::remove);
        if (CollectionUtils.isEmpty(targetDomain)
                && !CollectionUtils.isEmpty(serviceProxyService.getServiceProxyListByVirtualGwId(domainBindDTO.getVirtualGwId()))){
            return CommonErrorCode.invalidParameter("当前虚拟网关下存在服务，不允许清空域名");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public void unbindDomain(DomainBindDTO domainBindDTO) {
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(domainBindDTO.getVirtualGwId());
        List<Long> domainIds = virtualGateway.getDomainInfos().stream().map(DomainInfoDTO::getId).collect(Collectors.toList());
        domainIds.removeAll(domainBindDTO.getDomainIds());
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(domainIds);
        virtualGateway.setDomainInfos(domainInfos);
        virtualGatewayInfoService.update(virtualGateway);
    }
}
