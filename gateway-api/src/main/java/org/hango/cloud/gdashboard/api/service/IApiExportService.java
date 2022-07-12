package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.ApiExportDto;

import java.util.List;

public interface IApiExportService {
    /**
     * 通过服务id获取ApiExportDto相关导出数据
     *
     * @param serviceId   服务id
     * @param serviceType 服务类型，http/dubbo
     * @return
     */
    List<ApiExportDto> getApiInfo(long serviceId, String serviceType);

    /**
     * 导入Api相关信息至portal数据库
     *
     * @param apiExportDtos ApiExportDto json
     * @param serviceId     服务id
     * @param serviceType   服务类型，http/dubbo
     * @param projectId     服务对应的项目id
     */
    void addApiInfos(List<ApiExportDto> apiExportDtos, long serviceId, String serviceType, long projectId);
}
