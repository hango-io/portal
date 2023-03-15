package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.ApiParamType;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:47.
 */
public interface ApiParamTypeDao extends IBaseDao<ApiParamType> {

    List<ApiParamType> findAll(String location);

}
