package org.hango.cloud.envoy.infra.dubbo.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboBindingInfo;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
public interface IDubboBindingService extends CommonService<DubboBindingInfo, DubboBindingDto> {


    /**
     * 删除Dubbo转换参数,同时更改k8s Virtual-service
     *
     * @param objectId
     * @param objectType
     * @return
     */
    boolean deleteDubboInfo(long objectId, String objectType);


    /**
     * 获取Dubbo转换参数
     *
     * @param objectId
     * @param objectType
     * @return
     */
    DubboBindingDto getByIdAndType(long objectId, String objectType);


    /**
     * 判断方法是否有效
     *
     * @param dto
     * @return
     */
    void processMethodWorks(DubboBindingDto dto);


    /**
     * 保存Dubbo转换参数
     *
     * @param dto
     */
    long saveDubboInfo(DubboBindingDto dto);

    /**
     * 设置原始类型默认值
     *
     * @param dto
     */
    void parseDefaultValue(DubboBindingDto dto);


    /**
     * 参数校验
     *
     * @param dto
     * @return
     */
    ErrorCode checkAndComplete(DubboBindingDto dto);

}
