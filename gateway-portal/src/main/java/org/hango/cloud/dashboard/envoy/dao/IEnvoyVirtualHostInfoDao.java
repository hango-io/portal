package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;

import java.util.List;

/**
 * vh dao层接口
 *
 * @author hzchenzhongyang 2020-01-08
 */
public interface IEnvoyVirtualHostInfoDao extends IBaseDao<EnvoyVirtualHostInfo> {

    /**
     * 查询满足条件的 vh 列表
     *
     * @param gwId          网关id，非0时gwId将作为查询条件
     * @param projectIdList 项目id列表，非空时projectIdList将作为查询条件
     * @param domain        域名，支持模糊查询
     * @return {@link List <EnvoyVirtualHostInfo>} vh 列表
     */
    List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, List<Long> projectIdList, String domain, String bindType);

    List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, String bindType);

    EnvoyVirtualHostInfo getVirtualHostInfo(long gwId, long projectId);

}
