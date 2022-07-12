package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.hango.cloud.dashboard.apiserver.dao.ServiceProtobufDao;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ServiceProtobuf;
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
public class ServiceProtobufDaoImpl extends BaseDao implements ServiceProtobufDao {

    @Override
    public long add(ServiceProtobuf serviceProtobuf) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_gportal_service_protobuf (create_date, modify_date, service_id, pb_name, pb_file_name, pb_file_content, desc_file_content, description, pb_status) "
                + " values (:createDate, :modifyDate, :serviceId, :pbName, :pbFileName, :pbFileContent, :descFileContent, :description, :pbStatus)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobuf);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(ServiceProtobuf serviceProtobuf) {
        String sql = "update apigw_gportal_service_protobuf set modify_date = :modifyDate, service_id = :serviceId, pb_name=:pbName, pb_file_name=:pbFileName, " +
                "pb_file_content=:pbFileContent, desc_file_content=:descFileContent, description=:description, pb_status=:pbStatus where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProtobuf);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceProtobuf serviceProtobuf) {
        String sql = "DELETE from apigw_gportal_service_protobuf where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", serviceProtobuf.getId()));
        return 0;
    }

    @Override
    public ServiceProtobuf get(long id) {
        String sql = "select * from apigw_gportal_service_protobuf where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceProtobufRowMapper());
    }

    @Override
    public List<ServiceProtobuf> findAll() {
        String sql = "select * from apigw_gportal_service_protobuf order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceProtobufRowMapper());
    }

    @Override
    public List<ServiceProtobuf> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_service_protobuf where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProtobufRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_service_protobuf where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ServiceProtobufRowMapper implements RowMapper<ServiceProtobuf> {
        @Override
        public ServiceProtobuf mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServiceProtobuf serviceProtobuf = new ServiceProtobuf();
            serviceProtobuf.setId(rs.getLong("id"));
            serviceProtobuf.setCreateDate(rs.getLong("create_date"));
            serviceProtobuf.setModifyDate(rs.getLong("modify_date"));
            serviceProtobuf.setServiceId(rs.getLong("service_id"));
            serviceProtobuf.setPbName(rs.getString("pb_name"));
            serviceProtobuf.setPbFileName(rs.getString("pb_file_name"));
            serviceProtobuf.setPbFileContent(rs.getString("pb_file_content"));
            serviceProtobuf.setDescFileContent(rs.getString("desc_file_content"));
            serviceProtobuf.setDescription(rs.getString("description"));
            serviceProtobuf.setPbStatus(rs.getInt("pb_status"));
            return serviceProtobuf;
        }
    }
}
