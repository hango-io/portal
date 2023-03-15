package org.hango.cloud.common.infra.serviceproxy.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceproxy.dao.IServiceProxyDao;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.gdashboard.api.util.Const;
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
 * 网关元服务关联至网关服务相关dao
 *
 * @author hanjiahao
 */
@Component
public class ServiceProxyDaoImpl extends BaseDao implements IServiceProxyDao {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyDaoImpl.class);

    @Override
    public long add(ServiceProxyInfo serviceProxyInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_service_proxy (service_id, code, publish_protocol, backend_service, publish_type, virtual_gw_id, project_id, create_time, update_time, load_balancer, subsets, registry_center_type, traffic_policy, gw_type) "
                + " values (:serviceId, :code, :publishProtocol, :backendService, :publishType, :virtualGwId, :projectId, :createTime, :updateTime, :loadBalancer, :subsets, :registryCenterType, :trafficPolicy, :gwType)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("add ServiceProxyInfo: {}", serviceProxyInfo);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(ServiceProxyInfo serviceProxyInfo) {
        String sql = "update hango_service_proxy set service_id=:serviceId, code=:code, publish_protocol=:publishProtocol, backend_service=:backendService, publish_type=:publishType, virtual_gw_id=:virtualGwId, update_time=:updateTime, load_balancer=:loadBalancer, subsets=:subsets,  registry_center_type=:registryCenterType," +
                " traffic_policy=:trafficPolicy, gw_type=:gwType where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        logger.info("update ServiceProxyInfo: {}", serviceProxyInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceProxyInfo serviceProxyInfo) {
        String sql = "delete from hango_service_proxy where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceProxyInfo);
        logger.info("delete ServiceInfo: {}", serviceProxyInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ServiceProxyInfo get(long id) {
        String sql = "select * from hango_service_proxy where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> findAll() {
        String sql = "select * from hango_service_proxy";
        return namedParameterJdbcTemplate.query(sql, new ServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_service_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProxyRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_service_proxy where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getCount(long virtualGwId, List<Long> serviceId, long projectId) {
        StringBuilder sql = new StringBuilder("select count(*) from hango_service_proxy where  project_id=:projectId ");
        Map<String, Object> params = Maps.newHashMap();
        if (virtualGwId != NumberUtils.LONG_ZERO){
            sql.append(" and  virtual_gw_id =:virtualGwId ");
            params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        }
        sql.append(" and service_id in (:serviceId) ");
        params.put("serviceId", serviceId);
        params.put("projectId", projectId);

        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByLimit(long virtualGwId, List<Long> serviceId, long projectId,
                                                         long offset, long limit) {
        Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder();
        builder.append("select * from hango_service_proxy where project_id=:projectId ");
        if (NumberUtils.INTEGER_ZERO != virtualGwId) {
            builder.append("and virtual_gw_id=:virtualGwId ");
            params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        }
        if (!CollectionUtils.isEmpty(serviceId)){
            builder.append("and service_id in (:serviceId) ");
            params.put("serviceId", serviceId);
        }
        builder.append(" order by id desc limit :limit offset :offset ");
        params.put("projectId", projectId);
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(builder.toString(), params, new ServiceProxyRowMapper());
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByLimit(long virtualGwId, long serviceId, long projectId, long offset, long limit) {
        return getServiceProxyByLimit(virtualGwId, Lists.newArrayList(serviceId), projectId, offset, limit);
    }



    @Override
    public List<ServiceProxyInfo> batchGetServiceProxyList(long virtualGwId, List<Long> serviceIdList) {
        String sql = "select * from hango_service_proxy where service_id in (:serviceIdList) and virtual_gw_id=:virtualGwId";
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceIdList", serviceIdList);
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceProxyRowMapper());
    }

    @Override
    public long updateVersion(long id, long version) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("id", id);
        params.put("version", version);
        String sql = "update hango_service_proxy set version =:version where id =:id";
        return namedParameterJdbcTemplate.update(sql, params);
    }


    public static class ServiceProxyRowMapper implements RowMapper<ServiceProxyInfo> {
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
            serviceProxyInfo.setVirtualGwId(rs.getLong("virtual_gw_id"));
            serviceProxyInfo.setGwType(rs.getString("gw_type"));
            serviceProxyInfo.setProjectId(rs.getLong("project_id"));
            serviceProxyInfo.setPublishProtocol(rs.getString("publish_protocol"));
            serviceProxyInfo.setLoadBalancer(rs.getString("load_balancer"));
            serviceProxyInfo.setTrafficPolicy(rs.getString("traffic_policy"));
            serviceProxyInfo.setSubsets(rs.getString("subsets"));
            serviceProxyInfo.setRegistryCenterType(rs.getString("registry_center_type"));
            serviceProxyInfo.setVersion(rs.getLong("version"));
            return serviceProxyInfo;
        }
    }
}
