package org.hango.cloud.common.infra.route.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.service.CommonService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.RouteMirrorDto;
import org.hango.cloud.common.infra.route.dto.RouteQueryDto;
import org.hango.cloud.common.infra.route.dto.UpdateRouteDto;
import org.hango.cloud.common.infra.route.pojo.DestinationInfo;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;

import java.util.List;

/**
 * @author xin li
 * @date 2022/9/6 15:31
 */
public interface IRouteService extends CommonService<RoutePO, RouteDto> {

    /**
     * 路由更新参数校验
     *
     * @param routeDto 更新路由信息
     * @return 参数校验结果
     */
    ErrorCode checkUpdateParam(RouteDto routeDto);

    /**
     * 路由流量镜像信息校验
     *
     * @param routeMirrorDto 路由流量镜像信息
     * @return 参数校验是否成功
     */
    ErrorCode checkUpdateMirrorTrafficParam(RouteMirrorDto routeMirrorDto);

    /**
     * 发布流量镜像
     */
    long publishMirrorTraffic(RouteMirrorDto routeMirrorDto);

    /**
     * 基于路由关联的服务信息生成VirtualService的Destination信息
     * <p>
     * [
     * {
     * "port": 80,
     * "serviceId": 128,
     * "subsetName": "v1",
     * "weight": 34
     * },
     * {
     * "port": 80,
     * "serviceId": 128,
     * "subsetName": "v2",
     * "weight": 33
     * }
     * ]
     *
     * @param routeDto 路由信息
     * @return 基于路由信息中关联服务转换的VS中的目标信息
     */
    List<DestinationInfo> genDestinationInfoFromRouteServiceMeta(RouteDto routeDto);


    /**
     * 分页查询已发布路由规则列表
     */
    Page<RoutePO> getRoutePage(RouteQueryDto routeQueryDto);

    /**
     * 查询已发布路由规则列表
     */
    List<RouteDto> getRouteList(RouteQuery routeQuery);

    /**
     * 查询路由规则
     *
     * @param virtualGwId 网关id，若为0则不加入查询条件
     * @param id          路由规则id，若为0则不加入查询条件
     * @return 路由规则
     */
    RouteDto getRoute(long virtualGwId, long id);

    /**
     * 通过路由名称获取路由
     *
     * @param routeName 路由名称
     * @param virtualGwId 虚拟网关ID
     * @param projectId 项目ID
     * @return 路由对象
     */
    RouteDto getRouteByNameInProjectGateway(String routeName, long virtualGwId, long projectId);


    /**
     * 通过路由规则id查询路由规则发布情况
     *
     * @param routeRuleId 路由规则id
     * @return 路由规则发布信息
     */
    List<RouteDto> getRouteById(long routeRuleId);

    /**
     * 填充路由信息（项目ID等）
     *
     * @param routeDto 路由详情
     * @return 是否成功
     */
    ErrorCode fillRouteInfo(RouteDto routeDto);

    /**
     * 填充路由流量镜像参数
     *
     * @param routeMirrorDto 路由流量镜像配置
     */
    ErrorCode fillRouteMirrorDto(RouteMirrorDto routeMirrorDto);


    /**
     * 路由更新填充基本信息
     */
    RouteDto fillUpdateInfo(UpdateRouteDto updateRouteDto);

    ErrorCode checkDeleteParam(RouteDto routeDto);

}
