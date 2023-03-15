package org.hango.cloud.envoy.infra.healthcheck.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @author TC_WANG
 * @date 2019/12/2 下午8:34.
 */
public class EnvoyServiceInstanceDto implements Serializable {
    /**
     * 实例地址: ip + port
     */
    @JSONField(name = "InstanceAddr")
    private String instanceAddr;

    /**
     * 实例状态：1表示健康，0表示不健康
     */
    @JSONField(name = "HealthyStatus")
    private Integer status;

    public String getInstanceAddr() {
        return instanceAddr;
    }

    public void setInstanceAddr(String instanceAddr) {
        this.instanceAddr = instanceAddr;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
