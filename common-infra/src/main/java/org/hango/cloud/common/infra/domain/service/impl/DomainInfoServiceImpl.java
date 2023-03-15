package org.hango.cloud.common.infra.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainRefreshResult;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;
import static org.hango.cloud.common.infra.domain.enums.DomainStatusEnum.Active;
import static org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL;
import static org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE;

@Slf4j
@Service
public class DomainInfoServiceImpl implements IDomainInfoService {

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private CertificateInfoMapper certificateInfoMapper;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IPluginInfoService pluginInfoService;


    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Autowired
    private IVirtualGatewayDao virtualGatewayDao;

    @Autowired
    private IGatewayService gatewayService;


    public static final String REFRESH_RESULT = "RefreshResult";

    @Override
    public long create(DomainInfoDTO domainInfoDTO){
        //设置默认属性
        fillCreateInfo(domainInfoDTO);
        //创建域名
        DomainInfoPO domainInfoPO = toMeta(domainInfoDTO);
        domainInfoMapper.insert(domainInfoPO);
        return domainInfoPO.getId();
    }

    //设置初始化状态
    private void fillCreateInfo(DomainInfoDTO domainInfoDTO){
        if (StringUtils.isNotBlank(domainInfoDTO.getStatus())){
            return;
        }
        Boolean active = virtualGatewayInfoService.existManagedVirtualGateway(domainInfoDTO.getProjectId(), domainInfoDTO.getEnv(), domainInfoDTO.getProtocol());
        if (active){
            domainInfoDTO.setStatus(DomainStatusEnum.NotActive.name());
        }else {
            domainInfoDTO.setStatus(DomainStatusEnum.NotUse.name());
        }
    }




    @Override
    public long update(DomainInfoDTO domainInfoDTO){
        DomainInfoPO domainInfoPO = toMeta(domainInfoDTO);
        //更新域名信息
        domainInfoMapper.updateById(domainInfoPO);
        return domainInfoPO.getId();
    }


    @Override
    public void delete(DomainInfoDTO domainInfoDTO){
        DomainInfoPO domainInfoPO = domainInfoMapper.selectById(domainInfoDTO);
        if (DomainStatusEnum.enable(domainInfoPO.getStatus())){
            domainInfoPO.setStatus(DomainStatusEnum.WaitDelete.name());
            domainInfoMapper.updateById(domainInfoPO);
        }else {
            domainInfoMapper.deleteById(domainInfoDTO.getId());

        }
    }


    @Override
    public void createDomainInfoByVgId(long projectId,List<String> virtualHostList,long virtualGatewayId) {
        //hango虚拟网关创建的时候需要创建该虚拟网关对应的项目以及对其进行绑定，触发hook配置下发更新
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        virtualGatewayDto.setProjectIdList(Collections.singletonList(projectId));
        virtualGatewayDto.setProtocol(virtualGatewayDto.getProtocol());
        virtualGatewayInfoService.update(virtualGatewayDto);
        VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto();
        virtualGatewayBindDto.setProjectIdList(Collections.singletonList(projectId));
        virtualGatewayBindDto.setVirtualGwId(virtualGatewayId);

        if (!CollectionUtils.isEmpty(virtualHostList)) {
            virtualHostList.forEach(host -> {
                DomainInfoDTO domainInfoDTO = getDomainInfoDTO(virtualGatewayDto, host);
                DomainInfoPO domainInfoPO = toMeta(domainInfoDTO);
                domainInfoMapper.insert(domainInfoPO);
            });
            //虚拟网关绑定域名
            virtualGatewayProjectService.bindProject(virtualGatewayBindDto);
            virtualGatewayProjectService.updateBindDomainStatus(virtualGatewayBindDto);
        }
    }

    @Override
    public Page<DomainInfoPO> getDomainInfoPage(long projectId, String host, int offset, int limit) {
        LambdaQueryWrapper<DomainInfoPO> query = Wrappers.lambdaQuery();
        query.eq(DomainInfoPO::getProjectId, projectId);
        if (StringUtils.isNotBlank(host)){
            query.like(DomainInfoPO::getHost, host);
        }
        query.ne(DomainInfoPO::getStatus, DomainStatusEnum.RelevanceOnly.name());
        return domainInfoMapper.selectPage(PageUtil.of(limit, offset), query);

    }

    @Override
    public List<DomainInfoDTO> getDomainInfos(List<Long> projectIds, String protocol, String env) {
        LambdaQueryWrapper<DomainInfoPO> query = Wrappers.lambdaQuery();
        query.eq(DomainInfoPO::getProtocol, protocol);
        query.in(DomainInfoPO::getProjectId, projectIds);
        query.and(
                StringUtils.isNotEmpty(env),
                wrapper -> wrapper.isNull(DomainInfoPO::getEnv).or().eq(DomainInfoPO::getEnv, env)
        );
        query.ne(DomainInfoPO::getStatus, DomainStatusEnum.RelevanceOnly.name());
        List<DomainInfoPO> domainInfoPOS = domainInfoMapper.selectList(query);
        return domainInfoPOS.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<DomainInfoDTO> getRelevanceOnlyDomainInfos(long virtualGatewayId) {
        LambdaQueryWrapper<DomainInfoPO> query = Wrappers.lambdaQuery();
        query.eq(DomainInfoPO::getStatus, DomainStatusEnum.RelevanceOnly.name());
        query.in(DomainInfoPO::getRelevanceId, virtualGatewayId);
        List<DomainInfoPO> domainInfoPOS = domainInfoMapper.selectList(query);
        return domainInfoPOS.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<String> getHosts(long projectId, long virtualGatewayId) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(virtualGatewayId);
        if (virtualGateway == null){
            return new ArrayList<>();
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        return getDomainInfos(Collections.singletonList(projectId), virtualGateway.getProtocol(), gatewayDto.getEnvId()).stream().map(DomainInfoDTO::getHost).collect(Collectors.toList());
    }

    @Override
    public List<String> getEnableHosts(long projectId, long virtualGatewayId) {
        VirtualGateway virtualGateway = virtualGatewayDao.get(virtualGatewayId);
        if (virtualGateway == null){
            return new ArrayList<>();
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGateway.getGwId());
        return getDomainInfos(Collections.singletonList(projectId), virtualGateway.getProtocol(), gatewayDto.getEnvId()).stream()
                .filter(o -> !DomainStatusEnum.WaitDelete.name().equals(o.getStatus()))
                .map(DomainInfoDTO::getHost)
                .collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkCreateParam(DomainInfoDTO domainInfoDTO){
        ErrorCode errorCode = paramCheck(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        String host = domainInfoDTO.getHost();
        if (host.startsWith("*")){
            return CommonErrorCode.invalidParameter(  "不支持泛域名 " + host);
        }
        DomainInfoPO domainInfoPO = DomainInfoPO.builder().host(host).build();
        Long count = domainInfoMapper.selectCount(new QueryWrapper<>(domainInfoPO));
        if (count > 0){
            return CommonErrorCode.invalidParameter("域名已存在，不允许重复创建");
        }

        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(DomainInfoDTO domainInfoDTO){
        ErrorCode errorCode = paramCheck(domainInfoDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        if (domainInfoDTO.getId() == null || domainInfoDTO.getId() <= 0){
            return CommonErrorCode.invalidParameter("域名id不能为空");
        }
        DomainInfoPO domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
        if (domainInfoPO == null){
            return CommonErrorCode.invalidParameter("域名不存在，更新域名信息失败");
        }
        if (SCHEME_HTTPS.equals(domainInfoDTO.getProtocol()) && domainInfoDTO.getCertificateId() == null){
            return CommonErrorCode.invalidParameter("HTTPS域名必须携带证书");
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public ErrorCode checkDeleteParam(DomainInfoDTO domainInfoDTO){
        DomainInfoPO domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
        if (domainInfoPO == null){
            return CommonErrorCode.invalidParameter("未找到需要删除的域名");
        }
        if (DomainStatusEnum.WaitDelete.name().equals(domainInfoPO.getStatus())){
            return CommonErrorCode.invalidParameter("域名已等待下线，请勿重复操作");
        }
        if (DomainStatusEnum.Active.name().equals(domainInfoPO.getStatus())){
            List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getManagedVirtualGatewayList(
                    domainInfoPO.getProjectId(), domainInfoPO.getEnv(), domainInfoPO.getProtocol());
            for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
                List<String> enableHosts = getEnableHosts(domainInfoPO.getProjectId(), virtualGatewayDto.getId());
                if (enableHosts.size() == 1 && enableHosts.get(0).equals(domainInfoPO.getHost())){
                    return CommonErrorCode.invalidParameter(String.format("网关(%s)只存在当前域名，不允许删除", virtualGatewayDto.getName()));
                }

            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode paramCheck(DomainInfoDTO domainInfoDTO){
        if (SCHEME_HTTPS.equals(domainInfoDTO.getProtocol()) && domainInfoDTO.getCertificateId() == null){
            return CommonErrorCode.invalidParameter("HTTPS域名必须携带证书");
        }
        if (domainInfoDTO.getCertificateId() != null){
            CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(domainInfoDTO.getCertificateId());
            if (certificateInfoPO == null){
                return CommonErrorCode.invalidParameter("无效的证书id");
            }
            if (StringUtils.isBlank(certificateInfoPO.getPrivateKey())){
                return CommonErrorCode.invalidParameter("未上传服务器私钥");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public DomainInfoPO toMeta(DomainInfoDTO domainInfoDTO){
        return DomainInfoPO.builder()
                .env(domainInfoDTO.getEnv())
                .description(domainInfoDTO.getDescription())
                .status(domainInfoDTO.getStatus())
                .protocol(domainInfoDTO.getProtocol())
                .id(domainInfoDTO.getId())
                .host(domainInfoDTO.getHost())
                .projectId(domainInfoDTO.getProjectId())
                .certificateId(domainInfoDTO.getCertificateId())
                .relevanceId(domainInfoDTO.getRelevanceId())
                .build();
    }

    @Override
    public DomainInfoDTO toView(DomainInfoPO domainInfoPO) {
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setEnv(domainInfoPO.getEnv());
        domainInfoDTO.setDescription(domainInfoPO.getDescription());
        domainInfoDTO.setStatus(domainInfoPO.getStatus());
        domainInfoDTO.setStatusDesc(DomainStatusEnum.valueOf(domainInfoPO.getStatus()).getDesc());
        domainInfoDTO.setProtocol(domainInfoPO.getProtocol());
        domainInfoDTO.setId(domainInfoPO.getId());
        domainInfoDTO.setHost(domainInfoPO.getHost());
        domainInfoDTO.setCertificateId(domainInfoPO.getCertificateId());
        Long certificateId = domainInfoPO.getCertificateId();
        if (certificateId != null){
            CertificateInfoPO certificateInfoPO = certificateInfoMapper.selectById(domainInfoPO.getCertificateId());
            domainInfoDTO.setCertificateName(certificateInfoPO.getName());
        }
        domainInfoDTO.setProjectId(domainInfoPO.getProjectId());
        domainInfoDTO.setRelevanceId(domainInfoPO.getRelevanceId());
        return domainInfoDTO;
    }


    @Override
    public List<DomainRefreshResult> getDomainRefreshResult(long projectId) {
        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getGwEnvByProjectId(projectId);
        if(CollectionUtils.isEmpty(virtualGatewayDtos)){
            return null;
        }
        List<DomainRefreshResult> results = new ArrayList<>();
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            DomainRefreshResult domainRefreshResult = new DomainRefreshResult();
            //查询虚拟网关数据
            domainRefreshResult.setVirtualGwId(virtualGatewayDto.getId());
            domainRefreshResult.setVirtualGwName(virtualGatewayDto.getName());
            domainRefreshResult.setEnv(virtualGatewayDto.getEnvId());
            //查询路由名称
            RouteRuleQuery query = RouteRuleQuery.builder().virtualGwId(virtualGatewayDto.getId()).build();
            List<RouteRuleProxyDto> routeRuleProxyDtos = routeRuleProxyService.getRouteRuleProxyList(query);
            List<Long> routeIds = routeRuleProxyDtos.stream().map(RouteRuleProxyDto::getRouteRuleId).collect(Collectors.toList());
            List<String> routeNames = routeRuleInfoService.getRouteRuleList(routeIds).stream().map(RouteRuleDto::getRouteRuleName).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(routeNames)){
                domainRefreshResult.setRouteProxyNames(routeNames);
            }
            //查询项目插件
            List<String> pluginTypes = pluginInfoService.getEnablePluginBindingList(virtualGatewayDto.getId(), String.valueOf(projectId),
                    BINDING_OBJECT_TYPE_ROUTE_RULE).stream().map(PluginBindingInfo::getPluginType).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pluginTypes)){
                domainRefreshResult.setPluginTypes(pluginTypes);
            }
            results.add(domainRefreshResult);
        }
        return results;
    }


    @Override
    public List<DomainRefreshResult> refreshDomain(long projectId) {
        List<DomainRefreshResult> failRes = new ArrayList<>();
        //查询虚拟网关
        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getGwEnvByProjectId(projectId);
        if(CollectionUtils.isEmpty(virtualGatewayDtos)){
            return new ArrayList<>();
        }
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            Map<String, Object> freshResult = doRefresh(virtualGatewayDto, projectId);
            if (CollectionUtils.isEmpty(freshResult)){
                updateDomainStatus(projectId, virtualGatewayDto.getGwId(), virtualGatewayDto.getProtocol());
            }else {
                DomainRefreshResult result = new DomainRefreshResult();
                result.setEnv(virtualGatewayDto.getEnvId());
                result.setVirtualGwId(virtualGatewayDto.getId());
                result.setVirtualGwName(virtualGatewayDto.getName());
                result.setFailedResult(freshResult);
                failRes.add(result);
            }
        }
        return failRes;
    }

    @Override
    public void updateDomainInfoByVg(long projectId,List<String> virtualHostList, long virtualGatewayId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto();
        virtualGatewayBindDto.setProjectIdList(virtualGatewayDto.getProjectIdList());
        virtualGatewayBindDto.setVirtualGwId(virtualGatewayDto.getId());
        virtualGatewayDto.setProjectIdList(Collections.singletonList(projectId));
        //先取出已有的域名
        List<DomainInfoPO> domainInfoPOCollect = domainInfoMapper.selectList(new LambdaQueryWrapper<DomainInfoPO>()
                .eq(DomainInfoPO::getProjectId, projectId));
        //数据库中该项目下的的域名
        Set<String> domainInfoSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(domainInfoPOCollect)) {
            domainInfoSet.addAll(domainInfoPOCollect.stream().map(DomainInfoPO::getHost).collect(Collectors.toSet()));
        }
        //取出需要删除的域名，是指数据库中的域名不存在于传入的域名列表
        domainInfoSet.forEach(host -> {
            if (!virtualHostList.contains(host)) {
                domainInfoMapper.delete(new LambdaQueryWrapper<DomainInfoPO>().eq(DomainInfoPO::getHost,host).eq(DomainInfoPO::getProjectId,virtualGatewayDto.getProjectIdList().get(0)));
            }
        });
        //插入新增的域名
        virtualHostList.forEach(host -> {
            if (!domainInfoSet.contains(host)) {
                DomainInfoDTO domainInfoDTO = getDomainInfoDTO(virtualGatewayDto, host);
                DomainInfoPO domainInfoPO = toMeta(domainInfoDTO);
                domainInfoMapper.insert(domainInfoPO);
            }
        });
        //虚拟网关绑定域名
        virtualGatewayProjectService.bindProject(virtualGatewayBindDto);
        virtualGatewayProjectService.updateBindDomainStatus(virtualGatewayBindDto);

    }

    @Override
    public long deleteDomainInfoByVgId(long projectId,long virtualGatewayId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        //虚拟网关解绑域名
        VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto();
        virtualGatewayBindDto.setProjectIdList(virtualGatewayDto.getProjectIdList());
        virtualGatewayBindDto.setVirtualGwId(virtualGatewayDto.getId());
        virtualGatewayProjectService.unbindProject(virtualGatewayDto.getId(), projectId);
        virtualGatewayProjectService.updateUnbindDomainStatus(virtualGatewayDto.getId(), projectId);
        return domainInfoMapper.delete(new LambdaQueryWrapper<DomainInfoPO>().eq(DomainInfoPO::getProjectId, projectId));
    }

    private DomainInfoDTO getDomainInfoDTO(VirtualGatewayDto virtualGatewayDto, String host) {
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setProtocol(virtualGatewayDto.getProtocol());
        domainInfoDTO.setHost(host);
        domainInfoDTO.setStatus(DomainStatusEnum.NotUse.name());
        domainInfoDTO.setEnv(virtualGatewayDto.getEnvId());
        if (!CollectionUtils.isEmpty(virtualGatewayDto.getProjectIdList())){
            domainInfoDTO.setProjectId(virtualGatewayDto.getProjectIdList().get(0));
        }
        return domainInfoDTO;
    }


    private void updateDomainStatus(long projectId, long gwId, String protocol){
        GatewayDto gatewayDto = gatewayService.get(gwId);
        List<DomainInfoDTO> domainInfoDTOS = getDomainInfos(Collections.singletonList(projectId), protocol, gatewayDto.getEnvId());
        for (DomainInfoDTO domainInfoDTO : domainInfoDTOS) {
            switch (DomainStatusEnum.getByName(domainInfoDTO.getStatus())){
                case WaitDelete:
                    domainInfoMapper.deleteById(domainInfoDTO.getId());
                    break;
                case NotActive:
                    DomainInfoPO infoPO = DomainInfoPO.builder().id(domainInfoDTO.getId()).status(Active.name()).build();
                    domainInfoMapper.updateById(infoPO);
                    break;
                default:
                    break;
            }
        }

    }




    private Map<String, Object> doRefresh(VirtualGatewayDto virtualGatewayDto, long projectId){
        Map<String, Object> result = new HashMap<>();
        //刷新路由
        RouteRuleQuery query = RouteRuleQuery.builder().virtualGwId(virtualGatewayDto.getId()).build();
        List<RouteRuleProxyDto> routeRuleProxyDtos = routeRuleProxyService.getRouteRuleProxyList(query);
        List<String> failedRouteNames = new ArrayList<>();
        for (RouteRuleProxyDto routeRuleProxyDto : routeRuleProxyDtos) {
            long updateResult = routeRuleProxyService.update(routeRuleProxyDto);
            if (BaseConst.ERROR_RESULT == updateResult){
                RouteRuleDto routeRuleDto = routeRuleInfoService.get(routeRuleProxyDto.getRouteRuleId());
                failedRouteNames.add(routeRuleDto.getRouteRuleName());
            }
        }
        if (CollectionUtils.isNotEmpty(failedRouteNames)){
            result.put(ROUTE, failedRouteNames);
        }
        //刷新项目插件
        List<String> failedProjectPluginTypes = new ArrayList<>();
        List<PluginBindingInfo> enablePluginBindingList = pluginInfoService.getEnablePluginBindingList(virtualGatewayDto.getId(), String.valueOf(projectId),
                BINDING_OBJECT_TYPE_GLOBAL);
        for (PluginBindingInfo pluginBindingInfo : enablePluginBindingList) {
            long updateResult = pluginInfoService.update(pluginInfoService.toView(pluginBindingInfo));
            if (BaseConst.ERROR_RESULT == updateResult){
                failedProjectPluginTypes.add(pluginBindingInfo.getPluginType());
            }
        }
        if (CollectionUtils.isNotEmpty(failedProjectPluginTypes)){
            result.put(PROJECT_PLUGIN, failedProjectPluginTypes);
        }
        VirtualGatewayBindDto virtualGatewayBindDto = new VirtualGatewayBindDto();
        virtualGatewayBindDto.setVirtualGwId(virtualGatewayDto.getId());
        virtualGatewayBindDto.setProjectIdList(virtualGatewayDto.getProjectIdList());
        //刷新虚拟网关
        long updateResult = virtualGatewayProjectService.bindProject(virtualGatewayBindDto);
        if (BaseConst.ERROR_RESULT == updateResult){
            result.put(VIRTUAL_GATEWAY, virtualGatewayDto.getGwName());
        }
        return result;
    }
}
