package org.hango.cloud.common.infra.api.controller.apimanage;


import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.gdashboard.api.dto.ApiParamTypeDto;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hango.cloud.common.infra.base.meta.BaseConst.HANGO_DASHBOARD_PREFIX;

/**
 * 获取api param相关type
 */
@RestController
@RequestMapping(value = HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ApiParamTypeController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(ApiParamTypeController.class);

    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiModelService apiModelService;

    /**
     * 获取请求头中的param
     *
     * @return
     */
    @RequestMapping(params = {"Action=DescribeHeaderParamType"}, method = RequestMethod.GET)
    public Object describeHeaderParam() {
        List<ApiParamType> apiParamTypes = apiParamTypeService.listParamTypeInHeader();
        List<ApiParamTypeDto> apiParamTypeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiParamTypes)) {
            apiParamTypeDtos = BeanUtil.copyList(apiParamTypes, ApiParamTypeDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("ApiParamType", apiParamTypeDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 获取请求queryString中的param
     *
     * @return
     */
    @RequestMapping(params = {"Action=DescribeQueryStringParamType"}, method = RequestMethod.GET)
    public Object describeQueryStringParam() {
        List<ApiParamType> apiParamTypes = apiParamTypeService.listParamTypeInBody();
        List<ApiParamTypeDto> apiParamTypeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apiParamTypes)) {
            apiParamTypeDtos = BeanUtil.copyList(apiParamTypes, ApiParamTypeDto.class);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("QueryStringParamType", apiParamTypeDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    @RequestMapping(params = {"Action=DescribeBodyParam"}, method = RequestMethod.GET)
    public Object describeBodyParam(@RequestParam(value = "ServiceId") Long serviceId) {
        List<Long> modelIdList = apiModelService.getApiModelInfoByServiceId(String.valueOf(serviceId));

        //根据ModelId查询param_type表
        List<ApiParamType> paramTypeModelList = apiParamTypeService.listModleParamType(modelIdList);
        //查询基本类型参数
        List<ApiParamType> paramTypeBodyList = apiParamTypeService.listParamTypeInBody();

        List<ApiParamTypeDto> apiParamBodyTypeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(paramTypeBodyList)) {
            apiParamBodyTypeDtos = BeanUtil.copyList(paramTypeBodyList, ApiParamTypeDto.class);
        }
        List<ApiParamTypeDto> apiParamModelTypeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(paramTypeModelList)) {
            apiParamModelTypeDtos = BeanUtil.copyList(paramTypeModelList, ApiParamTypeDto.class);
        }

        List<ApiParamTypeDto> apiResultParam = new ArrayList<>();
        apiResultParam.addAll(apiParamBodyTypeDtos);
        apiResultParam.addAll(apiParamModelTypeDtos);

        Map<String, Object> result = Maps.newHashMap();
        result.put("BodyParam", apiResultParam);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    @RequestMapping(params = {"Action=DescribeApiDocument"}, method = RequestMethod.GET)
    public Object describeApiDocument() {
        List<ApiDocumentStatus> apiDocumentStatuses = apiParamTypeService.listApiDocumentStatus();
        Map<String, Object> result = Maps.newHashMap();
        result.put("Result", apiDocumentStatuses);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }
}
