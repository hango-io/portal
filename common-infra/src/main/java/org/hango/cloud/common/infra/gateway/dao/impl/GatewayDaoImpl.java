package org.hango.cloud.common.infra.gateway.dao.impl;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.gateway.dao.IGatewayDao;
import org.hango.cloud.common.infra.gateway.meta.Gateway;
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
 * @Desc 网关信息表
 * @date 2022/10/25
 */ 
 @Component
public class GatewayDaoImpl extends BaseDao implements IGatewayDao {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayDaoImpl.class);

    @Override
    public long add(Gateway gateway) {
        String sql = "insert into hango_gateway (name, env_id, svc_type, svc_name, type, gw_cluster_name, conf_addr, description, create_time, modify_time)"
            + "values(:name, :envId, :svcType, :svcName, :type, :gwClusterName, :confAddr, :description, :createTime, :modifyTime)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(gateway);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addGatewayInfo {}", ReflectionToStringBuilder.toString(gateway, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(Gateway gateway) {
        String sql = "update hango_gateway set name=:name, env_id=:envId, svc_type=:svcType, svc_name=:svcName, type=:type, gw_cluster_name=:gwClusterName, conf_addr=:confAddr, description=:description, create_time=:createTime, modify_time=:modifyTime where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(gateway);
        logger.info("Gateway {}", ReflectionToStringBuilder.toString(gateway, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(Gateway gateway) {
        String sql = "delete from hango_gateway where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(gateway);
        logger.info("deleteGatewayInfo {}", ReflectionToStringBuilder.toString(gateway, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public Gateway get(long id) {
        String sql = "select * from hango_gateway where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new GatewayRowMapper());
    }

    @Override
    public List<Gateway> findAll() {
        String sql = "select * from hango_gateway";
        return namedParameterJdbcTemplate.query(sql, new GatewayRowMapper());
    }

    @Override
    public List<Gateway> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_gateway where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayRowMapper());
    }

    @Override
    public List<Gateway> getRecordsByField(Map<String, Object> params, long offset, long limit) {
        String head = "select * from hango_gateway where ";
        String sql = getQueryCondition(head, params);
        sql += " limit :offset, :limit";
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayRowMapper());
    }


     @Override
     public List<Gateway> getByName(String name, long offset, long limit) {
         StringBuilder sql = new StringBuilder("select * from hango_gateway where 1=1 ");
         Map<String, Object> params = Maps.newHashMap();
         if (StringUtils.isNotBlank(name)) {
             sql.append("and name like :name ");
             params.put("name", "%" + name + "%");
         }
         sql.append("limit :limit offset :offset");
         params.put("offset", offset);
         params.put("limit", limit);
         return namedParameterJdbcTemplate.query(sql.toString(), params, new GatewayRowMapper());
     }

     @Override
     public int countByName(String name) {
         StringBuilder sql = new StringBuilder("select count(*) from hango_gateway where 1=1 ");
         Map<String, Object> params = Maps.newHashMap();
         if (StringUtils.isNotBlank(name)) {
             sql.append("and name like :name ");
             params.put("name", "%" + name + "%");
         }
         return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Integer.class);

     }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_gateway where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class GatewayRowMapper implements RowMapper<Gateway> {

        @Override
        public Gateway mapRow(ResultSet rs, int rowNum) throws SQLException {
            Gateway info = new Gateway();
            info.setId(rs.getLong("id"));
            info.setName(rs.getString("name"));
			info.setEnvId(rs.getString("env_id"));
			info.setSvcType(rs.getString("svc_type"));
			info.setSvcName(rs.getString("svc_name"));
			info.setType(rs.getString("type"));
			info.setGwClusterName(rs.getString("gw_cluster_name"));
			info.setConfAddr(rs.getString("conf_addr"));
			info.setDescription(rs.getString("description"));
			info.setCreateTime(rs.getLong("create_time"));
			info.setModifyTime(rs.getLong("modify_time"));
            return info;
        }
    }
}
