package org.hango.cloud.common.infra.route.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.route.dto.CopyRouteDTO;

import java.util.List;
import java.util.Map;

/**
 * 拷贝路由业务
 *
 * @author yutao04
 */
public interface ICopyRoute {
    /**
     * 校验拷贝路由参数
     *
     * @param routeId    源路由id
     * @param originGwId 源网关id
     * @param desGwId    目的网关id
     * @return 参数校验结果
     */
    ErrorCode checkCopyRoute(long routeId, long originGwId, long desGwId);

    /**
     * 复制拷贝已发布路由
     *
     * @param routeId    源路由id
     * @param originGwId 源网关id
     * @param desGwId    目的网关id
     * @return 拷贝结果
     */
    CopyRouteDTO copyRoute(long routeId, long originGwId, long desGwId);

    /**
     * 复制服务
     *
     * @param serviceIdList 服务ID集合
     * @param desGwId 目标网关ID
     * @return 源服务和模板网关服务ID映射集合
     */
    Map<Long, Long> copyService(List<Long> serviceIdList, long desGwId);
}
