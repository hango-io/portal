package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * webservice服务对应的参数dto
 *
 * @author
 */
public class WebServiceParamInfoDto implements Serializable {

    private static final long serialVersionUID = 7143023953952293269L;

    @JSONField(name = "ApiId")
    private long id;

    /**
     * 类名
     */
    @JSONField(name = "ClassName")
    @NotEmpty
    private String className;

    /**
     * 方法名
     */
    @JSONField(name = "MethodName")
    @NotEmpty
    private String methodName;

    /**
     * 请求参数列表
     */
    @JSONField(name = "RequestParam")
    @Valid
    private List<WebServiceRequestParamDto> requestParam;

    /**
     * 响应参数列表
     */
    @JSONField(name = "ResponseParam")
    @Valid
    private List<WebServiceRequestParamDto> responseParam;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<WebServiceRequestParamDto> getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(List<WebServiceRequestParamDto> requestParam) {
        this.requestParam = requestParam;
    }

    public List<WebServiceRequestParamDto> getResponseParam() {
        return responseParam;
    }

    public void setResponseParam(List<WebServiceRequestParamDto> responseParam) {
        this.responseParam = responseParam;
    }
}