package org.hango.cloud.common.infra.api.controller.apimanage;


import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiManageConst;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeaderBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeadersDto;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
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
 * Api header controller,包括 request header 以及 response header
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class ApiHeaderController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiHeaderController.class);

    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiHeaderService apiHeaderService;


    /**
     * 添加api header
     *
     * @param apiHeadersDto
     * @return
     */
    @RequestMapping(params = {"Action=CreateRequestHeader"}, method = RequestMethod.POST)
    public Object addRequestHeader(@Validated @RequestBody ApiHeadersDto apiHeadersDto) {
        logger.info("创建API request:header：api header:{}", apiHeadersDto);
        ApiErrorCode errorCode = apiHeaderService.checkCreateOrUpdateHeader(apiHeadersDto);
        //参数校验
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
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
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_REQUEST_HEADER, apiHeaderId, apiHeader.getParamName()));
        }
        //操作审计记录资源名称
        //AuditResourceHolder.set(resourceDataDtoList);


        stringBuilder.append("}");
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * 添加response header
     *
     * @param apiHeadersDto
     * @return
     */
    @RequestMapping(params = {"Action=CreateResponseHeader"}, method = RequestMethod.POST)
    public Object addResponseHeader(@Validated @RequestBody ApiHeadersDto apiHeadersDto) {
        logger.info("创建API response header，api response header:{}", apiHeadersDto);
        ApiErrorCode errorCode = apiHeaderService.checkCreateOrUpdateHeader(apiHeadersDto);
        //参数校验
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        List<ApiHeader> headerList = apiHeaderService.generateApiHeaderFromApiHeaderList(apiHeadersDto, Const.RESPONSE_PARAM_TYPE);
        apiHeaderService.deleteHeader(apiHeadersDto.getId(), Const.RESPONSE_PARAM_TYPE);
        //构造审计资源
        List<ResourceDataDto> resourceDataDtoList = new ArrayList<>();

        headerList.forEach(apiHeader -> {
            long apiHeaderId = apiHeaderService.addHeader(apiHeader);
            resourceDataDtoList.add(new ResourceDataDto(ApiManageConst.AUDIT_RESOURCE_TYPE_API_RESPONSE_HEADER, apiHeaderId, apiHeader.getParamName()));

        });
        //操作审计记录资源名称
        //AuditResourceHolder.set(resourceDataDtoList);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        int count = 1;
        for (ApiHeader apiHeader : headerList) {
            stringBuilder.append(count + ". 名称：" + apiHeader.getParamName() + ", 取值：" + apiHeader.getParamValue() + ", 描述："
                    + apiHeader.getDescription() + ". ");
            count++;
        }
        stringBuilder.append("}");
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * 查询request header
     */
    @RequestMapping(params = {"Action=DescribeRequestHeader"}, method = RequestMethod.GET)
    public Object getRequestHeader(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的requestHeader", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiHeader> apiHeaders = apiHeaderService.getHeader(apiId, Const.REQUEST_PARAM_TYPE);
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiHeaders)) {
            apiHeaderBasicDtos = BeanUtil.copyList(apiHeaders, ApiHeaderBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("Headers", apiHeaderBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);

    }

    /**
     * 查询request header
     */
    @RequestMapping(params = {"Action=DescribeResponseHeader"}, method = RequestMethod.GET)
    public Object getResponseHeader(@RequestParam(value = "ApiId") long apiId) {
        logger.info("查询apiId:{}下的responseHeader", apiId);
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        List<ApiHeader> apiHeaders = apiHeaderService.getHeader(apiId, Const.RESPONSE_PARAM_TYPE);
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiHeaders)) {
            apiHeaderBasicDtos = BeanUtil.copyList(apiHeaders, ApiHeaderBasicDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("Headers", apiHeaderBasicDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);

    }

    /**
     * 删除header中的某个param
     */
    @RequestMapping(params = {"Action=DeleteHeaderByParamId"}, method = RequestMethod.GET)
    public Object deleteHeaderParam(@RequestParam(value = "ParamId") Long paramId) {
        apiHeaderService.deleteHeaderParam(paramId);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

}

