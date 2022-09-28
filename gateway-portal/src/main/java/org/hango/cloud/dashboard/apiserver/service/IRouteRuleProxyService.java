package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;

import java.util.List;

/**
 * 路由规则管理Service层接口
 *
 * @author hzchenzhongyang 2019-09-18
 */
public interface IRouteRuleProxyService {
    /**
     * 发布路由规则时的参数校验..
     * 路由规则发布不需要选择服务，相当于后端destination直接关联路由规则所属服务
     *
     * @param routeRuleProxyDto 路由规则发布Dto
     * @return @return {@link ErrorCodeEnum#Success} ErrorCodeEnum.Success 即参数校验成功，否则参数校验失败并返回对应的错误码
     */
    ErrorCode checkPublishParam(RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 路由发布更新参数校验
     *
     * @param routeRuleProxyDto 路由发布更新Dto
     * @return 参数校验结果
     */
    ErrorCode checkUpdateParam(RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 查询是否具有完全相同的路由规则，如果完全相同，不允许修改
     *
     * @param routeRuleProxyInfo 已发布路由匹配规则信息
     * @return 是否存在
     */
    boolean isSameRouteRuleProxyInfo(RouteRuleProxyInfo routeRuleProxyInfo);

    /**
     * 发布路由规则到指定网关，包含：（1）调用api-plane接口发布路由规则；（2）记录已发布路由规则到数据库
     *
     * @param routeRuleProxyInfo   路由规则发布信息
     * @param pluginConfigurations 插件配置列表
     * @param updateHosts          是否更新hosts
     * @return 生成的已发布信息的自增id，发布失败时返回-1
     */
    long publishRouteRule(RouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations, boolean updateHosts);

    /**
     * 发布路由至多网关
     *
     * @param gwIds             网关id
     * @param routeRuleProxyDto 路由规则ProxyInfo
     * @return 发布失败的网关名称 列表
     */
    List<String> publishRouteRuleBatch(List<Long> gwIds, RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 将路由规则发布信息写入数据库
     *
     * @param routeRuleProxyInfo 路由规则发布信息
     * @return 生成的已发布信息的自增id
     */
    long addRouteRuleProxy(RouteRuleProxyInfo routeRuleProxyInfo);

    /**
     * 分页查询已发布路由规则列表
     *
     * @param gwId      网关id，若为0则不加入查询条件
     * @param serviceId 服务id，若为0则不加入查询条件
     * @param pattern   路由pattern
     * @param sortKey   路由规则查询sortKey
     * @param sortValue 路由规则查询sortValue
     * @param offset    分页查询参数offset
     * @param limit     分页查询参数limit
     * @return {@link List< RouteRuleProxyInfo >} 已发布路由规则列表
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, String pattern, String sortKey,
                                                   String sortValue, long offset, long limit);

    /**
     * 查询已发布路由规则列表
     *
     * @param serviceId 服务id，若为0则不加入查询条件
     * @return {@link List< RouteRuleProxyInfo >} 已发布路由规则列表
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyList(long serviceId);

    /**
     * 通过projectId查询已发布路由集合
     *
     * @param projectId 项目ID
     * @param gwId      网关ID
     * @return 已发布路由集合
     */
    List<RouteRuleProxyInfo> getRouteListByProjectIdAndGwId(long projectId, long gwId);


    /**
     * 查询已发布路由规则数量
     *
     * @param gwId      网关id，若为0则查询所有网关
     * @param serviceId 服务id，若为0则查询所有服务
     * @return 已发布路由规则数量
     */
    long getRouteRuleProxyCountByService(long gwId, long serviceId);

    long getRouteRuleProxyCount(long gwId, long serviceId, String pattern);

    long getAuthRouteCount(long gwId, long serviceId, long routeId, boolean auth);

    /**
     * 查询已发布路由规则数量
     *
     * @param gwId        网关id，若为0则不加入查询条件
     * @param routeRuleId 路由规则id，若为0则不加入查询条件
     * @return 已发布路由规则数量
     */
    long getRouteRuleProxyCount(long gwId, long routeRuleId);

    /**
     * 查询已发布路由规则
     *
     * @param gwId        网关id，若为0则不加入查询条件
     * @param routeRuleId 路由规则id，若为0则不加入查询条件
     * @return 已发布路由规则
     */
    RouteRuleProxyInfo getRouteRuleProxy(long gwId, long routeRuleId);

    /**
     * 下线已发布路由规则
     *
     * @param gwId        网关id
     * @param routeRuleId 路由规则id
     * @return 下线结果，true表示下线成功，false表示下线失败
     */
    boolean deleteRouteRuleProxy(long gwId, long routeRuleId);

    /**
     * 下线已发布路由规则参数校验
     *
     * @param gwId        网关id
     * @param routeRuleId 路由规则id
     * @param serviceIds  下线路由规则所关联的服务id
     * @return {@link ErrorCode}
     */
    ErrorCode checkDeleteRouteRuleProxy(long gwId, long routeRuleId, List<Long> serviceIds);

    /**
     * 路由使能状态参数校验
     *
     * @param gwId        网关id
     * @param routeRuleId 路由id
     * @param enableState 使能状态，enable,disable
     * @return 参数校验结果
     */
    ErrorCode checkUpdateEnableState(long gwId, long routeRuleId, String enableState);

    /**
     * 路由使能状态更新
     *
     * @param gwId        网关id
     * @param routeRuleId 路由id
     * @param enableState 使能状态，enable，disable
     * @return 更新结果
     */
    long updateEnableState(long gwId, long routeRuleId, String enableState);

    /**
     * 更新路由规则
     *
     * @param proxyInfo 更新的路由规则
     * @return 更新结果
     */
    long updateEnvoyRouteRuleProxy(RouteRuleProxyInfo proxyInfo);


    /**
     * 查询路由规则发布具体信息
     *
     * @param id 路由规则发布主键id
     * @return {@link RouteRuleProxyInfo} 路由规则发布详细信息
     */
    RouteRuleProxyInfo getRouteRuleProxy(long id);

    /**
     * 通过路由规则id查询路由规则发布情况
     *
     * @param routeRuleId 路由规则id
     * @return 路由规则发布信息
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyByRouteRuleId(long routeRuleId);

    /**
     * 通过EnvoyRouteRuleProxyInfo 构造EnvoyRoyteRuleProxyDto
     *
     * @param proxyInfo EnvoyRouteRuleProxyInfo
     * @return {@link RouteRuleProxyDto}
     */
    RouteRuleProxyDto fromMeta(RouteRuleProxyInfo proxyInfo);


    RouteRuleProxyInfo toMeta(RouteRuleProxyDto proxyDto);


    /**
     * 查询某个服务下的已发布路由规则列表
     *
     * @param gwId      网关id
     * @param serviceId 服务id，若为0则查询所有服务
     * @return 已发布路由规则
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyListByServiceId(long gwId, long serviceId);

    List<RouteRuleProxyInfo> getRouteRuleProxyListByGwId(long gwId);

    /**
     * 校验流量镜像参数
     *
     * @param routeRuleProxyDto 要发布流量镜像的路由
     * @return 校验结果
     */
    ErrorCode checkPublishMirror(RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 发布流量镜像
     *
     * @param routeProxyDto 要发布流量镜像的路由
     * @return 发布结果
     */
    long publishRouteMirror(RouteRuleProxyDto routeProxyDto);

    /**
     * 根据流量镜像指向的服务id和网关id寻找已发布路由
     *
     * @param mirrorServiceId 流量镜像指向的服务id
     * @param gwId            网关id
     * @return 已发布的路由规则
     */
    List<RouteRuleProxyInfo> getRouteRuleProxyListByMirrorServiceIdAndGwId(long mirrorServiceId, long gwId);

}
