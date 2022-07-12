package org.hango.cloud.dashboard.envoy.web.controller;

import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集成proxy管理Controller
 */
@RestController
@Validated
@RequestMapping(value = Const.INTEGRATION_PREFIX, params = {"Version=2019-09-01"})
@ConditionalOnProperty("enableNsb")
public class EnvoyIntegrationProxyController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationProxyController.class);

    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IEnvoyIntegrationProxyService envoyIntegrationProxyService;

    @MethodReentrantLock
    @Audit(eventName = "PublishIntegration", description = "发布集成")
    @RequestMapping(params = {"Action=PublishIntegration"}, method = RequestMethod.GET)
    public String publishIntegration(@RequestParam(value = "IntegrationId") long integrationId,
                                     @RequestParam(value = "GwId") long gwId) {
        logger.info("发布集成, 集成id:{}，网关id:{}", integrationId, gwId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, integrationId, null);
        AuditResourceHolder.set(resource);

        //校验参数
        ErrorCode checkResult = envoyIntegrationProxyService.checkPublishParam(integrationId, gwId);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        //发布集成
        return apiReturn(envoyIntegrationProxyService.publishIntegration(integrationId, gwId));
    }

    @MethodReentrantLock
    @Audit(eventName = "OfflineIntegration", description = "下线集成")
    @RequestMapping(params = {"Action=OfflineIntegration"}, method = RequestMethod.GET)
    public String OfflineIntegration(@RequestParam(value = "IntegrationId") long integrationId) {
        logger.info("下线集成, 集成id:{}", integrationId);
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_ROUTE_RULE, integrationId, null);
        AuditResourceHolder.set(resource);

        ErrorCode result = envoyIntegrationProxyService.checkDeleteParam(integrationId);
        if (!CommonErrorCode.Success.getCode().equals(result.getCode())) {
            return apiReturn(result);
        }

        return apiReturn(envoyIntegrationProxyService.offlineIntegration(integrationId));
    }
}
