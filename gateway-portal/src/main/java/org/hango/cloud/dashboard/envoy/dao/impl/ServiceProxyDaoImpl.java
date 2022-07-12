package org.hango.cloud.dashboard.envoy.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.impl.BaseDao;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IServiceProxyDao;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关元服务关联至envoy网关服务相关dao
 *
 * @author hanjiahao
 */
@Component
public class ServiceProxyDaoImpl extends BaseDao implements IServiceProxyDao {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyDaoImpl.class);

    @Override
    public long add(ServiceProxyInfo serviceProxyInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into apigw_service_proxy (service_id, code, publish_protocol, backend_service, publish_type, gw_id, project_id, create_time, update_time, load_balancer, subsets, registry_center_addr, registry_center_type, traffic_policy, gw_type) "
                + " values (:serviceId, :code, :publishProtocol, :backendService, :publishType, :gwId, :projectId, :createTime, :updateTime, :loadBalancer, :subsets, :registryCenterAddr, :registryCenterType, :trafficPolicy, :gwType)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add ServiceProxyInfo: {}", serviceProxyInfo);
        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(ServiceProxyInfo serviceProxyInfo) {
        String sql = "update apigw_service_proxy set service_id=:serviceId, code=:code, publish_protocol=:publishProtocol, backend_service=:backendService, publish_type=:publishType, gw_id=:gwId, update_time=:updateTime, load_balancer=:loadBalancer, subsets=:subsets, registry_center_addr=:registryCenterAddr, registry_center_type=:registryCenterType," +
                " traffic_policy=:trafficPolicy, gw_type=:gwType where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        logger.info("update ServiceProxyInfo: {}", serviceProxyInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceProxyInfo serviceProxyInfo) {
        String sql = "delete from apigw_service_proxy where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        logger.info("delete EnvoyServiceInfo: {}", serviceProxyInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ServiceProxyInfo get(long id) {
        String sql = "select * from apigw_service_proxy where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new EnvoyServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> findAll() {
        String sql = "select * from apigw_service_proxy";
        return namedParameterJdbcTemplate.query(sql, new EnvoyServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_service_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyServiceProxyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_service_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getCount(long gwId, List<Long> serviceId, long projectId) {
        String sql = "select count(*) from apigw_service_proxy where gw_id = :gwId and project_id=:projectId "
                + "and service_id in (:serviceId)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("projectId", projectId);
        params.put("serviceId", serviceId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, List<Long> serviceId, long projectId,
                                                         long offset, long limit) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (NumberUtils.INTEGER_ZERO != gwId) {
            sql = "select * from apigw_service_proxy where gw_id=:gwId and project_id=:projectId  order by id desc limit :limit offset :offset";
            if (CollectionUtils.isNotEmpty(serviceId)) {
                sql = "select * from apigw_service_proxy where service_id in (:serviceId) and gw_id=:gwId and "
                        + "project_id=:projectId  order by id desc limit :limit offset :offset";
                params.put("serviceId", serviceId);
            }
            params.put("gwId", gwId);
        } else {
            sql = "select * from apigw_service_proxy where project_id=:projectId order by id desc limit :limit offset :offset";
            if (CollectionUtils.isNotEmpty(serviceId)) {
                sql = "select * from apigw_service_proxy where service_id in (:serviceId) and "
                        + "project_id=:projectId order by id desc limit :limit offset :offset";
                params.put("serviceId", serviceId);
            }
        }
        params.put("projectId", projectId);
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, long serviceId, long projectId, long offset, long limit) {
        List<Long> serviceIds = new ArrayList<>();
        if (NumberUtils.INTEGER_ZERO != serviceId) serviceIds.add(serviceId);
        return getServiceProxyByLimit(gwId, serviceIds, projectId, offset, limit);
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByLimit(long gwId, long serviceId, long projectId,
                                                         List<Long> authServiceId, long offset, long limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql =
                "select * from apigw_service_proxy where gw_id=:gwId and project_id=:projectId and service_id in"
                        + " (:authServiceId) order by id " + "desc limit :limit offset :offset";
        if (NumberUtils.INTEGER_ZERO != serviceId) {
            sql = "select * from apigw_service_proxy where service_id=:serviceId and gw_id=:gwId and "
                    + "project_id=:projectId and service_id in (:authServiceId) order by id desc limit :limit offset"
                    + " :offset";
            params.put("serviceId", serviceId);
        }
        params.put("authServiceId", authServiceId);
        params.put("gwId", gwId);
        params.put("projectId", projectId);
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyServiceProxyRowMapper());
    }

    @Override
    public long getAuthServiceProxyCount(final long gwId, final long serviceId, final long projectId,
                                         final List<Long> authServiceId) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql;
        if (NumberUtils.INTEGER_ZERO != serviceId) {
            sql = "select count(*) from apigw_service_proxy where service_id=:serviceId and gw_id=:gwId and "
                    + "service_id in (:authServiceId)";
            params.put("serviceId", serviceId);
        } else {
            sql = "select count(*) from apigw_service_proxy where gw_id=:gwId and project_id=:projectId and "
                    + "service_id in (:authServiceId) ";
            params.put("projectId", projectId);
        }
        params.put("authServiceId", authServiceId);
        params.put("gwId", gwId);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<ServiceProxyInfo> batchGetServiceProxyList(long gwId, List<Long> serviceIdList) {
        String sql = "select * from apigw_service_proxy where service_id in (:serviceIdList) and gw_id=:gwId";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceIdList", serviceIdList);
        params.put("gwId", gwId);
        return namedParameterJdbcTemplate.query(sql, params, new EnvoyServiceProxyRowMapper());
    }

    class EnvoyServiceProxyRowMapper implements RowMapper<ServiceProxyInfo> {
        @Override
        public ServiceProxyInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServiceProxyInfo serviceProxyInfo = new ServiceProxyInfo();
            serviceProxyInfo.setId(rs.getLong("id"));
            serviceProxyInfo.setCreateTime(rs.getLong("create_time"));
            serviceProxyInfo.setUpdateTime(rs.getLong("update_time"));
            serviceProxyInfo.setCode(rs.getString("code"));
            serviceProxyInfo.setServiceId(rs.getLong("service_id"));
            serviceProxyInfo.setBackendService(rs.getString("backend_service"));
            serviceProxyInfo.setPublishType(rs.getString("publish_type"));
            serviceProxyInfo.setGwId(rs.getLong("gw_id"));
            serviceProxyInfo.setGwType(rs.getString("gw_type"));
            serviceProxyInfo.setProjectId(rs.getLong("project_id"));
            serviceProxyInfo.setPublishProtocol(rs.getString("publish_protocol"));
            serviceProxyInfo.setLoadBalancer(rs.getString("load_balancer"));
            serviceProxyInfo.setTrafficPolicy(rs.getString("traffic_policy"));
            serviceProxyInfo.setSubsets(rs.getString("subsets"));
            serviceProxyInfo.setRegistryCenterAddr(rs.getString("registry_center_addr"));
            serviceProxyInfo.setRegistryCenterType(rs.getString("registry_center_type"));
            return serviceProxyInfo;
        }
    }
}
