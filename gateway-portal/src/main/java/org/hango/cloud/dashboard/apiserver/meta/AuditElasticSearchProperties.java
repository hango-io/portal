package org.hango.cloud.dashboard.apiserver.meta;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/27
 */
@JSONType(ignores = "proxy")
public class AuditElasticSearchProperties {

    /**
     * Comma-separated list of the Elasticsearch instances to use.
     */
    @JSONField(name = "spring.elasticsearch.jest.uris")
    private List<String> uris = new ArrayList<String>(
            Collections.singletonList("http://localhost:9200"));

    /**
     * Login user.
     */
    @JSONField(name = "spring.elasticsearch.jest.username")
    private String username;

    /**
     * Login password.
     */
    @JSONField(name = "spring.elasticsearch.jest.password")
    private String password;

    /**
     * Enable connection requests from multiple execution threads.
     */
    @JSONField(name = "spring.elasticsearch.jest.multi-threaded")
    private boolean multiThreaded = true;

    /**
     * Connection timeout in milliseconds.
     */
    @JSONField(name = "spring.elasticsearch.jest.connection-timeout")
    private int connectionTimeout = 30000;

    /**
     * Read timeout in milliseconds.
     */
    @JSONField(name = "spring.elasticsearch.jest.read-timeout")
    private int readTimeout = 30000;


    private Proxy proxy;

    /**
     * Proxy host the HTTP client should use.
     */
    @JSONField(name = "spring.elasticsearch.jest.proxy.host")
    private String host;

    /**
     * Proxy port the HTTP client should use.
     */
    @JSONField(name = "spring.elasticsearch.jest.proxy.port")
    private Integer port;

    @JSONField(name = "spring.elasticsearch.jest.discovery.enable")
    private boolean discoveryEnable = false;

    @JSONField(name = "spring.elasticsearch.jest.discovery.frequency")
    private long discoveryFrequency = 30000;

    public Proxy getProxy() {
        return new Proxy(host, port);
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMultiThreaded() {
        return multiThreaded;
    }

    public void setMultiThreaded(boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean getDiscoveryEnable() {
        return discoveryEnable;
    }

    public void setDiscoveryEnable(boolean discoveryEnable) {
        this.discoveryEnable = discoveryEnable;
    }

    public long getDiscoveryFrequency() {
        return discoveryFrequency;
    }

    public void setDiscoveryFrequency(long discoveryFrequency) {
        this.discoveryFrequency = discoveryFrequency;
    }

    public static class Proxy {

        private String host;

        private Integer port;

        public Proxy(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return this.host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return this.port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

    }
}
