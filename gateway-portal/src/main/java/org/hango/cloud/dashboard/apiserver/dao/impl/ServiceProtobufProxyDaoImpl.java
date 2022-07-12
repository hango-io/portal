package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.hango.cloud.dashboard.apiserver.dao.ServiceProtobufProxyDao;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ServiceProtobufProxy;
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
public class ServiceProtobufProxyDaoImpl extends BaseDao implements ServiceProtobufProxyDao {

    @Override
    public long add(ServiceProtobufProxy serviceProtobufProxy) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_gportal_service_protobuf_proxy (create_date, modify_date, pb_id, gw_id) "
                + " values (:createDate, :modifyDate, :pbId, :gwId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobufProxy);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(ServiceProtobufProxy serviceProtobufProxy) {
        String sql = "update apigw_gportal_service_protobuf_proxy set modify_date = :modifyDate, pb_id = :pbId, gw_id=:gwId where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobufProxy);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceProtobufProxy serviceProtobufProxy) {
        String sql = "DELETE from apigw_gportal_service_protobuf_proxy where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", serviceProtobufProxy.getId()));
        return 0;
    }

    @Override
    public ServiceProtobufProxy get(long id) {
        String sql = "select * from apigw_gportal_service_protobuf_proxy where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceProtobufProxyRowMapper());
    }

    @Override
    public List<ServiceProtobufProxy> findAll() {
        String sql = "select * from apigw_gportal_service_protobuf_proxy order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceProtobufProxyRowMapper());
    }

    @Override
    public List<ServiceProtobufProxy> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_service_protobuf_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProtobufProxyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_service_protobuf_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceProtobufProxyRowMapper implements RowMapper<ServiceProtobufProxy> {
        @Override
        public ServiceProtobufProxy mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServiceProtobufProxy serviceProtobufProxy = new ServiceProtobufProxy();
            serviceProtobufProxy.setId(rs.getLong("id"));
            serviceProtobufProxy.setCreateDate(rs.getLong("create_date"));
            serviceProtobufProxy.setModifyDate(rs.getLong("modify_date"));
            serviceProtobufProxy.setGwId(rs.getLong("gw_id"));
            serviceProtobufProxy.setPbId(rs.getLong("pb_id"));
            return serviceProtobufProxy;
        }
    }
}
