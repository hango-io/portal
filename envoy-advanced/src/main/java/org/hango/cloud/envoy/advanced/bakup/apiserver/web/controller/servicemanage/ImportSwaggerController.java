package org.hango.cloud.envoy.advanced.bakup.apiserver.web.controller.servicemanage;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.holder.RequestContextHolder;
import org.hango.cloud.common.infra.cache.ICacheService;
import org.hango.cloud.common.infra.operationaudit.meta.ResourceDataDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.advanced.bakup.apiserver.util.Const;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.hango.cloud.gdashboard.api.service.swagger.ImportSwaggerService;
import org.hango.cloud.gdashboard.api.util.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 导入swagger json文件，生成gportal支持的相关API信息
 *
 * @author hanjiahao
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
public class ImportSwaggerController extends AbstractController {

    public static final String[] HEADERS = {"Type", "Name", "Status", "Path", "Method", "Message"};
    private static Logger logger = LoggerFactory.getLogger(ImportSwaggerController.class);
    @Autowired
    private ImportSwaggerService importSwaggerService;
    @Autowired
    private ICacheService cacheService;
    @Autowired
    private IServiceProxyService serviceProxyService;

    private static String convertToJson(MultipartFile file) throws IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(file.getInputStream(), Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    @RequestMapping(params = {"Action=GetSwaggerDetailsByFile"}, method = RequestMethod.POST)
    public String swaggerDetailsByFile(@RequestParam("file") MultipartFile file, @RequestHeader("Service-Id") Long serviceId) {
        ServiceProxyDto serviceInDb = serviceProxyService.get(serviceId);
        if (serviceInDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }

        ApiErrorCode errorCode = importSwaggerService.checkSwaggerFile(file);
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        Swagger swagger = null;
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode;
            if (".yaml".equals(fileType)) {
                jsonNode = mapper.readTree(convertToJson(file));
            } else {
                jsonNode = mapper.readTree(file.getInputStream());
            }
            swagger = new SwaggerParser().read(jsonNode);
        } catch (IOException e) {
            logger.error("解析swagger文件异常,具体异常信息为：e:{}", e);
            return apiReturn(CommonApiErrorCode.ParseSwaggerFailure);
        }
        Map<String, Object> swaggerDetails = importSwaggerService.getSwaggerDetails(swagger, serviceId, serviceInDb.getName());
        if (swaggerDetails.containsKey(Const.SWAGGER_DETAILS)) {
            cacheService.setValue((String) swaggerDetails.get(Const.SWAGGER_DETAILS_KEY), JSONObject.toJSONString(swaggerDetails.get(Const.SWAGGER_DETAILS)), 60 * 60L);
        }
        return apiReturn(200, null, null, swaggerDetails);
    }

    @RequestMapping(params = {"Action=GetSwaggerDetailsByLocation"}, method = RequestMethod.POST)
    public String swaggerDetailsByLocation(@RequestBody String body, @RequestHeader("Service-Id") Long serviceId) {
        ServiceProxyDto serviceInDb = serviceProxyService.get(serviceId);
        if (serviceInDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        String location = null;
        try {
            location = JSONObject.parseObject(body).getString("Location").trim();
        } catch (Exception e) {
            return apiReturn(CommonErrorCode.invalidParameterValue(location, "Location"));
        }
        if (StringUtils.isBlank(location)) {
            return apiReturn(CommonErrorCode.MissingParameter("Location"));
        }
        Swagger swagger = new SwaggerParser().read(location);
        if (swagger != null) {
            Map<String, Object> swaggerDetails = importSwaggerService.getSwaggerDetails(swagger, serviceId, serviceInDb.getName());
            if (swaggerDetails.containsKey(Const.SWAGGER_DETAILS)) {
                cacheService.setValue((String) swaggerDetails.get(Const.SWAGGER_DETAILS_KEY), JSONObject.toJSONString(swaggerDetails.get(Const.SWAGGER_DETAILS)), 60 * 60L);
            }
            return apiReturn(200, null, null, swaggerDetails);
        } else {
            return apiReturn(CommonApiErrorCode.ParseSwaggerFailure);
        }
    }

    @RequestMapping(params = {"Action=ConfirmImportByFile"}, method = RequestMethod.POST)
    public String confirmSwaggerByFile(@RequestParam("file") MultipartFile file, @RequestHeader("Service-Id") Long serviceId) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, serviceId, "导入swagger");

        ServiceProxyDto serviceInDb = serviceProxyService.get(serviceId);
        if (serviceInDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }

        ApiErrorCode errorCode = importSwaggerService.checkSwaggerFile(file);
        if (!CommonApiErrorCode.Success.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        Swagger swagger = null;
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode;
            if (".yaml".equals(fileType)) {
                jsonNode = mapper.readTree(convertToJson(file));
            } else {
                jsonNode = mapper.readTree(file.getInputStream());
            }
            swagger = new SwaggerParser().read(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
            return apiReturn(CommonApiErrorCode.ParseSwaggerFailure);
        }
        boolean flag = importSwaggerService.insertSwagger(swagger, serviceId);
        return flag ? apiReturn(CommonErrorCode.SUCCESS) : apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(params = {"Action=ConfirmImportByLocation"}, method = RequestMethod.POST)
    public String confirmSwaggerByLocation(@RequestBody String body, @RequestHeader("Service-Id") Long serviceId) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_API, serviceId, "导入swagger");

        ServiceProxyDto serviceInDb = serviceProxyService.get(serviceId);
        if (serviceInDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        String location = null;
        try {
            location = JSONObject.parseObject(body).getString("Location").trim();
        } catch (Exception e) {
            return apiReturn(CommonErrorCode.invalidParameterValue(location, "Location"));
        }
        if (StringUtils.isBlank(location)) {
            return apiReturn(CommonErrorCode.MissingParameter("Location"));
        }
        Swagger swagger = new SwaggerParser().read(location);
        if (swagger != null) {
            boolean flag = importSwaggerService.insertSwagger(swagger, serviceId);
            return flag ? apiReturn(CommonErrorCode.SUCCESS) : apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        } else {
            return apiReturn(CommonApiErrorCode.ParseSwaggerFailure);
        }
    }

    @RequestMapping(params = {"Action=DownloadSwaggerDetails"}, method = RequestMethod.GET)
    public String downloadSwaggerDetails(@RequestParam("SwaggerDetailKey") String fileKey) {
        HttpServletResponse response = RequestContextHolder.getResponse();
        List<SwaggerDetailsDto> swaggerDetails = getSwaggerDetails(fileKey);
        if (CollectionUtils.isEmpty(swaggerDetails)) {
            return apiReturn(CommonApiErrorCode.FileExpire);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String swaggerFileName = "swaggerAPIList_" + df.format(new Date());
        CsvUtils.writeBean(response, HEADERS, swaggerDetails, swaggerFileName);
        return apiReturn(CommonErrorCode.SUCCESS, null);
    }

    private List<SwaggerDetailsDto> getSwaggerDetails(String fileKey){
        String value = cacheService.getValue(fileKey);
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        return JSONObject.parseArray(value, SwaggerDetailsDto.class);
    }

}
