package org.hango.cloud.dashboard.apiserver.meta.audit;

import org.hango.cloud.dashboard.apiserver.meta.enums.SqlComparison;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/6/12
 */
public class QueryRange {

    /**
     * 属性
     */
    private String property;

    /**
     * 区间条件
     */
    private SqlComparison sqlComparison;

    /**
     * 查询值
     */
    private String value;

    public QueryRange() {
    }

    public QueryRange(String property, SqlComparison sqlComparison, String value) {
        this.property = property;
        this.sqlComparison = sqlComparison;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public SqlComparison getSqlComparison() {
        return sqlComparison;
    }

    public void setSqlComparison(SqlComparison sqlComparison) {
        this.sqlComparison = sqlComparison;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
