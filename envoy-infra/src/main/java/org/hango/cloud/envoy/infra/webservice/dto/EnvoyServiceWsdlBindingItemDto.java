package org.hango.cloud.envoy.infra.webservice.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * ServiceWsdlDto.bindings的成员
 */
public class EnvoyServiceWsdlBindingItemDto {
    @JSONField(name = "Service")
    private String service;

    @JSONField(name = "Port")
    private String port;

    @JSONField(name = "PortType")
    private String portType;

    @JSONField(name = "Operation")
    private String operation;

    @JSONField(name = "Binding")
    private String binding;

    @JSONField(name = "Input")
    private String input;

    @JSONField(name = "Output")
    private String output;

    @JSONField(name = "Address")
    private String address;

    @JSONField(name = "RequestAllElements")
    private List<EnvoyServiceWsdlElementDto> requestAllElements;

    @JSONField(name = "ResponseAllElements")
    private List<EnvoyServiceWsdlElementDto> responseAllElements;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<EnvoyServiceWsdlElementDto> getRequestAllElements() {
        return requestAllElements;
    }

    public void setRequestAllElements(List<EnvoyServiceWsdlElementDto> requestAllElements) {
        this.requestAllElements = requestAllElements;
    }

    public List<EnvoyServiceWsdlElementDto> getResponseAllElements() {
        return responseAllElements;
    }

    public void setResponseAllElements(List<EnvoyServiceWsdlElementDto> responseAllElements) {
        this.responseAllElements = responseAllElements;
    }
}
