package org.hango.cloud.envoy.infra.grpc.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;


import java.util.List;

/**
 * pb文件对应的dto，用于返回给前端
 *
 * @author TC_WANG
 */
public class EnvoyServiceProtobufDto {

    @JSONField(name = "PbId")
    private long id;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "ModifyDate")
    private long modifyDate;

    @JSONField(name = "ServiceId")
    private long serviceId;

    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

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
     * pb中包含的service列表
     */
    @JSONField(name = "PbServiceList")
    private List<String> pbServiceList;

    public EnvoyServiceProtobufDto() {
    }

    public EnvoyServiceProtobufDto(EnvoyServiceProtobuf serviceProtobuf) {
        this.id = serviceProtobuf.getId();
        this.createDate = serviceProtobuf.getCreateDate();
        this.modifyDate = serviceProtobuf.getModifyDate();
        this.serviceId = serviceProtobuf.getServiceId();
        this.pbFileName = serviceProtobuf.getPbFileName();
        this.pbFileContent = serviceProtobuf.getPbFileContent();
        this.pbServiceList = JSON.parseArray(serviceProtobuf.getPbServiceList(), String.class);
    }

    public EnvoyServiceProtobufDto(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        this.id = serviceProtobufProxy.getId();
        this.createDate = serviceProtobufProxy.getCreateDate();
        this.modifyDate = serviceProtobufProxy.getModifyDate();
        this.serviceId = serviceProtobufProxy.getServiceId();
        this.virtualGwId = serviceProtobufProxy.getVirtualGwId();
        this.pbFileName = serviceProtobufProxy.getPbFileName();
        this.pbFileContent = serviceProtobufProxy.getPbFileContent();
        this.pbServiceList = JSON.parseArray(serviceProtobufProxy.getPbServiceList(), String.class);
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

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
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

    public List<String> getPbServiceList() {
        return pbServiceList;
    }

    public void setPbServiceList(List<String> pbServiceList) {
        this.pbServiceList = pbServiceList;
    }

    public EnvoyServiceProtobuf toMeta() {
        EnvoyServiceProtobuf envoyServiceProtobuf = new EnvoyServiceProtobuf();
        envoyServiceProtobuf.setId(getId());
        envoyServiceProtobuf.setCreateDate(getCreateDate());
        envoyServiceProtobuf.setModifyDate(getModifyDate());
        envoyServiceProtobuf.setServiceId(getServiceId());
        envoyServiceProtobuf.setPbFileName(getPbFileName());
        envoyServiceProtobuf.setPbFileContent(getPbFileContent());
        envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(getPbServiceList()));
        return envoyServiceProtobuf;
    }
}
