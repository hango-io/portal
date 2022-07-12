package org.hango.cloud.dashboard.apiserver.web.controller.open;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dto.CreateApiDto;
import org.hango.cloud.dashboard.apiserver.dto.GatewayInfoDto;
import org.hango.cloud.dashboard.apiserver.dto.PublishedServiceInfoForSkiffDto;
import org.hango.cloud.dashboard.apiserver.dto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IOpenApiService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.ParameterVerification;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiConvertToJsonService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 暴露给Mock平台的接口以及nsf的接口
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/4/25 19:42.
 */
@RestController
@RequestMapping(value = "/gportal")
public class OpenApiController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(OpenApiController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiConvertToJsonService apiConvertToJsonService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IOpenApiService openApiService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;


    /**
     * 查询此API详情
     *
     * @param request
     * @param apiId
     * @param token
     * @return
     */
    @RequestMapping(params = {"Action=DescribeApiById", "Version=2018-04-25"}, method = RequestMethod.GET)
    public Object describeApiById(final HttpServletRequest request, @RequestParam(name = "ApiId") long apiId,
                                  @RequestHeader("X-Auth-Token") final String token) throws JsonProcessingException {

        ErrorCode errorCode = null;
        //判断ApiId的合法性
        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));

        if (apiInfo == null) {
            errorCode = CommonErrorCode.InvalidParameterApiId(String.valueOf(apiId));
            return apiReturn(errorCode);
        }

        String swaggerJson = apiConvertToJsonService.generateSwaggerJson(apiId);

        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(swaggerJson)) {
            result.put("ApiInfo", new Object());
        } else {
            result.put("ApiInfo", JSONObject.parseObject(swaggerJson));
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(apiInfo.getServiceId());
        result.put("ServiceName", serviceInfo.getServiceName());
        result.put("ServiceId", apiInfo.getServiceId());
        return apiReturnSuccess(result);
    }

    /**
     * 创建API，提供给nsf agent使用
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(params = {"Action=RegisterApi", "Version=2018-04-25"}, method = RequestMethod.POST)
    public Object createApi(@RequestBody String body, final HttpServletRequest request, @RequestHeader("X-Auth-Token") final String token) throws JsonProcessingException {

        ErrorCode errorCode = CommonErrorCode.InvalidBodyFormat;

        CreateApiDto createApiDto;
        try {
            createApiDto = JSON.parseObject(body, CreateApiDto.class);
        } catch (Exception e) {
            logger.error("json parse error", e);
            return apiReturn(errorCode);
        }

        //判断参数是否合法
        errorCode = checkParams(createApiDto);
        if (errorCode != CommonErrorCode.Success) {
            return apiReturn(errorCode);
        }

        //判断API是否存在
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceName(createApiDto.getServiceName());
        if (apiInfoService.isRestfulApiExists(createApiDto.getPath(), createApiDto.getMethod(), serviceInfo.getId(), 0)) {
            errorCode = CommonErrorCode.ApiAlreadyExist;
            return apiReturn(errorCode);
        }

        //创建API
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setApiName(createApiDto.getApiName());
        apiInfo.setServiceId(serviceInfo.getId());
        apiInfo.setApiMethod(createApiDto.getMethod());
        apiInfo.setApiPath(createApiDto.getPath());
        apiInfo.setType(createApiDto.getType());
        apiInfo.setCreateDate(System.currentTimeMillis());
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setStatus("0");
        ApiDocumentStatus apiDocumentStatus = apiInfoService.getApiDocumentStatus("开发中");
        apiInfo.setDocumentStatusId(apiDocumentStatus.getId());

        if (StringUtils.isNotBlank(createApiDto.getDesc())) {
            apiInfo.setDescription(createApiDto.getDesc());
        }

        String regex = CommonUtil.getRegexFromApi(createApiDto.getPath());
        apiInfo.setRegex(regex);

        long apiId = apiInfoService.addApi(apiInfo);

        Map<String, Object> params = new HashMap<>();
        params.put("ApiId", apiId);
        return apiReturnSuccess(params);
    }


    /**
     * 查询服务详情，提供给 轻舟 告警模块
     *
     * @param request
     * @return
     */
    @RequestMapping(params = {"Action=DescribeServices", "Version=2018-08-29"}, method = RequestMethod.GET)
    public Object apiList(final HttpServletRequest request, @RequestParam(name = "AccountId") final String accountId) throws JsonProcessingException {

        List<PublishedServiceInfoForSkiffDto> list = openApiService.getPublishedServiceInfoByAccountId(accountId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("ServiceInfoList", list);
        return apiReturnSuccess(result);
    }

    /**
     * 查询服务列表，提供给 GoAPI 模块
     *
     * @param request
     * @return
     */
    @RequestMapping(params = {"Action=DescribeServicesForGoApi", "Version=2018-09-13"}, method = RequestMethod.GET)
    public Object serviceInfoList(final HttpServletRequest request, @RequestHeader("X-Auth-Token") final String token) throws JsonProcessingException {

        List<ServiceInfoDto> serviceInfoDtoList = new ArrayList<>();
        List<ServiceInfo> serviceInfoList = serviceInfoService.findAll();
        if (!CollectionUtils.isEmpty(serviceInfoList)) {
            for (ServiceInfo serviceInfo : serviceInfoList) {
                serviceInfoDtoList.add(new ServiceInfoDto(serviceInfo.getId(), serviceInfo.getServiceName(), serviceInfo.getDisplayName()));
            }
            if (!CollectionUtils.isEmpty(serviceInfoDtoList)) {
                serviceInfoDtoList.stream().filter(serviceInfoDto -> StringUtils.isNotBlank(serviceInfoDto.getServiceName())).collect(Collectors.toList());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ServiceList", serviceInfoDtoList);
        return apiReturnSuccess(result);
    }


    /**
     * 查询网关环境列表，提供给 GoAPI 模块
     *
     * @param request
     * @return
     */
    @RequestMapping(params = {"Action=DescribeGwList", "Version=2018-09-13"}, method = RequestMethod.GET)
    public Object gwInfoList(final HttpServletRequest request, @RequestHeader("X-Auth-Token") final String token) throws JsonProcessingException {

        List<GatewayInfoDto> gatewayInfoDtoList = new ArrayList<>();
        List<GatewayInfo> gatewayInfoList = gatewayInfoService.findAll();
        for (GatewayInfo gatewayInfo : gatewayInfoList) {
            gatewayInfoDtoList.add(new GatewayInfoDto(gatewayInfo.getId(), gatewayInfo.getGwName(), gatewayInfo.getGwAddr()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("GwList", gatewayInfoDtoList);
        return apiReturnSuccess(result);
    }


    private ErrorCode checkParams(CreateApiDto createApiDto) {
        String apiName = createApiDto.getApiName();
        String serviceName = createApiDto.getServiceName();
        String method = createApiDto.getMethod();
        String path = createApiDto.getPath();
        String type = createApiDto.getType();
        String desc = createApiDto.getDesc();

        ErrorCode errorCode = CommonErrorCode.Success;

        if (StringUtils.isBlank(apiName) || apiName.length() > 32) {
            errorCode = CommonErrorCode.InvalidParameterApiName(apiName);
            return errorCode;
        }

        if (StringUtils.isBlank(serviceName) || serviceName.length() > 32 ||
                null == serviceInfoService.getServiceByServiceName(serviceName)) {
            errorCode = CommonErrorCode.InvalidParameterServiceName(serviceName);
            return errorCode;
        }

        if (StringUtils.isBlank(method) || !Const.METHOD_LIST.contains(method)) {
            errorCode = CommonErrorCode.InvalidParameterMethod(method);
            return errorCode;
        }

        if (StringUtils.isBlank(path) || !ParameterVerification.isApiPathValid(path)) {
            errorCode = CommonErrorCode.InvalidParameterApiPath(path);
            return errorCode;
        }

        if (StringUtils.isBlank(type) || !"RESTFUL".equals(type)) {
            errorCode = CommonErrorCode.InvalidParameterApiType(type);
            return errorCode;
        }

        if (StringUtils.isNotBlank(desc) && desc.length() > 200) {
            errorCode = CommonErrorCode.InvalidParameterApiDesc(desc);
            return errorCode;
        }

        return errorCode;
    }

}
