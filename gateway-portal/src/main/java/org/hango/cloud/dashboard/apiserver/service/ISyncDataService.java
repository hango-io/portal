package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.MicroServiceDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.SyncServiceInfo;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/23
 */
public interface ISyncDataService {


    /**
     * 从元数据服务同步服务信息
     *
     * @param syncList
     * @return
     */
    void syncServiceInfo(List<SyncServiceInfo> syncList);

    /**
     * 根据项目ID从元数据服务获取服列表
     *
     * @param projectId
     * @return
     */
    List<MicroServiceDto> getServiceInfoFromMetaService(Long projectId);


    /**
     * 获取同步数据列表（其中包含不需同步的数据，调用时应该根据PreSyncStatus判断）
     *
     * @param projectId
     * @return
     */
    List<SyncServiceInfo> getSyncListByProjectId(Long projectId);


    /**
     * 获取同步数据统计信息 KEY : 统计粒度(preSyncStatus) Value : 统计数
     *
     * @param syncServiceInfoList
     * @return
     */
    Map<String, List<String>> getServiceSyncStatistics(List<SyncServiceInfo> syncServiceInfoList);

    /**
     * 同步API信息
     *
     * @param projectId
     * @return 返回API同步总数
     */
    Integer syncApiInfo(Long projectId);

    /**
     * 同步API信息
     *
     * @param serviceInfoList
     * @return 返回API同步总数
     */
    Integer syncApiInfo(List<? extends ServiceInfo> serviceInfoList);
}
