package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ApiExportDto {
    @JSONField(name = "ApiInfo")
    private ApiInfoBasicDto apiInfoBasicDto;
    @JSONField(name = "RequestHeader")
    private List<ApiHeaderBasicDto> requestHeader;
    @JSONField(name = "ResponseHeader")
    private List<ApiHeaderBasicDto> responseHeader;
    @JSONField(name = "QueryString")
    private List<ApiBodyBasicDto> queryString;
    @JSONField(name = "RequestBody")
    private List<ApiBodyBasicDto> requestBody;
    @JSONField(name = "ResponseBody")
    private List<ApiBodyBasicDto> responseBody;
    @JSONField(name = "StatusCode")
    private List<ApiStatusCodeBasicDto> statusCode;
    @JSONField(name = "ApiExample")
    private ApiExampleDto apiExampleDto;

    public ApiInfoBasicDto getApiInfoBasicDto() {
        return apiInfoBasicDto;
    }

    public void setApiInfoBasicDto(ApiInfoBasicDto apiInfoBasicDto) {
        this.apiInfoBasicDto = apiInfoBasicDto;
    }

    public List<ApiHeaderBasicDto> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(List<ApiHeaderBasicDto> requestHeader) {
        this.requestHeader = requestHeader;
    }

    public List<ApiHeaderBasicDto> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(List<ApiHeaderBasicDto> responseHeader) {
        this.responseHeader = responseHeader;
    }

    public List<ApiBodyBasicDto> getQueryString() {
        return queryString;
    }

    public void setQueryString(List<ApiBodyBasicDto> queryString) {
        this.queryString = queryString;
    }

    public List<ApiBodyBasicDto> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(List<ApiBodyBasicDto> requestBody) {
        this.requestBody = requestBody;
    }

    public List<ApiBodyBasicDto> getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(List<ApiBodyBasicDto> responseBody) {
        this.responseBody = responseBody;
    }

    public List<ApiStatusCodeBasicDto> getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(List<ApiStatusCodeBasicDto> statusCode) {
        this.statusCode = statusCode;
    }

    public ApiExampleDto getApiExampleDto() {
        return apiExampleDto;
    }

    public void setApiExampleDto(ApiExampleDto apiExampleDto) {
        this.apiExampleDto = apiExampleDto;
    }
}
