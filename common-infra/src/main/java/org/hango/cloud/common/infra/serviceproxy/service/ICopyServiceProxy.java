package org.hango.cloud.common.infra.serviceproxy.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;

public interface ICopyServiceProxy {
    /**
     * 已发布服务复制，一键发布至目标网关，包括服务和已发布路由
     *
     * @param serviceId  服务id
     * @param originGwId 源网关id
     * @param de    目标网关id
     * @return 返回已发布服务list
     */
    List<Long> copyServiceProxy(long serviceId, long originGwId, long de);

    /**
     * 已发布服务参数校验
     *
     * @param serviceId  服务id
     * @param originGwId 源网关id
     * @param de    目标网关id
     * @return
     */
    ErrorCode checkCopyServiceProxy(List<Long> serviceId, long originGwId, long de);

    /**
     * 已发布服务列表一键复制
     *
     * @param serviceId  服务id列表
     * @param originGwId 源网关id
     * @param de    目的网关id
     * @return
     */
    Map<Long, List<Long>> copyServiceProxy(List<Long> serviceId, long originGwId, long de);
}
