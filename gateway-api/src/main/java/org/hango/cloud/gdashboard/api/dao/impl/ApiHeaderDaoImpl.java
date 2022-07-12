package org.hango.cloud.gdashboard.api.dao.impl;


import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiHeaderDao;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
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
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:48.
 */
@Component
public class ApiHeaderDaoImpl extends BaseDao implements ApiHeaderDao {

    @Autowired
    private ApiConfig apiConfig;

    private String header_param_table;

    @PostConstruct
    public void init() {
        header_param_table = apiConfig.getDatabasePrefix() + "header_param";
    }


    @Override
    public long add(ApiHeader apiHeader) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + header_param_table + "(create_date, modify_date, api_id, param_name, param_value, description, type) " +
                "VALUES (:createDate,:modifyDate, :apiId, :paramName, :paramValue, :description, :type)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiHeader);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiHeader apiHeader) {
        String sql = "UPDATE " + header_param_table + " SET modify_date=:modifyDate, param_name=:paramName, param_value=:paramValue, description=:description  WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiHeader);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiHeader apiHeader) {
        String sql = "DELETE " + header_param_table + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiHeader);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ApiHeader get(long id) {
        String sql = "select * from " + header_param_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiHeaderRowMapper());
    }

    @Override
    public List<ApiHeader> findAll() {
        String sql = "select * from " + header_param_table;
        return namedParameterJdbcTemplate.query(sql, new ApiHeaderRowMapper());
    }

    @Override
    public List<ApiHeader> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + header_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiHeaderRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + header_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<ApiHeader> getHeader(long apiId, String type) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        mapSqlParameterSource.addValue("type", type);
        String sql = "select * from " + header_param_table + " where api_id = :apiId and type=:type";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiHeaderRowMapper());
    }

    @Override
    public List<ApiHeader> getHeaderParam(String paramName, String type, long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("paramName", paramName);
        mapSqlParameterSource.addValue("type", type);
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + header_param_table + " where param_name = :paramName and type=:type and api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiHeaderRowMapper());
    }

    @Override
    public void deleteHeader(long apiId, String type) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("type", type);
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "delete from " + header_param_table + " where type=:type and api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);
    }

    @Override
    public void deleteHeaderParam(long paramId) {
        String sql = "delete from " + header_param_table + " where id = :paramId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("paramId", paramId));
    }

    @Override
    public void deleteHeader(long apiId) {
        String sql = "delete from " + header_param_table + " where api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", apiId));
    }

    @Override
    public List<ApiHeader> getHeaderByApiId(long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + header_param_table + " where api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiHeaderRowMapper());

    }

    class ApiHeaderRowMapper implements RowMapper<ApiHeader> {
        @Override
        public ApiHeader mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiHeader apiHeader = new ApiHeader();
            apiHeader.setId(rs.getLong("id"));
            apiHeader.setCreateDate(rs.getLong("create_date"));
            apiHeader.setModifyDate(rs.getLong("modify_date"));
            apiHeader.setParamName(rs.getString("param_name"));
            apiHeader.setParamValue(rs.getString("param_value"));
            apiHeader.setApiId(rs.getLong("api_id"));
            apiHeader.setType(rs.getString("type"));
            apiHeader.setDescription(rs.getString("description"));

            return apiHeader;
        }
    }
}
