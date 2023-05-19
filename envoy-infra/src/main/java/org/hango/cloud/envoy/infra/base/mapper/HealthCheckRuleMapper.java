package org.hango.cloud.envoy.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.envoy.infra.healthcheck.pojo.HealthCheckRulePO;

/**
 * @Author zhufengwei
 * @Date 2023/1/4
 */
@Mapper
public interface HealthCheckRuleMapper extends BaseMapper<HealthCheckRulePO> {
}
