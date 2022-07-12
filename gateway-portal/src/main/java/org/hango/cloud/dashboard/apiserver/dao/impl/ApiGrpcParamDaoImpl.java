package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.hango.cloud.dashboard.apiserver.dao.ApiGrpcParamDao;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ApiGrpcParam;
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
public class ApiGrpcParamDaoImpl extends BaseDao implements ApiGrpcParamDao {

    @Override
    public long add(ApiGrpcParam apiGrpcParam) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_gportal_grpc_param (create_date, modify_date, api_id, service_id, pb_name, pb_package_name, pb_service_name, pb_method_name) "
                + " values (:createDate, :modifyDate, :apiId, :serviceId, :pbName, :pbPackageName, :pbServiceName, :pbMethodName)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiGrpcParam);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(ApiGrpcParam apiGrpcParam) {
        String sql = "update apigw_gportal_grpc_param set modify_date = :modifyDate, api_id = :apiId, service_id=:serviceId, pb_name=:pbName, pb_package_name=:pbPackageName, " +
                "pb_service_name=:pbServiceName, pb_method_name=:pbMethodName where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiGrpcParam);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiGrpcParam apiGrpcParam) {
        String sql = "DELETE from apigw_gportal_grpc_param where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", apiGrpcParam.getId()));
        return 0;
    }

    @Override
    public int delete(long apiId) {
        String sql = "DELETE from apigw_gportal_grpc_param where api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", apiId));
        return 0;
    }

    @Override
    public ApiGrpcParam get(long id) {
        String sql = "select * from apigw_gportal_grpc_param where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiGrpcParamRowMapper());
    }

    @Override
    public List<ApiGrpcParam> findAll() {
        String sql = "select * from apigw_gportal_grpc_param order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ApiGrpcParamRowMapper());
    }

    @Override
    public List<ApiGrpcParam> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_grpc_param where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiGrpcParamRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_grpc_param where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ApiGrpcParamRowMapper implements RowMapper<ApiGrpcParam> {
        @Override
        public ApiGrpcParam mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiGrpcParam apiGrpcParam = new ApiGrpcParam();
            apiGrpcParam.setId(rs.getLong("id"));
            apiGrpcParam.setCreateDate(rs.getLong("create_date"));
            apiGrpcParam.setModifyDate(rs.getLong("modify_date"));
            apiGrpcParam.setApiId(rs.getLong("api_id"));
            apiGrpcParam.setServiceId(rs.getLong("service_id"));
            apiGrpcParam.setPbName(rs.getString("pb_name"));
            apiGrpcParam.setPbPackageName(rs.getString("pb_package_name"));
            apiGrpcParam.setPbServiceName(rs.getString("pb_service_name"));
            apiGrpcParam.setPbMethodName(rs.getString("pb_method_name"));
            return apiGrpcParam;
        }
    }
}
