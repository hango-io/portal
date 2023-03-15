package org.hango.cloud.envoy.infra.grpc.dao.impl;


import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.grpc.dao.EnvoyServiceProtobufDao;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
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
 */
@Component
public class EnvoyServiceProtobufDaoImpl extends BaseDao implements EnvoyServiceProtobufDao {

    @Override
    public long add(EnvoyServiceProtobuf serviceProtobuf) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_service_protobuf (create_date, modify_date, service_id, pb_file_name, pb_file_content, pb_service_list) "
                + " values (:createDate, :modifyDate, :serviceId,:pbFileName,:pbFileContent,:pbServiceList) ";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobuf);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(EnvoyServiceProtobuf serviceProtobuf) {
        String sql = "update hango_service_protobuf set modify_date = :modifyDate, service_id = :serviceId, pb_file_name=:pbFileName, " +
                "pb_file_content=:pbFileContent, pb_service_list=:pbServiceList where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobuf);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyServiceProtobuf serviceProtobuf) {
        String sql = "DELETE from hango_service_protobuf where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", serviceProtobuf.getId()));
        return 0;
    }

    @Override
    public EnvoyServiceProtobuf get(long id) {
        String sql = "select * from hango_service_protobuf where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceProtobufRowMapper());
    }

    @Override
    public List<EnvoyServiceProtobuf> findAll() {
        String sql = "select * from hango_service_protobuf order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceProtobufRowMapper());
    }

    @Override
    public List<EnvoyServiceProtobuf> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_service_protobuf where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProtobufRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_service_protobuf where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceProtobufRowMapper implements RowMapper<EnvoyServiceProtobuf> {
        @Override
        public EnvoyServiceProtobuf mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyServiceProtobuf serviceProtobuf = new EnvoyServiceProtobuf();
            serviceProtobuf.setId(rs.getLong("id"));
            serviceProtobuf.setCreateDate(rs.getLong("create_date"));
            serviceProtobuf.setModifyDate(rs.getLong("modify_date"));
            serviceProtobuf.setServiceId(rs.getLong("service_id"));
            serviceProtobuf.setPbFileName(rs.getString("pb_file_name"));
            serviceProtobuf.setPbFileContent(rs.getString("pb_file_content"));
            serviceProtobuf.setPbServiceList(rs.getString("pb_service_list"));
            return serviceProtobuf;
        }
    }
}
