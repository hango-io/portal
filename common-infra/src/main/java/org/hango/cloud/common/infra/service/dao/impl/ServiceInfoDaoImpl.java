package org.hango.cloud.common.infra.service.dao.impl;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.service.dao.IServiceInfoDao;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;
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
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 服务元信息
 * @date 2022/09/05
 */
@Component
public class ServiceInfoDaoImpl extends BaseDao implements IServiceInfoDao {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInfoDaoImpl.class);


    @Override
    public long add(ServiceInfo serviceInfo) {
        String sql = "insert into hango_service_info (create_date, modify_date, display_name, service_name, contacts, description, status, service_type, project_id, extension_info)"
                + "values(:createDate, :modifyDate, :displayName, :serviceName, :contacts, :description, :status, :serviceType, :projectId, :extensionInfo)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceInfo);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addServiceInfoInfo {}", ReflectionToStringBuilder.toString(serviceInfo, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(ServiceInfo serviceInfo) {
        String sql = "update hango_service_info set create_date=:createDate, modify_date=:modifyDate, display_name=:displayName, service_name=:serviceName, contacts=:contacts, description=:description, status=:status, service_type=:serviceType, project_id=:projectId, extension_info=:extensionInfo where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceInfo);
        logger.info("ServiceInfo {}", ReflectionToStringBuilder.toString(serviceInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(ServiceInfo serviceInfo) {
        String sql = "delete from hango_service_info where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(serviceInfo);
        logger.info("deleteServiceInfoInfo {}", ReflectionToStringBuilder.toString(serviceInfo, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public ServiceInfo get(long id) {
        String sql = "select * from hango_service_info where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> findAll() {
        String sql = "select * from hango_service_info";
        return namedParameterJdbcTemplate.query(sql, new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_service_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> getRecordsByField(Map<String, Object> params, long offset, long limit) {
        String head = "select * from hango_service_info where ";
        String sql = getQueryCondition(head, params);
        sql += " limit :offset, :limit";
        params.put("offset", offset);
        params.put("limit", limit);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_service_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    @Override
    public List<ServiceInfo> getServiceByProjectId(long projectId) {
        String sql = "select * from hango_service_info where project_id=:projectId order by id desc ";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(BaseConst.PROJECT_ID, projectId), new ServiceInfoRowMapper());
    }


    @Override
    public List<ServiceInfo> getServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from hango_service_info where project_id=:projectId and (service_name like :pattern or display_name like :pattern) order by id desc limit :limit offset :offset";
            params.put(BaseConst.PATTERN, "%" + pattern + "%");
        } else {
            sql = "select * from hango_service_info where project_id=:projectId order by id desc limit :limit offset :offset";
        }
        params.put("offset", offset);
        params.put("limit", limit);
        params.put(BaseConst.PROJECT_ID, projectId);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    @Override
    public List<ServiceInfo> findAllServiceByDisplayName(String pattern, int status, long projectId) {
        String sql;
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(pattern)) {
            sql = "select * from hango_service_info where project_id=:projectId and status=:status and display_name like :pattern";
            params.put(BaseConst.PATTERN, "%" + pattern + "%");
        } else {
            sql = "select * from hango_service_info where project_id=:projectId and status=:status ";
        }
        params.put("status", status);
        params.put(BaseConst.PROJECT_ID, projectId);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }


    @Override
    public long getServiceCountByProjectId(String pattern, long projectId) {
        String sql = "select count(*) from hango_service_info where project_id=:projectId and (service_name like :pattern or display_name like :pattern)";
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.PROJECT_ID, projectId);
        params.put(BaseConst.PATTERN, "%" + pattern + "%");
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public void delete(long serviceId) {
        String sql = "DELETE from hango_service_info where id = :serviceId";
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("serviceId", serviceId));
    }

    @Override
    public long updateStatus(long serviceId) {
        String sql = "update hango_service_info set status = 1 where id=:serviceId";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("serviceId", serviceId));
    }

    @Override
    public List<ServiceInfo> findAllOrderByCreateDateDesc() {
        String sql = "select * from hango_service_info order by create_date desc";
        return namedParameterJdbcTemplate.query(sql, new ServiceInfoRowMapper());
    }


    @Override
    public List<Long> getServiceIdListByDisplayNameFuzzy(String serviceName, long projectId) {
        String sql = "select id from hango_service_info where project_id=:projectId and display_name like :serviceName";
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.PROJECT_ID, projectId);
        params.put("serviceName", "%" + serviceName + "%");
        return namedParameterJdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public List<ServiceInfo> getServiceInfoList(List<Long> serviceIdList) {
        String sql = "select * from hango_service_info where id in (:serviceIdList)";
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceIdList", serviceIdList);
        return namedParameterJdbcTemplate.query(sql, params, new ServiceInfoRowMapper());
    }

    class ServiceInfoRowMapper implements RowMapper<ServiceInfo> {

        @Override
        public ServiceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ServiceInfo info = new ServiceInfo();
            info.setId(rs.getLong("id"));
            info.setCreateDate(rs.getLong("create_date"));
            info.setModifyDate(rs.getLong("modify_date"));
            info.setDisplayName(rs.getString("display_name"));
            info.setServiceName(rs.getString("service_name"));
            info.setContacts(rs.getString("contacts"));
            info.setDescription(rs.getString("description"));
            info.setStatus(rs.getInt("status"));
            info.setServiceType(rs.getString("service_type"));
            info.setProjectId(rs.getLong("project_id"));
            info.setExtensionInfo(rs.getString("extension_info"));

            return info;
        }
    }
}
