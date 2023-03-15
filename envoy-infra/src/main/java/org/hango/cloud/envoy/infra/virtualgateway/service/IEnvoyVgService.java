package org.hango.cloud.envoy.infra.virtualgateway.service;

import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayBindDto;
import org.hango.cloud.common.infra.virtualgateway.hooker.AbstractVgBindHooker;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/7
 */
public interface IEnvoyVgService {

    /**
     * 发布虚拟网关配置到数据面
     * 当且仅当执行虚拟网关关联项目操作时，虚拟网关配置才会下发到数据面
     *
     * @param virtualGatewayBindDto
     * @return
     * @see AbstractVgBindHooker 使用该Hooker进行增强
     * @see org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService#bindProject(VirtualGatewayBindDto)
     */
    boolean publishToGateway(VirtualGatewayBindDto virtualGatewayBindDto);


    /**
     * 从数据面下线虚拟网关配置
     * 当且仅当执行虚拟网关项目操作时，且解绑最后一个项目时，虚拟网关配置才会下发到数据面，进行删除
     *
     * @param virtualGwId
     * @param projectId
     * @return
     * @see AbstractVgBindHooker 使用该Hooker进行增强
     * @see org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService#unbindProject(long, long) (VirtualGatewayBindDto)
     */
    boolean offlineToGateway(long virtualGwId, long projectId);


    /**
     * 刷新项目下的虚拟网关配置
     *
     * @param domainInfoDTO
     * @return
     */
    boolean refreshToGateway(DomainInfoDTO domainInfoDTO);



}
