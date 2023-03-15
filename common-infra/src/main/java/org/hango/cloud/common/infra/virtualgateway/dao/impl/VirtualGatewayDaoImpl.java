package org.hango.cloud.common.infra.virtualgateway.dao.impl;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_GATEWAY;
import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_INGRESS;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Time: 创建时间: 2018/1/17 下午5:25.
 */
@Component
public class VirtualGatewayDaoImpl extends BaseDao implements IVirtualGatewayDao {


    private static final Logger logger = LoggerFactory.getLogger(VirtualGatewayDaoImpl.class);

    @Override
    public long add(VirtualGateway virtualGateway) {
        String sql = "insert into hango_virtual_gateway (gw_id, name, code, addr, project_id, description, type, protocol, port, create_time, modify_time)"
                + "values(:gwId, :name, :code, :addr, :projectId, :description, :type, :protocol, :port, :createTime, :modifyTime)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(virtualGateway);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);
        logger.info("addVirtualGatewayInfo {}", ReflectionToStringBuilder.toString(virtualGateway, ToStringStyle.SIMPLE_STYLE));
        return keyHolder.getKey().longValue();
    }

    @Override
    public int update(VirtualGateway virtualGateway) {
        String sql = "update hango_virtual_gateway set gw_id=:gwId, name=:name, code=:code, addr=:addr, project_id=:projectId, description=:description, type=:type, protocol=:protocol, port=:port, create_time=:createTime, modify_time=:modifyTime where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(virtualGateway);
        logger.info("VirtualGateway {}", ReflectionToStringBuilder.toString(virtualGateway, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(VirtualGateway virtualGateway) {
        String sql = "delete from hango_virtual_gateway where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(virtualGateway);
        logger.info("deleteVirtualGatewayInfo {}", ReflectionToStringBuilder.toString(virtualGateway, ToStringStyle.SIMPLE_STYLE));
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public VirtualGateway get(long id) {
        String sql = "select * from hango_virtual_gateway where id=:id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new VirtualGatewayRowMapper());
    }

    @Override
    public List<VirtualGateway> findAll() {
        String sql = "select * from hango_virtual_gateway";
        return namedParameterJdbcTemplate.query(sql, new VirtualGatewayRowMapper());
    }

    @Override
    public List<VirtualGateway> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_virtual_gateway where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new VirtualGatewayRowMapper());
    }

    @Override
    public List<VirtualGateway> getRecordsByField(Map<String, Object> params, long offset, long limit) {
        String head = "select * from hango_virtual_gateway where ";
        String sql = getQueryCondition(head, params);
        sql += " limit :offset, :limit";
        params.put(BaseConst.OFFSET, offset);
        params.put(BaseConst.LIMIT, limit);
        return namedParameterJdbcTemplate.query(sql, params, new VirtualGatewayRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_virtual_gateway where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    @Override
    public long getGatewayInfoCountsByPattern(String pattern , long projectId) {
        StringBuilder sql = new StringBuilder("select count(*) from hango_virtual_gateway where 1=1");
        Map<String, Object> params = Maps.newHashMap();
        if (projectId > NumberUtils.LONG_ONE) {
            sql.append(" and find_in_set(:projectId,project_id) ");
            params.put(BaseConst.PROJECT_ID, projectId);
        }
        if (StringUtils.isNotBlank(pattern)){
            sql.append(" and name like :pattern ");
            params.put(BaseConst.PATTERN, "%" + pattern + "%");
        }
        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
    }

    @Override
    public List<Long> getGwIdListByNameFuzzy(String gwName, long projectId) {
        StringBuilder sql = new StringBuilder("select id from hango_virtual_gateway where name like :gwName ");
        Map<String, Object> params = Maps.newHashMap();
        if (projectId > NumberUtils.LONG_ONE) {
            sql.append("and find_in_set(:projectId,project_id)");
            params.put(BaseConst.PROJECT_ID, projectId);
        }
        if (StringUtils.isNotBlank(gwName)){
            sql.append(" and name like :gwName ");
            params.put(BaseConst.PATTERN, "%" + gwName + "%");
        }
        params.put("gwName", "%" + gwName + "%");
        return namedParameterJdbcTemplate.queryForList(sql.toString(), params, Long.class);
    }

    @Override
    public List<VirtualGateway> getGatewayInfoList(List<Long> gwIdList) {
        String sql = "select * from hango_virtual_gateway where id in (:gwIdList);";
        Map<String, Object> params = Maps.newHashMap();
        params.put("gwIdList", gwIdList);
        return namedParameterJdbcTemplate.query(sql, params, new VirtualGatewayRowMapper());
    }


    @Override
    public List<VirtualGateway> getGatewayInfoByProjectIdAndLimit(String pattern, long projectId, long offset, long limit) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        sql.append("select * from hango_virtual_gateway where 1=1 ");
        if (projectId > NumberUtils.LONG_ONE) {
            sql.append("and find_in_set(:projectId,project_id) ");
            params.put(BaseConst.PROJECT_ID, projectId);
        }
        //支持根据网关名称的模糊匹配
        if (StringUtils.isNotBlank(pattern)) {
            sql.append("and (name like :pattern ) ");
            params.put(BaseConst.PATTERN, "%" + pattern + "%");
        }
        sql.append("order by type desc limit :limit offset :offset");
        params.put(BaseConst.OFFSET, offset);
        params.put(BaseConst.LIMIT, limit);

        return namedParameterJdbcTemplate.query(sql.toString(), params, new VirtualGatewayRowMapper());
    }

    @Override
    public List<VirtualGateway> getManagedGatewayInfo(long projectId, String protocol) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("types", Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS));
        sql.append("select * from hango_virtual_gateway where type not in (:types) ");
        if (projectId > NumberUtils.LONG_ONE) {
            sql.append("and find_in_set(:projectId,project_id) ");
            params.put(BaseConst.PROJECT_ID, projectId);
        }
        if (StringUtils.isNotBlank(protocol)){
            sql.append("and protocol=:protocol");
            params.put("protocol", protocol);
        }
        return namedParameterJdbcTemplate.query(sql.toString(), params, new VirtualGatewayRowMapper());
    }

    @Override
    public List<VirtualGateway> getVirtualGatewayByConditions(QueryVirtualGatewayDto query) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        sql.append("select * from hango_virtual_gateway where 1=1 ");
        processMultiFindInSet(query, sql);
        //支持根据网关名称的模糊匹配
        if (StringUtils.isNotBlank(query.getPattern())) {
            sql.append("and (name like :pattern) ");
            params.put(BaseConst.PATTERN, "%" + query.getPattern() + "%");
        }

        if (query.isManaged()) {
            sql.append("and type not in (:types)");
            params.put("types", Arrays.asList(KUBERNETES_GATEWAY, KUBERNETES_INGRESS));
        }
        sql.append("order by type desc limit :limit offset :offset");
        params.put(BaseConst.OFFSET, query.getOffset());
        params.put(BaseConst.LIMIT, query.getLimit());

        return namedParameterJdbcTemplate.query(sql.toString(),params , new VirtualGatewayRowMapper());
    }

    @Override
    public Integer countVirtualGatewayByConditions(QueryVirtualGatewayDto query) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        sql.append("select count(*) from hango_virtual_gateway where 1=1 ");
        processMultiFindInSet(query, sql);
        //支持根据网关名称的模糊匹配
        if (StringUtils.isNotBlank(query.getPattern())) {
            sql.append("and (name like :pattern) ");
            params.put(BaseConst.PATTERN, "%" + query.getPattern() + "%");
        }
        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
    }

    /**
     * 处理填充多个数据使用 find_in_set 查询情况
     * @param query
     * @param sql
     */
    private void processMultiFindInSet(QueryVirtualGatewayDto query, StringBuilder sql) {
        if (CollectionUtils.isEmpty(query.getProjectIdList())) {
            return;
        }
        sql.append(" and (");
        String findInSetTemplate = " find_in_set(%d,project_id) ";
        Set<String> projectQuery = query.getProjectIdList().stream().map(p -> String.format(findInSetTemplate, p)).collect(Collectors.toSet());
        sql.append(StringUtils.joinWith("or", projectQuery.toArray()))
                        .append(")");
    }


    class VirtualGatewayRowMapper implements RowMapper<VirtualGateway> {

        @Override
        public VirtualGateway mapRow(ResultSet rs, int rowNum) throws SQLException {
            VirtualGateway info = new VirtualGateway();
            info.setId(rs.getLong("id"));
            info.setGwId(rs.getLong("gw_id"));
            info.setName(rs.getString("name"));
            info.setCode(rs.getString("code"));
            info.setAddr(rs.getString("addr"));
            info.setProjectId(rs.getString("project_id"));
            info.setDescription(rs.getString("description"));
            info.setType(rs.getString("type"));
            info.setProtocol(rs.getString("protocol"));
            info.setPort(rs.getInt("port"));
            info.setCreateTime(rs.getLong("create_time"));
            info.setModifyTime(rs.getLong("modify_time"));

            return info;
        }
    }


}
