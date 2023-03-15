package org.hango.cloud.gdashboard.api.service.impl;


import org.hango.cloud.gdashboard.api.dto.ApiBodyBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiExampleDto;
import org.hango.cloud.gdashboard.api.dto.ApiExportDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeaderBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodeBasicDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiExportService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Api相关元数据导入导出
 *
 * @author hanjiahao
 */
@Service
public class ApiExportServiceImpl implements IApiExportService {
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private ApiDtoGenerateService apiDtoGenerateService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiHeaderService apiHeaderService;
    @Autowired
    private IApiBodyService apiBodyService;

    @Override
    public List<ApiExportDto> getApiInfo(long serviceId, String serviceType) {
        List<ApiInfo> apiInfos = apiInfoService.findAllApiByServiceId(serviceId);
        if (CollectionUtils.isEmpty(apiInfos)) return new ArrayList<>();

        List<ApiExportDto> apiExportDtos = new ArrayList<>();
        for (ApiInfo apiInfo : apiInfos) {
            ApiExportDto apiExportDto = new ApiExportDto();
            apiExportDto.setApiInfoBasicDto(apiDtoGenerateService.getApiInfoDto(apiInfo));
            List<ApiHeaderBasicDto> apiRequestHeaders = apiDtoGenerateService.getApiHeader(apiInfo.getId(), Const.REQUEST_PARAM_TYPE);
            List<ApiHeaderBasicDto> apiResponseHeaders = apiDtoGenerateService.getApiHeader(apiInfo.getId(), Const.RESPONSE_PARAM_TYPE);
            apiExportDto.setRequestHeader(apiRequestHeaders);
            apiExportDto.setResponseHeader(apiResponseHeaders);

            List<ApiBodyBasicDto> apiQueryString = apiDtoGenerateService.getApiBody(apiInfo.getId(), Const.QUERYSTRING_PARAM_TYPE);
            apiExportDto.setQueryString(apiQueryString);

            List<ApiStatusCodeBasicDto> apiStatusCodeDtos = apiDtoGenerateService.getStatusCode(apiInfo.getId());
            apiExportDto.setStatusCode(apiStatusCodeDtos);

            ApiExampleDto apiExampleDto = apiDtoGenerateService.getApiExample(apiInfo.getId());
            apiExportDto.setApiExampleDto(apiExampleDto);

            List<ApiBodyBasicDto> apiRequestBody = apiDtoGenerateService
                .getApiBody(apiInfo.getId(), Const.REQUEST_PARAM_TYPE);
            List<ApiBodyBasicDto> apiResponseBody = apiDtoGenerateService
                .getApiBody(apiInfo.getId(), Const.RESPONSE_PARAM_TYPE);

            apiExportDto.setRequestBody(apiRequestBody);
            apiExportDto.setResponseBody(apiResponseBody);

            apiExportDtos.add(apiExportDto);
        }
        return apiExportDtos;
    }


    @Override
    public void addApiInfos(List<ApiExportDto> apiExportDtos, long serviceId, String serviceType, long projectId) {
        if (CollectionUtils.isEmpty(apiExportDtos)) return;
        apiExportDtos.forEach(apiExportDto -> {
            if (apiExportDto.getApiInfoBasicDto() == null) return;

            ApiInfo apiInfo = apiInfoService.getApiInfoByApiPathAndService(apiExportDto.getApiInfoBasicDto().getApiPath(),
                    apiExportDto.getApiInfoBasicDto().getApiMethod(), serviceId);
            long apiId = 0;
            //修改API信息
            if (apiInfo != null) {
                //删除请求头，请求body相关信息
                apiHeaderService.deleteHeader(apiInfo.getId());
                apiBodyService.deleteBody(apiInfo.getId());
                apiInfo.setApiName(apiExportDto.getApiInfoBasicDto().getApiName());
                apiInfo.setDescription(apiExportDto.getApiInfoBasicDto().getDescription());
                if (apiExportDto.getApiExampleDto() != null) {
                    apiInfo.setRequestExampleValue(apiExportDto.getApiExampleDto().getRequestExample());
                    apiInfo.setResponseExampleValue(apiExportDto.getApiExampleDto().getResponseExample());
                }
                apiInfoService.updateApi(apiInfo);
                apiId = apiInfo.getId();
            } else {
                apiInfo = apiDtoGenerateService.getApiInfoFromJson(apiExportDto.getApiInfoBasicDto(), apiExportDto.getApiExampleDto(), serviceId, projectId);
                //添加API基本信息
                apiId = apiInfoService.addApi(apiInfo);
            }
            List<ApiHeader> requestHeader = new ArrayList<>();
            List<ApiHeader> responseHeader = new ArrayList<>();
            if (apiExportDto.getRequestHeader() != null) {
                requestHeader = apiDtoGenerateService.getApiHeaderFromJson(apiExportDto.getRequestHeader(), Const.REQUEST_PARAM_TYPE, apiId);
            }
            if (apiExportDto.getResponseHeader() != null) {
                responseHeader = apiDtoGenerateService.getApiHeaderFromJson(apiExportDto.getRequestHeader(), Const.RESPONSE_PARAM_TYPE, apiId);
            }
            //添加api header
            requestHeader.addAll(responseHeader);
            requestHeader.forEach(apiHeader -> {
                apiHeaderService.addHeader(apiHeader);
            });

            //添加queryString and body
            List<ApiBody> queryString = apiDtoGenerateService.getApiBodyFromJson(apiExportDto.getQueryString(), Const.QUERYSTRING_PARAM_TYPE, apiId, serviceId);
            List<ApiBody> requestBody = apiDtoGenerateService.getApiBodyFromJson(apiExportDto.getRequestBody(), Const.REQUEST_PARAM_TYPE, apiId, serviceId);
            List<ApiBody> responseBody = apiDtoGenerateService.getApiBodyFromJson(apiExportDto.getResponseBody(), Const.RESPONSE_PARAM_TYPE, apiId, serviceId);
            queryString.addAll(requestBody);
            queryString.addAll(responseBody);
            queryString.forEach(apiBody -> {
                apiBodyService.addBody(apiBody);
            });

            //添加status code
            //删除原有的api
            apiBodyService.deleteStatusCode(apiId, "api");
            List<ApiStatusCode> apiStatusCodes = apiDtoGenerateService.getStatusCodeFromJson(apiExportDto.getStatusCode(), apiId);
            apiBodyService.addStatusCode(apiStatusCodes);
        });
    }


}
