package org.hango.cloud.gdashboard.api.service.swagger;

import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ISwaggerModelService {
    /**
     * 通过CreateApiModelDto list加入swagger API Models
     *
     * @param apiModelDtos
     */
    @Transactional
    void addSwaggerApiModels(List<CreateApiModelDto> apiModelDtos);

    /**
     * 将所有已经同步的本服务下的全部变为失步
     *
     * @param apiModelDtos
     */
    void swaggerSyncToNotSync(List<CreateApiModelDto> apiModelDtos);

    /**
     * 更新swagger API Model
     *
     * @param createApiModelDto
     * @param apiModel
     * @return
     */
    ApiModel updateSwagggerApiModel(CreateApiModelDto createApiModelDto, ApiModel apiModel);

    /**
     * 将数据模型插入到对应的gportal数据库中
     *
     * @param createApiModelDtos
     * @param serviceId
     */
    void addApiModel(List<CreateApiModelDto> createApiModelDtos, long serviceId);

    /**
     * 获取具有冲突的apiModel Name
     *
     * @param apiModelDtos
     * @param serviceId
     * @return
     */
    List<SwaggerDetailsDto> getConflixApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId, String serviceName);

    /**
     * 获取覆盖的数据模型 apiModel Name
     *
     * @param apiModelDtos
     * @param serviceId
     * @return
     */
    List<SwaggerDetailsDto> getOverrideApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId);

    /**
     * 获取完全新导入的数据模型 apiModel Name
     *
     * @param apiModelDtos
     * @param serviceId
     * @return
     */
    List<SwaggerDetailsDto> getNewApiModel(List<CreateApiModelDto> apiModelDtos, long serviceId);
}
