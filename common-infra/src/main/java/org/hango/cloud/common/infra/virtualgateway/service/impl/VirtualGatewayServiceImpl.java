package org.hango.cloud.common.infra.virtualgateway.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewaySetting;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_GATEWAY;
import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_INGRESS;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:27.
 */
@Service
public class VirtualGatewayServiceImpl implements IVirtualGatewayInfoService {

    private static final Logger logger = LoggerFactory.getLogger(VirtualGatewayServiceImpl.class);


    @Autowired
    private IVirtualGatewayDao virtualGatewayDao;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    public static final String LOAD_BALANCE = "LoadBalance";


    @Override
    public long create(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway info = toMeta(virtualGatewayDto);
        info.setId(null);
        virtualGatewayDao.add(info);
        return info.getId();
    }

    @Override
    public long update(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway virtualGateway = toMeta(virtualGatewayDto);
        virtualGatewayDao.update(virtualGateway);
        return virtualGateway.getId();
    }

    @Override
    public void delete(VirtualGatewayDto virtualGatewayDto) {
        virtualGatewayDao.delete(toMeta(virtualGatewayDto));
    }


    @Override
    public long createWithoutHooker(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway info = toMeta(virtualGatewayDto);
        virtualGatewayDao.add(info);
        return info.getId();
    }

    @Override
    public long updateWithoutHooker(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway virtualGateway = toMeta(virtualGatewayDto);
        virtualGatewayDao.update(virtualGateway);
        return virtualGateway.getId();
    }

    @Override
    public void deleteWithoutHooker(VirtualGatewayDto virtualGatewayDto) {
        virtualGatewayDao.delete(toMeta(virtualGatewayDto));
    }

    @Override
    public List<VirtualGatewayDto> findAll() {
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.findAll();
        if (CollectionUtils.isEmpty(virtualGatewayList)) {
            return Collections.emptyList();
        }
        return virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }



    @Override
    public List<VirtualGatewayDto> getKubernetesGatewayList(long gwId) {
        VirtualGatewayQuery query = VirtualGatewayQuery.builder().gwIds(Collections.singletonList(gwId)).managed(Boolean.FALSE).build();
        return virtualGatewayDao.getVirtualGatewayList(query).stream().map(this::toView).filter(Objects::nonNull).collect(Collectors.toList());
    }


    @Override
    public List<VirtualGatewayDto> getVirtualGatewayList(List<Long> gwIdList) {
        if (CollectionUtils.isEmpty(gwIdList)) {
            return Lists.newArrayList();
        }
        VirtualGatewayQuery query = VirtualGatewayQuery.builder().gwIds(gwIdList).build();
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getVirtualGatewayList(query);
        return  virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<VirtualGatewayDto> getByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getByIds(ids);
        return  virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public VirtualGatewayDto toView(VirtualGateway virtualGateway) {
        if (virtualGateway == null) {
            return null;
        }
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setProtocol(virtualGateway.getProtocol());
        virtualGatewayDto.setModifyTime(virtualGateway.getUpdateTime());
        virtualGatewayDto.setCode(virtualGateway.getCode());
        virtualGatewayDto.setCreateTime(virtualGateway.getCreateTime());
        virtualGatewayDto.setPort(virtualGateway.getPort());
        virtualGatewayDto.setName(virtualGateway.getName());
        virtualGatewayDto.setDescription(virtualGateway.getDescription());
        virtualGatewayDto.setGwId(virtualGateway.getGwId());
        virtualGatewayDto.setId(virtualGateway.getId());
        virtualGatewayDto.setType(virtualGateway.getType());
        virtualGatewayDto.setAddr(virtualGateway.getAddr());
        List<Long> projectIds = CommonUtil.splitStringToLongList(virtualGateway.getProjectId(), ",").stream().distinct().collect(Collectors.toList());
        virtualGatewayDto.setProjectIdList(projectIds);
        List<Long> domainIds = CommonUtil.splitStringToLongList(virtualGateway.getDomainId(), ",");
        List<DomainInfoDTO> domainInfos = domainInfoService.getDomainInfos(domainIds);
        virtualGatewayDto.setDomainInfos(domainInfos);
        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        if(gatewayDto == null){
            return virtualGatewayDto;
        }
        virtualGatewayDto.setPublishServiceCount(serviceProxyService.countServiceProxy(ServiceProxyQuery.builder()
                .virtualGwId(virtualGateway.getId())
                .projectId(ProjectTraceHolder.getProId())
                .build()));
        virtualGatewayDto.setConfAddr(gatewayDto.getConfAddr());
        virtualGatewayDto.setGwType(gatewayDto.getType());
        virtualGatewayDto.setGwClusterName(gatewayDto.getGwClusterName());
        virtualGatewayDto.setGwName(gatewayDto.getName());
        virtualGatewayDto.setEnvId(gatewayDto.getEnvId());
        return virtualGatewayDto;
    }

    @Override
    public VirtualGateway toMeta(VirtualGatewayDto virtualGatewayDto) {
        if (virtualGatewayDto == null) {
            return null;
        }
        VirtualGateway virtualGateway = new VirtualGateway();
        virtualGateway.setProtocol(virtualGatewayDto.getProtocol());
        virtualGateway.setCode(virtualGatewayDto.getCode());
        virtualGateway.setPort(virtualGatewayDto.getPort());
        virtualGateway.setName(virtualGatewayDto.getName());
        virtualGateway.setDescription(virtualGatewayDto.getDescription());
        virtualGateway.setGwId(virtualGatewayDto.getGwId());
        virtualGateway.setId(virtualGatewayDto.getId());
        virtualGateway.setType(virtualGatewayDto.getType());
        virtualGateway.setAddr(virtualGatewayDto.getAddr());
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        virtualGateway.setProjectId(projectIdList.stream().map(Object::toString).collect(Collectors.joining(",")));
        List<DomainInfoDTO> domainInfos = virtualGatewayDto.getDomainInfos();
        if (!CollectionUtils.isEmpty(domainInfos)){
            virtualGateway.setDomainId(domainInfos.stream().map(DomainInfoDTO::getId).map(Object::toString).collect(Collectors.joining(",")));
        }
        return virtualGateway;
    }


    @Override
    public ErrorCode checkCreateParam(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway virtualGateway = getByName(virtualGatewayDto.getName());
        if (virtualGateway != null) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_NAME;
        }
        VirtualGatewayQuery query = VirtualGatewayQuery.builder().code(virtualGatewayDto.getCode()).build();
        if (virtualGatewayDao.exist(query)) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_CODE;
        }
        query = VirtualGatewayQuery.builder()
                .port(virtualGatewayDto.getPort())
                .gwIds(Collections.singletonList(virtualGatewayDto.getGwId())).build();
        if (virtualGatewayDao.exist(query)) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_PORT;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway origin = virtualGatewayDao.get(virtualGatewayDto.getId());
        if (origin == null) {
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }

        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null){
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        origin = getByName(virtualGatewayDto.getName());
        if (origin == null){
            return CommonErrorCode.SUCCESS;
        }
        if (origin.getId() != virtualGatewayDto.getId()) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_NAME;
        }

        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(VirtualGatewayDto virtualGatewayDto) {
        if (virtualGatewayDto == null){
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        if (Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS).contains(virtualGatewayDto.getType())){
            return CommonErrorCode.SUCCESS;
        }
        if (!CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())) {
            return CommonErrorCode.CANNOT_DELETE_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public VirtualGatewayDto get(long id) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(id);
        return toView(virtualGateway);
    }

    @Override
    public Page<VirtualGatewayDto> getVirtualGatewayPage(QueryVirtualGatewayDto query) {
        List<Long> projectIdList = query.getProjectIdList();
        //当项目检索条件不为空，但长度为0,认为租户下无项目,返回空数据
        if (projectIdList != null && projectIdList.size() == 0) {
            return Page.of(0,0);
        }
        Page<VirtualGateway> page = virtualGatewayDao.getVirtualGatewayPage(toMeta(query));
        return toPageView(page);
    }

    @Override
    public List<VirtualGatewayDto> getVirtualGatewayList(QueryVirtualGatewayDto query) {
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getVirtualGatewayList(toMeta(query));
        if (query.getServiceId() != null){
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(query.getServiceId());
            if (serviceProxyDto != null){
                virtualGatewayList = virtualGatewayList.stream().filter(o -> protocolFilter(o, serviceProxyDto.getProtocol())).collect(Collectors.toList());
            }
        }
        return virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public void updateGatewaySetting(GatewaySettingDTO gatewaySettingDto) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(gatewaySettingDto.getVirtualGwId());
        virtualGateway.setAdvancedSetting(toMeta(gatewaySettingDto));
        virtualGatewayDao.update(virtualGateway);
    }

    @Override
    public GatewaySettingDTO getGatewaySetting(Long id) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(id);
        if (virtualGateway == null || virtualGateway.getAdvancedSetting() == null){
            return null;
        }
        GatewaySettingDTO settingDTO = toView(virtualGateway.getAdvancedSetting());
        settingDTO.setVirtualGwId(id);
        return settingDTO;
    }

    @Override
    public void fillVirtualGatewayInfo(VirtualGatewayDto virtualGatewayDto) {
        return;
    }

    @Override
    public Boolean exist(VirtualGatewayQuery query) {
        return virtualGatewayDao.exist(query);
    }

    private VirtualGatewayQuery toMeta(QueryVirtualGatewayDto queryDto){
        VirtualGatewayQuery query = VirtualGatewayQuery.builder()
                .type(queryDto.getType())
                .projectIds(queryDto.getProjectIdList())
                .pattern(queryDto.getPattern())
                .managed(queryDto.getManaged())
                .build();
        if(queryDto.getGwId() != null){
            query.setGwIds(Collections.singletonList(queryDto.getGwId()));
        }
        query.setLimit(queryDto.getLimit());
        query.setOffset(queryDto.getOffset());
        return query;
    }

    private boolean protocolFilter(VirtualGateway virtualGateway, String protocol){
        if (StringUtils.isBlank(protocol)){
            return false;
        }
        if (Const.HTTP.equals(protocol)){
            return true;
        }

        return !virtualGateway.getType().equalsIgnoreCase(LOAD_BALANCE);
    }


    private VirtualGateway getByName(String name){
        VirtualGatewayQuery query = VirtualGatewayQuery.builder().name(name).build();
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getVirtualGatewayList(query);
        return CollectionUtils.isEmpty(virtualGatewayList) ? null : virtualGatewayList.get(0);
    }

    private GatewaySettingDTO toView(VirtualGatewaySetting virtualGatewaySetting){
        GatewaySettingDTO settingDTO = new GatewaySettingDTO();
        settingDTO.setIpSource(virtualGatewaySetting.getIpSource());
        settingDTO.setXffNumTrustedHops(virtualGatewaySetting.getXffNumTrustedHops());
        settingDTO.setCustomIpAddressHeader(virtualGatewaySetting.getCustomIpAddressHeader());
        return settingDTO;
    }

    private VirtualGatewaySetting toMeta(GatewaySettingDTO gatewaySettingDTO){
        if (gatewaySettingDTO == null) {
            return null;
        }
        VirtualGatewaySetting virtualGatewaySetting = new VirtualGatewaySetting();
        virtualGatewaySetting.setIpSource(gatewaySettingDTO.getIpSource());
        virtualGatewaySetting.setCustomIpAddressHeader(gatewaySettingDTO.getCustomIpAddressHeader());
        virtualGatewaySetting.setXffNumTrustedHops(gatewaySettingDTO.getXffNumTrustedHops());
        return virtualGatewaySetting;
    }

    @Override
    public VirtualGatewayDto getByCode(String code) {
        Page<VirtualGateway> virtualGatewayPage = virtualGatewayDao.getVirtualGatewayPage(VirtualGatewayQuery.builder().code(code).build());
        if (CollectionUtils.isEmpty(virtualGatewayPage.getRecords())){
            return null;
        }
        return toView(virtualGatewayPage.getRecords().get(0));
    }
}