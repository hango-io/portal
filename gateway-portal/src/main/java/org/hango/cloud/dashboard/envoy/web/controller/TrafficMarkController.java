package org.hango.cloud.dashboard.envoy.web.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.TrafficMarkInfo;
import org.hango.cloud.dashboard.envoy.service.ITrafficMarkService;
import org.hango.cloud.dashboard.envoy.web.dto.TrafficMarkDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 入口流量染色相关controller
 * 流量染色控制台crud
 *
 * @author qilu
 */
@RestController
@Validated
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2019-09-01"})
public class TrafficMarkController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(TrafficMarkController.class);

    @Autowired
    private ITrafficMarkService trafficMarkService;

    /**
     * 创建流量染色规则
     *
     * @param trafficColorDto 流量染色dto
     * @return
     */
    @MethodReentrantLock
    @Audit(eventName = "CreateTrafficColorRuleBatch", description = "创建流量染色规则")
    @RequestMapping(params = {"Action=CreateTrafficColorRule"}, method = RequestMethod.POST)
    public Object createTrafficColorRuleBatch(@Validated @RequestBody TrafficMarkDto trafficColorDto) {
        logger.info("创建入口流量染色规则，trafficColorDto:{}", trafficColorDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, null, trafficColorDto.getRouteRuleNames());
        AuditResourceHolder.set(resource);

        ErrorCode errorCode = trafficMarkService.checkCreateTrafficColorParam(trafficColorDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        trafficColorDto.setEnableStatus(Const.PLUGIN_STATE_DISABLE);
        TrafficMarkInfo trafficMarkInfo = trafficMarkService.addTrafficColorInfo(trafficColorDto);
        resource.setResourceId(trafficMarkInfo.getId());
        return apiReturn(CommonErrorCode.Success);

    }

    /**
     * 修改流量染色规则
     *
     * @param trafficColorDto 流量染色dto
     * @return
     */
    @MethodReentrantLock
    @Audit(eventName = "UpdateTrafficColorRule", description = "修改流量染色规则")
    @RequestMapping(params = {"Action=UpdateTrafficColorRule"}, method = RequestMethod.POST)
    public Object updateTrafficColorRule(@Validated @RequestBody TrafficMarkDto trafficColorDto) {
        logger.info("修改入口流量染色规则，trafficColorDto:{}", trafficColorDto);
        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, null, trafficColorDto.getRouteRuleNames()));

        ErrorCode errorCode = trafficMarkService.checkUpdateTrafficColorParam(trafficColorDto);
        //参数校验
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        TrafficMarkInfo trafficMarkInfo = trafficMarkService.getTrafficColorRuleById(trafficColorDto.getId());
        trafficMarkInfo.setTrafficParam(TrafficMarkDto.toMeta(trafficColorDto).getTrafficParam());
        trafficMarkInfo.setGwId(trafficColorDto.getGwId());
        trafficMarkInfo.setTrafficMatch(trafficColorDto.getTrafficMatch());
        trafficMarkService.updateTrafficColorInfo(trafficMarkInfo);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 根据流量染色id删除流量染色规则
     *
     * @param trafficColorRuleId 流量染色规则id
     * @return
     */
    @MethodReentrantLock
    @Audit(eventName = "DeleteTrafficColorRule", description = "删除流量染色规则")
    @RequestMapping(params = {"Action=DeleteTrafficColorRule"}, method = RequestMethod.GET)
    public Object deleteTrafficColorRule(@RequestParam(value = "TrafficColorRuleId") long trafficColorRuleId) {
        logger.info("删除入口流量染色规则，trafficColorRuleId:{}", trafficColorRuleId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, trafficColorRuleId, null);
        AuditResourceHolder.set(resource);

        TrafficMarkInfo trafficMarkInfo = trafficMarkService.getTrafficColorRuleById(trafficColorRuleId);
        if (null != trafficMarkInfo) {
            resource.setResourceName(trafficMarkInfo.getRouteRuleNames());
            if (trafficMarkInfo.getEnableStatus() == 1) {
                logger.info("流量染色规则已启用, 不允许删除");
                return apiReturn(CommonErrorCode.CannotDeleteOnlineTrafficColorRule);
            }
        }
        trafficMarkService.delete(trafficColorRuleId);
        return apiReturn(CommonErrorCode.Success);
    }

    /**
     * 查询某个染色标识下所有的流量染色规则，返回前端当前染色标识下的所有染色规则列表
     *
     * @param colorTag 流量染色标识
     * @param offset   偏移
     * @param limit    每页条数
     * @return 流量染色规则列表
     */
    @RequestMapping(params = {"Action=DescribeTrafficColorRuleList"}, method = RequestMethod.GET)
    public Object trafficColorRuleList(@RequestParam(value = "ColorTag", required = false) String colorTag, @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset, @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        logger.info("获取当前染色标识下的染色规则列表，colorTag：{}", colorTag);
        //offset,limit校验
        ErrorCode errorCode = CommonUtil.checkOffsetAndLimit(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);

        List<TrafficMarkInfo> trafficColorInfos = trafficMarkService.getTrafficColorByTagLimit(colorTag, offset, limit);
        if (CollectionUtils.isEmpty(trafficColorInfos)) {
            result.put("TrafficColorRuleCount", NumberUtils.INTEGER_ZERO);
            result.put("TrafficColorRuleList", Collections.EMPTY_LIST);
            return apiReturn(CommonErrorCode.Success, result);
        }
        List<TrafficMarkDto> trafficColorDtos = trafficColorInfos.stream().map(TrafficMarkDto::toDto).collect(Collectors.toList());
        result.put("TrafficColorRuleCount", trafficMarkService.getTrafficColorRuleCountByColorTag(colorTag));
        result.put("TrafficColorRuleList", trafficColorDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 开启流量染色规则
     *
     * @param trafficMarkRuleId 流量染色规则id
     * @return 操作结果
     */
    @PostMapping(params = {"Action=ModifyTrafficMarkRuleStatus"})
    public Object modifyTrafficMarkRuleStatus(@RequestParam(value = "TrafficColorRuleId") Long trafficMarkRuleId, @RequestParam(value = "TrafficMarkStatus") Integer trafficMarkStatus) {
        logger.info("modifyTrafficMarkRuleStatus id：{}, status: {}", trafficMarkRuleId, trafficMarkStatus);

        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_TRAFFIC_MARK, trafficMarkRuleId, null));

        // 校验参数
        if (!trafficMarkService.checkTrafficMarkStatus(trafficMarkStatus)) {
            logger.error("trafficMarkStatus is illegal, trafficMarkStatus: {}", trafficMarkStatus);
            return apiReturn(CommonErrorCode.InvalidParameter(String.valueOf(trafficMarkStatus), "trafficMarkStatus"));
        }
        if (!trafficMarkService.isTrafficColorExists(trafficMarkRuleId)) {
            logger.error("trafficMarkRuleId is illegal, trafficMarkRule not found, trafficMarkRuleId: {}", trafficMarkRuleId);
            return apiReturn(CommonErrorCode.NoSuchTrafficColorRule);
        }

        // 执行染色规则状态修改业务
        ErrorCode resultCode = trafficMarkService.modifyTrafficMarkRuleStatus(trafficMarkRuleId, trafficMarkStatus);
        return apiReturn(resultCode);
    }
}
