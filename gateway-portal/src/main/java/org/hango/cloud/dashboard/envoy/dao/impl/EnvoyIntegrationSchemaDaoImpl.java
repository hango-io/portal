package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationSchemaDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationSchemaInfo;
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

@Component
public class EnvoyIntegrationSchemaDaoImpl extends BaseDao implements IEnvoyIntegrationSchemaDao {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationSchemaDaoImpl.class);

    @Override
    public long add(EnvoyIntegrationSchemaInfo envoyIntegrationSchemaInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_integration_schema (id,category,kind,name,description,schema) " +
                "values (:id, :category, :kind, :name, :description, :schema)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationSchemaInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyIntegrationSchemaInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationSchemaInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public long update(EnvoyIntegrationSchemaInfo envoyIntegrationSchemaInfo) {
        String sql = "update apigw_envoy_integration_schema set category=:category, kind=:kind, name=:name, description=:description," +
                " schema=:schema where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationSchemaInfo);
        logger.info("update envoyIntegrationSchemaInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationSchemaInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyIntegrationSchemaInfo envoyIntegrationSchemaInfo) {
        String sql = "delete from apigw_envoy_integration_schema where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyIntegrationSchemaInfo);
        logger.info("delete envoyIntegrationSchemaInfo: {}", ReflectionToStringBuilder.toString(envoyIntegrationSchemaInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyIntegrationSchemaInfo get(long id) {
        String sql = "select * from apigw_envoy_integration_schema where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyIntegrationSchemaRowMapper());
    }

    @Override
    public List<EnvoyIntegrationSchemaInfo> findAll() {
        String sql = "select * from apigw_envoy_integration_schema";
        return namedParameterJdbcTemplate.query(sql, new EnvoyIntegrationSchemaRowMapper());
    }

    @Override
    public List<EnvoyIntegrationSchemaInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_integration_schema where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyIntegrationSchemaRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_integration_schema where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<EnvoyIntegrationSchemaInfo> getSchemaKindList() {
        String sql = "SELECT category,kind,name,description FROM apigw_envoy_integration_schema";
        return namedParameterJdbcTemplate.query(sql, new EnvoyIntegrationSchemaListRowMapper());
    }

    @Override
    public EnvoyIntegrationSchemaInfo getSchemaByKind(String schemaKind) {
        String sql = "SELECT * FROM apigw_envoy_integration_schema where kind=:kind";
        return queryForObject(sql, new MapSqlParameterSource("kind", schemaKind), new EnvoyIntegrationSchemaRowMapper());
    }

    class EnvoyIntegrationSchemaRowMapper implements RowMapper<EnvoyIntegrationSchemaInfo> {
        @Override
        public EnvoyIntegrationSchemaInfo mapRow(ResultSet rs, int i) throws SQLException {
            EnvoyIntegrationSchemaInfo info = new EnvoyIntegrationSchemaInfo();
            info.setId(rs.getLong("id"));
            info.setCategory(rs.getString("category"));
            info.setKind(rs.getString("kind"));
            info.setName(rs.getString("name"));
            info.setDescription(rs.getString("description"));
            info.setSchema(rs.getString("schema"));
            return info;
        }
    }

    class EnvoyIntegrationSchemaListRowMapper implements RowMapper<EnvoyIntegrationSchemaInfo> {
        @Override
        public EnvoyIntegrationSchemaInfo mapRow(ResultSet rs, int i) throws SQLException {
            EnvoyIntegrationSchemaInfo info = new EnvoyIntegrationSchemaInfo();
            info.setCategory(rs.getString("category"));
            info.setKind(rs.getString("kind"));
            info.setName(rs.getString("name"));
            info.setDescription(rs.getString("description"));
            return info;
        }
    }
}
