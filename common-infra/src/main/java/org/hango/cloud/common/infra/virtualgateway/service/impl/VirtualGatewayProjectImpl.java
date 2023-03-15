package org.hango.cloud.common.infra.virtualgateway.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.virtualgateway.dto.*;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL;

@Service
public class VirtualGatewayProjectImpl implements IVirtualGatewayProjectService {


    private static final Logger logger = LoggerFactory.getLogger(VirtualGatewayProjectImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private IServiceProxyDao serviceProxyDao;

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
    public void updateBindDomainStatus(VirtualGatewayBindDto virtualGatewayBind){
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayBind.getVirtualGwId());
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        if (CollectionUtils.isEmpty(projectIdList)){
            return;
        }
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(projectIdList, virtualGatewayDto.getProtocol(), virtualGatewayDto.getEnvId());
        for (DomainInfoDTO domainInfo : domainInfos) {
            if (DomainStatusEnum.NotUse.name().equals(domainInfo.getStatus())){
                domainInfo.setStatus(DomainStatusEnum.Active.name());
                domainInfoService.update(domainInfo);
            }
        }
    }

    @Override
    public void updateUnbindDomainStatus(long virtualGwId, long projectId){
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(Collections.singletonList(projectId), virtualGatewayDto.getProtocol(), virtualGatewayDto.getEnvId());
        Boolean active =  virtualGatewayInfoService.existManagedVirtualGateway(projectId, virtualGatewayDto.getEnvId(), virtualGatewayDto.getProtocol());
        if (active){
            return;
        }
        for (DomainInfoDTO domainInfo : domainInfos) {
            if (DomainStatusEnum.enable(domainInfo.getStatus())){
                domainInfo.setStatus(DomainStatusEnum.NotUse.name());
                domainInfoService.update(domainInfo);
            }
        }
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
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("virtualGwId", virtualGwId);
        int publishedServiced = serviceProxyDao.getCountByFields(params);
        if (publishedServiced > 0) {
            return CommonErrorCode.EXIST_PUBLISHED_SERVICE;
        }
        List<PluginBindingDto> bindingPluginList = pluginInfoService.getPluginBindingList(virtualGwId, String.valueOf(projectId), BINDING_OBJECT_TYPE_GLOBAL);
        if (!CollectionUtils.isEmpty(bindingPluginList)){
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

    private SingleVgBindDto getSingleVgBindDto(VirtualGatewayDto virtualGatewayDto, Long projectId){
        List<String> hosts = domainInfoService.getHosts(projectId, virtualGatewayDto.getId());
        return new SingleVgBindDto(projectId, virtualGatewayDto.getId(), hosts);
    }

    @Override
    public long countBindList(QueryVirtualGatewayDto query) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(query.getVirtualGwId());
        if (virtualGatewayDto == null){
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
            return stream.collect(Collectors.counting());
        }
        return stream.filter(p -> queryList.contains(p)).collect(Collectors.counting());
    }
}
