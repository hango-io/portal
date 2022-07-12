package org.hango.cloud.dashboard.apiserver.meta.grpc;

import java.io.Serializable;

/**
 * API和gRPC method之间的对应关系
 *
 * @Author: TC_WANG
 * @Date: 2019/7/2
 */
public class ApiGrpcParam implements Serializable {

    private static final long serialVersionUID = -5064433855984714447L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long apiId;
    private long serviceId;

    /**
     * pb对应的名称
     */
    private String pbName;

    /**
     * package名称
     */
    private String pbPackageName;

    /**
     * service名称
     */
    private String pbServiceName;

    /**
     * method名称
     */
    private String pbMethodName;

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

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
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

    public String getPbPackageName() {
        return pbPackageName;
    }

    public void setPbPackageName(String pbPackageName) {
        this.pbPackageName = pbPackageName;
    }

    public String getPbServiceName() {
        return pbServiceName;
    }

    public void setPbServiceName(String pbServiceName) {
        this.pbServiceName = pbServiceName;
    }

    public String getPbMethodName() {
        return pbMethodName;
    }

    public void setPbMethodName(String pbMethodName) {
        this.pbMethodName = pbMethodName;
    }
}
