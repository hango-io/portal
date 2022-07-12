package org.hango.cloud.dashboard.apiserver.web.controller.webservice;

import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IWebServiceParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析wsdl文件中的接口名和方法名
 *
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2019/1/7 下午5:32.
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class WebServiceController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(WebServiceController.class);


    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IWebServiceParamService webServiceParamService;

    /**
     * 获取wsdl文件中的接口（服务）列表
     *
     * @param apiId
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=GetWebServiceInterface"}, method = RequestMethod.GET)
    public Object getWebServiceInterface(@RequestParam(name = "ApiId") final long apiId) throws URISyntaxException {

        ErrorCode errorCode;
        //查询服务基本信息
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }

        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        ServiceInfo serviceInfo = serviceInfoService.getServiceById(String.valueOf(apiInfo.getServiceId()));
        if (serviceInfo.getServiceType().equals(ServiceType.webservice.name())) {
            list = webServiceParamService.getWebServiceInterfaceByCache(serviceInfo.getWsdlUrl());
        }
        map.put("Interfaces", list);
        return apiReturnSuccess(map);
    }

    /**
     * 根据Id查询api基本信息
     *
     * @param apiId
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=GetWebServiceMethod"}, method = RequestMethod.GET)
    public Object getWebServiceMethod(@RequestParam(name = "ApiId") final long apiId) throws URISyntaxException {

        ErrorCode errorCode;
        //查询服务基本信息
        ApiInfo apiInfo = apiInfoService.getApiById(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchApiInterface);
        }

        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        ServiceInfo serviceInfo = serviceInfoService.getServiceById(String.valueOf(apiInfo.getServiceId()));
        if (serviceInfo.getServiceType().equals(ServiceType.webservice.name())) {
            list = webServiceParamService.getWebServiceMethod(serviceInfo.getWsdlUrl());
            map.put("Methods", list);
            return apiReturnSuccess(map);
        }
        return list;
    }
}
