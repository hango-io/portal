package org.hango.cloud.dashboard.envoy.meta;

import java.util.Map;

/**
 * 集成发布信息
 */
public class EnvoyIntegrationProxyInfo {

    /**
     * 表的主键
     */
    private long id;

    /**
     * 对应的集成id
     */
    private long integrationId;

    /**
     * 发布所属网关的id
     */
    private long gwId;

    /**
     * 集成发布时使用的相关数据
     */
    private Map<String, Object> metadata;
    private String metadataStr;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(long integrationId) {
        this.integrationId = integrationId;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getMetadataStr() {
        return metadataStr;
    }

    public void setMetadataStr(String metadataStr) {
        this.metadataStr = metadataStr;
    }
}
