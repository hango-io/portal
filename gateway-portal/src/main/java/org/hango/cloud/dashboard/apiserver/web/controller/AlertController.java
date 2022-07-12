package org.hango.cloud.dashboard.apiserver.web.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.alertdto.AlertRuleDto;
import org.hango.cloud.dashboard.apiserver.dto.alertdto.MetricDataDto;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.DimensionType;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IAlertRuleService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IMetricService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.ResultActionWithMessage;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/7/5
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2019-07-11"})
@Validated
public class AlertController extends AbstractController {

    @Autowired
    private IAlertRuleService alertRuleService;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IMetricService metricService;

    @RequestMapping(params = {"Action=CreateAlarmEventRule"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateAlarmEventRule", description = "创建告警规则")
    public String addRule(@RequestBody @Validated AlertRuleDto alertRuleDto) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ALERT, null, alertRuleDto.getName());
        AuditResourceHolder.set(resource);
        ResultActionWithMessage ram = alertRuleService.addRule(alertRuleDto, ProjectTraceHolder.getTenantId(), ProjectTraceHolder.getProId(), UserPermissionHolder.getAccountId());
        if (ram.getStatusCode() != HttpStatus.SC_OK) {
            return apiReturn(ram.getStatusCode(), ram.getCode(), ram.getMessage(), null);
        }
        return apiReturnSuccess(null);
    }

    @RequestMapping(params = {"Action=UpdateAlarmEventRule"}, method = RequestMethod.POST)
    @Audit(eventName = "UpdateAlarmEventRule", description = "更新告警规则")
    public String updateRule(@RequestBody @Validated AlertRuleDto alertRuleDto) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ALERT, null, alertRuleDto.getName());
        AuditResourceHolder.set(resource);
        ResultActionWithMessage ram = alertRuleService.updateRule(alertRuleDto, ProjectTraceHolder.getTenantId(), ProjectTraceHolder.getProId(), UserPermissionHolder.getAccountId());
        if (ram.getStatusCode() != HttpStatus.SC_OK) {
            return apiReturn(ram.getStatusCode(), ram.getCode(), ram.getMessage(), null);
        }
        return apiReturnSuccess(null);
    }

    @RequestMapping(params = "Action=DeleteAlarmEventRule", method = RequestMethod.GET)
    @Audit(eventName = "UpdateAlarmEventRule", description = "删除告警规则")
    public String deleteRule(@RequestParam("Name") String name) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ALERT, null, name);
        AuditResourceHolder.set(resource);
        if (StringUtils.isBlank(name)) {
            return apiReturn(CommonErrorCode.MissingParameter("Name"));
        }
        ResultActionWithMessage ram = alertRuleService.deleteRule(name, ProjectTraceHolder.getTenantId(), ProjectTraceHolder.getProId(), UserPermissionHolder.getAccountId());
        if (ram.getStatusCode() != HttpStatus.SC_OK) {
            return apiReturn(ram.getStatusCode(), ram.getCode(), ram.getMessage(), null);
        }
        return apiReturnSuccess(null);
    }

    @RequestMapping(params = "Action=GetAlarmEventRule", method = RequestMethod.GET)
    public String getRule(@RequestParam("Name") String name) {
        if (StringUtils.isBlank(name)) {
            return apiReturn(CommonErrorCode.MissingParameter("Name"));
        }
        ResultActionWithMessage ram = alertRuleService.getRule(name, ProjectTraceHolder.getTenantId(), ProjectTraceHolder.getProId(), UserPermissionHolder.getAccountId());
        if (ram.getStatusCode() != HttpStatus.SC_OK) {
            return apiReturn(ram.getStatusCode(), ram.getCode(), ram.getMessage(), null);
        }
        return apiReturnSuccess(ram.getResult());
    }

    @RequestMapping(params = "Action=DescribeAlarmEventRuleList", method = RequestMethod.GET)
    public String getAll(@RequestParam(value = "Limit", defaultValue = "20") @Min(1) int limit,
                         @RequestParam(value = "Offset", defaultValue = "0") @Min(0) int offset,
                         @RequestParam(value = "ShowAll", defaultValue = "false") boolean showAll,
                         @RequestParam(value = "Sort", required = false) String sort,
                         @RequestParam(value = "Asc", required = false) boolean asc) {
        ResultActionWithMessage ram = alertRuleService.getRuleList(ProjectTraceHolder.getTenantId(),
                ProjectTraceHolder.getProId(), UserPermissionHolder.getAccountId(), sort, asc, showAll, offset, limit);
        if (ram.getStatusCode() != HttpStatus.SC_OK) {
            return apiReturn(ram.getStatusCode(), ram.getCode(), ram.getMessage(), null);
        }
        return apiReturnSuccess(((Map<String, Object>) ram.getResult()));
    }


    @RequestMapping(params = {"Action=DescribeMetricData"}, method = RequestMethod.GET)
    public String addRule(@RequestParam("DimensionType") String dimensionType,
                          @RequestParam("DimensionId") long dimensionId,
                          @RequestParam(value = "StartTime", required = false, defaultValue = "0") long startTime,
                          @RequestParam(value = "EndTime", required = false, defaultValue = "0") long endTime,
                          @RequestParam(value = "Step", required = false, defaultValue = "60") long step,
                          @RequestParam("MetricTypes") String[] metricTypes,
                          @RequestParam(value = "ProjectDivided", required = false, defaultValue = "true") boolean projectDivided,
                          @RequestParam(value = "GwId", required = false) String gwId) {

        logger.info("开始查询监控数据，GwId={},DimensionId={},DimensionType={},StartTime={},EndTime={},Step={},MetricTypes={}"
                , gwId, dimensionId, dimensionType, startTime, endTime, startTime, JSON.toJSONString(metricTypes));

        long gwIdForLong = NumberUtils.toLong(gwId);
        if (DimensionType.GATEWAY.getTarget().equals(dimensionType) && gwIdForLong != dimensionId) {
            return apiReturn(CommonErrorCode.InvalidParameterGwId(gwId));
        }
        if (NumberUtils.INTEGER_ZERO == startTime || NumberUtils.INTEGER_ZERO == endTime) {
            endTime = System.currentTimeMillis();
            startTime = endTime - Const.MS_OF_HOUR;
        }
        if (endTime - startTime > Const.QUERY_MAX_DAY * Const.MS_OF_DAY) {
            return apiReturn(CommonErrorCode.TimeRangeTooLarge(String.valueOf(Const.QUERY_MAX_DAY)));
        }
        if (startTime > endTime) {
            return apiReturn(CommonErrorCode.QueryTimeIllegal);
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwIdForLong);
        if (gatewayInfo == null) {
            logger.info("网关未找到");
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        ErrorCode errorCode = metricService.validMetricQueryParam(dimensionType, dimensionId, gatewayInfo, metricTypes);
        if (!CommonErrorCode.Success.equals(errorCode)) {
            logger.info("查询失败，ErrorCode = {}", JSON.toJSONString(errorCode));
            return apiReturn(errorCode);
        }
        Map<String, List<MetricDataDto>> metricData = metricService.describeMetricData(dimensionType, dimensionId, gatewayInfo, startTime, endTime, step, projectDivided, metricTypes);

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT, metricData);
        return apiReturnSuccess(result);
    }
}
