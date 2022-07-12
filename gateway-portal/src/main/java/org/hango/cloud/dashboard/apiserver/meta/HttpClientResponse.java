package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.httpclient.Header;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * http response object.
 *
 * @author Feng Changjian (hzfengchj@corp.netease.com)
 * @version $Id: HttpClientResponse.java, v 1.0 2013-8-2 下午04:10:16
 */
public class HttpClientResponse {
    private int statusCode;

    private List<Header> headerList = null;

    private String responseBody = null;

    private byte[] bodyContent = null;


    /**
     * @param statusCode
     * @param headerList
     * @param responseBody
     */
    public HttpClientResponse(int statusCode, List<Header> headerList, String responseBody) {
        super();
        this.statusCode = statusCode;
        this.headerList = headerList;
        this.responseBody = responseBody;
    }

    public HttpClientResponse(int statusCode, List<Header> headerList, byte[] responseBody) {
        super();
        this.statusCode = statusCode;
        this.headerList = headerList;
        this.setBodyContent(responseBody);
    }


    /**
     * Getter method for property <tt>statusCode</tt>.
     *
     * @return property value of statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Setter method for property <tt>statusCode</tt>.
     *
     * @param statusCode value to be assigned to property statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Getter method for property <tt>headerList</tt>.
     *
     * @return property value of headerList
     */
    public List<Header> getHeaderList() {
        return headerList;
    }

    /**
     * Setter method for property <tt>headerList</tt>.
     *
     * @param headerList value to be assigned to property headerList
     */
    public void setHeaderList(List<Header> headerList) {
        this.headerList = headerList;
    }

    /**
     * Getter method for property <tt>responseBody</tt>.
     *
     * @return property value of responseBody
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Setter method for property <tt>responseBody</tt>.
     *
     * @param responseBody value to be assigned to property responseBody
     */
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public byte[] getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(byte[] bodyContent) {
        this.bodyContent = bodyContent;
    }

}
