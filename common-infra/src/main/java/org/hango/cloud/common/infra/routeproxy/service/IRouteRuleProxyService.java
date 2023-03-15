package org.hango.cloud.common.infra.routeproxy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.dto.RouteMirrorDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/6 15:31
 */
public interface IRouteRuleProxyService extends CommonService<RouteRuleProxyPO, RouteRuleProxyDto> {


    /**
     * 路由发布更新参数校验
     *
     * @param routeRuleProxyDto 路由发布更新Dto
     * @return 参数校验结果
     */
    ErrorCode checkUpdateParam(RouteRuleProxyDto routeRuleProxyDto);

    /**
     * 路由发布更新参数校验
     * @return 参数校验结果
     */
    ErrorCode checkUpdateMirrorTrafficParam(RouteMirrorDto routeMirrorDto);

    /**
     * 发布流量镜像
     *
     */
    long publishMirrorTraffic(RouteMirrorDto routeMirrorDto);

    /**
     * 更新已发布路由
     *
     */
    long updateRouteProxy(RouteRuleProxyDto routeRuleProxyDto);


    /**
     * 分页查询已发布路由规则列表
     */
    Page<RouteRuleProxyPO> getRouteRuleProxyPage(RouteRuleQueryDto routeRuleQueryDto);

    /**
     * 查询已发布路由规则列表
     */
    List<RouteRuleProxyDto> getRouteRuleProxyList(RouteRuleQuery routeRuleQuery);

    /**
     * 查询已发布路由规则
     *
     *
     * @param virtualGwId        网关id，若为0则不加入查询条件
     * @param routeRuleId 路由规则id，若为0则不加入查询条件
     * @return 已发布路由规则
     */
    RouteRuleProxyDto getRouteRuleProxy(long virtualGwId, long routeRuleId);


    /**
     * 通过路由规则id查询路由规则发布情况
     *
     * @param routeRuleId 路由规则id
     * @return 路由规则发布信息
     */
    List<RouteRuleProxyDto> getRouteRuleProxyByRouteRuleId(long routeRuleId);




    ErrorCode fillRouteRuleProxy(RouteRuleProxyDto routeRuleProxyDto);
}
