package org.hango.cloud.envoy.infra.healthcheck.controller;

import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.infra.healthcheck.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.envoy.infra.healthcheck.service.IEnvoyHealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康检查功能
 *
 * @author TC_WANG
 * @date 2019/11/19 下午2:59.
 */
@RestController
@RequestMapping(value = {ApiConst.HANGO_SERICE_V1_PREFIX})
public class EnvoyHealthCheckController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckController.class);

    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @RequestMapping(params = {"Action=UpdateHealthCheckRule"}, method = RequestMethod.POST)
    public String updateHealthCheckRule(@Validated @RequestBody HealthCheckRuleDto healthCheckRuleDto) {
        logger.info("更新服务健康检查规则，healthCheckRuleDto:{}", healthCheckRuleDto);

        ErrorCode checkResult = envoyHealthCheckService.checkUpdateHealthCheckRuleParam(healthCheckRuleDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(Result.err(checkResult));
        }

        checkResult = envoyHealthCheckService.updateHealthCheckRuleParam(healthCheckRuleDto);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        return apiReturn(new Result(true));
    }

    @GetMapping(params = {"Action=DescribeHealthCheckRule"})
    public String describeHealthCheckRule(@RequestParam(value = "ServiceId") long serviceId) {
        logger.info("查询服务健康检查规则, serviceId:{}", serviceId);
        HealthCheckRuleDto healthCheckRuleDto = envoyHealthCheckService.getHealthCheckRule(serviceId);
        Map<String, Object> result = new HashMap<>(1);
        result.put("HealthCheckRule", healthCheckRuleDto);
        return apiReturnSuccess(result);
    }

    @GetMapping(params = {"Action=DescribeServiceInstanceList"})
    public String describeServiceInstanceList(@RequestParam(value = "ServiceId") long serviceId) {
        logger.info("查询服务实例详情, serviceId:{}", serviceId);
        ServiceProxyDto serviceProxyDto = serviceProxyService.get(serviceId);
        if (serviceProxyDto == null){
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        List<EnvoyServiceInstanceDto> envoyServiceInstanceDtoList = envoyHealthCheckService.getServiceInstanceList(serviceProxyDto);
        Map<String, Object> result = new HashMap<>(1);
        result.put("InstanceList", envoyServiceInstanceDtoList);
        return apiReturnSuccess(result);
    }

}
