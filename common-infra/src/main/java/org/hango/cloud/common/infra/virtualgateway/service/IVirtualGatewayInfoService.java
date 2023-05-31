package org.hango.cloud.common.infra.virtualgateway.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:26.
 */
public interface IVirtualGatewayInfoService extends CommonService<VirtualGateway, VirtualGatewayDto> {


    /**
     * 通过gwId和虚拟网关类型查询列表信息
     */
    List<VirtualGatewayDto> getKubernetesGatewayList(long gwId);


    /**
     * 根据网关id列表查询网关信息列表
     *
     * @param gwIdList 网关id列表
     * @return {@link List< VirtualGateway >} 网关信息列表
     */
    List<VirtualGatewayDto> getVirtualGatewayList(List<Long> gwIdList);

    /**
     * 根据网关id列表查询网关信息
     */
    List<VirtualGatewayDto> getByIds(List<Long> ids);


    /**
     * 分页查询网关列表
     *
     * @param query
     * @return 网关列表
     */
    Page<VirtualGatewayDto> getVirtualGatewayPage(QueryVirtualGatewayDto query);


    /**
     * 列表查询网关列表
     *
     * @param query
     * @return 网关列表
     */
    List<VirtualGatewayDto> getVirtualGatewayList(QueryVirtualGatewayDto query);


    /**
     * 是否存在虚拟网关
     */
    Boolean exist(VirtualGatewayQuery query);


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

    /**
     * 更新虚拟网关高级配置
     */
    void updateGatewaySetting(GatewaySettingDTO gatewaySettingDTO);


    /**
     * 查询虚拟网关配置
     */
    GatewaySettingDTO getGatewaySetting(Long id);
}



