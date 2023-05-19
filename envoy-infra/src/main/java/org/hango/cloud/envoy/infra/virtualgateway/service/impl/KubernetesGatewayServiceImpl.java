package org.hango.cloud.envoy.infra.virtualgateway.service.impl;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.util.YamlUtil;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayDTO;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayHttpRouteDTO;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayInfo;
import org.hango.cloud.envoy.infra.virtualgateway.rpc.VirtualGatewayRpcService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IKubernetesGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.gdashboard.api.util.Const.*;

/**
 * @Author zhufengwei
 * @Date 2022/12/6
 */
@Slf4j
@Service
public class KubernetesGatewayServiceImpl implements IKubernetesGatewayService {

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private VirtualGatewayRpcService virtualGatewayRpcService;

    @Autowired
    private IPluginManagerService pluginManagerService;


    @Override
    public ErrorCode refreshK8sGateway() {
        List<? extends GatewayDto> gatewayDtos = gatewayService.findAll();
        for (GatewayDto gatewayDto : gatewayDtos) {
            ErrorCode errorCode = doRefreshK8sGateway(gatewayDto);
            if (!CommonErrorCode.SUCCESS.equals(errorCode)){
                return errorCode;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<KubernetesGatewayDTO> getKubernetesGatewayList(Long virtualGatewayId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        if (virtualGatewayDto == null || CollectionUtils.isEmpty(virtualGatewayDto.getDomainInfos())){
            return new ArrayList<>();
        }
        List<KubernetesGatewayDTO> kubernetesGatewayDTOS = new ArrayList<>();
        for (DomainInfoDTO domainInfoDTO : virtualGatewayDto.getDomainInfos()) {
            KubernetesGatewayDTO kubernetesGatewayDTO = new KubernetesGatewayDTO();
            kubernetesGatewayDTO.setDomainId(domainInfoDTO.getId());
            kubernetesGatewayDTO.setHostname(domainInfoDTO.getHost());
            List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(virtualGatewayId, String.valueOf(domainInfoDTO.getId()), PluginBindingInfo.BINDING_OBJECT_TYPE_HOST);
            kubernetesGatewayDTO.setPluginBindingDtos(pluginBindingList);
            kubernetesGatewayDTOS.add(kubernetesGatewayDTO);
        }
        return kubernetesGatewayDTOS;
    }

    @Override
    public String getKubernetesGatewayYaml(Long virtualGatewayId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        if (virtualGatewayDto == null){
            return StringUtils.EMPTY;
        }
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (gatewayDto == null){
            return StringUtils.EMPTY;
        }
        List<KubernetesGatewayInfo> kubernetesGateway = virtualGatewayRpcService.getKubernetesGateway(gatewayDto.getConfAddr(), virtualGatewayDto.getName());
        if (CollectionUtils.isEmpty(kubernetesGateway)){
            return StringUtils.EMPTY;
        }
        return kubernetesGateway.get(0).getContent();
    }

    @Override
    public List<KubernetesGatewayHttpRouteDTO> getKubernetesGatewayHTTPRouteList(Long virtualGatewayId) {
        List<KubernetesGatewayHttpRouteDTO> result = new ArrayList<>();
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        log.info("getKubernetesGatewayHTTPRouteList virtualGatewayDto: {}", virtualGatewayDto);
        if (virtualGatewayDto == null) {
            return result;
        }
        List<HTTPRoute> kubernetesGatewayHttpRouteList = virtualGatewayRpcService.getKubernetesGatewayHttpRoute(virtualGatewayDto.getConfAddr(), virtualGatewayDto.getName());

        if (CollectionUtils.isEmpty(kubernetesGatewayHttpRouteList)) {
            log.warn("kubernetesGatewayHttpRouteList isEmpty ");
            return result;
        }
        List<KubernetesGatewayHttpRouteDTO> httpRouteDTOList = httpRouteListToView(kubernetesGatewayHttpRouteList);
        result.addAll(httpRouteDTOList);
        return result;
    }

    public List<KubernetesGatewayHttpRouteDTO> httpRouteListToView(List<HTTPRoute> kubernetesGatewayHttpRouteList) {
        return kubernetesGatewayHttpRouteList.stream().map(httpRoute -> {
            KubernetesGatewayHttpRouteDTO routeDTO = new KubernetesGatewayHttpRouteDTO();
            routeDTO.setRouteName(httpRoute.getMetadata().getName());
            routeDTO.setRouteHosts(httpRoute.getSpec().getHostnames());
            routeDTO.setYamlStr(new YamlUtil<>().obj2yaml(httpRoute));
            List<HTTPRouteRule> rules = httpRoute.getSpec().getRules();
            if (!CollectionUtils.isEmpty(rules)) {
                List<KubernetesGatewayHttpRouteDTO.Rule> ruleDTOList = httpRouteRuleToDTO(rules);
                routeDTO.setRules(ruleDTOList);
            }
            return routeDTO;
        }).collect(Collectors.toList());
    }


    private ErrorCode doRefreshK8sGateway(GatewayDto gatewayDto){
        //数据查询
        List<KubernetesGatewayInfo> targetResources = virtualGatewayRpcService.getKubernetesGateway(gatewayDto.getConfAddr(), null);
        //数据校验
        ErrorCode errorCode = portDuplicateCheck(targetResources, gatewayDto.getId());
        if (CommonErrorCode.SUCCESS != errorCode){
            return errorCode;
        }
        //数据过滤
        targetResources = targetResources.stream().filter(this::refreshFilter).collect(Collectors.toList());
        List<VirtualGatewayDto> dbResources = virtualGatewayInfoService.getVirtualGatewayList(gatewayDto.getId(), KUBERNETES_GATEWAY);
        //数据聚合，集合需要删除、创建或更新的数据
        Map<String, KubernetesGatewayInfo> targetResourceMap = targetResources.stream()
                .filter(o -> StringUtils.isNotBlank(o.getName()))
                .collect(Collectors.groupingBy(KubernetesGatewayInfo::getName,
                        Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))
                ));
        Map<String, VirtualGatewayDto> dbResourceMap = dbResources.stream()
                .filter(o -> StringUtils.isNotBlank(o.getName()))
                .collect(Collectors.groupingBy(VirtualGatewayDto::getName,
                        Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))
                ));
        Set<String> allNames = getAllResourceName(targetResources, dbResources);
        List<VirtualGatewayDto> needDeleteVirtualGateway = new ArrayList<>();
        List<KubernetesGatewayInfo> needCreateVirtualGateway = new ArrayList<>();
        List<KubernetesGatewayInfo> needUpdateVirtualGateway = new ArrayList<>();
        for (String resourceName : allNames) {
            VirtualGatewayDto dbResource = dbResourceMap.get(resourceName);
            KubernetesGatewayInfo targetResource = targetResourceMap.get(resourceName);
            if (dbResource == null){
                needCreateVirtualGateway.add(targetResource);
            } else if (targetResource == null) {
                needDeleteVirtualGateway.add(dbResource);
            }else {
                targetResource.setVirtualGatewayId(dbResource.getId());
                needUpdateVirtualGateway.add(targetResource);
            }
        }
        //数据处理
        handleCreateResource(needCreateVirtualGateway, gatewayDto);
        handleUpdateResource(needUpdateVirtualGateway, gatewayDto);
        handleDeleteResource(needDeleteVirtualGateway);
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode portDuplicateCheck(List<KubernetesGatewayInfo> kubernetesGatewayInfos, long gwId){
        //校验k8s gateway之间是否存在端口冲突
        long count = kubernetesGatewayInfos.stream().map(KubernetesGatewayInfo::getPort).distinct().count();
        if (count < kubernetesGatewayInfos.size()){
            return CommonErrorCode.invalidParameter("kubernetes gateway 端口冲突，不允许刷新");
        }
        List<VirtualGatewayDto> dbResources = virtualGatewayInfoService.getVirtualGatewayList(Collections.singletonList(gwId)).stream()
                .filter(o -> !Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS).contains(o.getType())).collect(Collectors.toList());
        Set<Integer> dbPorts = dbResources.stream().map(VirtualGatewayDto::getPort).collect(Collectors.toSet());
        for (KubernetesGatewayInfo kubernetesGatewayInfo : kubernetesGatewayInfos) {
            if (dbPorts.contains(kubernetesGatewayInfo.getPort())){
                String format = String.format("%s端口号(%s)冲突", kubernetesGatewayInfo.getName(), kubernetesGatewayInfo.getPort());
                return CommonErrorCode.invalidParameter(format);
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private boolean refreshFilter(KubernetesGatewayInfo gatewayInfo){
        if (gatewayInfo == null){
            log.warn("gateway filter|gatewayInfo is null");
            return false;
        }
        if (StringUtils.isBlank(gatewayInfo.getName())){
            log.warn("gateway filter|name is null");
            return false;
        }
        String protocol = gatewayInfo.getProtocol();
        if (StringUtils.isBlank(protocol)){
            log.warn("gateway filter|protocol is null|name:{}", gatewayInfo.getName());
            return false;
        }
        if (!HTTP.equalsIgnoreCase(protocol) && !HTTP.equalsIgnoreCase(protocol)){
            log.warn("gateway filter|protocol is error|name:{}, protocol:{}", gatewayInfo.getName(), protocol);
            return false;
        }
        if (CollectionUtils.isEmpty(gatewayInfo.getRouteHosts())){
            log.warn("gateway filter|route host is null|name:{}", gatewayInfo.getName());
            return false;
        }
        if (StringUtils.isBlank(gatewayInfo.getHost())){
            log.warn("gateway filter|host is null|name:{}", gatewayInfo.getName());
            return false;
        }
        return true;
    }

    private void handleDeleteResource(List<VirtualGatewayDto> virtualGatewayDtos){
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            if (!CollectionUtils.isEmpty(virtualGatewayDto.getDomainInfos())){
                for (DomainInfoDTO domainInfoDTO : virtualGatewayDto.getDomainInfos()) {
                    //删除插件
                    List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(virtualGatewayDto.getId(), String.valueOf(domainInfoDTO.getId()), PluginBindingInfo.BINDING_OBJECT_TYPE_HOST);
                    pluginBindingList.forEach(pluginInfoService::delete);
                    //删除域名
                    domainInfoService.delete(domainInfoDTO);
                }
            }
            //删除plm
            pluginManagerService.offlinePluginManager(virtualGatewayDto);
            //删除虚拟网关
            virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
        }
    }

    private void handleCreateResource(List<KubernetesGatewayInfo> kubernetesGatewayInfos, GatewayDto gatewayDto){
        for (KubernetesGatewayInfo kubernetesGatewayInfo : kubernetesGatewayInfos) {
            VirtualGatewayDto virtualGatewayDto = transByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            //创建域名
            List<DomainInfoDTO> domainInfoDTOS = transDomainByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            domainInfoDTOS.forEach(domainInfoService::create);
            //创建虚拟网关
            virtualGatewayDto.setDomainInfos(domainInfoDTOS);
            virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);
            //创建plm
            pluginManagerService.publishPluginManager(virtualGatewayDto);
        }
    }

    private void handleUpdateResource(List<KubernetesGatewayInfo> kubernetesGatewayInfos, GatewayDto gatewayDto){
        for (KubernetesGatewayInfo kubernetesGatewayInfo : kubernetesGatewayInfos) {
            VirtualGatewayDto virtualGatewayDto = transByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            //更新域名
            List<DomainInfoDTO> domainInfoDTOS = handleRefreshDomain(kubernetesGatewayInfo, gatewayDto);
            //更新虚拟网关
            virtualGatewayDto.setDomainInfos(domainInfoDTOS);
            virtualGatewayInfoService.updateWithoutHooker(virtualGatewayDto);
            //更新plm
            pluginManagerService.publishPluginManager(virtualGatewayDto);

        }
    }

    private List<DomainInfoDTO> handleRefreshDomain(KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        List<String> targetHosts = kubernetesGatewayInfo.getRouteHosts();
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(kubernetesGatewayInfo.getVirtualGatewayId());
        List<DomainInfoDTO> dbDomains = virtualGatewayDto.getDomainInfos();
        for (DomainInfoDTO dbDomain : dbDomains) {
            if (!targetHosts.contains(dbDomain.getHost())){
                //删除插件
                List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(kubernetesGatewayInfo.getVirtualGatewayId(), String.valueOf(dbDomain.getId()), PluginBindingInfo.BINDING_OBJECT_TYPE_HOST);
                pluginBindingList.forEach(pluginInfoService::delete);
                //删除域名
                domainInfoService.delete(dbDomain);
            }
        }
        Map<String, DomainInfoDTO> dbResourceMap = dbDomains.stream().filter(o -> StringUtils.isNotBlank(o.getHost())).collect(
                Collectors.groupingBy(DomainInfoDTO::getHost, Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))));

        //创建域名
        List<DomainInfoDTO> addedDomainDTOS = targetHosts.stream()
                .filter(o -> !dbResourceMap.containsKey(o))
                .map(o -> transDomainInfo(o, kubernetesGatewayInfo, gatewayDto))
                .peek(domainInfoService::create)
                .collect(Collectors.toList());
        //获取目标域名
        List<DomainInfoDTO> existDomainDTOS  = targetHosts.stream().map(dbResourceMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        List<DomainInfoDTO> targetDomainDTOS = new ArrayList<>();
        targetDomainDTOS.addAll(existDomainDTOS);
        targetDomainDTOS.addAll(addedDomainDTOS);
        return targetDomainDTOS;
    }

    private VirtualGatewayDto transByK8sGateway(KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setGwId(gatewayDto.getId());
        virtualGatewayDto.setName(kubernetesGatewayInfo.getName());
        virtualGatewayDto.setCode(kubernetesGatewayInfo.getName());
        if (kubernetesGatewayInfo.getVirtualGatewayId() != null){
            virtualGatewayDto.setId(kubernetesGatewayInfo.getVirtualGatewayId());
        }
        virtualGatewayDto.setProjectIdList(Collections.singletonList(kubernetesGatewayInfo.getProjectId()));
        virtualGatewayDto.setDescription(AUTO_GENERATE);
        virtualGatewayDto.setType(KUBERNETES_GATEWAY);
        virtualGatewayDto.setProtocol(kubernetesGatewayInfo.getProtocol());
        virtualGatewayDto.setPort(kubernetesGatewayInfo.getPort());
        return virtualGatewayDto;
    }

    private List<DomainInfoDTO> transDomainByK8sGateway(KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        List<DomainInfoDTO> domainInfoDTOS = new ArrayList<>();
        //更新域名
        List<String> routeHosts = kubernetesGatewayInfo.getRouteHosts();
        if (!CollectionUtils.isEmpty(routeHosts)){
            for (String routeHost : routeHosts) {
                domainInfoDTOS.add(transDomainInfo(routeHost, kubernetesGatewayInfo, gatewayDto));
            }
        }
        return domainInfoDTOS;
    }

    private DomainInfoDTO transDomainInfo(String routeHost, KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setHost(routeHost);
        domainInfoDTO.setProjectId(kubernetesGatewayInfo.getProjectId());
        domainInfoDTO.setProtocol(kubernetesGatewayInfo.getProtocol());
        domainInfoDTO.setStatus(DomainStatusEnum.RelevanceOnly.name());
        domainInfoDTO.setDescription(AUTO_GENERATE);
        return domainInfoDTO;
    }



    private Set<String> getAllResourceName(List<KubernetesGatewayInfo> targetKubernetesGateway, List<VirtualGatewayDto> dbVirtialGateway){
        Set<String> resourceNames = new HashSet<>();
        List<String> targetResourceName = targetKubernetesGateway.stream().map(KubernetesGatewayInfo::getName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        List<String>dbResourceName = dbVirtialGateway.stream().map(VirtualGatewayDto::getName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(targetResourceName)){
            resourceNames.addAll(targetResourceName);
        }
        if (!CollectionUtils.isEmpty(dbResourceName)){
            resourceNames.addAll(dbResourceName);
        }
        return resourceNames;
    }

    private List<KubernetesGatewayHttpRouteDTO.Rule> httpRouteRuleToDTO(List<HTTPRouteRule> rules) {
        return rules.stream().map(rule -> {
            KubernetesGatewayHttpRouteDTO.Rule dtoRule = new KubernetesGatewayHttpRouteDTO.Rule();
            //封装backend
            List<HTTPBackendRef> backendRefs = rule.getBackendRefs();
            if (!CollectionUtils.isEmpty(backendRefs)) {
                List<KubernetesGatewayHttpRouteDTO.BackendRef> backendRefDTOList = this.backendRefsToDTO(backendRefs);
                dtoRule.setBackendRefs(backendRefDTOList);
            }
            //封装matches
            List<HTTPRouteMatch> matches = rule.getMatches();
            if (!CollectionUtils.isEmpty(matches)) {
                List<KubernetesGatewayHttpRouteDTO.Match> matchesDTO = matchesToDTO(matches);
                dtoRule.setMatches(matchesDTO);
            }
            //封装filter
            List<HTTPRouteFilter> filters = rule.getFilters();
            if (!CollectionUtils.isEmpty(filters)) {
                List<KubernetesGatewayHttpRouteDTO.Filter> filterDTOList = httpRouteFilterToDTO(filters);
                dtoRule.setFilters(filterDTOList);
            }
            return dtoRule;
        }).collect(Collectors.toList());
    }

    private List<KubernetesGatewayHttpRouteDTO.BackendRef> backendRefsToDTO(List<HTTPBackendRef> backendRefs) {
        return backendRefs.stream().map(backendRef -> {
            KubernetesGatewayHttpRouteDTO.BackendRef backendRefDTO = new KubernetesGatewayHttpRouteDTO.BackendRef();
            backendRefDTO.setName(backendRef.getName());
            backendRefDTO.setWeight(backendRef.getWeight());
            backendRefDTO.setPort(backendRef.getPort());
            return backendRefDTO;
        }).collect(Collectors.toList());
    }

    private List<KubernetesGatewayHttpRouteDTO.Match> matchesToDTO(List<HTTPRouteMatch> matches) {
        return matches.stream().filter(httpRouteMatch -> httpRouteMatch.getPath() != null).map(match -> {
            KubernetesGatewayHttpRouteDTO.Match matchDTO = new KubernetesGatewayHttpRouteDTO.Match();
            matchDTO.setType(match.getPath().getType());
            matchDTO.setValue(match.getPath().getValue());
            return matchDTO;
        }).collect(Collectors.toList());
    }

    private List<KubernetesGatewayHttpRouteDTO.Filter> httpRouteFilterToDTO(List<HTTPRouteFilter> filters) {
        return filters.stream().map(filter -> {
            KubernetesGatewayHttpRouteDTO.Filter filterDTO = new KubernetesGatewayHttpRouteDTO.Filter();
            filterDTO.setType(filter.getType());
            return filterDTO;
        }).collect(Collectors.toList());
    }
}
