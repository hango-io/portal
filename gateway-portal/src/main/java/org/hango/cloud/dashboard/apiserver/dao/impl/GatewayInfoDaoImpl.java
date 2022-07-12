package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.GatewayInfoDao;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.util.Const;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/17 下午5:25.
 */
@Component
public class GatewayInfoDaoImpl extends BaseDao implements GatewayInfoDao {

    private static final Logger logger = LoggerFactory.getLogger(GatewayInfoDaoImpl.class);

    @Override
    public long add(GatewayInfo gatewayInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "insert into apigw_gportal_gateway_info (gw_name, gw_addr, create_date, modify_date, description, health_interface_path,project_id,env_id,auth_addr,mongo_addr,mysql_addr,audit_datasource_switch,gw_uni_id,metric_url,api_plane_addr, gw_cluster_name, gw_type,audit_db_config,prom_addr,camel_addr) "
                + " values (:gwName, :gwAddr, :createDate, :modifyDate, :description, :healthInterfacePath,:projectId,:envId,:authAddr,:mongoAddr,:mysqlAddr,:auditDatasourceSwitch,:gwUniId,:metricUrl, :apiPlaneAddr, :gwClusterName, :gwType, :auditDbConfig, :promAddr, :camelAddr)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(gatewayInfo);
        logger.info("add GatewayInfo: {}", ReflectionToStringBuilder.toString(gatewayInfo, ToStringStyle.SIMPLE_STYLE));
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(GatewayInfo gatewayInfo) {
        String sql = "update apigw_gportal_gateway_info set gw_name = :gwName, gw_addr=:gwAddr, description=:description, status=:status, " +
                "last_check_time=:lastCheckTime, modify_date=:modifyDate, health_interface_path=:healthInterfacePath,project_id=:projectId, env_id=:envId,auth_addr=:authAddr, mongo_addr=:mongoAddr ,mysql_addr=:mysqlAddr,audit_datasource_switch=:auditDatasourceSwitch, gw_uni_id=:gwUniId, metric_url=:metricUrl, " +
                "audit_db_config=:auditDbConfig, prom_addr=:promAddr, api_plane_addr=:apiPlaneAddr, gw_cluster_name=:gwClusterName, camel_addr=:camelAddr where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(gatewayInfo);
        logger.info("update GatewayInfo: {}", ReflectionToStringBuilder.toString(gatewayInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(GatewayInfo gatewayInfo) {
        return 0;
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE from apigw_gportal_gateway_info where id = :id";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public List<GatewayInfo> get(String gwName) {
        String sql = "select * from apigw_gportal_gateway_info where gw_name = :gwName";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource("gwName", gwName), new GatewayInfoRowMapper());
    }

    @Override
    public GatewayInfo get(long id) {
        String sql = "select * from apigw_gportal_gateway_info where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new GatewayInfoRowMapper());
    }

    @Override
    public List<GatewayInfo> findAll() {
        String sql = "select * from apigw_gportal_gateway_info order by gw_type desc";
        return namedParameterJdbcTemplate.query(sql, new GatewayInfoRowMapper());
    }

    @Override
    public List<GatewayInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_gateway_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_gateway_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<GatewayInfo> getGatewayInfoByLimit(String pattern, long offset, long limit) {
        String sql = null;
        Map<String, Object> params = new HashMap<String, Object>();
        //支持根据网关名称的模糊匹配
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from apigw_gportal_gateway_info where gw_name like :pattern or env_id like :pattern order by gw_type desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from apigw_gportal_gateway_info order by gw_type desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayInfoRowMapper());
    }

    @Override
    public List<GatewayInfo> getGatewayInfoByProjectIdAndLimit(String pattern, long offset, long limit, long projectId) {
        String sql = null;
        Map<String, Object> params = new HashMap<String, Object>();
        //支持根据网关名称的模糊匹配
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from apigw_gportal_gateway_info where find_in_set(:projectId,project_id) and (gw_name like :pattern or env_id like :pattern) order by gw_type desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from apigw_gportal_gateway_info where find_in_set(:projectId,project_id) order by gw_type desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayInfoRowMapper());
    }

    @Override
    public List<GatewayInfo> getGatewayInfoByProjectId(String pattern, long projectId) {
        String sql = null;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from apigw_gportal_gateway_info where find_in_set(:projectId,project_id) and (gw_name like :pattern or env_id like :pattern)  order by id desc";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from apigw_gportal_gateway_info where find_in_set(:projectId,project_id) order by id desc";
        }
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayInfoRowMapper());
    }

    @Override
    public long getGatewayInfoCountsByPattern(String pattern) {
        String sql = "select count(*) from apigw_gportal_gateway_info where gw_name like :pattern or env_id like :pattern";
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", "%" + pattern + "%");
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Long> getGwIdListByNameFuzzy(String gwName, long projectId) {
        String sql = "select id from apigw_gportal_gateway_info where gw_name like :gwName and find_in_set(:projectId, project_id)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwName", "%" + gwName + "%");
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public List<GatewayInfo> getGatewayInfoList(List<Long> gwIdList) {
        String sql = "select * from apigw_gportal_gateway_info where id in (:gwIdList);";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwIdList", gwIdList);
        return namedParameterJdbcTemplate.query(sql, params, new GatewayInfoRowMapper());
    }

    class GatewayInfoRowMapper implements RowMapper<GatewayInfo> {
        @Override
        public GatewayInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setId(rs.getLong("id"));
            gatewayInfo.setCreateDate(rs.getLong("create_date"));
            gatewayInfo.setModifyDate(rs.getLong("modify_date"));
            gatewayInfo.setGwName(rs.getString("gw_name"));
            gatewayInfo.setGwAddr(rs.getString("gw_addr"));
            gatewayInfo.setDescription(rs.getString("description"));
            gatewayInfo.setStatus(rs.getInt("status"));
            gatewayInfo.setLastCheckTime(rs.getLong("last_check_time"));
            gatewayInfo.setHealthInterfacePath(rs.getString("health_interface_path"));
            gatewayInfo.setProjectId(rs.getString("project_id"));
            gatewayInfo.setEnvId(rs.getString("env_id"));
            gatewayInfo.setAuthAddr(rs.getString("auth_addr"));
            gatewayInfo.setMongoAddr(rs.getString("mongo_addr"));
            gatewayInfo.setMysqlAddr(rs.getString("mysql_addr"));
            gatewayInfo.setAuditDatasourceSwitch(rs.getString("audit_datasource_switch"));
            gatewayInfo.setGwUniId(rs.getString("gw_uni_id"));
            gatewayInfo.setMetricUrl(rs.getString("metric_url"));
            gatewayInfo.setGwType(rs.getString("gw_type"));
            gatewayInfo.setApiPlaneAddr(rs.getString("api_plane_addr"));
            gatewayInfo.setGwClusterName(rs.getString("gw_cluster_name"));
            gatewayInfo.setAuditDbConfig(rs.getString("audit_db_config"));
            gatewayInfo.setPromAddr(rs.getString("prom_addr"));
            gatewayInfo.setCamelAddr(rs.getString("camel_addr"));
            return gatewayInfo;
        }
    }
}
