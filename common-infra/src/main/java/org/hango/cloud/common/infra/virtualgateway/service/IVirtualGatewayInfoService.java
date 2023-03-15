package org.hango.cloud.common.infra.virtualgateway.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:26.
 */
public interface IVirtualGatewayInfoService extends CommonService<VirtualGateway, VirtualGatewayDto> {


    /**
     * 通过gwId查询虚拟网关信息
     *
     * @param gwId 网关Id
     * @return {@link VirtualGatewayDto} 虚拟网关信息
     */
    List<VirtualGatewayDto> getGatewayInfoByGwId(long gwId);

    /**
     * 通过gwId和虚拟网关类型查询列表信息
     *
     */
    List<VirtualGatewayDto> getGatewayList(long gwId, String type);

    /**
     * 根据路由id查询已发布服务所发布的网关
     *
     */
    List<VirtualGatewayDto> getPublishedServiceGateway(Long routeId);

    /**
     * 通过名称查询虚拟网关信息
     *
     * @param name
     * @return
     */
    VirtualGatewayDto getByName(String name);

    /**
     * 通过标识查询虚拟网关信息
     *
     * @param code
     * @return
     */
    VirtualGatewayDto getByCode(String code);

    /**
     * 通过端口查询虚拟网关信息
     *
     * @param port
     * @return
     */
    VirtualGatewayDto getByPort(int port);

    /**
     * 通过网关ID判断网关是否存在
     *
     * @param virtualGwId
     * @return
     */
    boolean isGwExists(long virtualGwId);


    /**
     * 根据网关id列表查询网关信息列表
     *
     * @param gwIdList 网关id列表
     * @return {@link List< VirtualGateway >} 网关信息列表
     */
    List<VirtualGatewayDto> getGatewayInfoList(List<Long> gwIdList);


    /**
     * 根据条件获取网关列表
     *
     * @param query
     * @return 网关列表
     */
    List<VirtualGatewayDto> getVirtualGatewayListByConditions(QueryVirtualGatewayDto query);


    /**
     * 根据条件获取网关列表
     *
     * @param query
     * @return 网关列表
     */
    Integer countVirtualGatewayByConditions(QueryVirtualGatewayDto query);


    /**
     * 根据gwName查询满足条件网关id列表
     *
     * @param gwName    网关名称，支持模糊查询
     * @param projectId 项目id
     * @return {@link List<Long>} 满足条件的网关id列表
     */
    List<Long> getGwIdListByNameFuzzy(String gwName, long projectId);


    /**
     * 根据项目id查询所属项目下的网关实例
     *
     * @param projectId
     * @return
     */
    List<VirtualGatewayDto> getGwEnvByProjectId(Long projectId);


    /**
     * 根据项目id和环境查询所属项目下的网关实例
     */
    List<VirtualGatewayDto> getManagedVirtualGatewayList(Long projectId, String env, String protocol);


    /**
     * 判断指定项目和环境是否存在虚拟网关
     */
    Boolean existManagedVirtualGateway(Long projectId, String env, String protocol);

    /**
     * 只创建DB数据，不进行hooker
     */
    long createWithoutHooker(VirtualGatewayDto virtualGatewayDto);

    /**
     * 只更新DB数据，不进行hooker
     */
    long updateWithoutHooker(VirtualGatewayDto virtualGatewayDto);

    /**
     * 只删除DB数据，不进行hooker
     */
    void deleteWithoutHooker(VirtualGatewayDto virtualGatewayDto);

    ErrorCode checkDeleteVirtualGatewayParamFromHango(VirtualGatewayDto virtualGatewayDto);
}

