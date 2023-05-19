package org.hango.cloud.common.infra.plugin.dao.impl;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
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
import java.util.List;
import java.util.Map;

/**
 * 插件模板dao层实现类
 *
 * @author hzchenzhongyang 2020-04-08
 */
@Component
public class PluginTemplateDaoImpl extends BaseDao implements IPluginTemplateDao {
    private static final Logger logger = LoggerFactory.getLogger(PluginBindingInfoDaoImpl.class);

    @Override
    public long add(PluginTemplateInfo pluginTemplateInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_plugin_template (create_time, update_time, plugin_type, plugin_name, plugin_configuration, project_id, template_version, template_name, template_notes) "
                + " values (:createTime, :updateTime, :pluginType, :pluginName, :pluginConfiguration, :projectId, :templateVersion, :templateName, :templateNotes)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginTemplateInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add pluginTemplateInfo: {}", ReflectionToStringBuilder.toString(pluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(PluginTemplateInfo pluginTemplateInfo) {
        String sql = "update hango_plugin_template set update_time=:updateTime, "
                + "plugin_configuration=:pluginConfiguration, template_version=:templateVersion, "
                + "template_name=:templateName, template_notes=:templateNotes, plugin_type=:pluginType where "
                + "id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginTemplateInfo);
        logger.info("update pluginTemplateInfo: {}", ReflectionToStringBuilder.toString(pluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(PluginTemplateInfo pluginTemplateInfo) {
        String sql = "delete from hango_plugin_template where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginTemplateInfo);
        logger.info("delete pluginTemplateInfo: {}", ReflectionToStringBuilder.toString(pluginTemplateInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public PluginTemplateInfo get(long id) {
        String sql = "select * from hango_plugin_template where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new PluginTemplateInfoRowMapper());
    }

    @Override
    public List<PluginTemplateInfo> findAll() {
        String sql = "select * from hango_plugin_template";
        return namedParameterJdbcTemplate.query(sql, new PluginTemplateInfoRowMapper());
    }

    @Override
    public List<PluginTemplateInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_plugin_template where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new PluginTemplateInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_plugin_template where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<PluginTemplateInfo> getPluginTemplateInfoList(long projectId, String pluginType, long offset, long limit) {
        Map<String, Object> params = Maps.newHashMap();
        String sql = "select * from hango_plugin_template where project_id=:projectId";
        if (StringUtils.isNotBlank(pluginType)) {
            sql = sql + " and plugin_type=:pluginType ";
            params.put("pluginType", pluginType);
        }
        sql = sql + " order by id desc limit :limit offset :offset";
        params.put("projectId", projectId);
        params.put("limit", limit);
        params.put("offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, new PluginTemplateInfoRowMapper());
    }

    @Override
    public long getPluginTemplateInfoCount(long projectId) {
        return 0;
    }

    @Override
    public List<PluginTemplateInfo> batchGet(List<Long> templateIdList) {
        String sql = "select * from hango_plugin_template where id in (:templateIdList)";
        Map<String, Object> params = Maps.newHashMap();
        params.put("templateIdList", templateIdList);
        return namedParameterJdbcTemplate.query(sql, params, new PluginTemplateInfoRowMapper());
    }

    class PluginTemplateInfoRowMapper implements RowMapper<PluginTemplateInfo> {
        @Override
        public PluginTemplateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PluginTemplateInfo templateInfo = new PluginTemplateInfo();
            templateInfo.setId(rs.getLong("id"));
            templateInfo.setTemplateNotes(rs.getString("template_notes"));
            templateInfo.setProjectId(rs.getLong("project_id"));
            templateInfo.setCreateTime(rs.getLong("create_time"));
            templateInfo.setUpdateTime(rs.getLong("update_time"));
            templateInfo.setPluginType(rs.getString("plugin_type"));
            templateInfo.setPluginName(rs.getString("plugin_name"));
            templateInfo.setTemplateName(rs.getString("template_name"));
            templateInfo.setTemplateVersion(rs.getLong("template_version"));
            templateInfo.setPluginConfiguration(rs.getString("plugin_configuration"));
            return templateInfo;
        }
    }

}
