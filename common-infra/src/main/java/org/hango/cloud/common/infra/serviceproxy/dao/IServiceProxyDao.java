package org.hango.cloud.common.infra.serviceproxy.dao;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/25
 */
public interface IServiceProxyDao extends ICommonDao<ServiceProxyInfo> {


    /**
     * 批量查询已发布服务
     *
     * @param query 查询条件
     * @return 服务发布数据
     */
    Page<ServiceProxyInfo> getServiceProxyByLimit(ServiceProxyQuery query);

    /**
     * 更新版本号
     *
     * @param id      实体ID
     * @param version 版本号
     * @return 成功标志
     */
    long updateVersion(long id, long version);

    /**
     * 通过条件获取项目下已发布的服务
     * 条件可缺省，缺省时不加入查询
     *
     * @param query
     * @return
     */
    List<ServiceProxyInfo> getByConditionOptional(ServiceProxyQuery query);
}
