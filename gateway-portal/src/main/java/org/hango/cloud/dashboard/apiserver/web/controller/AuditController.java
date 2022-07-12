package org.hango.cloud.dashboard.apiserver.web.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.AuditInfoDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.AuditQueryDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.CallStatisticsInfoDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ServiceRankDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.AuditQuery;
import org.hango.cloud.dashboard.apiserver.meta.audit.CallStatisticsInfo;
import org.hango.cloud.dashboard.apiserver.meta.audit.ServiceRankInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.AuditStatService;
import org.hango.cloud.dashboard.apiserver.service.IAuditService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IMetricService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.RequestContextHolder;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class AuditController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);
    private static final String[] labels = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    @Autowired
    private IAuditService auditService;
    @Autowired
    private AuditStatService auditStatService;
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IMetricService metricService;
    @Autowired
    private ApiServerConfig apiServerConfig;

    /**
     * 获取项目下所有服务
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllService"})
    public String getServiceList(@RequestParam(name = "GwId") long gwId) {
        logger.info("开始查询接入网关的所有服务");
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(gwId));
        }
        List<String> services = auditStatService.getAllServiceTag(gatewayInfo);
        //显示名称
        List<String> serviceNames = new ArrayList<>();
        for (String service : services) {
            ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceName(service);
            serviceNames.add(serviceInfo.getDisplayName());
        }
        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, serviceNames);
        return apiReturnSuccess(result);

    }

    /**
     * 获取审计数据列表get
     *
     * @param auditQueryDto
     * @return
     */
    @GetMapping(params = {"Action=DescribeAuditDetail"})
    public Object getAuditDetail(AuditQueryDto auditQueryDto, @RequestParam(name = "GwId") long gwId) {
        return getAuditDetails(auditQueryDto, gwId);
    }

    /**
     * 获取审计数据列表post
     *
     * @param auditQueryDto
     * @return
     */
    @PostMapping(params = {"Action=DescribeAuditDetail"})
    public Object postAuditDetail(@RequestBody AuditQueryDto auditQueryDto, @RequestParam(name = "GwId") long gwId) {
        return getAuditDetails(auditQueryDto, gwId);
    }

    public Object getAuditDetails(AuditQueryDto auditQueryDto, long gwId) {
        logger.info("查询审计列表, auditQueryDto = {}", JSON.toJSONString(auditQueryDto));
        //判断网关环境，是否存在当前网关环境
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId)));
        }

        ErrorCode errorCode = validRequestParam(auditQueryDto);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        AuditQuery auditQuery = auditQueryDto.castInfo();
        Page<AuditInfo> pageResult = auditService.getAuditInfoList(auditQuery, gatewayInfo);
        Map<String, Object> result = new HashMap<>(2);
        HttpServletRequest request = RequestContextHolder.getRequest();
        Object attribute = request.getAttribute(Const.ATTRIBUTE_FOR_AUDIT);
        if (attribute != null) {
            if (Const.AUDIT_HOST_UNREACHABLE.equals(attribute)) {
                return apiReturn(CommonErrorCode.InternalServerError);
            } else if (Const.AUDIT_QUERY_TIMEOUT.equals(attribute)) {
                return apiReturn(CommonErrorCode.ReadTimeOut);
            } else if (Const.AUDIT_SCROLL_TIMEOUT.equals(attribute)) {
                return apiReturn(CommonErrorCode.ScrollTimeOut);
            }
        }
        result.put(RESULT, pageResult == null ? null : BeanUtil.copyList(pageResult.getContent(), AuditInfoDto.class));
        result.put(TOTAL_COUNT, pageResult == null ? null : pageResult.getTotalElements());
        result.put(Const.AUDIT_SCROLL_ID, request.getAttribute(Const.AUDIT_SCROLL_ID));
        return apiReturnSuccess(result);
    }

    /**
     * 获取审计数据详情
     *
     * @param auditId
     * @param auditIndex
     * @param gwId
     * @return
     */
    @GetMapping(params = {"Action=GetAuditInfo"})
    public Object GetAuditInfo(@RequestParam(name = "AuditId") String auditId,
                               @RequestParam(name = "AuditIndex") String auditIndex,
                               @RequestParam(name = "GwId") long gwId) {
        logger.info("查询审计详情, AuditId = {} ,AuditTIme = {} GwId = {}", auditId, auditIndex, gwId);
        //判断网关环境，是否存在当前网关环境
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId)));
        }
        String auditDetail = auditService.getAuditDetail(auditId, auditIndex, gatewayInfo);
        Object attribute = RequestContextHolder.getRequest().getAttribute(Const.ATTRIBUTE_FOR_AUDIT);

        if (attribute != null && Const.AUDIT_QUERY_TIMEOUT.equals(attribute)) {
            //return apiReturn(CommonErrorCode.t)
        }
        Map<String, Object> result = new HashMap<>(2);
        result.put(RESULT, auditDetail);
        return apiReturnSuccess(result);
    }

    /**
     * 获取审计数据描述
     *
     * @param gwId
     * @return
     */
    @GetMapping(params = {"Action=GetAuditDescription"}, produces = "text/plain")
    public Object getDescription(@RequestParam(name = "GwId") long gwId) {
        logger.info("查询审计数据描述 ，GwId = {}", gwId);
        //判断网关环境，是否存在当前网关环境
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId)));
        }
        String auditDescription = apiServerConfig.getAuditDescription();
        if (StringUtils.isBlank(auditDescription)) {
            return apiReturn(CommonErrorCode.Success);
        }
        for (String s : auditDescription.split(";")) {
            String[] split = s.split(":");
            if (split[0].equals(gatewayInfo.getGwType())) {
                return apiReturnSuccess(split[1].split(","));
            }
        }
        logger.info("不存在的网关审计数据描述配置");
        return apiReturnSuccess(null);
    }

    /**
     * 获取服务调用统计
     *
     * @param auditQueryDto
     * @return
     */
    @GetMapping(params = {"Action=DescribeAuditStatistics"})
    public Object getAuditStatistics(AuditQueryDto auditQueryDto, @RequestParam(name = "GwId") long gwId) {
        logger.info("查询审计统计信息, auditQueryDto = {}", JSON.toJSONString(auditQueryDto));
        //判断网关环境，是否存在当前网关环境
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(gwId));
        }

        ErrorCode errorCode = validRequestParam(auditQueryDto);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        if (!Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())
                && StringUtils.isBlank(auditQueryDto.getServiceName())) {
            return apiReturn(CommonErrorCode.MissingParameterServiceName);
        }
        CallStatisticsInfo auditStatisticsInfo = auditService.getAuditStatisticsInfo(auditQueryDto.castInfo(), gatewayInfo);
        return apiReturnSuccess(BeanUtil.copy(auditStatisticsInfo, CallStatisticsInfoDto.class));
    }


    @GetMapping(params = {"Action=ServiceRank"})
    public String serviceCallRank(@RequestParam(value = "StartTime") long startTime,
                                  @RequestParam(value = "EndTime") long endTime,
                                  @RequestParam(value = "RankType") String rankType,
                                  @RequestParam(name = "GwId") long gwId) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            logger.warn("不存在当前gwId:{}的网关环境", gwId);
            return apiReturn(CommonErrorCode.InvalidParameterGwId(gwId));
        }
        if (endTime - startTime > Const.QUERY_MAX_DAY * Const.MS_OF_DAY) {
            return apiReturn(CommonErrorCode.TimeRangeTooLarge(String.valueOf(Const.QUERY_MAX_DAY)));
        }
        if (startTime > endTime) {
            return apiReturn(CommonErrorCode.QueryTimeIllegal);
        }
        List<ServiceRankInfo> rankInfoList = metricService.getServiceRank(startTime, endTime, gatewayInfo, ProjectTraceHolder.getProId(), rankType);
        return apiReturnSuccess(BeanUtil.copyList(rankInfoList, ServiceRankDto.class));
    }


    /**
     * 校验HTTP入参
     *
     * @param auditQueryDto
     * @return
     */
    private ErrorCode validRequestParam(AuditQueryDto auditQueryDto) {
        if (NumberUtils.LONG_ZERO == (auditQueryDto.getEndTime() & auditQueryDto.getStartTime())) {
            return CommonErrorCode.MissingParameterQueryTime;
        }

        if (StringUtils.isNotBlank(auditQueryDto.getRespCode()) &&
                !StringUtils.equalsAny(auditQueryDto.getRespCode(), Const.AUDIT_RESP_CODE_4XX,
                        Const.AUDIT_RESP_CODE_5XX, Const.AUDIT_RESP_CODE_ALL)
                && !NumberUtils.isDigits(auditQueryDto.getRespCode())) {
            return CommonErrorCode.InvalidParameterRespCode(auditQueryDto.getRespCode());
        }

        if (StringUtils.isNotBlank(auditQueryDto.getServiceName())) {
            ServiceInfo serviceInfo = serviceInfoService.describeDisplayName(auditQueryDto.getServiceName(), ProjectTraceHolder.getProId());
            if (serviceInfo != null) {
                //显示名称替换为服务标志
                auditQueryDto.setServiceName(serviceInfo.getServiceName());
            }
        }

        if (NumberUtils.INTEGER_ZERO > auditQueryDto.getDuration()) {
            return CommonErrorCode.InvalidDuration(String.valueOf(auditQueryDto.getDuration()));
        }

        if (NumberUtils.INTEGER_ZERO > auditQueryDto.getMinDuration()) {
            return CommonErrorCode.InvalidDuration(String.valueOf(auditQueryDto.getMinDuration()));
        }

        if (NumberUtils.INTEGER_ZERO > auditQueryDto.getMaxDuration()) {
            return CommonErrorCode.InvalidDuration(String.valueOf(auditQueryDto.getMaxDuration()));
        }

        if (auditQueryDto.getMaxDuration() < auditQueryDto.getMinDuration()) {
            return CommonErrorCode.QueryTimeIllegal;
        }

        if (auditQueryDto.getEndTime() - auditQueryDto.getStartTime() > Const.QUERY_MAX_DAY * Const.MS_OF_DAY) {
            return CommonErrorCode.TimeRangeTooLarge(String.valueOf(Const.QUERY_MAX_DAY));
        }
        if (auditQueryDto.getStartTime() > auditQueryDto.getEndTime()) {
            return CommonErrorCode.QueryTimeIllegal;
        }
        return CommonErrorCode.Success;
    }
}
