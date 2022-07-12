package org.hango.cloud.dashboard.apiserver.web.controller.platform;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.platform.ServiceAuthServiceImpl;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanjiahao
 * 提供至前端，从平台获取环境信息以及外部认证相关信息
 * 从平台获取用户信息,支持模糊匹配
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class AuthorityEnvController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityEnvController.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private ServiceAuthServiceImpl serviceAuthService;

    @RequestMapping(params = "Action=DescribeExterServiceFuzzyMatch", method = RequestMethod.GET)
    public String describeExterServiceByFuzzy(@RequestParam(value = "GwId") String gwId,
                                              @RequestParam(value = "Pattern", required = false) String pattern) {
        logger.info("获取gwId:{}环境下的patter:{}外部服务认证", gwId, pattern);

        GatewayInfo gatewayInfo = gatewayInfoService.get(NumberUtils.toLong(gwId));
        if (gatewayInfo == null) {
            //异常处理
            ErrorCode errorCode = CommonErrorCode.InvalidParameterValueGwId(gwId);
            return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
        }
        ResultWithMessage resultWithMessage;
        if (StringUtils.isNotBlank(pattern)) {
            resultWithMessage = serviceAuthService.getExterServiceFromAuth(gatewayInfo.getAuthAddr(), pattern.trim());
        } else {
            resultWithMessage = serviceAuthService.getExterServiceByLimit(gatewayInfo.getAuthAddr());
        }
        if (resultWithMessage != null) {
            Map<String, Object> result = new HashMap<>();
            if (resultWithMessage.getCode() != HttpStatus.SC_OK && resultWithMessage.getResult() != null) {
//                return apiReturn(resultWithMessage.getCode(), (String) ((JSONObject) resultWithMessage.getResult()).get("Code"), resultWithMessage.getMessage(), null);
                //auth返回异常，gportal统一处理，返回空
                return apiReturn(200, null, null, null);
            }
            JSONObject responseBodyObject = (JSONObject) resultWithMessage.getResult();
            result.put("ExterServiceList", responseBodyObject.get("ExterServiceList"));
            return apiReturn(200, null, null, result);
        } else {
            return apiReturn(200, null, null, null);
        }
    }
}
