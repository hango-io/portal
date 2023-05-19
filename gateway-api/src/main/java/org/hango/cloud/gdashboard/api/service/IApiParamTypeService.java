package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 16:20.
 */
public interface IApiParamTypeService {

    /**
     * 获取参数类型，区分Body或Header
     *
     * @return
     */
    List<String> findAll(String location);


    /**
     * 获取Header中参数类型
     *
     * @return
     */
    List<ApiParamType> listParamTypeInHeader();

    /**
     * 获取Body中参数类型
     *
     * @return
     */
    List<ApiParamType> listParamTypeInBody();

    /**
     * 根据paramTypeId判断参数类型是否存在
     *
     * @param paramTypeId
     * @return
     */
    boolean isApiParamTypeExists(long paramTypeId);

    /**
     * 查询ApiParamType
     *
     * @param paramTypeId
     * @return
     */
    ApiParamType listApiParamType(long paramTypeId);

    ApiParamType listApiParamType(String paramType);

    /**
     * 根据paramType和modelId获取ApiParamType
     *
     * @param paramType
     * @param modelId
     * @return
     */
    ApiParamType listApiParamTypeByModelId(String paramType, long modelId);

    List<ApiParamType> listParamTypeInHeaderAndBodyButNotModel();

    List<ApiParamType> listModleParamType(List<Long> modelIdList);

    ApiParamType listModleParamType(long modelId);

    List<ApiDocumentStatus> listApiDocumentStatus();

    /**
     * 获取真实的paramTypeId
     *
     * @param type
     * @param serviceId
     * @return
     */
    long generateExactByService(String type, long serviceId);
}
