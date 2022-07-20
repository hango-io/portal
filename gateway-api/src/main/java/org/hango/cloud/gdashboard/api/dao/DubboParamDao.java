package org.hango.cloud.gdashboard.api.dao;

import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 15:46.
 */
public interface DubboParamDao extends IBaseDao<DubboParamInfo> {
    /**
     * 获取API参数
     *
     * @param apiId
     * @return
     */
    List<DubboParamInfo> getDubboInfo(long apiId);

    /**
     * @param dubboType
     * @param apiId
     * @return
     */
    List<DubboParamInfo> getDubboParam(String dubboType, long apiId);

    /**
     * 根据paramId删除参数
     *
     * @param paramId
     */
    void delete(long paramId);

    /**
     * 根据apiId删除dubbo info
     *
     * @param apiId
     */
    void deleteDubboParam(long apiId);

    /**
     * 批量增加
     *
     * @param dubboParamInfos
     */
    void batchAdd(List<DubboParamInfo> dubboParamInfos);

}
