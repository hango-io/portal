package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ActionInfoHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.audit.meta.AuditMetaData;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 集成规则管理Controller
 */

@RestController
@Validated
@RequestMapping(value = Const.INTEGRATION_PREFIX, params = {"Version=2019-09-01"})
public class EnvoyIntegrationController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationController.class);

    @Autowired
    private IAuditConfigService auditConfigService;

    @Autowired
    private IEnvoyIntegrationService envoyIntegrationService;

    @MethodReentrantLock
    @Audit(eventName = "CreateIntegration", description = "创建集成")
    @RequestMapping(params = {"Action=CreateIntegration"}, method = RequestMethod.POST)
    public String createIntegration(@RequestBody EnvoyIntegrationDto envoyIntegrationDto) {
        logger.info("创建集成，envoyIntegrationDto:{}", envoyIntegrationDto);

        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, null, envoyIntegrationDto.getIntegrationName());
        AuditResourceHolder.set(resource);

        //配置审计,创建路由规则
        auditConfigService.record(new AuditMetaData(System.currentTimeMillis(), UserPermissionHolder.getAccountId(),
                ActionInfoHolder.getAction(), JSONObject.parseObject(JSON.toJSONString(envoyIntegrationDto))));

        EnvoyIntegrationInfo envoyIntegrationInfo = envoyIntegrationDto.toMeta();
        envoyIntegrationInfo.setProjectId(ProjectTraceHolder.getProId());

        //检验参数是否正确
        ErrorCode checkResult = envoyIntegrationService.checkAddParam(envoyIntegrationInfo);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        long integrationId = envoyIntegrationService.addIntegration(envoyIntegrationInfo);
        if (-1 == integrationId) {
            return apiReturn(CommonErrorCode.InternalServerError);
        }

        resource.setResourceId(integrationId);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("IntegrationId", integrationId);

        return apiReturn(CommonErrorCode.Success, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdateIntegration", description = "更新集成")
    @RequestMapping(params = {"Action=UpdateIntegration"}, method = RequestMethod.POST)
    public String updateIntegration(@RequestBody EnvoyIntegrationDto envoyIntegrationDto) {
        logger.info("更新路由规则，routeRuleDto: {}", envoyIntegrationDto);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, envoyIntegrationDto.getId(), envoyIntegrationDto.getIntegrationName());
        AuditResourceHolder.set(resource);

        //校验参数
        EnvoyIntegrationInfo envoyIntegrationInfo = envoyIntegrationDto.toMeta();
        ErrorCode checkResult = envoyIntegrationService.checkUpdateParams(envoyIntegrationInfo);

        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        ErrorCode errorCode = envoyIntegrationService.updateIntegration(envoyIntegrationInfo);
        return apiReturn(errorCode);
    }

    @MethodReentrantLock
    @Audit(eventName = "DeleteIntegration", description = "删除集成")
    @RequestMapping(params = {"Action=DeleteIntegration"}, method = RequestMethod.GET)
    public String deleteIntegration(@RequestParam(value = "IntegrationId") long id) {
        logger.info("根据集成id:{}，删除集成", id);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, id, null);
        AuditResourceHolder.set(resource);

        //校验参数
        ErrorCode checkResult = envoyIntegrationService.checkDeleteParam(id);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }
        ErrorCode errorCode = envoyIntegrationService.deleteIntegration(id);
        return apiReturn(errorCode);
    }

    @MethodReentrantLock
    @Audit(eventName = "DescribeIntegrationList", description = "查询集成列表")
    @RequestMapping(params = {"Action=DescribeIntegrationList"}, method = RequestMethod.GET)
    public String describeIntegrationList(@RequestParam(value = "Pattern", required = false) String pattern,
                                          @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                          @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                          @RequestParam(value = "Type", required = false) String type) {
        logger.info("分页集成，pattern:{}, projectId:{}", pattern, ProjectTraceHolder.getProId());

        if (type != null && type.length() > 0 && !type.equals("sub") && !type.equals("main")) {
            return apiReturn(CommonErrorCode.InvalidParameterValue(type, "type"));
        }
        ErrorCode checkResult = envoyIntegrationService.checkDescribeParam(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        long integrationInfoCount = envoyIntegrationService.getIntegrationInfoCount(ProjectTraceHolder.getProId(), pattern, type);
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("TotalCount", integrationInfoCount);

        List<EnvoyIntegrationInfo> integrationInfoList = envoyIntegrationService.getEnvoyIntegrationInfoByPattern(ProjectTraceHolder.getProId(), pattern, offset, limit, type);
        List<EnvoyIntegrationDto> envoyIntegrationDtoList = new ArrayList<>();

        Iterator<EnvoyIntegrationInfo> iterator = integrationInfoList.iterator();
        while (iterator.hasNext()) {
            envoyIntegrationDtoList.add(envoyIntegrationService.fromMeta(iterator.next()));
        }
        result.put("IntegrationInfoList", envoyIntegrationDtoList);
        return apiReturn(CommonErrorCode.Success, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DescribeIntegrationInfo", description = "根据ID查询集成详细信息")
    @RequestMapping(params = {"Action=DescribeIntegrationInfo"}, method = RequestMethod.GET)
    public String describeIntegrationInfo(@RequestParam(value = "IntegrationId") long integrationId) {
        logger.info("根据id查询集成详细信息，id{}", integrationId);

        EnvoyIntegrationInfo integrationInfo = envoyIntegrationService.getIntegrationInfoById(integrationId);
        if (integrationInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchIntegration);
        } else {
            Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
            EnvoyIntegrationDto integrationDto = envoyIntegrationService.fromMeta(integrationInfo);
            result.put("IntegrationInfo", integrationDto);
            return apiReturn(CommonErrorCode.Success, result);
        }
    }
}
