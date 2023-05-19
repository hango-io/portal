package org.hango.cloud.common.infra.plugin.dao.impl;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.gdashboard.api.util.Const;
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
public class PluginBindingInfoDaoImpl extends BaseDao implements IPluginBindingInfoDao {
    private static final Logger logger = LoggerFactory.getLogger(PluginBindingInfoDaoImpl.class);

    private static final String ID_LIST = "idList";

    @Override
    public long add(PluginBindingInfo pluginBindingInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_plugin_binding (plugin_type, plugin_name, binding_object_id, binding_object_type, plugin_configuration, create_time, update_time, virtual_gw_id, project_id, binding_status, template_id, template_version, gw_type)" + " values (:pluginType, :pluginName, :bindingObjectId, :bindingObjectType, :pluginConfiguration, :createTime, :updateTime, :virtualGwId, :projectId, :bindingStatus, :templateId, :templateVersion, :gwType)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginBindingInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add pluginBindingInfo: {}", ReflectionToStringBuilder.toString(pluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(PluginBindingInfo pluginBindingInfo) {
        String sql = "update hango_plugin_binding set plugin_configuration=:pluginConfiguration, " + "binding_status=:bindingStatus, update_time=:updateTime, template_id=:templateId, " + "template_version=:templateVersion, plugin_type = :pluginType, gw_type = :gwType where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginBindingInfo);
        logger.info("update pluginBindingInfo: {}", ReflectionToStringBuilder.toString(pluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(PluginBindingInfo pluginBindingInfo) {
        String sql = "delete from hango_plugin_binding where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pluginBindingInfo);
        logger.info("delete pluginBindingInfo: {}", ReflectionToStringBuilder.toString(pluginBindingInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public PluginBindingInfo get(long id) {
        String sql = "select * from hango_plugin_binding where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new PluginBindingInfoRowMapper());
    }

    @Override
    public List<PluginBindingInfo> findAll() {
        String sql = "select * from hango_plugin_binding";
        return namedParameterJdbcTemplate.query(sql, new PluginBindingInfoRowMapper());
    }

    @Override
    public List<PluginBindingInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_plugin_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new PluginBindingInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_plugin_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    @Override
    public long updateVersion(long id, long version) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("id", id);
        params.put("version", version);
        String sql = "update hango_plugin_binding set version =:version where id =:id";
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public long getBindingPluginCount(long projectId, long virtualGwId, String bindingObjectId, String bindingObjectType, String pattern, List<String> excludedPluginTypeList) {
        Map<String, Object> params = Maps.newHashMap();
        String sql = "select count(*) from hango_plugin_binding where project_id=:projectId";
        params.put(BaseConst.PROJECT_ID, projectId);

        // 排除指定插件类型（例如内部使用插件）
        if (!CollectionUtils.isEmpty(excludedPluginTypeList)) {
            sql += " and plugin_type not in (:excludedPluginTypeList)";
            params.put("excludedPluginTypeList", excludedPluginTypeList);
        }

        sql = buildPatternSql(virtualGwId, bindingObjectId, bindingObjectType, pattern, params, sql);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    private String buildPatternSql(long virtualGwId, String bindingObjectId, String bindingObjectType, String pattern, Map<String, Object> params, String sql) {
        if (0 < virtualGwId) {
            sql = sql + " and virtual_gw_id=:virtualGwId";
            params.put("virtualGwId", virtualGwId);
        }
        if (StringUtils.isNotBlank(bindingObjectId)) {
            sql = sql + " and binding_object_id=:bindingObjectId";
            params.put("bindingObjectId", bindingObjectId);
        }
        if (StringUtils.isNotBlank(bindingObjectType)) {
            sql = sql + " and binding_object_type=:bindingObjectType";
            params.put("bindingObjectType", bindingObjectType);
        }
        if (StringUtils.isNotBlank(pattern)) {
            sql = sql + " and plugin_type like :pattern";
            params.put("pattern", "%" + pattern + "%");
        }
        return sql;
    }

    @Override
    public List<PluginBindingInfo> getBindingPluginList(long projectId, long virtualGwId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue) {
        Map<String, Object> params = Maps.newHashMap();
        String sql = "select * from hango_plugin_binding where project_id=:projectId";
        params.put(BaseConst.PROJECT_ID, projectId);

        sql = buildPatternSql(virtualGwId, bindingObjectId, bindingObjectType, pattern, params, sql);

        sql = sql + " order by " + sortKey + " " + sortValue + " limit :limit offset :offset";
        params.put("limit", limit);
        params.put("offset", offset);
        return namedParameterJdbcTemplate.query(sql, params, new PluginBindingInfoRowMapper());
    }

    @Override
    public long batchDeleteBindingInfo(List<PluginBindingDto> bindingInfoList) {
        if (CollectionUtils.isEmpty(bindingInfoList)) {
            return 0;
        }
        List<Long> bindingInfoIdList = bindingInfoList.stream().map(PluginBindingDto::getId).collect(Collectors.toList());
        String sql = "delete from hango_plugin_binding where id in (:idList)";
        Map<String, Object> params = Maps.newHashMap();
        params.put(ID_LIST, bindingInfoIdList);
        logger.info("批量删除插件绑定关系! pluginBindingInfoList:{}", bindingInfoList);
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public long batchDissociateTemplate(List<Long> bindingInfoIdList) {
        String sql = "update hango_plugin_binding set template_id = 0 and template_version = 0 where id in (:idList)";
        Map<String, Object> params = Maps.newHashMap();
        params.put(ID_LIST, bindingInfoIdList);
        logger.info("批量解除插件关联的模板! pluginBindingInfoList:{}", bindingInfoIdList);
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public List<PluginBindingInfo> batchGetById(List<Long> bindingInfoIdList) {
        String sql = "select * from hango_plugin_binding where id in (:idList)";
        Map<String, Object> params = Maps.newHashMap();
        params.put(ID_LIST, bindingInfoIdList);
        return namedParameterJdbcTemplate.query(sql, params, new PluginBindingInfoRowMapper());
    }

    @Override
    public List<PluginBindingInfo> getBindingPluginList(long projectId, long virtualGwId, String bindingObjectType, List<String> bindingObjectIdList) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.PROJECT_ID, projectId);

        String sql = "select * from hango_plugin_binding where project_id=:projectId";
        if (0 < virtualGwId) {
            params.put("virtualGwId", virtualGwId);
            sql = sql + " and virtual_gw_id=:virtualGwId";
        }
        if (StringUtils.isNotBlank(bindingObjectType)) {
            params.put("bindingObjectType", bindingObjectType);
            sql = sql + " and binding_object_type=:bindingObjectType";
        }
        if (!CollectionUtils.isEmpty(bindingObjectIdList)) {
            sql = sql + " and binding_object_id in (:bindingObjectIdList) ";
            params.put("bindingObjectIdList", bindingObjectIdList);
        }
        return namedParameterJdbcTemplate.query(sql, params, new PluginBindingInfoRowMapper());
    }

    class PluginBindingInfoRowMapper implements RowMapper<PluginBindingInfo> {
        @Override
        public PluginBindingInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PluginBindingInfo pluginBinding = new PluginBindingInfo();
            pluginBinding.setId(rs.getLong("id"));
            pluginBinding.setVirtualGwId(rs.getLong("virtual_gw_id"));
            pluginBinding.setProjectId(rs.getLong("project_id"));
            pluginBinding.setCreateTime(rs.getLong("create_time"));
            pluginBinding.setUpdateTime(rs.getLong("update_time"));
            pluginBinding.setPluginType(rs.getString("plugin_type"));
            pluginBinding.setPluginName(rs.getString("plugin_name"));
            pluginBinding.setBindingStatus(rs.getString("binding_status"));
            pluginBinding.setBindingObjectId(rs.getString("binding_object_id"));
            pluginBinding.setBindingObjectType(rs.getString("binding_object_type"));
            pluginBinding.setPluginConfiguration(rs.getString("plugin_configuration"));
            pluginBinding.setTemplateId(rs.getLong("template_id"));
            pluginBinding.setTemplateVersion(rs.getLong("template_version"));
            pluginBinding.setGwType(rs.getString("gw_type"));
            pluginBinding.setVersion(rs.getLong("version"));
            return pluginBinding;
        }
    }
}
