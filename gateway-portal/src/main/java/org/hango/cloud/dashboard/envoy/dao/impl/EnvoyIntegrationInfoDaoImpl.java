package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationInfo;
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
 * 集成Dao实现类
 */
@Component
public class EnvoyIntegrationInfoDaoImpl extends BaseDao implements IEnvoyIntegrationInfoDao {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationInfoDaoImpl.class);

    @Override
    public long add(EnvoyIntegrationInfo envoyIntegrationInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_integration_info (integration_name, update_time, create_time, project_id, description) " +
                "values (:integrationName, :updateTime, :createTime, :projectId, :description)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyIntegrationInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public long update(EnvoyIntegrationInfo envoyIntegrationInfo) {
        String sql = "update apigw_envoy_integration_info set integration_name=:integrationName, publish_status=:publishStatus, publish_time=:publishTime, " +
                "update_time=:updateTime, create_time=:createTime, project_id=:projectId, description=:description, step=:step, type=:type where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationInfo);
        logger.info("update envoyIntegrationInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyIntegrationInfo envoyIntegrationInfo) {
        String sql = "delete from apigw_envoy_integration_info where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationInfo);
        logger.info("delete envoyIntegrationInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyIntegrationInfo get(long id) {
        String sql = "select * from apigw_envoy_integration_info where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyIntegrationInfoRowMapper());
    }

    @Override
    public List<EnvoyIntegrationInfo> findAll() {
        String sql = "select * from apigw_envoy_integration_info";
        return namedParameterJdbcTemplate.query(sql, new EnvoyIntegrationInfoRowMapper());
    }

    @Override
    public List<EnvoyIntegrationInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_integration_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyIntegrationInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_integration_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getIntegrationInfoCount(long projectId, String pattern, String type) {
        String sql;
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);
        params.put("type", type);

        if (StringUtils.isNotBlank(pattern)) {
            sql = "select count(*) from apigw_envoy_integration_info where integration_name like :pattern and project_id=:projectId and type=:type";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select count(*) from apigw_envoy_integration_info where project_id=:projectId and type=:type";
        }
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getIntegrationInfoCount(long projectId, String pattern) {
        String sql;
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);

        if (StringUtils.isNotBlank(pattern)) {
            sql = "select count(*) from apigw_envoy_integration_info where integration_name like :pattern and project_id=:projectId";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select count(*) from apigw_envoy_integration_info where project_id=:projectId";
        }
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByLimit(long projectId, String pattern, long offset, long limit, String type) {
        String sql;
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);

        if (StringUtils.isNotBlank(pattern)) {
            sql = "select id,integration_name,publish_status,publish_time,update_time,create_time,description,step,type from apigw_envoy_integration_info where integration_name like :pattern " +
                    "and project_id=:projectId and type=:type order by create_time desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select id,integration_name,publish_status,publish_time,update_time,create_time,description,step,type from apigw_envoy_integration_info where project_id=:projectId and " +
                    "type=:type order by create_time desc limit :limit offset :offset";
        }
        params.put("projectId", projectId);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("type", type);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyIntegrationInfoListRowMapper());
    }

    @Override
    public List<EnvoyIntegrationInfo> getEnvoyIntegrationInfoByLimit(long projectId, String pattern, long offset, long limit) {
        String sql;
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);

        if (StringUtils.isNotBlank(pattern)) {
            sql = "select id,integration_name,publish_status,publish_time,update_time,create_time,description,step,type from apigw_envoy_integration_info where integration_name " +
                    "like :pattern and project_id=:projectId order by create_time desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select id,integration_name,publish_status,publish_time,update_time,create_time,description,step,type from apigw_envoy_integration_info where " +
                    "project_id=:projectId order by create_time desc limit :limit offset :offset";
        }
        params.put("projectId", projectId);
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyIntegrationInfoListRowMapper());
    }

    @Override
    public List<EnvoyIntegrationInfo> getByIdlist(Map<String, Long> idMap) {
        String head = "select * from apigw_envoy_integration_info where ";
        StringBuilder sb = new StringBuilder(head);
        boolean flag = false;
        for (Map.Entry<String, Long> stringObjectEntry : idMap.entrySet()) {
            if (!flag) {
                flag = !flag;
            } else {
                sb.append(" or ");
            }
            Map.Entry<String, Long> entry = stringObjectEntry;
            sb.append("id").append(" = ").append(":").append(entry.getKey());
        }
        String sql = sb.toString();
        return namedParameterJdbcTemplate.query(sql, idMap, new EnvoyIntegrationInfoRowMapper());
    }

    class EnvoyIntegrationInfoRowMapper implements RowMapper<EnvoyIntegrationInfo> {
        @Override
        public EnvoyIntegrationInfo mapRow(ResultSet rs, int i) throws SQLException {
            EnvoyIntegrationInfo info = new EnvoyIntegrationInfo();

            info.setId(rs.getLong("id"));
            info.setIntegrationName(rs.getString("integration_name"));
            info.setPublishStatus(rs.getInt("publish_status"));
            info.setPublishTime(rs.getLong("publish_time"));
            info.setUpdateTime(rs.getLong("update_time"));
            info.setCreateTime(rs.getLong("create_time"));
            info.setProjectId(rs.getLong("project_id"));
            info.setDescription(rs.getString("description"));
            info.setStep(rs.getString("step"));
            info.setType(rs.getString("type"));
            return info;
        }
    }

    class EnvoyIntegrationInfoListRowMapper implements RowMapper<EnvoyIntegrationInfo> {
        @Override
        public EnvoyIntegrationInfo mapRow(ResultSet rs, int i) throws SQLException {
            EnvoyIntegrationInfo info = new EnvoyIntegrationInfo();

            info.setId(rs.getLong("id"));
            info.setIntegrationName(rs.getString("integration_name"));
            info.setDescription(rs.getString("description"));
            info.setPublishStatus(rs.getInt("publish_status"));
            info.setPublishTime(rs.getLong("publish_time"));
            info.setUpdateTime(rs.getLong("update_time"));
            info.setCreateTime(rs.getLong("create_time"));
            info.setStep(rs.getString("step"));
            info.setType(rs.getString("type"));
            return info;
        }
    }
}
