package org.hango.cloud.gdashboard.api.service.impl.swagger;

import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerApiInfo;
import org.hango.cloud.gdashboard.api.meta.swagger.SwaggerDetailsDto;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.swagger.ISwaggerApiInfoService;
import org.hango.cloud.gdashboard.api.service.swagger.ISwaggerModelService;
import org.hango.cloud.gdashboard.api.service.swagger.ImportSwaggerService;
import org.hango.cloud.gdashboard.api.util.CommonUtil;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImportSwaggerServiceImpl implements ImportSwaggerService {

    private static Logger logger = LoggerFactory.getLogger(ImportSwaggerServiceImpl.class);
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private ISwaggerApiInfoService swaggerApiInfoService;
    @Autowired
    private ISwaggerModelService swaggerModelService;

    @Override
    public List<SwaggerApiInfo> getSwaggerApiInfo(String baseUrl, Map<String, Path> paths) {
        List<SwaggerApiInfo> swaggerApiInfos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(paths)) {
            //请求类型参数
            for (Map.Entry<String, Path> entry : paths.entrySet()) {
                if (StringUtils.isEmpty(baseUrl) || "/".equals(baseUrl)) {
                    swaggerApiInfos.addAll(getApiInfoByURL(entry.getKey(), entry.getValue()));
                } else {
                    swaggerApiInfos.addAll(getApiInfoByURL(baseUrl + entry.getKey(), entry.getValue()));
                }
            }
        }
        return swaggerApiInfos;
    }

    @Override
    public List<SwaggerApiInfo> getApiInfoByURL(String url, Path pathItem) {
        List<SwaggerApiInfo> swaggerApiInfos = new ArrayList<>();
        if (pathItem.getGet() != null) {
            SwaggerApiInfo swaggerApiInfo = getSwaggerApiInfo("GET", pathItem.getGet());
            swaggerApiInfo.setApiInfo(getApiInfo(url, "GET", pathItem.getGet()));
            swaggerApiInfos.add(swaggerApiInfo);
        }
        if (pathItem.getPost() != null) {
            SwaggerApiInfo swaggerApiInfo = getSwaggerApiInfo("POST", pathItem.getPost());
            swaggerApiInfo.setApiInfo(getApiInfo(url, "POST", pathItem.getPost()));
            swaggerApiInfos.add(swaggerApiInfo);
        }
        if (pathItem.getPut() != null) {
            SwaggerApiInfo swaggerApiInfo = getSwaggerApiInfo("PUT", pathItem.getPut());
            swaggerApiInfo.setApiInfo(getApiInfo(url, "PUT", pathItem.getPut()));
            swaggerApiInfos.add(swaggerApiInfo);
        }
        if (pathItem.getDelete() != null) {
            SwaggerApiInfo swaggerApiInfo = getSwaggerApiInfo("DELETE", pathItem.getDelete());
            swaggerApiInfo.setApiInfo(getApiInfo(url, "DELETE", pathItem.getDelete()));
            swaggerApiInfos.add(swaggerApiInfo);
        }
        return swaggerApiInfos;
    }

    @Override
    public SwaggerApiInfo getSwaggerApiInfo(String method, Operation operation) {
        SwaggerApiInfo swaggerApiInfo = new SwaggerApiInfo();
        swaggerApiInfo.setApiQueryString(getQueryString(operation));
        swaggerApiInfo.setApiRequestHeader(getApiRequestHeader(operation));
        swaggerApiInfo.setApiResponseHeader(getApiResponseHeader(operation));
        swaggerApiInfo.setApiResponseBody(getApiResponseBody(operation));
        swaggerApiInfo.setApiStatusCodes(getApiStatusCode(operation));
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            swaggerApiInfo.setApiRequestBody(getApiRequestBody(operation));
        }
        return swaggerApiInfo;
    }

    @Override
    public ApiInfo getApiInfo(String url, String method, Operation operation) {
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setApiPath(url);
        apiInfo.setApiMethod(method);
        apiInfo.setDescription(operation.getSummary());
        if (operation.getOperationId() != null) {
            apiInfo.setApiName(operation.getOperationId());
        } else {
            apiInfo.setApiName(method + url);
        }
//        apiInfo.setApiName();
        //导入API为restful风格接口
        apiInfo.setType("RESTFUL");
        apiInfo.setCreateDate(System.currentTimeMillis());
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setStatus("0");
        //标识出从swagger同步，同步状态为已同步
        apiInfo.setSwaggerSync(1);
        //开发中
        apiInfo.setDocumentStatusId(1);
        apiInfo.setRegex(CommonUtil.getRegexFromApi(url));
        return apiInfo;
    }

    @Override
    public List<ApiHeader> getApiRequestHeader(Operation operation) {
        List<ApiHeader> apiHeaders = new ArrayList<>();
        List<Parameter> parameters = operation.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Parameter parameter : parameters) {
                ApiHeader apiHeader = new ApiHeader();
                if ("header".equalsIgnoreCase(parameter.getIn())) {
                    apiHeader.setParamName(parameter.getName());
                    apiHeader.setDescription(parameter.getDescription());
                    apiHeader.setCreateDate(System.currentTimeMillis());
                    apiHeader.setModifyDate(System.currentTimeMillis());
                    apiHeader.setType(Const.REQUEST_PARAM_TYPE);
                    apiHeaders.add(apiHeader);
                }
            }
        }
        return apiHeaders;
    }

    @Override
    public List<ApiHeader> getApiResponseHeader(Operation operation) {
        List<ApiHeader> apiHeaders = new ArrayList<>();
        Set<Map.Entry<String, Response>> entries = operation.getResponses().entrySet();
        if (!CollectionUtils.isEmpty(entries)) {
            for (Map.Entry<String, Response> entry : entries) {
                Map<String, Property> headerMap = entry.getValue().getHeaders();
                if (!CollectionUtils.isEmpty(headerMap)) {
                    for (Map.Entry<String, Property> headerEntry : headerMap.entrySet()) {
                        ApiHeader apiHeader = new ApiHeader();
                        apiHeader.setParamName(headerEntry.getKey());
                        apiHeader.setDescription(headerEntry.getValue().getDescription());
                        apiHeader.setCreateDate(System.currentTimeMillis());
                        apiHeader.setModifyDate(System.currentTimeMillis());
                        apiHeader.setType(Const.RESPONSE_PARAM_TYPE);
                        apiHeaders.add(apiHeader);
                    }
                }
            }
        }
        return apiHeaders;
    }

    @Override
    public List<ApiBody> getApiRequestBody(Operation operation) {
        List<ApiBody> apiBodies = new ArrayList<>();
        List<Parameter> parameters = operation.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Parameter parameter : parameters) {
                if ("body".equals(parameter.getIn()) || "formData".equalsIgnoreCase(parameter.getIn())) {
                    apiBodies.add(getApiBodyFromParameter(parameter, Const.REQUEST_PARAM_TYPE));
                }
            }
        }
        return apiBodies;
    }

    @Override
    public List<ApiBody> getApiResponseBody(Operation operation) {
        List<ApiBody> apiBodies = new ArrayList<>();
        Map<String, Response> responses = operation.getResponses();
        if (responses != null) {
            for (Map.Entry<String, Response> entry : responses.entrySet()) {
                Response response = entry.getValue();
                if ("200".equalsIgnoreCase(entry.getKey()) && response != null && response.getResponseSchema() != null) {
                    Model responseSchema = response.getResponseSchema();
                    if (CollectionUtils.isEmpty(responseSchema.getProperties())) {
                        apiBodies.add(getApiBodyFromModel(responseSchema, Const.RESPONSE_PARAM_TYPE));
                    } else {
                        Map<String, Property> properties = responseSchema.getProperties();
                        if (!CollectionUtils.isEmpty(properties)) {
                            properties.forEach((key, property) -> {
                                //重新设置APIBody description
                                apiBodies.add(getApiBodyFromProperties(key, property, Const.RESPONSE_PARAM_TYPE));
                            });
                        }
                    }
                }
            }
        }
        return apiBodies;
    }

    @Override
    public List<ApiBody> getQueryString(Operation operation) {
        List<ApiBody> queryStringBodys = new ArrayList<>();
        List<Parameter> parameters = operation.getParameters();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Parameter parameter : parameters) {
                if ("query".equals(parameter.getIn())) {
                    queryStringBodys.add(getApiBodyFromParameter(parameter, Const.QUERYSTRING_PARAM_TYPE));
                }
            }
        }
        return queryStringBodys;
    }

    @Override
    public List<ApiStatusCode> getApiStatusCode(Operation operation) {
        List<ApiStatusCode> apiStatusCodes = new ArrayList<>();
        Map<String, Response> responses = operation.getResponses();
        if (!CollectionUtils.isEmpty(responses)) {
            for (Map.Entry<String, Response> entry : responses.entrySet()) {
                if (NumberUtils.isDigits(entry.getKey())) {
                    ApiStatusCode apiStatusCode = new ApiStatusCode();
                    apiStatusCode.setStatusCode(Long.valueOf(entry.getKey()));
                    apiStatusCode.setDescription(entry.getValue().getDescription());
                    apiStatusCode.setType("api");
                    apiStatusCode.setCreateDate(System.currentTimeMillis());
                    apiStatusCode.setModifyDate(System.currentTimeMillis());
                    apiStatusCodes.add(apiStatusCode);
                }
            }
        }
        return apiStatusCodes;
    }

    @Override
    public List<CreateApiModelDto> getApiModels(Swagger swagger) {
        List<CreateApiModelDto> createApiModelDtos = new ArrayList<>();
        Map<String, Model> definitions = swagger.getDefinitions();
        if (!CollectionUtils.isEmpty(definitions)) {
            for (Map.Entry<String, Model> modelEntry : definitions.entrySet()) {
                CreateApiModelDto apiModelDto = new CreateApiModelDto();
                apiModelDto.setModelName(modelEntry.getKey());
                apiModelDto.setDescription(modelEntry.getValue().getDescription());
                //ModelImpl形式的model
                if (modelEntry.getValue() instanceof ModelImpl) {
                    ModelImpl model = (ModelImpl) modelEntry.getValue();
                    List<ApiParamDto> apiParamDtos = getApiParamDtosFromModel(model);
                    apiModelDto.setParams(apiParamDtos);
                    createApiModelDtos.add(apiModelDto);
                }
                //ComposedModel形式的model
                if (modelEntry.getValue() instanceof ComposedModel) {
                    ComposedModel model = (ComposedModel) modelEntry.getValue();
                    List<ApiParamDto> apiParamDtos = getApiParamDtosFromModel(model);
                    apiModelDto.setParams(apiParamDtos);
                    createApiModelDtos.add(apiModelDto);
                }
                //ArrayModel形式的Model
                if (modelEntry.getValue() instanceof ArrayModel) {
                    ArrayModel model = (ArrayModel) modelEntry.getValue();
                    List<ApiParamDto> apiParamDtos = getApiParamDtosFromModel(model);
                    if (!CollectionUtils.isEmpty(apiParamDtos)) {
                        apiParamDtos.forEach(apiParamDto -> {
                            apiParamDto.setParamName(modelEntry.getKey());
                        });
                    }
                    apiModelDto.setParams(apiParamDtos);
                    createApiModelDtos.add(apiModelDto);
                }
            }
        }
        return createApiModelDtos;
    }


    public long generateExactTypeId(String type, long modelId) {
        //基本类型创建id，复杂类型需要在写数据库时候，创建id
        ApiParamType apiParamType = apiParamTypeService.listApiParamTypeByModelId(type, modelId);
        if (apiParamType != null) {
            return apiParamType.getId();
        }
        return 0;
    }

    public String convertBodyType(String type, String format, String ref) {
        String convertType = ref;
        if (StringUtils.isNotBlank(type)) {
            switch (type) {
                case "integer":
                    format = StringUtils.isNotBlank(format) ? format : "Int";
                    convertType = "int64".equalsIgnoreCase(format) ? "Long" : "Int";
                    break;
                case "string":
                    convertType = "String";
                    break;
                case "array":
                    convertType = "Array";
                    break;
                case "boolean":
                    convertType = "Boolean";
                    break;
                case "number":
                    convertType = "Double";
                    break;
                default:
                    convertType = type;
            }
        }
        return convertType;
    }


    @Override
    public long generateExactByService(String type, long serviceId) {
        return apiParamTypeService.generateExactByService(type, serviceId);
    }

    @Override
    public ApiBody getApiBodyFromParameter(Parameter parameter, String type) {
        ApiBody apiBody = new ApiBody();
        apiBody.setParamName(parameter.getName());
        apiBody.setType(type);
        apiBody.setDescription(parameter.getDescription());
        String convertType = null;
        String arrayType = null;
        if (Const.QUERYSTRING_PARAM_TYPE.equalsIgnoreCase(type)) {
            convertType = convertBodyType(((QueryParameter) parameter).getType(), ((QueryParameter) parameter).getFormat(), null);
            //数组类型的具体类型
            if ("Array".equalsIgnoreCase(convertType)) {
                arrayType = convertBodyType(((QueryParameter) parameter).getItems().getType(), ((QueryParameter) parameter).getItems().getFormat(), null);
                if ("ref".equalsIgnoreCase(convertType)) {
                    RefProperty refProperty = (RefProperty) ((QueryParameter) parameter).getItems();
                    arrayType = refProperty.getSimpleRef();
                }
            }
        } else if (Const.REQUEST_PARAM_TYPE.equalsIgnoreCase(type)) {
            if ("body".equalsIgnoreCase(parameter.getIn())) {
                Model schema = ((BodyParameter) parameter).getSchema();
                convertType = getConvertTypeFromModelSchema(schema);
                apiBody.setParamName(Const.BLANK_CONST);
                //数组类型的具体类型
                if ("Array".equalsIgnoreCase(convertType)) {
                    apiBody.setParamName(Const.BLANK_ARRAY_CONST);
                    arrayType = getArrayTypeNameFromSchema((ArrayModel) schema);
                }
            } else {
                convertType = convertBodyType(((FormParameter) parameter).getType(), ((FormParameter) parameter).getFormat(), null);
            }
        }
        apiBody.setParamType(convertType);
        apiBody.setRequired(parameter.getRequired() ? "1" : "0");
        apiBody.setArrayDataTypeName(arrayType);
        apiBody.setCreateDate(System.currentTimeMillis());
        apiBody.setModifyDate(System.currentTimeMillis());
        return apiBody;
    }

    @Override
    public Map<String, Object> getSwaggerDetails(Swagger swagger, long serviceId, String serviceName) {
        Map<String, Path> paths = swagger.getPaths();
        List<CreateApiModelDto> apiModelDtos = getApiModels(swagger);
        List<SwaggerApiInfo> swaggerApiInfos = getSwaggerApiInfo(swagger.getBasePath(), paths);
        List<SwaggerDetailsDto> conflictModel = swaggerModelService.getConflixApiModel(apiModelDtos, serviceId, serviceName);
        List<SwaggerDetailsDto> conflictApi = swaggerApiInfoService.getConflixApi(swaggerApiInfos, serviceId, serviceName);
        List<SwaggerDetailsDto> coverModel = swaggerModelService.getOverrideApiModel(apiModelDtos, serviceId);
        List<SwaggerDetailsDto> coverApi = swaggerApiInfoService.getOverrideApi(swaggerApiInfos, serviceId);
        List<SwaggerDetailsDto> newModel = swaggerModelService.getNewApiModel(apiModelDtos, serviceId);
        List<SwaggerDetailsDto> newApi = swaggerApiInfoService.getNewApi(swaggerApiInfos, serviceId);
        List<SwaggerDetailsDto> swaggerDetailsDtos = new ArrayList<>();
        //model
        swaggerDetailsDtos.addAll(newModel);
        swaggerDetailsDtos.addAll(coverModel);
        swaggerDetailsDtos.addAll(conflictModel);
        //api
        swaggerDetailsDtos.addAll(newApi);
        swaggerDetailsDtos.addAll(coverApi);
        swaggerDetailsDtos.addAll(conflictApi);
        String swaggerDetailKey = null;
        Map<String, Object> result = new HashMap<>();
        if (!CollectionUtils.isEmpty(swaggerDetailsDtos)) {
            swaggerDetailKey = "gatewayPortal_csv_swaggerDetail_" + UUID.randomUUID().toString();
            result.put(Const.SWAGGER_DETAILS, swaggerDetailsDtos);
        }
        result.put(Const.SWAGGER_DETAILS_KEY, swaggerDetailKey);
        result.put("ConflictApiTotal", conflictApi.size());
        result.put("ConflictModelTotal", conflictModel.size());
        result.put("CoverApiTotal", coverApi.size());
        result.put("CoverModelTotal", coverModel.size());
        result.put("NewApiTotal", newApi.size());
        result.put("NewModelTotal", newModel.size());
        return result;
    }

    @Override
    public boolean insertSwagger(Swagger swagger, long serviceId) {
        try {
            Map<String, Path> paths = swagger.getPaths();
            List<CreateApiModelDto> createApiModelDtos = getApiModels(swagger);
            swaggerModelService.addApiModel(createApiModelDtos, serviceId);
            List<SwaggerApiInfo> swaggerApiInfoList = getSwaggerApiInfo(swagger.getBasePath(), paths);
            swaggerApiInfoService.addApiInfos(swaggerApiInfoList, serviceId);
        } catch (Exception e) {
            logger.error("导入swagger描述文件异常,e:{}", e);
            return false;
        }
        return true;
    }

    @Override
    public ApiErrorCode checkSwaggerFile(MultipartFile file) {
        if (file.isEmpty() || file == null) {
            return CommonApiErrorCode.FileIsEmpty;
        }
        String fileName = file.getOriginalFilename();
        if (!fileName.contains(".")) {
            return CommonApiErrorCode.IllegalFileFormat;
        }
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        //文件格式校验
        if (!".yaml".equals(fileType) && !".json".equals(fileType)) {
            return CommonApiErrorCode.IllegalFileFormat;
        }
        return CommonApiErrorCode.Success;
    }

    public List<ApiParamDto> getApiParamDtosFromModel(Model model) {
        List<ApiParamDto> apiParamDtos = new ArrayList<>();
        if (model instanceof ModelImpl && !CollectionUtils.isEmpty(model.getProperties())) {
            model.getProperties().forEach(
                    (key, property) -> {
                        ApiParamDto apiParamDto = new ApiParamDto();
                        apiParamDto.setParamName(key);
                        String convertType = convertBodyType(property.getType(), property.getFormat(), null);
                        if ("ref".equalsIgnoreCase(convertType)) {
                            convertType = ((RefProperty) property).getSimpleRef();
                        }
                        apiParamDto.setParamTypeName(convertType);
                        apiParamDto.setParamTypeId(generateExactTypeId(convertType, 0));
                        apiParamDto.setDescription(property.getDescription());
                        apiParamDto.setRequired(property.getRequired() ? "1" : "0");
                        //array类型需要获取array的数据类型
                        if ("Array".equalsIgnoreCase(convertType)) {
                            ArrayProperty arrayProperty = (ArrayProperty) property;
                            String type = convertBodyType(arrayProperty.getItems().getType(), arrayProperty.getItems().getFormat(), null);
                            if ("ref".equalsIgnoreCase(type)) {
                                RefProperty refProperty = (RefProperty) arrayProperty.getItems();
                                type = refProperty.getSimpleRef();
                            }
                            apiParamDto.setArrayDataTypeName(type);
                            apiParamDto.setArrayDataTypeId(generateExactTypeId(type, 0));
                        }
                        apiParamDtos.add(apiParamDto);
                    }
            );
        }
        if (model instanceof ComposedModel && ((ComposedModel) model).getAllOf() != null) {
            List<Model> allOf = ((ComposedModel) model).getAllOf();
            allOf.forEach(model1 -> {
                ApiParamDto apiParamDto = new ApiParamDto();
                String convertType = getConvertTypeFromModelSchema(model1);
                if (StringUtils.isEmpty(convertType)) {
                    return;
                }
                String arrayType = null;
                //数组类型的具体类型
                if ("Array".equalsIgnoreCase(convertType)) {
                    arrayType = getArrayTypeNameFromSchema((ArrayModel) model1);
                    apiParamDto.setArrayDataTypeName(arrayType);
                    apiParamDto.setArrayDataTypeId(generateExactTypeId(arrayType, 0));
                }
                //数组类型有可能没有convertType，引用类型convertType为name
                apiParamDto.setParamName(convertType);
                apiParamDto.setParamTypeName(convertType);
                apiParamDto.setParamTypeId(generateExactTypeId(convertType, 0));
                apiParamDto.setDescription(model1.getDescription());
                apiParamDto.setRequired("1");
                apiParamDtos.add(apiParamDto);
            });
        }
        if (model instanceof ArrayModel) {
            ApiParamDto apiParamDto = new ApiParamDto();
            String convertType = "Array";
            apiParamDto.setParamTypeName(convertType);
            apiParamDto.setParamTypeId(generateExactTypeId(convertType, 0));
            String arrayType = getArrayTypeNameFromSchema((ArrayModel) model);
            apiParamDto.setArrayDataTypeName(arrayType);
            apiParamDto.setArrayDataTypeId(generateExactTypeId(arrayType, 0));
            apiParamDto.setDescription(model.getDescription());
            apiParamDto.setRequired("1");
            apiParamDtos.add(apiParamDto);
        }
        return apiParamDtos;
    }

    public ApiBody getApiBodyFromModel(Model model, String type) {
        ApiBody apiBody = new ApiBody();
        String convertType = getConvertTypeFromModelSchema(model);
        String arrayType = null;
        //数组类型的具体类型
        if ("Array".equalsIgnoreCase(convertType)) {
            arrayType = getArrayTypeNameFromSchema((ArrayModel) model);
            apiBody.setParamName(Const.BLANK_ARRAY_CONST);
        } else {
            apiBody.setParamName(Const.BLANK_CONST);
        }
        apiBody.setDescription(model.getDescription());
        apiBody.setType(type);
        apiBody.setParamType(convertType);
        apiBody.setArrayDataTypeName(arrayType);
        apiBody.setCreateDate(System.currentTimeMillis());
        apiBody.setModifyDate(System.currentTimeMillis());
        boolean requeried = true;
        if (model instanceof ArrayModel) {
            requeried = ((ArrayModel) model).getItems().getRequired();
        }
        apiBody.setRequired(requeried ? "1" : "0");
        return apiBody;
    }

    public ApiBody getApiBodyFromProperties(String propertyKey, Property property, String type) {
        ApiBody apiBody = new ApiBody();
        apiBody.setParamName(propertyKey);
        apiBody.setDescription(property.getDescription());
        String convertType = getConvertTypeFromProperty(property);
        String arrayType = null;
        if ("Array".equalsIgnoreCase(convertType)) {
            arrayType = getArrayTypeNameFromProperty((ArrayProperty) property);
        }
        apiBody.setParamType(convertType);
        apiBody.setArrayDataTypeName(arrayType);
        apiBody.setType(type);
        apiBody.setRequired(property.getRequired() ? "1" : "0");
        return apiBody;
    }

    public String getConvertTypeFromProperty(Property property) {
        String convertType = null;
        if (property instanceof RefProperty) {
            convertType = ((RefProperty) property).getSimpleRef();
        } else {
            convertType = convertBodyType(property.getType(), property.getFormat(), null);
        }
        return convertType;
    }

    public String getConvertTypeFromModelSchema(Model schema) {
        String convertType = null;
        if (schema instanceof RefModel) {
            convertType = ((RefModel) schema).getSimpleRef();
        } else if (schema instanceof ModelImpl) {
            convertType = convertBodyType(((ModelImpl) schema).getType(), ((ModelImpl) schema).getFormat(), null);
        } else {
            convertType = convertBodyType(((ArrayModel) schema).getType(), null, null);
        }
        return convertType;
    }

    public String getArrayTypeNameFromSchema(ArrayModel arrayModel) {
        String arrayType = null;
        arrayType = convertBodyType(arrayModel.getItems().getType(), arrayModel.getItems().getFormat(), null);
        if ("ref".equalsIgnoreCase(arrayType)) {
            RefProperty refProperty = (RefProperty) arrayModel.getItems();
            arrayType = refProperty.getSimpleRef();
        }
        return arrayType;
    }

    public String getArrayTypeNameFromProperty(ArrayProperty arrayProperty) {
        String arrayType = null;
        arrayType = convertBodyType(arrayProperty.getItems().getType(), arrayProperty.getItems().getFormat(), null);
        if ("ref".equalsIgnoreCase(arrayType)) {
            RefProperty refProperty = (RefProperty) arrayProperty.getItems();
            arrayType = refProperty.getSimpleRef();
        }
        return arrayType;
    }
}
