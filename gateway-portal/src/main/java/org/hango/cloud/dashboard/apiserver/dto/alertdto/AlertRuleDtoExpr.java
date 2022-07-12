package org.hango.cloud.dashboard.apiserver.dto.alertdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2018/8/30
 */
public class AlertRuleDtoExpr {

    @JSONField(name = "Type")
    @NotEmpty
    @Pattern(regexp = "QPS|DURATION_AVG|FAILED_RATE|BAD_REQUEST|ERROR_REQUEST|HYSTRIX|TRAFFIC_CONTROL")
    private String type;

    @JSONField(name = "Level")
    @NotEmpty
    @Pattern(regexp = "Gateway|Service|Api")
    private String level;

    @JSONField(name = "Operator")
    @NotEmpty
    @Pattern(regexp = "[=<>!]+")
    private String operator;

    @JSONField(name = "Value")
    @NotNull
    private Double value;

    @JSONField(name = "Targets")
    @Size(min = 1)
    @NotNull
    private List<Map<String, String>> targets;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public List<Map<String, String>> getTargets() {
        return targets;
    }

    public void setTargets(List<Map<String, String>> targets) {
        this.targets = targets;
    }
}
