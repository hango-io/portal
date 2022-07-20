package org.hango.cloud.gdashboard.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.models.ArrayModel;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.dao.ApiModelParamDao;
import org.hango.cloud.gdashboard.api.dao.ApiParamObjectDao;
import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiModelParam;
import org.hango.cloud.gdashboard.api.meta.ApiParamObject;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiConvertToJsonService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/4/19 19:52.
 */
@Service
public class ApiConvertToJsonServiceImpl implements IApiConvertToJsonService {

    public static final String _BLANK = "_blank";
    public static final String MODEL = "MODEL";
    protected static final Logger logger = LoggerFactory.getLogger(ApiConvertToJsonServiceImpl.class);
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private ApiParamObjectDao apiParamObjectDao;
    @Autowired
    private ApiModelParamDao apiModelParamDao;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IApiHeaderService apiHeaderService;
    private LoadingCache<String, Optional<ApiParamType>> paramTypeCache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Optional<ApiParamType>>() {
                @Override
                public Optional<ApiParamType> load(String paramTypeName) throws Exception {
                    if (StringUtils.isBlank(paramTypeName)) {
                        return Optional.fromNullable(null);
                    }
                    return Optional.fromNullable(apiParamTypeService.listApiParamType(paramTypeName));
                }
            });

    public ApiParamType getApiParamTypeFromCache(String paramTypeName) {
        if (StringUtils.isBlank(paramTypeName)) {
            return null;
        }
        return paramTypeCache.getUnchecked(paramTypeName).orNull();
    }

    @Override
    public Map generateJsonForApi(long apiId, String type) {

//        List<ApiBody> apiBodyList = apiInfoService.getBody(String.valueOf(apiId), "REQUEST");
        List<ApiBody> apiBodyList = apiBodyService.getBody(apiId, type);
        Map<String, Object> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(apiBodyList)) {
            for (ApiBody apiBody : apiBodyList) {
                map.put(apiBody.getParamName(), convertToJson(apiBody));
            }
        }
        return map;
    }

    public Map generateJsonForApiWithDesc(long apiId, String type) {

        List<ApiBody> apiBodyList = apiBodyService.getBody(apiId, type);

        Map<String, Object> map = new LinkedHashMap<>();

        for (ApiBody apiBody : apiBodyList) {

            //参数字段名为 _blank 时进行特别处理
            if (_BLANK.equals(apiBody.getParamName())) {
                ApiParamType apiParamType = apiParamTypeService.listApiParamType(apiBody.getParamTypeId());
                //如果 _blank对应的value为一个Model时
                if (MODEL.equals(apiParamType.getLocation())) {
                    //_blank不再作为嵌套层的key,_blank中的内容直接作为参数
                    map.putAll(convertModelToMap(apiParamType.getModelId()));
                    continue;
                } else {
                    map.put(Const.BLANK_CONST, convertToJson(apiBody));
                    map.put(Const.BLANK_CONST + ".gwDesc", apiBody.getDescription());
                    if ("1".equals(apiBody.getRequired())) {
                        map.put(Const.BLANK_CONST + ".isRequired", true);
                    } else {
                        map.put(Const.BLANK_CONST + ".isRequired", false);
                    }
                    continue;
                }
            }

            map.put(apiBody.getParamName(), convertToJson(apiBody));
            map.put(apiBody.getParamName() + ".gwDesc", apiBody.getDescription());
            if ("1".equals(apiBody.getRequired())) {
                map.put(apiBody.getParamName() + ".isRequired", true);
            } else {
                map.put(apiBody.getParamName() + ".isRequired", false);
            }

        }
        return map;
    }

    /**
     * 注意swagger中response body是作为一个ObjectProperty输出的，
     * 而request body是按照一个参数对应一个BodyParameter来输出的
     *
     * @param apiId
     * @param swagger
     * @return
     */
    @Override
    public Map<String, Object> convertResponseBodyToSwagger(long apiId, Swagger swagger) {
        Map<String, Object> result = new HashMap<>();
        //得到body的map形式
        Map<String, Object> map = generateJsonForApiWithDesc(apiId, "RESPONSE");
        Map<String, Property> propertyMap = generatePropertiesFromMap(apiId, swagger, map, Const.RESPONSE_PARAM_TYPE);
        if (!map.keySet().contains(Const.BLANK_ARRAY_CONST) && !map.keySet().contains(Const.BLANK_CONST)) {
            ObjectProperty objectProperty = new ObjectProperty();
            objectProperty.setProperties(propertyMap);
            result.put("properties", objectProperty);
            return result;
        }
        if (map.keySet().contains(Const.BLANK_CONST)) {
            ModelImpl modelImpl = new ModelImpl();
            Property property = propertyMap.get(Const.BLANK_CONST);
            modelImpl.setType(property.getType());
            modelImpl.setFormat(property.getFormat());
            modelImpl.setDescription(property.getDescription());
            if (property.getRequired()) {
                List<String> requiredList = new ArrayList<>();
                requiredList.add(property.getName());
                modelImpl.setRequired(requiredList);
            }
            result.put(Const.BLANK_CONST, modelImpl);
            return result;
        }
        //array model 返回
        ArrayModel arrayModel = new ArrayModel();
        ArrayProperty blank_array = (ArrayProperty) propertyMap.get(Const.BLANK_ARRAY_CONST);
        arrayModel.setItems(blank_array.getItems());
        result.put(Const.BLANK_ARRAY_CONST, arrayModel);
        return result;

    }

    private Map<String, Property> generatePropertiesFromMap(long apiId, Swagger swagger, Map<String, Object> map, String prefix) {
        Map<String, Property> propertyMap = new LinkedHashMap<>();
        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));
        String modelNamePrefix;
        //查询API,为了生成modelName时不会重复
        modelNamePrefix = apiInfo.getApiName() + "." + apiInfo.getApiMethod() + "." + prefix + ".";
        for (Entry<String, Object> entry : map.entrySet()) {

            if (entry.getKey().equals(Const.MODEL_NAME_PREFIX)) {
                continue;
            }

            if (entry.getKey().endsWith("isRequired")) {
                continue;
            }
            if (entry.getKey().endsWith(".gwDesc")) {
                continue;
            }

            String des = map.keySet().contains(entry.getKey() + ".gwDesc") ? (String) map.get(entry.getKey() + ".gwDesc") : null;
            boolean required = map.keySet().contains(entry.getKey() + ".isRequired") ? (Boolean) map.get(entry.getKey() + ".isRequired") : true;

            if (entry.getValue() instanceof String) {
                StringProperty stringProperty = new StringProperty();
                stringProperty.setName(entry.getKey());
                stringProperty.setDescription(des);
                stringProperty.setRequired((Boolean) map.get(entry.getKey() + ".isRequired"));
                propertyMap.put(entry.getKey(), stringProperty);
            } else if (entry.getValue() instanceof Boolean) {
                BooleanProperty booleanProperty = new BooleanProperty();
                booleanProperty.setName(entry.getKey());
                booleanProperty.setDescription(des);
                booleanProperty.setRequired(required);
                propertyMap.put(entry.getKey(), booleanProperty);
            } else if (entry.getValue() instanceof Integer) {
                IntegerProperty integerProperty = new IntegerProperty();
                integerProperty.setName(entry.getKey());
                integerProperty.setDescription(des);
                integerProperty.setRequired(required);
                propertyMap.put(entry.getKey(), integerProperty);
            } else if (entry.getValue() instanceof Long) {
                LongProperty longProperty = new LongProperty();
                longProperty.setName(entry.getKey());
                longProperty.setDescription(des);
                longProperty.setRequired(required);
                propertyMap.put(entry.getKey(), longProperty);// model.setProperties(propertyMap);
            } else if (entry.getValue() instanceof Double) {
                DoubleProperty doubleProperty = new DoubleProperty();
                doubleProperty.setName(entry.getKey());
                doubleProperty.setDescription(des);
                doubleProperty.setRequired(required);
                propertyMap.put(entry.getKey(), doubleProperty);
            } else if (entry.getValue() instanceof List) {
                ArrayProperty arrayProperty = convertListToSwaggerjson((List) entry.getValue(), modelNamePrefix + entry.getKey(), swagger);
//                ArrayProperty arrayProperty = convertListToSwaggerjson((List) entry.getValue(), entry.getValue(), swagger);
                arrayProperty.setDescription(des);
                arrayProperty.setRequired(required);
                propertyMap.put(entry.getKey(), arrayProperty);
            } else if (entry.getValue() instanceof Map) {
                //参数是Object类型
                if (((Map) entry.getValue()).size() == 0) {
                    //未包含任何参数
                    ObjectProperty paramObjectProperty = new ObjectProperty();
                    paramObjectProperty.setName(entry.getKey());
                    paramObjectProperty.setDescription(des);
                    paramObjectProperty.setRequired(required);
                    propertyMap.put(entry.getKey(), paramObjectProperty);
                } else {
                    //注意如果Map中不包含任何参数，则RefProperty在前端显示的时候显示不出来
                    Map resultMap = (Map) entry.getValue();
                    String modelName = modelNamePrefix + entry.getKey();
                    if (resultMap.get(Const.MODEL_NAME_PREFIX) != null && resultMap.get(Const.MODEL_NAME_PREFIX) instanceof String) {
                        modelName = (String) resultMap.get(Const.MODEL_NAME_PREFIX);
                    }
                    convertMapToSwaggerjson(resultMap, modelName, swagger);
                    RefProperty refProperty = new RefProperty(modelName);
                    refProperty.setDescription(des);
                    refProperty.setRequired(required);
                    propertyMap.put(entry.getKey(), refProperty);
                }

            }
        }
        return propertyMap;
    }

    @Override
    public void convertStatusCodeToSwaggerResponse(Operation operation, long apiId) {
        List<ApiStatusCode> apiStatusCodeList = apiBodyService.listStatusCode(apiId);
        if (apiStatusCodeList.size() == 0) {
            return;
        }
        for (ApiStatusCode apiStatusCode : apiStatusCodeList) {
            // Response response = new Response();
            // String desc = "";
            // desc += "Code:";
            // desc += (String) apiStatusCode.getErrorCode();
            // desc += "; Message:";
            // desc += (String) apiStatusCode.getMessage();
            // desc += "; Description:";
            // desc += (String) apiStatusCode.getDescription();
            // response.setDescription(desc);
            // 会覆盖的
            Response response = new Response();
            String desc;

            if (operation.getResponses() == null || operation.getResponses().get(String.valueOf(apiStatusCode.getStatusCode())) == null) {
                desc = "| 错误代码（Code） | 错误提示（Message） | 说明 |\n";
            } else {
                desc = operation.getResponses().get(String.valueOf(apiStatusCode.getStatusCode())).getDescription();
            }
            desc += " | ";
            desc += (String) apiStatusCode.getErrorCode();
            desc += " | ";
            desc += (String) apiStatusCode.getMessage();
            desc += " | ";
            desc += (String) apiStatusCode.getDescription();
            desc += " | ";
            desc += "\n";
            response.setDescription(desc);
            operation.response((int) apiStatusCode.getStatusCode(), response);
        }

    }

    public void convertNormalStatusCodeToSwaggerResponse(Operation operation, long apiId) {
        List<ApiStatusCode> apiStatusCodeList = apiBodyService.listStatusCode(apiId);
        if (apiStatusCodeList.size() == 0) {
            return;
        }

        for (ApiStatusCode apiStatusCode : apiStatusCodeList) {
            if (apiStatusCode.getStatusCode() == 200) continue;
            Response response = new Response();
            String desc = apiStatusCode.getDescription();
            response.setDescription(desc);
            operation.response((int) apiStatusCode.getStatusCode(), response);
        }
    }


    @Override
    public Operation generateOperstaionForSwagger(long apiId, Swagger swagger) {

        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));
        List<ApiHeader> reqHeaders = apiHeaderService.getHeader(apiId, "REQUEST");
        List<ApiHeader> rspHeaders = apiHeaderService.getHeader(apiId, "RESPONSE");
        List<ApiBody> queryString = apiBodyService.getBody(apiId, Const.QUERYSTRING_PARAM_TYPE);

        Operation operation = new Operation()
                .produces("application/json;charset=utf-8")
                .summary(apiInfo.getApiName())
                .tag(apiInfo.getType())
                .description(apiInfo.getDescription());

        //query string

        boolean flag;
        Property property;
        for (ApiBody query : queryString) {
            flag = false;
            property = new StringProperty();
            if ("1".equals(query.getRequired())) {
                flag = true;
            }
            String paramType = query.getParamType();

            if (paramType != null && paramType.equals("Boolean")) {
                property = new BooleanProperty();
            }
            if (paramType != null && paramType.equals("Int")) {
                property = new IntegerProperty();
            }
            if (paramType != null && paramType.equals("Long")) {
                property = new LongProperty();
            }
            if (paramType != null && paramType.equals("Double")) {
                property = new DoubleProperty();
            }
            if (paramType != null && paramType.equals("Number")) {
                property = new LongProperty();
            }
            if (paramType != null && paramType.equals("Array")) {
                property = new ArrayProperty();
            }

            operation.parameter(new QueryParameter()
                    .name(query.getParamName())
                    .description(query.getDescription())
                    .required(flag)
                    .property(property)
            );
        }

        // 请求参数header
        for (ApiHeader apiHeader : reqHeaders) {
            operation.parameter(new HeaderParameter()
                    .name(apiHeader.getParamName())
                    .description(apiHeader.getDescription())
                    .required(true)
                    .property(new StringProperty())
            );
        }

        // 请求参数body
        BodyParameter requestBodyParameter = convertRequestBodyToSwagger(apiInfo.getId(), swagger);

        if (requestBodyParameter.getRequired() && !("GET".equals(apiInfo.getApiMethod()))) {
            operation.parameter(requestBodyParameter);
        }

        // 响应response
        final Response successResponse = new Response();

        // 设置响应的headers
        Map<String, Property> rspSwaggerHeaders = new HashMap<>();
        for (ApiHeader apiHeader : rspHeaders) {
            rspSwaggerHeaders.put(apiHeader.getParamName(), new StringProperty().description(apiHeader.getDescription()));
        }
        successResponse.setHeaders(rspSwaggerHeaders);

        // 响应参数body
        Map<String, Object> map = convertResponseBodyToSwagger(apiInfo.getId(), swagger);
        if (map.get("properties") != null) {
            ObjectProperty objectProperty = (ObjectProperty) map.get("properties");

            if (!objectProperty.getProperties().isEmpty()) {
                successResponse.setDescription("响应类型200,新建200响应状态码会覆盖响应信息\n");
                successResponse.setSchema(objectProperty);
                operation.response(200, successResponse);
            }
        } else if (map.get(Const.BLANK_ARRAY_CONST) != null) {
            ArrayModel arrayModel = (ArrayModel) map.get("_blank_array");
            successResponse.setDescription("响应类型200,新建200响应状态码会覆盖响应信息\n");
            successResponse.setResponseSchema(arrayModel);
            operation.response(200, successResponse);
        } else if (map.get(Const.BLANK_CONST) != null) {
            ModelImpl model = (ModelImpl) map.get(Const.BLANK_CONST);
            successResponse.setDescription("响应类型200,新建200响应状态码会覆盖响应信息\n");
            successResponse.setResponseSchema(model);
            operation.response(200, successResponse);
        }


        //注意如果录入的status code 中有200，会覆盖上面的200对应的example value
        if (Const.API_ACTION_TYPE.equals(apiInfo.getType())) {
            convertStatusCodeToSwaggerResponse(operation, apiInfo.getId());
        } else {
            convertNormalStatusCodeToSwaggerResponse(operation, apiInfo.getId());
        }
        return operation;
    }

    @Override
    public String generateSwaggerJson(long apiId) throws JsonProcessingException {
        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));
        String pathString = apiInfo.getApiPath();
        if (apiInfo == null) {
            return null;
        }

        final Info info = new Info();
        Swagger swagger = new Swagger()
                .info(info)
                .scheme(Scheme.HTTP)
                .consumes("application/json;charset=utf-8")
                .produces("application/json;charset=utf-8");


        Operation operation = generateOperstaionForSwagger(apiId, swagger);
        String method = apiInfo.getApiMethod().toLowerCase();
        swagger.path(pathString, new Path().set(method, operation));

//        return JSON.toJSONString(swagger);
        return Json.mapper().writeValueAsString(swagger);

    }

    /**
     * 如果是json导入的话会覆盖原来的，因此先删除以前的参数
     * 导入的类型是基本类型或者build_in类型
     *
     * @param apiId
     * @param serviceId
     * @param params
     * @param type
     * @return
     */
    @Transactional
    @Override
    public List<ApiBody> generateApiBodyByJson(long apiId, long serviceId, Map<String, Object> params, String type) {
        List<ApiBody> apiBodyList = new ArrayList<>();
        if (params == null || params.size() == 0) {
            return apiBodyList;
        }
        apiBodyService.deleteBody(apiId, type);

        ApiParamType apiParamType;
        for (Entry<String, Object> entry : params.entrySet()) {
            apiParamType = apiParamTypeService.listApiParamType("String");
            ApiBody apiBody = new ApiBody();
            apiBody.setApiId(apiId);
            apiBody.setParamName(entry.getKey());
            apiBody.setType(type);
            apiBody.setRequired("1");
            apiBody.setParamType("String");
            apiBody.setCreateDate(System.currentTimeMillis());
            if (entry.getValue() instanceof Boolean) {
                apiBody.setParamType("Boolean");
                apiParamType = apiParamTypeService.listApiParamType("Boolean");
            } else if (entry.getValue() instanceof Integer) {
                apiBody.setParamType("Int");
                apiParamType = apiParamTypeService.listApiParamType("Int");
            } else if (entry.getValue() instanceof Long) {
                apiBody.setParamType("Long");
                apiParamType = apiParamTypeService.listApiParamType("Long");
            } else if (entry.getValue() instanceof Double) {
                apiBody.setParamType("Double");
                apiParamType = apiParamTypeService.listApiParamType("Double");
            } else if (entry.getValue() instanceof Map) {
                //创建一个不带参数的model
                CreateApiModelDto createApiModelDto = new CreateApiModelDto();
                createApiModelDto.setServiceId(serviceId);
                createApiModelDto.setModelName(entry.getKey());
                createApiModelDto.setDescription("自动创建的");
                createApiModelDto.setParams(new ArrayList<ApiParamDto>());

                //判断ModelName是否存在
                if (apiModelService.isApiModelExists(createApiModelDto.getModelName(), serviceId)) {
                    //已存在
                    createApiModelDto.setModelName("m" + System.currentTimeMillis());
                }
                long modelId = apiModelService.addApiModel(createApiModelDto);

                apiParamType = apiParamTypeService.listModleParamType(modelId);
                if (apiParamType != null) {
                    apiBody.setParamType(createApiModelDto.getModelName());
                    apiBody.setParamTypeId(apiParamType.getId());
                    apiBody.setDescription("自动创建的模型，需要核对参数是否正确并增加描述");
                }

                if (((Map) entry.getValue()).size() != 0) {
                    //生成model中的参数
                    generate((Map<String, Object>) entry.getValue(), modelId, serviceId);
                }
            } else if (entry.getValue() instanceof List) {
                apiBody.setParamType("Array");
                apiParamType = apiParamTypeService.listApiParamType("Array");
                apiBody.setArrayDataTypeName("String");

                ApiParamType apiParamType1;

                List list = (List) entry.getValue();
                if (list == null || list.size() == 0) {
                    apiParamType1 = getApiParamTypeFromCache("Object");
                    apiBody.setArrayDataTypeId(apiParamType1.getId());
                } else {
                    if (list.get(0) instanceof String) {
                        apiBody.setArrayDataTypeName("String");
                        apiParamType1 = getApiParamTypeFromCache("String");
                        apiBody.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Boolean) {
                        apiBody.setArrayDataTypeName("Boolean");
                        apiParamType1 = apiParamTypeService.listApiParamType("Boolean");
                        apiBody.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Integer) {
                        apiBody.setArrayDataTypeName("Int");
                        apiParamType1 = apiParamTypeService.listApiParamType("Int");
                        apiBody.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Long) {
                        apiBody.setArrayDataTypeName("Long");
                        apiParamType1 = apiParamTypeService.listApiParamType("Long");
                        apiBody.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Double) {
                        apiBody.setArrayDataTypeName("Double");
                        apiParamType1 = apiParamTypeService.listApiParamType("Double");
                        apiBody.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Map) {
                        //创建一个不带参数的model
                        CreateApiModelDto createApiModelDto = new CreateApiModelDto();
                        createApiModelDto.setServiceId(serviceId);
                        createApiModelDto.setModelName(entry.getKey());
                        createApiModelDto.setDescription("自动创建的");
                        createApiModelDto.setParams(new ArrayList<ApiParamDto>());

                        //判断ModelName是否存在
                        if (apiModelService.isApiModelExists(createApiModelDto.getModelName(), serviceId)) {
                            //已存在
                            createApiModelDto.setModelName("m" + System.currentTimeMillis());
                        }
                        long modelId = apiModelService.addApiModel(createApiModelDto);

                        apiParamType1 = apiParamTypeService.listModleParamType(modelId);
                        if (apiParamType1 != null) {
                            apiBody.setArrayDataTypeName(createApiModelDto.getModelName());
                            apiBody.setArrayDataTypeId(apiParamType1.getId());
                            apiBody.setDescription("将Array中的参数自动创建为模型，需要核对参数是否正确并增加描述");
                        }
                        if (((Map) list.get(0)).size() != 0) {
                            //生成model中的参数
                            generate((Map<String, Object>) list.get(0), modelId, serviceId);
                        }
                    }
                }
            }
            if (apiParamType == null) {
                logger.warn("json方式导入生成body时，参数{}的类型解析失败，为对应到库中的paramType", entry.getKey());
                continue;
            }
            apiBody.setParamTypeId(apiParamType.getId());
            long paramId = apiBodyService.addBody(apiBody);
            apiBody.setId(paramId);
            apiBodyList.add(apiBody);
        }

        return apiBodyList;
    }

    //生成model
    public void generate(Map<String, Object> params, long modelId, long serviceId) {

        ApiParamType apiParamType;
        for (Entry<String, Object> entry : params.entrySet()) {
            apiParamType = apiParamTypeService.listApiParamType("String");
            //添加model参数
            ApiModelParam apiModelParam = new ApiModelParam();
            apiModelParam.setCreateDate(System.currentTimeMillis());
            apiModelParam.setModelId(modelId);
            apiModelParam.setParamName(entry.getKey());
            apiModelParam.setRequired("1");

            if (entry.getValue() instanceof Boolean) {
                apiParamType = getApiParamTypeFromCache("Boolean");
            } else if (entry.getValue() instanceof Integer) {
                apiParamType = getApiParamTypeFromCache("Int");
            } else if (entry.getValue() instanceof Long) {
                apiParamType = getApiParamTypeFromCache("Long");
            } else if (entry.getValue() instanceof Double) {
                apiParamType = getApiParamTypeFromCache("Double");
            } else if (entry.getValue() instanceof Map) {
                //创建一个模型
                CreateApiModelDto createApiModelDto = new CreateApiModelDto();
                createApiModelDto.setServiceId(serviceId);
                createApiModelDto.setModelName(entry.getKey());
                createApiModelDto.setDescription("自动创建的");
                createApiModelDto.setParams(new ArrayList<>());

                //判断ModelName是否存在
                if (apiModelService.isApiModelExists(createApiModelDto.getModelName(), serviceId)) {
                    //已存在
                    createApiModelDto.setModelName("m" + System.currentTimeMillis());
                }

                long modelId1 = apiModelService.addApiModel(createApiModelDto);
                apiParamType = apiParamTypeService.listModleParamType(modelId1);

                if (((Map) entry.getValue()).size() != 0) {
                    //生成model中的参数
                    generate((Map<String, Object>) entry.getValue(), modelId1, serviceId);
                }
            } else if (entry.getValue() instanceof List) {
                apiParamType = getApiParamTypeFromCache("Array");

                ApiParamType apiParamType1;

                List list = (List) entry.getValue();
                if (list == null || list.size() == 0) {
                    apiParamType1 = getApiParamTypeFromCache("Object");
                    apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                } else {
                    if (list.get(0) instanceof String) {
                        apiParamType1 = getApiParamTypeFromCache("String");
                        apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Boolean) {
                        apiParamType1 = getApiParamTypeFromCache("Boolean");
                        apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Integer) {
                        apiParamType1 = getApiParamTypeFromCache("Int");
                        apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Long) {
                        apiParamType1 = getApiParamTypeFromCache("Long");
                        apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Double) {
                        apiParamType1 = getApiParamTypeFromCache("Double");
                        apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                    } else if (list.get(0) instanceof Map) {
                        //创建一个不带参数的model
                        CreateApiModelDto createApiModelDto = new CreateApiModelDto();
                        createApiModelDto.setServiceId(serviceId);
                        createApiModelDto.setModelName(entry.getKey());
                        createApiModelDto.setDescription("自动创建的");
                        createApiModelDto.setParams(new ArrayList<>());

                        //判断ModelName是否存在
                        if (apiModelService.isApiModelExists(createApiModelDto.getModelName(), serviceId)) {
                            //已存在
                            createApiModelDto.setModelName("m" + System.currentTimeMillis());
                        }
                        long modelId1 = apiModelService.addApiModel(createApiModelDto);

                        apiParamType1 = apiParamTypeService.listModleParamType(modelId1);
                        if (apiParamType1 != null) {
                            apiModelParam.setArrayDataTypeId(apiParamType1.getId());
                        }
                        if (((Map) list.get(0)).size() != 0) {
                            //生成model中的参数
                            generate((Map<String, Object>) list.get(0), modelId1, serviceId);
                        }
                    }
                }
            }
            apiModelParam.setParamTypeId(apiParamType.getId());
            apiModelParamDao.add(apiModelParam);
        }
    }

    /**
     * 注意swagger中response body是作为一个ObjectProperty输出的，
     * 而request body是按照一个参数对应一个BodyParameter来输出的
     *
     * @param apiId
     * @param swagger
     * @return
     */
    @Override
    // public List<BodyParameter> convertRequestBodyToSwagger(long apiId, Swagger swagger) {
    public BodyParameter convertRequestBodyToSwagger(long apiId, Swagger swagger) {
        //得到body的map形式
        Map<String, Object> map = generateJsonForApiWithDesc(apiId, "REQUEST");

        //查询API,为了生成modelName时不会重复
        ApiInfo apiInfo = apiInfoService.getApi(String.valueOf(apiId));
        //遍历map
        Model model;

        BodyParameter bodyParameter = new BodyParameter();

        bodyParameter.setName("Body");
        bodyParameter.setDescription(apiInfo.getDescription());
        if (map == null || map.size() < 1) {
            bodyParameter.setRequired(false);
        } else {
            bodyParameter.setRequired(true);
        }
        Map<String, Property> propertyMap = generatePropertiesFromMap(apiId, swagger, map, Const.REQUEST_PARAM_TYPE);

        if (map.keySet().contains(Const.BLANK_CONST)) {
            ModelImpl modelImpl = new ModelImpl();
            modelImpl.setType(propertyMap.get(Const.BLANK_CONST).getType());
            bodyParameter.setSchema(modelImpl);
            return bodyParameter;
        }
        if (!map.keySet().contains(Const.BLANK_ARRAY_CONST)) {
            model = new ModelImpl();
            model.setProperties(propertyMap);
            bodyParameter.setSchema(model);
            return bodyParameter;
        }
        //array model 返回
        ArrayModel arrayModel = new ArrayModel();
        ArrayProperty blank_array = (ArrayProperty) propertyMap.get(Const.BLANK_ARRAY_CONST);
        arrayModel.setItems(blank_array.getItems());
        bodyParameter.setSchema(arrayModel);
        return bodyParameter;
    }

    /**
     * 为参数生成描述
     *
     * @param apiBody
     * @return
     */
    // public String generateDescForParam(ApiBody apiBody) {
    //
    // if (apiBody == null) {
    // return "";
    // }
    //
    // String desc = "";
    // if (!StringUtils.isBlank(apiBody.getDescription())) {
    // desc += apiBody.getParamName();
    // desc += ": ";
    // desc += apiBody.getDescription();
    // desc += "; ";
    // desc += "\n";
    // }
    //
    // ApiParamType apiParamType = apiParamTypeService.listApiParamType(apiBody.getParamTypeId());
    // //TODO
    // if (apiParamType == null) {
    // return desc;
    // }
    //
    // //如果是特殊类型
    // if ("Array".equals(apiParamType.getParamType())) {
    // apiParamType = apiParamTypeService.listApiParamType(apiBody.getArrayDataTypeId());
    // //TODO
    // if (apiParamType != null && "MODEL".equals(apiParamType.getLocation())) {
    // //如果Array中嵌套的是Model，则将Model中的描述进行拼接
    // desc += generateDescForModel(apiParamType.getModelId());
    // }
    // } else if (apiParamType != null && "MODEL".equals(apiParamType.getLocation())) {
    // desc += generateDescForModel(apiParamType.getModelId());
    // }
    // return desc;
    // }


    /**
     * 为数据模型生成描述：将其中各个字段的描述拼接起来
     *
     * @param modelId
     * @return
     */
    public String generateDescForModel(long modelId) {

        CreateApiModelDto createApiModelDto = apiModelService.getApiModelByModelId(modelId);
        List<ApiParamDto> apiParamDtoList = createApiModelDto.getParams();

        Map<String, Object> map = new HashMap<>();
        ApiParamType apiParamType;
        String desc = "";

        for (ApiParamDto apiParamDto : apiParamDtoList) {

            if (!StringUtils.isBlank(apiParamDto.getDescription())) {
                desc += apiParamDto.getParamName();
                desc += ": ";
                desc += apiParamDto.getDescription();
                desc += ";   ";
                desc += "\n";
            }

            apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());
            if ("Object".equals(apiParamType.getParamType())) {
                ApiParamObject apiParamObject = apiParamObjectDao.get(apiParamDto.getObjectId());

                List<JSONObject> apiParamDtos = JSON.parseObject(apiParamObject.getObjectValue(), List.class);

                for (JSONObject temp : apiParamDtos) {
                    apiParamDto = JSONObject.toJavaObject(temp, ApiParamDto.class);
                    desc += generateDescForObject(apiParamDto);
                }

            } else if ("Array".equals(apiParamType.getParamType())) {
                //如果是数组类型
                //TODO 目前Array中不能存在Object以及Array，
                apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getArrayDataTypeId());
                if ("MODEL".equals(apiParamType.getLocation())) {
                    desc += generateDescForModel(apiParamType.getModelId());
                }
            } else if ("MODEL".equals(apiParamType.getLocation())) {
                //如果是Model类型
                desc += generateDescForModel(apiParamType.getModelId());
            }
        }
        return desc;
    }


    /**
     * 如果Model中存在嵌套的数据类型，需要进行递归处理
     *
     * @return
     */
    public String generateDescForObject(ApiParamDto apiParamDto) {

        String desc = "";
        ApiParamType apiParamType;

        apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());
        if (!StringUtils.isBlank(apiParamDto.getDescription())) {
            desc += apiParamDto.getParamName();
            desc += ": ";
            desc += apiParamDto.getDescription();
            desc += ";   ";
            desc += "\n";
        }

        if ("Array".equals(apiParamType.getParamType())) {
            apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getArrayDataTypeId());
            if ("MODEL".equals(apiParamType.getLocation())) {
                desc += generateDescForModel(apiParamType.getModelId());
            }
        } else if ("MODEL".equals(apiParamType.getLocation())) {
            //如果是Model类型
            desc += generateDescForModel(apiParamType.getModelId());
        } else if ("Object".equals(apiParamType.getParamType())) {
            if (apiParamDto.getObjectId() == 0 && apiParamDto.getObjectParams() != null) {
                //object类型多层嵌套对应object_params表
                for (ApiParamDto apiParamDto1 : apiParamDto.getObjectParams()) {
                    desc += generateDescForObject(apiParamDto1);
                }
            } else {
                desc += apiParamDto.getDescription();
            }
        }

        return desc;
    }

    /**
     * 将按条存储的参数进行转换
     *
     * @param apiBody
     * @return
     */
    public Object convertToJson(ApiBody apiBody) {

        ApiParamType apiParamType = apiParamTypeService.listApiParamType(apiBody.getParamTypeId());
        //TODO
        if (apiParamType == null) {
            return "随机值";
        }
        if ("String".equals(apiParamType.getParamType())) {
            //如果是基本类型，如String
            return "随机值";
        } else if ("Int".equals(apiParamType.getParamType())) {
            return 0;
        } else if ("Long".equals(apiParamType.getParamType())) {
            return 0L;
        } else if ("Double".equals(apiParamType.getParamType())) {
            return 0D;
        } else if ("Number".equals(apiParamType.getParamType())) {
            return 0L;
        } else if ("Boolean".equals(apiParamType.getParamType())) {
            return false;
        } else if ("Object".equals(apiParamType.getParamType())) {
            //TODO 如果是Object类型，取出对应的ObjectId对应的json串，后续直接处理ApiParamDto
            return new HashMap<>();
        } else if ("Array".equals(apiParamType.getParamType())) {
            //TODO 如果模型被删除，则参数类型自动修改为String
            apiParamType = apiParamTypeService.listApiParamType(apiBody.getArrayDataTypeId());
            if (apiParamType == null) {
                List<String> list = new ArrayList<>();
                list.add("随机值");
                return list;
            }
            //如果是Array类型，判断Array里存储的是特殊类型还是基本类型
            if ("String".equals(apiParamType.getParamType())) {
                List<String> list = new ArrayList<>();
                list.add("随机值");
                return list;
            } else if ("Int".equals(apiParamType.getParamType())) {
                List<Integer> list = new ArrayList<>();
                list.add(new Integer(0));
                return list;
            } else if ("Long".equals(apiParamType.getParamType())) {
                List<Long> list = new ArrayList<>();
                list.add(new Long(0L));
                return list;
            } else if ("Double".equals(apiParamType.getParamType())) {
                List<Double> list = new ArrayList<>();
                list.add(new Double(0D));
                return list;
            } else if ("Number".equals(apiParamType.getParamType())) {
                List<Long> list = new ArrayList<>();
                list.add(new Long(0L));
                return list;
            } else if ("Boolean".equals(apiParamType.getParamType())) {
                List<Boolean> list = new ArrayList<>();
                list.add(false);
                return list;
            } else if ("Object".equals(apiParamType.getParamType())) {
                List<Map> list = new ArrayList<>();
                list.add(new HashMap<>());
                return list;
            } else if ("MODEL".equals(apiParamType.getLocation())) {
                List<Map> list = new ArrayList<>();
                list.add(convertModelToMap(apiParamType.getModelId()));
                return list;
            }

        } else if ("MODEL".equals(apiParamType.getLocation())) {
            //如果是Model类型
            return convertModelToMap(apiParamType.getModelId());
        }
        return "";
    }

    /**
     * 模型一定是个Map
     *
     * @param apiParamDto
     * @return
     */
    public Map convertModelToMap(ApiParamDto apiParamDto) {
        if (apiParamDto == null) {
            return null;
        }

        ApiParamType apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());

        Map<String, Object> map = new LinkedHashMap<>();

        if ("String".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), "随机值");
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Int".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), 0);
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Long".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), 0L);
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Double".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), 0D);
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Number".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), 0);
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Boolean".equals(apiParamType.getParamType())) {
            if (StringUtils.isBlank(apiParamDto.getDefValue())) {
                map.put(apiParamDto.getParamName(), false);
            } else {
                map.put(apiParamDto.getParamName(), apiParamDto.getDefValue());
            }
        } else if ("Array".equals(apiParamType.getParamType())) {

            map.put(apiParamDto.getParamName(), convertArrayToJson(apiParamDto.getArrayDataTypeName(), apiParamDto.getArrayDataTypeId()));

//            //TODO 目前Array中不能存在Object以及Array，因为前端拖动不支持
//            List<Object> list = new ArrayList<>();
//            if ("String".equals(apiParamDto.getArrayDataTypeName())) {
//                list.add("随机值");
//                map.put(apiParamDto.getParamName(), list);
//            } else if ("Number".equals(apiParamDto.getArrayDataTypeName())) {
//                list.add(0);
//                map.put(apiParamDto.getParamName(), list);
//            } else if ("Boolean".equals(apiParamDto.getArrayDataTypeName())) {
//                list.add(false);
//                map.put(apiParamDto.getParamName(), list);
//            } else {
//                //根据
//                apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getArrayDataTypeId());
//                if ("MODEL".equals(apiParamType.getLocation())) {
//                    list.add(convertModelToMap(apiParamType.getModelId()));
//                    map.put(apiParamDto.getParamName(), list);
//                } else {
//                    //其余全部暂时处理为String
//                    map.put(apiParamDto.getParamName(), list);
//                }
//            }
        } else if ("Object".equals(apiParamType.getParamType())) {
            //获取object value

            if (apiParamDto.getObjectId() == 0 && apiParamDto.getObjectParams() != null) {
                //object类型多层嵌套对应object_params表
                Map<String, Object> map1;
                for (ApiParamDto apiParamDto1 : apiParamDto.getObjectParams()) {
                    map1 = convertModelToMap(apiParamDto1);
                    if (apiParamDto1.getObjectParams() != null) {
                        map.put(apiParamDto1.getParamName(), map1);
                    } else {
                        //如果是最里层的Object，则不能使用map.put(apiParamDto1.getParamName(), map1)，会造成重复，因此采用下面这种
                        for (Entry<String, Object> entry : map1.entrySet()) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            } else {
                Map<String, Object> map1;
                map1 = convertObjectToJson(apiParamDto.getObjectId(), apiParamDto.getParamName());
                for (Entry<String, Object> entry : map1.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }

//            if (apiParamDto.getObjectId() == 0) {
//                map.put(apiParamDto.getParamName(), new HashMap<>());
//            } else {
//                Map<String, Object> map1 = new HashMap<>();
//                //获取ObjectValue
//                ApiParamObject apiParamObject = apiParamObjectDao.get(apiParamDto.getObjectId());
//                if ("null".equals(apiParamObject.getObjectValue())) {
//                    map.put(apiParamDto.getParamName(), new HashMap<>());
//                } else {
//                    List<JSONObject> apiParamDtoList = JSON.parseObject(apiParamObject.getObjectValue(), List.class);
//                    if (apiParamDtoList == null) {
//                        //如果转换失败，则apiParamDtoList为null
//                        map.put(apiParamDto.getParamName(), new HashMap<>());
//                    } else {
//                        for (JSONObject temp : apiParamDtoList) {
//                            apiParamDto = JSONObject.toJavaObject(temp, ApiParamDto.class);
//                            map1.put(apiParamDto.getParamName(), convertModelToMap(apiParamDto));
//                        }
//                        map.put(apiParamDto.getParamName(), map1);
//                    }
//
//                }
//            }

        } else {
            apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());
            if ("MODEL".equals(apiParamType.getLocation())) {
                //如果是模型，查询该模型对应的
                map.put(apiParamDto.getParamName(), convertModelToMap(apiParamType.getModelId()));
            }
        }
        return map;
    }


    /**
     * 将模型转换为Map
     *
     * @param modelId
     * @return
     */
    public Map convertModelToMap(long modelId) {
        CreateApiModelDto createApiModelDto = apiModelService.getApiModelByModelId(modelId);
        List<ApiParamDto> apiParamDtoList = createApiModelDto.getParams();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(Const.MODEL_NAME_PREFIX, createApiModelDto.getModelName());
        ApiParamType apiParamType;
        for (ApiParamDto apiParamDto : apiParamDtoList) {
            apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());
            map.put((String) apiParamDto.getParamName() + ".gwDesc", apiParamDto.getDescription());
            map.put((String) apiParamDto.getParamName() + ".isRequired", ("1".equals(apiParamDto.getRequired()) ? true : false));
            if ("String".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), "随机值");
            } else if ("Int".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), 0);
            } else if ("Long".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), 0L);
            } else if ("Double".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), 0D);
            } else if ("Number".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), 0L);
            } else if ("Boolean".equals(apiParamType.getParamType())) {
                map.put(apiParamDto.getParamName(), false);
            } else if ("Object".equals(apiParamType.getParamType())) {
                Map<String, Object> map1;
                map1 = convertObjectToJson(apiParamDto.getObjectId(), apiParamDto.getParamName());
                for (Entry<String, Object> entry : map1.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            } else if ("Array".equals(apiParamType.getParamType())) {
                //如果是数组类型
                //TODO 目前Array中不能存在Object以及Array，因为前端拖动不支持
                List<Object> list;
                list = convertArrayToJson(apiParamDto.getArrayDataTypeName(), apiParamDto.getArrayDataTypeId());
                map.put(apiParamDto.getParamName(), list);
            } else if ("MODEL".equals(apiParamType.getLocation())) {
                //如果是Model类型，则调用convertModelToMap
                Map<String, Object> map1;
                map1 = convertModelToMap(apiParamDto);
                for (Entry<String, Object> entry : map1.entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
                map.put((String) apiParamDto.getParamTypeName() + ".gwDesc", apiParamDto.getDescription());
                map.put((String) apiParamDto.getParamTypeName() + ".isRequired", ("1".equals(apiParamDto.getRequired()) ? true : false));
            }
        }
        return map;
    }


    /**
     * 将Array转换成json
     *
     * @param arrayDataTypeName
     * @param arrayDataTypeId
     * @return
     */
    public List<Object> convertArrayToJson(String arrayDataTypeName, long arrayDataTypeId) {
        List<Object> list = new ArrayList<>();
        if ("String".equals(arrayDataTypeName)) {
            list.add("随机值");
            return list;
        } else if ("Int".equals(arrayDataTypeName)) {
            list.add(0);
            return list;
        } else if ("Long".equals(arrayDataTypeName)) {
            list.add(0L);
            return list;
        } else if ("Double".equals(arrayDataTypeName)) {
            list.add(0D);
            return list;
        } else if ("Number".equals(arrayDataTypeName)) {
            list.add(0);
            return list;
        } else if ("Boolean".equals(arrayDataTypeName)) {
            list.add(false);
            return list;
        } else if ("Object".equals(arrayDataTypeName)) {
            list.add(new HashMap<>());
            return list;
        } else {
            //TODO 后续补充Object类型
            ApiParamType apiParamType = apiParamTypeService.listApiParamType(arrayDataTypeId);
            if (apiParamType != null && "MODEL".equals(apiParamType.getLocation())) {
                //如果是Model类型
                list.add(convertModelToMap(apiParamType.getModelId()));
                return list;
            } else {
                //其余类型全部暂时处理为Object
                return new ArrayList<>();
            }
        }
    }

    /**
     * 将Object转换成json
     *
     * @param objectId
     * @param paramName
     * @return
     */
    public Map convertObjectToJson(long objectId, String paramName) {

        ApiParamType apiParamType;
        Map<String, Object> map = new HashMap<>();

        if (objectId == 0) {
            map.put(paramName, new HashMap<>());
        } else {
            Map<String, Object> map1 = new HashMap<>();
            //获取ObjectValue
            ApiParamObject apiParamObject = apiParamObjectDao.get(objectId);
            if ("null".equals(apiParamObject.getObjectValue())) {
                map.put(paramName, new HashMap<>());
            } else {
                List<JSONObject> apiParamDtoList = JSON.parseObject(apiParamObject.getObjectValue(), List.class);
                if (apiParamDtoList == null) {
                    //如果转换失败，则apiParamDtoList为null
                    map.put(paramName, new HashMap<>());
                } else {
                    for (JSONObject temp : apiParamDtoList) {
                        ApiParamDto apiParamDto = JSONObject.toJavaObject(temp, ApiParamDto.class);
                        //即使转换成功，也可能是基本类型
                        apiParamType = apiParamTypeService.listApiParamType(apiParamDto.getParamTypeId());
                        if ("String".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), "随机值");
                        } else if ("Int".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), 0);
                        } else if ("Long".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), 0L);
                        } else if ("Double".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), 0D);
                        } else if ("Number".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), 0);
                        } else if ("Boolean".equals(apiParamType.getParamType())) {
                            map1.put(apiParamDto.getParamName(), false);
                        } else if ("Array".equals(apiParamType.getParamType())) {
                            List<Object> list;
                            list = convertArrayToJson(apiParamDto.getArrayDataTypeName(), apiParamDto.getArrayDataTypeId());
                            map1.put(apiParamDto.getParamName(), list);
                        } else if ("Object".equals(apiParamType.getParamType())) {
                            //object多层嵌套
                            map1.put(apiParamDto.getParamName(), convertModelToMap(apiParamDto));
                        } else if ("MODEL".equals(apiParamType.getLocation())) {
                            Map<String, Object> map2;
                            map2 = convertModelToMap(apiParamDto);
                            for (Entry<String, Object> entry : map2.entrySet()) {
                                map1.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    map.put(paramName, map1);
                }
            }
        }
        return map;
    }


    /**
     * 将数组类型转换成swagger json
     *
     * @return
     */
    public ArrayProperty convertListToSwaggerjson(List<Object> list, String modelName, Swagger swagger) {

        ArrayProperty arrayProperty = new ArrayProperty();

        if (list == null || list.size() == 0) {
            return arrayProperty;
        }
        //list中的参数类型只能为一种
        Object param = list.get(0);
        if (param instanceof String) {
            arrayProperty.setItems(new StringProperty());
        } else if (param instanceof Boolean) {
            arrayProperty.setItems(new BooleanProperty());
        } else if (param instanceof Integer) {
            arrayProperty.setItems(new IntegerProperty());
        } else if (param instanceof Long) {
            arrayProperty.setItems(new LongProperty());
        } else if (param instanceof Double) {
            arrayProperty.setItems(new DoubleProperty());
        } else if (param instanceof Map) {
            if (((Map) param).size() == 0) {
                arrayProperty.setItems(new ObjectProperty());
            } else {
                Map resultMap = (Map) param;
                if (resultMap.get(Const.MODEL_NAME_PREFIX) != null && resultMap.get(Const.MODEL_NAME_PREFIX) instanceof String) {
                    modelName = (String) resultMap.get(Const.MODEL_NAME_PREFIX);
                }
                convertMapToSwaggerjson((Map) param, modelName, swagger);
                arrayProperty.setItems(new RefProperty(modelName));
            }
        } else {
            arrayProperty.setItems(new StringProperty());
        }
        return arrayProperty;
    }

    /**
     * 将Map(Object)类型转换成swagger json
     *
     * @return
     */
    public void convertMapToSwaggerjson(Map<String, Object> map, String modelName, Swagger swagger) {
//        if (map == null || map.size() == 0) {
//            return
//        }
        Model model = new ModelImpl();
        Map<String, Property> propertyMap = new LinkedHashMap<>();

        //遍历Map
        for (Entry<String, Object> entry : map.entrySet()) {

            if (entry.getKey().equals(Const.MODEL_NAME_PREFIX)) {
                continue;
            }

            if (entry.getKey().endsWith(".gwDesc")) {
                continue;
            }
            if (entry.getKey().endsWith(".isRequired")) {
                continue;
            }

            String des = map.keySet().contains(entry.getKey() + ".gwDesc") ? (String) map.get(entry.getKey() + ".gwDesc") : null;
            boolean required = map.keySet().contains(entry.getKey() + ".isRequired") ? (Boolean) map.get(entry.getKey() + ".isRequired") : true;

            if (entry.getValue() instanceof String) {
                StringProperty stringProperty = new StringProperty();
                stringProperty.setDescription(des);
                stringProperty.setRequired(required);
                propertyMap.put(entry.getKey(), stringProperty);
            } else if (entry.getValue() instanceof Boolean) {
                BooleanProperty booleanProperty = new BooleanProperty();
                booleanProperty.setDescription(des);
                booleanProperty.setRequired(required);
                propertyMap.put(entry.getKey(), booleanProperty);
            } else if (entry.getValue() instanceof Integer) {
                IntegerProperty integerProperty = new IntegerProperty();
                integerProperty.setDescription(des);
                integerProperty.setRequired(required);
                propertyMap.put(entry.getKey(), integerProperty);// model.setProperties(propertyMap);
            } else if (entry.getValue() instanceof Long) {
                LongProperty longProperty = new LongProperty();
                longProperty.setDescription(des);
                longProperty.setRequired(required);
                propertyMap.put(entry.getKey(), longProperty);// model.setProperties(propertyMap);
            } else if (entry.getValue() instanceof Double) {
                DoubleProperty doubleProperty = new DoubleProperty();
                doubleProperty.setDescription(des);
                doubleProperty.setRequired(required);
                propertyMap.put(entry.getKey(), doubleProperty);// model.setProperties(propertyMap);
            } else if (entry.getValue() instanceof List) {
                // propertyMap.put(entry.getKey(), convertListToSwaggerjson((List) entry.getValue(), modelName + "." + entry.getKey(), swagger));

                ArrayProperty arrayProperty = convertListToSwaggerjson((List) entry.getValue(), modelName + "." + entry.getKey(), swagger);
                arrayProperty.setDescription(des);
                arrayProperty.setRequired(required);

                propertyMap.put(entry.getKey(), arrayProperty);
            } else if (entry.getValue() instanceof Map) {
                //如果多层嵌套，定义modelName=modelName+"."+entry.getKey()
                if (((Map) entry.getValue()).size() == 0) {
                    ObjectProperty objectProperty = new ObjectProperty();
                    objectProperty.setName(entry.getKey());
                    objectProperty.setDescription(des);
                    objectProperty.setRequired(required);
                    propertyMap.put(entry.getKey(), objectProperty);
                    continue;
                }

                Map resultMap = (Map) entry.getValue();
                if (resultMap.get(Const.MODEL_NAME_PREFIX) != null && resultMap.get(Const.MODEL_NAME_PREFIX) instanceof String) {
                    convertMapToSwaggerjson((Map) entry.getValue(), (String) resultMap.get(Const.MODEL_NAME_PREFIX), swagger);
                } else {
                    convertMapToSwaggerjson((Map) entry.getValue(), modelName, swagger);
                }
                // propertyMap.put(entry.getKey(), new RefProperty(modelName + "." + entry.getKey()));

                RefProperty refProperty = new RefProperty((String) resultMap.get(Const.MODEL_NAME_PREFIX));
                refProperty.setDescription(des);
                refProperty.setRequired(required);

                propertyMap.put(entry.getKey(), refProperty);
            } else {
                propertyMap.put(entry.getKey(), new StringProperty());
            }
        }
        model.setProperties(propertyMap);
        swagger.model(modelName, model);
    }
}
