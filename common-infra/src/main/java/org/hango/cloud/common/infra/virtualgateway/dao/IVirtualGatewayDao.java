package org.hango.cloud.common.infra.virtualgateway.dao;

import org.hango.cloud.common.infra.base.dao.IBaseDao;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;

import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:25.
 */
public interface IVirtualGatewayDao extends IBaseDao<VirtualGateway> {

    /**
     * 通过项目ID、网关属性全模糊检索
     *
     * @param pattern   网关属性
     * @param offset    偏移量
     * @param limit     每页限制
     * @param projectId 项目ID， 当值小于等于1时，不开启该功能
     * @return VirtualGateway
     */
    List<VirtualGateway> getGatewayInfoByProjectIdAndLimit(String pattern, long projectId, long offset, long limit);

    /**
     * 查询被管理的虚拟网关信息
     */
    List<VirtualGateway> getManagedGatewayInfo(long projectId, String protocol);


    /**
     * 通过网关属性全模糊统计
     *
     * @param pattern   网关属性
     * @param projectId 项目ID， 当值小于等于1时，不开启该功能
     * @return count
     */
    long getGatewayInfoCountsByPattern(String pattern, long projectId);

    /**
     * 根据gwName查询满足条件网关id列表
     *
     * @param gwName    网关名称，支持模糊查询
     * @param projectId 项目id
     * @return {@link List<Long>} 满足条件的网关id列表
     */
    List<Long> getGwIdListByNameFuzzy(String gwName, long projectId);

    /**
     * 根据网关id列表查询网关信息列表
     *
     * @param gwIdList 网关id列表
     * @return {@link List< VirtualGateway >} 网关信息列表
     */
    List<VirtualGateway> getGatewayInfoList(List<Long> gwIdList);


    /**
     * 通过条件获取虚拟网关环境
     *
     * @param params 查询参数
     * @param offset 偏移量
     * @param limit  每页限制
     * @return
     */
    List<VirtualGateway> getRecordsByField(Map<String, Object> params, long offset, long limit);

    /**
     * 通过条件获取虚拟网关环境
     *
     * @param query
     * @return
     */
    List<VirtualGateway> getVirtualGatewayByConditions(QueryVirtualGatewayDto query);

    /**
     * 通过条件统计虚拟网关环境数
     *
     * @param query
     * @return
     */
    Integer countVirtualGatewayByConditions(QueryVirtualGatewayDto query);
}
