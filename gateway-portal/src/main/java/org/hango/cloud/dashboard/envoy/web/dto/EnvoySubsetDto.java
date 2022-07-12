package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 对应DR中的subset
 *
 * @author TC_WANG
 * @date 2019/12/26 下午3:23.
 */
public class EnvoySubsetDto implements Serializable {
    /**
     * 名称
     */
    @JSONField(name = "Name")
    @Pattern(regexp = "^[A-Za-z0-9]{1,32}$")
    private String name;

    /**
     * 标签集合
     */
    @JSONField(name = "Labels")
    private Map<String, String> labels;

    /**
     * 静态地址列表
     */
    @JSONField(name = "StaticAddrList")
    private List<String> staticAddrList;

    /**
     * 高级配置包含负载均衡策略和连接池
     */
    @JSONField(name = "TrafficPolicy")
    private EnvoyServiceTrafficPolicyDto trafficPolicy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public List<String> getStaticAddrList() {
        return staticAddrList;
    }

    public void setStaticAddrList(List<String> staticAddrList) {
        this.staticAddrList = staticAddrList;
    }

    public EnvoyServiceTrafficPolicyDto getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(EnvoyServiceTrafficPolicyDto trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
