package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * 获取带有port的服务信息
 */
public class BackendServiceWithPortDto {

    @JSONField(name = "Name")
    private String name;
    @JSONField(name = "Port")
    private List<Integer> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

}
