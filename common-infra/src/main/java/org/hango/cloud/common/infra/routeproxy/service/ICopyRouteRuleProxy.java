package org.hango.cloud.common.infra.routeproxy.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

public interface ICopyRouteRuleProxy {
    ErrorCode checkCopyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId);

    /**
     * 复制拷贝已发布路由
     *
     * @param routeRuleId 源路由id
     * @param originGwId  源网关id
     * @param desGwId     目的网关id
     * @return 拷贝结果
     */
    boolean copyRouteRuleProxy(long routeRuleId, long originGwId, long desGwId);
}
