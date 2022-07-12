package org.hango.cloud.gdashboard.api.dao;

import org.hango.cloud.gdashboard.api.meta.WebServiceParamInfo;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:46.
 */
public interface WebServiceParamDao extends IBaseDao<WebServiceParamInfo> {
    /**
     * 通过API id删除webservice param
     *
     * @param apiId
     * @return
     */
    int deleteByApiId(long apiId);
}
