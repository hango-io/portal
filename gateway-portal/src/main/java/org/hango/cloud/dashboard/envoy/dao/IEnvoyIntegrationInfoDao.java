package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;

import java.util.List;
import java.util.Map;

/**
 * 集成Dao层接口
 */
public interface IEnvoyIntegrationInfoDao extends IBaseDao<EnvoyIntegrationInfo> {

    /**
     * 根据项目ID和模糊查询查询集成数量
     *
     * @param projectId 项目id
     * @param pattern   模糊查询参数
     * @param type      集成类型
     * @return 返回符合要求的集成数量
     */
    public long getIntegrationInfoCount(long projectId, String pattern, String type);

    /**
     * 根据项目ID和模糊查询查询集成数量
     *
     * @param projectId 项目id
     * @param pattern   模糊查询参数
     * @return 返回符合要求的集成数量
     */
    public long getIntegrationInfoCount(long projectId, String pattern);

    /**
     * 条件查询集成列表
     *
     * @param projectId 项目id
     * @param pattern   模糊查询参数
     * @param offset    位移
     * @param limit     每页的限制
     * @param type      集成类型
     * @return 符合条件的集成列表
     */
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByLimit(long projectId, String pattern, long offset, long limit, String type);

    /**
     * 条件查询集成列表
     *
     * @param projectId 项目id
     * @param pattern   模糊查询参数
     * @param offset    位移
     * @param limit     每页的限制
     * @return 符合条件的集成列表
     */
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByLimit(long projectId, String pattern, long offset, long limit);

    /**
     * 根据id列表返回集成列表
     *
     * @param idMap
     * @return
     */
    public List<EnvoyIntegrationInfo> getByIdlist(Map<String, Long> idMap);
}
