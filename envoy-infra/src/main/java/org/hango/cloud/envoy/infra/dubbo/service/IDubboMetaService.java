package org.hango.cloud.envoy.infra.dubbo.service;


import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboMeta;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
public interface IDubboMetaService extends CommonService<DubboMeta, DubboMetaDto> {


    /**
     * 通过分组,方法名称,版本,接口名称,应用名称获取Dubbo 元数据信息
     *
     * @param group           分组
     * @param method          方法名称
     * @param version         版本
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @return
     */
    List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String applicationName, String group, String version, String method);



    /**
     * 通过分组,方法名称,版本,接口名称获取Dubbo 元数据信息
     *
     * @param group         分组
     * @param method        方法名称
     * @param version       版本
     * @param interfaceName 接口名称
     * @return
     */
    List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String group, String version, String method);

    /**
     * 通过分组,版本,接口名称获取Dubbo 元数据信息
     *
     * @param group         分组
     * @param version       版本
     * @param interfaceName 接口名称
     * @return
     */
    List<DubboMetaDto> findByCondition(long virtualGwId, String interfaceName, String group, String version);


    /**
     * 通过接口名称分组,版本的组合 获取Dubbo 元数据信息
     *
     * @param virtualGwId
     * @param igv
     * @return
     */
    List<DubboMetaDto> findByIgv(long virtualGwId, String igv);


    /**
     * 通过接口名称分组,版本的组合 批量删除 Dubbo 元数据信息
     *
     * @param virtualGwId
     * @param igv
     */
    void batchDeleteByCondition(long virtualGwId, String igv);


    /**
     * 通过接口名称分组,版本的组合 刷新Dubbo 元数据信息
     *
     * @param virtualGwId
     * @param igv
     * @return
     */
    List<DubboMetaDto> refreshDubboMeta(long virtualGwId, String igv);
}