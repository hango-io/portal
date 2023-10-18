package org.hango.cloud.common.infra.api.controller.apimanage;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.ApiManageConst;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.gdashboard.api.dto.*;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiConvertToJsonService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.HANGO_DASHBOARD_PREFIX;

/**
 * API body controller，包括request body,response body 以及queryString
 * 同时包括dubbo param以及webservice param的创建
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiBodyController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(ApiBodyController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IApiConvertToJsonService apiConvertToJsonService;

    public static final String ASSOCIATION_TYPE = "NORMAL";

    /**
     * 添加request body
     *
     * @param apiBodysDto apiBody包装dto
     * @return 创建request body结果
     */
    @RequestMapping(params = {"Action=CreateRequestBody"}, method = RequestMethod.POST)
    public Object addRequestBody(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建request body，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //FIXME dubbo类型如何判断
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.REQUEST_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.REQUEST_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            apiBody.setAssociationType(ASSOCIATION_TYPE);
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_REQUEST_BODY, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        StringBuilder stringBuilder = getOperationLog(bodyList);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * create query String
     *
     * @param apiBodysDto apiBody包装dto
     * @return 创建queryString结果
     */
    @RequestMapping(params = {"Action=CreateQueryString"}, method = RequestMethod.POST)
    public Object addQueryString(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建query string，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.QUERYSTRING_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.QUERYSTRING_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_QUERY_STRING, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        //AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = getOperationLog(bodyList);
        return apiReturn(CommonErrorCode.SUCCESS);
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
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.QUERYSTRING_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("QueryString", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 创建statusCode
     *
     * @param apiStatusCodesDto statusCode的包装dto
     * @return 创建结果
     */
    @RequestMapping(params = {"Action=CreateStatusCode"}, method = RequestMethod.POST)
    public Object addStatusCode(@Validated @RequestBody ApiStatusCodesDto apiStatusCodesDto) {
        ApiInfo apiInfo = apiInfoService.getApiById(apiStatusCodesDto.getId());
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        //FIXME dubbo服务不需要statuscode
        List<ApiStatusCode> apiStatusCodes = apiBodyService.generateApiStatusCodeFromCodeList(apiStatusCodesDto);
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        for (ApiStatusCode apiStatusCode : apiStatusCodes) {
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_STATUS_CODE, StringUtils.EMPTY, String.valueOf(apiStatusCode.getStatusCode())));
        }
        //操作审计记录资源名称
        //AuditResourceHolder.set(resourceDataDtoList);
        apiBodyService.addStatusCodes(apiStatusCodes, apiStatusCodesDto.getId(), ApiConst.API);

        return apiReturn(CommonErrorCode.SUCCESS);
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
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiStatusCode> apiStatusCodes = apiBodyService.listStatusCode(apiId, ApiConst.API);
        List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiStatusCodes)) {
            apiStatusCodeBasicDtos = BeanUtil.copyList(apiStatusCodes, ApiStatusCodeBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("ResponseStatusCode", apiStatusCodeBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 生成responsebody
     *
     * @param apiBodysDto apiBody的包装dto
     * @return 创建结果
     */
    @RequestMapping(params = {"Action=CreateResponseBody"}, method = RequestMethod.POST)
    public Object addResponseBody(@Validated @RequestBody ApiBodysDto apiBodysDto) {
        logger.info("创建response body，apiBody：{}", apiBodysDto);
        ApiErrorCode errorCode = apiBodyService.checkApiBodyBasicInfo(apiBodysDto);
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        List<ApiBody> bodyList = apiBodyService.generateApiBodyFromApiBodyList(apiBodysDto, Const.RESPONSE_PARAM_TYPE);
        apiBodyService.deleteBody(apiBodysDto.getId(), Const.RESPONSE_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        bodyList.forEach(apiBody -> {
            apiBody.setAssociationType(org.hango.cloud.gdashboard.api.meta.AssociationType.NORMAL.name());
            long apiBodyId = apiBodyService.addBody(apiBody);
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_RESPONSE_BODY, apiBodyId, apiBody.getParamName()));
        });

        //操作审计记录资源名称
        //AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = getOperationLog(bodyList);
        return apiReturn(CommonErrorCode.SUCCESS);
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
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.RESPONSE_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("ResponseBody", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
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
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiBody> apiBodies = apiBodyService.getBody(apiId, Const.REQUEST_PARAM_TYPE);
        List<ApiBodyBasicDto> apiBodyBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiBodies)) {
            apiBodyBasicDtos = BeanUtil.copyList(apiBodies, ApiBodyBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("RequestBody", apiBodyBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 通过json导入body
     *
     * @param apiBodyJsonDto json导入的包装dto
     * @return
     */
    @RequestMapping(params = {"Action=GenerateBodyByJson"}, method = RequestMethod.POST)
    public Object addBodyByJson(@Validated @RequestBody ApiBodyJsonDto apiBodyJsonDto) {
        ResourceDataDto resource = new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API, apiBodyJsonDto.getId(), null);
       

        ApiInfo apiInfo = apiInfoService.getApiById(apiBodyJsonDto.getId());
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        resource.setResourceName(apiInfo.getApiName());
        //duubo服务不支持通过json导入body
        //FIXME duubo的判断，是否可以通过前端隐藏
//        ServiceDto serviceDto = serviceInfoService.get(apiInfo.getServiceId());
//        if (ServiceType.dubbo.name().equals(serviceDto.getServiceType())) {
//            return apiReturn(CommonErrorCode.SUCCESS);
//        }
        //type值校验
        if (!Const.REQUEST_PARAM_TYPE.equals(apiBodyJsonDto.getType()) &&
                !Const.RESPONSE_PARAM_TYPE.equals(apiBodyJsonDto.getType())) {
            return apiReturn(CommonErrorCode.invalidParameter(apiBodyJsonDto.getType(), "Type"));
        }

        List<ApiBody> bodyList = apiConvertToJsonService.generateApiBodyByJson(apiBodyJsonDto.getId(), apiInfo.getServiceId(), apiBodyJsonDto.getParams(), apiBodyJsonDto.getType());
        StringBuilder stringBuilder = getOperationLog(bodyList);
        return apiReturn(CommonErrorCode.SUCCESS);
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
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        //type值校验
        if (!Const.REQUEST_PARAM_TYPE.equals(type) &&
                !Const.RESPONSE_PARAM_TYPE.equals(type)) {
            return apiReturn(CommonErrorCode.invalidParameter(type, "Type"));
        }
        Map<String, Object> paramMap = apiConvertToJsonService.generateJsonForApi(apiId, type);
        Map<String, Object> result = Maps.newHashMap();
        result.put("Result", paramMap);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 删除body中的某个param
     */
    @RequestMapping(params = {"Action=DeleteBodyParamId"}, method = RequestMethod.GET)
    public Object deleteBodyParamId(@RequestParam(value = "ParamId") Long paramId) {
        apiBodyService.deleteBodyParam(paramId);
        return apiReturn(CommonErrorCode.SUCCESS);
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
