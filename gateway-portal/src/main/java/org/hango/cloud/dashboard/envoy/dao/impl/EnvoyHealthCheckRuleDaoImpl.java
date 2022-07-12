package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.envoy.dao.EnvoyHealthCheckRuleDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
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
 * 健康检查DAO
 *
 * @author TC_WANG
 * @date 2019/11/19 下午4:23.
 */
@Component
public class EnvoyHealthCheckRuleDaoImpl extends BaseDao implements EnvoyHealthCheckRuleDao {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckRuleDaoImpl.class);

    @Override
    public long add(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_health_check_rule (create_time, update_time, service_id, gw_id, active_switch, path, timeout, expected_statuses, healthy_interval, healthy_threshold, " +
                "unhealthy_interval, unhealthy_threshold, passive_switch, consecutive_errors, base_ejection_time, max_ejection_percent, min_health_percent) values(:createTime, :updateTime, :serviceId, :gwId, :activeSwitch," +
                ":path, :timeout, :expectedStatuses, :healthyInterval, :healthyThreshold, :unhealthyInterval, :unhealthyThreshold, :passiveSwitch, :consecutiveErrors, :baseEjectionTime, :maxEjectionPercent, :minHealthPercent)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyHealthCheckRuleInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyHealthCheckRule: {}", ReflectionToStringBuilder.toString(envoyHealthCheckRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        String sql = "update apigw_envoy_health_check_rule set update_time=:updateTime, service_id=:serviceId, gw_id=:gwId, active_switch=:activeSwitch, path=:path, " +
                "timeout=:timeout, expected_statuses=:expectedStatuses, healthy_interval=:healthyInterval, healthy_threshold=:healthyThreshold, unhealthy_interval=:unhealthyInterval," +
                "unhealthy_threshold=:unhealthyThreshold, passive_switch=:passiveSwitch, consecutive_errors=:consecutiveErrors, base_ejection_time=:baseEjectionTime, max_ejection_percent=:maxEjectionPercent, min_health_percent=:minHealthPercent where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyHealthCheckRuleInfo);
        logger.info("update envoyHealthCheckRule: {}", ReflectionToStringBuilder.toString(envoyHealthCheckRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        String sql = "delete from apigw_envoy_health_check_rule where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyHealthCheckRuleInfo);
        logger.info("delete envoyHealthCheckRule: {}", ReflectionToStringBuilder.toString(envoyHealthCheckRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int deleteByServiceId(long serviceId) {
        String sql = "delete from apigw_envoy_health_check_rule where service_id=:serviceId";
        SqlParameterSource ps = new MapSqlParameterSource("serviceId", serviceId);
        logger.info("delete envoyHealthCheckRules by serviceId: {}", serviceId);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyHealthCheckRuleInfo get(long id) {
        String sql = "select * from apigw_envoy_health_check_rule where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyHealthCheckRulesRowMapper());
    }

    @Override
    public List<EnvoyHealthCheckRuleInfo> findAll() {
        String sql = "select * from apigw_envoy_health_check_rule";
        return namedParameterJdbcTemplate.query(sql, new EnvoyHealthCheckRulesRowMapper());
    }

    @Override
    public List<EnvoyHealthCheckRuleInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_health_check_rule where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyHealthCheckRulesRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_health_check_rule where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class EnvoyHealthCheckRulesRowMapper implements RowMapper<EnvoyHealthCheckRuleInfo> {
        @Override
        public EnvoyHealthCheckRuleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo = new EnvoyHealthCheckRuleInfo();
            envoyHealthCheckRuleInfo.setId(rs.getLong("id"));
            envoyHealthCheckRuleInfo.setCreateTime(rs.getLong("create_time"));
            envoyHealthCheckRuleInfo.setUpdateTime(rs.getLong("update_time"));
            envoyHealthCheckRuleInfo.setServiceId(rs.getLong("service_id"));
            envoyHealthCheckRuleInfo.setGwId(rs.getLong("gw_id"));
            envoyHealthCheckRuleInfo.setActiveSwitch(rs.getInt("active_switch"));
            envoyHealthCheckRuleInfo.setPath(rs.getString("path"));
            envoyHealthCheckRuleInfo.setTimeout(rs.getInt("timeout"));
            envoyHealthCheckRuleInfo.setExpectedStatuses(rs.getString("expected_statuses"));
            envoyHealthCheckRuleInfo.setHealthyInterval(rs.getInt("healthy_interval"));
            envoyHealthCheckRuleInfo.setHealthyThreshold(rs.getInt("healthy_threshold"));
            envoyHealthCheckRuleInfo.setUnhealthyInterval(rs.getInt("unhealthy_interval"));
            envoyHealthCheckRuleInfo.setUnhealthyThreshold(rs.getInt("unhealthy_threshold"));
            envoyHealthCheckRuleInfo.setPassiveSwitch(rs.getInt("passive_switch"));
            envoyHealthCheckRuleInfo.setConsecutiveErrors(rs.getInt("consecutive_errors"));
            envoyHealthCheckRuleInfo.setBaseEjectionTime(rs.getInt("base_ejection_time"));
            envoyHealthCheckRuleInfo.setMaxEjectionPercent(rs.getInt("max_ejection_percent"));
            envoyHealthCheckRuleInfo.setMinHealthPercent(rs.getInt("min_health_percent"));
            return envoyHealthCheckRuleInfo;
        }
    }
}
