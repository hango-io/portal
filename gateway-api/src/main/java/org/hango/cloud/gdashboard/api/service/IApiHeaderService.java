package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.ApiHeadersDto;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;

import java.util.List;

public interface IApiHeaderService {
    /**
     * 创建或更新API header 参数校验
     *
     * @param apiHeadersDto
     * @return
     */
    ApiErrorCode checkCreateOrUpdateHeader(ApiHeadersDto apiHeadersDto);

    /**
     * 根据API header dto生成API header
     *
     * @param apiHeadersDto apiHeaderDto，包装apiHeader信息
     * @param type          类型，Requset、Response
     * @return
     */
    List<ApiHeader> generateApiHeaderFromApiHeaderList(ApiHeadersDto apiHeadersDto, String type);

    /***
     * 创建API header
     * @param apiHeader apiHeader类
     * @return
     */
    long addHeader(ApiHeader apiHeader);

    /**
     * 通过API id和type获取APIheader
     *
     * @param apiId ApiId
     * @param type  type
     * @return
     */
    List<ApiHeader> getHeader(long apiId, String type);

    /**
     * 删除header参数
     *
     * @param paramId
     */
    void deleteHeaderParam(long paramId);

    /**
     * 添加header
     *
     * @param apiHeaderList
     * @return
     */
    List<Long> addHeader(List<ApiHeader> apiHeaderList);

    /**
     * 判断header中参数是否已经存在
     *
     * @param paramName
     * @param type
     * @param apiId
     * @return
     */
    boolean getHeaderParam(String paramName, String type, String apiId);

    /**
     * 根据API id和type删除header
     *
     * @param apiId apiId
     * @param type  Type Request,Response
     */
    void deleteHeader(long apiId, String type);


    /**
     * 根据API id删除header
     *
     * @param apiId apiId
     */
    void deleteHeader(long apiId);

    /**
     * 根据ApiId获取Req/Resp Header
     *
     * @param apiId
     * @return
     */
    List<ApiHeader> getHeader(String apiId);

}
