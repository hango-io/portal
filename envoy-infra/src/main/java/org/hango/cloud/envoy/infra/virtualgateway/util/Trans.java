package org.hango.cloud.envoy.infra.virtualgateway.util;

import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.*;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.base.util.YamlUtil;
import org.hango.cloud.envoy.infra.virtualgateway.dto.*;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hango.cloud.gdashboard.api.util.Const.*;

/**
 * @Author zhufengwei
 * @Date 2023/5/31
 */
public class Trans {
    public static List<KubernetesGatewayHttpRouteDTO> httpRouteListToView(List<HTTPRoute> kubernetesGatewayHttpRouteList) {
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

    public static VirtualGatewayDto transByK8sGateway(KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setGwId(gatewayDto.getId());
        virtualGatewayDto.setName(kubernetesGatewayInfo.getName());
        virtualGatewayDto.setCode(kubernetesGatewayInfo.getName());
        if (kubernetesGatewayInfo.getVirtualGatewayId() != null){
            virtualGatewayDto.setId(kubernetesGatewayInfo.getVirtualGatewayId());
        }
        virtualGatewayDto.setProjectIdList(Collections.singletonList(kubernetesGatewayInfo.getProjectId()));
        virtualGatewayDto.setDescription(AUTO_GENERATE);
        virtualGatewayDto.setType(kubernetesGatewayInfo.getType());
        virtualGatewayDto.setProtocol(kubernetesGatewayInfo.getProtocol());
        virtualGatewayDto.setPort(kubernetesGatewayInfo.getPort());
        return virtualGatewayDto;
    }

    public static List<DomainInfoDTO> transDomainByK8sGateway(KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
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

    public static DomainInfoDTO transDomainInfo(String routeHost, KubernetesGatewayInfo kubernetesGatewayInfo, GatewayDto gatewayDto){
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setHost(routeHost);
        domainInfoDTO.setProjectId(kubernetesGatewayInfo.getProjectId());
        domainInfoDTO.setProtocol(kubernetesGatewayInfo.getProtocol());
        domainInfoDTO.setStatus(DomainStatusEnum.RelevanceOnly.name());
        domainInfoDTO.setDescription(AUTO_GENERATE);
        return domainInfoDTO;
    }

    private static List<KubernetesGatewayHttpRouteDTO.Rule> httpRouteRuleToDTO(List<HTTPRouteRule> rules) {
        return rules.stream().map(rule -> {
            KubernetesGatewayHttpRouteDTO.Rule dtoRule = new KubernetesGatewayHttpRouteDTO.Rule();
            //封装backend
            List<HTTPBackendRef> backendRefs = rule.getBackendRefs();
            if (!CollectionUtils.isEmpty(backendRefs)) {
                List<KubernetesGatewayHttpRouteDTO.BackendRef> backendRefDTOList = backendRefsToDTO(backendRefs);
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

    private static List<KubernetesGatewayHttpRouteDTO.BackendRef> backendRefsToDTO(List<HTTPBackendRef> backendRefs) {
        return backendRefs.stream().map(backendRef -> {
            KubernetesGatewayHttpRouteDTO.BackendRef backendRefDTO = new KubernetesGatewayHttpRouteDTO.BackendRef();
            backendRefDTO.setName(backendRef.getName());
            backendRefDTO.setWeight(backendRef.getWeight());
            backendRefDTO.setPort(backendRef.getPort());
            return backendRefDTO;
        }).collect(Collectors.toList());
    }

    private static List<KubernetesGatewayHttpRouteDTO.Match> matchesToDTO(List<HTTPRouteMatch> matches) {
        return matches.stream().filter(httpRouteMatch -> httpRouteMatch.getPath() != null).map(match -> {
            KubernetesGatewayHttpRouteDTO.Match matchDTO = new KubernetesGatewayHttpRouteDTO.Match();
            matchDTO.setType(match.getPath().getType());
            matchDTO.setValue(match.getPath().getValue());
            return matchDTO;
        }).collect(Collectors.toList());
    }

    private static List<KubernetesGatewayHttpRouteDTO.Filter> httpRouteFilterToDTO(List<HTTPRouteFilter> filters) {
        return filters.stream().map(filter -> {
            KubernetesGatewayHttpRouteDTO.Filter filterDTO = new KubernetesGatewayHttpRouteDTO.Filter();
            filterDTO.setType(filter.getType());
            return filterDTO;
        }).collect(Collectors.toList());
    }

    public static Set<String> mergeResourceName(List<KubernetesGatewayInfo> targetKubernetesGateway, List<VirtualGatewayDto> dbVirtialGateway){
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

    public static IngressViewDTO toView(VirtualGatewayDto virtualGatewayDto, IngressDTO ingressDTO){
        if (ingressDTO == null || virtualGatewayDto == null || CollectionUtils.isEmpty(virtualGatewayDto.getDomainInfos())){
            return null;
        }
        IngressViewDTO ingressViewDTO = new IngressViewDTO();
        ingressViewDTO.setName(virtualGatewayDto.getName());
        ingressViewDTO.setContent(ingressDTO.getContent());
        List<IngressRuleDTO> ingressRuleDTOS = ingressDTO.getIngressRuleDTOS();
        if (CollectionUtils.isEmpty(ingressRuleDTOS)){
            return null;
        }
        //rule转换，会基于域名进行过滤
        Map<String, DomainInfoDTO> domianMap = virtualGatewayDto.getDomainInfos().stream()
                .collect(Collectors.toMap(DomainInfoDTO::getHost, Function.identity(), (existing, replacement) -> replacement));

        List<IngressRuleViewDTO> ruleViewDTOS =  ingressRuleDTOS.stream()
                .map(Trans::toView)
                .filter(o -> domianMap.containsKey(o.getHost()))
                .peek(o -> o.setDomainId(domianMap.get(o.getHost()).getId()))
                .collect(Collectors.toList());

        ingressViewDTO.setRules(ruleViewDTOS);
        return ingressViewDTO;
    }

    private static IngressRuleViewDTO toView(IngressRuleDTO ingressRuleDTO){
        if (ingressRuleDTO == null){
            return null;
        }
        IngressRuleViewDTO viewDTO = new IngressRuleViewDTO();
        viewDTO.setHost(ingressRuleDTO.getHost());
        List<HTTPIngressPathDTO> httpRuleValueDTOS = ingressRuleDTO.getHttpRuleValueDTOS();
        if (CollectionUtils.isEmpty(httpRuleValueDTOS)){
            return null;
        }
        //当前ingress只取第一个http rule
        HTTPIngressPathDTO httpIngressPathDTO = httpRuleValueDTOS.get(0);

        viewDTO.setPath(httpIngressPathDTO.getPath());
        viewDTO.setPathType(httpIngressPathDTO.getPathType());
        viewDTO.setServiceName(httpIngressPathDTO.getServiceName());
        viewDTO.setServicePort(httpIngressPathDTO.getServicePort());
        return viewDTO;
    }

    public static KubernetesGatewayInfo toGateway(IngressDTO ingressDTO){
        if (StringUtils.isEmpty(ingressDTO.getName()) || StringUtils.isEmpty(ingressDTO.getNamespace())){
            return null;
        }
        List<IngressRuleDTO> ingressRuleDTOS = ingressDTO.getIngressRuleDTOS();
        if (CollectionUtils.isEmpty(ingressRuleDTOS)){
            return null;
        }
        KubernetesGatewayInfo gatewayInfo = new KubernetesGatewayInfo();
        gatewayInfo.setName(ingressDTO.getName() + "/" + ingressDTO.getNamespace());
        gatewayInfo.setProjectId(ingressDTO.getProjectId());
        //ingress指定80端口
        gatewayInfo.setPort(80);
        gatewayInfo.setProtocol("HTTP");
        List<String> hosts = ingressRuleDTOS.stream().map(IngressRuleDTO::getHost).distinct().collect(Collectors.toList());
        gatewayInfo.setRouteHosts(hosts);
        gatewayInfo.setType(KUBERNETES_INGRESS);
        return gatewayInfo;
    }

}
