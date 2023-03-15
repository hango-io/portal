package org.hango.cloud.envoy.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.hango.cloud.envoy.infra.healthcheck.pojo.HealthCheckRulePO;
import org.springframework.stereotype.Repository;

/**
 * @Author zhufengwei
 * @Date 2023/1/4
 */
@Repository
public interface HealthCheckRuleMapper extends BaseMapper<HealthCheckRulePO> {
}
