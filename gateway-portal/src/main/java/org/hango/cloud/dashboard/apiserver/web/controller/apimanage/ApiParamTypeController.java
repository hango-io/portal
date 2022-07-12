package org.hango.cloud.dashboard.apiserver.web.controller.apimanage;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.gdashboard.api.dto.ApiParamTypeDto;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取api param相关type
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
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
        Map<String, Object> result = new HashMap<>();
        result.put("ApiParamType", apiParamTypeDtos);
        return apiReturn(CommonErrorCode.Success, result);
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
        Map<String, Object> result = new HashMap<>();
        result.put("QueryStringParamType", apiParamTypeDtos);
        return apiReturn(CommonErrorCode.Success, result);
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
//            //构造modelDto
//            apiParamModelTypeDtos.forEach(apiParamTypeDto -> {
//                CreateApiModelDto apiModelByModelId = apiModelService.getApiModelByModelId(apiParamTypeDto.getModelId());
//                apiParamTypeDto.setCreateApiModelDto(apiModelByModelId);
//            });
        }

        List<ApiParamTypeDto> apiResultParam = new ArrayList<>();
        apiResultParam.addAll(apiParamBodyTypeDtos);
        apiResultParam.addAll(apiParamModelTypeDtos);

        Map<String, Object> result = new HashMap<>();
        result.put("BodyParam", apiResultParam);
        return apiReturn(CommonErrorCode.Success, result);
    }

    @RequestMapping(params = {"Action=DescribeApiDocument"}, method = RequestMethod.GET)
    public Object describeApiDocument() {
        List<ApiDocumentStatus> apiDocumentStatuses = apiParamTypeService.listApiDocumentStatus();
        Map<String, Object> result = new HashMap<>();
        result.put("Result", apiDocumentStatuses);
        return apiReturn(CommonErrorCode.Success, result);
    }
}
