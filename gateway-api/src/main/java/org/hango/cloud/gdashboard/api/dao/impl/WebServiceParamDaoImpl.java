package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.dao.WebServiceParamDao;
import org.hango.cloud.gdashboard.api.meta.WebServiceParamInfo;
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
 * @Date: 创建时间: 2018/1/2 15:49.
 */
@Component
public class WebServiceParamDaoImpl extends BaseDao implements WebServiceParamDao {
    @Autowired
    private ApiParamTypeDao apiParamTypeDao;
    @Autowired
    private ApiConfig apiConfig;

    private String webserviceParamTable;

    @PostConstruct
    public void init() {
        webserviceParamTable = apiConfig.getDatabasePrefix() + "webservice_param";
    }

    @Override
    public long add(WebServiceParamInfo paramInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + webserviceParamTable + " (create_date, modify_date, api_id, param_name, param_type_id, array_data_type_id, type,description,param_sort) " +
                "VALUES (:createDate, :modifyDate, :apiId, :paramName, :paramTypeId, :arrayDataTypeId, :type, :description, :paramSort);";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(paramInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public WebServiceParamInfo get(long id) {
        String sql = "select * from " + webserviceParamTable + " where id=:id";
        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), new WebServiceParamInfoRowMapper());
    }

    @Override
    public List<WebServiceParamInfo> findAll() {
        String sql = "select * from " + webserviceParamTable;
        return namedParameterJdbcTemplate.query(sql, new WebServiceParamInfoRowMapper());
    }

    @Override
    public List<WebServiceParamInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + webserviceParamTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new WebServiceParamInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + webserviceParamTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public int update(WebServiceParamInfo webServiceParamInfo) {
        String sql = "update " + webserviceParamTable + " set create_date = :createDate, modify_date=:modifyDate, api_id=:apiId, " +
                "param_name=:paramName, param_type=:paramType, type=:type, param_type_id=:paramTypeId, array_data_type_id=:arrayDataTypeId,param_sort=:paramSort,description=:description where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(webServiceParamInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(WebServiceParamInfo webServiceParamInfo) {
        String sql = "delete from " + webserviceParamTable + " where api_id = :apiId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", webServiceParamInfo.getApiId()));
    }

    @Override
    public int deleteByApiId(long apiId) {
        String sql = "delete from " + webserviceParamTable + " where api_id = :apiId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", apiId));
    }

    class WebServiceParamInfoRowMapper implements RowMapper<WebServiceParamInfo> {
        @Override
        public WebServiceParamInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            WebServiceParamInfo webServiceParamInfo = new WebServiceParamInfo();
            webServiceParamInfo.setId(rs.getLong("id"));
            webServiceParamInfo.setCreateDate(rs.getLong("create_date"));
            webServiceParamInfo.setModifyDate(rs.getLong("modify_date"));
            webServiceParamInfo.setApiId(rs.getLong("api_id"));
            webServiceParamInfo.setParamName(rs.getString("param_name"));
            webServiceParamInfo.setParamTypeId(rs.getLong("param_type_id"));
            String paramType = "String";
            if (webServiceParamInfo.getParamTypeId() != 0) {
                paramType = apiParamTypeDao.get(webServiceParamInfo.getParamTypeId()).getParamType();
            }
            webServiceParamInfo.setParamType(paramType == null ? "String" : paramType);
            webServiceParamInfo.setArrayDataTypeId(rs.getLong("array_data_type_id"));
            webServiceParamInfo.setType(rs.getString("type"));
            webServiceParamInfo.setParamSort(rs.getInt("param_sort"));
            webServiceParamInfo.setDescription(rs.getString("description"));
            return webServiceParamInfo;
        }
    }
}
