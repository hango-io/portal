package org.hango.cloud.common.infra.domain.dao;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.meta.DomainInfoQuery;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/25
 */
public interface IDomainInfoDao extends ICommonDao<DomainInfo> {

    /**
     * 分页查询域名信息
     */
    Page<DomainInfo> getDomainInfoPage(DomainInfoQuery domainInfoQuery);


    /**
     * 查询域名列表
     */
    List<DomainInfo> getDomainInfoList(DomainInfoQuery domainInfoQuery);
}
