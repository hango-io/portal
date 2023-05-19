package org.hango.cloud.envoy.infra.dubbo.dao.impl;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.dubbo.dao.IDubboBindingDao;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboBindingInfo;
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
public class DubboBindingDaoImpl extends BaseDao implements IDubboBindingDao {

    private static final Logger logger = LoggerFactory.getLogger(DubboBindingDaoImpl.class);

    @Override
    public long add(DubboBindingInfo t) {
        String sql = "insert into hango_dubbo_binding (object_id, object_type, dubbo_info)"
                + "values(:objectId, :objectType, :dubboInfo)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(DubboBindingInfo t) {
        String sql = "update hango_dubbo_binding set object_type=:objectType, dubbo_info=:dubboInfo where object_id=:objectId";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        logger.info("updateEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(DubboBindingInfo t) {
        String sql = "delete from hango_dubbo_binding where object_id=:objectId";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(t);
        logger.info("deleteEnvoyDubboInfo {}", ReflectionToStringBuilder.toString(t, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public DubboBindingInfo get(long id) {
        String sql = "select * from hango_dubbo_binding where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyDubboInfoRowMapper());
    }

    @Override
    public List<DubboBindingInfo> findAll() {
        String sql = "select * from hango_dubbo_binding";
        return namedParameterJdbcTemplate.query(sql, new EnvoyDubboInfoRowMapper());
    }

    @Override
    public List<DubboBindingInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_dubbo_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyDubboInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_dubbo_binding where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    class EnvoyDubboInfoRowMapper implements RowMapper<DubboBindingInfo> {
        @Override
        public DubboBindingInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            DubboBindingInfo dubboBindingInfo = new DubboBindingInfo();
            dubboBindingInfo.setId(rs.getLong("id"));
            dubboBindingInfo.setObjectId(rs.getLong("object_id"));
            dubboBindingInfo.setObjectType(rs.getString("object_type"));
            dubboBindingInfo.setDubboInfo(rs.getString("dubbo_info"));
            return dubboBindingInfo;
        }
    }
}
