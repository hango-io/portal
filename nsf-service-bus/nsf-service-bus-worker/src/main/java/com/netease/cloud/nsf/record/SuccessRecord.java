package com.netease.cloud.nsf.record;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/26
 **/
public class SuccessRecord {
    private String recordId;

    private String integrationId;

    private String timestamp;

    private String tag;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
