package org.hango.cloud.envoy.infra.trafficmark.service;




import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.dto.TrafficMarkDto;

import java.util.List;

/**
 * 流量染色service层接口
 *
 * @author qilu01
 */
public interface ITrafficMarkService {

    /**
     * 获取染色标识下的所有流量染色规则
     *
     * @param colorTag 流量染色标识
     * @param offset   偏移
     * @param limit    每页的条数
     * @return 流量染色规则列表
     */
    List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long offset, long limit);

    /**
     * 获取染色标识下的染色规则数量
     *
     * @param colorTag 流量染色标识
     * @return 当前染色标识下的染色规则数量
     */
    long getTrafficColorRuleCountByColorTag(String colorTag);

    /**
     * 增加入口流量染色规则
     *
     * @param envoyTrafficColorDto 流量染色规则信息
     * @return 流量染色元数据
     */
    TrafficMarkInfo addTrafficColorInfo(TrafficMarkDto envoyTrafficColorDto);

    /**
     * 修改入口流量染色规则
     *
     * @param envoyTrafficColorInfo 流量染色规则信息
     * @return true为修改成功 false为失败
     */
    boolean updateTrafficColorInfo(TrafficMarkInfo envoyTrafficColorInfo);

    /**
     * 创建流量染色规则，参数校验
     *
     * @param envoyTrafficColorDto 流量染色规则信息
     * @return errorcode 错误码
     */
    ErrorCode checkCreateTrafficColorParam(TrafficMarkDto envoyTrafficColorDto);

    /**
     * 修改流量染色规则，参数校验
     *
     * @param envoyTrafficColorDto 流量染色规则信息
     * @return errorcode 错误码
     */
    ErrorCode checkUpdateTrafficColorParam(TrafficMarkDto envoyTrafficColorDto);

    /**
     * 检验染色规则名称是否存在
     *
     * @param trafficColorName 染色规则名称
     * @return 存在返回true 不存在返回false
     */
    boolean isTrafficColorNameExists(String trafficColorName);

    /**
     * 判断当前服务路由下是否已经有流量染色规则
     *
     * @param routeRuleIds 路由id列表
     * @return true为已经存在流量染色规则 false为不存在
     */
    boolean isTrafficColorExists(String routeRuleIds);

    /**
     * 判断流量染色规则是否存在
     *
     * @param id 流量染色规则id
     * @return true为已经存在流量染色规则 false为不存在
     */
    boolean isTrafficColorExists(long id);

    /**
     * 通过id获取流量染色规则
     *
     * @param id 流量染色规则id
     * @return 流量染色规则信息
     */
    TrafficMarkInfo getTrafficColorRuleById(long id);

    /**
     * 删除流量染色规则
     *
     * @param trafficColorRuleId 流量染色规则id
     * @return
     */
    void delete(long trafficColorRuleId);

    /**
     * 校验规则状态
     * 0: 禁用
     * 1: 启用
     *
     * @param trafficMarkStatus 流量染色状态
     * @return 是否合法
     */
    boolean checkTrafficMarkStatus(Integer trafficMarkStatus);

    /**
     * 修改流量染色规则的状态（启用\停用）
     *
     * @param trafficMarkRuleId 流量染色规则ID
     * @param trafficMarkStatus 流量染色状态（0:停用; 1:启用）
     * @return 操作结果
     */
    ErrorCode modifyTrafficMarkRuleStatus(Long trafficMarkRuleId, Integer trafficMarkStatus);

    /**
     * 根据流量染色规则对象中的“规则”组装流量染色插件字符串，下面对关键参数做阐述
     * trafficMatch 代表匹配类型（Header\Cookie等）
     * param 代表流量染色“规则”，是集合形式，单个元素规则："key + 匹配规则 + value"
     * colorTag 代表染色标记（例如"red"; "dev1"等）
     *
     * @param trafficColorRule 流量染色规则对象，param代表流量染色“规则”
     * @return 流量染色插件配置字符串
     */
    String assembleTrafficMarkPlugin(TrafficMarkInfo trafficColorRule);

    /**
     * 启用流量染色插件
     *
     * @param trafficMarkRuleId 流量染色规则ID
     * @return 操作结果
     */
    ErrorCode enableTrafficMarkPlugin(Long trafficMarkRuleId);

    /**
     * 停用流量染色插件
     *
     * @param trafficMarkRuleId 流量染色规则ID
     * @return 操作结果
     */
    ErrorCode disableTrafficMarkPlugin(Long trafficMarkRuleId);

    List<TrafficMarkInfo> getTrafficColorRulesByRouteRuleId(long routeRuleId);
}
