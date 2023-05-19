package org.hango.cloud.common.infra.gateway.dao;

import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.gateway.meta.Gateway;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
public interface IGatewayDao extends IBaseDao<Gateway> {

    /**
     * 通过条件获取网关信息表
     *
     * @param params
     * @param offset
     * @param limit
     * @return
     */
    List<Gateway> getRecordsByField(Map<String, Object> params, long offset, long limit);

    /**
     * 通过条件模糊获取网关信息表
     *
     * @param name
     * @param offset
     * @param limit
     * @return
     */
    List<Gateway> getByName(String name, long offset, long limit);


    /**
     * 通过条件模糊统计网关数
     *
     * @param name
     * @return
     */
    int countByName(String name);
}