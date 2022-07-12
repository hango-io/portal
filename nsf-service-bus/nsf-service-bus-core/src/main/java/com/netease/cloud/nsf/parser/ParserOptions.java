package com.netease.cloud.nsf.parser;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;

/**
 * Parser选项
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/26
 **/
public class ParserOptions {
    // openApi需要暴露的host
    private String openApiHost = "0.0.0.0";
    // openApi需要暴露的端口
    private Integer openApiPort = 80;
    // onSuccessBean
    private String onSuccessBean = "onSuccessProcessor";
    // quartz simple类型 misfire 补偿策略，默认立即触发
    private Integer quartzSimpleTriggerMisfireInstructions = SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;
    // quartz cron类型 misfire 补偿策略，默认立即触发
    private Integer quartzCronTriggerMisfireInstructions = CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;

    public String getOpenApiHost() {
        return openApiHost;
    }

    public void setOpenApiHost(String openApiHost) {
        this.openApiHost = openApiHost;
    }

    public Integer getOpenApiPort() {
        return openApiPort;
    }

    public void setOpenApiPort(Integer openApiPort) {
        this.openApiPort = openApiPort;
    }

    public String getOnSuccessBean() {
        return onSuccessBean;
    }

    public void setOnSuccessBean(String onSuccessBean) {
        this.onSuccessBean = onSuccessBean;
    }

    public Integer getQuartzSimpleTriggerMisfireInstructions() {
        return quartzSimpleTriggerMisfireInstructions;
    }

    public void setQuartzSimpleTriggerMisfireInstructions(Integer quartzSimpleTriggerMisfireInstructions) {
        this.quartzSimpleTriggerMisfireInstructions = quartzSimpleTriggerMisfireInstructions;
    }

    public Integer getQuartzCronTriggerMisfireInstructions() {
        return quartzCronTriggerMisfireInstructions;
    }

    public void setQuartzCronTriggerMisfireInstructions(Integer quartzCronTriggerMisfireInstructions) {
        this.quartzCronTriggerMisfireInstructions = quartzCronTriggerMisfireInstructions;
    }
}
