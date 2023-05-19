package org.hango.cloud.gdashboard.api.service.impl.swagger;

import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.AssociationType;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerApiInfo;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.hango.cloud.gdashboard.api.service.swagger.ISwaggerApiInfoService;
import org.hango.cloud.gdashboard.api.service.swagger.ImportSwaggerService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SwaggerApiInfoServiceImpl implements ISwaggerApiInfoService {
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private ImportSwaggerService importSwaggerService;
    @Autowired
    private IApiHeaderService apiHeaderService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IGetProjectIdService projectIdService;

    @Override
    public void syncApiInfoToNotSync(long serviceId) {
        List<ApiInfo> apiInfos = apiInfoService.findAllApiBySwaggerSync(serviceId, 1);
        if (!CollectionUtils.isEmpty(apiInfos)) {
            apiInfos.forEach(apiInfo -> {
                //修改swagger状态为失步
                apiInfo.setSwaggerSync(2);
                apiInfoService.updateApi(apiInfo);
            });
        }
    }

    @Override
//    @Transactional
    public void addApiInfos(List<SwaggerApiInfo> swaggerApiInfos, long serviceId) {
        if (CollectionUtils.isEmpty(swaggerApiInfos)) {
            return;
        }
        syncApiInfoToNotSync(serviceId);
        swaggerApiInfos.forEach(swaggerApiInfo -> {
            ApiInfo apiInfo = swaggerApiInfo.getApiInfo();
            apiInfo.setServiceId(serviceId);
            apiInfo.setProjectId(projectIdService.getProjectId());

            //删除之前的APIInfo，重新进行同步
            ApiInfo apiInfoInDB = apiInfoService.getApiInfoByApiPathAndService(apiInfo.getApiPath(), apiInfo.getApiMethod(), serviceId);
            long apiId;
            if (apiInfoInDB != null) {
                //如果是用户创建的服务，则以用户创建的为准，直接进行lamda的下一次循环
                if (0 == apiInfoInDB.getSwaggerSync()) {
                    return;
                } else {
                    apiId = apiInfoInDB.getId();
                    //设为同步状态,修改当前api基本信息
                    apiInfoInDB.setSwaggerSync(1);
                    apiInfoInDB.setModifyDate(System.currentTimeMillis());
                    apiInfoInDB.setApiName(apiInfo.getApiName());
                    apiInfoService.updateApi(apiInfoInDB);
                    //删除api header，body等数据
                    apiInfoService.deleteApiHeaderBody(apiId);
                }
            } else {
                apiId = apiInfoService.addApi(apiInfo);
            }

            List<ApiBody> apiBodies = new ArrayList<>();
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiQueryString())) {
                apiBodies.addAll(swaggerApiInfo.getApiQueryString());
            }
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiRequestBody())) {
                apiBodies.addAll(swaggerApiInfo.getApiRequestBody());
            }
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiResponseBody())) {
                apiBodies.addAll(swaggerApiInfo.getApiResponseBody());
            }
            //插入apiQueryString,requestBody,responseBody
            if (!CollectionUtils.isEmpty(apiBodies)) {
                apiBodies.forEach(apiBody -> {
                    if (!StringUtils.isEmpty(apiBody.getParamName())) {
                        apiBody.setApiId(apiId);
                        apiBody.setAssociationType(AssociationType.NORMAL.name());
                        //具体类型的paramTypeId获取
                        apiBody.setParamTypeId(importSwaggerService.generateExactByService(apiBody.getParamType(), serviceId));
                        //具体数组类型的dataTypeId获取
                        apiBody.setArrayDataTypeId(importSwaggerService.generateExactByService(apiBody.getArrayDataTypeName(), serviceId));
                        apiBodyService.addBody(apiBody);
                    }
                });
            }
            //插入requestHeader
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiRequestHeader())) {
                swaggerApiInfo.getApiRequestHeader().forEach(apiRequestHeader -> {
                    apiRequestHeader.setApiId(apiId);
                    apiHeaderService.addHeader(apiRequestHeader);
                });
            }
            //插入responseHeader
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiResponseHeader())) {
                swaggerApiInfo.getApiResponseHeader().forEach(apiHeader -> {
                    apiHeader.setApiId(apiId);
                    apiHeaderService.addHeader(apiHeader);
                });
            }
            //插入statusCode
            if (!CollectionUtils.isEmpty(swaggerApiInfo.getApiStatusCodes())) {
                swaggerApiInfo.getApiStatusCodes().forEach(apiStatusCode -> {
                    apiStatusCode.setObjectId(apiId);
                    apiBodyService.addStatusCode(apiStatusCode);
                });
            }
        });
    }

    @Override
    public List<SwaggerDetailsDto> getConflixApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId, String serviceName) {
        List<SwaggerDetailsDto> conflixApi = new ArrayList<>();
        if (!CollectionUtils.isEmpty(swaggerApiInfos)) {
            swaggerApiInfos.forEach(swaggerApiInfo -> {
                ApiInfo apiInfo = apiInfoService.getApiInfoByApiPathAndService(swaggerApiInfo.getApiInfo().getApiPath(), swaggerApiInfo.getApiInfo().getApiMethod(), serviceId);
                if (apiInfo != null && apiInfo.getSwaggerSync() == 0) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.API_TYPE, apiInfo.getApiName(), Const.SWAGGER_SYNC_CONFLICT);
                    swaggerDetailsDto.setApiPath(apiInfo.getApiPath());
                    swaggerDetailsDto.setApiMethod(apiInfo.getApiMethod());
                    StringBuilder message = new StringBuilder();
                    message.append("冲突:").append(apiInfo.getApiName()).append("(所属服务:").
                            append(serviceName).append(")");
                    swaggerDetailsDto.setMessage(message.toString());
                    conflixApi.add(swaggerDetailsDto);
                }
            });
        }
        return conflixApi;
    }

    @Override
    public List<SwaggerDetailsDto> getOverrideApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId) {
        List<SwaggerDetailsDto> overrideApi = new ArrayList<>();
        if (!CollectionUtils.isEmpty(swaggerApiInfos)) {
            swaggerApiInfos.forEach(swaggerApiInfo -> {
                ApiInfo apiInfo = apiInfoService.getApiInfoByApiPathAndService(swaggerApiInfo.getApiInfo().getApiPath(), swaggerApiInfo.getApiInfo().getApiMethod(), serviceId);
                if (apiInfo != null && apiInfo.getSwaggerSync() != 0) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.API_TYPE, apiInfo.getApiName(), Const.SWAGGER_SYNC_COVER);
                    swaggerDetailsDto.setApiPath(apiInfo.getApiPath());
                    swaggerDetailsDto.setApiMethod(apiInfo.getApiMethod());
                    overrideApi.add(swaggerDetailsDto);
                }
            });
        }
        return overrideApi;
    }

    @Override
    public List<SwaggerDetailsDto> getNewApi(List<SwaggerApiInfo> swaggerApiInfos, long serviceId) {
        List<SwaggerDetailsDto> newAPi = new ArrayList<>();
        if (!CollectionUtils.isEmpty(swaggerApiInfos)) {
            swaggerApiInfos.forEach(swaggerApiInfo -> {
                if (apiInfoService.getApiInfoByApiPathAndService(swaggerApiInfo.getApiInfo().getApiPath(),
                        swaggerApiInfo.getApiInfo().getApiMethod(), serviceId) == null) {
                    SwaggerDetailsDto swaggerDetailsDto = new SwaggerDetailsDto(Const.API_TYPE, swaggerApiInfo.getApiInfo().getApiName(), Const.SWAGGER_SYNC_NEW);
                    swaggerDetailsDto.setApiPath(swaggerApiInfo.getApiInfo().getApiPath());
                    swaggerDetailsDto.setApiMethod(swaggerApiInfo.getApiInfo().getApiMethod());
                    newAPi.add(swaggerDetailsDto);
                }
            });
        }
        return newAPi;
    }
}
