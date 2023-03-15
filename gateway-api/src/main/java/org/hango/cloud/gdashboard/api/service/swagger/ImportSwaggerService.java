package org.hango.cloud.gdashboard.api.service.swagger;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerApiInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * swagger导入相关interface
 *
 * @author hanjiahao
 */
public interface ImportSwaggerService {
    /**
     * 获取swagger API 基本信息
     *
     * @param baseUrl baseUrl，swagger描述中的baseUrl
     * @param paths   多个swagger Path信息
     * @return SwaggerApiInfo list
     */
    List<SwaggerApiInfo> getSwaggerApiInfo(String baseUrl, Map<String, Path> paths);

    /**
     * 获取swagger API基本信息
     *
     * @param url      url，path的url信息
     * @param pathItem Path信息
     * @return SwaggerApiInfo list
     */
    List<SwaggerApiInfo> getApiInfoByURL(String url, Path pathItem);

    /**
     * 获取SwaggerApiInfo
     *
     * @param method    请求method
     * @param operation swagger operation
     * @return {@link SwaggerApiInfo}
     */
    SwaggerApiInfo getSwaggerApiInfo(String method, Operation operation);

    /**
     * 获取ApiInfo
     *
     * @param url       url
     * @param method    请求method
     * @param operation swagger operation
     * @return {@link ApiInfo}
     */
    ApiInfo getApiInfo(String url, String method, Operation operation);

    /**
     * 获取request header
     *
     * @param operation swagger operation
     * @return {@link List< ApiHeader >}
     */
    List<ApiHeader> getApiRequestHeader(Operation operation);

    /**
     * 获取response header
     *
     * @param operation swagger operation
     * @return {@link List<ApiHeader>}
     */
    List<ApiHeader> getApiResponseHeader(Operation operation);

    /**
     * 获取request body
     *
     * @param operation swagger operation
     * @return {@link List< ApiBody >}
     */
    List<ApiBody> getApiRequestBody(Operation operation);

    /**
     * 获取response body
     *
     * @param operation swagger operaton
     * @return {@link List<ApiBody>}
     */
    List<ApiBody> getApiResponseBody(Operation operation);

    /**
     * 获取queryString
     *
     * @param operation swagger operation
     * @return {@link List<ApiBody>}
     */
    List<ApiBody> getQueryString(Operation operation);

    /**
     * 获取status code
     *
     * @param operation swagger operation
     * @return {@link List< ApiStatusCode >}
     */
    List<ApiStatusCode> getApiStatusCode(Operation operation);

    /**
     * 通过swagger文件获取API model数据
     *
     * @param swagger
     * @return
     */
    List<CreateApiModelDto> getApiModels(Swagger swagger);

    /**
     * @param type      类型
     * @param serviceId 服务id
     * @return long型
     */
    long generateExactByService(String type, long serviceId);

    /**
     * 获取ApiBody
     *
     * @param parameter Parameter parameter
     * @param type      类型
     * @return {@link ApiBody}
     */
    ApiBody getApiBodyFromParameter(Parameter parameter, String type);

    /**
     * 获取swagger detail 用于swagger详细展示
     *
     * @param swagger   Swagger swagger
     * @param serviceId 服务id
     * @return Map<String, Object>
     */
    Map<String, Object> getSwaggerDetails(Swagger swagger, long serviceId, String serviceName);

    /**
     * 插入swagger insert相关
     *
     * @param swagger   Swagger swagger
     * @param serviceId 服务id
     * @return 插入结果，true/false
     */
    boolean insertSwagger(Swagger swagger, long serviceId);

    /**
     * 校验swagger file的正确性
     *
     * @param file swagger file
     * @return {@link ApiErrorCode}
     */
    ApiErrorCode checkSwaggerFile(MultipartFile file);
}
