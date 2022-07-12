package org.hango.cloud.dashboard.apiserver.web.controller.apimanage;

import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.gdashboard.api.dto.ApiHeaderBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeadersDto;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.OperationLog;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
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
 * Api header controller,包括 request header 以及 response header
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class ApiHeaderController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiHeaderController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IOperationLogService operationLogService;
    @Autowired
    private IApiHeaderService apiHeaderService;


    /**
     * 添加api header
     *
     * @param apiHeadersDto
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateRequestHeader"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateRequestHeader", description = "编辑API request header参数")
    public Object addRequestHeader(@Validated @RequestBody ApiHeadersDto apiHeadersDto) {
        logger.info("创建API request:header：api header:{}", apiHeadersDto);
        ApiErrorCode errorCode = apiHeaderService.checkCreateOrUpdateHeader(apiHeadersDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ApiHeader> headerList = apiHeaderService.generateApiHeaderFromApiHeaderList(apiHeadersDto, Const.REQUEST_PARAM_TYPE);
        apiHeaderService.deleteHeader(apiHeadersDto.getId(), Const.REQUEST_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        int count = 1;
        for (ApiHeader apiHeader : headerList) {
            long apiHeaderId = apiHeaderService.addHeader(apiHeader);
            stringBuilder.append(count + ". 名称：" + apiHeader.getParamName() + ", 取值：" + apiHeader.getParamValue() + ", 描述："
                    + apiHeader.getDescription() + ". ");
            count++;
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_REQUEST_HEADER, apiHeaderId, apiHeader.getParamName()));
        }
        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);


        stringBuilder.append("}");
        String operation = UserPermissionHolder.getAccountId() + "修改了该API的 Request Header，修改后Request Header中的参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiHeadersDto.getId(), "api", operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 添加response header
     *
     * @param apiHeadersDto
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateResponseHeader"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateResponseHeader", description = "编辑API response header参数")
    public Object addResponseHeader(@Validated @RequestBody ApiHeadersDto apiHeadersDto) {
        logger.info("创建API response header，api response header:{}", apiHeadersDto);
        ApiErrorCode errorCode = apiHeaderService.checkCreateOrUpdateHeader(apiHeadersDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        List<ApiHeader> headerList = apiHeaderService.generateApiHeaderFromApiHeaderList(apiHeadersDto, Const.RESPONSE_PARAM_TYPE);
        apiHeaderService.deleteHeader(apiHeadersDto.getId(), Const.RESPONSE_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();

        headerList.forEach(apiHeader -> {
            long apiHeaderId = apiHeaderService.addHeader(apiHeader);
            resourceDataDtoList.add(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API_RESPONSE_HEADER, apiHeaderId, apiHeader.getParamName()));

        });
        //操作审计记录资源名称
        AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        int count = 1;
        for (ApiHeader apiHeader : headerList) {
            stringBuilder.append(count + ". 名称：" + apiHeader.getParamName() + ", 取值：" + apiHeader.getParamValue() + ", 描述："
                    + apiHeader.getDescription() + ". ");
            count++;
        }
        stringBuilder.append("}");
        String operation = UserPermissionHolder.getAccountId() + "修改了该API的 Response Header，修改后Request Header中的参数信息为：" + stringBuilder.toString();
        OperationLog operationLog = operationLogService.getOperationLog(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                apiHeadersDto.getId(), "api", operation);
        operationLogService.addApiOperationLog(operationLog);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 查询request header
     */
    @RequestMapping(params = {"Action=DescribeRequestHeader"}, method = RequestMethod.GET)
    public Object getRequestHeader(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的requestHeader", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiHeader> apiHeaders = apiHeaderService.getHeader(apiId, Const.REQUEST_PARAM_TYPE);
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiHeaders)) {
            apiHeaderBasicDtos = BeanUtil.copyList(apiHeaders, ApiHeaderBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Headers", apiHeaderBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);

    }

    /**
     * 查询request header
     */
    @RequestMapping(params = {"Action=DescribeResponseHeader"}, method = RequestMethod.GET)
    public Object getResponseHeader(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的responseHeader", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }
        List<ApiHeader> apiHeaders = apiHeaderService.getHeader(apiId, Const.RESPONSE_PARAM_TYPE);
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiHeaders)) {
            apiHeaderBasicDtos = BeanUtil.copyList(apiHeaders, ApiHeaderBasicDto.class);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Headers", apiHeaderBasicDtos);
        return apiReturn(CommonErrorCode.Success, result);

    }

    /**
     * 删除header中的某个param
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteHeaderByParamId"}, method = RequestMethod.GET)
    public Object deleteHeaderParam(@RequestParam(value = "ParamId") Long paramId) {
        apiHeaderService.deleteHeaderParam(paramId);
        return apiReturn(CommonErrorCode.Success);
    }

}
