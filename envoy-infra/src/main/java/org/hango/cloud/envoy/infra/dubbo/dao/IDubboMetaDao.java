package org.hango.cloud.envoy.infra.dubbo.dao;


import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboMeta;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
public interface IDubboMetaDao extends IBaseDao<DubboMeta> {

    /**
     * 通过条件获取Dubbo 元数据信息表
     *
     * @param params
     * @param offset
     * @param limit
     * @return
     */
    List<DubboMeta> getRecordsByField(Map<String, Object> params, long offset, long limit);


    /**
     * 批量删除同一个IGV下的数据
     *
     * @param virtualGwId
     * @param interfaceName
     * @param group
     * @param version
     */
    void batchDeleteByCondition(long virtualGwId, String interfaceName, String group, String version);
}