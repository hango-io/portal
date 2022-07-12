package com.netease.cloud.nsf.dao.impl;

import com.netease.cloud.nsf.dao.StatusDao;
import com.netease.cloud.nsf.dao.meta.StatusInfo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class StatusDaoImpl implements StatusDao {
    private String statusTableName;
    private NamedParameterJdbcTemplate namedTemplate;

    public StatusDaoImpl(String statusTableName, NamedParameterJdbcTemplate namedTemplate) {
        this.statusTableName = statusTableName;
        this.namedTemplate = namedTemplate;
    }

    @Override
    public String get(String name) {
        try {
            String sql = String.format("select * from %s where name=:name", statusTableName);
            SqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("name", name);
            return namedTemplate.queryForObject(sql, ps, (resultSet, i) -> resultSet.getString("value"));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void update(StatusInfo statusInfo) {
        String sql = String.format("update %s set value=:value where name=:name", statusTableName);
        SqlParameterSource ps = new BeanPropertySqlParameterSource(statusInfo);
        namedTemplate.update(sql, ps);
    }

    @Override
    public List<StatusInfo> list() {
        String sql = String.format("select * from %s", statusTableName);
        return namedTemplate.query(sql, (resultSet, i) -> new StatusInfo(resultSet.getString("name"), resultSet.getString("value")));
    }
}
