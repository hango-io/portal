package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;

import java.util.List;

public interface AuditStatService {

    /**
     * 获取所有服务标识
     *
     * @param gatewayInfo
     * @return
     */
    List<String> getAllServiceTag(GatewayInfo gatewayInfo);

}
