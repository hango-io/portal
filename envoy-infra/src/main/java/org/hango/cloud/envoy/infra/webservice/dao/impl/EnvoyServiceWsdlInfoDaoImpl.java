package org.hango.cloud.envoy.infra.webservice.dao.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.webservice.dao.EnvoyServiceWsdlInfoDao;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlBindingItem;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlInfo;
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
public class EnvoyServiceWsdlInfoDaoImpl extends BaseDao implements EnvoyServiceWsdlInfoDao {
    @Override
    public int deleteByServiceId(long virtualGwId, long serviceId) {
        String sql = "delete from hango_service_wsdl_info where gw_id =:virtualGwId and service_id = :serviceId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(ImmutableMap.of("virtualGwId", virtualGwId, "serviceId", serviceId)));
    }

    @Override
    public EnvoyServiceWsdlInfo getByServiceId(long virtualGwId, long serviceId) {
        String sql = "select * from hango_service_wsdl_info where gw_id =:virtualGwId and service_id = :serviceId";
        return queryForObject(sql, new MapSqlParameterSource(ImmutableMap.of("virtualGwId", virtualGwId, "serviceId", serviceId)), new ServiceWsInfoRowMapper());
    }

    @Override
    public long add(EnvoyServiceWsdlInfo envoyServiceWsdlInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_service_wsdl_info (create_date, modify_date, gw_id, service_id, wsdl_file_name, wsdl_file_content, wsdl_binding_list) "
                + " values (:createDate, :modifyDate, :virtualGwId, :serviceId,:wsdlFileName,:wsdlFileContent,:wsdlBindingList)";
        MapSqlParameterSource ps = new MapSqlParameterSource();
        ps.addValue("createDate", envoyServiceWsdlInfo.getCreateDate());
        ps.addValue("modifyDate", envoyServiceWsdlInfo.getModifyDate());
        ps.addValue("virtualGwId", envoyServiceWsdlInfo.getVirtualGwId());
        ps.addValue("serviceId", envoyServiceWsdlInfo.getServiceId());
        ps.addValue("wsdlFileName", envoyServiceWsdlInfo.getWsdlFileName());
        ps.addValue("wsdlFileContent", envoyServiceWsdlInfo.getWsdlFileContent());
        ps.addValue("wsdlBindingList", JSON.toJSONString(envoyServiceWsdlInfo.getWsdlBindingList()));
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(EnvoyServiceWsdlInfo envoyServiceWsdlInfo) {
        String sql = "update hango_service_wsdl_info set modify_date = :modifyDate, service_id = :serviceId, wsdl_file_name=:wsdlFileName, " +
                "wsdl_file_content=:wsdlFileContent, wsdl_binding_list=:wsdlBindingList where id=:id";
        MapSqlParameterSource ps = new MapSqlParameterSource();
        ps.addValue("id", envoyServiceWsdlInfo.getId());
        ps.addValue("modifyDate", envoyServiceWsdlInfo.getModifyDate());
        ps.addValue("serviceId", envoyServiceWsdlInfo.getServiceId());
        ps.addValue("wsdlFileName", envoyServiceWsdlInfo.getWsdlFileName());
        ps.addValue("wsdlFileContent", envoyServiceWsdlInfo.getWsdlFileContent());
        ps.addValue("wsdlBindingList", JSON.toJSONString(envoyServiceWsdlInfo.getWsdlBindingList()));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyServiceWsdlInfo envoyServiceWsdlInfo) {
        String sql = "DELETE from hango_service_wsdl_info where id = :id";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", envoyServiceWsdlInfo.getId()));
    }

    @Override
    public EnvoyServiceWsdlInfo get(long id) {
        String sql = "select * from hango_service_wsdl_info where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceWsInfoRowMapper());
    }

    @Override
    public List<EnvoyServiceWsdlInfo> findAll() {
        String sql = "select * from hango_service_wsdl_info order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceWsInfoRowMapper());
    }

    @Override
    public List<EnvoyServiceWsdlInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_service_wsdl_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceWsInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_service_wsdl_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceWsInfoRowMapper implements RowMapper<EnvoyServiceWsdlInfo> {
        @Override
        public EnvoyServiceWsdlInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyServiceWsdlInfo wsInfo = new EnvoyServiceWsdlInfo();
            wsInfo.setId(rs.getLong("id"));
            wsInfo.setCreateDate(rs.getLong("create_date"));
            wsInfo.setModifyDate(rs.getLong("modify_date"));
            wsInfo.setServiceId(rs.getLong("service_id"));
            wsInfo.setWsdlFileName(rs.getString("wsdl_file_name"));
            wsInfo.setWsdlFileContent(rs.getString("wsdl_file_content"));
            wsInfo.setWsdlBindingList(JSON.parseArray(rs.getString("wsdl_binding_list"), EnvoyServiceWsdlBindingItem.class));
            return wsInfo;
        }
    }
}
