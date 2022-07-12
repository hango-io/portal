package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.IRegistryCenterDao;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/12
 */
@Component
public class RegistryCenterDaoImpl extends BaseDao implements IRegistryCenterDao {

    public static final Logger logger = LoggerFactory.getLogger(RegistryCenterDaoImpl.class);

    @Override
    public long add(RegistryCenterInfo registryCenterInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = " insert into apigw_gportal_registry_center (registry_type, registry_addr,registry_alias, "
                + "create_date, modify_date,    project_id,is_shared,gw_id)" +
                "values (:registryType, :registryAddr,:registryAlias, :createDate, :modifyDate, :projectId, "
                + ":isShared, :gwId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(registryCenterInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addRegistryCenterInfo {}", ReflectionToStringBuilder.toString(registryCenterInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(RegistryCenterInfo registryCenterInfo) {
        String sql = " update apigw_gportal_registry_center set registry_type = :registryType, registry_addr = "
                + ":registryAddr,     registry_alias = :registryAlias, modify_date = :modifyDate, project_id= "
                + ":projectId,is_shared= :isShared, gw_id= :gwId where id = :id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(registryCenterInfo);
        logger.info("updateRegistryCenterInfo {}", ReflectionToStringBuilder.toString(registryCenterInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(RegistryCenterInfo registryCenterInfo) {
        String sql = "DELETE from apigw_gportal_registry_center where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", registryCenterInfo.getId()));
        logger.info("deleteRegistryCenterInfo {}", ReflectionToStringBuilder.toString(registryCenterInfo, ToStringStyle.SIMPLE_STYLE));
        return 0;
    }

    @Override
    public RegistryCenterInfo get(long id) {
        String sql = "select * from apigw_gportal_registry_center where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new RegistryCenterInfoRowMapper());
    }

    @Override
    public List<RegistryCenterInfo> findAll() {
        String sql = "select * from apigw_gportal_registry_center order by id desc";
        return namedParameterJdbcTemplate.query(sql, new RegistryCenterInfoRowMapper());
    }

    @Override
    public List<RegistryCenterInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_registry_center where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new RegistryCenterInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_registry_center where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class RegistryCenterInfoRowMapper implements RowMapper<RegistryCenterInfo> {
        @Override
        public RegistryCenterInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RegistryCenterInfo registryCenterInfo = new RegistryCenterInfo();
            registryCenterInfo.setId(rs.getLong("id"));
            registryCenterInfo.setRegistryType(rs.getString("registry_type"));
            registryCenterInfo.setRegistryAddr(rs.getString("registry_addr"));
            registryCenterInfo.setRegistryAlias(rs.getString("registry_alias"));
            registryCenterInfo.setCreateDate(rs.getLong("create_date"));
            registryCenterInfo.setModifyDate(rs.getLong("modify_date"));
            registryCenterInfo.setProjectId(rs.getLong("project_id"));
            registryCenterInfo.setIsShared(rs.getInt("is_shared"));
            registryCenterInfo.setGwId(rs.getLong("gw_id"));
            return registryCenterInfo;
        }
    }
}
