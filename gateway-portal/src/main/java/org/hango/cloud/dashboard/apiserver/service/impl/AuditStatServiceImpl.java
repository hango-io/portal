package org.hango.cloud.dashboard.apiserver.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.AuditStatService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @modified hanjiahao
 * 参数传递之间增加gwId，网关环境信息
 */
@Service
public class AuditStatServiceImpl implements AuditStatService {
    private static final Logger logger = LoggerFactory.getLogger(AuditStatServiceImpl.class);

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    /**
     * 支持网关多环境选择
     *
     * @param gatewayInfo
     * @return
     */
    @Override
    public List<String> getAllServiceTag(GatewayInfo gatewayInfo) {
        if (gatewayInfo == null) {
            return Collections.emptyList();
        }
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            List<ServiceProxyInfo> envoyServiceProxyByLimit = serviceProxyService.getEnvoyServiceProxy(gatewayInfo.getId(), NumberUtils.LONG_ZERO, ProjectTraceHolder.getProId(), 0, 1000);
            if (CollectionUtils.isEmpty(envoyServiceProxyByLimit)) {
                return Collections.emptyList();
            }
            List<String> serviceNames = new ArrayList<>();

            for (ServiceProxyInfo serviceProxyInfo : envoyServiceProxyByLimit) {
                //从gportal获取发布到网关的服务名称
                serviceNames.add(serviceInfoService.getServiceById(String.valueOf(serviceProxyInfo.getServiceId())).getServiceName());
            }
            return serviceNames;
        }
        return Collections.emptyList();
    }
}
