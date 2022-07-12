package org.hango.cloud.dashboard.apiserver.dao;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:25.
 */
public interface GatewayInfoDao extends IBaseDao<GatewayInfo> {

    void delete(long id);

    List<GatewayInfo> get(String gwName);

    List<GatewayInfo> getGatewayInfoByLimit(String pattern, long offset, long limit);

    List<GatewayInfo> getGatewayInfoByProjectIdAndLimit(String pattern, long offset, long limit, long projectId);

    List<GatewayInfo> getGatewayInfoByProjectId(String pattern, long projectId);

    long getGatewayInfoCountsByPattern(String pattern);

    /**
     * 根据gwName查询满足条件网关id列表
     *
     * @param gwName    网关名称，支持模糊查询
     * @param projectId 项目id
     * @return {@link List<Long>} 满足条件的网关id列表
     */
    List<Long> getGwIdListByNameFuzzy(String gwName, long projectId);

    /**
     * 根据网关id列表查询网关信息列表
     *
     * @param gwIdList 网关id列表
     * @return {@link List<GatewayInfo>} 网关信息列表
     */
    List<GatewayInfo> getGatewayInfoList(List<Long> gwIdList);

}
