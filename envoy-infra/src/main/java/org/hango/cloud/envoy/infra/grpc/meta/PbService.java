package org.hango.cloud.envoy.infra.grpc.meta;

/**
 * @author Xin Li
 * @date 2022/12/5 16:15
 */
public class PbService {

    public static final long NOT_PUBLISHED_PB_PROXY_ID = -1;

    public static final int PUBLISH_STATUS_NOT_PUBLISHED = 0;

    public static final int PUBLISH_STATUS_PUBLISHED = 1;


    /**
     * 主键id
     */
    private Long id;

    /**
     * grpc服务名
     */
    private String serviceName;

    /**
     * proto文件id
     */
    private Long pbId;

    /**
     * proto发布信息id，当proto已发布时非空
     */
    private Long pbProxyId;

    /**
     * 发布状态；0=未发布，1=已发布
     */
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
