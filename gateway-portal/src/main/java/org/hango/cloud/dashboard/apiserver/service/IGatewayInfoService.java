package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.PluginInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:26.
 */
public interface IGatewayInfoService {

    /**
     * 根据Id获取某个环境信息
     *
     * @param id
     * @return
     */
    GatewayInfo get(long id);

    /**
     * 通过网关名称获取网关
     *
     * @param gwName
     * @return
     */
    GatewayInfo getGatewayByName(String gwName);

    /**
     * 用于envoy网关，通过gwClusterName查询网关信息
     *
     * @param gwClusterName 网关集群名称
     * @return {@link GatewayInfo} 网关信息
     */
    GatewayInfo getGatewayInfoByGwClusterName(String gwClusterName);

    /**
     * 更新环境信息
     *
     * @param gatewayInfo
     * @param updateProjectId Envoy网关是否更新project_id字段
     * @return
     */
    boolean updateGwInfo(GatewayInfo gatewayInfo, boolean updateProjectId);

    boolean update(GatewayInfo gatewayInfo);

    /**
     * 健康检查更新环境信息
     *
     * @param gatewayInfo
     * @return
     */
    boolean updateGwInfoForHealth(GatewayInfo gatewayInfo);


    boolean delete(long id, List<PluginInfo> pluginInfoList);

    /**
     * 获取所有网关
     *
     * @return
     */
    List<GatewayInfo> findAll();

    /**
     * 分页获取所有网关
     *
     * @param pattern 支持根据网关名称进行模糊匹配
     * @param offset
     * @param limit
     * @return
     */
    List<GatewayInfo> findGatewayByLimit(String pattern, long offset, long limit);

    /**
     * 分页获取指定projectId下的所有网关
     *
     * @param pattern   支持模糊匹配，如果pattern为null，则不进行模糊匹配
     * @param offset
     * @param limit
     * @param projectId
     * @return
     */
    List<GatewayInfo> findGatwayByProjectIdAndLimit(String pattern, long offset, long limit, long projectId);

    /**
     * 获取网关数量
     *
     * @return
     */
    long getGatewayCount(String pattern);

    /**
     * 获取项目id下的网关数量
     *
     * @param pattern   支持根据网关名称进行模糊匹配
     * @param projectId
     * @return
     */
    long getGatewayCountByProjectId(String pattern, long projectId);

    boolean isExistGwInstance(String gwName);

    boolean isGwExists(long gwId);

    ErrorCode checkGwIdParam(String gwId);

    List<GatewayInfo> getGwEnvByProjectId(Long projectId);

    long addGatewayByMetaDto(GatewayDto gatewayDto);

//    GatewayInfo updateGatewayInfo(GatewayInfo gatewayInfo,GatewayDto gatewayInfoMetaDto);

    List<GatewayInfo> getGwByEnvId(String envId);

    GatewayInfo getGwByUniId(String gwUniId);

    /**
     * 创建网关参数校验
     *
     * @param gatewayDto 创建网关dto
     * @return 返回校验结果，{@link ErrorCode # Success}
     */
    ErrorCode checkAddParam(GatewayDto gatewayDto);

    ErrorCode checkCommonDataParam(GatewayDto gatewayDto);

    /**
     * 更新网关参数校验
     *
     * @param gatewayDto 网关dto
     * @return 返回校验结果，{@link ErrorCode # Success}
     */
    ErrorCode checkUpdateParam(GatewayDto gatewayDto);

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

    /**
     * 对“与projectID关联的网关集合”进一步划分，划分当前projectID关联的其他网关，不包含其所属网关
     *
     * @param gatewayList 与projectID关联的网关集合
     * @param projectId   项目ID
     * @param gwId        网关
     * @return 网关关系Map
     */
    Map<String, List<GatewayInfo>> distinguishGatewayRelationshipByProjectIdAndGwId(List<GatewayInfo> gatewayList, Long projectId, Long gwId);

    /**
     * 根据条件获取网关列表
     *
     * @param pattern   模糊匹配
     * @param offset    分页offset
     * @param limit     每页数量
     * @param tenantId  租户id
     * @param projectId 项目id
     * @return 网关列表
     */
    List<GatewayInfo> getGatewayListByConditions(String pattern, long offset, long limit, long tenantId, long projectId);

    /**
     * 根据项目idlist查询网关
     *
     * @param pattern    模糊匹配
     * @param projectIds 项目idlist
     * @return 网关列表
     */
    List<GatewayInfo> getGatewayByProjectIds(String pattern, List<Long> projectIds);
}

