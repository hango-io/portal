package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hango.cloud.dashboard.apiserver.dao.IDubboDao;
import org.hango.cloud.dashboard.apiserver.meta.DubboInfo;
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
 * @date 2020/12/2
 */
@Component
public class DubboDaoImpl extends BaseDao implements IDubboDao {

    private static final Logger logger = LoggerFactory.getLogger(DubboDaoImpl.class);

    @Override
    public long add(DubboInfo t) {
        String sql = "insert into apigw_gportal_dubbo_info (object_id, object_type, dubbo_info)"
                + "values(:objectId, :objectType, :dubboInfo)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public long update(DubboInfo t) {
        String sql = "update apigw_gportal_dubbo_info set object_id=:objectId, object_type=:objectType, dubbo_info=:dubboInfo where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        logger.info("updateEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(DubboInfo t) {
        String sql = "delete from apigw_gportal_dubbo_info where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        logger.info("deleteEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public DubboInfo get(long id) {
        String sql = "select * from apigw_gportal_dubbo_info where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyDubboInfoRowMapper());
    }

    @Override
    public List<DubboInfo> findAll() {
        String sql = "select * from apigw_gportal_dubbo_info";
        return namedParameterJdbcTemplate.query(sql, new EnvoyDubboInfoRowMapper());
    }

    @Override
    public List<DubboInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_dubbo_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyDubboInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_dubbo_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    class EnvoyDubboInfoRowMapper implements RowMapper<DubboInfo> {
        @Override
        public DubboInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            DubboInfo dubboInfo = new DubboInfo();
            dubboInfo.setId(rs.getLong("id"));
            dubboInfo.setObjectId(rs.getLong("object_id"));
            dubboInfo.setObjectType(rs.getString("object_type"));
            dubboInfo.setDubboInfo(rs.getString("dubbo_info"));
            return dubboInfo;
        }
    }
}
