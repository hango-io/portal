package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;

import java.util.List;

public interface IApiModelExportService {
    /**
     * 根据服务id获取服务下的ApiModel
     *
     * @param serviceId 服务id
     * @return
     */
    List<CreateApiModelDto> getApiModels(long serviceId);

    /**
     * 根据List<CreateApiModelDto>插入至ApiModel中
     *
     * @param createApiModelDtos CreateApiModelDtos
     * @param serviceId          服务id
     */
    void addApiModel(List<CreateApiModelDto> createApiModelDtos, long serviceId);
}
