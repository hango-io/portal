package org.hango.cloud.envoy.infra.grpc.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 服务对应的pb文件元信息
 *
 * @author TC_WANG
 */
public class EnvoyServiceProtobuf implements Serializable {

    private static final long serialVersionUID = -8678836644806911754L;

    /**
     * 主键id
     */
    private long id;

    /**
     * 创建时间
     */
    private long createDate;

    /**
     * 修改时间
     */
    private long modifyDate;

    /**
     * 服务id
     */
    private long serviceId;

    /**
     * 上传的pb文件名
     */
    private String pbFileName;

    /**
     * pb文件内容，直接存string
     */
    private String pbFileContent;

    /**
     * pb中包含的service列表
     */
    private String pbServiceList;

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

    public String getPbServiceList() {
        return pbServiceList;
    }

    public void setPbServiceList(String pbServiceList) {
        this.pbServiceList = pbServiceList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
