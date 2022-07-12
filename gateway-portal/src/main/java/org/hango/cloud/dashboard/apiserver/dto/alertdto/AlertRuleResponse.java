package org.hango.cloud.dashboard.apiserver.dto.alertdto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2018/9/1
 */
public class AlertRuleResponse {

    @JsonProperty("Result")
    private AlertRuleDto result;

    public AlertRuleDto getResult() {
        return result;
    }

    public void setResult(AlertRuleDto result) {
        this.result = result;
    }
}
