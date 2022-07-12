package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.apiserver.meta.DubboMeta;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
public interface IDubboMetaService {


    /**
     * 创建Dubbo 元数据信息
     *
     * @param dubboMetaDto
     * @return
     */
    long createDubboMeta(DubboMetaDto dubboMetaDto);

    /**
     * 修改Dubbo 元数据信息
     *
     * @param dubboMetaDto
     * @return
     */
    long updateDubboMeta(DubboMetaDto dubboMetaDto);

    /**
     * 删除Dubbo 元数据信息
     *
     * @param id
     */
    void deleteDubboMeta(Long id);

    /**
     * 获取所有Dubbo 元数据信息
     *
     * @return
     */
    List<DubboMetaDto> findAll();

    /**
     * 分页获取所有Dubbo 元数据信息
     *
     * @param limit
     * @param offset
     * @return
     */
    List<DubboMetaDto> findAll(long offset, long limit);

    /**
     * 获取Dubbo 元数据信息总数
     *
     * @return
     */
    long countAll();

    /**
     * 通过Id获取Dubbo 元数据信息信息
     *
     * @param id
     * @return
     */
    DubboMetaDto get(long id);

    /**
     * 通过接口名称,应用名称获取Dubbo 元数据信息
     *
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @return
     */
    List<DubboMetaDto> findByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName);

    /**
     * 通过接口名称,应用名称获取Dubbo 元数据信息条数
     *
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @return
     */
    int countByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName);

    /**
     * 通过接口名称,应用名称分页获取Dubbo 元数据信息
     *
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @param offset
     * @param limit
     * @return
     */
    List<DubboMetaDto> findByInterfaceNameAndApplicationName(long gwId, String interfaceName, String applicationName, long offset, long limit);

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
    List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method);

    /**
     * 通过分组,方法名称,版本,接口名称,应用名称获取Dubbo 元数据信息条数
     *
     * @param group           分组
     * @param method          方法名称
     * @param version         版本
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @return
     */
    int countByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method);

    /**
     * 通过分组,方法名称,版本,接口名称,应用名称分页获取Dubbo 元数据信息
     *
     * @param group           分组
     * @param method          方法名称
     * @param version         版本
     * @param interfaceName   接口名称
     * @param applicationName 应用名称
     * @param offset
     * @param limit
     * @return
     */
    List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String applicationName, String group, String version, String method, long offset, long limit);

    /**
     * 通过应用名称获取Dubbo 元数据信息
     *
     * @param applicationName 应用名称
     * @return
     */
    List<DubboMetaDto> findByApplicationName(String applicationName);

    /**
     * 通过应用名称获取Dubbo 元数据信息条数
     *
     * @param applicationName 应用名称
     * @return
     */
    int countByApplicationName(String applicationName);

    /**
     * 通过应用名称分页获取Dubbo 元数据信息
     *
     * @param applicationName 应用名称
     * @param offset
     * @param limit
     * @return
     */
    List<DubboMetaDto> findByApplicationName(String applicationName, long offset, long limit);

    /**
     * 通过分组,方法名称,版本,接口名称获取Dubbo 元数据信息
     *
     * @param group         分组
     * @param method        方法名称
     * @param version       版本
     * @param interfaceName 接口名称
     * @return
     */
    List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version, String method);

    /**
     * 通过分组,版本,接口名称获取Dubbo 元数据信息
     *
     * @param group         分组
     * @param version       版本
     * @param interfaceName 接口名称
     * @return
     */
    List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version);

    /**
     * 通过分组,方法名称,版本,接口名称获取Dubbo 元数据信息条数
     *
     * @param group         分组
     * @param method        方法名称
     * @param version       版本
     * @param interfaceName 接口名称
     * @return
     */
    int countByCondition(long gwId, String interfaceName, String group, String version, String method);

    /**
     * 通过分组,方法名称,版本,接口名称分页获取Dubbo 元数据信息
     *
     * @param group         分组
     * @param method        方法名称
     * @param version       版本
     * @param interfaceName 接口名称
     * @param offset
     * @param limit
     * @return
     */
    List<DubboMetaDto> findByCondition(long gwId, String interfaceName, String group, String version, String method, long offset, long limit);


    /**
     * 通过接口名称分组,版本的组合 获取Dubbo 元数据信息
     *
     * @param gwId
     * @param igv
     * @return
     */
    List<DubboMetaDto> findByIgv(long gwId, String igv);


    /**
     * 通过接口名称分组,版本的组合 批量删除 Dubbo 元数据信息
     *
     * @param gwId
     * @param igv
     */
    void batchDeleteByCondition(long gwId, String igv);


    /**
     * 通过接口名称分组,版本的组合 刷新Dubbo 元数据信息
     *
     * @param gwId
     * @param igv
     * @return
     */
    List<DubboMetaDto> refreshDubboMeta(long gwId, String igv);

    /**
     * 转化为显示层
     *
     * @param dubboMeta
     * @return
     */
    DubboMetaDto toView(DubboMeta dubboMeta);

    /**
     * 转化为元数据
     *
     * @param dubboMetaDto
     * @return
     */
    DubboMeta toMeta(DubboMetaDto dubboMetaDto);

    /**
     * 校验新增参数
     *
     * @param dubboMetaDto
     * @return
     */
    ErrorCode checkCreateParam(DubboMetaDto dubboMetaDto);

    /**
     * 校验更新参数
     *
     * @param dubboMetaDto
     * @return
     */
    ErrorCode checkUpdateParam(DubboMetaDto dubboMetaDto);

    /**
     * 校验删除参数
     *
     * @param dubboMetaDto
     * @return
     */
    ErrorCode checkDeleteParam(DubboMetaDto dubboMetaDto);
}