package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyPluginTemplateDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginTemplateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件模板dao层实现类
 *
 * @author hzchenzhongyang 2020-04-08
 */
@Component
public class EnvoyPluginTemplateDaoImpl extends BaseDao implements IEnvoyPluginTemplateDao {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyPluginBindingInfoDaoImpl.class);

    @Override
    public long add(EnvoyPluginTemplateInfo envoyPluginTemplateInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_plugin_template (create_time, update_time, plugin_type, plugin_configuration, project_id, template_version, template_name, template_notes) "
                + " values (:createTime, :updateTime, :pluginType, :pluginConfiguration, :projectId, :templateVersion, :templateName, :templateNotes)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginTemplateInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyPluginTemplateInfo: {}", ReflectionToStringBuilder.toString(envoyPluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(EnvoyPluginTemplateInfo envoyPluginTemplateInfo) {
        String sql = "update apigw_envoy_plugin_template set update_time=:updateTime, "
                + "plugin_configuration=:pluginConfiguration, template_version=:templateVersion, "
                + "template_name=:templateName, template_notes=:templateNotes, plugin_type=:pluginType where "
                + "id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginTemplateInfo);
        logger.info("update envoyPluginTemplateInfo: {}", ReflectionToStringBuilder.toString(envoyPluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyPluginTemplateInfo envoyPluginTemplateInfo) {
        String sql = "delete from apigw_envoy_plugin_template where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyPluginTemplateInfo);
        logger.info("delete envoyPluginTemplateInfo: {}", ReflectionToStringBuilder.toString(envoyPluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyPluginTemplateInfo get(long id) {
        String sql = "select * from apigw_envoy_plugin_template where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyPluginTemplateInfoRowMapper());
    }

    @Override
    public List<EnvoyPluginTemplateInfo> findAll() {
        String sql = "select * from apigw_envoy_plugin_template";
        return namedParameterJdbcTemplate.query(sql, new EnvoyPluginTemplateInfoRowMapper());
    }

    @Override
    public List<EnvoyPluginTemplateInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_plugin_template where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginTemplateInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_plugin_template where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<EnvoyPluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String sql = "select * from apigw_envoy_plugin_template where project_id=:projectId";
        if (StringUtils.isNotBlank(pluginType)) {
            sql = sql + " and plugin_type=:pluginType ";
            params.put("pluginType", pluginType);
        }
        sql = sql + " order by id desc limit :limit offset :offset";
        params.put("projectId", projectId);
        params.put("limit", limit);
        params.put("offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginTemplateInfoRowMapper());
    }

    @Override
    public long getPluginTemplateInfoCount(long projectId) {
        return 0;
    }

    @Override
    public List<EnvoyPluginTemplateInfo> batchGet(List<Long> templateIdList) {
        String sql = "select * from apigw_envoy_plugin_template where id in (:templateIdList)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("templateIdList", templateIdList);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyPluginTemplateInfoRowMapper());
    }

    class EnvoyPluginTemplateInfoRowMapper implements RowMapper<EnvoyPluginTemplateInfo> {
        @Override
        public EnvoyPluginTemplateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyPluginTemplateInfo templateInfo = new EnvoyPluginTemplateInfo();
            templateInfo.setId(rs.getLong("id"));
            templateInfo.setTemplateNotes(rs.getString("template_notes"));
            templateInfo.setProjectId(rs.getLong("project_id"));
            templateInfo.setCreateTime(rs.getLong("create_time"));
            templateInfo.setUpdateTime(rs.getLong("update_time"));
            templateInfo.setPluginType(rs.getString("plugin_type"));
            templateInfo.setTemplateName(rs.getString("template_name"));
            templateInfo.setTemplateVersion(rs.getLong("template_version"));
            templateInfo.setPluginConfiguration(rs.getString("plugin_configuration"));
            return templateInfo;
        }
    }

}
