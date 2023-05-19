package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 连接池配置
 *
 * @author TC_WANG
 * @date 2020/1/20 下午2:51.
 */
public class ServiceConnectionPoolDto implements Serializable {
    /**
     * TCP连接池
     */
    @JSONField(name = "TCP")
    @Valid
    private ServiceTcpConnectionPoolDto serviceTcpConnectionPoolDto;

    /**
     * HTTP连接池
     */
    @JSONField(name = "HTTP")
    @Valid
    private ServiceHttpConnectionPoolDto serviceHttpConnectionPoolDto;

    public ServiceTcpConnectionPoolDto getServiceTcpConnectionPoolDto() {
        return serviceTcpConnectionPoolDto;
    }

    public void setServiceTcpConnectionPoolDto(ServiceTcpConnectionPoolDto serviceTcpConnectionPoolDto) {
        this.serviceTcpConnectionPoolDto = serviceTcpConnectionPoolDto;
    }

    public ServiceHttpConnectionPoolDto getServiceHttpConnectionPoolDto() {
        return serviceHttpConnectionPoolDto;
    }

    public void setServiceHttpConnectionPoolDto(ServiceHttpConnectionPoolDto serviceHttpConnectionPoolDto) {
        this.serviceHttpConnectionPoolDto = serviceHttpConnectionPoolDto;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class ServiceHttpConnectionPoolDto implements Serializable {

        /**
         * 最大等待HTTP请求数。默认值是1024，仅适用于HTTP/1.1的服务，因为HTTP/2协议的请求在到来时
         * 会立即复用连接，不会在连接池等待
         */
        @JSONField(name = "Http1MaxPendingRequests")
        @Min(1)
        @Max(100000)
        private Integer http1MaxPendingRequests;

        /**
         * 最大请求数。默认值是1024，仅使用于HTTP/2的服务。HTTP/1.1的服务使用maxConnections即可
         */
        @JSONField(name = "Http2MaxRequests")
        @Min(1)
        @Max(100000)
        private Integer http2MaxRequests;

        /**
         * 每个连接的最大请求数。HTTP/1.1和HTTP/2连接池都遵循此参数，如果没有设置则没有限制，如果设置
         * 为1则表示禁用了keep-alive，0表示不限制最多处理的请求数为2^29
         */
        @JSONField(name = "MaxRequestsPerConnection")
        @Min(0)
        @Max(100000)
        private Integer maxRequestsPerConnection;

        /**
         * 空闲超时，定义在多长时间内没有活动请求则关闭连接
         */
        @JSONField(name = "IdleTimeout")
        @Min(1)
        @Max(1024000)
        private Integer idleTimeout;

        public Integer getHttp1MaxPendingRequests() {
            return http1MaxPendingRequests;
        }

        public void setHttp1MaxPendingRequests(Integer http1MaxPendingRequests) {
            this.http1MaxPendingRequests = http1MaxPendingRequests;
        }

        public Integer getHttp2MaxRequests() {
            return http2MaxRequests;
        }

        public void setHttp2MaxRequests(Integer http2MaxRequests) {
            this.http2MaxRequests = http2MaxRequests;
        }

        public Integer getMaxRequestsPerConnection() {
            return maxRequestsPerConnection;
        }

        public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
            this.maxRequestsPerConnection = maxRequestsPerConnection;
        }

        public Integer getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Integer idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class ServiceTcpConnectionPoolDto implements Serializable {
        /**
         * 最大连接数
         */
        @JSONField(name = "MaxConnections")
        @Min(1)
        @Max(100000)
        private Integer maxConnections;

        /**
         * tcp连接超时时间
         */
        @JSONField(name = "ConnectTimeout")
        @Min(1)
        @Max(1024000)
        private Integer connectTimeout;

        public Integer getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
        }

        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
