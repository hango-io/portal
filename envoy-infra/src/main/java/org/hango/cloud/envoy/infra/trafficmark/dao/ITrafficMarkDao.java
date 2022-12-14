package org.hango.cloud.envoy.infra.trafficmark.dao;



import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;

import java.util.List;

/**
 * 流量染色控制台dao层接口
 *
 * @author qilu01
 */
public interface ITrafficMarkDao extends IBaseDao<TrafficMarkInfo> {

    /**
     * 获取染色标识下的所有流量染色规则
     *
     * @param colorTag  流量染色标识
     * @param projectId 项目id
     * @param sortKey   排序key
     * @param sortValue 排序方式
     * @param offset    偏移
     * @param limit     每页的条数
     * @return 流量染色规则列表
     */

    List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long projectId, String sortKey, String sortValue, long offset, long limit);

    /**
     * 根据id删除流量染色规则
     *
     * @param trafficColorRuleId
     */
    void delete(long trafficColorRuleId);

    /**
     * 获取流量染色规则数量
     *
     * @return
     */
    long getCount();

    List<TrafficMarkInfo> getTrafficColorRulesByRouteRuleId(long routeRuleId,String colorTag);
}
