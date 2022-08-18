package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.ITrafficMarkDao;
import org.hango.cloud.dashboard.envoy.meta.TrafficMarkInfo;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网关流量染色相关dao
 *
 * @author qilu
 */
@Component
public class TrafficMarkDaoImpl extends BaseDao implements ITrafficMarkDao {

    private static final Logger logger = LoggerFactory.getLogger(TrafficMarkDaoImpl.class);

    @Override
    public long add(TrafficMarkInfo trafficMarkInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_gportal_entry_traffic_policy (gw_id, gw_name, protocol, traffic_color_name, service_name, route_rule_names, create_time, update_time, route_rule_ids, enable_status, traffic_match, color_tag, param) "
                + " values (:gwId, :gwName, :protocol, :trafficColorName, :serviceName, :routeRuleNames, :createTime, :updateTime, :routeRuleIds, :enableStatus, :trafficMatch, :colorTag, :trafficParam)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(trafficMarkInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add TrafficMarkInfo: {}", trafficMarkInfo);
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(TrafficMarkInfo trafficMarkInfo) {
        String sql = "update apigw_gportal_entry_traffic_policy set gw_id=:gwId, gw_name=:gwName, update_time=:updateTime, route_rule_ids=:routeRuleIds, enable_status=:enableStatus, traffic_match=:trafficMatch, color_tag=:colorTag," +
                " param=:trafficParam, service_name=:serviceName, route_rule_names=:routeRuleNames, traffic_color_name=:trafficColorName where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(trafficMarkInfo);
        logger.info("update TrafficMarkInfo: {}", trafficMarkInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(TrafficMarkInfo trafficMarkInfo) {
        return 0;
    }

    @Override
    public void delete(long trafficColorRuleId) {
        String sql = "delete from apigw_gportal_entry_traffic_policy where id=:trafficColorRuleId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("trafficColorRuleId", trafficColorRuleId));
    }

    @Override
    public TrafficMarkInfo get(long id) {
        String sql = "select * from apigw_gportal_entry_traffic_policy where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new TrafficMarkDaoImpl.TrafficMarkRowMapper());
    }

    @Override
    public List<TrafficMarkInfo> findAll() {
        String sql = "select * from apigw_gportal_entry_traffic_policy";
        return namedParameterJdbcTemplate.query(sql, new TrafficMarkDaoImpl.TrafficMarkRowMapper());
    }

    @Override
    public List<TrafficMarkInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_entry_traffic_policy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new TrafficMarkDaoImpl.TrafficMarkRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_entry_traffic_policy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getCount() {
        String sql = "select count(*) from apigw_gportal_entry_traffic_policy";
        return namedParameterJdbcTemplate.queryForObject(sql, new HashMap<>(0), Long.class);
    }

    @Override
    public List<TrafficMarkInfo> getTrafficColorRulesByRouteRuleId(long routeRuleId) {
        String sql = "select * from apigw_gportal_entry_traffic_policy where route_rule_ids like :pattern";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pattern", "%" + routeRuleId + "%");
        List<TrafficMarkInfo> trafficMarkInfos = namedParameterJdbcTemplate.query(sql, params, new TrafficMarkRowMapper());
        // 因路由id为like匹配故需要recheck过滤
        return CollectionUtils.isEmpty(trafficMarkInfos) ? Collections.emptyList() :
                trafficMarkInfos.stream()
                        .filter(trafficMarkInfo -> isTrafficMarkMatchRouteRuleId(trafficMarkInfo, routeRuleId))
                        .collect(Collectors.toList());
    }

    private boolean isTrafficMarkMatchRouteRuleId(TrafficMarkInfo trafficMarkInfo, long routeRuleId) {
        if (trafficMarkInfo == null || StringUtils.isEmpty(trafficMarkInfo.getRouteRuleIds())) {
            return false;
        }
        for (String rId : trafficMarkInfo.getRouteRuleIds().split(",")) {
            if (rId.equals(String.valueOf(routeRuleId))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<TrafficMarkInfo> getTrafficColorByTagLimit(String colorTag, long offset, long limit) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String sql = "select * from apigw_gportal_entry_traffic_policy order by id desc limit :limit offset :offset";
        if (StringUtils.isNotBlank(colorTag)) {
            sql = "select * from apigw_gportal_entry_traffic_policy where color_tag=:colorTag order by id desc limit :limit offset :offset";
            params.put("colorTag", colorTag);
        }
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new TrafficMarkDaoImpl.TrafficMarkRowMapper());
    }

    class TrafficMarkRowMapper implements RowMapper<TrafficMarkInfo> {
        @Override
        public TrafficMarkInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            TrafficMarkInfo trafficMarkInfo = new TrafficMarkInfo();
            trafficMarkInfo.setId(rs.getLong("id"));
            trafficMarkInfo.setGwId(rs.getLong("gw_id"));
            trafficMarkInfo.setGwName(rs.getString("gw_name"));
            trafficMarkInfo.setCreateTime(rs.getLong("create_time"));
            trafficMarkInfo.setUpdateTime(rs.getLong("update_time"));
            trafficMarkInfo.setRouteRuleIds(rs.getString("route_rule_ids"));
            trafficMarkInfo.setEnableStatus(rs.getInt("enable_status"));
            trafficMarkInfo.setTrafficMatch(rs.getString("traffic_match"));
            trafficMarkInfo.setColorTag(rs.getString("color_tag"));
            trafficMarkInfo.setTrafficParam(rs.getString("param"));
            trafficMarkInfo.setTrafficColorName(rs.getString("traffic_color_name"));
            trafficMarkInfo.setServiceName(rs.getString("service_name"));
            trafficMarkInfo.setRouteRuleNames(rs.getString("route_rule_names"));
            trafficMarkInfo.setProtocol(rs.getString("protocol"));
            return trafficMarkInfo;
        }
    }
}
