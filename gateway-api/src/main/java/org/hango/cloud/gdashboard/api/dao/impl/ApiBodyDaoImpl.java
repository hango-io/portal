package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiBodyDao;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
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
 * @Date: 创建时间: 2018/1/2 15:49.
 */
@Component
public class ApiBodyDaoImpl extends BaseDao implements ApiBodyDao {

    @Autowired
    private ApiParamTypeDao apiParamTypeDao;
    @Autowired
    private ApiConfig apiConfig;

    private String body_param_table;

    @PostConstruct
    public void init() {
        body_param_table = apiConfig.getDatabasePrefix() + "body_param";
    }

    @Override
    public long add(ApiBody apiBody) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + body_param_table + "(create_date, modify_date, api_id, param_name, param_type_id, array_data_type_id, required, def_value, description, type,association_type) " +
                "VALUES (:createDate, :modifyDate, :apiId, :paramName, :paramTypeId, :arrayDataTypeId, :required, :defValue, :description, :type,:associationType);";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiBody);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiBody apiBody) {
        String sql = "UPDATE " + body_param_table + " SET modify_date=:modifyDate, param_name=:paramName, required=:required, param_type_id=:paramTypeId, array_data_type_id=:arrayDataTypeId, required=:required, def_value=:defValue, description=:description  WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiBody);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiBody apiBody) {
        String sql = "DELETE " + body_param_table + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiBody);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public void delete(long paramId) {
        String sql = "delete from " + body_param_table + " where id = :paramId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("paramId", paramId));
    }

    @Override
    public void deleteBody(long apiId) {
        String sql = "delete from " + body_param_table + " where api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", apiId));
    }

    @Override
    public ApiBody get(long id) {
        String sql = "select * from " + body_param_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiBodyRowMapper());
    }

    @Override
    public List<ApiBody> findAll() {
        String sql = "select * from " + body_param_table;
        return namedParameterJdbcTemplate.query(sql, new ApiBodyRowMapper());
    }

    @Override
    public List<ApiBody> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + body_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiBodyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + body_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<ApiBody> getBody(long apiId, String type) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        mapSqlParameterSource.addValue("type", type);
        String sql = "select * from " + body_param_table + " where api_id = :apiId and type=:type";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiBodyRowMapper());
    }

    @Override
    public List<ApiBody> getBodyParam(String paramName, String type, long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("paramName", paramName);
        mapSqlParameterSource.addValue("type", type);
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + body_param_table + " where param_name = :paramName and type=:type and api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiBodyRowMapper());
    }

    @Override
    public void deleteBody(long apiId, String type) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("type", type);
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "delete from " + body_param_table + " where type=:type and api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);
    }

    @Override
    public List<ApiBody> getBodyByApiId(long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + body_param_table + " where api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new ApiBodyRowMapper());
    }

    class ApiBodyRowMapper implements RowMapper<ApiBody> {
        @Override
        public ApiBody mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiBody apiBody = new ApiBody();
            apiBody.setId(rs.getLong("id"));
            apiBody.setCreateDate(rs.getLong("create_date"));
            apiBody.setModifyDate(rs.getLong("modify_date"));
            apiBody.setApiId(rs.getLong("api_id"));
            apiBody.setParamName(rs.getString("param_name"));
            apiBody.setParamTypeId(rs.getLong("param_type_id"));
            String paramType = apiParamTypeDao.get(apiBody.getParamTypeId()).getParamType();
            apiBody.setParamType(paramType == null ? "String" : paramType);
            apiBody.setArrayDataTypeId(rs.getLong("array_data_type_id"));
            apiBody.setRequired(rs.getString("required"));
            apiBody.setDefValue(rs.getString("def_value"));
            apiBody.setType(rs.getString("type"));
            apiBody.setDescription(rs.getString("description"));
            apiBody.setAssociationType(rs.getString("association_type"));
            return apiBody;
        }
    }
}
