package org.hango.cloud.envoy.infra.grpc.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;

import java.util.List;

/**
 * pb文件对应的dto，用于返回给前端
 *
 * @author TC_WANG
 */
public class EnvoyPublishedServiceProtobufDto {

    @JSONField(name = "PbId")
    private long id;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "ModifyDate")
    private long modifyDate;

    @JSONField(name = "ServiceId")
    private long serviceId;

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

    /**
     * 网关名称
     */
    @JSONField(name = "VirtualGwId")
    private long virtualGwId;

    /**
     * 网关id
     */
    @JSONField(name = "GwName")
    private String gwName;

    /**
     * 服务地址，如果是注册中心，则为实例地址
     */
    @JSONField(name = "ServiceAddr")
    private String[] serviceAddr;

    public EnvoyPublishedServiceProtobufDto() {
    }

    public EnvoyPublishedServiceProtobufDto(EnvoyServiceProtobufProxy serviceProtobufProxy, long virtualGwId, String gwName, String[] serviceAddr) {
        this.id = serviceProtobufProxy.getId();
        this.createDate = serviceProtobufProxy.getCreateDate();
        this.modifyDate = serviceProtobufProxy.getModifyDate();
        this.serviceId = serviceProtobufProxy.getServiceId();
        this.pbFileName = serviceProtobufProxy.getPbFileName();
        this.pbFileContent = serviceProtobufProxy.getPbFileContent();
        this.pbServiceList = JSON.parseArray(serviceProtobufProxy.getPbServiceList(), String.class);
        this.virtualGwId = virtualGwId;
        this.gwName = gwName;
        this.serviceAddr = serviceAddr;
    }

    public static EnvoyPublishedServiceProtobufDto getFromProtoMeta(EnvoyServiceProtobufDto envoyServiceProtobufDto) {
        if (envoyServiceProtobufDto == null) {
            return null;
        }
        EnvoyPublishedServiceProtobufDto envoyPublishedServiceProtobufDto = new EnvoyPublishedServiceProtobufDto();
        envoyPublishedServiceProtobufDto.setId(envoyServiceProtobufDto.getId());
        envoyPublishedServiceProtobufDto.setCreateDate(envoyServiceProtobufDto.getCreateDate());
        envoyPublishedServiceProtobufDto.setModifyDate(envoyServiceProtobufDto.getModifyDate());
        envoyPublishedServiceProtobufDto.setServiceId(envoyServiceProtobufDto.getServiceId());
        envoyPublishedServiceProtobufDto.setVirtualGwId(envoyServiceProtobufDto.getVirtualGwId());
        envoyPublishedServiceProtobufDto.setPbFileName(envoyServiceProtobufDto.getPbFileName());
        envoyPublishedServiceProtobufDto.setPbFileContent(envoyServiceProtobufDto.getPbFileContent());
        envoyPublishedServiceProtobufDto.setPbServiceList(envoyServiceProtobufDto.getPbServiceList());
        return envoyPublishedServiceProtobufDto;
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

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String[] getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String[] serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public EnvoyServiceProtobufProxy toMeta() {

        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = new EnvoyServiceProtobufProxy();
        envoyServiceProtobufProxy.setId(this.getId());
        envoyServiceProtobufProxy.setCreateDate(this.getCreateDate());
        envoyServiceProtobufProxy.setModifyDate(this.getModifyDate());
        envoyServiceProtobufProxy.setServiceId(this.getServiceId());
        envoyServiceProtobufProxy.setPbFileName(this.getPbFileName());
        envoyServiceProtobufProxy.setPbFileContent(this.getPbFileContent());
        envoyServiceProtobufProxy.setPbServiceList(JSON.toJSONString(this.getPbServiceList()));
        envoyServiceProtobufProxy.setVirtualGwId(this.getVirtualGwId());
        return envoyServiceProtobufProxy;
    }

}
