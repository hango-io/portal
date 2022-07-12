package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IApiInfoService {
    long addApi(ApiInfo apiInfo);

    /**
     * 增加dubbo或webservice API
     *
     * @param apiInfo apiInfo
     * @param type    api 类型
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    long addDubboOrWebServiceApi(ApiInfo apiInfo, String type);

    /**
     * 添加API
     *
     * @param apiInfoBasicDto apiBasicDto
     * @param type            api 类型，http, dubbo, webservice
     * @return
     */
    long addApiInfos(ApiInfoBasicDto apiInfoBasicDto, String type);

    /**
     * 判断ApiInfoBasicDto参数校验
     *
     * @param apiInfoBasicDto
     * @return
     */
    ApiErrorCode checkParamApiBasicDto(ApiInfoBasicDto apiInfoBasicDto);

    /**
     * 批量添加API
     *
     * @param apiInfos apiInfoList
     */
    @Transactional(rollbackFor = Exception.class)
    void addApiList(List<ApiInfo> apiInfos);

    ApiInfo getApi(String apiId);

    ApiInfo getApiById(long apiId);

    /**
     * 获取API详情
     *
     * @param apiPath   路径
     * @param method    方法
     * @param serviceId 服务id
     * @return
     */
    ApiInfo getApiInfo(String apiPath, String method, long serviceId);

    boolean isApiExists(String action, String version, long serviceId, long apiId);

    boolean isRestfulApiExists(String apiPath, String method, long serviceId, long apiId);

    boolean isApiExists(long apiId);

    List<ApiInfo> findAllApi();

    /**
     * 通过项目id获取所有API
     *
     * @param projectId 项目id
     * @return ApiInfo List
     */
    List<ApiInfo> findAllApi(long projectId);

    /**
     * 通过服务id获取所有API
     *
     * @param serviceId 服务id
     * @return ApiInfo list
     */
    List<ApiInfo> findAllApiByServiceId(long serviceId);

    List<ApiInfo> findAllApiBySwaggerSync(long serviceId, int swaggerSync);

    long getApiCountByServiceId(long serviceId);

    void deleteApi(long apiId);

    void deleteApiHeaderBody(long apiId);

    long updateApiStatus(String apiId, String status);

    ApiDocumentStatus getApiDocumentStatusById(long id);

    ApiDocumentStatus getApiDocumentStatus(String status);

    /**
     * API 标识是否存在
     *
     * @param serviceId 服务id
     * @param aliasName api标识
     * @param apiId     apiId
     * @return
     */
    boolean isExistAliasName(String serviceId, String aliasName, long apiId);

    /**
     * 更新API
     *
     * @param apiInfo API info
     * @return 更新结果，true,false
     */
    boolean updateApi(ApiInfo apiInfo);

    long addApiWithId(ApiInfo apiInfo);

    /**
     * 分页获取API信息
     *
     * @param projectId     项目id
     * @param serviceId     服务id
     * @param apiDocumentId API状态
     * @param pattern       API Pattern，模糊匹配
     * @param offset        分页查询offset
     * @param limit         分页查询limit
     * @return API info list
     */
    List<ApiInfo> findAllApiByProjectLimit(long projectId, long serviceId, long apiDocumentId, String pattern, long offset, long limit);

    /**
     * 获取API Info数量
     *
     * @param projectId     项目id
     * @param serviceId     服务id
     * @param apiDocumentId API状态
     * @param pattern       模糊匹配pattern
     * @return int类型数量
     */
    long getCountByProjectOrService(long projectId, long serviceId, long apiDocumentId, String pattern);

    ApiInfo getApiInfoByApiPathAndService(String apiPath, String apiMethod, long serviceId);
}
