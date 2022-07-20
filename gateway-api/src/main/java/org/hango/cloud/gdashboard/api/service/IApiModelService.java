package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 16:12.
 */
public interface IApiModelService {

    /**
     * 添加新模型，包括模型基本信息和模型参数
     *
     * @param createApiModelDto
     * @return
     */
    long addApiModel(CreateApiModelDto createApiModelDto);

    ApiModel getApiModel(CreateApiModelDto createApiModelDto);

    /**
     * 添加新模型，添加模型基本信息
     *
     * @param apiModel
     * @return
     */
    @Transactional
    long addApiModelBasic(ApiModel apiModel);

    /**
     * 添加新模型，添加模型类型
     *
     * @param createApiModelDto
     * @param modelId
     */
    long addApiParamModel(CreateApiModelDto createApiModelDto, long modelId);


    @Transactional
    long addApiModelParam(CreateApiModelDto createApiModelDto, long modelId, Map<String, Long> apiModels);

    /**
     * 根据ModelId查询模型详细信息，包含参数
     *
     * @param apiModelId
     * @return
     */
    CreateApiModelDto getApiModelByModelId(long apiModelId);

    List<Long> getApiModelInfoByServiceId(String serviceId);

    void deleteApiModel(long serviceId);

    String getApiModelRefer(long modelId);

    /**
     * 根据ModelId查询模型的基本信息
     *
     * @param apiModelId
     * @return
     */
    ApiModel getApiModelInfoByModelId(long apiModelId);

    /**
     * 根据serviceId和modelName查询基本信息
     *
     * @param serviceId
     * @param modelName
     * @return
     */
    ApiModel getApiModelByServiceIdAndModelName(long serviceId, String modelName);

    /**
     * 修改模型信息，包含基本信息和参数
     *
     * @param createApiModelDto
     * @param flag
     * @return
     */
    boolean updateApiModel(CreateApiModelDto createApiModelDto, long modelId, String modelName, boolean flag);

    /**
     * 根据ModelId删除模型
     *
     * @param apiModelId
     * @return
     */
    long deleteApiModelByModelId(long apiModelId, boolean flag);


    List<ApiModel> getApiModelByServiceId(long serviceId);

    /**
     * 根据ModelId查询参数详细信息
     *
     * @param apiModelId
     * @return
     */
    List<ApiParamDto> getApiModelParamsByModelId(long apiModelId);

    /**
     * 根据ModelName判断是否已存在
     *
     * @param apiModelName
     * @return
     */
    boolean isApiModelExists(String apiModelName, long serviceId);

    /**
     * 根据ModelId判断是否已存在
     *
     * @param apiModelId
     * @return
     */
    boolean isApiModelExists(long apiModelId);

    /**
     * 分页获取projectId下的所有数据模型，支持模糊匹配
     *
     * @param serviceId 如果serviceid为0，则代表通过projectId搜索
     * @param projectId serviceId为0，才起作用
     * @param offset
     * @param limit
     * @param pattern
     * @return
     */
    List<ApiModel> findAllApiModelByProjectLimit(long serviceId, long projectId, long offset, long limit, String pattern);

    /**
     * 根据pattern正则以及服务或者项目，获取API model数量
     *
     * @param serviceId
     * @param projectId
     * @param pattern
     * @return
     */
    long getApiModelCountByProjectOrService(long serviceId, long projectId, String pattern);

    List<ApiModel> findApiModelBySwaggerSync(long serviceId, long swaggerSync);

    /**
     * 更新apiModel
     *
     * @param apiModel
     * @return
     */
    long updateApiModels(ApiModel apiModel);

    /**
     * 创建API Model参数校验
     *
     * @param createApiModelDto
     * @return
     */
    ApiErrorCode checkAddApiModelParam(CreateApiModelDto createApiModelDto);
}
