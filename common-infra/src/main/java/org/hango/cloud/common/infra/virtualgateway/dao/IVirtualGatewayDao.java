package org.hango.cloud.common.infra.virtualgateway.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.dao.ICommonDao;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGatewayQuery;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:25.
 */
public interface IVirtualGatewayDao extends ICommonDao<VirtualGateway> {



    /**
     * 分页查询虚拟网关域名信息
     */
    Page<VirtualGateway> getVirtualGatewayPage(VirtualGatewayQuery query);


    /**
     * 查询域名列表
     */
    List<VirtualGateway> getVirtualGatewayList(VirtualGatewayQuery query);


    /**
     * 查询域名列表
     */
    Boolean exist(VirtualGatewayQuery query);
}
