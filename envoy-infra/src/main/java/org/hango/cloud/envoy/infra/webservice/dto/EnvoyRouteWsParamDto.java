package org.hango.cloud.envoy.infra.webservice.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyRouteWsParamInfo;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class EnvoyRouteWsParamDto {
    /**
     * 主键id
     */
    @JSONField(name = "Id")
    private long id;

    /**
     * 创建时间
     */
    @JSONField(name = "CreateData")
    private long createDate;

    /**
     * 修改时间
     */
    @JSONField(name = "ModifyDate")
    private long modifyDate;

    /**
     * 网关id
     */
    @JSONField(name = "VirtualGwId")
    @NotNull
    private long virtualGwId;

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    @NotNull
    private long serviceId;

    /**
     * 路由id
     */
    @JSONField(name = "RouteId")
    @NotNull
    private long routeId;

    /**
     * 请求模板
     */
    @JSONField(name = "RequestTemplate")
    @NotEmpty
    private String requestTemplate;

    /**
     * 响应中数组类型的message数组
     */
    @JSONField(name = "ResponseArrayTypeList")
    private List<String> responseArrayTypeList;

    /**
     * 绑定的webservice的portType
     */
    @JSONField(name = "WsPortType")
    @NotEmpty
    private String wsPortType;

    /**
     * 绑定的webservice的operation
     */
    @JSONField(name = "WsOperation")
    @NotEmpty
    private String wsOperation;

    /**
     * 绑定的webserivce的binding
     */
    @JSONField(name = "WsBinding")
    @NotEmpty
    private String wsBinding;

    /**
     * 绑定的webservice的address
     */
    @JSONField(name = "WsAddress")
    @NotEmpty
    private String wsAddress;

    public EnvoyRouteWsParamDto(EnvoyRouteWsParamInfo wsParamInfo) {
        this.id = wsParamInfo.getId();
        this.createDate = wsParamInfo.getCreateDate();
        this.modifyDate = wsParamInfo.getModifyDate();
        this.virtualGwId = wsParamInfo.getVirtualGwId();
        this.serviceId = wsParamInfo.getServiceId();
        this.routeId = wsParamInfo.getRouteId();
        this.requestTemplate = wsParamInfo.getRequestTemplate();
        this.responseArrayTypeList = wsParamInfo.getResponseArrayTypeList();
        this.wsPortType = wsParamInfo.getWsPortType();
        this.wsOperation = wsParamInfo.getWsOperation();
        this.wsBinding = wsParamInfo.getWsBinding();
        this.wsAddress = wsParamInfo.getWsAddress();
    }

    public EnvoyRouteWsParamDto() {
    }

    public EnvoyRouteWsParamDto(long id, long createDate, long modifyDate, long virtualGwId, long serviceId, long routeId, String requestTemplate, List<String> responseArrayTypeList, String wsPortType, String wsOperation, String wsBinding, String wsAddress) {
        this.id = id;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.virtualGwId = virtualGwId;
        this.serviceId = serviceId;
        this.routeId = routeId;
        this.requestTemplate = requestTemplate;
        this.responseArrayTypeList = responseArrayTypeList;
        this.wsPortType = wsPortType;
        this.wsOperation = wsOperation;
        this.wsBinding = wsBinding;
        this.wsAddress = wsAddress;
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

    public EnvoyRouteWsParamInfo toMeta() {
        EnvoyRouteWsParamInfo paramInfo = new EnvoyRouteWsParamInfo();
        paramInfo.setVirtualGwId(virtualGwId);
        paramInfo.setServiceId(serviceId);
        paramInfo.setRouteId(routeId);
        paramInfo.setId(id);
        paramInfo.setCreateDate(createDate);
        paramInfo.setModifyDate(modifyDate);
        paramInfo.setRequestTemplate(requestTemplate);
        paramInfo.setResponseArrayTypeList(responseArrayTypeList);
        paramInfo.setWsPortType(wsPortType);
        paramInfo.setWsOperation(wsOperation);
        paramInfo.setWsBinding(wsBinding);
        paramInfo.setWsAddress(wsAddress);
        return paramInfo;
    }
}
