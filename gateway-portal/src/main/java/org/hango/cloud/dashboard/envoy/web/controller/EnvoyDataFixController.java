package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.cloud.ncegdashboard.envoy.web.dto.PublishResultDto;
import com.netease.cloud.ncegdashboard.envoy.web.dto.RePublishPluginDto;
import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.envoy.service.IEnvoyDateFixService;
import org.hango.cloud.dashboard.envoy.web.dto.RePublishRouteRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.RePublishServiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Envoy网关数据修复相关接口
 *
 * @author hzchenzhongyang 2020-01-19
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyDataFixController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyDataFixController.class);

    @Autowired
    IEnvoyDateFixService envoyDateFixService;

    @PostMapping(params = {"Action=RePublishService"})
    public String rePublishService(@Validated @RequestBody RePublishServiceDto rePublishServiceDto) {
        logger.info("重新发布已发布服务, rePublishServiceDto:{}", rePublishServiceDto);
        ErrorCode checkResult = envoyDateFixService.checkRePublishServiceParam(rePublishServiceDto.getGwId(), rePublishServiceDto.getRePublishAllService(), rePublishServiceDto.getServiceIdList());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        List<Long> failedServiceList = envoyDateFixService.rePublishService(rePublishServiceDto.getGwId(), rePublishServiceDto.getRePublishAllService(), rePublishServiceDto.getServiceIdList());
        if (CollectionUtils.isEmpty(failedServiceList)) {
            return apiReturn(CommonErrorCode.Success);
        }

        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("FailedServiceIdList", failedServiceList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=RePublishRouteRule"})
    public String rePublishRouteRule(@Validated @RequestBody RePublishRouteRuleDto rePublishRouteRuleDto) {
        logger.info("重新发布已发布的路由规则, rePublishRouteRuleDto:{}", rePublishRouteRuleDto);
        ErrorCode checkResult = envoyDateFixService.checkRePublishRouteRuleParam(rePublishRouteRuleDto.getGwId(), rePublishRouteRuleDto.getRePublishAllRouteRule(), rePublishRouteRuleDto.getServiceIdList(), rePublishRouteRuleDto.getRouteRuleIdList());
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        List<Long> failedRouteRuleIdList = envoyDateFixService.rePublishRouteRule(rePublishRouteRuleDto.getGwId(), rePublishRouteRuleDto.getRePublishAllRouteRule(), rePublishRouteRuleDto.getServiceIdList(), rePublishRouteRuleDto.getRouteRuleIdList());
        if (CollectionUtils.isEmpty(failedRouteRuleIdList)) {
            return apiReturn(CommonErrorCode.Success);
        }

        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("FailedRouteRuleIdList", failedRouteRuleIdList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=RePublishRouteDto"})
    public String rePublishRouteDto(@RequestParam(value = "GwId") long gwId) {
        logger.info("重新更新已发布路由，网关id, gwId:{}", gwId);
        List<Long> failedRouteRuleIdList = envoyDateFixService.reFixPublishedRouteDao(gwId);
        if (CollectionUtils.isEmpty(failedRouteRuleIdList)) {
            return apiReturn(CommonErrorCode.Success);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("FailedRouteRuleIdList", failedRouteRuleIdList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }


    @PostMapping(params = {"Action=RePublishPlugin"})
    public String rePublishPlugin(@Validated @RequestBody RePublishPluginDto rePublishPluginDto) {
        logger.info("重新更新已绑定插件，{}", JSONObject.toJSONString(rePublishPluginDto));
        PublishResultDto publishResult = envoyDateFixService.rePublishPlugin(rePublishPluginDto);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("publishResult", publishResult);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=FixAuthnType"})
    public String fixAuthnType(@RequestParam(value = "GwId") long gwId) {
        logger.info("更新super-auth插件kind", gwId);
        if (envoyDateFixService.fixAuthPluginConfig(gwId)){
            return apiReturn(CommonErrorCode.Success);
        }
        return apiReturn(CommonErrorCode.InternalServerError);
    }

}
