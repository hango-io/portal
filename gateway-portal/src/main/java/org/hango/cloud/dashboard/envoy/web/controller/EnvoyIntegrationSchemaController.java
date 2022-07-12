package org.hango.cloud.dashboard.envoy.web.controller;

import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationSchemaInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationSchemaService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationSchemaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
 * 集成schema的Controller层
 */
@RestController
@Validated
@RequestMapping(value = Const.INTEGRATION_PREFIX, params = {"Version=2019-09-01"})
public class EnvoyIntegrationSchemaController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationSchemaController.class);

    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IEnvoyIntegrationSchemaService envoyIntegrationSchemaService;

    @MethodReentrantLock
    @Audit(eventName = "DescribeIntegrationSchemaKind", description = "查询schema种类列表")
    @RequestMapping(params = {"Action=DescribeIntegrationSchemaKind"}, method = RequestMethod.GET)
    public String describeIntegrationSchemaKind() {
        logger.info("查询所有的schema的种类列表");

        List<EnvoyIntegrationSchemaInfo> schemaKindList = envoyIntegrationSchemaService.getSchemaKindList();

        Iterator<EnvoyIntegrationSchemaInfo> iterator = schemaKindList.iterator();
        List<EnvoyIntegrationSchemaDto> schemaDtos = new ArrayList<>();
        while (iterator.hasNext()) {
            schemaDtos.add(envoyIntegrationSchemaService.fromMeta(iterator.next()));
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("SchemaKindList", schemaDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    @MethodReentrantLock
    @Audit(eventName = "DescribeIntegrationSchemaInfo", description = "查询schema详细信息")
    @RequestMapping(params = {"Action=DescribeIntegrationSchemaInfo"}, method = RequestMethod.GET)
    public String describeIntegrationSchemaInfo(@RequestParam(value = "SchemaKind") String schemaKind) {
        logger.info("根据schema的kind查询详细信息，kind:{}", schemaKind);

        EnvoyIntegrationSchemaInfo schemaInfo = envoyIntegrationSchemaService.getSchemaByKind(schemaKind);

        if (schemaInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchSchema);
        }
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("SchemaKind", envoyIntegrationSchemaService.fromMeta(schemaInfo));
        return apiReturn(CommonErrorCode.Success, result);
    }
}
