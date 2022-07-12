package com.netease.cloud.nsf.dao.impl;

import com.netease.cloud.nsf.dao.ResourceDao;
import com.netease.cloud.nsf.dao.meta.ResourceInfo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class ResourceDaoImpl implements ResourceDao {

    private String resourceTableName;
    private NamedParameterJdbcTemplate namedTemplate;

    public ResourceDaoImpl(String resourceTableName, NamedParameterJdbcTemplate namedTemplate) {
        this.resourceTableName = resourceTableName;
        this.namedTemplate = namedTemplate;
    }

    @Override
    public boolean contains(String name) {
        String sql = String.format("select count(*) from %s where name=:name", resourceTableName);
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("name", name);
        return namedTemplate.queryForObject(sql, ps, (resultSet, i) -> resultSet.getInt(1) > 0);
    }

    @Override
    public boolean contains(String name, String version) {
        String sql = String.format("select count(*) from %s where name=:name and version=:version", resourceTableName);
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("version", version);
        return namedTemplate.queryForObject(sql, ps, (resultSet, i) -> resultSet.getInt(1) > 0);
    }

    @Override
    public void add(ResourceInfo resourceInfo) {
        String sql = String.format("insert into %s(name, version, labels, create_time, body) values(:name, :version, :labels, :createTime, :body)", resourceTableName);
        SqlParameterSource ps = new BeanPropertySqlParameterSource(resourceInfo);
        namedTemplate.update(sql, ps);
    }

    @Override
    public void delete(String name) {
        String sql = String.format("delete from %s where name=:name", resourceTableName);
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("name", name);
        namedTemplate.update(sql, ps);
    }

    @Override
    public void update(ResourceInfo resourceInfo) {
        String sql = String.format("update %s set name=:name, version=:version, labels=:labels, create_time=:createTime, body=:body where name=:name", resourceTableName);
        SqlParameterSource ps = new BeanPropertySqlParameterSource(resourceInfo);
        namedTemplate.update(sql, ps);
    }

    @Override
    public ResourceInfo get(String name) {
        try {
            String sql = String.format("select * from %s where name=:name", resourceTableName);
            SqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("name", name);
            return namedTemplate.queryForObject(sql, ps, new ResourceRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<ResourceInfo> list() {
        String sql = String.format("select * from %s", resourceTableName);
        return namedTemplate.query(sql, new ResourceRowMapper());
    }

    @Override
    public List<ResourceInfo> list(String labelMatch) {
        String sql = String.format("select * from %s where labels like :labelMatch", resourceTableName);
        SqlParameterSource ps = new MapSqlParameterSource()
                .addValue("labelMatch", labelMatch);
        return namedTemplate.query(sql, ps, new ResourceRowMapper());
    }

    static final class ResourceRowMapper implements RowMapper<ResourceInfo> {
        @Override
        public ResourceInfo mapRow(ResultSet resultSet, int i) throws SQLException {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(resultSet.getString("name"));
            resourceInfo.setVersion(resultSet.getString("version"));
            resourceInfo.setLabels(resultSet.getString("labels"));
            resourceInfo.setCreateTime(resultSet.getString("create_time"));
            resourceInfo.setBody(resultSet.getString("body"));
            return resourceInfo;
        }
    }
}
