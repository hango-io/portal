package org.hango.cloud.dashboard.envoy.web.controller;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.audit.service.IAuditConfigService;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIntegrationExecutionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集成执行历史Controller
 */
@RestController
@Validated
@RequestMapping(value = Const.INTEGRATION_PREFIX, params = {"Version=2019-09-01"})
public class EnvoyIntegrationExecutionHistoryController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationExecutionHistoryController.class);

    @Autowired
    private IAuditConfigService auditConfigService;
    @Autowired
    private IEnvoyIntegrationExecutionHistoryService integrationExecutionHistoryService;

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeAllIntegrationExecutionHistory"}, method = RequestMethod.GET)
    public String describeAllIntegrationExecutionHistory(@RequestParam(value = "Offset", required = false, defaultValue = "0") int offset,
                                                         @RequestParam(value = "Limit", required = false, defaultValue = "20") int limit) {
        logger.info("分页查询集成执行历史，offset:{}，limit:{}", offset, limit);
        //校验参数
        ErrorCode checkResult = integrationExecutionHistoryService.checkDescribeParam(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        Map<String, Object> param = integrationExecutionHistoryService.getExecutionHistoryByPage(offset, limit, 0);
        return apiReturn(CommonErrorCode.Success, param);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeIntegrationExecutionHistory"}, method = RequestMethod.GET)
    public String describeIntegrationExecutionHistory(@RequestParam(value = "IntegrationId") long integrationId,
                                                      @RequestParam(value = "Offset", required = false, defaultValue = "0") int offset,
                                                      @RequestParam(value = "Limit", required = false, defaultValue = "20") int limit) {
        logger.info("按照集成id:{},分页查询集成执行历史，offset:{}，limit:{}", integrationId, offset, limit);
        //校验参数
        ErrorCode checkResult = integrationExecutionHistoryService.checkDescribeParam(offset, limit);
        if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
            return apiReturn(checkResult);
        }

        Map<String, Object> param = integrationExecutionHistoryService.getExecutionHistoryByPage(offset, limit, integrationId);
        return apiReturn(CommonErrorCode.Success, param);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeIntegrationLog"}, method = RequestMethod.GET)
    public String DescribeIntegrationLog(@RequestParam(value = "ExecutionId") String executionId) {
        logger.info("按照执行号:{}查询日志", executionId);
        List<Map<String, Object>> traceLog = integrationExecutionHistoryService.getIntegrationTraceLog(executionId);
        List<Map<String, Object>> exceptionLog = integrationExecutionHistoryService.getIntegrationExceptionLog(executionId);
        Map<String, Object> param = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        param.put("TraceLog", traceLog);
        param.put("ExceptionLog", exceptionLog);
        return apiReturn(CommonErrorCode.Success, param);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeIntegrationStepLog"}, method = RequestMethod.GET)
    public String describeIntegrationStepLog(@RequestParam(value = "ExecutionId") String executionId,
                                             @RequestParam(value = "IntegrationId") long integrationId) {
        logger.info("按照执行号:{}查询step详细日志", executionId);
        List<Map<String, Object>> traceList = integrationExecutionHistoryService.getIntegrationTraceLog(executionId);
        List<Map<String, Object>> exceptionList = integrationExecutionHistoryService.getIntegrationExceptionLog(executionId);
        Map<String, Object> traceLog = integrationExecutionHistoryService.toStepMap(traceList);
        Map<String, Object> exceptionLog = integrationExecutionHistoryService.toStepMap(exceptionList);
        String step = integrationExecutionHistoryService.getStep(integrationId);

        Map<String, Object> param = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        param.put("Step", step);
        param.put("TraceLog", traceLog);
        param.put("ExceptionLog", exceptionLog);
        return apiReturn(CommonErrorCode.Success, param);
    }

}
