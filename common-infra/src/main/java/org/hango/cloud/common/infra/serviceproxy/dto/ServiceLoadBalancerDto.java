package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 服务级别负载均衡策略
 *
 * @author TC_WANG
 * @date 2020/2/3 上午10:45.
 */
public class ServiceLoadBalancerDto implements Serializable {

    /**
     * 通过type区分类型
     */
    @JSONField(name = "Type")
    @Pattern(regexp = "|Simple|ConsistentHash")
    private String type;

    /**
     * 包含三种类型，ROUND_ROBIN、LEAST_CONN、RANDOM
     */
    @JSONField(name = "Simple")
    @Pattern(regexp = "|ROUND_ROBIN|LEAST_CONN|RANDOM")
    private String simple;

    /**
     * 一致性哈希
     */
    @Valid
    @JSONField(name = "ConsistentHash")
    private ServiceConsistentHashDto consistentHash;

    @JSONField(name = "SlowStartWindow")
    private Integer slowStartWindow;

    @JSONField(name = "LocalitySetting")
    private LocalitySettingDto localitySetting;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    public ServiceConsistentHashDto getConsistentHash() {
        return consistentHash;
    }

    public void setConsistentHash(ServiceConsistentHashDto consistentHash) {
        this.consistentHash = consistentHash;
    }

    public Integer getSlowStartWindow() {
        return slowStartWindow;
    }

    public void setSlowStartWindow(Integer slowStartWindow) {
        this.slowStartWindow = slowStartWindow;
    }

    public LocalitySettingDto getLocalitySetting() {
        return localitySetting;
    }

    public void setLocalitySetting(LocalitySettingDto localitySetting) {
        this.localitySetting = localitySetting;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
