package org.hango.cloud.dashboard.apiserver.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.dao.ServiceInfoDao;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.util.Const;
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
 * @Date: 创建时间: 2017/12/26 16:49.
 */
@Component
public class ServiceInfoDaoImpl extends BaseDao implements ServiceInfoDao {

    @Override
    public long add(ServiceInfo serviceInfo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        //注意serviceName对应数据库中displayName,serviceTag对应数据库中serviceName
        String sql = "insert into apigw_gportal_service (create_date, modify_date, display_name, service_name, contacts, description,service_type, wsdl_url, project_id,sync_status,ext_service_id) "
                + " values (:createDate, :modifyDate, :displayName, :serviceName, :contacts, :description,:serviceType, :wsdlUrl, :projectId,:syncStatus,:extServiceId)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceInfo);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public long update(ServiceInfo serviceInfo) {
        String sql = "update apigw_gportal_service set modify_date = :modifyDate, display_name = :displayName, service_name=:serviceName, contacts=:contacts, " +
                "description=:description, wsdl_url=:wsdlUrl, status=:status, health_interface_path=:healthInterfacePath,sync_status=:syncStatus,ext_service_id=:extServiceId where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceInfo);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceInfo serviceInfo) {
        return 0;
    }

    @Override
    public void delete(long serviceId) {
        String sql = "DELETE from apigw_gportal_service where id = :serviceId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("serviceId", serviceId));
    }

    @Override
    public long updateStatus(long serviceId) {
        String sql = "update apigw_gportal_service set status = 1 where id=:serviceId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("serviceId", serviceId));
    }

    @Override
    public ServiceInfo get(long id) {
        String sql = "select * from apigw_gportal_service where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> findAll() {
        String sql = "select * from apigw_gportal_service order by id desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> findAllOrderByCreateDateDesc() {
        String sql = "select * from apigw_gportal_service order by create_date desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> getServiceByProjectId(long projectId) {
        String sql = "select * from apigw_gportal_service where project_id=:projectId order by id desc ";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource("projectId", projectId), new ServiceInfoRowMapper());
    }


    @Override
    public List<ServiceInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from apigw_gportal_service where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from apigw_gportal_service where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<ServiceInfo> getServiceByServiceName(String serviceName) {
        String sql = "select * from apigw_gportal_service where service_name=:serviceName";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource("serviceName", serviceName), new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> getServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from apigw_gportal_service where project_id=:projectId and (service_name like :pattern or display_name like :pattern) order by id desc limit :limit offset :offset";
            params.put("pattern", "%" + pattern + "%");
        } else {
            sql = "select * from apigw_gportal_service where project_id=:projectId order by id desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("projectId", projectId);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    @Override
    public long getServiceCountByProjectId(String pattern, long projectId) {
        String sql = "select count(*) from apigw_gportal_service where project_id=:projectId and (service_name like :pattern or display_name like :pattern)";
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("pattern", "%" + pattern + "%");
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public List<Long> getServiceIdListByDisplayNameFuzzy(String serviceName, long projectId) {
        String sql = "select id from apigw_gportal_service where project_id=:projectId and display_name like :serviceName";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("projectId", projectId);
        params.put("serviceName", "%" + serviceName + "%");
        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public List<ServiceInfo> getServiceInfoList(List<Long> serviceIdList) {
        String sql = "select * from apigw_gportal_service where id in (:serviceIdList)";
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceIdList", serviceIdList);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    class ServiceInfoRowMapper implements RowMapper<ServiceInfo> {
        @Override
        public ServiceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setId(rs.getLong("id"));
            serviceInfo.setCreateDate(rs.getLong("create_date"));
            serviceInfo.setModifyDate(rs.getLong("modify_date"));
            serviceInfo.setDisplayName(rs.getString("display_name"));
            serviceInfo.setServiceName(rs.getString("service_name"));
            serviceInfo.setContacts(rs.getString("contacts"));
            serviceInfo.setStatus(rs.getInt("status"));
            serviceInfo.setServiceType(rs.getString("service_type"));
            serviceInfo.setWsdlUrl(rs.getString("wsdl_url"));
            serviceInfo.setDescription(rs.getString("description"));
            serviceInfo.setHealthInterfacePath(rs.getString("health_interface_path"));
            serviceInfo.setProjectId(rs.getLong("project_id"));
            serviceInfo.setSyncStatus(rs.getInt("sync_status"));
            serviceInfo.setExtServiceId(rs.getLong("ext_service_id"));
            return serviceInfo;
        }
    }
}
