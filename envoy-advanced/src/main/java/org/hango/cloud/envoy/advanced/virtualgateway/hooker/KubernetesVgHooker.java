package org.hango.cloud.envoy.advanced.virtualgateway.hooker;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.advanced.project.service.IPlatformPermissionScopeService;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayInfo;
import org.hango.cloud.envoy.infra.virtualgateway.hooker.AbstractKubernetesVgHooker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author zhufengwei
 * @Date 2023/6/6
 */
@Component
public class KubernetesVgHooker extends AbstractKubernetesVgHooker<VirtualGateway, VirtualGatewayDto> {

    @Autowired
    IPlatformPermissionScopeService platformPermissionScopeService;

    @Override
    public int getOrder() {
        return 0;
    }


    @Override
    protected void fillProjectId(List<KubernetesGatewayInfo> gatewayInfoList) {
        super.fillProjectId(gatewayInfoList);
        Map<String, PermissionScopeDto> projectCodeMap = gatewayInfoList.stream()
                .map(KubernetesGatewayInfo::getProjectCode)
                .filter(StringUtils::isNotBlank)
                .map(platformPermissionScopeService::getPermissionScope)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(PermissionScopeDto::getPermissionScopeEnName, Function.identity()));

        for (KubernetesGatewayInfo kubernetesGatewayInfo : gatewayInfoList) {
            String projectCode = kubernetesGatewayInfo.getProjectCode();
            if (kubernetesGatewayInfo.getProjectId() == null && StringUtils.isNotBlank(projectCode)){
                PermissionScopeDto permissionScopeDto = projectCodeMap.get(projectCode);
                kubernetesGatewayInfo.setProjectId(permissionScopeDto.getId());
            }

        }
    }
}
