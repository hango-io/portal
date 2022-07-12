package org.hango.cloud.gdashboard.api.dao.impl;

import org.hango.cloud.gdashboard.api.config.ApiConfig;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.dao.DubboParamDao;
import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
public class DubboParamDaoImpl extends BaseDao implements DubboParamDao {

    @Autowired
    private ApiConfig apiConfig;

    private String dubboParamTable;
    @Autowired
    private ApiParamTypeDao apiParamTypeDao;

    @PostConstruct
    public void init() {
        dubboParamTable = apiConfig.getDatabasePrefix() + "dubbo_param";
    }

    @Override
    public long add(DubboParamInfo paramInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO " + dubboParamTable + " (create_date, api_id, param_name, param_type_id, array_data_type_id, required, def_value,dubbo_type ,description,param_sort,param_alias) " +
                "VALUES (:createDate, :apiId, :paramName, :paramTypeId, :arrayDataTypeId, :required, :defValue,:dubboType, :description, :paramSort,:paramAlias);";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(paramInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public void delete(long paramId) {
        String sql = "delete from " + dubboParamTable + " where id = :paramId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("paramId", paramId));
    }


    @Override
    public DubboParamInfo get(long id) {
        String sql = "select * from " + dubboParamTable + " where id=:id";
        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), new DubboParamInfoRowMapper());
    }

    @Override
    public List<DubboParamInfo> findAll() {
        String sql = "select * from " + dubboParamTable;
        return namedParameterJdbcTemplate.query(sql, new DubboParamInfoRowMapper());
    }

    @Override
    public List<DubboParamInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from " + dubboParamTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new DubboParamInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from " + dubboParamTable + " where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<DubboParamInfo> getDubboInfo(long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + dubboParamTable + " where api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new DubboParamInfoRowMapper());
    }

    @Override
    public List<DubboParamInfo> getDubboParam(String dubboType, long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "select * from " + dubboParamTable + " where dubbo_type=:dubboType and api_id = :apiId";
        return namedParameterJdbcTemplate.query(sql, mapSqlParameterSource, new DubboParamInfoRowMapper());
    }

    @Override
    public void deleteDubboParam(long apiId) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("apiId", apiId);
        String sql = "delete from " + dubboParamTable + " where api_id = :apiId";
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(List<DubboParamInfo> dubboParamInfos) {
        if (!CollectionUtils.isEmpty(dubboParamInfos)) {
            for (DubboParamInfo dubboParamInfo : dubboParamInfos) {
                add(dubboParamInfo);
            }
        }
    }

    @Override
    public int update(DubboParamInfo dubboParamInfo) {
        return 0;
    }

    @Override
    public int delete(DubboParamInfo dubboParamInfo) {
        return 0;
    }

    class DubboParamInfoRowMapper implements RowMapper<DubboParamInfo> {
        @Override
        public DubboParamInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            DubboParamInfo dubboParamInfo = new DubboParamInfo();
            dubboParamInfo.setId(rs.getLong("id"));
            dubboParamInfo.setCreateDate(rs.getLong("create_date"));
            dubboParamInfo.setModifyDate(rs.getTimestamp("modify_date").getTime());
            dubboParamInfo.setApiId(rs.getLong("api_id"));
            dubboParamInfo.setParamName(rs.getString("param_name"));
            dubboParamInfo.setParamTypeId(rs.getLong("param_type_id"));
            String paramType = apiParamTypeDao.get(dubboParamInfo.getParamTypeId()).getParamType();
            dubboParamInfo.setParamType(paramType == null ? "String" : paramType);
            dubboParamInfo.setArrayDataTypeId(rs.getLong("array_data_type_id"));
            dubboParamInfo.setRequired(rs.getString("required"));
            dubboParamInfo.setDefValue(rs.getString("def_value"));
            dubboParamInfo.setDubboType(rs.getString("dubbo_type"));
            dubboParamInfo.setDescription(rs.getString("description"));
            dubboParamInfo.setParamSort(rs.getInt("param_sort"));
            dubboParamInfo.setParamAlias(rs.getString("param_alias"));
            return dubboParamInfo;
        }
    }
}
