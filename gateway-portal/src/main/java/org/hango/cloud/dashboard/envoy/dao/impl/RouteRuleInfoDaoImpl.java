package org.hango.cloud.dashboard.envoy.dao.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleMapMatchInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteStringMatchInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
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
 * 路由规则Dao层实现类
 *
 * @author hzchenzhongyang 2019-09-11
 * @modify hanjiahao
 */
@Component
public class RouteRuleInfoDaoImpl extends BaseDao implements IRouteRuleInfoDao {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoDaoImpl.class);

    @Override
    public long add(RouteRuleInfo routeRuleInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_route_rule (service_id, route_rule_name, uri, method, host, header, query_param, priority, orders, project_id, publish_status, create_time, update_time,description,header_operation,route_rule_source) "
                + " values (:serviceId, :routeRuleName, :uri, :method, :host, :header, :queryParam, :priority, :orders, :projectId, :publishStatus, :createTime, :updateTime, :description, :headerOperation, :routeRuleSource)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyRouteRuleInfo: {}", ReflectionToStringBuilder.toString(routeRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(RouteRuleInfo routeRuleInfo) {
        String sql = "update apigw_route_rule set route_rule_name=:routeRuleName, uri=:uri, method=:method, host=:host, header=:header, query_param=:queryParam, " +
                "priority=:priority, orders=:orders, project_id=:projectId, publish_status=:publishStatus,  update_time=:updateTime, description=:description, header_operation=:headerOperation, route_rule_source=:routeRuleSource where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleInfo);
        logger.info("update envoyRouteRuleInfo: {}", ReflectionToStringBuilder.toString(routeRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(RouteRuleInfo routeRuleInfo) {
        String sql = "delete from apigw_route_rule where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(routeRuleInfo);
        logger.info("delete envoyRouteRuleInfo: {}", ReflectionToStringBuilder.toString(routeRuleInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public RouteRuleInfo get(long id) {
        String sql = "select * from apigw_route_rule where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyRouteRuleInfoRowMapper());
    }

    @Override
    public List<RouteRuleInfo> findAll() {
        String sql = "select * from apigw_route_rule";
        return namedParameterJdbcTemplate.query(sql, new EnvoyRouteRuleInfoRowMapper());
    }

    @Override
    public List<RouteRuleInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_route_rule where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_route_rule where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<RouteRuleInfo> getRuleInfoByLimit(String pattern, int publishStatus, long projectId, String sortKey, String sortValue, long offset, long limit) {
        String sql;
        if (Const.CONST_PRIORITY.equals(sortKey)) {
            sortKey = "orders";
        }
        String orderString = StringUtils.isBlank(sortKey) ? "id desc" : sortKey + " " + sortValue;
        if (Const.CONST_PRIORITY.equals(sortKey) && Const.CONST_DESC.equals(sortValue)) {
            orderString += orderString + ", create_time asc";
        } else if (Const.CONST_PRIORITY.equals(sortKey) && Const.CONST_DESC.equals(sortValue)) {
            orderString += orderString + ", create_time desc";
        }
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            if (-1 == publishStatus) {
                sql = "select * from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and project_id=:projectId order by " + orderString + " limit :limit offset :offset";
            } else {
                //通过服务名称和发布状态分页查询
                sql = "select * from apigw_route_rule where (route_rule_name like :pattern  or uri like :pattern or host like :pattern) and publish_status=:publishStatus and project_id=:projectId order by " + orderString + " limit :limit offset :offset";
                params.put("publishStatus", publishStatus);
            }
            params.put("pattern", "%" + pattern + "%");
        } else {
            if (-1 == publishStatus) {
                sql = "select * from apigw_route_rule where project_id=:projectId  order by " + orderString + " limit :limit offset :offset";
            } else {
                sql = "select * from apigw_route_rule where publish_status=:publishStatus and project_id=:projectId order by " + orderString + " limit :limit offset :offset";
                params.put("publishStatus", publishStatus);
            }
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleInfoRowMapper());
    }

    @Override
    public List<RouteRuleInfo> getRuleInfoByServiceLimit(String pattern, int publishStatus, long serviceId, String sortKey, String sortValue, long offset, long limit) {
        String sql;
        if (Const.CONST_PRIORITY.equals(sortKey)) {
            sortKey = "orders";
        }
        String orderString = StringUtils.isBlank(sortKey) ? "id desc" : sortKey + " " + sortValue;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            if (-1 == publishStatus) {
                sql = "select * from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and service_id=:serviceId order by " + orderString + " limit :limit offset :offset";
            } else {
                //通过服务名称和发布状态分页查询
                sql = "select * from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and publish_status=:publishStatus and service_id=:serviceId order by " + orderString + " limit :limit offset :offset";
                params.put("publishStatus", publishStatus);
            }
            params.put("pattern", "%" + pattern + "%");
        } else {
            if (-1 == publishStatus) {
                sql = "select * from apigw_route_rule where service_id=:serviceId  order by " + orderString + " limit :limit offset :offset";
            } else {
                sql = "select * from apigw_route_rule where publish_status=:publishStatus and service_id=:serviceId order by " + orderString + " limit :limit offset :offset";
                params.put("publishStatus", publishStatus);
            }
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("serviceId", serviceId);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleInfoRowMapper());
    }

    @Override
    public long getRuleInfoCount(String pattern, int publishStatus, long projectId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            if (-1 == publishStatus) {
                sql = "select count(*) from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and project_id=:projectId";
            } else {
                sql = "select count(*) from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and publish_status=:publishStatus and project_id=:projectId";
                params.put("publishStatus", publishStatus);
            }
            params.put("pattern", "%" + pattern + "%");
        } else {
            if (-1 == publishStatus) {
                sql = "select count(*) from apigw_route_rule where project_id=:projectId";
            } else {
                sql = "select count(*) from apigw_route_rule where project_id=:projectId and publish_status=:publishStatus";
                params.put("publishStatus", publishStatus);
            }
        }
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getRuleInfoByServiceCount(String pattern, int publishStatus, long serviceId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            if (-1 == publishStatus) {
                sql = "select count(*) from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and service_id=:serviceId";
            } else {
                sql = "select count(*) from apigw_route_rule where (route_rule_name like :pattern or uri like :pattern or host like :pattern) and publish_status=:publishStatus and service_id=:serviceId";
                params.put("publishStatus", publishStatus);
            }
            params.put("pattern", "%" + pattern + "%");
        } else {
            if (-1 == publishStatus) {
                sql = "select count(*) from apigw_route_rule where service_id=:serviceId";
            } else {
                sql = "select count(*) from apigw_route_rule where service_id=:serviceId and publish_status=:publishStatus";
                params.put("publishStatus", publishStatus);
            }
        }
        params.put("serviceId", serviceId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Long> getRouteRuleIdListByNameFuzzy(String routeRuleName, long projectId) {
        // FIXME 新增几个sql的索引问题确认
        String sql = "select id from apigw_route_rule where project_id=:projectId and route_rule_name like :routeRuleName";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);
        params.put("routeRuleName", "%" + routeRuleName + "%");
        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public List<RouteRuleInfo> getRouteRuleList(List<Long> routeRuleIdList) {
        String sql = "select * from apigw_route_rule where id in (:routeRuleIdList);";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("routeRuleIdList", routeRuleIdList);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyRouteRuleInfoRowMapper());
    }

    class EnvoyRouteRuleInfoRowMapper implements RowMapper<RouteRuleInfo> {
        @Override
        public RouteRuleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RouteRuleInfo routeRuleInfo = new RouteRuleInfo();
            routeRuleInfo.setId(rs.getLong("id"));
            routeRuleInfo.setServiceId(rs.getLong("service_id"));
            routeRuleInfo.setUri(rs.getString("uri"));
            routeRuleInfo.setUriMatchInfo(JSON.parseObject(routeRuleInfo.getUri(), EnvoyRouteStringMatchInfo.class));

            routeRuleInfo.setMethod(rs.getString("method"));
            routeRuleInfo.setMethodMatchInfo(JSON.parseObject(routeRuleInfo.getMethod(), EnvoyRouteStringMatchInfo.class));

            routeRuleInfo.setHost(rs.getString("host"));
            routeRuleInfo.setHostMatchInfo(JSON.parseObject(routeRuleInfo.getHost(), EnvoyRouteStringMatchInfo.class));

            routeRuleInfo.setQueryParam(rs.getString("query_param"));
            routeRuleInfo.setQueryParamList(JSON.parseArray(routeRuleInfo.getQueryParam(), EnvoyRouteRuleMapMatchInfo.class));

            routeRuleInfo.setHeader(rs.getString("header"));
            routeRuleInfo.setHeaderList(JSON.parseArray(routeRuleInfo.getHeader(), EnvoyRouteRuleMapMatchInfo.class));

            routeRuleInfo.setProjectId(rs.getLong("project_id"));
            routeRuleInfo.setPublishStatus(rs.getInt("publish_status"));
            routeRuleInfo.setCreateTime(rs.getLong("create_time"));
            routeRuleInfo.setUpdateTime(rs.getLong("update_time"));
            routeRuleInfo.setRouteRuleName(rs.getString("route_rule_name"));
            routeRuleInfo.setDescription(rs.getString("description"));
            routeRuleInfo.setPriority(rs.getLong("priority"));
            routeRuleInfo.setOrders(rs.getLong("orders"));
            routeRuleInfo.setHeaderOperation(rs.getString("header_operation"));
            routeRuleInfo.setRouteRuleSource(rs.getString("route_rule_source"));
            return routeRuleInfo;
        }
    }
}
