package org.hango.cloud.gdashboard.api.dao.impl;


import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiInfoDao;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
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
 * @Date: 创建时间: 2018/1/2 10:36.
 */
@Component
public class ApiInfoDaoImpl extends BaseDao implements ApiInfoDao {

    @Autowired
    private ApiConfig apiConfig;

    private String api_table;

    @PostConstruct
    public void init() {
        api_table = apiConfig.getDatabasePrefix() + "api";
    }

    @Override
    public long add(ApiInfo apiInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + api_table + "(create_date,modify_date, api_name,api_path, api_method, description, type, service_id, regex, document_status_id, request_example_value, response_example_value,alias_name,sync_status,ext_api_id,project_id,swagger_sync) "
                + "VALUES (:createDate,:modifyDate, :apiName, :apiPath, :apiMethod, :description, :type, :serviceId, :regex, :documentStatusId, :requestExampleValue, :responseExampleValue,:aliasName,:syncStatus,:extApiId,:projectId,:swaggerSync)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ApiInfo apiInfo) {
        String sql = "update " + api_table + " set api_name=:apiName, api_path=:apiPath, api_method=:apiMethod, modify_date=:modifyDate,"
                + "description=:description, type=:type, status=:status, regex=:regex, document_status_id=:documentStatusId, request_example_value=:requestExampleValue, response_example_value=:responseExampleValue , alias_name=:aliasName,sync_status=:syncStatus,ext_api_id=:extApiId, swagger_sync=:swaggerSync where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int update(long apiId, String status) {
        String sql = "update " + api_table + " set status=:status where id=:apiId";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        mapSqlParameterSource.addValue("status", status);
        return namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);
    }

    @Override
    public int delete(ApiInfo apiInfo) {
        return 0;
    }

    @Override
    public ApiInfo get(long id) {
        String sql = "select * from " + api_table + " where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ApiInfoRowMapper1());
    }

    @Override
    public List<ApiInfo> findAll() {
        String sql = "select * from " + api_table;
        return namedParameterJdbcTemplate.query(sql, new ApiInfoRowMapper1());
    }

    @Override
    public List<ApiInfo> find() {
        String sql = "SELECT * FROM " + api_table + " WHERE regex is NULL and type= 'RESTFUL'";
        return namedParameterJdbcTemplate.query(sql, new ApiInfoRowMapper1());
    }


    @Override
    public List<ApiInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + api_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ApiInfoRowMapper1());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + api_table + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public void deleteApi(long apiId) {
        String sql = "delete from " + api_table + " where id = :apiId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("apiId", apiId));
    }

    @Override
    public long addWithId(ApiInfo apiInfo) {
        String sql = "INSERT INTO " + api_table + " (id, create_date, api_name,api_path, api_method, type, service_id, action, version, status, id_cert) "
                + "VALUES (:id, :createDate, :apiName, :apiPath, :apiMethod, :type, :serviceId, :action, :version, :status, :idCert)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(apiInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public List<ApiInfo> findAllApiInfoByServiceLimit(long serviceId, long documentStatusId, String pattern, long offset, long limit) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (documentStatusId != 0) {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select * from " + api_table + " where service_id=:serviceId and document_status_id=:documentStatusId  and (api_name like :pattern  or api_path like :pattern) order by id desc limit :limit offset :offset";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select * from " + api_table + " where service_id=:serviceId and document_status_id=:documentStatusId order by id desc limit :limit offset :offset";
            }
            params.put("documentStatusId", documentStatusId);
        } else {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select * from " + api_table + " where service_id=:serviceId  and (api_name like :pattern  or api_path like :pattern) order by id desc limit :limit offset :offset";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select * from " + api_table + " where service_id=:serviceId  order by id desc limit :limit offset :offset";
            }
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("serviceId", serviceId);

        return namedParameterJdbcTemplate.query(sql, params, new ApiInfoRowMapper1());
    }

    @Override
    public List<ApiInfo> findAllApiInfoByProjectLimit(long projectId, long documentStatusId, String pattern, long offset, long limit) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (documentStatusId != 0) {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select * from " + api_table + " where project_id=:projectId and document_status_id=:documentStatusId  and (api_name like :pattern  or api_path like :pattern) order by id desc limit :limit offset :offset";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select * from " + api_table + " where project_id=:projectId and document_status_id=:documentStatusId order by id desc limit :limit offset :offset";
            }
            params.put("documentStatusId", documentStatusId);
        } else {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select * from " + api_table + " where project_id=:projectId and (api_name like :pattern  or api_path like :pattern) order by id desc limit :limit offset :offset";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select * from " + api_table + " where project_id=:projectId order by id desc limit :limit offset :offset";
            }
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("projectId", projectId);

        return namedParameterJdbcTemplate.query(sql, params, new ApiInfoRowMapper1());
    }

    @Override
    public long getApiCountByService(long serviceId, long documentStatusId, String pattern) {
        String sql;
        Map<String, Object> params = new HashMap<>();
        //查询指定文档状态
        if (documentStatusId != 0) {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select count(*) from " + api_table + "  where service_id=:serviceId and document_status_id=:documentStatusId  and (api_name like :pattern  or api_path like :pattern)";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select count(*) from " + api_table + "  where service_id=:serviceId and document_status_id=:documentStatusId";
            }
            params.put("documentStatusId", documentStatusId);
        } else {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select count(*) from " + api_table + "  where service_id=:serviceId  and (api_name like :pattern  or api_path like :pattern)";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select count(*) from " + api_table + "  where service_id=:serviceId ";
            }
        }

        params.put("serviceId", serviceId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public long getApiCountByProject(long projectId, long documentStatusId, String pattern) {
        String sql;
        Map<String, Object> params = new HashMap<>();
        //查询所有的document id
        if (documentStatusId != 0) {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select count(*) from " + api_table + "  where project_id=:projectId and document_status_id=:documentStatusId  and (api_name like :pattern  or api_path like :pattern)";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select count(*) from " + api_table + "  where project_id=:projectId and document_status_id=:documentStatusId";
            }
            params.put("documentStatusId", documentStatusId);
        } else {
            if (StringUtils.isNotBlank(pattern)) {
                sql = "select count(*) from " + api_table + "  where project_id=:projectId  and (api_name like :pattern  or api_path like :pattern)";
                params.put("pattern", "%" + pattern + "%");
            } else {
                sql = "select count(*) from " + api_table + "  where project_id=:projectId ";
            }
        }
        params.put("projectId", projectId);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    class ApiInfoRowMapper1 implements RowMapper<ApiInfo> {
        @Override
        public ApiInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApiInfo apiInfo = new ApiInfo();
            apiInfo.setId(rs.getLong("id"));
            apiInfo.setCreateDate(rs.getLong("create_date"));
            apiInfo.setModifyDate(rs.getLong("modify_date"));
            apiInfo.setApiName(rs.getString("api_name"));
            apiInfo.setApiPath(rs.getString("api_path"));
            apiInfo.setApiMethod(rs.getString("api_method"));
            apiInfo.setDescription(rs.getString("description"));
            apiInfo.setType(rs.getString("type"));
            apiInfo.setServiceId(rs.getLong("service_id"));
            apiInfo.setStatus(rs.getString("status"));
            apiInfo.setRegex(rs.getString("regex"));
            apiInfo.setDocumentStatusId(rs.getLong("document_status_id"));
            apiInfo.setRequestExampleValue(rs.getString("request_example_value"));
            apiInfo.setResponseExampleValue(rs.getString("response_example_value"));
            apiInfo.setAliasName(rs.getString("alias_name"));
            apiInfo.setSyncStatus(rs.getInt("sync_status"));
            apiInfo.setExtApiId(rs.getLong("ext_api_id"));
            apiInfo.setProjectId(rs.getLong("project_id"));
            apiInfo.setSwaggerSync(rs.getInt("swagger_sync"));
            return apiInfo;
        }
    }
}
