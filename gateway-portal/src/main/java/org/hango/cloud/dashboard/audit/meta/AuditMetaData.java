package org.hango.cloud.dashboard.audit.meta;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 配置审计元数据信息，包括
 * 操作时间、操作用户，具体详细配置信息
 */
public class AuditMetaData {
    /**
     * 操作时间
     */
    private long time;
    /**
     * 操作用户
     */
    private String account;

    /**
     * 操作事件
     */
    private String event;

    /**
     * 具体的操作数据
     */
    private JSONObject data;

    public AuditMetaData(long time, String account, String event, JSONObject data) {
        this.time = time;
        this.account = account;
        this.event = event;
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
