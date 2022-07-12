package org.hango.cloud.dashboard.apiserver.meta.enums;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/2
 */
public enum SqlComparison {
    EQ("="), GTE(">="), GT(">"), LT("<"), LTE("<=");

    private String value;

    private SqlComparison(String text) {
        this.value = text;
    }

    public String getValue() {
        return this.value;
    }
}
