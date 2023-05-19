package org.hango.cloud.envoy.infra.serviceproxy.controller;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ResultDTO;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ServiceRefreshDTO;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/5
 */
@RestController
@RequestMapping(value = ApiConst.HANGO_SERICE_V1_PREFIX)
@Validated
public class EnvoyServiceProxyController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyController.class);

    @Autowired
    private IEnvoyServiceRefreshService envoyServiceProxyService;

    /**
     * 刷新服务域名（会刷新vs资源）
     * @return
     */
    @RequestMapping(params = {"Action=RefreshServiceHost"}, method = RequestMethod.POST)
    public Object refreshServiceHost(@Validated @RequestBody ServiceRefreshDTO serviceRefreshDTO) {
        logger.info("刷新服务, refresh:{}", JSONObject.toJSONString(serviceRefreshDTO));
        ErrorCode errorCode = envoyServiceProxyService.checkRefrshParam(serviceRefreshDTO);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        ResultDTO resultDTO = envoyServiceProxyService.refreshServiceHost(serviceRefreshDTO);
        return apiReturn(new Result(resultDTO));
    }
}
