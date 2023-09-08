package org.hango.cloud.envoy.advanced.serviceproxy.hooker;
import org.hango.cloud.common.advanced.project.service.IPlatformPermissionScopeService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.envoy.infra.serviceproxy.hooker.AbstractEnvoyServiceProxyHooker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdvancedForEnvoyServiceProxyHooker extends AbstractEnvoyServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto> {
    @Autowired
    private IPlatformPermissionScopeService platformPermissionScopeService;

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 查询服务获取额外参数方法的后置Hook，具体实现见各增强包
     *
     * @param registry 注册中心类型
     * @return 额外的参数
     */
    @Override
    protected Map<String, String> postGetExtraServiceParamsHook(String registry) {
        return platformPermissionScopeService.createServiceFilters(registry);
    }
}