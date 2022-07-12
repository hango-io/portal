package org.hango.cloud.dashboard.envoy.dao;

import org.hango.cloud.dashboard.apiserver.dao.IBaseDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationSchemaInfo;

import java.util.List;

/**
 * 集成schema表Dao
 */
public interface IEnvoyIntegrationSchemaDao extends IBaseDao<EnvoyIntegrationSchemaInfo> {

    /**
     * 返回所有schema种类列表
     *
     * @return
     */
    public List<EnvoyIntegrationSchemaInfo> getSchemaKindList();

    /**
     * 根据schema的kind查询schema的详细信息
     *
     * @param schemaKind
     * @return
     */
    public EnvoyIntegrationSchemaInfo getSchemaByKind(String schemaKind);
}
