package org.hango.cloud.envoy.advanced.bakup.envoy.web.controller;

import com.google.common.collect.Maps;
import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.hango.cloud.envoy.advanced.bakup.envoy.service.IEnvoyDateFixService;
import org.hango.cloud.envoy.advanced.bakup.envoy.web.dto.RePublishRouteRuleDto;
import org.hango.cloud.envoy.advanced.bakup.envoy.web.dto.RePublishServiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        ErrorCode checkResult = envoyDateFixService.checkRePublishServiceParam(rePublishServiceDto.getVirtualGwId(), rePublishServiceDto.getRePublishAllService(), rePublishServiceDto.getServiceIdList());
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        List<Long> failedServiceList = envoyDateFixService.rePublishService(rePublishServiceDto.getVirtualGwId(), rePublishServiceDto.getRePublishAllService(), rePublishServiceDto.getServiceIdList());
        if (CollectionUtils.isEmpty(failedServiceList)) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("FailedServiceIdList", failedServiceList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @PostMapping(params = {"Action=RePublishRouteRule"})
    public String rePublishRouteRule(@Validated @RequestBody RePublishRouteRuleDto rePublishRouteRuleDto) {
        logger.info("重新发布已发布的路由规则, rePublishRouteRuleDto:{}", rePublishRouteRuleDto);
        ErrorCode checkResult = envoyDateFixService.checkRePublishRouteRuleParam(rePublishRouteRuleDto.getVirtualGwId(), rePublishRouteRuleDto.getRePublishAllRouteRule(), rePublishRouteRuleDto.getServiceIdList(), rePublishRouteRuleDto.getRouteRuleIdList());
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }

        List<Long> failedRouteRuleIdList = envoyDateFixService.rePublishRouteRule(rePublishRouteRuleDto.getVirtualGwId(), rePublishRouteRuleDto.getRePublishAllRouteRule(), rePublishRouteRuleDto.getServiceIdList(), rePublishRouteRuleDto.getRouteRuleIdList());
        if (CollectionUtils.isEmpty(failedRouteRuleIdList)) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("FailedRouteRuleIdList", failedRouteRuleIdList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }

    @GetMapping(params = {"Action=RePublishRouteDto"})
    public String rePublishRouteDto(@RequestParam(value = "VirtualGwId") long virtualGwId) {
        logger.info("重新更新已发布路由，网关id, virtualGwId:{}", virtualGwId);
        List<Long> failedRouteRuleIdList = envoyDateFixService.reFixPublishedRouteDao(virtualGwId);
        if (CollectionUtils.isEmpty(failedRouteRuleIdList)) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("FailedRouteRuleIdList", failedRouteRuleIdList);
        return apiReturn(HttpStatus.SC_OK, null, null, result);
    }



    @GetMapping(params = {"Action=FixAuthnType"})
    public String fixAuthnType(@RequestParam(value = "VirtualGwId") long virtualGwId) {
        logger.info("更新super-auth插件kind", virtualGwId);
        if (envoyDateFixService.fixAuthPluginConfig(virtualGwId)) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }
        return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

}
