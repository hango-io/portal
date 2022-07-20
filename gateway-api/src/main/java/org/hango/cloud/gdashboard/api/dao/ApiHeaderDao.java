package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.ApiHeader;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 15:46.
 */
public interface ApiHeaderDao extends IBaseDao<ApiHeader> {

    /**
     * 查询API的Request Header或Response Header
     *
     * @param apiId
     * @param type
     * @return
     */
    List<ApiHeader> getHeader(long apiId, String type);

    /**
     * 查询Header中的某个参数
     *
     * @param paramName
     * @param type
     * @param apiId
     * @return
     */
    List<ApiHeader> getHeaderParam(String paramName, String type, long apiId);

    /**
     * 根据参数Id删除该参数
     *
     * @param paramId
     */
    void deleteHeaderParam(long paramId);


    /**
     * 根据apiId删除Header
     *
     * @param apiId
     */
    void deleteHeader(long apiId);


    /**
     * 根据apiId删除Request Header 或Response Header
     *
     * @param apiId
     * @param type
     */
    void deleteHeader(long apiId, String type);

    /**
     * 根据ApiId查询
     *
     * @param apiId
     * @return
     */
    List<ApiHeader> getHeaderByApiId(long apiId);
}
