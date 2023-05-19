package org.hango.cloud.envoy.infra.virtualgateway.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainBindDTO;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
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
     * 绑定域名
     */
    boolean bindDomain(DomainBindDTO domainBindDTO);

    /**
     * 解绑域名
     */
    boolean unBindDomain(DomainBindDTO domainBindDTO);

    /**
     * 发布虚拟网关配置到数据面
     */
    boolean publishToGateway(VirtualGatewayDto virtualGatewayDto);


    /**
     * 从数据面下线虚拟网关配置
     * 当且仅当执行虚拟网关项目操作时，且解绑最后一个项目时，虚拟网关配置才会下发到数据面，进行删除
     *
     * @param virtualGwId
     * @see AbstractVgBindHooker 使用该Hooker进行增强
     * @see org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService#unbindProject(long, long) (VirtualGatewayBindDto)
     */
    boolean offlineToGateway(long virtualGwId);


    /**
     * 刷新项目下的虚拟网关配置
     *
     * @param domainInfoDTO
     * @return
     */
    boolean refreshToGateway(DomainInfoDTO domainInfoDTO);


    /**
     * 发布虚拟网关高级配置
     */
    ErrorCode updateEnvoyGatewaySetting(GatewaySettingDTO gatewaySettingDto);


    /**
     * 获取虚拟网关高级配置
     */
    GatewaySettingDTO getEnvoyGatewaySetting(Long virtualGwId);


    /**
     * 下线自定义ip配置
     */
    boolean deleteIpSource(Long vgId);

}
