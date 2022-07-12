package org.hango.cloud.dashboard.apiserver.meta.grpc;

import java.io.Serializable;

/**
 * 服务对应的protobuf文件元信息
 *
 * @author TC_WANG
 * @date 2019/7/2
 */
public class ServiceProtobuf implements Serializable {

    private static final long serialVersionUID = -8678836644806911754L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long serviceId;
    /**
     * pb对应的名称
     */
    private String pbName;

    /**
     * 上传的pb文件名
     */
    private String pbFileName;

    /**
     * pb文件内容，直接存string
     */
    private String pbFileContent;

    /**
     * description文件内容，base64格式存储
     */
    private String descFileContent;

    /**
     * 描述
     */
    private String description;

    /**
     * 发布状态，0表示未发布，1表示已发布
     */
    private int pbStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getPbName() {
        return pbName;
    }

    public void setPbName(String pbName) {
        this.pbName = pbName;
    }

    public String getPbFileName() {
        return pbFileName;
    }

    public void setPbFileName(String pbFileName) {
        this.pbFileName = pbFileName;
    }

    public String getPbFileContent() {
        return pbFileContent;
    }

    public void setPbFileContent(String pbFileContent) {
        this.pbFileContent = pbFileContent;
    }

    public String getDescFileContent() {
        return descFileContent;
    }

    public void setDescFileContent(String descFileContent) {
        this.descFileContent = descFileContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPbStatus() {
        return pbStatus;
    }

    public void setPbStatus(int pbStatus) {
        this.pbStatus = pbStatus;
    }
}
