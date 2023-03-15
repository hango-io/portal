package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.ApiInfo;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 10:27.
 */
public interface ApiInfoDao extends IBaseDao<ApiInfo> {
    /**
     * 根据Id删除API
     *
     * @param apiId
     */
    void deleteApi(long apiId);


    /**
     * 修复数据使用
     *
     * @return
     */
    List<ApiInfo> find();


    /**
     * 修改API状态
     *
     * @param apiId
     * @param status
     * @return
     */
    int update(long apiId, String status);

    /**
     * 该方法插入数据库的时候指定id而不使用数据库自增id
     */
    long addWithId(ApiInfo apiInfo);

    /**
     * 通过serviceId以及pattern模糊匹配apiInfo
     *
     * @param serviceId
     * @param documentStatusId
     * @param pattern
     * @param offset
     * @param limit
     * @return
     */
    List<ApiInfo> findAllApiInfoByServiceLimit(long serviceId, long documentStatusId, String pattern, long offset, long limit);

    List<ApiInfo> findAllApiInfoByProjectLimit(long projectId, long documentStatusId, String pattern, long offset, long limit);

    long getApiCountByService(long serviceId, long documentStatusId, String pattern);

    long getApiCountByProject(long projectId, long documentStatusId, String pattern);
}
