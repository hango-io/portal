package org.hango.cloud.common.infra.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.swagger.models.*;
import io.swagger.util.Json;
import org.apache.commons.io.Charsets;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.holder.RequestContextHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiConvertToJsonService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.ISwaggerToMarkDown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hango.cloud.common.infra.base.meta.BaseConst.HANGO_DASHBOARD_PREFIX;


/**
 * @author wengyanghui@corp.netease.com
 * @version 创建时间: 2017/12/4 上午10:33.
 * @Modified hanjiahao
 */

@RestController
@RequestMapping(value = HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class SwaggerController extends AbstractController {

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IApiConvertToJsonService apiConvertToJsonService;

    @Autowired
    private ISwaggerToMarkDown swaggerToMarkDown;

    @Autowired
    private IApiBodyService apiBodyService;

    public static final String MARK_DOWN_DOC = "markDownDoc";

    /**
     * 生成swagger serviceid
     *
     * @param serviceId
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(params = {"Action=DescribeSwaggerServiceById"}, method = RequestMethod.GET)
    public void getServicesDocs(@RequestParam(value = "ServiceId") @NotNull long serviceId) throws JsonProcessingException {
        String method;
        Operation operation;
        String pathString;
        Map<String, Set<MethodOperation>> apiMethodMap = Maps.newHashMap();
        Map<String, Operation> operationMap = Maps.newHashMap();

        ServiceProxyDto serviceInfo = serviceProxyService.get(serviceId);
        if (serviceInfo == null) {
            apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
            return;
        }
        final Info info = new Info().title("所属服务：" + serviceInfo.getName());
        Swagger swagger = new Swagger()
                .info(info)
                .scheme(Scheme.HTTP)
                .consumes("application/json")
                .produces("application/json");

        List<ApiInfo> apiInfoList = apiInfoService.findAllApiByServiceId(serviceId);
        for (ApiInfo apiInfo : apiInfoList) {
            operation = apiConvertToJsonService.generateOperstaionForSwagger(apiInfo.getId(), swagger);
            method = apiInfo.getApiMethod().toLowerCase();
            pathString = apiInfo.getApiPath();

            if (apiMethodMap.containsKey(pathString)) {
                Set<MethodOperation> methodList = apiMethodMap.get(pathString);
                methodList.add(new MethodOperation(method, operation));
            } else {
                Set<MethodOperation> methodList = new HashSet<>();
                methodList.add(new MethodOperation(method, operation));
                apiMethodMap.put(pathString, methodList);
            }
        }

        //swagger中如果path相同，则会发生Operation覆盖的情况
        for (Map.Entry<String, Set<MethodOperation>> entry : apiMethodMap.entrySet()) {
            Path path = new Path();
            Set<MethodOperation> methodOperationSet = entry.getValue();
            for (MethodOperation methodOperation : methodOperationSet) {
                path.set(methodOperation.getMethod(), methodOperation.getOperation());
            }
            swagger.path(entry.getKey(), path);
        }

        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        try {
            response.getWriter().write(Json.mapper().writeValueAsString(swagger));
        } catch (IOException e) {
            logger.info("response写入出现异常");
            e.printStackTrace();
        }
//        return Json.mapper().writeValueAsString(swagger);
    }

    /**
     * 通过apiId 生成swagger Json
     *
     * @param interfaceId
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(params = {"Action=DescribeSwaggerApiById"}, method = RequestMethod.GET)
    public void getInterfacesDocs(@RequestParam(value = "InterfaceId") @NotNull long interfaceId) throws JsonProcessingException {
        if (apiInfoService.getApi(String.valueOf(interfaceId)) == null) {
            apiReturn(CommonApiErrorCode.NoSuchApiInterface);
            return;
        }
        HttpServletResponse response = RequestContextHolder.getResponse();
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.setContentType(MappingJackson2JsonView.DEFAULT_CONTENT_TYPE);
        try {
            response.getWriter().write(apiConvertToJsonService.generateSwaggerJson(interfaceId));
        } catch (IOException e) {
            logger.info("response写入出现异常");
            e.printStackTrace();
        }
    }

    /**
     * 获取接口的 markdown
     *
     * @param interfaceId
     * @return
     * @throws JsonProcessingException
     * @throws Exception
     */
    @RequestMapping(params = {"Action=DescribeMarkdownApiById"}, method = RequestMethod.GET)
    public Object getApiMarkDownDocs(@RequestParam(value = "InterfaceId") @NotNull long interfaceId) throws JsonProcessingException, Exception {
        logger.info("获取apiId:{}下的接口markdown", interfaceId);
        if (apiInfoService.getApi(String.valueOf(interfaceId)) == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        String swagger = apiConvertToJsonService.generateSwaggerJson(interfaceId);
        List<ApiStatusCode> apiStatusCodeList = apiBodyService.listStatusCode(interfaceId);
        String markDownDoc = swaggerToMarkDown.swaggerToMd(swagger, interfaceId, apiStatusCodeList, 1).get(MARK_DOWN_DOC);
        Map<String, Object> result = Maps.newHashMap();
        result.put("ServiceMarkdownString", markDownDoc);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 通过服务id，获取服务markdown
     *
     * @param serviceId
     * @return
     */
    @RequestMapping(params = {"Action=DescribeMarkdownServiceById"}, method = RequestMethod.GET)
    public Object getServiceMarkDownDocs(@RequestParam(value = "ServiceId") @NotNull long serviceId) {
        logger.info("获取serviceId:{}下的markdown文档", serviceId);
        ServiceProxyDto serviceInfo = serviceProxyService.get(serviceId);
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        String markDownDoc = getMarkDownDocsByServiceId(serviceId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("ServiceMarkdownString", markDownDoc);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 通过接口id下载markdown
     *
     * @param interfaceId
     * @return
     * @throws JsonProcessingException
     * @throws Exception
     */
    @RequestMapping(params = {"Action=DownloadMarkdownApiById"}, method = RequestMethod.GET, produces = "application/octet-stream;charset=UTF-8")
    public Object downloadApiMarkDownDocs(@RequestParam(value = "InterfaceId") @NotNull long interfaceId) throws JsonProcessingException, Exception {
        logger.info("获取apiId:{}下的接口markdown", interfaceId);
        ApiInfo apiInfo = apiInfoService.getApiById(interfaceId);
        if (apiInfo == null) {
            return apiReturn(CommonApiErrorCode.NoSuchApiInterface);
        }
        String swagger = apiConvertToJsonService.generateSwaggerJson(interfaceId);
        List<ApiStatusCode> apiStatusCodeList = apiBodyService.listStatusCode(interfaceId);
        String markDownDoc = swaggerToMarkDown.swaggerToMd(swagger, interfaceId, apiStatusCodeList, 1).get(MARK_DOWN_DOC);
        byte[] bytes = markDownDoc.getBytes(BaseConst.DEFAULT_ENCODING);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + apiInfo.getApiName() + "_Interface.md");
        HttpStatus statusCode = HttpStatus.OK;
        ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(bytes, headers, statusCode);
        return entity;

    }

    /**
     * 通过服务id，下载markdown
     *
     * @param serviceId
     * @return
     */
    @RequestMapping(params = {"Action=DownloadMarkdownServiceById"}, method = RequestMethod.GET, produces = "application/octet-stream;charset=UTF-8")
    public Object downloadServiceMarkDownDocs(@RequestParam(value = "ServiceId") @NotNull long serviceId) {
        logger.info("获取serviceId:{}下的markdown文档", serviceId);
        ServiceProxyDto serviceInfo = serviceProxyService.get(serviceId);
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        String markDownDoc = getMarkDownDocsByServiceId(serviceId);
        byte[] bytes = new byte[0];
        try {
            bytes = markDownDoc.getBytes(BaseConst.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            logger.info("通过服务id下线markdown文档出现异常,e:{}", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + serviceInfo.getName() + "_Service.md");
        HttpStatus statusCode = HttpStatus.OK;
        ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(bytes, headers, statusCode);
        return entity;
    }

    public String getMarkDownDocsByServiceId(long serviceId) {
        ServiceProxyDto serviceInfo = serviceProxyService.get(serviceId);
        String markDownDoc = "";
        markDownDoc += "# 服务名称\n\n";
        markDownDoc += "## " + serviceInfo.getName() + "\n\n";
        markDownDoc += "# 该服务下接口信息\n\n";
        markDownDoc += "---\n";
        int apiNumber = 1;

        List<ApiInfo> apiInfoList = apiInfoService.findAllApiByServiceId(serviceId);
        long id = 1;
        for (ApiInfo apiInfo : apiInfoList) {
            String swagger = "";
            try {
                swagger = apiConvertToJsonService.generateSwaggerJson(apiInfo.getId());
            } catch (Exception failedToLoadSwagger) {
                continue;
            }
            markDownDoc += "## " + apiNumber + ". " + apiInfo.getApiName() + "\n\n";
            List<ApiStatusCode> apiStatusCodeList = apiBodyService.listStatusCode(apiInfo.getId());
            Map<String, String> apiMarkDownMap = swaggerToMarkDown.swaggerToMd(swagger, apiInfo.getId(), apiStatusCodeList, id);
            id = Long.parseLong(apiMarkDownMap.get("id")) + 1;
            String apiMarkDownDoc = apiMarkDownMap.get(markDownDoc);
            markDownDoc += apiMarkDownDoc + "\n";
            apiNumber++;
        }
        return markDownDoc;
    }

    class MethodOperation {
        private String method;
        private Operation operation;

        public MethodOperation(String method, Operation operation) {
            this.method = method;
            this.operation = operation;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }
    }

}

