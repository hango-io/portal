package org.hango.cloud.gdashboard.api.meta.swagger;

import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;

import java.util.List;

public class SwaggerApiInfo {
    /**
     * ApiInfo基本信息
     */
    private ApiInfo apiInfo;
    /**
     * api request 请求header
     */
    private List<ApiHeader> apiRequestHeader;
    /**
     * api response 请求header
     */
    private List<ApiHeader> apiResponseHeader;
    /**
     * api request body
     */
    private List<ApiBody> apiRequestBody;
    /**
     * api response body
     */
    private List<ApiBody> apiResponseBody;
    /**
     * api query string
     */
    private List<ApiBody> apiQueryString;
    /**
     * api status code
     */
    private List<ApiStatusCode> apiStatusCodes;

    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo = apiInfo;
    }

    public List<ApiHeader> getApiRequestHeader() {
        return apiRequestHeader;
    }

    public void setApiRequestHeader(List<ApiHeader> apiRequestHeader) {
        this.apiRequestHeader = apiRequestHeader;
    }

    public List<ApiHeader> getApiResponseHeader() {
        return apiResponseHeader;
    }

    public void setApiResponseHeader(List<ApiHeader> apiResponseHeader) {
        this.apiResponseHeader = apiResponseHeader;
    }

    public List<ApiBody> getApiRequestBody() {
        return apiRequestBody;
    }

    public void setApiRequestBody(List<ApiBody> apiRequestBody) {
        this.apiRequestBody = apiRequestBody;
    }

    public List<ApiBody> getApiResponseBody() {
        return apiResponseBody;
    }

    public void setApiResponseBody(List<ApiBody> apiResponseBody) {
        this.apiResponseBody = apiResponseBody;
    }

    public List<ApiBody> getApiQueryString() {
        return apiQueryString;
    }

    public void setApiQueryString(List<ApiBody> apiQueryString) {
        this.apiQueryString = apiQueryString;
    }

    public List<ApiStatusCode> getApiStatusCodes() {
        return apiStatusCodes;
    }

    public void setApiStatusCodes(List<ApiStatusCode> apiStatusCodes) {
        this.apiStatusCodes = apiStatusCodes;
    }
}
