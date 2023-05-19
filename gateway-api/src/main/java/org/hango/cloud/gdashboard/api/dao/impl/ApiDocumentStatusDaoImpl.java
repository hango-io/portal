package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiDocumentStatusDao;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/6/27 11:33.
 */
@SuppressWarnings({ "java:S1192"})
@Component
public class ApiDocumentStatusDaoImpl extends BaseDao implements ApiDocumentStatusDao {

    @Autowired
    private ApiConfig apiConfig;

    private String api_document_status_table;

    @PostConstruct
    public void init() {
        api_document_status_table = apiConfig.getDatabasePrefix() + "api_document_status";
    }

    @Override
    public long add(ApiDocumentStatus apiDocumentStatus) {
        return 0;
    }

    @Override
    public int update(ApiDocumentStatus apiDocumentStatus) {
        return 0;
    }

    @Override
    public int delete(ApiDocumentStatus apiDocumentStatus) {
        return 0;
    }

    @Override
    public ApiDocumentStatus get(long id) {
        String sql = "select * from " + api_document_status_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiDocumentStatusRowMapper());
    }

    @Override
    public List<ApiDocumentStatus> findAll() {
        String sql = "select * from " + api_document_status_table;
        return namedParameterJdbcTemplate.query(sql, new ApiDocumentStatusRowMapper());
    }

    @Override
    public List<ApiDocumentStatus> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + api_document_status_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiDocumentStatusRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        return 0;
    }

    class ApiDocumentStatusRowMapper implements RowMapper<ApiDocumentStatus> {
        @Override
        public ApiDocumentStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiDocumentStatus apiDocumentStatus = new ApiDocumentStatus();
            apiDocumentStatus.setId(rs.getLong("id"));
            apiDocumentStatus.setStatus(rs.getString("status"));
            return apiDocumentStatus;
        }
    }
}
