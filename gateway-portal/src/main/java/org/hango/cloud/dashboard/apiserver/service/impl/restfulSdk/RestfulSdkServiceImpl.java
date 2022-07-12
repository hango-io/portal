package org.hango.cloud.dashboard.apiserver.service.impl.restfulSdk;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.CodeGenerator;
import org.hango.cloud.dashboard.apiserver.service.impl.sdk.SdkConst;
import org.hango.cloud.dashboard.apiserver.service.sdk.IRestfulSdkService;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author hanjiahao
 * restful sdk生成service
 */
@Service
public class RestfulSdkServiceImpl implements IRestfulSdkService {
    private static Logger logger = LoggerFactory.getLogger(RestfulSdkServiceImpl.class);
    Configuration config;
    Template requestTemp = null;
    Template responseTemp = null;
    Template clientTemp = null;
    Template dataModelTemp = null;
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private RestfulSdkUtils restfulSdkUtils;
    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IApiHeaderService apiHeaderService;


    // 服务加载时读取模板配置
    public RestfulSdkServiceImpl() {
        config = new Configuration(Configuration.VERSION_2_3_26);
        config.setClassForTemplateLoading(RestfulSdkServiceImpl.class, "/");
        // 加载模板文件
        try {
            clientTemp = config.getTemplate("/codeTemplate/restfulClient.ftl");
            requestTemp = config.getTemplate("/codeTemplate/restfulRequest.ftl");
            responseTemp = config.getTemplate("/codeTemplate/restfulResponse.ftl");
            dataModelTemp = config.getTemplate("/codeTemplate/dataModel.ftl");

        } catch (IOException e) {
            logger.error("Failed to load template file. Check the tamplate Existence or Syntax error in tamplate. \n" + e.getMessage());
        }
    }

    /**
     * 获得单个接口sdk
     *
     * @param apiId
     * @param uuid
     * @param singleApiFlag
     * @return
     */
    @Override
    public String generateApiSdk(String apiId, String uuid, boolean singleApiFlag) {
        if (requestTemp == null || responseTemp == null || clientTemp == null) {
            logger.error(SdkConst.TEMPLATE_LOAD_FIALED);
            return SdkConst.TEMPLATE_LOAD_FIALED;
        }
        // 查询API基本信息
        ApiInfo apiInfo = apiInfoService.getApi(apiId);
        if (apiInfo == null) {
            logger.info(SdkConst.API_NOT_FOUND);
            return SdkConst.API_NOT_FOUND;
        }
        // 查询API的Query string部分
        List<ApiBody> apiQueryString = apiBodyService.getBody(NumberUtils.toLong(apiId), Const.QUERYSTRING_PARAM_TYPE);
        for (ApiBody apiBody : apiQueryString) {
            restfulSdkUtils.getArrayType(apiBody);
        }

        // 查询API的Request header部分
        List<ApiHeader> apiRequestHeaders = apiHeaderService.getHeader(NumberUtils.toLong(apiId), Const.REQUEST_PARAM_TYPE);

        // 查询API的Response header部分
        List<ApiHeader> apiResponseHeaders = apiHeaderService.getHeader(NumberUtils.toLong(apiId), Const.RESPONSE_PARAM_TYPE);

        // 查询API的Request body部分
        List<ApiBody> apiRequestBody = apiBodyService.getBody(NumberUtils.toLong(apiId), Const.REQUEST_PARAM_TYPE);
        for (ApiBody apiBody : apiRequestBody) {
            restfulSdkUtils.getArrayType(apiBody);
        }

        // 查询API的Response body部分
        List<ApiBody> apiResponseBody = apiBodyService.getBody(NumberUtils.toLong(apiId), Const.RESPONSE_PARAM_TYPE);
        for (ApiBody apiBody : apiResponseBody) {
            restfulSdkUtils.getArrayType(apiBody);
        }

        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(apiInfo.getServiceId());
        // 生成代码
        try {
            RestfulCodeGenerator.generateRequest(requestTemp, apiInfo, serviceInfo, apiQueryString, apiRequestHeaders, apiRequestBody, uuid);
            RestfulCodeGenerator.generateResponse(responseTemp, apiInfo, serviceInfo, apiResponseHeaders, apiResponseBody, uuid);
        } catch (Exception e) {
            logger.error("生成sdk请求信息异常:{}", e);
            return SdkConst.WRITE_FILE_ERROR;
        }
        logger.info("Success generate SDK of API:{} {}", apiId, apiInfo.getApiName());

        // 生成client及数据模型代码（仅当单个API的SDK生成时执行）
        if (singleApiFlag) {
            //生成client信息
            List<ApiInfo> apiInfoList = new ArrayList<ApiInfo>();
            apiInfoList.add(apiInfo);
            if (!generateClient(apiInfoList, serviceInfo.getServiceName(), uuid)) {
                return null;
            }
            //生成model信息
            if (!generateModel(String.valueOf(apiInfo.getServiceId()), serviceInfo.getServiceName(), uuid)) {
                return null;
            }
            // 加入配置清单
            try {
                CodeGenerator.generateManifest(uuid);
            } catch (Exception e) {
                logger.error("生成配置信息出现异常，{},e:{}", SdkConst.WRITE_FILE_ERROR, e);
                return null;
            }
            // 压缩jar包
            return RestfulCodeGenerator.pack(apiInfo, serviceInfo, uuid, true);
        } else {
            return SdkConst.SERVICE_API_GENERATE_SUCCESS;
        }

    }

    @Override
    public String generateServiceSdk(String serviceId, String UUID) {
        if (requestTemp == null || responseTemp == null || clientTemp == null) {
            logger.info(SdkConst.TEMPLATE_LOAD_FIALED);
            return SdkConst.TEMPLATE_LOAD_FIALED;
        }
        // 查询服务信息参数
        long limit = 1000;
        long offset = 0;
        if (!RestfulSdkUtils.isValidLong(serviceId) || serviceInfoService.getServiceById(serviceId) == null) {
            return SdkConst.SERVICE_NOT_FOUND;
        }
        // 查询该服务下所有API信息
        List<ApiInfo> apiInfoList = apiInfoService.findAllApiByServiceId(NumberUtils.toLong(serviceId));
        if (apiInfoList.size() == 0) {
            return SdkConst.API_NOT_FOUND;
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(NumberUtils.toLong(serviceId));
        String serviceName = serviceInfo.getServiceName();
        logger.info("Start generating SDK of service {} {}", serviceId, serviceName);

        // 生成所有API的SDK
        Iterator<ApiInfo> iterator = apiInfoList.iterator();
        while (iterator.hasNext()) {
            ApiInfo apiInfo = iterator.next();
            // 跳过ACTION风格API
            if (apiInfo.getType().equals("ACTION")) {
                iterator.remove();
                continue;
            }
            String result = generateApiSdk(String.valueOf(apiInfo.getId()), UUID, SdkConst.MULTIPLE_API_SDK);
            //生成api sdk失败
            if (!SdkConst.SERVICE_API_GENERATE_SUCCESS.equals(result)) {
                return result;
            }
        }
        // 生成client
        if (!generateClient(apiInfoList, serviceName, UUID)) {
            return null;
        }
        //生成model信息
        if (!generateModel(serviceId, serviceName, UUID)) {
            return null;
        }
        // 加入配置清单
        try {
            CodeGenerator.generateManifest(UUID);
        } catch (Exception e) {
            logger.error("生成配置信息出现异常，{},e:{}", SdkConst.WRITE_FILE_ERROR, e);
            return null;
        }

        // 压缩jar包
        String jarName = RestfulCodeGenerator.pack(apiInfoList.get(0), serviceInfo, UUID, false);
        return jarName;
    }

    /**
     * 生成调用API的client信息
     *
     * @param apiInfoList
     * @return
     */
    @Override
    public boolean generateClient(List<ApiInfo> apiInfoList, String serviceName, String uuid) {
        try {
            RestfulCodeGenerator.generateClient(clientTemp, apiInfoList, serviceName, uuid);
        } catch (TemplateException e) {
            logger.error("生成client出现异常，{},e:{}", SdkConst.TEMPLATE_SYNTAX_ERROR, e);
            return false;
        } catch (Exception e) {
            logger.error("生成client出现异常，{},e:{}", SdkConst.WRITE_FILE_ERROR, e);
            return false;
        }
        return true;
    }

    /**
     * 生成API所依赖的model信息
     *
     * @param serviceId
     * @param serviceName
     * @param uuid
     * @return
     */
    @Override
    public boolean generateModel(String serviceId, String serviceName, String uuid) {
        List<CreateApiModelDto> apiModelList = new ArrayList<CreateApiModelDto>();
        List<Long> serviceModelIdList = apiModelService.getApiModelInfoByServiceId(serviceId);
        for (Long modelId : serviceModelIdList) {
            apiModelList.add(apiModelService.getApiModelByModelId(modelId));
        }
        try {
            CodeGenerator.generateDataModel(dataModelTemp, apiModelList, uuid, serviceName);
        } catch (IOException e) {
            logger.error("生成model出现异常，{},e:{}", SdkConst.WRITE_FILE_ERROR, e);
            return false;
        } catch (TemplateException e) {
            logger.error("生成model出现异常，{},e:{}", SdkConst.TEMPLATE_SYNTAX_ERROR, e);
            return false;
        } catch (Exception e) {
            logger.error("生成model出现异常，{},e:{}", SdkConst.WRITE_FILE_ERROR, e);
            return false;
        }
        return true;
    }

    @Override
    public void deleteTemp(String UUID) {
        File tempFile = new File(SdkConst.CODE_DIRECTORY + UUID);
        try {
            FileUtils.deleteDirectory(tempFile);
            logger.info("Temp folder {} already delete", UUID);
        } catch (IOException e) {
            logger.error("Temp folder {} delete failed");
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> supplyDownload(String filePath, String fileName) throws IOException {
//        File jarFile = new File(filePath);
//        try ( InputStream inputStream = new FileInputStream(jarFile)){
//            byte[] body = new byte[inputStream.available()];
//            inputStream.read(body);
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Disposition", "attachment;filename=" + fileName);
//            HttpStatus httpStatus = HttpStatus.OK;
//            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(body, headers, httpStatus);
//            inputStream.close();
//            return response;
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            return null;
//        }
        FileSystemResource file = new FileSystemResource(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(file.getInputStream()));
    }
}
