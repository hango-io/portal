package org.hango.cloud.gdashboard.api.dao.impl;


import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiModelParamDao;
import org.hango.cloud.gdashboard.api.meta.ApiModelParam;
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
 * @Date: 创建时间: 2018/1/2 16:09.
 */
@SuppressWarnings({ "java:S1192"})
@Component
public class ApiModelParamDaoImpl extends BaseDao implements ApiModelParamDao {

    @Autowired
    private ApiConfig apiConfig;

    private String model_param_table;

    @PostConstruct
    public void init() {
        model_param_table = apiConfig.getDatabasePrefix() + "model_param";
    }

    @Override
    public long add(ApiModelParam apiModelParam) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + model_param_table + "(create_date,modify_date, model_id, param_name, def_value, description, object_id, param_type_id, array_data_type_id, required) "
                + "VALUES (:createDate, :modifyDate, :modelId, :paramName, :defValue, :description, :objectId, :paramTypeId, :arrayDataTypeId, :required)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModelParam);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiModelParam apiModelParam) {
        String sql = "UPDATE " + model_param_table + " SET modify_date=:modifyDate, model_id=:modelId, param_name=:paramName, def_value=:defValue, description=:description, " +
                " object_id=:objectId, param_type_id =:paramTypeId, array_data_type_id=:arrayDataTypeId, required=:required WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModelParam);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiModelParam apiModelParam) {
        String sql = "DELETE " + model_param_table + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModelParam);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ApiModelParam get(long id) {
        String sql = "select * from " + model_param_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiModelParamRowMapper());
    }

    @Override
    public List<ApiModelParam> findAll() {
        String sql = "select * from " + model_param_table;
        return namedParameterJdbcTemplate.query(sql, new ApiModelParamRowMapper());
    }

    @Override
    public List<ApiModelParam> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + model_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiModelParamRowMapper());
    }


    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + model_param_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long deleteApiModelParamByModelId(long modelId) {
        String sql = "DELETE from " + model_param_table + " WHERE model_id=:modelId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("modelId", modelId));
    }

    class ApiModelParamRowMapper implements RowMapper<ApiModelParam> {
        @Override
        public ApiModelParam mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiModelParam apiModelParam = new ApiModelParam();
            apiModelParam.setId(rs.getLong("id"));
            apiModelParam.setCreateDate(rs.getLong("create_date"));
            apiModelParam.setModifyDate(rs.getLong("modify_date"));
            apiModelParam.setModelId(rs.getLong("model_id"));
            apiModelParam.setParamName(rs.getString("param_name"));
            apiModelParam.setObjectId(rs.getLong("object_id"));
            apiModelParam.setArrayDataTypeId(rs.getLong("array_data_type_id"));
            apiModelParam.setParamTypeId(rs.getLong("param_type_id"));
            apiModelParam.setDefValue(rs.getString("def_value"));
            apiModelParam.setDescription(rs.getString("description"));
            apiModelParam.setRequired(rs.getString("required"));
            return apiModelParam;
        }
    }
}
