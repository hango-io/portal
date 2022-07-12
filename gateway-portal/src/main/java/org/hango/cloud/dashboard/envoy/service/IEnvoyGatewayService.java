package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginManagerDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostUpdateDto;
import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderItemDto;

import java.util.List;
import java.util.Set;

/**
 * Envoy网关Service层接口
 *
 * @author hzchenzhongyang 2020-01-08
 */
public interface IEnvoyGatewayService {

    /**
     * 创建virtual host list时的参数校验
     *
     * @param gwId   网关id
     * @param vhList virtual host 列表
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkVirtualHostList(long gwId, List<EnvoyVirtualHostInfo> vhList);

    /**
     * 创建virtual host时的参数校验
     *
     * @param vhInfo virtual host 信息
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkCreateVirtualHost(EnvoyVirtualHostInfo vhInfo);

    /**
     * 创建virtual host
     *
     * @param vhInfo virtual host 信息
     * @return 创建结果 true：成功  false：失败
     */
    boolean createVirtualHost(EnvoyVirtualHostInfo vhInfo);

    /**
     * 根据网关id查询virtual host列表
     *
     * @param gwId 网关id
     * @return {@link List<EnvoyVirtualHostInfo> } virtual host列表
     */
    List<EnvoyVirtualHostInfo> getVirtualHostListByGwId(long gwId);

    /**
     * 批量更新virtual host
     *
     * @param gwId   网关id
     * @param vhList virtual host列表
     * @return 更新结果 true: 更新成功； false: 更新失败
     */
    boolean updateVirtualHostList(long gwId, List<EnvoyVirtualHostInfo> vhList);

    /**
     * 根据网关id、项目id查询virtual host信息
     *
     * @param gwId      网关id
     * @param projectId 项目id
     * @return virtual host信息
     */
    EnvoyVirtualHostInfo getVirtualHostByGwIdAndProjectId(long gwId, long projectId);

    /**
     * 更新vh时参数校验
     *
     * @param virtualHostId vh id
     * @param hostList      更新后域名列表
     * @return {@link ErrorCodeEnum} 参数校验结果，当校验通过时返回 ErrorCodeEnum.Success
     */
    ErrorCode checkUpdateVirtualHost(long virtualHostId, List<String> hostList, String bindType, List<Long> projectList);

    /**
     * 更新vh的域名
     *
     * @param envoyVirtualHostUpdateDto updateVirtualDto
     * @return 更新结果 true：成功  false：失败
     */
    boolean updateVirtualHost(EnvoyVirtualHostUpdateDto envoyVirtualHostUpdateDto);


    /**
     * 查询满足条件的 vh 列表
     *
     * @param gwId          网关id，非0时gwId将作为查询条件
     * @param projectIdList 项目id列表，非空时projectIdList将作为查询条件
     * @param domain        域名，支持模糊查询
     * @return {@link List<EnvoyVirtualHostInfo>} vh 列表
     */
    List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, List<Long> projectIdList, String domain);

    /**
     * 根据id查询 virtual host信息
     *
     * @param virtualHostId vh id
     * @return {@link EnvoyVirtualHostInfo} vh信息
     */
    EnvoyVirtualHostInfo getVirtualHost(long virtualHostId);

    /**
     * 取消关联项目时参数校验
     *
     * @param virtualHostId vh id
     */
    ErrorCode checkDeleteVirtualHost(long virtualHostId);

    /**
     * 删除virtual host
     *
     * @param virtualHostId vh id
     * @return 删除结果 true：成功；false：失败
     */
    ErrorCode deleteVirtualHost(long virtualHostId);

    /**
     * 通过ProjectId获取绑定为project类型的VirtualHost
     *
     * @param projectId 项目ID
     * @return VirtualHost集合
     */
    List<EnvoyVirtualHostInfo> getProjectTypeVhByProjectId(Long projectId);

    Set<Long> getHostTypeVhProject(long gwId);

    /**
     * 获取网关插件配置信息
     *
     * @param gatewayInfo
     * @return
     */
    List<EnvoyPluginManagerDto> getEnvoyPluginManager(GatewayInfo gatewayInfo);

    /**
     * 获取网关插件配置信息
     *
     * @param apiPlaneAddr
     * @param gwClusterName
     * @return
     */
    List<PluginOrderItemDto> getEnvoyPluginManager(String apiPlaneAddr, String gwClusterName);

    /**
     * 校验网关修改配置参数
     *
     * @param gatewayInfo
     * @param name
     * @param enable
     * @return
     */
    ErrorCode checkEnvoyPluginManager(GatewayInfo gatewayInfo, String name, boolean enable);

    /**
     * 更新网关插件配置
     *
     * @param gatewayInfo
     * @param name
     * @param enable
     * @return
     */
    boolean updateEnvoyPluginManager(GatewayInfo gatewayInfo, String name, boolean enable);

    /**
     * 插件配置更新到APIPlane
     *
     * @param gatewayInfo
     * @param envoyPluginManager
     * @return
     */
    Boolean publishPluginToAPIPlane(GatewayInfo gatewayInfo, List<PluginOrderItemDto> envoyPluginManager);


    /**
     * @param listenerPort       网关端口号
     * @param apiPlaneAddr       apiPlane地址
     * @param gwClusterName      gw集群名称
     * @param protoDescriptorBin pb文件
     * @param services           要支持协议转换的服务列表
     * @return 是否发布成功
     */
    boolean publishGrpcEnvoyFilterToAPIPlane(int listenerPort, String apiPlaneAddr, String gwClusterName, String protoDescriptorBin, List<String> services);
}
