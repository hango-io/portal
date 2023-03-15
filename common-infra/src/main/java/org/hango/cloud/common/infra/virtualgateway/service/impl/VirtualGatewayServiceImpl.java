package org.hango.cloud.common.infra.virtualgateway.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SYMBOL_COMMA;

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
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;


    public static final String LOAD_BALANCE = "LoadBalance";




    @Override
    @Transactional(rollbackFor = Exception.class)
    public long create(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway info = toMeta(virtualGatewayDto);
        info.setCreateTime(System.currentTimeMillis());
        info.setModifyTime(System.currentTimeMillis());
        long id = virtualGatewayDao.add(info);
        virtualGatewayDto.setId(id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long update(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(virtualGatewayDto.getId());
        virtualGateway.setAddr(virtualGatewayDto.getAddr());
        virtualGateway.setName(virtualGatewayDto.getName());
        virtualGateway.setDescription(virtualGatewayDto.getDescription());
        virtualGateway.setModifyTime(System.currentTimeMillis());
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        if (projectIdList != null){
            List<String> proIds = projectIdList.stream().map(String::valueOf).collect(Collectors.toList());
            virtualGateway.setProjectId(String.join(SYMBOL_COMMA, proIds));
        }
        int result = virtualGatewayDao.update(virtualGateway);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(VirtualGatewayDto s) {
        virtualGatewayDao.delete(toMeta(s));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createWithoutHooker(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway info = toMeta(virtualGatewayDto);
        info.setCreateTime(System.currentTimeMillis());
        info.setModifyTime(System.currentTimeMillis());
        long id = virtualGatewayDao.add(info);
        virtualGatewayDto.setId(id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateWithoutHooker(VirtualGatewayDto virtualGatewayDto) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(virtualGatewayDto.getId());
        virtualGateway.setAddr(virtualGatewayDto.getAddr());
        virtualGateway.setName(virtualGatewayDto.getName());
        virtualGateway.setPort(virtualGatewayDto.getPort());
        virtualGateway.setProtocol(virtualGatewayDto.getProtocol());
        virtualGateway.setDescription(virtualGatewayDto.getDescription());
        virtualGateway.setModifyTime(System.currentTimeMillis());
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        if (projectIdList != null){
            List<String> proIds = projectIdList.stream().map(String::valueOf).collect(Collectors.toList());
            virtualGateway.setProjectId(String.join(SYMBOL_COMMA, proIds));
        }
        int result = virtualGatewayDao.update(virtualGateway);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithoutHooker(VirtualGatewayDto s) {
        virtualGatewayDao.delete(toMeta(s));
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
    public long countAll() {
        Map<String, Object> params = Maps.newHashMap();
        return virtualGatewayDao.getCountByFields(params);
    }

    @Override
    public List<VirtualGatewayDto> findAll(long offset, long limit) {
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getRecordsByField(Maps.newHashMap(), offset, limit);
        if (CollectionUtils.isEmpty(virtualGatewayList)) {
            return Collections.emptyList();
        }
        return virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public VirtualGatewayDto get(long id) {
        return toView(virtualGatewayDao.get(id));
    }


    @Override
    public List<VirtualGatewayDto> getGatewayInfoByGwId(long gwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("gwId", gwId);
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getRecordsByField(params);
        return virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<VirtualGatewayDto> getPublishedServiceGateway(Long routeId) {
        RouteRuleDto routeRuleDto = routeRuleInfoService.get(routeId);
        if (routeRuleDto == null){
            return new ArrayList<>();
        }

        List<Long> routeVirtualGwIds  = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeId).stream()
                .map(RouteRuleProxyDto::getVirtualGwId).collect(Collectors.toList());

        return serviceProxyService.getServiceProxyByServiceId(routeRuleDto.getServiceId()).stream()
                .map(ServiceProxyDto::getVirtualGwId)
                .filter(o -> !routeVirtualGwIds.contains(o))
                .map(this::get)
                .collect(Collectors.toList());

    }


    @Override
    public List<VirtualGatewayDto> getGatewayList(long gwId, String type) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("gwId", gwId);
        params.put("type", type);
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getRecordsByField(params);
        return virtualGatewayList.stream().map(this::toView).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public VirtualGatewayDto getByName(String name) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", name);
        return toView(Iterables.getFirst(virtualGatewayDao.getRecordsByField(params),null));
    }

    @Override
    public VirtualGatewayDto getByCode(String code) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", code);
        return toView(Iterables.getFirst(virtualGatewayDao.getRecordsByField(params),null));
    }

    @Override
    public VirtualGatewayDto getByPort(int port) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("port", port);
        return toView(Iterables.getFirst(virtualGatewayDao.getRecordsByField(params),null));
    }

    @Override
    public boolean isGwExists(long virtualGwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", virtualGwId);
        return virtualGatewayDao.getCountByFields(params) != 0;
    }

    @Override
    public List<VirtualGatewayDto> getGatewayInfoList(List gwIdList) {
        if (CollectionUtils.isEmpty(gwIdList)) {
            return Lists.newArrayList();
        }
        List<VirtualGateway> virtualGatewayList = virtualGatewayDao.getGatewayInfoList(gwIdList);
        return CollectionUtils.isEmpty(virtualGatewayList) ? Lists.newArrayList() : virtualGatewayList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<Long> getGwIdListByNameFuzzy(String gwName, long projectId) {
        List<Long> gwIdList = virtualGatewayDao.getGwIdListByNameFuzzy(gwName, projectId);
        return CollectionUtils.isEmpty(gwIdList) ? Lists.newArrayList() : gwIdList;
    }

    @Override
    public List<VirtualGatewayDto> getGwEnvByProjectId(Long projectId) {
        List<VirtualGateway> virtualGatewayByProjectId = virtualGatewayDao.getGatewayInfoByProjectIdAndLimit(null, projectId, NumberUtils.LONG_ZERO, Integer.MAX_VALUE);
        return virtualGatewayByProjectId.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<VirtualGatewayDto> getManagedVirtualGatewayList(Long projectId, String env, String protocol) {
        List<VirtualGateway> virtualGatewayByProjectId = virtualGatewayDao.getManagedGatewayInfo(projectId, protocol);
        return virtualGatewayByProjectId.stream().map(this::toView).filter(o -> envFilter(o, env)).collect(Collectors.toList());
    }

    @Override
    public Boolean existManagedVirtualGateway(Long projectId, String env, String protocol) {
        List<VirtualGatewayDto> virtualGatewayList = getManagedVirtualGatewayList(projectId, env, protocol);
        return !CollectionUtils.isEmpty(virtualGatewayList);
    }

    private boolean envFilter(VirtualGatewayDto virtualGatewayDto, String env){
        if (StringUtils.isEmpty(env)){
            return true;
        }
        return StringUtils.equals(virtualGatewayDto.getEnvId(), env);
    }

    @Override
    public VirtualGatewayDto toView(VirtualGateway virtualGateway) {
        if (virtualGateway == null) {
            return null;
        }
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setProtocol(virtualGateway.getProtocol());
        virtualGatewayDto.setModifyTime(virtualGateway.getModifyTime());
        virtualGatewayDto.setCode(virtualGateway.getCode());
        virtualGatewayDto.setCreateTime(virtualGateway.getCreateTime());
        virtualGatewayDto.setPort(virtualGateway.getPort());
        virtualGatewayDto.setName(virtualGateway.getName());
        virtualGatewayDto.setDescription(virtualGateway.getDescription());
        virtualGatewayDto.setGwId(virtualGateway.getGwId());
        virtualGatewayDto.setId(virtualGateway.getId());
        virtualGatewayDto.setType(virtualGateway.getType());
        virtualGatewayDto.setAddr(virtualGateway.getAddr());
        virtualGatewayDto.setProjectIdList(virtualGateway.getProjectIdList());

        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        if(gatewayDto == null){
            return virtualGatewayDto;
        }
        if (!CollectionUtils.isEmpty(virtualGateway.getProjectIdList())){
            List<DomainInfoPO> domainInfoPOS = domainInfoMapper.selectList(new LambdaQueryWrapper<DomainInfoPO>().in(DomainInfoPO::getProjectId, virtualGateway.getProjectIdList()));
            if (!CollectionUtils.isEmpty(domainInfoPOS)) {
                List<String> hostCollect = domainInfoPOS.stream().map(DomainInfoPO::getHost).collect(Collectors.toList());
                virtualGatewayDto.setVirtualHostList(hostCollect);
            }else {
                virtualGatewayDto.setVirtualHostList(new ArrayList<>());
            }
        }else {
            virtualGatewayDto.setVirtualHostList(new ArrayList<>());
        }

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
        virtualGateway.setModifyTime(virtualGatewayDto.getModifyTime());
        virtualGateway.setCode(virtualGatewayDto.getCode());
        virtualGateway.setCreateTime(virtualGatewayDto.getCreateTime());
        virtualGateway.setPort(virtualGatewayDto.getPort());
        virtualGateway.setName(virtualGatewayDto.getName());
        virtualGateway.setDescription(virtualGatewayDto.getDescription());
        virtualGateway.setGwId(virtualGatewayDto.getGwId());
        virtualGateway.setId(virtualGatewayDto.getId());
        virtualGateway.setType(virtualGatewayDto.getType());
        virtualGateway.setAddr(virtualGatewayDto.getAddr());
        List<Long> projectIdList = virtualGatewayDto.getProjectIdList();
        virtualGateway.setProjectId(projectIdList.stream().map(Object::toString).collect(Collectors.joining(",")));

        return virtualGateway;
    }


    @Override
    public ErrorCode checkCreateParam(VirtualGatewayDto virtualGatewayDto) {
        VirtualGatewayDto nameCheck = getByName(virtualGatewayDto.getName());
        if (nameCheck != null) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_NAME;
        }
        VirtualGatewayDto codeCheck = getByCode(virtualGatewayDto.getCode());
        if (codeCheck != null) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_CODE;
        }
        VirtualGatewayDto portCheck = getByPort(virtualGatewayDto.getPort());
        if (portCheck != null){
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_PORT;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(VirtualGatewayDto virtualGatewayDto) {
        VirtualGatewayDto origin = get(virtualGatewayDto.getId());
        if (origin == null) {
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }

        VirtualGatewayDto nameCheck = getByName(virtualGatewayDto.getName());

        if (nameCheck != null && nameCheck.getId() != virtualGatewayDto.getId()) {
            return CommonErrorCode.ALREADY_EXIST_VIRTUAL_GW_NAME;
        }

        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null){
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(VirtualGatewayDto virtualGatewayDto) {
        VirtualGatewayDto origin = get(virtualGatewayDto.getId());
        if (origin == null){
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        if (!CollectionUtils.isEmpty(origin.getProjectIdList())) {
            return CommonErrorCode.CANNOT_DELETE_VIRTUAL_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteVirtualGatewayParamFromHango(VirtualGatewayDto virtualGatewayDto) {
        VirtualGatewayDto origin = get(virtualGatewayDto.getId());
        if (origin == null){
            return CommonErrorCode.NO_SUCH_VIRTUAL_GATEWAY;
        }
        //检查是否还有未下线的服务
        List<ServiceProxyDto> serviceProxyListByVirtualGwId = serviceProxyService.getServiceProxyListByVirtualGwId(origin.getId());
        if (!CollectionUtils.isEmpty(serviceProxyListByVirtualGwId)){
            return CommonErrorCode.EXIST_PUBLISHED_SERVICE;
        }
        return CommonErrorCode.SUCCESS;
    }
    @Override
    public List<VirtualGatewayDto> getVirtualGatewayListByConditions(QueryVirtualGatewayDto query) {
        List<Long> projectIdList = query.getProjectIdList();
        //当项目检索条件不为空，但长度为0,认为租户下无项目,返回空数据
        if (projectIdList != null && projectIdList.size() == 0) {
            return Collections.emptyList();
        }
        Stream<VirtualGateway> virtualGatewayStream = virtualGatewayDao.getVirtualGatewayByConditions(query).stream();
        if (query.getServiceId() != null){
            ServiceDto serviceDto = serviceInfoService.get(query.getServiceId());
            if (serviceDto != null){
                virtualGatewayStream = virtualGatewayStream.filter(o -> serviceTypeFilter(o, serviceDto.getServiceType()));
            }
        }
        return virtualGatewayStream.map(this::toView).collect(Collectors.toList());
    }

    private boolean serviceTypeFilter(VirtualGateway virtualGateway, String servicType){
        if (StringUtils.isBlank(servicType)){
            return false;
        }
        if (Const.HTTP.equals(servicType)){
            return true;
        }

        return !virtualGateway.getType().equalsIgnoreCase(LOAD_BALANCE);
    }

    @Override
    public Integer countVirtualGatewayByConditions(QueryVirtualGatewayDto query) {
        List<Long> projectIdList = query.getProjectIdList();
        //当项目检索条件不为空，但长度为0,认为租户下无项目,返回空数据
        if (projectIdList != null && projectIdList.size() == 0) {
            return NumberUtils.INTEGER_ZERO;
        }
        return virtualGatewayDao.countVirtualGatewayByConditions(query);
    }
}