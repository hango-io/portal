package org.hango.cloud.dashboard.envoy.meta.webservice;

import java.util.List;

public class EnvoyServiceWsdlBindingItem {

    /**
     * webservice的service
     */
    private String service;
    /**
     * webservice的port
     */
    private String port;
    /**
     * webservice的portType
     */
    private String portType;

    /**
     * webservice的operation
     */
    private String operation;

    /**
     * webservice的binding
     */
    private String binding;

    /**
     * webservice的input
     */
    private String input;

    /**
     * webservice的output
     */
    private String output;

    /**
     * webservice的address
     */
    private String address;

    /**
     * webservice请求中所有的elements
     */
    private List<ElementInfo> requestAllElements;

    /**
     * webservice响应中所有的elements
     */
    private List<ElementInfo> responseAllElements;

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

    public List<ElementInfo> getRequestAllElements() {
        return requestAllElements;
    }

    public void setRequestAllElements(List<ElementInfo> requestAllElements) {
        this.requestAllElements = requestAllElements;
    }

    public List<ElementInfo> getResponseAllElements() {
        return responseAllElements;
    }

    public void setResponseAllElements(List<ElementInfo> responseAllElements) {
        this.responseAllElements = responseAllElements;
    }
}
