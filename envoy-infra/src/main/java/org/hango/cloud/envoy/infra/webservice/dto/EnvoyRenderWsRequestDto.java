package org.hango.cloud.envoy.infra.webservice.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;


public class EnvoyRenderWsRequestDto {
    @JSONField(name = "RequestTemplate")
    @NotEmpty
    private String requestTemplate;

    @JSONField(name = "Body")
    private String body;

    @JSONField(name = "Param")
    private String param;

    public String getRequestTemplate() {
        return requestTemplate;
    }

    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
