package org.hango.cloud.gdashboard.api.service.impl;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.dao.WebServiceParamDao;
import org.hango.cloud.gdashboard.api.dto.WebServiceParamInfoDto;
import org.hango.cloud.gdashboard.api.dto.WebServiceRequestParamDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.WebServiceParamInfo;
import org.hango.cloud.gdashboard.api.meta.WebServiceParamType;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IWebServiceParamService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.hango.cloud.gdashboard.api.util.WsdlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2019/1/6 下午11:25.
 */
@Component
public class WebServiceParamServiceImpl implements IWebServiceParamService {

    private static Logger logger = LoggerFactory.getLogger(WebServiceParamServiceImpl.class);

    @Autowired
    private WebServiceParamDao webServiceParamDao;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiBodyService apiBodyService;


    //设置参数类型的缓存
    private LoadingCache<String, Optional<List>> wsdlUrlCache = CacheBuilder.newBuilder().maximumSize(5000)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<List>>() {

                @Override
                public Optional<List> load(String wsdlUrl) throws Exception {
                    List<String> serviceNameList = new ArrayList<>();
                    if (StringUtils.isBlank(wsdlUrl)) {
                        return Optional.fromNullable(serviceNameList);
                    }

                    return Optional.fromNullable(getWebServiceInterface(wsdlUrl));

                }

            });

    @Override
    public WebServiceParamInfoDto getWebServiceParam(long apiId) {
        Map<String, Object> params = new HashMap<>();
        params.put("apiId", apiId);

        List<WebServiceParamInfo> webServiceParamInfoList = webServiceParamDao.getRecordsByField(params);
        WebServiceParamInfoDto webServiceParamInfoDto = new WebServiceParamInfoDto();
        List<WebServiceRequestParamDto> requestParamDtoList = new ArrayList<>();
        List<WebServiceRequestParamDto> responseParamDtoList = new ArrayList<>();
        for (WebServiceParamInfo webServiceParamInfo : webServiceParamInfoList) {
            if (webServiceParamInfo.getType().equals(WebServiceParamType.Service.name())) {
                webServiceParamInfoDto.setClassName(webServiceParamInfo.getParamName());
                continue;
            }
            if (webServiceParamInfo.getType().equals(WebServiceParamType.Method.name())) {
                webServiceParamInfoDto.setMethodName(webServiceParamInfo.getParamName());
                continue;
            }
            WebServiceRequestParamDto webServiceRequestParamDto = new WebServiceRequestParamDto();
            webServiceRequestParamDto.setArrayDataTypeId(webServiceParamInfo.getArrayDataTypeId());
            webServiceRequestParamDto.setParamTypeId(webServiceParamInfo.getParamTypeId());
            webServiceRequestParamDto.setParamName(webServiceParamInfo.getParamName());
            webServiceRequestParamDto.setDescription(webServiceParamInfo.getDescription());
            //缓存
            webServiceRequestParamDto.setParamType(apiParamTypeService.listApiParamType(webServiceParamInfo.getParamTypeId()).getParamType());
            webServiceRequestParamDto.setArrayDataTypeName(apiParamTypeService.listApiParamType(webServiceParamInfo.getParamTypeId()).getParamType());

            webServiceRequestParamDto.setParamSort(webServiceParamInfo.getParamSort());
            if (webServiceParamInfo.getType().equals(WebServiceParamType.RequestParam.name())) {
                requestParamDtoList.add(webServiceRequestParamDto);
            }
            if (webServiceParamInfo.getType().equals(WebServiceParamType.ResponseParam.name())) {
                responseParamDtoList.add(webServiceRequestParamDto);
            }
        }
        webServiceParamInfoDto.setRequestParam(requestParamDtoList);
        webServiceParamInfoDto.setResponseParam(responseParamDtoList);

        return webServiceParamInfoDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateWebServiceParam(WebServiceParamInfoDto webServiceParamInfoDto, long apiId) {
        //先删除
        WebServiceParamInfo webServiceParamInfo = new WebServiceParamInfo();
        webServiceParamInfo.setApiId(apiId);
        webServiceParamDao.delete(webServiceParamInfo);

        apiBodyService.deleteBody(apiId, Const.REQUEST_PARAM_TYPE);
        apiBodyService.deleteBody(apiId, Const.RESPONSE_PARAM_TYPE);

        //后创建
        List<WebServiceParamInfo> webServiceParamInfoList = new ArrayList<>();
        List<ApiBody> apiBodyList = new ArrayList<>();

        String className = webServiceParamInfoDto.getClassName();
        webServiceParamInfo = new WebServiceParamInfo();
        webServiceParamInfo.setApiId(apiId);
        webServiceParamInfo.setCreateDate(System.currentTimeMillis());
        webServiceParamInfo.setModifyDate(System.currentTimeMillis());
        webServiceParamInfo.setParamName(className);
        webServiceParamInfo.setType(WebServiceParamType.Service.name());
        webServiceParamInfoList.add(webServiceParamInfo);

        String method = webServiceParamInfoDto.getMethodName();
        webServiceParamInfo = new WebServiceParamInfo();
        webServiceParamInfo.setApiId(apiId);
        webServiceParamInfo.setCreateDate(System.currentTimeMillis());
        webServiceParamInfo.setModifyDate(System.currentTimeMillis());
        webServiceParamInfo.setParamName(method);
        webServiceParamInfo.setType(WebServiceParamType.Method.name());
        webServiceParamInfoList.add(webServiceParamInfo);

        //请求参数
        for (WebServiceRequestParamDto webServiceRequestParamDto : webServiceParamInfoDto.getRequestParam()) {
            webServiceParamInfo = new WebServiceParamInfo();
            webServiceParamInfo.setApiId(apiId);
            webServiceParamInfo.setCreateDate(System.currentTimeMillis());
            webServiceParamInfo.setModifyDate(System.currentTimeMillis());
            webServiceParamInfo.setParamName(webServiceRequestParamDto.getParamName());
            webServiceParamInfo.setType(WebServiceParamType.RequestParam.name());
            webServiceParamInfo.setParamTypeId(webServiceRequestParamDto.getParamTypeId());
            webServiceParamInfo.setParamType(apiParamTypeService.listApiParamType(webServiceRequestParamDto.getParamTypeId()).getParamType());
            webServiceParamInfo.setArrayDataTypeId(webServiceRequestParamDto.getArrayDataTypeId());
            webServiceParamInfo.setParamSort(webServiceRequestParamDto.getParamSort());
            webServiceParamInfo.setDescription(webServiceRequestParamDto.getDescription());
            webServiceParamInfoList.add(webServiceParamInfo);
            apiBodyList.add(WebServiceParamInfo.castToApiBody(webServiceParamInfo));
        }

        //响应参数
        for (WebServiceRequestParamDto webServiceRequestParamDto : webServiceParamInfoDto.getResponseParam()) {
            webServiceParamInfo = new WebServiceParamInfo();
            webServiceParamInfo.setApiId(apiId);
            webServiceParamInfo.setCreateDate(System.currentTimeMillis());
            webServiceParamInfo.setModifyDate(System.currentTimeMillis());
            webServiceParamInfo.setParamName(webServiceRequestParamDto.getParamName());
            webServiceParamInfo.setType(WebServiceParamType.ResponseParam.name());
            webServiceParamInfo.setParamTypeId(webServiceRequestParamDto.getParamTypeId());
            webServiceParamInfo.setParamType(apiParamTypeService.listApiParamType(webServiceRequestParamDto.getParamTypeId()).getParamType());
            webServiceParamInfo.setArrayDataTypeId(webServiceRequestParamDto.getArrayDataTypeId());
            webServiceParamInfo.setParamSort(webServiceRequestParamDto.getParamSort());
            webServiceParamInfo.setDescription(webServiceRequestParamDto.getDescription());
            webServiceParamInfoList.add(webServiceParamInfo);
            apiBodyList.add(WebServiceParamInfo.castToApiBody(webServiceParamInfo));
        }

        for (WebServiceParamInfo webServiceParamInfo1 : webServiceParamInfoList) {
            webServiceParamDao.add(webServiceParamInfo1);
        }

        //添加到BodyParam
        apiBodyService.addBody(apiBodyList);

        return 0;
    }

    /**
     * wsdl : "http://localhost:8080/services/CommonService?wsdl";
     *
     * @param wsdlUrl
     * @return
     */
    @Override
    public List<String> getWebServiceInterfaceByCache(String wsdlUrl) {
        return wsdlUrlCache.getUnchecked(wsdlUrl).orNull();
    }

    public List<String> getWebServiceInterface(String wsdlUrl) {
        List<String> serviceNameList = new ArrayList<>();

        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", true);
            reader.setFeature("javax.wsdl.importDocuments", true);
            Definition def = reader.readWSDL(wsdlUrl);

            Map<QName, Service> map = def.getServices();
            for (Map.Entry<QName, Service> entry : map.entrySet()) {
                serviceNameList.add(entry.getKey().getLocalPart());
            }
        } catch (Exception e) {
            logger.warn("获取wsdl中接口列表时，读取wsdl文件发生异常, wsdlUrl: {}", wsdlUrl);
            e.printStackTrace();
        }
        return serviceNameList;
    }

    /**
     * wsdl : "http://localhost:8080/services/CommonService?wsdl";
     *
     * @param wsdlUrl
     * @return
     */
    @Override
    public List<String> getWebServiceMethod(String wsdlUrl) {
        List<String> methodList = new ArrayList<>();

        try {
            methodList = WsdlUtil.getOperationList(wsdlUrl);
        } catch (Exception e) {
            logger.warn("获取wsdl中方法列表时，读取wsdl文件发生异常, wsdlUrl: {}", wsdlUrl);
            e.printStackTrace();
        }
        return methodList;
    }

    @Override
    public ApiErrorCode checkWebServiceParam(WebServiceParamInfoDto webServiceParamInfoDto) {

        if (StringUtils.isBlank(webServiceParamInfoDto.getClassName())) {
            return CommonApiErrorCode.MissingParameter("ClassName");
        }
        if (StringUtils.isBlank(webServiceParamInfoDto.getMethodName())) {
            return CommonApiErrorCode.MissingParameter("MethodName");
        }
        //是否存在requestParam
        if (!CollectionUtils.isEmpty(webServiceParamInfoDto.getRequestParam())) {
            //请求参数
            for (WebServiceRequestParamDto webServiceRequestParamDto : webServiceParamInfoDto.getRequestParam()) {
                if (StringUtils.isBlank(webServiceRequestParamDto.getParamName())) {
                    return CommonApiErrorCode.MissingParameter("ParamName");
                }

                if (webServiceRequestParamDto.getParamSort() <= 0) {
                    return CommonApiErrorCode.InvalidParameter(String.valueOf(webServiceRequestParamDto.getParamSort()), "ParamSort");
                }
                if (apiParamTypeService.listApiParamType(webServiceRequestParamDto.getParamTypeId()) == null) {
                    return CommonApiErrorCode.NoSuchParamType;
                }
                if (webServiceRequestParamDto.getArrayDataTypeId() != 0 && apiParamTypeService.listApiParamType(webServiceRequestParamDto.getArrayDataTypeId()) == null) {
                    return CommonApiErrorCode.NoSuchArrayDataType;
                }
            }
        }
        return CommonApiErrorCode.Success;
    }

    @Override
    public StringBuilder getWebServiceOperationLog(WebServiceParamInfoDto webServiceParamInfoDto) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");

        int count = 3;
        String className = webServiceParamInfoDto.getClassName();
        String methodName = webServiceParamInfoDto.getMethodName();
        stringBuilder.append("1. 类名" + className);
        stringBuilder.append("2. 方法名" + methodName);

        List<WebServiceRequestParamDto> webServiceRequestParamDtoList = webServiceParamInfoDto.getRequestParam();
        for (WebServiceRequestParamDto webServiceRequestParamDto : webServiceRequestParamDtoList) {
            stringBuilder.append(count + ". 请求参数名称：" + webServiceRequestParamDto.getParamName() + ", 类型：" +
                    apiParamTypeService.listApiParamType(webServiceRequestParamDto.getParamTypeId()).getParamType() + ", 描述：" + webServiceRequestParamDto.getDescription() + ". ");
            count++;
        }
        webServiceRequestParamDtoList = webServiceParamInfoDto.getResponseParam();
        for (WebServiceRequestParamDto webServiceRequestParamDto : webServiceRequestParamDtoList) {
            stringBuilder.append(count + ". 响应参数名称：" + webServiceRequestParamDto.getParamName() + ", 类型：" +
                    apiParamTypeService.listApiParamType(webServiceRequestParamDto.getParamTypeId()).getParamType() + ", 描述：" + webServiceRequestParamDto.getDescription() + ". ");
            count++;
        }
        stringBuilder.append("}");
        return stringBuilder;
    }

    @Override
    public long deleteWebserviceParam(long apiId) {
        return webServiceParamDao.deleteByApiId(apiId);
    }
}
