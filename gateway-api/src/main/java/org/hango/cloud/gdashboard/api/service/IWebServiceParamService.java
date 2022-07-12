package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.dto.WebServiceParamInfoDto;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/27
 */
public interface IWebServiceParamService {

    WebServiceParamInfoDto getWebServiceParam(long apiId);

    long updateWebServiceParam(WebServiceParamInfoDto webServiceParamInfoDto, long apiId);

    /**
     * 获取wsdl文件中的接口（服务）列表
     *
     * @param wsdlUrl
     * @return
     */
    List<String> getWebServiceInterfaceByCache(String wsdlUrl);

    /**
     * 获取wsdl文件中的方法名
     *
     * @param wsdlUrl
     * @return
     */
    List<String> getWebServiceMethod(String wsdlUrl);

    /**
     * 校验webservice param
     *
     * @param webServiceParamInfoDto webserviceParam包装dto
     * @return
     */
    ApiErrorCode checkWebServiceParam(WebServiceParamInfoDto webServiceParamInfoDto);

    /**
     * 获取webserviceParam操作记录
     *
     * @param webServiceParamInfoDto webserviceParam包装dto
     * @return 操作记录stringBuilder
     */
    StringBuilder getWebServiceOperationLog(WebServiceParamInfoDto webServiceParamInfoDto);

    long deleteWebserviceParam(long apiId);

}
