package org.hango.cloud.dashboard.envoy.meta;

import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;

/**
 * 路由规则发布目的服务Info
 *
 * @author hzchenzhongyang 2019-09-19
 */
public class EnvoyDestinationInfo {

    /**
     * 路由规则目标服务serviceId
     */
    private long serviceId;

    /**
     * 路由规则目标服务权重
     */
    private long weight;

    /**
     * 路由规则目标服务端口
     */
    private int port;

    /**
     * subset名称
     */
    private String subsetName;

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSubsetName() {
        return subsetName;
    }

    public void setSubsetName(String subsetName) {
        this.subsetName = subsetName;
    }

    public EnvoyDestinationDto fromMeta() {
        EnvoyDestinationDto dto = new EnvoyDestinationDto();
        dto.setWeight(this.weight);
        dto.setServiceId(this.serviceId);
        dto.setPort(this.port);
        dto.setSubsetName(this.subsetName);
        return dto;
    }
}
