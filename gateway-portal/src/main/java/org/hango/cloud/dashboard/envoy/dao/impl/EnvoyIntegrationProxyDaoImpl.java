package org.hango.cloud.dashboard.envoy.dao.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyIntegrationProxyDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyIntegrationProxyInfo;
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
public class EnvoyIntegrationProxyDaoImpl extends BaseDao implements IEnvoyIntegrationProxyDao {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyIntegrationProxyDaoImpl.class);

    @Override
    public long add(EnvoyIntegrationProxyInfo integrationProxyInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_integration_proxy (integration_id, gw_id, metadata) " +
                "values (:integrationId, :gwId, :metadataStr)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(integrationProxyInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add integrationProxyInfo: {}", ReflectionToStringBuilder.toString(integrationProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public long update(EnvoyIntegrationProxyInfo integrationProxyInfo) {
        String sql = "update apigw_envoy_integration_proxy set integration_id=:integrationId, metadata=:metadata ,gw_id=:gwId where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(integrationProxyInfo);
        logger.info("update integrationProxyInfo: {}", ReflectionToStringBuilder.toString(integrationProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyIntegrationProxyInfo integrationProxyInfo) {
        String sql = "delete from apigw_envoy_integration_proxy where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(integrationProxyInfo);
        logger.info("delete integrationProxyInfo: {}", ReflectionToStringBuilder.toString(integrationProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyIntegrationProxyInfo get(long id) {
        String sql = "select * from apigw_envoy_integration_proxy where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyIntegrationProxyRowMapper());
    }

    @Override
    public List<EnvoyIntegrationProxyInfo> findAll() {
        String sql = "select * from apigw_envoy_integration_proxy";
        return namedParameterJdbcTemplate.query(sql, new EnvoyIntegrationProxyRowMapper());
    }

    @Override
    public List<EnvoyIntegrationProxyInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_integration_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyIntegrationProxyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_integration_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public EnvoyIntegrationProxyInfo getByIntegrationId(long integrationId) {
        String sql = "select * from apigw_envoy_integration_proxy where integration_id=:integrationId";
        return queryForObject(sql, new MapSqlParameterSource("integrationId", integrationId), new EnvoyIntegrationProxyRowMapper());
    }

    class EnvoyIntegrationProxyRowMapper implements RowMapper<EnvoyIntegrationProxyInfo> {
        @Override
        public EnvoyIntegrationProxyInfo mapRow(ResultSet rs, int i) throws SQLException {
            EnvoyIntegrationProxyInfo integrationProxyInfo = new EnvoyIntegrationProxyInfo();
            integrationProxyInfo.setId(rs.getLong("id"));
            integrationProxyInfo.setIntegrationId(rs.getLong("integration_id"));
            integrationProxyInfo.setGwId(rs.getLong("gw_id"));
            integrationProxyInfo.setMetadata(JSON.parseObject(rs.getString("metadata")));
            return integrationProxyInfo;
        }
    }
}
