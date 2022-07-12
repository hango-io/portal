package org.hango.cloud.dashboard.envoy.dao.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyVirtualHostInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * vh dao层实现类
 *
 * @author hzchenzhongyang 2020-01-08
 */
@Component
public class EnvoyVirtualHostInfoDaoImpl extends BaseDao implements IEnvoyVirtualHostInfoDao {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyVirtualHostInfoDaoImpl.class);

    @Override
    public long add(EnvoyVirtualHostInfo envoyVirtualHostInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_envoy_virtual_host_info (project_id, gw_id, hosts, virtual_host_code, bind_type, projects, create_time, update_time) "
                + " values (:projectId, :gwId, :hosts, :virtualHostCode, :bindType, :projects, :createTime, :updateTime)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyVirtualHostInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add envoyVirtualHostInfo: {}", envoyVirtualHostInfo);
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(EnvoyVirtualHostInfo envoyVirtualHostInfo) {
        String sql = "update apigw_envoy_virtual_host_info set bind_type=:bindType, hosts=:hosts, projects=:projects, update_time=:updateTime where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyVirtualHostInfo);
        logger.info("update envoyVirtualHostInfo: {}", envoyVirtualHostInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(EnvoyVirtualHostInfo envoyVirtualHostInfo) {
        String sql = "delete from apigw_envoy_virtual_host_info where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(envoyVirtualHostInfo);
        logger.info("delete envoyVirtualHostInfo: {}", envoyVirtualHostInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public EnvoyVirtualHostInfo get(long id) {
        String sql = "select * from apigw_envoy_virtual_host_info where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyVirtualHostInfoRowMapper());
    }

    @Override
    public List<EnvoyVirtualHostInfo> findAll() {
        String sql = "select * from apigw_envoy_virtual_host_info";
        return namedParameterJdbcTemplate.query(sql, new EnvoyVirtualHostInfoRowMapper());
    }

    @Override
    public List<EnvoyVirtualHostInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_envoy_virtual_host_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyVirtualHostInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_envoy_virtual_host_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    @Override
    public List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, List<Long> projectIdList, String domain, String bindType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        String sql = "select * from apigw_envoy_virtual_host_info where 1=1 ";
        if (0 < gwId) {
            sql = sql + " and gw_id=:gwId ";
            params.put("gwId", gwId);
        }
        if (StringUtils.isNotBlank(bindType)) {
            sql = sql + " and bind_type= :bindType";
            params.put("bindType", bindType);
        }
        if (!CollectionUtils.isEmpty(projectIdList)) {
            sql = sql + " and project_id in (:projectIdList)";
            params.put("projectIdList", projectIdList);
        }
        if (StringUtils.isNotBlank(domain)) {
            sql = sql + " and hosts like :domain";
            params.put("domain", "%" + domain + "%");
        }
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyVirtualHostInfoRowMapper());
    }

    @Override
    public List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, String bindType) {
        String sql = "select * from apigw_envoy_virtual_host_info where gw_id=:gwId and bind_type=:bindType";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("bindType", bindType);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyVirtualHostInfoRowMapper());
    }

    @Override
    public EnvoyVirtualHostInfo getVirtualHostInfo(long gwId, long projectId) {
        String sql = "select * from apigw_envoy_virtual_host_info where gw_id=:gwId and project_id=:projectId";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("projectId", projectId);
        return queryForObject(sql, params, new EnvoyVirtualHostInfoRowMapper());
    }


    class EnvoyVirtualHostInfoRowMapper implements RowMapper<EnvoyVirtualHostInfo> {
        @Override
        public EnvoyVirtualHostInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EnvoyVirtualHostInfo virtualHostInfo = new EnvoyVirtualHostInfo();
            virtualHostInfo.setId(rs.getLong("id"));
            virtualHostInfo.setGwId(rs.getLong("gw_id"));
            virtualHostInfo.setHosts(rs.getString("hosts"));
            virtualHostInfo.setProjectId(rs.getLong("project_id"));
            virtualHostInfo.setCreateTime(rs.getLong("create_time"));
            virtualHostInfo.setUpdateTime(rs.getLong("update_time"));
            virtualHostInfo.setVirtualHostCode(rs.getString("virtual_host_code"));
            virtualHostInfo.setHostList(JSON.parseArray(virtualHostInfo.getHosts(), String.class));
            virtualHostInfo.setProjects(rs.getString("projects"));
            virtualHostInfo.setProjectList(JSON.parseArray(virtualHostInfo.getProjects(), Long.class));
            virtualHostInfo.setBindType(rs.getString("bind_type"));
            return virtualHostInfo;
        }
    }
}
