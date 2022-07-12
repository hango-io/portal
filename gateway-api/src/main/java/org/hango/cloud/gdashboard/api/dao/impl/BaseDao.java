package org.hango.cloud.gdashboard.api.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

@Component
public abstract class BaseDao {

    private static final Logger log = LoggerFactory.getLogger(BaseDao.class);
    /**
     * 容器注入JdbcTemplate实例，Spirng Jdbc SQL操作
     */
    @Resource
    protected JdbcTemplate jdbcTemplate;

    /**
     * 容器注入NamedParameterJdbcTemplate实例，Spirng Jdbc SQL操作，功能比JdbcTemplate强大
     */
    @Resource
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 插入记录
     *
     * @param sql  sql语句
     * @param args 参数
     * @return 插入记录数
     */
    public int add(String sql, Object[] args) {
        return jdbcTemplate.update(sql, args);
    }

    /**
     * 更新记录
     *
     * @param sql  sql语句
     * @param objs 参数
     * @return 更新记录数
     */
    public int update(String sql, Object[] objs) {
        return jdbcTemplate.update(sql, objs);
    }

    /**
     * 删除记录
     *
     * @param sql  sql语句
     * @param args 参数
     * @return 删除记录数
     */
    public int delete(String sql, Object[] args) {
        return jdbcTemplate.update(sql, args);
    }

    /**
     * 查找单个对象
     *
     * @param sql
     * @param args
     * @param rowMapper
     * @return
     */
    protected <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        try {
            return jdbcTemplate.queryForObject(sql, args, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    protected <T> T queryForObject(String sql, SqlParameterSource ps, RowMapper<T> rowMapper) {
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, ps, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    protected <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, paramMap, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 大小驼峰方式转换成下划线命名
     *
     * @param name
     * @return
     */
    public String camel2UnderScode(String name) {
        StringBuilder result = new StringBuilder();
        if (name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());

            for (int i = 1; i < name.length(); i++) {
                String str = name.substring(i, i + 1);
                if (str.equals(str.toUpperCase()) && !Character.isDigit(str.charAt(0))) {
                    result.append("_");
                }
                result.append(str.toLowerCase());
            }
        }
        return result.toString();
    }

    public String camel2UnderScodeWithSymbol(String name) {
        StringBuilder result = new StringBuilder();
        if (name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());

            for (int i = 1; i < name.length(); i++) {
                String str = name.substring(i, i + 1);
                if (Character.isUpperCase(str.charAt(0))) {
                    result.append("_");
                }
                result.append(str.toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 获取查询条件
     *
     * @param head   查询表头如： select * from CronTask where
     * @param params 查询条件
     * @return
     */
    public String getQueryCondition(String head, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(head).append(" 1 = 1");

        for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
            if (!sb.toString().equals(head)) {
                sb.append(" and ");
            }
            Entry<String, Object> entry = stringObjectEntry;
            String newKey = camel2UnderScode(entry.getKey());
            // String newValue = warpValue(entry.getValue());
            sb.append(newKey).append(" = ").append(":").append(entry.getKey());
        }
        String sql = sb.toString();
        log.debug(sql);
        return sql;
    }


    /**
     * 获取模糊查询条件
     *
     * @param head   查询表头如： select * from CronTask where
     * @param params 查询条件
     * @return
     */
    public String getFuzzyQueryCondition(String head, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(head).append(" 1 = 1");
        if (params.size() == 0) {
            return sb.toString();
        } else if (params.size() == 1) {
            sb.append("and");
            for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
                Entry<String, Object> entry = stringObjectEntry;
                String newKey = camel2UnderScode(entry.getKey());
                sb.append(newKey).append(" like ").append("'%").append(entry.getValue()).append("%'");
            }
        } else {
            sb.append(" and ").append("(");
            int i = 0;
            for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
                Entry<String, Object> entry = stringObjectEntry;
                String newKey = camel2UnderScode(entry.getKey());
                sb.append(newKey).append(" like ").append("'%").append(entry.getValue()).append("%'");
                if (i < params.size() - 1) {
                    sb.append(" or ");
                }
                i++;
            }
            sb.append(") ");
        }
        String sql = sb.toString();
        log.debug(sql);
        return sql;
    }

    public String getFuzzyQueryConditionWithSymbol(String head, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(head).append(" 1 = 1");
        if (params.size() == 0) {
            return sb.toString();
        } else if (params.size() == 1) {
            sb.append("and");
            for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
                Entry<String, Object> entry = stringObjectEntry;
                String newKey = camel2UnderScodeWithSymbol(entry.getKey());
                sb.append(newKey).append(" like ").append("'%").append(entry.getValue()).append("%'");
            }
        } else {
            sb.append(" and ").append("(");
            int i = 0;
            for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
                Entry<String, Object> entry = stringObjectEntry;
                String newKey = camel2UnderScodeWithSymbol(entry.getKey());
                sb.append(newKey).append(" like ").append("'%").append(entry.getValue()).append("%'");
                if (i < params.size() - 1) {
                    sb.append(" or ");
                }
                i++;
            }
            sb.append(") ");
        }
        String sql = sb.toString();
        log.debug(sql);
        return sql;
    }


    public StringBuilder getSbQueryCondition(String head, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(head).append(" 1 = 1");

        for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
            if (!sb.toString().equals(head)) {
                sb.append(" and ");
            }
            Entry<String, Object> entry = stringObjectEntry;
            String newKey = camel2UnderScode(entry.getKey());
            // String newValue = warpValue(entry.getValue());
            sb.append(newKey).append(" = ").append(":").append(entry.getKey());
        }
        String sql = sb.toString();
        log.debug(sql);
        return sb;
    }

    protected void appendOrderBy(StringBuilder sb, String order, boolean asc) {
        sb.append(" order by ").append(order);
        if (asc) {
            sb.append(" asc");
        } else {
            sb.append(" desc ");
        }
    }

    protected String appendOrderBy(String sb, String order, boolean asc) {
        StringBuilder stringBuilder = new StringBuilder(sb);
        stringBuilder.append(" order by ").append(order);
        if (asc) {
            stringBuilder.append(" asc");
        } else {
            stringBuilder.append(" desc ");
        }
        return stringBuilder.toString();
    }

    protected String appendLimitOffset(String sb, long offset, long limit) {
        StringBuilder stringBuilder = new StringBuilder(sb);
        stringBuilder.append(" limit ").append(offset).append(",").append(limit);
        return stringBuilder.toString();
    }


    protected void appendNot(StringBuilder sb, Map<String, Object> notParams) {
        if (notParams.isEmpty())
            return;
        sb.append(" and ( ");
        for (String key : notParams.keySet()) {
            // Object value = notParams.get(key);
            String newKey = camel2UnderScode(key);
            sb.append(newKey).append(" != ").append(":").append(key);
        }
        sb.append(" ) ");
    }


    protected void appendOrderBy(StringBuilder sb, String order) {
        appendOrderBy(sb, order, true);
    }

    public String getQueryCondition(String head, Map<String, Object> params, String order) {
        return getQueryCondition(head, params, order, true);
    }

    public String getQueryCondition(String head, Map<String, Object> params, String order, boolean asc) {
        StringBuilder sb = new StringBuilder(head);

        for (Entry<String, Object> stringObjectEntry : params.entrySet()) {
            if (!sb.toString().equals(head)) {
                sb.append(" and ");
            }
            Entry<String, Object> entry = stringObjectEntry;
            String newKey = camel2UnderScode(entry.getKey());
            // String newValue = warpValue(entry.getValue());
            sb.append(newKey).append(" = ").append(":").append(entry.getKey());
        }
        sb.append(" order by ").append(order);
        if (asc) {
            sb.append(" asc");
        } else {
            sb.append(" desc ");
        }
        String sql = sb.toString();
        log.debug(sql);
        return sql;
    }


    /**
     * 将value改成sql支持的格式
     *
     * @param value
     * @return
     */
    private String warpValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof Boolean) {
            boolean boolValue = (Boolean) value;
            if (boolValue) {
                return "1";
            } else {
                return "0";
            }
        }
        return value.toString();
    }

    /**
     * 返回int指的SQL通用方法
     *
     * @param headSql
     * @param params
     * @return
     */
    public Integer getInt(String headSql, Map<String, Object> params) {
        String sql = getQueryCondition(headSql, params);
        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }

    /**
     * 添加返回统一的JDBC模板
     *
     * @return
     */
    public JdbcTemplate getTemplate() {
        return jdbcTemplate;
    }

    public static class IdsRowMapper implements RowMapper<Long> {

        private String id;

        public IdsRowMapper(String id) {
            this.id = id;
        }

        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong(id);
        }
    }

}
