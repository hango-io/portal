package org.hango.cloud.dashboard.envoy.web.controller;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyHealthCheckController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckController.class);

    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @MethodReentrantLock
    @Audit(eventName = "UpdateHealthCheckRule", description = "更新服务健康检查规则")
    @RequestMapping(params = {"Action=UpdateHealthCheckRule"}, method = RequestMethod.POST)
    public String updateHealthCheckRule(@Validated @RequestBody EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto) {
        logger.info("更新服务健康检查规则，healthCheckRuleDto:{}", envoyHealthCheckRuleDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_HEALTH_CHECK, null, null);
        resource.setResourceId(envoyHealthCheckRuleDto.getServiceId());
        AuditResourceHolder.set(resource);

        ErrorCode checkResult = envoyHealthCheckService.checkUpdateHealthCheckRuleParam(envoyHealthCheckRuleDto);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        checkResult = envoyHealthCheckService.updateHealthCheckRuleParam(EnvoyHealthCheckRuleDto.dtoToMeta(envoyHealthCheckRuleDto));
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribeHealthCheckRule"})
    public String getPluginInfo(@RequestParam(value = "ServiceId") long serviceId,
                                @RequestParam(value = "GwId") long gwId) {
        logger.info("查询服务健康检查规则, serviceId:{}, gwId:{}", serviceId, gwId);
        if (!gatewayInfoService.isGwExists(gwId)) {
            return apiReturn(CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId)));
        }
        if (!serviceInfoService.isServiceExists(serviceId)) {
            return apiReturn(CommonErrorCode.InvalidParameterServiceId(String.valueOf(serviceId)));
        }

        EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = envoyHealthCheckService.getHealthCheckRule(serviceId, gwId);
        Map<String, Object> result = new HashMap<>(1);
        if (envoyHealthCheckRuleDto != null) {
            result.put("HealthCheckRule", envoyHealthCheckRuleDto);
        } else {
            result.put("HealthCheckRule", null);
        }
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

    @GetMapping(params = {"Action=DescribeServiceInstanceList"})
    public String describeServiceInstanceList(@RequestParam(value = "ServiceId") long serviceId,
                                              @RequestParam(value = "GwId") long gwId) {
        logger.info("查询服务实例详情, serviceId:{}, gwId:{}", serviceId, gwId);
        if (!gatewayInfoService.isGwExists(gwId)) {
            return apiReturn(CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId)));
        }
        if (!serviceInfoService.isServiceExists(serviceId)) {
            return apiReturn(CommonErrorCode.InvalidParameterServiceId(String.valueOf(serviceId)));
        }

        List<EnvoyServiceInstanceDto> envoyServiceInstanceDtoList = envoyHealthCheckService.getServiceInstanceList(serviceInfoService.getServiceByServiceId(serviceId),
                gatewayInfoService.get(gwId));
        Map<String, Object> result = new HashMap<>(1);
        result.put("InstanceList", envoyServiceInstanceDtoList);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }

}
