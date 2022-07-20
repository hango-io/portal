package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.OperationLogDao;
import org.hango.cloud.gdashboard.api.meta.OperationLog;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/4/24 19:56.
 */
@Component
public class OperationLogDaoImpl extends BaseDao implements OperationLogDao {

    @Autowired
    private ApiConfig apiConfig;

    private String operationLogTable;

    @PostConstruct
    public void init() {
        operationLogTable = apiConfig.getDatabasePrefix() + "operation_log";
    }

    @Override
    public long add(OperationLog operationLog) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + operationLogTable + "(create_date, email, object_id, type, operation) " +
                "VALUES (:createDate, :email, :objectId, :type, :operation)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(operationLog);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(OperationLog operationLog) {
        String sql = "UPDATE " + operationLogTable + " SET email = :email, object_id = :objectId, operation = :operation, type = :type WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(operationLog);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(OperationLog operationLog) {
        String sql = "DELETE from " + operationLogTable + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(operationLog);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public OperationLog get(long id) {
        String sql = "select * from " + operationLogTable + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiOperationLogRowMapper());

    }

    @Override
    public List<OperationLog> findAll() {
        String sql = "select * from " + operationLogTable;
        return namedParameterJdbcTemplate.query(sql, new ApiOperationLogRowMapper());
    }

    @Override
    public List<OperationLog> findAll(long objectId, String type, long limit, long offset) {
        String sql = "select * from " + operationLogTable + " WHERE object_id = :objectId and type = :type order by id desc limit :offset, :limit ";
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("objectId", objectId);
        params.put("type", type);
        return namedParameterJdbcTemplate.query(sql, params, new ApiOperationLogRowMapper());
    }

    @Override
    public List<OperationLog> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + operationLogTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiOperationLogRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + operationLogTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    class ApiOperationLogRowMapper implements RowMapper<OperationLog> {
        @Override
        public OperationLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            OperationLog operationLog = new OperationLog();
            operationLog.setId(rs.getLong("id"));
            operationLog.setCreateDate(rs.getLong("create_date"));
            operationLog.setEmail(rs.getString("email"));
            operationLog.setObjectId(rs.getLong("object_id"));
            operationLog.setType(rs.getString("type"));
            operationLog.setOperation(rs.getString("operation"));
            return operationLog;
        }
    }
}
