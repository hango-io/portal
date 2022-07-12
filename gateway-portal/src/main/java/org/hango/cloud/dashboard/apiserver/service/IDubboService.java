package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.DubboInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.DubboInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
public interface IDubboService {

    /**
     * 添加Dubbo转换参数
     *
     * @param dto
     * @return
     */
    long addDubboInfo(DubboInfoDto dto);

    /**
     * 删除Dubbo转换参数,同时更改k8s Virtual-service
     *
     * @param objectId
     * @param objectType
     * @return
     */
    boolean deleteDubboInfo(long objectId, String objectType);


    /**
     * 删除Dubbo转换参数
     *
     * @param objectId
     * @param objectType
     * @return
     */
    void delete(long objectId, String objectType);

    /**
     * 获取Dubbo转换参数
     *
     * @param objectId
     * @param objectType
     * @return
     */
    DubboInfoDto getDubboDto(long objectId, String objectType);

    /**
     * 获取Dubbo转换参数
     *
     * @param objectId
     * @param objectType
     * @return
     */
    DubboInfo getDubboInfo(long objectId, String objectType);

    /**
     * 更新Dubbo转换参数
     *
     * @param dto
     * @return
     */
    long updateDubboInfo(DubboInfoDto dto);

    /**
     * 保存Dubbo转换参数
     *
     * @param dto
     * @return
     */
    long saveDubboInfo(DubboInfoDto dto);

    ErrorCode checkAndComplete(DubboInfoDto dto);

    /**
     * 将Dubbo信息转换为Envoy Head to Add 信息
     *
     * @param dto
     * @param headerOperationDto
     * @return
     */
    EnvoyRouteRuleHeaderOperationDto getDubboHeaderOperation(DubboInfoDto dto, EnvoyRouteRuleHeaderOperationDto headerOperationDto);

}
