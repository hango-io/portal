package org.hango.cloud.envoy.infra.healthcheck.dto;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2023/2/9
 */
public enum HealthStatusEnum {
    HEALTHY(1),
    UNHEALTHY(0),
    UNNKOW(-1);

    private Integer value;
    HealthStatusEnum(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public static Integer getValueByName(String name){
        return Stream.of(values()).filter(o -> o.name().equalsIgnoreCase(name)).map(HealthStatusEnum::getValue).findFirst().orElse(null);
    }
}