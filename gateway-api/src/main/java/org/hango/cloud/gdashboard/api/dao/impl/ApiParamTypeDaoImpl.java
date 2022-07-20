package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/2 16:01.
 */
@Component
public class ApiParamTypeDaoImpl extends BaseDao implements ApiParamTypeDao {

    @Autowired
    private ApiConfig apiConfig;

    private String param_type_table;

    @PostConstruct
    public void init() {
        param_type_table = apiConfig.getDatabasePrefix() + "param_type";
    }


    @Override
    public long add(ApiParamType apiParamType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + param_type_table + "(create_date, modify_date, param_type, location, model_id) " +
                "VALUES (:createDate,:modifyDate, :paramType, :location, :modelId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamType);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiParamType apiParamType) {
        String sql = "UPDATE " + param_type_table + " SET modify_date=:modifyDate, param_type=:paramType WHERE model_id=:modelId";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamType);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiParamType apiParamType) {
        String sql = "DELETE from " + param_type_table + " WHERE model_id=:modelId";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamType);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ApiParamType get(long id) {
        String sql = "select * from " + param_type_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiParamTypeRowMapper());
    }

    @Override
    public List<ApiParamType> findAll() {
        return null;
    }

    @Override
    public List<ApiParamType> findAll(String location) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("location", location);
        String sql = "select * from " + param_type_table + " where location = :location";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiParamTypeRowMapper());
    }

    @Override
    public List<ApiParamType> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + param_type_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiParamTypeRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + param_type_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ApiParamTypeRowMapper implements RowMapper<ApiParamType> {
        @Override
        public ApiParamType mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiParamType apiParamType = new ApiParamType();
            apiParamType.setId(rs.getLong("id"));
            apiParamType.setCreateDate(rs.getLong("create_date"));
            apiParamType.setModifyDate(rs.getLong("modify_date"));
            apiParamType.setParamType(rs.getString("param_type"));
            apiParamType.setLocation(rs.getString("location"));
            apiParamType.setModelId(rs.getLong("model_id"));
            return apiParamType;
        }
    }
}
