package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationSchemaInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyIntegrationSchemaDto;

import java.util.List;

/**
 * 集成schema的Service层
 */
public interface IEnvoyIntegrationSchemaService {

    /**
     * 返回所有schema种类列表
     *
     * @return 所有schema种类列表
     */
    public List<EnvoyIntegrationSchemaInfo> getSchemaKindList();

    /**
     * 将info转为dto
     *
     * @return schema的Dto
     */
    public EnvoyIntegrationSchemaDto fromMeta(EnvoyIntegrationSchemaInfo envoyIntegrationSchemaInfo);

    /**
     * 根据schema的kind查询schema的详细信息
     *
     * @param schemaKind schema种类
     * @return 对应的schema具体信息
     */
    public EnvoyIntegrationSchemaInfo getSchemaByKind(String schemaKind);
}
