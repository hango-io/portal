package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;

/**
 * 健康检查DAO
 *
 * @author TC_WANG
 * @date 2019/11/19 下午4:23.
 */
public interface EnvoyHealthCheckRuleDao extends IBaseDao<EnvoyHealthCheckRuleInfo> {

    /**
     * 删除服务对应的健康检查规则
     *
     * @param serviceId
     */
    int deleteByServiceId(long serviceId);
}
