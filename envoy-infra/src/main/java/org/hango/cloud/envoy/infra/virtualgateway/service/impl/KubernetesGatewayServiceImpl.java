package org.hango.cloud.envoy.infra.virtualgateway.service.impl;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.hango.cloud.envoy.infra.virtualgateway.dto.*;
import org.hango.cloud.envoy.infra.virtualgateway.rpc.VirtualGatewayRpcService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IKubernetesGatewayService;
import org.hango.cloud.envoy.infra.virtualgateway.util.Trans;
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

    @Autowired
    private IKubernetesGatewayService kubernetesGatewayService;

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
        List<KubernetesGatewayHttpRouteDTO> httpRouteDTOList = Trans.httpRouteListToView(kubernetesGatewayHttpRouteList);
        result.addAll(httpRouteDTOList);
        return result;
    }


    @Override
    public IngressViewDTO getIngress(Long virtualGatewayId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        if (virtualGatewayDto == null){
            return null;
        }
        List<IngressDTO> ingress = virtualGatewayRpcService.getKubernetesIngress(virtualGatewayDto.getConfAddr(), virtualGatewayDto.getName());
        if (CollectionUtils.isEmpty(ingress)){
            return null;
        }
        IngressViewDTO ingressViewDTO = Trans.toView(virtualGatewayDto, ingress.get(0));
        if (ingressViewDTO == null || CollectionUtils.isEmpty(ingressViewDTO.getRules())){
            return null;
        }
        //查询插件信息
        fillPluginInfo(ingressViewDTO, virtualGatewayId);
        return ingressViewDTO;
    }

    @Override
    public void fillGatewayInfo(List<KubernetesGatewayInfo> gatewayInfoList) {
        return;
    }

    private void fillPluginInfo(IngressViewDTO ingressRuleViewDTO, Long virtualGatewayId){
        if (ingressRuleViewDTO == null){
            return;
        }
        List<IngressRuleViewDTO> rules = ingressRuleViewDTO.getRules();
        if (CollectionUtils.isEmpty(rules)){
            return;
        }
        for (IngressRuleViewDTO rule : rules) {
            if (rule == null || rule.getDomainId() == null){
                continue;
            }
            List<PluginBindingDto> pluginBindingList = pluginInfoService.getPluginBindingList(virtualGatewayId, String.valueOf(rule.getDomainId()), PluginBindingInfo.BINDING_OBJECT_TYPE_HOST);
            rule.setPluginBindingDtos(pluginBindingList);
        }

    }

    private ErrorCode doRefreshK8sGateway(GatewayDto gatewayDto){
        //数据查询
        List<KubernetesGatewayInfo> targetResources = getKubernetesGatewayList(gatewayDto.getConfAddr());
        //数据校验
        ErrorCode errorCode = portDuplicateCheck(targetResources, gatewayDto.getId());
        if (CommonErrorCode.SUCCESS != errorCode){
            return errorCode;
        }
        //数据过滤
        targetResources = targetResources.stream().filter(this::refreshFilter).collect(Collectors.toList());
        List<VirtualGatewayDto> dbResources = virtualGatewayInfoService.getKubernetesGatewayList(gatewayDto.getId());
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
        Set<String> allNames = Trans.mergeResourceName(targetResources, dbResources);
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

    /**
     * 获取kubernetes gateway，包括gateway api和
     * @return
     */
    private List<KubernetesGatewayInfo> getKubernetesGatewayList(String confAddr){
        //查询gateway api
        List<KubernetesGatewayInfo> targetResources = virtualGatewayRpcService.getKubernetesGateway(confAddr, null);
        //查询ingress
        List<IngressDTO> ingress = virtualGatewayRpcService.getKubernetesIngress(confAddr, null);
        if (CollectionUtils.isEmpty(ingress)){
            return targetResources;
        }
        List<KubernetesGatewayInfo> ingressGatewayList = ingress.stream().map(Trans::toGateway).filter(Objects::nonNull).collect(Collectors.toList());
        targetResources.addAll(ingressGatewayList);
        //设置projectId
        kubernetesGatewayService.fillGatewayInfo(targetResources);
        //过滤projectId
        targetResources = targetResources.stream().filter(o -> o.getProjectId() != null).collect(Collectors.toList());
        return targetResources;
    }

    private ErrorCode portDuplicateCheck(List<KubernetesGatewayInfo> kubernetesGatewayInfos, long gwId){
        List<Integer> k8sGatewayPortList = kubernetesGatewayInfos.stream().filter(o -> KUBERNETES_GATEWAY.equals(o.getType())).map(KubernetesGatewayInfo::getPort).distinct().collect(Collectors.toList());
        List<Integer> ingressPortList = kubernetesGatewayInfos.stream().filter(o -> KUBERNETES_INGRESS.equals(o.getType())).map(KubernetesGatewayInfo::getPort).distinct().collect(Collectors.toList());
        if (ingressPortList.stream().anyMatch(k8sGatewayPortList::contains) ){
            return CommonErrorCode.invalidParameter("kubernetes gateway和ingress端口冲突，不允许刷新");
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
        if (!Arrays.asList(HTTP, HTTPS).contains(protocol.toLowerCase())){
            log.warn("gateway filter|protocol is error|name:{}, protocol:{}", gatewayInfo.getName(), protocol);
            return false;
        }
        if (CollectionUtils.isEmpty(gatewayInfo.getRouteHosts())){
            log.warn("gateway filter|route host is null|name:{}", gatewayInfo.getName());
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
            deletePluginManager(virtualGatewayDto);
            //删除虚拟网关
            virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
        }
    }

    private void handleCreateResource(List<KubernetesGatewayInfo> kubernetesGatewayInfos, GatewayDto gatewayDto){
        for (KubernetesGatewayInfo kubernetesGatewayInfo : kubernetesGatewayInfos) {

            VirtualGatewayDto virtualGatewayDto = Trans.transByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            //创建plm
            publishPluginManager(virtualGatewayDto);
            //创建域名
            List<DomainInfoDTO> domainInfoDTOS = Trans.transDomainByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            domainInfoDTOS.forEach(domainInfoService::create);
            //创建虚拟网关
            virtualGatewayDto.setDomainInfos(domainInfoDTOS);
            virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        }
    }

    /**
     * 创建plm资源
     * ingress资源使用80端口，所有ingress资源共用plm，只有创建第一个ingress资源时才创建plm
     */
    private void publishPluginManager(VirtualGatewayDto virtualGatewayDto){
        if (KUBERNETES_INGRESS.equals(virtualGatewayDto.getType())){
            List<VirtualGatewayDto> ingressList = getIngressList(virtualGatewayDto.getGwId());
            //当前网关下已经存在ingress资源，不发布plm
            if (!CollectionUtils.isEmpty(ingressList)){
                return;
            }
        }
        pluginManagerService.publishPluginManager(virtualGatewayDto);
    }

    /**
     * 删除plm资源
     * ingress资源使用80端口，所有ingress资源共用plm，只有删除最后一个ingress时才删除plm
     */
    private void deletePluginManager(VirtualGatewayDto virtualGatewayDto){
        if (KUBERNETES_INGRESS.equals(virtualGatewayDto.getType())){
            List<VirtualGatewayDto> ingressList = getIngressList(virtualGatewayDto.getGwId());
            ingressList = ingressList.stream().filter(o -> o.getId() != virtualGatewayDto.getId()).collect(Collectors.toList());
            //当前网关下存在其他ingress资源，不删除plm
            if (!CollectionUtils.isEmpty(ingressList)){
                return;
            }
        }
        pluginManagerService.offlinePluginManager(virtualGatewayDto);
    }

    private List<VirtualGatewayDto> getIngressList(Long gwId){
        QueryVirtualGatewayDto query = new QueryVirtualGatewayDto();
        query.setType(KUBERNETES_INGRESS);
        query.setGwId(gwId);
        return virtualGatewayInfoService.getVirtualGatewayList(query);
    }

    private void handleUpdateResource(List<KubernetesGatewayInfo> kubernetesGatewayInfos, GatewayDto gatewayDto){
        for (KubernetesGatewayInfo kubernetesGatewayInfo : kubernetesGatewayInfos) {
            VirtualGatewayDto virtualGatewayDto = Trans.transByK8sGateway(kubernetesGatewayInfo, gatewayDto);
            //更新域名
            List<DomainInfoDTO> domainInfoDTOS = handleRefreshDomain(kubernetesGatewayInfo, gatewayDto);
            //更新虚拟网关
            virtualGatewayDto.setDomainInfos(domainInfoDTOS);
            virtualGatewayInfoService.updateWithoutHooker(virtualGatewayDto);
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
                .map(o -> Trans.transDomainInfo(o, kubernetesGatewayInfo, gatewayDto))
                .peek(domainInfoService::create)
                .collect(Collectors.toList());
        //获取目标域名
        List<DomainInfoDTO> existDomainDTOS  = targetHosts.stream().map(dbResourceMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        List<DomainInfoDTO> targetDomainDTOS = new ArrayList<>();
        targetDomainDTOS.addAll(existDomainDTOS);
        targetDomainDTOS.addAll(addedDomainDTOS);
        return targetDomainDTOS;
    }


}
