package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.TrafficMarkInfo;

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
     * @param colorTag 流量染色标识
     * @param offset   偏移
     * @param limit    每页的条数
     * @return 流量染色规则列表
     */
    List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long offset, long limit);

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
}
