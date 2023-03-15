package org.hango.cloud.envoy.infra.webservice.dao.impl;

import com.alibaba.fastjson.JSON;

import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.webservice.dao.EnvoyRouteWsParamDao;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyRouteWsParamInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "java:S1192"})
@Component
public class EnvoyRouteWsParamDaoImpl extends BaseDao implements EnvoyRouteWsParamDao {
    @Override
    public EnvoyRouteWsParamInfo getByRoute(long virtualGwId, long serviceId, long routeId) {
        String sql = "select * from hango_route_ws_param_info where gw_id=:virtualGwId and service_id=:serviceId and route_id=:routeId";
        MapSqlParameterSource ps = new MapSqlParameterSource();
        ps.addValue("virtualGwId", virtualGwId);
        ps.addValue("serviceId", serviceId);
        ps.addValue("routeId", routeId);
        return queryForObject(sql, ps, new ServiceWsProxyInfoRowMapper());
    }

    @Override
    public long add(EnvoyRouteWsParamInfo envoyRouteWsParamInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_route_ws_param_info (create_date, modify_date, gw_id, service_id, route_id, request_template, response_array_type_list, ws_port_type, ws_operation, ws_binding, ws_address) "
                + " values (:createDate, :modifyDate, :virtualGwId, :serviceId, :routeId, :requestTemplate, :responseArrayTypeList, :wsPortType, :wsOperation, :wsBinding, :wsAddress)";
        MapSqlParameterSource ps = new MapSqlParameterSource();
        ps.addValue("createDate", envoyRouteWsParamInfo.getCreateDate());
        ps.addValue("modifyDate", envoyRouteWsParamInfo.getModifyDate());
        ps.addValue("virtualGwId", envoyRouteWsParamInfo.getVirtualGwId());
        ps.addValue("serviceId", envoyRouteWsParamInfo.getServiceId());
        ps.addValue("routeId", envoyRouteWsParamInfo.getRouteId());
        ps.addValue("requestTemplate", envoyRouteWsParamInfo.getRequestTemplate());
        ps.addValue("responseArrayTypeList", JSON.toJSONString(envoyRouteWsParamInfo.getResponseArrayTypeList()));
        ps.addValue("wsPortType", envoyRouteWsParamInfo.getWsPortType());
        ps.addValue("wsOperation", envoyRouteWsParamInfo.getWsOperation());
        ps.addValue("wsBinding", envoyRouteWsParamInfo.getWsBinding());
        ps.addValue("wsAddress", envoyRouteWsParamInfo.getWsAddress());
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(EnvoyRouteWsParamInfo envoyRouteWsParamInfo) {
        String sql = "update hango_route_ws_param_info set modify_date=:modifyDate, gw_id=:virtualGwId, service_id=:serviceId, route_id=:routeId, " +
                "request_template=:requestTemplate, response_array_type_list=:responseArrayTypeList, ws_port_type=:wsPortType, " +
                "ws_operation=:wsOperation, ws_binding=:wsBinding, ws_address=:wsAddress where id=:id";
        MapSqlParameterSource ps = new MapSqlParameterSource();
        ps.addValue("id", envoyRouteWsParamInfo.getId());
        ps.addValue("modifyDate", envoyRouteWsParamInfo.getModifyDate());
        ps.addValue("virtualGwId", envoyRouteWsParamInfo.getVirtualGwId());
        ps.addValue("serviceId", envoyRouteWsParamInfo.getServiceId());
        ps.addValue("routeId", envoyRouteWsParamInfo.getRouteId());
        ps.addValue("requestTemplate", envoyRouteWsParamInfo.getRequestTemplate());
        ps.addValue("responseArrayTypeList", JSON.toJSONString(envoyRouteWsParamInfo.getResponseArrayTypeList()));
        ps.addValue("wsPortType", envoyRouteWsParamInfo.getWsPortType());
        ps.addValue("wsOperation", envoyRouteWsParamInfo.getWsOperation());
        ps.addValue("wsBinding", envoyRouteWsParamInfo.getWsBinding());
        ps.addValue("wsAddress", envoyRouteWsParamInfo.getWsAddress());
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyRouteWsParamInfo envoyRouteWsParamInfo) {
        String sql = "delete from hango_route_ws_param_info where id = :id";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", envoyRouteWsParamInfo.getId()));
    }

    @Override
    public EnvoyRouteWsParamInfo get(long id) {
        String sql = "select * from hango_route_ws_param_info where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceWsProxyInfoRowMapper());
    }

    @Override
    public List<EnvoyRouteWsParamInfo> findAll() {
        String sql = "select * from hango_route_ws_param_info order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceWsProxyInfoRowMapper());
    }

    @Override
    public List<EnvoyRouteWsParamInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_route_ws_param_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceWsProxyInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_route_ws_param_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceWsProxyInfoRowMapper implements RowMapper<EnvoyRouteWsParamInfo> {
        @Override
        public EnvoyRouteWsParamInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyRouteWsParamInfo wsProxyInfo = new EnvoyRouteWsParamInfo();
            wsProxyInfo.setId(rs.getLong("id"));
            wsProxyInfo.setCreateDate(rs.getLong("create_date"));
            wsProxyInfo.setModifyDate(rs.getLong("modify_date"));
            wsProxyInfo.setVirtualGwId(rs.getLong("gw_id"));
            wsProxyInfo.setServiceId(rs.getLong("service_id"));
            wsProxyInfo.setRouteId(rs.getLong("route_id"));
            wsProxyInfo.setRequestTemplate(rs.getString("request_template"));
            wsProxyInfo.setResponseArrayTypeList(JSON.parseArray(rs.getString("response_array_type_list"), String.class));
            wsProxyInfo.setWsPortType(rs.getString("ws_port_type"));
            wsProxyInfo.setWsOperation(rs.getString("ws_operation"));
            wsProxyInfo.setWsBinding(rs.getString("ws_binding"));
            wsProxyInfo.setWsAddress(rs.getString("ws_address"));
            return wsProxyInfo;
        }
    }
}
