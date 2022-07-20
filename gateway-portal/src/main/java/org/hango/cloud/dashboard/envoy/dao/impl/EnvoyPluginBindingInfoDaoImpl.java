package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginBindingInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件绑定关系dao层接口类
 *
 * @author hzchenzhongyang 2019-11-11
 */
@Component
public class EnvoyPluginBindingInfoDaoImpl extends BaseDao implements IEnvoyPluginBindingInfoDao {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginBindingInfoDaoImpl.class);

    @Override
    public long add(EnvoyPluginBindingInfo envoyPluginBindingInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_plugin_binding (plugin_type, binding_object_id, binding_object_type, plugin_configuration, create_time, update_time, gw_id, project_id, plugin_priority, binding_status, template_id, template_version)"
                + " values (:pluginType, :bindingObjectId, :bindingObjectType, :pluginConfiguration, :createTime, :updateTime, :gwId, :projectId, :pluginPriority, :bindingStatus, :templateId, :templateVersion)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginBindingInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyPluginBindingInfo: {}", ReflectionToStringBuilder.toString(envoyPluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(EnvoyPluginBindingInfo envoyPluginBindingInfo) {
        String sql = "update apigw_envoy_plugin_binding set plugin_configuration=:pluginConfiguration, "
                + "binding_status=:bindingStatus, update_time=:updateTime, template_id=:templateId, "
                + "template_version=:templateVersion, plugin_type = :pluginType where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginBindingInfo);
        logger.info("update envoyPluginBindingInfo: {}", ReflectionToStringBuilder.toString(envoyPluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyPluginBindingInfo envoyPluginBindingInfo) {
        String sql = "delete from apigw_envoy_plugin_binding where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginBindingInfo);
        logger.info("delete envoyPluginBindingInfo: {}", ReflectionToStringBuilder.toString(envoyPluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyPluginBindingInfo get(long id) {
        String sql = "select * from apigw_envoy_plugin_binding where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyPluginBindingInfoRowMapper());
    }

    @Override
    public List<EnvoyPluginBindingInfo> findAll() {
        String sql = "select * from apigw_envoy_plugin_binding";
        return namedParameterJdbcTemplate.query(sql, new EnvoyPluginBindingInfoRowMapper());
    }

    @Override
    public List<EnvoyPluginBindingInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_plugin_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginBindingInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_plugin_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getBindingPluginCount(long gwId, long projectId, String bindingObjectId, String bindingObjectType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);

        String sql = "select count(*) from apigw_envoy_plugin_binding where project_id=:projectId";
        if (0 < gwId) {
            params.put("gwId", gwId);
            sql = sql + " and gw_id=:gwId";
        }
        if (StringUtils.isNotBlank(bindingObjectType)) {
            params.put("bindingObjectType", bindingObjectType);
            sql = sql + " and binding_object_type=:bindingObjectType";
        }
        if (StringUtils.isNotBlank(bindingObjectId)) {
            params.put("bindingObjectId", bindingObjectId);
            sql = sql + " and binding_object_id=:bindingObjectId";
        }
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getBindingPluginCount(long projectId, long gwId, List<Long> gwIdList, String bindingObjectId, List<String> bindingObjectIdList, List<String> bindingObjectTypeList, String pattern) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String sql = "select count(*) from apigw_envoy_plugin_binding where project_id=:projectId";
        params.put("projectId", projectId);

        sql = buildPatternSql(gwId, gwIdList, bindingObjectId, bindingObjectIdList, bindingObjectTypeList, pattern, params, sql);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    private String buildPatternSql(long gwId, List<Long> gwIdList, String bindingObjectId, List<String> bindingObjectIdList, List<String> bindingObjectTypeList, String pattern, Map<String, Object> params, String sql) {
        if (0 < gwId) {
            sql = sql + " and gw_id=:gwId";
            params.put("gwId", gwId);
        }
        if (StringUtils.isNotBlank(bindingObjectId)) {
            sql = sql + " and binding_object_id=:bindingObjectId";
            params.put("bindingObjectId", bindingObjectId);
        }
        if (!CollectionUtils.isEmpty(bindingObjectTypeList)) {
            sql = sql + " and binding_object_type in (:bindingObjectTypeList)";
            params.put("bindingObjectTypeList", bindingObjectTypeList);
        }
        if (StringUtils.isNotBlank(pattern)) {
            if (!CollectionUtils.isEmpty(gwIdList) && !CollectionUtils.isEmpty(bindingObjectIdList)) {
                sql = sql + " and (gw_id in (:gwIdList) or binding_object_id in (:bindingObjectIdList) or plugin_type like :pattern)";
                params.put("gwIdList", gwIdList);
                params.put("bindingObjectIdList", bindingObjectIdList);
                params.put("pattern", "%" + pattern + "%");
            } else if (!CollectionUtils.isEmpty(gwIdList) && CollectionUtils.isEmpty(bindingObjectIdList)) {
                sql = sql + " and (gw_id in (:gwIdList) or plugin_type like :pattern)";
                params.put("gwIdList", gwIdList);
                params.put("pattern", "%" + pattern + "%");
            } else if (CollectionUtils.isEmpty(gwIdList) && !CollectionUtils.isEmpty(bindingObjectIdList)) {
                sql = sql + " and (binding_object_id in (:bindingObjectIdList) or plugin_type like :pattern)";
                params.put("bindingObjectIdList", bindingObjectIdList);
                params.put("pattern", "%" + pattern + "%");
            } else if (CollectionUtils.isEmpty(gwIdList) && CollectionUtils.isEmpty(bindingObjectIdList)) {
                sql = sql + " and plugin_type like :pattern";
                params.put("pattern", "%" + pattern + "%");
            }
        }
        return sql;
    }

    @Override
    public List<EnvoyPluginBindingInfo> getBindingPluginList(long projectId, long gwId, List<Long> gwIdList, String bindingObjectId, List<String> bindingObjectIdList, List<String> bindingObjectTypeList, String pattern, long offset, long limit, String sortKey, String sortValue) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String sql = "select * from apigw_envoy_plugin_binding where project_id=:projectId";
        params.put("projectId", projectId);

        sql = buildPatternSql(gwId, gwIdList, bindingObjectId, bindingObjectIdList, bindingObjectTypeList, pattern, params, sql);

        sql = sql + " order by " + sortKey + " " + sortValue + " limit :limit offset :offset";
        params.put("limit", limit);
        params.put("offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginBindingInfoRowMapper());
    }

    @Override
    public long batchDeleteBindingInfo(List<EnvoyPluginBindingInfo> bindingInfoList) {
        if (CollectionUtils.isEmpty(bindingInfoList)) {
            return 0;
        }
        List<Long> bindingInfoIdList = bindingInfoList.stream().map(EnvoyPluginBindingInfo::getId).collect(Collectors.toList());
        String sql = "delete from apigw_envoy_plugin_binding where id in (:idList)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("idList", bindingInfoIdList);
        logger.info("批量删除插件绑定关系! envoyPluginBindingInfoList:{}", bindingInfoList);
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public long batchDissociateTemplate(List<Long> bindingInfoIdList) {
        String sql = "update apigw_envoy_plugin_binding set template_id = 0 and template_version = 0 where id in (:idList)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("idList", bindingInfoIdList);
        logger.info("批量解除插件关联的模板! envoyPluginBindingInfoList:{}", bindingInfoIdList);
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<EnvoyPluginBindingInfo> batchGetById(List<Long> bindingInfoIdList) {
        String sql = "select * from apigw_envoy_plugin_binding where id in (:idList)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("idList", bindingInfoIdList);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginBindingInfoRowMapper());
    }

    @Override
    public List<EnvoyPluginBindingInfo> getBindingPluginList(long projectId, long gwId, String bindingObjectType, List<String> bindingObjectIdList) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);

        String sql = "select * from apigw_envoy_plugin_binding where project_id=:projectId";
        if (0 < gwId) {
            params.put("gwId", gwId);
            sql = sql + " and gw_id=:gwId";
        }
        if (StringUtils.isNotBlank(bindingObjectType)) {
            params.put("bindingObjectType", bindingObjectType);
            sql = sql + " and binding_object_type=:bindingObjectType";
        }
        if (!CollectionUtils.isEmpty(bindingObjectIdList)) {
            sql = sql + " and binding_object_id in (:bindingObjectIdList) ";
            params.put("bindingObjectIdList", bindingObjectIdList);
        }
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginBindingInfoRowMapper());
    }

    class EnvoyPluginBindingInfoRowMapper implements RowMapper<EnvoyPluginBindingInfo> {
        @Override
        public EnvoyPluginBindingInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyPluginBindingInfo pluginBinding = new EnvoyPluginBindingInfo();
            pluginBinding.setId(rs.getLong("id"));
            pluginBinding.setGwId(rs.getLong("gw_id"));
            pluginBinding.setProjectId(rs.getLong("project_id"));
            pluginBinding.setCreateTime(rs.getLong("create_time"));
            pluginBinding.setUpdateTime(rs.getLong("update_time"));
            pluginBinding.setPluginType(rs.getString("plugin_type"));
            pluginBinding.setBindingStatus(rs.getString("binding_status"));
            pluginBinding.setPluginPriority(rs.getLong("plugin_priority"));
            pluginBinding.setBindingObjectId(rs.getString("binding_object_id"));
            pluginBinding.setBindingObjectType(rs.getString("binding_object_type"));
            pluginBinding.setPluginConfiguration(rs.getString("plugin_configuration"));
            pluginBinding.setTemplateId(rs.getLong("template_id"));
            pluginBinding.setTemplateVersion(rs.getLong("template_version"));
            return pluginBinding;
        }
    }
}
