package org.hango.cloud.envoy.infra.healthcheck.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.healthcheck.dto.HealthCheckRuleDto;
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
     * @return
     */
    ErrorCode updateHealthCheckRuleParam(HealthCheckRuleDto healthCheckRuleDto);

    /**
     * 当服务下线时，删除健康检查功能（不用调用apiplane接口，因为服务下线会删除dr）
     *
     * @param serviceId
     * @param virtualGwId
     */
    void deleteHealthCheck(long serviceId, long virtualGwId);

    /**
     * 查询健康检查规则
     *
     * @param serviceId
     * @param virtualGwId
     * @return
     */
    HealthCheckRuleDto getHealthCheckRule(long serviceId, long virtualGwId);


    /**
     * 查询服务实例详情
     *
     * @return
     */
    List<EnvoyServiceInstanceDto> getServiceInstanceList(long serviceId, long virtualGwId);


    /**
     * 更新健康检查规则时进行参数校验
     *
     * @param dto
     * @return
     */
    ErrorCode checkUpdateHealthCheckRuleParam(HealthCheckRuleDto dto);


}
