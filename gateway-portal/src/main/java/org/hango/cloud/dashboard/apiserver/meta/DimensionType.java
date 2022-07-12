package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 监控维度
 * @date 2019/7/8
 */
public enum DimensionType {

    /**
     * 网关维度
     */
    GATEWAY("gw", "网关", "gw,", "Gateway", "/#/redirect/alarm/notice/gateway"),

    /**
     * 服务维度
     */
    SERVICE("service", "服务", "gw,service,", "Service", "/#/redirect/alarm/notice/service"),

    /**
     * API维度
     */
    API("api", "API", "gw,service,api,", "Api", "/#/redirect/alarm/notice/interface");
    /**
     * 维度类型
     */
    private String dimensionType;

    /**
     * 描述
     */
    private String description;

    /**
     * 用于拼接prom表达式
     */
    private String levelToby;

    /**
     * 对应目标，用于前后端传递维度对应元信息
     */
    private String target;

    /**
     * 告警通知跳转地址后缀
     */
    private String suffix;


    DimensionType(String dimensionType, String description, String levelToby, String target, String suffix) {
        this.dimensionType = dimensionType;
        this.description = description;
        this.levelToby = levelToby;
        this.target = target;
        this.suffix = suffix;
    }

    public static String getTarget(String dimensionType) {
        for (DimensionType value : DimensionType.values()) {
            if (value.dimensionType.equals(dimensionType)) {
                return value.target;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getLevelToby(String dimensionType) {
        for (DimensionType value : DimensionType.values()) {
            if (value.dimensionType.equals(dimensionType)) {
                return value.levelToby;
            }
        }
        return StringUtils.EMPTY;
    }

    public static String getDimensionType(String target) {
        for (DimensionType value : DimensionType.values()) {
            if (value.target.equals(target)) {
                return value.dimensionType;
            }
        }
        return StringUtils.EMPTY;
    }

    public static Set<String> getDimensionTypeSet() {
        Set<String> dimensionTypeSet = new HashSet<>();
        for (DimensionType value : DimensionType.values()) {
            dimensionTypeSet.add(value.dimensionType);
        }
        return dimensionTypeSet;
    }

    public static String getDimensionTypePattern() {
        StringBuilder builder = new StringBuilder();
        for (DimensionType value : DimensionType.values()) {
            builder.append(value.dimensionType).append("|");
        }
        return builder.substring(0, builder.length() - 1);
    }

    public static DimensionType getByDimensionType(String dimensionType) {
        for (DimensionType value : DimensionType.values()) {
            if (value.dimensionType.equals(dimensionType)) {
                return value;
            }
        }
        return null;
    }

    public static DimensionType getByTarget(String target) {
        for (DimensionType value : DimensionType.values()) {
            if (value.target.equals(target)) {
                return value;
            }
        }
        return null;
    }

    public String getTarget() {
        return target;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public String getDescription() {
        return description;
    }

    public String getSuffix() {
        return suffix;
    }
}
