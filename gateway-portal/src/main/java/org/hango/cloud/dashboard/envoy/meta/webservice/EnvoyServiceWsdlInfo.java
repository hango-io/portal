package org.hango.cloud.dashboard.envoy.meta.webservice;

import java.io.Serializable;
import java.util.List;

public class EnvoyServiceWsdlInfo implements Serializable {
    private static final long serialVersionUID = -556231408518197474L;

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
     * 网关id
     */
    private long gwId;

    /**
     * 服务id
     */
    private long serviceId;

    /**
     * 上传的wsdl文件名
     */
    private String wsdlFileName;

    /**
     * wsdl文件内容，直接存string
     */
    private String wsdlFileContent;

    /**
     * wsdl的binding信息
     */
    private List<EnvoyServiceWsdlBindingItem> wsdlBindingList;

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

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public String getWsdlFileName() {
        return wsdlFileName;
    }

    public void setWsdlFileName(String wsdlFileName) {
        this.wsdlFileName = wsdlFileName;
    }

    public String getWsdlFileContent() {
        return wsdlFileContent;
    }

    public void setWsdlFileContent(String wsdlFileContent) {
        this.wsdlFileContent = wsdlFileContent;
    }

    public List<EnvoyServiceWsdlBindingItem> getWsdlBindingList() {
        return wsdlBindingList;
    }

    public void setWsdlBindingList(List<EnvoyServiceWsdlBindingItem> wsdlBindingList) {
        this.wsdlBindingList = wsdlBindingList;
    }
}
