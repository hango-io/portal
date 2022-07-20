package org.hango.cloud.gdashboard.api.dao;

import org.hango.cloud.gdashboard.api.meta.ApiBody;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 15:46.
 */
public interface ApiBodyDao extends IBaseDao<ApiBody> {
    /**
     * 获取API Request Body或Response Body
     *
     * @param apiId
     * @param type
     * @return
     */
    List<ApiBody> getBody(long apiId, String type);

    /**
     * 查询Body中的参数
     *
     * @param paramName
     * @param type
     * @return
     */
    List<ApiBody> getBodyParam(String paramName, String type, long apiId);

    /**
     * 根据paramId删除参数
     *
     * @param paramId
     */
    void delete(long paramId);

    /**
     * 根据apiId删除body
     *
     * @param apiId
     */
    void deleteBody(long apiId);

    void deleteBody(long apiId, String type);

    /**
     * 查询API Body
     *
     * @param apiId
     * @return
     */
    List<ApiBody> getBodyByApiId(long apiId);

}
