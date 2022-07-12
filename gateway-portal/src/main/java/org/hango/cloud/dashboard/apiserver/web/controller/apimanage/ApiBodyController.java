package org.hango.cloud.dashboard.apiserver.web.controller.apimanage;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.AssociationType;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.gdashboard.api.dto.ApiBodyBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiBodyJsonDto;
import org.hango.cloud.gdashboard.api.dto.ApiBodysDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodeBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodesDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.OperationLog;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiConvertToJsonService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API body controller，包括request body,response body 以及queryString
 * 同时包括dubbo param以及webservice param的创建
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiBodyController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(ApiBodyController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IOperationLogService operationLogService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IApiConvertToJsonService apiConvertToJsonService;

    /**
     * 添加request body
     *
     * @param apiBodysDto apiBody包装dto
     * @return 创建request body结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateRequestBody"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateRequestBody", description = "编辑API request body参数")
    public Object addRequestBody(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建request body，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        //FIXME dubbo类型如何判断
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.REQUEST_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.REQUEST_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            apiBody.setAssociationType(AssociationType.NORMAL.name());
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_REQUEST_BODY, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = getOperationLog(bodyList);
        String operation = UserPermissionHolder.getAccountId() + "修改了该API的Request Body，修改后Request Body中的参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiBodysDto.getId(), "api", operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * create query String
     *
     * @param apiBodysDto apiBody包装dto
     * @return 创建queryString结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateQueryString"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateQueryString", description = "编辑API queryString参数")
    public Object addQueryString(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建request body，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.QUERYSTRING_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.QUERYSTRING_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_QUERY_STRING, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = getOperationLog(bodyList);
        String operation = UserPermissionHolder.getAccountId() + "修改了该API的queryString，修改后queryString中的参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiBodysDto.getId(), "api", operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }


    /**
     * 查询queryString
     *
     * @param apiId 接口ID
     * @return ApiBodyBasicDtos，queryString的基本值
     */
    @RequestMapping(params = {"Action=DescribeQueryString"}, method = RequestMethod.GET)
    public Object getQueryString(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的queryString", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.QUERYSTRING_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("QueryString", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 创建statusCode
     *
     * @param apiStatusCodesDto statusCode的包装dto
     * @return 创建结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateStatusCode"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateStatusCode", description = "编辑API status code")
    public Object addStatusCode(@Validated @RequestBody ApiStatusCodesDto apiStatusCodesDto) {
        ApiInfo apiInfo = apiInfoService.getApiById(apiStatusCodesDto.getId());
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        //FIXME dubbo服务不需要statuscode
        List<ApiStatusCode> apiStatusCodes = apiBodyService.generateApiStatusCodeFromCodeList(apiStatusCodesDto);
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        for (ApiStatusCode apiStatusCode : apiStatusCodes) {
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_STATUS_CODE, StringUtils.EMPTY, String.valueOf(apiStatusCode.getStatusCode())));
        }
        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        apiBodyService.addStatusCodes(apiStatusCodes, apiStatusCodesDto.getId(), Const.API);

        String operation = UserPermissionHolder.getAccountId() + "修改了该API的响应吗，修改后响应码为：" + getStatusCodeOperationLog(apiStatusCodes).toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiStatusCodesDto.getId(), Const.API, operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }


    /**
     * 根据APIId查询statusCode
     *
     * @param apiId 接口id
     * @return statusCode
     */
    @RequestMapping(params = {"Action=DescribeStatusCode"}, method = RequestMethod.GET)
    public Object getStatusCode(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的statusCode", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiStatusCode> apiStatusCodes = apiBodyService.listStatusCode(apiId, Const.API);
        List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiStatusCodes)) {
            apiStatusCodeBasicDtos = BeanUtil.copyList(apiStatusCodes, ApiStatusCodeBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ResponseStatusCode", apiStatusCodeBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 生成responsebody
     *
     * @param apiBodysDto apiBody的包装dto
     * @return 创建结果
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateResponseBody"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateResponseBody", description = "编辑response body 参数")
    public Object addResponseBody(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建request body，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.RESPONSE_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.RESPONSE_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            apiBody.setAssociationType(org.hango.cloud.gdashboard.api.meta.AssociationType.NORMAL.name());
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_RESPONSE_BODY, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = getOperationLog(bodyList);
        String operation = UserPermissionHolder.getAccountId() + "修改了该API的Response Body，修改后response body中的参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiBodysDto.getId(), Const.API, operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }


    /**
     * 根据apiID查询responseBody
     *
     * @param apiId 接口APIId
     * @return ResponseBody
     */
    @RequestMapping(params = {"Action=DescribeResponseBody"}, method = RequestMethod.GET)
    public Object getResponseBody(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的response body", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.RESPONSE_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ResponseBody", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 查询request body
     *
     * @param apiId APIId
     * @return RequestBody
     */
    @RequestMapping(params = {"Action=DescribeRequestBody"}, method = RequestMethod.GET)
    public Object getRequestBody(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的request body", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.REQUEST_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("RequestBody", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 通过json导入body
     *
     * @param apiBodyJsonDto json导入的包装dto
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=GenerateBodyByJson"}, method = RequestMethod.POST)
    @Audit(eventName = "GenerateBodyByJson", description = "导入Json生成参数")
    public Object addBodyByJson(@Validated @RequestBody ApiBodyJsonDto apiBodyJsonDto) {
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, apiBodyJsonDto.getId(), null);
        AuditResourceHolder.set(resource);

        ApiInfo apiInfo = apiInfoService.getApiById(apiBodyJsonDto.getId());
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        resource.setResourceName(apiInfo.getApiName());
        //duubo服务不支持通过json导入body
        //FIXME duubo的判断，是否可以通过前端隐藏
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(apiInfo.getServiceId());
        if (ServiceType.dubbo.name().equals(serviceInfo.getServiceType())) {
            return apiReturn(CommonErrorCode.Success);
        }
        //type值校验
        if (!Const.REQUEST_PARAM_TYPE.equals(apiBodyJsonDto.getType()) &&
                !Const.RESPONSE_PARAM_TYPE.equals(apiBodyJsonDto.getType())) {
            return apiReturn(CommonErrorCode.InvalidParameter(apiBodyJsonDto.getType(), "Type"));
        }

        List<ApiBody> bodyList = apiConvertToJsonService.generateApiBodyByJson(apiBodyJsonDto.getId(), apiInfo.getServiceId(), apiBodyJsonDto.getParams(), apiBodyJsonDto.getType());
        StringBuilder stringBuilder = getOperationLog(bodyList);
        String operation = UserPermissionHolder.getAccountId() + "通过json导入的方式添加该API的" + apiBodyJsonDto.getType() + " Body，修改后参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiBodyJsonDto.getId(), Const.API, operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 查询json信息
     *
     * @param type  查询类型，Request,Response
     * @param apiId 接口id
     * @return
     */
    @RequestMapping(params = {"Action=DescribeBodyParamJson"}, method = RequestMethod.GET)
    public Object getBodyJson(@RequestParam(value = "Type") String type, @RequestParam(value = "ApiId") long apiId) {
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        //type值校验
        if (!Const.REQUEST_PARAM_TYPE.equals(type) &&
                !Const.RESPONSE_PARAM_TYPE.equals(type)) {
            return apiReturn(CommonErrorCode.InvalidParameter(type, "Type"));
        }
        Map<String, Object> paramMap = apiConvertToJsonService.generateJsonForApi(apiId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", paramMap);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 删除body中的某个param
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteBodyParamId"}, method = RequestMethod.GET)
    public Object deleteBodyParamId(@RequestParam(value = "ParamId") Long paramId) {
        apiBodyService.deleteBodyParam(paramId);
        return apiReturn(CommonErrorCode.Success);
    }

    private StringBuilder getOperationLog(List<ApiBody> bodyList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");

        ApiParamType apiParamType;

        int count = 1;
        String required;
        for (ApiBody apiBody : bodyList) {
            apiParamType = apiParamTypeService.listApiParamType(apiBody.getParamTypeId());
            stringBuilder.append(count + ". 名称：" + apiBody.getParamName() + ", 类型：" + apiParamType.getParamType() + ", ");
            if ("Array".equals(apiParamType.getParamType())) {
                //Array数据类型必须填写ArrayDataTypeId
                apiParamType = apiParamTypeService.listApiParamType(apiBody.getArrayDataTypeId());
                stringBuilder.append("其中Array中的数据类型为：" + apiParamType.getParamType() + ", ");
            }

            if ("0".equals(apiBody.getRequired())) {
                required = "否";
            } else {
                required = "是";
            }
            stringBuilder.append("默认取值：" + apiBody.getDefValue() + ", 是否必填：" + required + ", 描述：" + apiBody.getDescription() + ". ");
            count++;
        }
        stringBuilder.append("}");
        return stringBuilder;
    }


    private StringBuilder getStatusCodeOperationLog(List<ApiStatusCode> apiStatusCodeList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        int count = 1;
        for (ApiStatusCode statusCode : apiStatusCodeList) {
            stringBuilder.append(count + ". 返回码：" + statusCode.getStatusCode() + ", 描述：" + statusCode.getDescription());
            count++;
        }
        stringBuilder.append("}");
        return stringBuilder;
    }
}
