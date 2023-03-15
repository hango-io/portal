package org.hango.cloud.common.infra.route.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.common.infra.route.common.DestinationInfo;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 发布路由规则时指定的Destination Dto
 * <p>
 * 不使用ServiceProxyDto的原因在于：（1）前端与openapi客户端可以少传入参数；（2）需要增加权重weight属性；（3）服务发布时间等属性不使用
 *
 * @author hzchenzhongyang 2019-09-19
 */
public class DestinationDto {

    /**
     * 路由规则目标服务的权重
     */
    @JSONField(name = "Weight")
    @Min(0)
    @Max(100)
    private long weight;

    /**
     * 路由规则目标服务的serviceId
     */
    @JSONField(name = "ServiceId")
    @Min(1)
    private long serviceId;

    /**
     * 目标服务端口
     */
    @JSONField(name = "Port")
    @Max(65535)
    @Min(0)
    private int port;

    /**
     * 目标服务应用名称，用于前端展示，不进行存储
     */
    @JSONField(name = "ApplicationName")
    private String applicationName;

    /**
     * subset名称
     */
    @JSONField(name = "SubsetName")
    private String subsetName;

    /**
     * 流量镜像配置服务的类型
     * | 服务: application
     * | 版本: subset
     */
    @JSONField(name = "MirrorType")
    private String mirrorType;

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public String getMirrorType(){
        return mirrorType;
    }

    public void setMirrorType(String mirrorType){
        this.mirrorType = mirrorType;
    }

    public DestinationInfo toMeta() {
        DestinationInfo info = new DestinationInfo();
        info.setWeight(this.weight);
        info.setServiceId(this.serviceId);
        info.setPort(this.port);
        info.setSubsetName(this.subsetName);
        info.setMirrorType(this.mirrorType);
        return info;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
