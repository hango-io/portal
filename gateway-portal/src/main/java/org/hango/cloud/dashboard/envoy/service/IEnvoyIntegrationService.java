package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationDto;

import java.util.List;

/**
 * 集成Service层接口
 */
public interface IEnvoyIntegrationService {

    /**
     * 校验创建集成时的参数
     *
     * @param envoyIntegrationInfo 新增的集成数据
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkAddParam(EnvoyIntegrationInfo envoyIntegrationInfo);

    /**
     * 校验参数
     *
     * @param integrationName 集成名称
     * @param projectId       项目id
     * @return 正确返回true，错误返回false
     */
    public boolean checkSameName(String integrationName, long projectId);

    /**
     * 创建集成
     *
     * @param envoyIntegrationInfo 新增的集成数据
     * @return 创建的集成的ID
     */
    public long addIntegration(EnvoyIntegrationInfo envoyIntegrationInfo);

    /**
     * 校验更新集成时的参数
     *
     * @param envoyIntegrationInfo 要更新的集成信息
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkUpdateParams(EnvoyIntegrationInfo envoyIntegrationInfo);

    /**
     * 更新集成
     *
     * @param envoyIntegrationInfo 要更新的集成信息
     * @return {@link ErrorCode} 当更新成功时返回Success，失败时返回相应的原因
     */
    public ErrorCode updateIntegration(EnvoyIntegrationInfo envoyIntegrationInfo);

    /**
     * 删除集成参数校验
     *
     * @param id 集成id
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkDeleteParam(long id);

    /**
     * 根据集成ID删除集成
     *
     * @param id 集成id
     * @return {@link ErrorCode} 当正确删除时返回Success，失败时返回相应的原因
     */
    public ErrorCode deleteIntegration(long id);

    /**
     * 根据项目ID查询集成总数
     *
     * @param projectId 项目id
     * @param type      集成类型
     * @return 集成总数
     */
    public long getIntegrationInfoCount(long projectId, String pattern, String type);

    /**
     * 条件查询集成列表
     *
     * @param projectId 项目id
     * @param pattern   模糊查询参数
     * @param offset    位移
     * @param limit     每页条数
     * @param type      集成类型
     * @return 符合条件的集成列表
     */
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByPattern(long projectId, String pattern, long offset, long limit, String type);

    /**
     * 根据集成info生成dto
     *
     * @param integrationInfo 集成info
     * @return
     */
    public EnvoyIntegrationDto fromMeta(EnvoyIntegrationInfo integrationInfo);

    /**
     * 根据集成ID查询集成详细信息
     *
     * @param id 集成id
     * @return
     */
    public EnvoyIntegrationInfo getIntegrationInfoById(long id);

    /**
     * 集成规则全量更新
     *
     * @param envoyIntegrationInfo 集成信息
     * @return
     */
    public long updateAll(EnvoyIntegrationInfo envoyIntegrationInfo);

    /**
     * 根据id列表返回集成列表
     *
     * @param idList id列表
     * @return
     */
    public List<EnvoyIntegrationInfo> getByIdlist(List<Long> idList);

    /**
     * 校验分页查询参数
     *
     * @param offset 位移
     * @param limit  每页限制
     * @return
     */
    public ErrorCode checkDescribeParam(long offset, long limit);

    /**
     * 校验type，只允许为sub或main
     *
     * @param type 集成类型
     * @return {@link ErrorCode} 当校验正确时返回Success，失败时返回相应的原因
     */
    public ErrorCode checkType(String type);

}
