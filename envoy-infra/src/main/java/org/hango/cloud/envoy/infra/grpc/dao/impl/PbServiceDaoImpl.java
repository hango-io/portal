package org.hango.cloud.envoy.infra.grpc.dao.impl;

import org.hango.cloud.common.infra.base.dao.impl.BaseDao;
import org.hango.cloud.envoy.infra.grpc.dao.PbServiceDao;
import org.hango.cloud.envoy.infra.grpc.meta.PbService;
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
 * @author Xin Li
 * @date 2022/12/5 16:24
 */
@Component
public class PbServiceDaoImpl extends BaseDao implements PbServiceDao {
    @Override
    public long add(PbService pbService) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "insert into hango_pb_service_info (service_name, pb_id, pb_proxy_id, publish_status) "
                + " values (:serviceName, :pbId, :pbProxyId, :publishStatus)";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pbService);
        namedParameterJdbcTemplate.update(sql, ps, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(PbService pbService) {
        String sql = "update hango_pb_service_info set service_name = :serviceName, pb_id=:pbId, " +
                "pb_proxy_id=:pbProxyId, publish_status=:publishStatus where id=:id";
        SqlParameterSource ps = new BeanPropertySqlParameterSource(pbService);
        return namedParameterJdbcTemplate.update(sql, ps);
    }

    @Override
    public int delete(PbService pbService) {
        String sql = "DELETE from hango_pb_service_info where id = :id";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource("id", pbService.getId()));
    }

    @Override
    public PbService get(long id) {
        String sql = "select * from hango_pb_service_info where id = :id";
        return queryForObject(sql, new MapSqlParameterSource("id", id), new PbServiceRowMapper());
    }

    @Override
    public List<PbService> findAll() {
        String sql = "select * from hango_pb_service_info order by id desc";
        return namedParameterJdbcTemplate.query(sql, new PbServiceRowMapper());
    }

    @Override
    public List<PbService> getRecordsByField(Map<String, Object> params) {
        String head = "select * from hango_pb_service_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.query(sql, params, new PbServiceRowMapper());
    }

    @Override
    public int getCountByFields(Map<String, Object> params) {
        String head = "select count(*) from hango_pb_service_info where ";
        String sql = getQueryCondition(head, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    class PbServiceRowMapper implements RowMapper<PbService> {
        @Override
        public PbService mapRow(ResultSet rs, int rowNum) throws SQLException {
            PbService pbService = new PbService();
            pbService.setId(rs.getLong("id"));
            pbService.setServiceName(rs.getString("service_name"));
            pbService.setPbId(rs.getLong("pb_id"));
            pbService.setPbProxyId(rs.getLong("pb_proxy_id"));
            pbService.setPublishStatus(rs.getInt("publish_status"));
            return pbService;
        }
    }
}
