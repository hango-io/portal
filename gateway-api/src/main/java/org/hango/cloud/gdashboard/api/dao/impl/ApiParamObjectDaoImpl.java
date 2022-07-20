package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiParamObjectDao;
import org.hango.cloud.gdashboard.api.meta.ApiParamObject;
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
 * @Date: 创建时间: 2018/1/29 下午4:11.
 */
@Component
public class ApiParamObjectDaoImpl extends BaseDao implements ApiParamObjectDao {

    @Autowired
    private ApiConfig apiConfig;

    private String param_object_table;

    @PostConstruct
    public void init() {
        param_object_table = apiConfig.getDatabasePrefix() + "param_object";
    }

    @Override
    public long add(ApiParamObject apiParamObject) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + param_object_table + "(create_date, object_value) " +
                "VALUES (:createDate, :objectValue)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamObject);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiParamObject apiParamObject) {
        String sql = "UPDATE " + param_object_table + " SET object_value=:objectValue WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamObject);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiParamObject apiParamObject) {
        String sql = "DELETE from " + param_object_table + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiParamObject);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ApiParamObject get(long id) {
        String sql = "select * from " + param_object_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiParamObjectRowMapper());
    }

    @Override
    public List<ApiParamObject> findAll() {
        String sql = "select * from " + param_object_table;
        return namedParameterJdbcTemplate.query(sql, new ApiParamObjectRowMapper());
    }

    @Override
    public List<ApiParamObject> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + param_object_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiParamObjectRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + param_object_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ApiParamObjectRowMapper implements RowMapper<ApiParamObject> {
        @Override
        public ApiParamObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiParamObject apiParamObject = new ApiParamObject();
            apiParamObject.setId(rs.getLong("id"));
            apiParamObject.setCreateDate(rs.getLong("create_date"));
            apiParamObject.setModifyDate(rs.getTimestamp("modify_date").getTime());
            apiParamObject.setObjectValue(rs.getString("object_value"));
            return apiParamObject;
        }
    }
}
