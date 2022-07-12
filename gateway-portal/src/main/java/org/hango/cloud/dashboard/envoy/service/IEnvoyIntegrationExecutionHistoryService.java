package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationExecutionHistoryDto;

import java.util.List;
import java.util.Map;

/**
 * 集成执行历史Service层
 */
public interface IEnvoyIntegrationExecutionHistoryService {

    /**
     * 分页查询集成执行记录
     *
     * @param offset 偏移
     * @param limit  每页限制
     * @param id     集成id
     * @return 查询结果，包含总数和具体数据
     */
    public Map<String, Object> getExecutionHistoryByPage(int offset, int limit, long id);

    /**
     * 添加集成名称
     *
     * @param historyDtoList 集成列表
     * @return 添加了名字的集成列表
     */
    public List<EnvoyIntegrationExecutionHistoryDto> getIntegrationName(List<EnvoyIntegrationExecutionHistoryDto> historyDtoList);

    /**
     * 查询执行log
     *
     * @param executionId 执行号
     * @param type        日志种类
     * @return 按照种类和执行号查询日志
     */
    public List<Map<String, Object>> getIntegrationLog(String executionId, String type);

    /**
     * 校验查询参数
     *
     * @param offset 便宜
     * @param limit  每页限制
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkDescribeParam(long offset, long limit);

    /**
     * 查询执行失败日志
     *
     * @param executionId 执行号
     * @return 对应执行号的执行异常日志
     */
    public List<Map<String, Object>> getIntegrationExceptionLog(String executionId);

    /**
     * 查询执行trace日志
     *
     * @param executionId 执行号
     * @return 对应执行号的执行trace日志
     */
    public List<Map<String, Object>> getIntegrationTraceLog(String executionId);

    /**
     * 提取其中的stepId作为key
     *
     * @param list 集成执行日志
     * @return 按照stepId聚合的step日志
     */
    public Map<String, Object> toStepMap(List<Map<String, Object>> list);

    /**
     * 获取step
     *
     * @param id 集成id
     * @return 对应集成id的step
     */
    public String getStep(long id);
}
