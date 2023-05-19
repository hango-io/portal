package org.hango.cloud.envoy.infra.webservice.meta;

import java.io.Serializable;
import java.util.List;

public class EnvoyRouteWsParamInfo implements Serializable {
    private static final long serialVersionUID = -243463653613520088L;

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
    private long virtualGwId;

    /**
     * 服务id
     */
    private long serviceId;

    /**
     * 路由id
     */
    private long routeId;

    /**
     * 请求模板
     */
    private String requestTemplate;

    /**
     * 响应中数组类型的message数组
     */
    private List<String> responseArrayTypeList;

    /**
     * 绑定的webservice的portType
     */
    private String wsPortType;

    /**
     * 绑定的webservice的operation
     */
    private String wsOperation;

    /**
     * 绑定的webserivce的binding
     */
    private String wsBinding;

    /**
     * 绑定的webservice的address
     */
    private String wsAddress;

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

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public List<String> getResponseArrayTypeList() {
        return responseArrayTypeList;
    }

    public void setResponseArrayTypeList(List<String> responseArrayTypeList) {
        this.responseArrayTypeList = responseArrayTypeList;
    }

    public String getWsPortType() {
        return wsPortType;
    }

    public void setWsPortType(String wsPortType) {
        this.wsPortType = wsPortType;
    }

    public String getWsOperation() {
        return wsOperation;
    }

    public void setWsOperation(String wsOperation) {
        this.wsOperation = wsOperation;
    }

    public String getWsBinding() {
        return wsBinding;
    }

    public void setWsBinding(String wsBinding) {
        this.wsBinding = wsBinding;
    }

    public String getWsAddress() {
        return wsAddress;
    }

    public void setWsAddress(String wsAddress) {
        this.wsAddress = wsAddress;
    }
}
