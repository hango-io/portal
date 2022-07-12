package org.hango.cloud.dashboard.apiserver.dto.grpcdto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * 发布protobuf dto
 *
 * @author TC_WANG
 * @date 2019/7/2
 */
public class PublishedServiceProtobufDto implements Serializable {

    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * pb对应的名称
     */
    @JSONField(name = "PbName")
    private String pbName;

    /**
     * description文件内容，base64格式存储
     */
    @JSONField(name = "DescFileContent")
    private String descFileContent;

    /**
     * 要发布的API列表，包含对应的grpc参数
     */
    @JSONField(name = "PublishedApiList")
    private List<PublishedRestfulApiDto> publishedApiList;

    /**
     * 需要下线的API列表
     */
    @JSONField(name = "OfflineApiIdList")
    private List<Long> offlineApiIdList;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPbName() {
        return pbName;
    }

    public void setPbName(String pbName) {
        this.pbName = pbName;
    }

    public String getDescFileContent() {
        return descFileContent;
    }

    public void setDescFileContent(String descFileContent) {
        this.descFileContent = descFileContent;
    }

    public List<Long> getOfflineApiIdList() {
        return offlineApiIdList;
    }

    public void setOfflineApiIdList(List<Long> offlineApiIdList) {
        this.offlineApiIdList = offlineApiIdList;
    }

    public List<PublishedRestfulApiDto> getPublishedApiList() {
        return publishedApiList;
    }

    public void setPublishedApiList(List<PublishedRestfulApiDto> publishedApiList) {
        this.publishedApiList = publishedApiList;
    }
}

