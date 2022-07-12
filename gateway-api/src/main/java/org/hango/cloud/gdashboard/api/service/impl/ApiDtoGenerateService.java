package org.hango.cloud.gdashboard.api.service.impl;


import org.hango.cloud.gdashboard.api.dto.ApiBodyBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiExampleDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeaderBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodeBasicDto;
import org.hango.cloud.gdashboard.api.dto.DubboParamInfoDto;
import org.hango.cloud.gdashboard.api.dto.WebServiceParamInfoDto;
import org.hango.cloud.gdashboard.api.dto.WebServiceRequestParamDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IDubboParamService;
import org.hango.cloud.gdashboard.api.service.IWebServiceParamService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 构造Api Json以及通过ApiJson构造API信息
 *
 * @author hanjiahao
 */
@Service
public class ApiDtoGenerateService {
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiHeaderService apiHeaderService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IDubboParamService dubboParamService;
    @Autowired
    private IWebServiceParamService webServiceParamService;

    public ApiInfoBasicDto getApiInfoDto(ApiInfo apiInfo) {
        ApiInfoBasicDto apiInfoBasicDto = BeanUtil.copy(apiInfo, ApiInfoBasicDto.class);
        return apiInfoBasicDto;
    }

    public List<ApiHeaderBasicDto> getApiHeader(long apiId, String type) {
        List<ApiHeader> headers = apiHeaderService.getHeader(apiId, type);
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(headers)) {
            apiHeaderBasicDtos = BeanUtil.copyList(headers, ApiHeaderBasicDto.class);
        }
        return apiHeaderBasicDtos;
    }

    public List<ApiBodyBasicDto> getApiBody(long apiId, String type) {
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, type);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        return apiBodyBasicDtos;
    }

    public ApiExampleDto getApiExample(long apiId) {
        ApiExampleDto apiExampleDto = new ApiExampleDto();
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        apiExampleDto.setRequestExample(apiInfo.getRequestExampleValue());
        apiExampleDto.setResponseExample(apiInfo.getResponseExampleValue());
        return apiExampleDto;
    }

    public List<ApiStatusCodeBasicDto> getStatusCode(long apiId) {
        List<ApiStatusCode> statusCodes = apiBodyService.listStatusCode(apiId);
        List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(statusCodes)) {
            apiStatusCodeBasicDtos = BeanUtil.copyList(statusCodes, ApiStatusCodeBasicDto.class);
        }
        return apiStatusCodeBasicDtos;
    }

    public List<ApiStatusCode> getStatusCodeFromJson(List<ApiStatusCodeBasicDto> apiStatusCodeDtos, long apiId) {
        if (CollectionUtils.isEmpty(apiStatusCodeDtos)) return new ArrayList<>();
        List<ApiStatusCode> apiStatusCodes = new ArrayList<>();
        for (ApiStatusCodeBasicDto apiStatusCodeDto : apiStatusCodeDtos) {
            ApiStatusCode apiStatusCode = new ApiStatusCode();
            apiStatusCode.setStatusCode(apiStatusCodeDto.getStatusCode());
            apiStatusCode.setDescription(apiStatusCodeDto.getDescription());
            apiStatusCode.setMessage(apiStatusCodeDto.getMessage());
            apiStatusCode.setCreateDate(System.currentTimeMillis());
            apiStatusCode.setModifyDate(System.currentTimeMillis());
            apiStatusCode.setObjectId(apiId);
            apiStatusCode.setType("api");
            apiStatusCodes.add(apiStatusCode);
        }
        return apiStatusCodes;
    }

    public ApiInfo getApiInfoFromJson(ApiInfoBasicDto apiInfoDto, ApiExampleDto apiExampleDto, long serviceId, long projectId) {
        ApiInfo apiInfo = new ApiInfo();
        //TODO apiInfo regex
        apiInfo.setApiName(apiInfoDto.getApiName());
        apiInfo.setApiPath(apiInfoDto.getApiPath());
        apiInfo.setApiMethod(apiInfoDto.getApiMethod());
        apiInfo.setDescription(apiInfoDto.getDescription());
        apiInfo.setAliasName(apiInfoDto.getAliasName());
        //导入API为restful风格接口
        apiInfo.setType("RESTFUL");
        apiInfo.setCreateDate(System.currentTimeMillis());
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setStatus("0");
        if (apiExampleDto != null) {
            apiInfo.setRequestExampleValue(apiExampleDto.getRequestExample());
            apiInfo.setResponseExampleValue(apiExampleDto.getResponseExample());
        }
        //开发中
        apiInfo.setDocumentStatusId(1);
        apiInfo.setRegex(apiInfoDto.getApiPath().replaceAll("\\{[^}]*\\}", "*"));
        apiInfo.setServiceId(serviceId);
        apiInfo.setProjectId(projectId);
        return apiInfo;
    }

    public List<ApiBody> getApiBodyFromJson(List<ApiBodyBasicDto> apiBodyDtos, String type, long apiId, long serviceId) {
        List<ApiBody> apiBodies = new ArrayList<>();
        if (CollectionUtils.isEmpty(apiBodyDtos)) return apiBodies;

        apiBodyDtos.forEach(apiBodyDto -> {
            ApiBody apiBody = new ApiBody();
            apiBody.setParamName(apiBodyDto.getParamName());
            apiBody.setParamType(apiBodyDto.getParamType());
            apiBody.setArrayDataTypeName(apiBodyDto.getArrayDataTypeName());
            apiBody.setDescription(apiBodyDto.getDescription());
            apiBody.setRequired(apiBodyDto.getRequired());
            apiBody.setDefValue(apiBodyDto.getDefValue());
            apiBody.setAssociationType(apiBodyDto.getAssociationType());
            apiBody.setCreateDate(System.currentTimeMillis());
            apiBody.setModifyDate(System.currentTimeMillis());
            apiBody.setType(type);
            apiBody.setApiId(apiId);
            apiBody.setParamTypeId(apiParamTypeService.generateExactByService(apiBody.getParamType(), serviceId));
            //具体数组类型的dataTypeId获取
            apiBody.setArrayDataTypeId(apiParamTypeService.generateExactByService(apiBody.getArrayDataTypeName(), serviceId));
            apiBodies.add(apiBody);
        });
        return apiBodies;
    }

    public List<DubboParamInfo> getDubboParamInfos(List<DubboParamInfoDto> dubboParamDtos, long serviceId, long apiId) {
        if (CollectionUtils.isEmpty(dubboParamDtos)) {
            return new ArrayList<>();
        }
        List<DubboParamInfo> dubboParamInfos = BeanUtil.copyList(dubboParamDtos, DubboParamInfo.class);
        dubboParamInfos.forEach(dubboParamInfo -> {
            dubboParamInfo.setParamTypeId(apiParamTypeService.generateExactByService(dubboParamInfo.getParamType(), serviceId));
            //具体数组类型的dataTypeId获取
            dubboParamInfo.setArrayDataTypeId(apiParamTypeService.generateExactByService(dubboParamInfo.getArrayDataTypeName(), serviceId));
            dubboParamInfo.setCreateDate(System.currentTimeMillis());
            dubboParamInfo.setApiId(apiId);
        });
        return dubboParamInfos;
    }

    public List<ApiHeader> getApiHeaderFromJson(List<ApiHeaderBasicDto> apiHeadDtos, String type, long apiId) {
        if (CollectionUtils.isEmpty(apiHeadDtos)) return new ArrayList<>();
        List<ApiHeader> apiHeaders = new ArrayList<>();

        apiHeadDtos.forEach(apiHeadDto -> {
            ApiHeader apiHeader = new ApiHeader();
            apiHeader.setParamName(apiHeadDto.getParamName());
            apiHeader.setParamValue(apiHeadDto.getParamValue());
            apiHeader.setType(type);
            apiHeader.setDescription(apiHeadDto.getDescription());
            apiHeader.setApiId(apiId);
            apiHeader.setCreateDate(System.currentTimeMillis());
            apiHeader.setModifyDate(System.currentTimeMillis());
            apiHeaders.add(apiHeader);
        });
        return apiHeaders;
    }

    public List<DubboParamInfoDto> getDubboParamInfoDto(long apiId) {
        List<DubboParamInfo> dubboParamInfoList = dubboParamService.getDubboParamByApiId(apiId);
        for (DubboParamInfo dubboParamInfo : dubboParamInfoList) {
            if (apiParamTypeService.listApiParamType(dubboParamInfo.getParamTypeId()) == null) {
                dubboParamInfo.setParamType("String");
                continue;
            }
            dubboParamInfo.setParamType(apiParamTypeService.listApiParamType(dubboParamInfo.getParamTypeId()).getParamType());

            if (dubboParamInfo.getArrayDataTypeId() != 0) {
                ApiParamType apiParamType = apiParamTypeService.listApiParamType(dubboParamInfo.getArrayDataTypeId());
                if (apiParamType != null) {
                    dubboParamInfo.setArrayDataTypeName(apiParamType.getParamType());
                }
            }
        }
        List<DubboParamInfoDto> dubboParamDtos = BeanUtil.copyList(dubboParamInfoList, DubboParamInfoDto.class);
        return dubboParamDtos;
    }

    /**
     * 根据paramName，获取真实paramId
     *
     * @param webServiceParamInfoDto
     * @param serviceId
     * @return
     */
    public WebServiceParamInfoDto getWebserviceParamInfoDto(WebServiceParamInfoDto webServiceParamInfoDto, long serviceId) {
        List<WebServiceRequestParamDto> requestParam = webServiceParamInfoDto.getRequestParam();
        if (!CollectionUtils.isEmpty(requestParam)) {
            for (WebServiceRequestParamDto webServiceParam : requestParam) {
                webServiceParam.setParamTypeId(apiParamTypeService.generateExactByService(webServiceParam.getParamType(), serviceId));
                //具体数组类型的dataTypeId获取
                webServiceParam.setArrayDataTypeId(apiParamTypeService.generateExactByService(webServiceParam.getArrayDataTypeName(), serviceId));
            }
        }

        List<WebServiceRequestParamDto> responseParam = webServiceParamInfoDto.getResponseParam();
        if (!CollectionUtils.isEmpty(responseParam)) {
            for (WebServiceRequestParamDto webServiceParam : responseParam) {
                webServiceParam.setParamTypeId(apiParamTypeService.generateExactByService(webServiceParam.getParamType(), serviceId));
                //具体数组类型的dataTypeId获取
                webServiceParam.setArrayDataTypeId(apiParamTypeService.generateExactByService(webServiceParam.getArrayDataTypeName(), serviceId));
            }
        }
        return webServiceParamInfoDto;
    }

}
