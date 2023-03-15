package org.hango.cloud.common.infra.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainRefreshResult;
import org.hango.cloud.common.infra.domain.pojo.DomainInfoPO;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2022/10/26
 */
public interface IDomainInfoService extends CommonService<DomainInfoPO, DomainInfoDTO> {

    Page<DomainInfoPO> getDomainInfoPage(long projectId, String host, int offset, int limit);



    List<DomainInfoDTO> getDomainInfos(List<Long> projectIds, String protocol, String env);


    /**
     * 查询只用于关联的域名信息
     */
    List<DomainInfoDTO> getRelevanceOnlyDomainInfos(long virtualGatewayId);


    /**
     * 获取虚拟网关下的域名
     */
    List<String> getHosts(long projectId, long virtualGatewayId);


    /**
     * 获取虚拟网关下的有效域名，排除待下线域名，用于配置刷新
     */
    List<String> getEnableHosts(long projectId, long virtualGatewayId);


    /**
     * 获取项目下待刷新的配置
     */
    List<DomainRefreshResult> getDomainRefreshResult(long projectId);


    /**
     * 刷新域名配置
     * @param projectId 项目id
     * @return 刷新结果
     */
    List<DomainRefreshResult> refreshDomain(long projectId);


    /**
     * 更新域名
     */

    void createDomainInfoByVgId(long projectId,List<String> virtualHostList,long virtualGatewayId);

    //hango使用
    void updateDomainInfoByVg(long projectId,List<String> virtualHostList,long virtualGatewayId);
    //hango使用
    long deleteDomainInfoByVgId(long projectId,long virtualGatewayId);

}
