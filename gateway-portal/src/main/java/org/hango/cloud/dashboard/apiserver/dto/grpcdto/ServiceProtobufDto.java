package org.hango.cloud.dashboard.apiserver.dto.grpcdto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.PublishedDetailDto;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ServiceProtobuf;

import java.util.List;

/**
 * pb文件对应的dto，用于返回给前端
 *
 * @author TC_WANG
 * @date 2019/7/8
 */
public class ServiceProtobufDto {

    @JSONField(name = "PbId")
    private long id;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "ModifyDate")
    private long modifyDate;

    @JSONField(name = "ServiceId")
    private long serviceId;

    /**
     * pb对应的名称
     */
    @JSONField(name = "PbName")
    private String pbName;

    /**
     * 上传的pb文件名
     */
    @JSONField(name = "PbFileName")
    private String pbFileName;

    /**
     * pb文件内容，直接存string
     */
    @JSONField(name = "PbFileContent")
    private String pbFileContent;

    /**
     * 描述
     */
    @JSONField(name = "PbDesc")
    private String description;

    /**
     * 描述
     */
    @JSONField(name = "PbStatus")
    private int pbStatus;

    /**
     * 发布的网关个数
     */
    @JSONField(name = "PublishedCount")
    private int publishedCount;

    /**
     * 发布的详情
     */
    @JSONField(name = "PublishedDetails")
    private List<PublishedDetailDto> publishedDetailDtos;

    public ServiceProtobufDto(ServiceProtobuf serviceProtobuf, int publishedCount, List<PublishedDetailDto> publishedDetailDtoList) {
        this.id = serviceProtobuf.getId();
        this.createDate = serviceProtobuf.getCreateDate();
        this.modifyDate = serviceProtobuf.getModifyDate();
        this.serviceId = serviceProtobuf.getServiceId();
        this.pbName = serviceProtobuf.getPbName();
        this.pbFileName = serviceProtobuf.getPbFileName();
        this.pbFileContent = serviceProtobuf.getPbFileContent();
        this.description = serviceProtobuf.getDescription();
        this.pbStatus = serviceProtobuf.getPbStatus();
        this.publishedCount = publishedCount;
        this.publishedDetailDtos = publishedDetailDtoList;
    }

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

    public int getPublishedCount() {
        return publishedCount;
    }

    public void setPublishedCount(int publishedCount) {
        this.publishedCount = publishedCount;
    }

    public List<PublishedDetailDto> getPublishedDetailDtos() {
        return publishedDetailDtos;
    }

    public void setPublishedDetailDtos(List<PublishedDetailDto> publishedDetailDtos) {
        this.publishedDetailDtos = publishedDetailDtos;
    }
}
