package org.hango.cloud.gdashboard.api.service.swagger;

import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerApiInfo;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ISwaggerApiInfoService {
    /**
     * 设置当前apiInfo的swagger同步状态为非同步
     *
     * @param serviceId
     */
    void syncApiInfoToNotSync(long serviceId);

    /**
     * 将swaggerApiInfo插入至gportal对应的数据库
     *
     * @param swaggerApiInfos swaggerApiInfp
     * @param serviceId       服务id
     */
    @Transactional
    void addApiInfos(List<SwaggerApiInfo> swaggerApiInfos, long serviceId);

    /**
     * 获取冲突的API集合
     *
     * @param swaggerApiInfos swaggerApiInfos
     * @param serviceId       服务id
     * @param serviceName     服务名称
     * @return {@link List< SwaggerDetailsDto >}
     */
    List<SwaggerDetailsDto> getConflixApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId, String serviceName);

    /**
     * 获取覆盖的API集合
     *
     * @param swaggerApiInfos swaggerApiInfos
     * @param serviceId       服务id
     * @return {@link List<SwaggerDetailsDto>}
     */
    List<SwaggerDetailsDto> getOverrideApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId);

    /**
     * 获取新增的API集合
     *
     * @param swaggerApiInfos
     * @param serviceId
     * @return
     */
    List<SwaggerDetailsDto> getNewApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId);
}
