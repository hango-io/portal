package org.hango.cloud.dashboard.envoy.dao.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyDestinationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleMapMatchInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteStringMatchInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.HttpRetryDto;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由规则发布信息Dao层实现类
 *
 * @author hzchenzhongyang 2019-09-18
 */
@Component
public class RouteRuleProxyDaoImpl extends BaseDao implements IRouteRuleProxyDao {

    private static final Logger logger = LoggerFactory.getLogger(RouteRuleProxyDaoImpl.class);

    @Override
    public long add(RouteRuleProxyInfo routeRuleProxyInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_route_rule_proxy (route_rule_id, gw_id, destination_services, service_id, priority, orders, enable_state, create_time, update_time, project_id, hosts, timeout, http_retry, uri, method, host, header, query_param, virtual_cluster, mirror_traffic, mirror_service_id, gw_type, need_route_metric) "
                + " values (:routeRuleId, :gwId, :destinationServices, :serviceId, :priority, :orders, :enableState, :createTime, :updateTime, :projectId, :hosts, :timeout, :httpRetry, :uri, :method, :host, :header, :queryParam, :virtualCluster, :mirrorTraffic, :mirrorServiceId, :gwType,:needRouteMetric)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleProxyInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyRouteRuleProxyInfo: {}", ReflectionToStringBuilder.toString(routeRuleProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(RouteRuleProxyInfo routeRuleProxyInfo) {
        String sql = "update apigw_route_rule_proxy set update_time=:updateTime, destination_services=:destinationServices, service_id=:serviceId, enable_state=:enableState, hosts=:hosts, timeout=:timeout," +
                "http_retry=:httpRetry, uri=:uri, method=:method, host=:host, header=:header, query_param=:queryParam, priority=:priority, orders=:orders, virtual_cluster=:virtualCluster, mirror_traffic=:mirrorTraffic, mirror_service_id=:mirrorServiceId , gw_type=:gwType, need_route_metric=:needRouteMetric where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleProxyInfo);
        logger.info("update envoyRouteRuleProxyInfo: {}", ReflectionToStringBuilder.toString(routeRuleProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(RouteRuleProxyInfo routeRuleProxyInfo) {
        String sql = "delete from apigw_route_rule_proxy where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleProxyInfo);
        logger.info("delete envoyRouteRuleProxyInfo: {}", ReflectionToStringBuilder.toString(routeRuleProxyInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public RouteRuleProxyInfo get(long id) {
        String sql = "select * from apigw_route_rule_proxy where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyRouteRuleProxyInfoRowMapper());
    }

    @Override
    public List<RouteRuleProxyInfo> findAll() {
        String sql = "select * from apigw_route_rule_proxy";
        return namedParameterJdbcTemplate.query(sql, new EnvoyRouteRuleProxyInfoRowMapper());
    }

    @Override
    public List<RouteRuleProxyInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_route_rule_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleProxyInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_route_rule_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, long projectId, String sortKey, String sortValue, long offset, long limit) {
        return getRouteRuleProxyList(gwId, serviceId, Collections.EMPTY_LIST, projectId, sortKey, sortValue, offset, limit);
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, List<Long> routeRuleId, long projectId, String sortKey, String sortValue, long offset, long limit) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("offset", offset);
        params.put("limit", limit);
        String sql;
        if (Const.CONST_PRIORITY.equals(sortKey)) {
            sortKey = "orders";
        }
        //已发布页面，默认按照路由规则排序
        String orderString = StringUtils.isBlank(sortKey) ? "orders desc , create_time asc" : sortKey + " " + sortValue;
        if (Const.CONST_PRIORITY.equals(sortKey) && Const.CONST_ASC.equals(sortValue)) {
            orderString += orderString + ", create_time asc";
        } else if (Const.CONST_PRIORITY.equals(sortKey) && Const.CONST_DESC.equals(sortValue)) {
            orderString += orderString + ", create_time desc";
        }
        if (0 != gwId && 0 != serviceId) {
            sql = "select * from apigw_route_rule_proxy where service_id=:serviceId and gw_id=:gwId ";
            params.put("serviceId", serviceId);
            params.put("gwId", gwId);
        } else if (0 == gwId && 0 != serviceId) {
            sql = "select * from apigw_route_rule_proxy where service_id=:serviceId and project_id=:projectId  ";
            params.put("serviceId", serviceId);
            params.put("projectId", projectId);
        } else if (0 != gwId && 0 == serviceId) {
            sql = "select * from apigw_route_rule_proxy where gw_id=:gwId and project_id=:projectId  ";
            params.put("gwId", gwId);
            params.put("projectId", projectId);
        } else {
            sql = "select * from apigw_route_rule_proxy where project_id=:projectId";
            params.put("projectId", projectId);
        }
        if (CollectionUtils.isNotEmpty(routeRuleId)) {
            sql = sql + " and route_rule_id in (:routeRuleId) ";
            params.put("routeRuleId", routeRuleId);
        }
        sql += " order by " + orderString + " limit :limit offset :offset";
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleProxyInfoRowMapper());
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(final long gwId, final long serviceId, long projectId,
                                                          final long routeId, final List<Long> routeAuthId,
                                                          final long offset, final long limit) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("routeAuthId", routeAuthId);
        params.put("gwId", gwId);
        String sql;
        //按照优先级排序
        String orderString = "orders desc , create_time asc";
        /**
         * 查询网关所有路由
         */
        if (routeId == 0 && serviceId == 0) {
            sql = "select * from apigw_route_rule_proxy where gw_id=:gwId and project_id=:projectId and route_rule_id "
                    + "in (:routeAuthId) " + "order by " + orderString + " limit :limit offset :offset";
            params.put("projectId", projectId);
        } else if (routeId == 0 && serviceId != 0) {
            sql = "select * from apigw_route_rule_proxy where gw_id=:gwId and service_id=:serviceId and route_rule_id "
                    + "in (:routeAuthId) " + "order by " + orderString + " limit :limit offset :offset";
            params.put("serviceId", serviceId);
        } else {
            sql = "select * from apigw_route_rule_proxy where gw_id=:gwId and route_rule_id=:routeId and "
                    + "route_rule_id " + "in (:routeAuthId) " + "order by " + orderString + " limit :limit offset :offset";
            params.put("routeId", routeId);
        }
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleProxyInfoRowMapper());
    }

    @Override
    public List<RouteRuleProxyInfo> getRouteRuleProxyList(long serviceId) {
        String sql = "select * from apigw_route_rule_proxy where service_id=:serviceId";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource("serviceId", serviceId), new EnvoyRouteRuleProxyInfoRowMapper());

    }

    @Override
    public long getRouteRuleProxyCount(long gwId, long serviceId, long projectId) {
        return getRouteRuleProxyCount(gwId, serviceId, projectId, Collections.EMPTY_LIST);
    }

    @Override
    public long getRouteRuleProxyCount(long gwId, long serviceId, long projectId, List<Long> routeRuleId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (0 != gwId && 0 != serviceId) {
            sql = "select count(*) from apigw_route_rule_proxy where service_id=:serviceId and gw_id=:gwId";
            params.put("serviceId", serviceId);
            params.put("gwId", gwId);
        } else if (0 == gwId && 0 != serviceId) {
            sql = "select count(*) from apigw_route_rule_proxy where service_id=:serviceId and project_id=:projectId";
            params.put("serviceId", serviceId);
            params.put("projectId", projectId);
        } else if (0 != gwId && 0 == serviceId) {
            sql = "select count(*) from apigw_route_rule_proxy where gw_id=:gwId and project_id=:projectId";
            params.put("gwId", gwId);
            params.put("projectId", projectId);
        } else {
            sql = "select count(*) from apigw_route_rule_proxy where project_id=:projectId";
            params.put("projectId", projectId);
        }
        if (CollectionUtils.isNotEmpty(routeRuleId)) {
            sql = sql + " and route_rule_id in (:routeRuleId)";
            params.put("routeRuleId", routeRuleId);
        }
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getRouteRuleProxyCount(final long gwId, final long serviceId, final long projectId, long routeId,
                                       final List<Long> routeAuthId) {

        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("routeAuthId", routeAuthId);
        params.put("gwId", gwId);
        String sql;
        if (routeId == 0 && serviceId == 0) {
            sql = "select count(*) from apigw_route_rule_proxy where gw_id=:gwId and project_id=:projectId and "
                    + "route_rule_id in (:routeAuthId) ";
            params.put("projectId", projectId);
        } else if (routeId == 0 && serviceId != 0) {
            sql = "select count(*) from apigw_route_rule_proxy where gw_id=:gwId and service_id=:serviceId and "
                    + "route_rule_id in (:routeAuthId) ";
            params.put("serviceId", serviceId);
        } else {
            sql = "select count(*) from apigw_route_rule_proxy where gw_id=:gwId and route_rule_id=:routeId and "
                    + "route_rule_id " + "in (:routeAuthId) ";
            params.put("routeId", routeId);
        }
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class EnvoyRouteRuleProxyInfoRowMapper implements RowMapper<RouteRuleProxyInfo> {
        @Override
        public RouteRuleProxyInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RouteRuleProxyInfo routeRuleProxyInfo = new RouteRuleProxyInfo();
            routeRuleProxyInfo.setId(rs.getLong("id"));
            routeRuleProxyInfo.setGwId(rs.getLong("gw_id"));
            routeRuleProxyInfo.setGwType(rs.getString("gw_type"));
            routeRuleProxyInfo.setCreateTime(rs.getLong("create_time"));
            routeRuleProxyInfo.setUpdateTime(rs.getLong("update_time"));
            routeRuleProxyInfo.setRouteRuleId(rs.getLong("route_rule_id"));
            routeRuleProxyInfo.setDestinationServices(rs.getString("destination_services"));
            routeRuleProxyInfo.setProjectId(rs.getLong("project_id"));
            routeRuleProxyInfo.setServiceId(rs.getLong("service_id"));
            routeRuleProxyInfo.setPriority(rs.getLong("priority"));
            routeRuleProxyInfo.setOrders(rs.getLong("orders"));
            routeRuleProxyInfo.setEnableState(rs.getString("enable_state"));
            List<EnvoyDestinationInfo> envoyDestinationInfos = JSON.parseArray(routeRuleProxyInfo.getDestinationServices(), EnvoyDestinationInfo.class);
            routeRuleProxyInfo.setDestinationServiceList(envoyDestinationInfos);
            routeRuleProxyInfo.setHosts(rs.getString("hosts"));
            routeRuleProxyInfo.setTimeout(rs.getLong("timeout"));
            routeRuleProxyInfo.setMirrorTraffic(rs.getString("mirror_traffic"));
            routeRuleProxyInfo.setMirrorTrafficValue(JSON.parseObject(routeRuleProxyInfo.getMirrorTraffic(), EnvoyDestinationInfo.class));
            routeRuleProxyInfo.setMirrorServiceId(rs.getLong("mirror_service_id"));
            //构造HttpRetry
            routeRuleProxyInfo.setHttpRetry(rs.getString("http_retry"));
            if (StringUtils.isNotBlank(routeRuleProxyInfo.getHttpRetry())) {
                routeRuleProxyInfo.setHttpRetryDto(JSON.parseObject(routeRuleProxyInfo.getHttpRetry(), HttpRetryDto.class));
            }
            routeRuleProxyInfo.setUri(rs.getString("uri"));
            routeRuleProxyInfo.setUriMatchInfo(JSON.parseObject(routeRuleProxyInfo.getUri(), EnvoyRouteStringMatchInfo.class));

            routeRuleProxyInfo.setMethod(rs.getString("method"));
            routeRuleProxyInfo.setMethodMatchInfo(JSON.parseObject(routeRuleProxyInfo.getMethod(), EnvoyRouteStringMatchInfo.class));

            routeRuleProxyInfo.setHost(rs.getString("host"));
            routeRuleProxyInfo.setHostMatchInfo(JSON.parseObject(routeRuleProxyInfo.getHost(), EnvoyRouteStringMatchInfo.class));

            routeRuleProxyInfo.setQueryParam(rs.getString("query_param"));
            routeRuleProxyInfo.setQueryParamList(JSON.parseArray(routeRuleProxyInfo.getQueryParam(), EnvoyRouteRuleMapMatchInfo.class));

            routeRuleProxyInfo.setHeader(rs.getString("header"));
            routeRuleProxyInfo.setHeaderList(JSON.parseArray(routeRuleProxyInfo.getHeader(), EnvoyRouteRuleMapMatchInfo.class));
            routeRuleProxyInfo.setVirtualCluster(rs.getString("virtual_cluster"));
            routeRuleProxyInfo.setNeedRouteMetric(rs.getBoolean("need_route_metric"));
            return routeRuleProxyInfo;
        }
    }
}
