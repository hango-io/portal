package org.hango.cloud.envoy.infra.dubbo.dao.impl;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.dubbo.dao.IDubboMetaDao;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboMeta;
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
 * @Desc Dubbo 元数据信息表
 * @date 2021/09/15
 */
@Component
public class DubboMetaDaoImpl extends BaseDao implements IDubboMetaDao {

    private static final Logger logger = LoggerFactory.getLogger(DubboMetaDaoImpl.class);

    @Override
    public long add(DubboMeta dubboMeta) {
        String sql = "insert into hango_dubbo_meta (application_name, protocol_version, interface_name, dubbo_group, dubbo_version, method, dubbo_params, dubbo_returns, create_time, virtual_gw_id)"
                + "values(:applicationName, :protocolVersion, :interfaceName, :dubboGroup, :dubboVersion, :method, :dubboParams, :dubboReturns, :createTime, :virtualGwId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(dubboMeta);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addDubboMetaInfo {}", ReflectionToStringBuilder.toString(dubboMeta, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(DubboMeta dubboMeta) {
        String sql = "update hango_dubbo_meta set application_name=:applicationName, protocol_version=:protocolVersion, interface_name=:interfaceName, dubbo_group=:dubboGroup, dubbo_version=:dubboVersion, method=:method, dubbo_params=:dubboParams, dubbo_returns=:dubboReturns, create_time=:createTime, virtual_gw_id=:virtualGwId where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(dubboMeta);
        logger.info("DubboMeta {}", ReflectionToStringBuilder.toString(dubboMeta, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(DubboMeta dubboMeta) {
        String sql = "delete from hango_dubbo_meta where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(dubboMeta);
        logger.info("deleteDubboMetaInfo {}", ReflectionToStringBuilder.toString(dubboMeta, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public DubboMeta get(long id) {
        String sql = "select * from hango_dubbo_meta where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new DubboMetaRowMapper());
    }

    @Override
    public List<DubboMeta> findAll() {
        String sql = "select * from hango_dubbo_meta";
        return namedParameterJdbcTemplate.query(sql, new DubboMetaRowMapper());
    }

    @Override
    public List<DubboMeta> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_dubbo_meta where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new DubboMetaRowMapper());
    }

    @Override
    public List<DubboMeta> getRecordsByField(Map<String, Object> params, long offset, long limit) {
        String head = "select * from hango_dubbo_meta where ";
        String sql = getQueryCondition(head, params);
        sql += " limit :offset, :limit";
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new DubboMetaRowMapper());
    }

    @Override
    public void batchDeleteByCondition(long virtualGwId, String interfaceName, String group, String version) {
        String sql = "delete from hango_dubbo_meta where interface_name=:interfaceName and dubbo_group=:dubboGroup and dubbo_version=:dubboVersion and virtual_gw_id=:virtualGwId";
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("interfaceName", interfaceName);
        mapSqlParameterSource.addValue("virtualGwId", virtualGwId);
        mapSqlParameterSource.addValue("dubboGroup", group);
        mapSqlParameterSource.addValue("dubboVersion", version);
        logger.info("deleteDubboMetaInfo virtualGwId = {} interfaceName = {} ,group = {} ,version = {}", virtualGwId, interfaceName, group, version);
        namedParameterJdbcTemplate.update(sql, mapSqlParameterSource);
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_dubbo_meta where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class DubboMetaRowMapper implements RowMapper<DubboMeta> {

        @Override
        public DubboMeta mapRow(ResultSet rs, int rowNum) throws SQLException {
            DubboMeta info = new DubboMeta();
            info.setId(rs.getLong("id"));
            info.setApplicationName(rs.getString("application_name"));
            info.setProtocolVersion(rs.getString("protocol_version"));
            info.setInterfaceName(rs.getString("interface_name"));
            info.setDubboGroup(rs.getString("dubbo_group"));
            info.setDubboVersion(rs.getString("dubbo_version"));
            info.setMethod(rs.getString("method"));
            info.setDubboParams(rs.getString("dubbo_params"));
            info.setDubboReturns(rs.getString("dubbo_returns"));
            info.setCreateTime(rs.getLong("create_time"));
            info.setVirtualGwId(rs.getLong("virtual_gw_id"));

            return info;
        }
    }
}
