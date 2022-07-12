package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiStatusCodeDao;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
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
 * @Date: 创建时间: 2018/4/24 14:50.
 */
@Component
public class ApiStatusCodeDaoImpl extends BaseDao implements ApiStatusCodeDao {

    @Autowired
    private ApiConfig apiConfig;

    private String apiStatusCodeTable;

    @PostConstruct
    public void init() {
        apiStatusCodeTable = apiConfig.getDatabasePrefix() + "api_status_code";
    }

    @Override
    public long add(ApiStatusCode apiStatusCode) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into " + apiStatusCodeTable + " (create_date, modify_date, error_code, message, status_code, object_id, type, description)"
                + " values (:createDate, :modifyDate, :errorCode, :message, :statusCode, :objectId, :type, :description)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiStatusCode);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiStatusCode apiStatusCode) {
        String sql = "update " + apiStatusCodeTable + " set modifyDate = :modifyDate, error_code = :errorCode, message = :message, object_id = :objectId" +
                ", status_code = :statusCode, type = :type, description = :description where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiStatusCode);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiStatusCode apiStatusCode) {
        String sql = "DELETE from " + apiStatusCodeTable + " where id = :id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiStatusCode);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(long objectId, String type) {
        String sql = "DELETE from " + apiStatusCodeTable + " where object_id = :objectId and type = :type";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("objectId", objectId);
        mapSqlParameterSource.addValue("type", type);
        return namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);
    }

    @Override
    public ApiStatusCode get(long id) {
        String sql = "select * from " + apiStatusCodeTable + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiStatusCodeRowMapper());

    }

    @Override
    public List<ApiStatusCode> findAll() {
        String sql = "select * from " + apiStatusCodeTable;
        return namedParameterJdbcTemplate.query(sql, new ApiStatusCodeRowMapper());
    }

    @Override
    public List<ApiStatusCode> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + apiStatusCodeTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiStatusCodeRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + apiStatusCodeTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class ApiStatusCodeRowMapper implements RowMapper<ApiStatusCode> {
        @Override
        public ApiStatusCode mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiStatusCode apiStatusCode = new ApiStatusCode();
            apiStatusCode.setId(rs.getLong("id"));
            apiStatusCode.setCreateDate(rs.getLong("create_date"));
            apiStatusCode.setModifyDate(rs.getLong("modify_date"));
            apiStatusCode.setErrorCode(rs.getString("error_code"));
            apiStatusCode.setMessage(rs.getString("message"));
            apiStatusCode.setStatusCode(rs.getLong("status_code"));
            apiStatusCode.setObjectId(rs.getLong("object_id"));
            apiStatusCode.setType(rs.getString("type"));
            apiStatusCode.setDescription(rs.getString("description"));
            return apiStatusCode;
        }
    }
}
