package org.hango.cloud.envoy.infra.trafficmark.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.CommonUtil;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.trafficmark.dto.TrafficMarkDto;
import org.hango.cloud.envoy.infra.trafficmark.meta.TrafficMarkInfo;
import org.hango.cloud.envoy.infra.trafficmark.service.ITrafficMarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
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
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2019-09-01"})
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
    @RequestMapping(params = {"Action=CreateTrafficColorRule"}, method = RequestMethod.POST)
    public Object createTrafficColorRuleBatch(@Validated @RequestBody TrafficMarkDto trafficColorDto) {
        logger.info("创建入口流量染色规则，trafficColorDto:{}", trafficColorDto);
        ErrorCode errorCode = trafficMarkService.checkCreateTrafficColorParam(trafficColorDto);
        //参数校验
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        trafficColorDto.setEnableStatus(EnvoyConst.PLUGIN_STATE_DISABLE);
        trafficMarkService.addTrafficColorInfo(trafficColorDto);
        return apiReturn(CommonErrorCode.SUCCESS);

    }

    /**
     * 修改流量染色规则
     *
     * @param trafficColorDto 流量染色dto
     * @return
     */
    @RequestMapping(params = {"Action=UpdateTrafficColorRule"}, method = RequestMethod.POST)
    public Object updateTrafficColorRule(@Validated @RequestBody TrafficMarkDto trafficColorDto) {
        logger.info("修改入口流量染色规则，trafficColorDto:{}", trafficColorDto);
        //操作审计记录资源名称

        ErrorCode errorCode = trafficMarkService.checkUpdateTrafficColorParam(trafficColorDto);
        //参数校验
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        TrafficMarkInfo trafficMarkInfo = trafficMarkService.getTrafficColorRuleById(trafficColorDto.getId());
        trafficMarkInfo.setTrafficParam(TrafficMarkDto.toMeta(trafficColorDto).getTrafficParam());
        trafficMarkInfo.setVirtualGwId(trafficColorDto.getVirtualGwId());
        trafficMarkInfo.setTrafficMatch(trafficColorDto.getTrafficMatch());
        trafficMarkService.updateTrafficColorInfo(trafficMarkInfo);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    /**
     * 根据流量染色id删除流量染色规则
     *
     * @param trafficColorRuleId 流量染色规则id
     * @return
     */
    @RequestMapping(params = {"Action=DeleteTrafficColorRule"}, method = RequestMethod.GET)
    public Object deleteTrafficColorRule(@RequestParam(value = "TrafficColorRuleId") long trafficColorRuleId) {
        logger.info("删除入口流量染色规则，trafficColorRuleId:{}", trafficColorRuleId);
        TrafficMarkInfo trafficMarkInfo = trafficMarkService.getTrafficColorRuleById(trafficColorRuleId);
        if (null != trafficMarkInfo) {
            if (trafficMarkInfo.getEnableStatus() == 1) {
                logger.info("流量染色规则已启用, 不允许删除");
                return apiReturn(EnvoyErrorCode.CANNOT_DELETE_ONLINE_TRAFFIC_COLOR_RULE);
            }
        }
        trafficMarkService.delete(trafficColorRuleId);
        return apiReturn(CommonErrorCode.SUCCESS);
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
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        Map<String, Object> result = Maps.newHashMap();

        List<TrafficMarkInfo> trafficColorInfos = trafficMarkService.getTrafficColorByTagLimit(colorTag, offset, limit);
        if (CollectionUtils.isEmpty(trafficColorInfos)) {
            result.put("TrafficColorRuleCount", NumberUtils.INTEGER_ZERO);
            result.put("TrafficColorRuleList", Collections.EMPTY_LIST);
            return apiReturn(CommonErrorCode.SUCCESS, result);
        }
        List<TrafficMarkDto> trafficColorDtos = trafficColorInfos.stream().map(TrafficMarkDto::toDto).collect(Collectors.toList());
        result.put("TrafficColorRuleCount", trafficMarkService.getTrafficColorRuleCountByColorTag(colorTag));
        result.put("TrafficColorRuleList", trafficColorDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
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

        // 校验参数
        if (!trafficMarkService.checkTrafficMarkStatus(trafficMarkStatus)) {
            logger.error("trafficMarkStatus is illegal, trafficMarkStatus: {}", trafficMarkStatus);
            return apiReturn(CommonErrorCode.invalidParameter(String.valueOf(trafficMarkStatus), "trafficMarkStatus"));
        }
        if (!trafficMarkService.isTrafficColorExists(trafficMarkRuleId)) {
            logger.error("trafficMarkRuleId is illegal, trafficMarkRule not found, trafficMarkRuleId: {}", trafficMarkRuleId);
            return apiReturn(EnvoyErrorCode.NO_SUCH_TRAFFIC_COLOR_RULE);
        }

        // 执行染色规则状态修改业务
        ErrorCode resultCode = trafficMarkService.modifyTrafficMarkRuleStatus(trafficMarkRuleId, trafficMarkStatus);
        return apiReturn(resultCode);
    }
}
