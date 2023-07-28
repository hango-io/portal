package org.hango.cloud.common.infra.gateway.service;

import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.meta.Gateway;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
public interface IGatewayService extends CommonService<Gateway, GatewayDto> {


    /**
     * 通过网关名称获取网关信息条数
     *
     * @param name 网关名称
     * @return
     */
    int countByName(String name);

    /**
     * 通过网关名称分页获取网关信息
     *
     * @param name   网关名称
     * @param offset
     * @param limit
     * @return
     */
    List<GatewayDto> findByName(String name, long offset, long limit);


    /**
     * 通过网关clusterName获取网关信息
     *
     * @param clusterName 网关clusterName
     * @return
     */
    GatewayDto getByClusterName(String clusterName);

}