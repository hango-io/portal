package org.hango.cloud.envoy.infra.healthcheck.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.envoy.infra.healthcheck.dto.EnvoyServiceInstanceDto;

import java.util.List;

/**
 * 健康检查Service
 *
 * @author TC_WANG
 * @date 2019/11/19 下午3:29.
 */
public interface IEnvoyHealthCheckService {

    /**
     * 更新健康检查规则，如果不存在，则新建
     *
     * @param healthCheckRuleDto
     * @return
     */
    ErrorCode updateHealthCheckRuleParam(HealthCheckRuleDto healthCheckRuleDto);

    /**
     * 查询健康检查规则
     *
     * @param serviceId
     * @return
     */
    HealthCheckRuleDto getHealthCheckRule(long serviceId);


    /**
     * 查询服务实例详情
     *
     * @param serviceProxy
     * @return
     */
    List<EnvoyServiceInstanceDto> getServiceInstanceList(ServiceProxyDto serviceProxy);

    /**
     * 删除服务的健康检查配置
     *
     * @param serviceId
     */
    void deleteByServiceId(long serviceId);

    /**
     * 更新健康检查规则时进行参数校验
     *
     * @param dto
     * @return
     */
    ErrorCode checkUpdateHealthCheckRuleParam(HealthCheckRuleDto dto);


}
