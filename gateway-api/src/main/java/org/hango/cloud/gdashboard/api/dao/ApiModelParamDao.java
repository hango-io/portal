package org.hango.cloud.gdashboard.api.dao;


import org.hango.cloud.gdashboard.api.meta.ApiModelParam;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 16:08.
 */
public interface ApiModelParamDao extends IBaseDao<ApiModelParam> {

    /**
     * 根据ModelId删除模型参数
     *
     * @param modelId
     * @return
     */
    long deleteApiModelParamByModelId(long modelId);
}
