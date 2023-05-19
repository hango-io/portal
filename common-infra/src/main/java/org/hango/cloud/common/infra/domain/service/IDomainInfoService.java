package org.hango.cloud.common.infra.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainQueryDTO;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/26
 */
public interface IDomainInfoService extends CommonService<DomainInfo, DomainInfoDTO> {

    /**
     * 列表查询域名
     */
    List<DomainInfoDTO> getDomainInfoList(DomainQueryDTO queryDTO);


    /**
     * 分页查询域名
     */
    Page<DomainInfo> getDomainInfoPage(DomainQueryDTO queryDTO);


    /**
     * 获取虚拟网关下的域名
     */
    List<String> getHosts(long projectId, long virtualGatewayId);


    List<DomainInfoDTO> getDomainInfos(List<Long> ids);


    List<DomainInfoDTO> getBindDomainInfoList(DomainQueryDTO queryDTO);

}
