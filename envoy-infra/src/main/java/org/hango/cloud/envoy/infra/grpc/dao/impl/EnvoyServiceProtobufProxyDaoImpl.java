package org.hango.cloud.envoy.infra.grpc.dao.impl;


import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.grpc.dao.EnvoyServiceProtobufProxyDao;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
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
 * Dao层
 *
 * @Author: TC_WANG
 * @Date: 2019/7/2
 */
@Component
public class EnvoyServiceProtobufProxyDaoImpl extends BaseDao implements EnvoyServiceProtobufProxyDao {

    @Override
    public long add(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_service_protobuf_proxy (create_date, modify_date, service_id, virtual_gw_id, pb_file_name, pb_file_content, pb_service_list) "
                + " values (:createDate, :modifyDate, :serviceId, :virtualGwId, :pbFileName, :pbFileContent, :pbServiceList)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobufProxy);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        String sql = "update hango_service_protobuf_proxy set modify_date = :modifyDate, virtual_gw_id=:virtualGwId, service_id=:serviceId, " +
                "pb_file_name=:pbFileName, pb_file_content=:pbFileContent, pb_service_list=:pbServiceList where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobufProxy);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        String sql = "DELETE from hango_service_protobuf_proxy where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", serviceProtobufProxy.getId()));
        return 0;
    }

    @Override
    public EnvoyServiceProtobufProxy get(long id) {
        String sql = "select * from hango_service_protobuf_proxy where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceProtobufProxyRowMapper());
    }

    @Override
    public List<EnvoyServiceProtobufProxy> findAll() {
        String sql = "select * from hango_service_protobuf_proxy order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceProtobufProxyRowMapper());
    }

    @Override
    public List<EnvoyServiceProtobufProxy> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_service_protobuf_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProtobufProxyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_service_protobuf_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceProtobufProxyRowMapper implements RowMapper<EnvoyServiceProtobufProxy> {
        @Override
        public EnvoyServiceProtobufProxy mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyServiceProtobufProxy serviceProtobufProxy = new EnvoyServiceProtobufProxy();
            serviceProtobufProxy.setId(rs.getLong("id"));
            serviceProtobufProxy.setCreateDate(rs.getLong("create_date"));
            serviceProtobufProxy.setModifyDate(rs.getLong("modify_date"));
            serviceProtobufProxy.setVirtualGwId(rs.getLong("virtual_gw_id"));
            serviceProtobufProxy.setServiceId(rs.getLong("service_id"));
            serviceProtobufProxy.setPbFileName(rs.getString("pb_file_name"));
            serviceProtobufProxy.setPbFileContent(rs.getString("pb_file_content"));
            serviceProtobufProxy.setPbServiceList(rs.getString("pb_service_list"));
            return serviceProtobufProxy;
        }
    }
}
