package org.hango.cloud.gdashboard.api.dao.impl;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiModelDao;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
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
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 15:49.
 */
@SuppressWarnings({ "java:S1192"})
@Component
public class ApiModelDaoImpl extends BaseDao implements ApiModelDao {

    @Autowired
    private ApiConfig apiConfig;

    private String api_model_table;

    @PostConstruct
    public void init() {
        api_model_table = apiConfig.getDatabasePrefix() + "api_model";
    }


    @Override
    public long add(ApiModel apiModel) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + api_model_table + "(create_date, modify_date,model_name, description, type, format, service_id, swagger_sync,project_id) " +
                "VALUES (:createDate, :modifyDate,:modelName, :description, :type, :format, :serviceId, :swaggerSync,:projectId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModel);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiModel apiModel) {
        String sql = "UPDATE " + api_model_table + " SET model_name=:modelName, description=:description, modify_date=:modifyDate, type=:type " +
                ", format=:format, service_id = :serviceId, swagger_sync = :swaggerSync, project_id = :projectId where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModel);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ApiModel apiModel) {
        String sql = "DELETE FROM " + api_model_table + " WHERE id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiModel);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ApiModel get(long id) {
        String sql = "select * from " + api_model_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiModelRowMapper());
    }

    @Override
    public List<ApiModel> findAll() {
        String sql = "select * from " + api_model_table + " order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ApiModelRowMapper());

    }

    @Override
    public List<ApiModel> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + api_model_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiModelRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + api_model_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<ApiModel> findApiModelByProjectLimit(long projectId, long offset, long limit, String pattern) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from " + api_model_table + " where project_id=:projectId and model_name like :pattern order by id desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from " + api_model_table + " where project_id=:projectId order by id desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.query(sql, params, new ApiModelRowMapper());
    }

    @Override
    public List<ApiModel> findApiModelByServiceIdLimit(long serviceId, long offset, long limit, String pattern) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from " + api_model_table + " where service_id=:serviceId and model_name like :pattern order by id desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from " + api_model_table + " where service_id=:serviceId order by id desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("serviceId", serviceId);
        return namedParameterJdbcTemplate.query(sql, params, new ApiModelRowMapper());
    }

    @Override
    public long getApiModelCountByProjectPattern(long projectId, String pattern) {
        String sql;
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select count(*) from " + api_model_table + " where project_id=:projectId and model_name like :pattern";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select count(*) from " + api_model_table + " where project_id=:projectId";
        }
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long getApiModelCountByServicePattern(long serviceId, String pattern) {
        String sql;
        Map<String, Object> params = Maps.newHashMap();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select count(*) from " + api_model_table + " where service_id=:serviceId and model_name like :pattern";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select count(*) from " + api_model_table + " where service_id=:serviceId";
        }
        params.put("serviceId", serviceId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long deleteApiModelByServiceId(long serviceId) {
        String sql = "delete from " + api_model_table + " where service_id=:serviceId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        return namedParameterJdbcTemplate.update(sql, params);
    }

    class ApiModelRowMapper implements RowMapper<ApiModel> {
        @Override
        public ApiModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiModel apiModel = new ApiModel();
            apiModel.setId(rs.getLong("id"));
            apiModel.setCreateDate(rs.getLong("create_date"));
            apiModel.setModifyDate(rs.getLong("modify_date"));
            apiModel.setModelName(rs.getString("model_name"));
            apiModel.setServiceId(rs.getLong("service_id"));
            apiModel.setFormat(rs.getInt("format"));
            apiModel.setType(rs.getInt("type"));
            apiModel.setDescription(rs.getString("description"));
            apiModel.setSwaggerSync(rs.getInt("swagger_sync"));
            apiModel.setProjectId(rs.getLong("project_id"));
            return apiModel;
        }
    }
}
