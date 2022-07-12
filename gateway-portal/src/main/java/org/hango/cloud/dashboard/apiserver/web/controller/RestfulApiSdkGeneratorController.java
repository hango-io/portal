package org.hango.cloud.dashboard.apiserver.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.SdkConst;
import org.hango.cloud.dashboard.apiserver.service.sdk.IRestfulSdkService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.LogTraceUUIDHolder;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author hanjiahao
 * restful sdk 生成controller
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class RestfulApiSdkGeneratorController extends AbstractController {
    private static Logger logger = LoggerFactory.getLogger(RestfulApiSdkGeneratorController.class);

    @Autowired
    private IRestfulSdkService restfulSdkService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IApiInfoService apiInfoService;


    /**
     * 生成restfulSDK
     *
     * @param apiId
     * @return
     */
    @GetMapping(params = {"Action=GetRestfulApiSdk"}, produces = "application/octet-stream;charset=UTF-8")
    public Object getApiSdk(@RequestParam(value = "ApiId") String apiId) {
        ApiInfo apiInfo = apiInfoService.getApi(apiId);
        if (apiInfo == null) {
            return apiReturn(CommonErrorCode.InvalidParameterValue(apiId, "ApiId"));
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceById(String.valueOf(apiInfo.getServiceId()));
        if (serviceInfo != null && StringUtils.isBlank(serviceInfo.getServiceName())) {
            logger.error("生成服务SDK，服务标识为空，不允许下载");
            return apiReturn(CommonErrorCode.ServiceTagIsNull);
        }

        // 生成SDK
        String uuid = LogTraceUUIDHolder.getUUIDId();
        String jarName = restfulSdkService.generateApiSdk(apiId, uuid, SdkConst.SINGLE_API_SDK);

        // 如果生成失败返回响应信息
        if (!jarName.endsWith(".jar")) {
            if (jarName == SdkConst.API_NOT_FOUND) {
                restfulSdkService.deleteTemp(uuid);
                return apiReturn(CommonErrorCode.NoSuchApiInterface);
            } else {
                restfulSdkService.deleteTemp(uuid);
                return apiReturn(CommonErrorCode.InternalServerError);
            }
        }

        // 提供下载
        String jarPath = SdkConst.CODE_DIRECTORY + uuid + "/jar/" + jarName;
        ResponseEntity<InputStreamResource> response = null;
        try {
            response = restfulSdkService.supplyDownload(jarPath, jarName);
        } catch (Exception e) {
            logger.info("下载sdk出现异常，e:{}", e);
        }
        if (response == null) {
            restfulSdkService.deleteTemp(uuid);
            apiReturn(CommonErrorCode.InternalServerError);
        }
        logger.info("File download service success run");

        // 删除服务器生成文件
        restfulSdkService.deleteTemp(uuid);
        return response;
    }

    /**
     * 根据服务ID生成服务的SDK
     *
     * @param serviceId
     * @throws IOException
     */
    @GetMapping(params = {"Action=GetRestfulServiceSdk"}, produces = "application/octet-stream;charset=UTF-8")
    public Object getServiceSdk(@RequestParam(value = "ServiceId") String serviceId) {
        ServiceInfo serviceInfo = serviceInfoService.getServiceById(serviceId);
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.InvalidParameterValue(serviceId, "ServiceId"));
        }
        if (serviceInfo != null && StringUtils.isBlank(serviceInfo.getServiceName())) {
            logger.error("根据服务id生成服务SDK，服务标识为空，不允许下载");
            return apiReturn(CommonErrorCode.ServiceTagIsNull);
        }

        // 生成SDK
        String uuid = LogTraceUUIDHolder.getUUIDId();
        String jarName = restfulSdkService.generateServiceSdk(serviceId, uuid);

        // 如果生成失败返回响应信息
        if (!jarName.endsWith(".jar")) {
            if (jarName == SdkConst.SERVICE_NOT_FOUND) {
                restfulSdkService.deleteTemp(uuid);
                return apiReturn(CommonErrorCode.NoSuchService);
            } else if (jarName == SdkConst.API_NOT_FOUND) {
                restfulSdkService.deleteTemp(uuid);
                return apiReturn(CommonErrorCode.CannotDownloadSDK);
            } else {
                restfulSdkService.deleteTemp(uuid);
                return apiReturn(CommonErrorCode.InternalServerError);
            }
        }

        // 提供下载
        String jarPath = SdkConst.CODE_DIRECTORY + uuid + "/jar/" + jarName;
        ResponseEntity<InputStreamResource> response = null;
        try {
            response = restfulSdkService.supplyDownload(jarPath, jarName);
        } catch (Exception e) {
            logger.info("生成sdk出现异常，e:{}", e);
        }
        if (response == null) {
            restfulSdkService.deleteTemp(uuid);
            apiReturn(CommonErrorCode.InternalServerError);
        }
        logger.info("File download service success run");

        // 删除服务器生成文件
        restfulSdkService.deleteTemp(uuid);
        return response;
    }
}
