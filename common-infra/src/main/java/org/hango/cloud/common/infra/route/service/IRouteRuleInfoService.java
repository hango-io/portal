package org.hango.cloud.common.infra.route.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.route.dto.CopyRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/6 14:13
 */
public interface IRouteRuleInfoService extends CommonService<RouteRuleInfoPO, RouteRuleDto> {


    /**
     * 分页获取路由规则
     *
     */
    List<RouteRuleInfoPO> getRouteRuleList(RouteRuleQuery routeRuleQuery);

    /**
     * 分页获取路由规则
     *
     */
    Page<RouteRuleInfoPO> getRouteRulePage(RouteRuleQueryDto routeRuleQueryDto);



    /**
     *
     * @param copyRuleDto
     * @return
     */
    ErrorCode checkCopyParam(CopyRuleDto copyRuleDto);


    /**
     *
     * @param copyRuleDto
     * @return
     */
    long copyRouteRule(CopyRuleDto copyRuleDto);


    /**
     * 根据路由规则id列表查询路由规则详情列表
     *
     * @param routeRuleIdList 路由规则id列表
     * @return {@link List< RouteRuleDto >} 路由规则详情列表
     */
    List<RouteRuleDto> getRouteRuleList(List<Long> routeRuleIdList);


    int updatePublishStatus(Long id, Integer publishStatus);


}
