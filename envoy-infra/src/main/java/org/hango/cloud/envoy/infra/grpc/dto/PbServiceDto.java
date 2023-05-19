package org.hango.cloud.envoy.infra.grpc.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author Xin Li
 * @date 2022/12/5 17:17
 */
public class PbServiceDto {
    /**
     * 主键id
     */
    @JSONField(name = "Id")
    private Long id;

    /**
     * grpc服务名
     */
    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * proto文件id
     */
    @JSONField(name = "PbId")
    private Long pbId;

    /**
     * proto发布信息id，当proto已发布时非空
     */
    @JSONField(name = "PbProxyId")
    private Long pbProxyId;

    /**
     * 发布状态；0=未发布，1=已发布
     */
    @JSONField(name = "PublishStatus")
    private int publishStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getPbId() {
        return pbId;
    }

    public void setPbId(Long pbId) {
        this.pbId = pbId;
    }

    public Long getPbProxyId() {
        return pbProxyId;
    }

    public void setPbProxyId(Long pbProxyId) {
        this.pbProxyId = pbProxyId;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
    }
}
