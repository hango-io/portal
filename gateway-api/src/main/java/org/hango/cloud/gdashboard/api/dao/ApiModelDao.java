package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.ApiModel;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:47.
 * @Modified hanjiahao
 */
public interface ApiModelDao extends IBaseDao<ApiModel> {

    /**
     * 分页获取数据库中API model数据
     *
     * @param projectId
     * @param offset
     * @param limit
     * @param pattern
     * @return
     */
    List<ApiModel> findApiModelByProjectLimit(long projectId, long offset, long limit, String pattern);

    /**
     * 通过分页serviceId，分页获取数据库中APi model数据
     *
     * @param serviceId
     * @param offset
     * @param limit
     * @param pattern
     * @return
     */
    List<ApiModel> findApiModelByServiceIdLimit(long serviceId, long offset, long limit, String pattern);

    /**
     * 通过项目id和pattern获取model数量
     *
     * @param projectId
     * @param pattern
     * @return
     */
    long getApiModelCountByProjectPattern(long projectId, String pattern);

    /**
     * 通过服务id和pattern获取model数量
     *
     * @param serviceId
     * @param pattern
     * @return
     */
    long getApiModelCountByServicePattern(long serviceId, String pattern);

    long deleteApiModelByServiceId(long serviceId);
}
