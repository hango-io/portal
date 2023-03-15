package org.hango.cloud.gdashboard.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.dao.ApiBodyDao;
import org.hango.cloud.gdashboard.api.dao.ApiModelDao;
import org.hango.cloud.gdashboard.api.dao.ApiModelParamDao;
import org.hango.cloud.gdashboard.api.dao.ApiParamObjectDao;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.dto.ApiParamDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.ApiModelParam;
import org.hango.cloud.gdashboard.api.meta.ApiParamObject;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 16:13.
 */
@Service
public class ApiModelServiceImpl implements IApiModelService {

    private static Logger logger = LoggerFactory.getLogger(ApiModelServiceImpl.class);
    @Autowired
    private ApiModelDao apiModelDao;
    @Autowired
    private ApiModelParamDao apiModelParamDao;
    @Autowired
    private ApiParamTypeDao apiParamTypeDao;
    @Autowired
    private ApiBodyDao apiBodyDao;
    @Autowired
    private ApiParamObjectDao apiParamObjectDao;
    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IGetProjectIdService projectIdService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addApiModel(CreateApiModelDto createApiModelDto) {
        ApiModel apiModel = getApiModel(createApiModelDto);
        long modelId = addApiModelBasic(apiModel);
        //添加参数
        addApiModelParam(createApiModelDto, modelId, null);
        //将此Model添加到nce_gateway_param_type中
        addApiParamModel(createApiModelDto, modelId);
        return modelId;
    }

    @Override
    public ApiModel getApiModel(CreateApiModelDto createApiModelDto) {
        ApiModel apiModel = new ApiModel();
        apiModel.setCreateDate(System.currentTimeMillis());
        apiModel.setModifyDate(System.currentTimeMillis());
        apiModel.setModelName(createApiModelDto.getModelName());
        apiModel.setServiceId(createApiModelDto.getServiceId());
        apiModel.setProjectId(projectIdService.getProjectId());
        if (createApiModelDto.getDescription() != null) {
            apiModel.setDescription(createApiModelDto.getDescription());
        } else {
            apiModel.setDescription("该模型由swagger自动创建");
        }
        return apiModel;
    }

    @Override
    @Transactional
    public long addApiModelBasic(ApiModel apiModel) {
        //添加基本信息
        return apiModelDao.add(apiModel);
    }

    @Override
    @Transactional
    public long addApiParamModel(CreateApiModelDto createApiModelDto, long modelId) {
        //将此Model添加到nce_gateway_param_type中
        ApiParamType apiParamType = new ApiParamType();
        apiParamType.setCreateDate(System.currentTimeMillis());
        apiParamType.setModifyDate(System.currentTimeMillis());
        apiParamType.setParamType(createApiModelDto.getModelName());
        apiParamType.setLocation("MODEL");
        apiParamType.setModelId(modelId);
        return apiParamTypeDao.add(apiParamType);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addApiModelParam(CreateApiModelDto createApiModelDto, long modelId, Map<String, Long> apiModels) {
        if (CollectionUtils.isEmpty(createApiModelDto.getParams())) {
            return modelId;
        }
        for (ApiParamDto apiParamDto : createApiModelDto.getParams()) {
            //判断数据类型是否是Object
//            Map<String, Object> head = Maps.newHashMap();
//            head.put("id", apiParamDto.getParamTypeId());
//            apiParamTypeDao.getCountByFields(head);

            //添加model参数
            ApiModelParam apiModelParam = new ApiModelParam();
            apiModelParam.setCreateDate(System.currentTimeMillis());
            apiModelParam.setModelId(modelId);
            apiModelParam.setParamName(apiParamDto.getParamName());
            apiModelParam.setDefValue(apiParamDto.getDefValue());
            apiModelParam.setDescription(apiParamDto.getDescription());
            apiModelParam.setArrayDataTypeId(apiParamDto.getArrayDataTypeId());
            apiModelParam.setParamTypeId(apiParamDto.getParamTypeId());
            if (apiModels != null) {
                if (apiModels.get(apiParamDto.getParamTypeName()) != null) {
                    apiModelParam.setParamTypeId(apiModels.get(apiParamDto.getParamTypeName()));
                }
                if (apiModels.get(apiParamDto.getArrayDataTypeName()) != null) {
                    apiModelParam.setArrayDataTypeId(apiModels.get(apiParamDto.getArrayDataTypeName()));
                }
            }
            apiModelParam.setRequired(apiParamDto.getRequired());

            ApiParamType apiParamType = apiParamTypeDao.get(apiModelParam.getParamTypeId());
            if (apiParamType != null && Const.OBJECT_PARAM_TYPE.equals(apiParamType.getParamType())) {
                //将Object类型的参数其value值插入到nce_gateway_param_object中
                ApiParamObject apiParamObject = new ApiParamObject();
                apiParamObject.setCreateDate(System.currentTimeMillis());
                apiParamObject.setObjectValue(JSON.toJSONString(apiParamDto.getObjectParams()));

                long apiParamObjectId = apiParamObjectDao.add(apiParamObject);
                apiModelParam.setObjectId(apiParamObjectId);
            }

            apiModelParamDao.add(apiModelParam);
        }
        return modelId;
    }

    @Override
    public CreateApiModelDto getApiModelByModelId(long apiModelId) {
        CreateApiModelDto createApiModelDto = new CreateApiModelDto();

        ApiModel apiModel = apiModelDao.get(apiModelId);
        createApiModelDto.setModelName(apiModel.getModelName());
        createApiModelDto.setDescription(apiModel.getDescription());
        createApiModelDto.setServiceId(apiModel.getServiceId());
        createApiModelDto.setParams(getApiModelParamsByModelId(apiModelId));
        createApiModelDto.setCreateDate(apiModel.getCreateDate());
        createApiModelDto.setModifyDate(apiModel.getModifyDate());
        createApiModelDto.setId(apiModel.getId());
        createApiModelDto.setSwaggerSync(apiModel.getSwaggerSync());

        return createApiModelDto;
    }

    @Override
    public List<Long> getApiModelInfoByServiceId(String serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceId", Long.parseLong(serviceId));
        List<ApiModel> apiModelList = apiModelDao.getRecordsByField(params);
        List<Long> modelIdList = new ArrayList<>();

        for (ApiModel apiModel : apiModelList) {
            modelIdList.add(apiModel.getId());
        }
        return modelIdList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiModel(long serviceId) {
        List<ApiModel> apiModelByServiceId = getApiModelByServiceId(serviceId);
        if (CollectionUtils.isEmpty(apiModelByServiceId)) return;
        //依次删除ApiModel的相关数据
        for (ApiModel apiModel : apiModelByServiceId) {
            deleteApiModelByModelId(apiModel.getId(), true);
        }
    }

    @Override
    public String getApiModelRefer(long modelId) {
        String message = null;
        ApiParamType apiParamType = apiParamTypeService.listModleParamType(modelId);

        Map<String, Object> params = Maps.newHashMap();
        params.put("paramTypeId", apiParamType.getId());

        List<ApiBody> apiBodyList1 = apiBodyDao.getRecordsByField(params);
        List<ApiModelParam> apiModelList1 = apiModelParamDao.getRecordsByField(params);

        params = Maps.newHashMap();
        params.put("arrayDataTypeId", apiParamType.getId());
        List<ApiBody> apiBodyList2 = apiBodyDao.getRecordsByField(params);
        List<ApiModelParam> apiModelList2 = apiModelParamDao.getRecordsByField(params);

        if (apiBodyList1.size() == 0 && apiBodyList2.size() == 0 &&
                apiModelList1.size() == 0 && apiModelList2.size() == 0) {
            return message;
        } else {
            //可优化下提示
            message = "该模型被API或其它模型所引用，不能被删除，请先解除引用";
            return message;
        }
    }


    @Override
    public List<ApiModel> getApiModelByServiceId(long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceId", serviceId);
        List<ApiModel> apiModelList = apiModelDao.getRecordsByField(params);

        return apiModelList;
    }

    @Override
    public ApiModel getApiModelInfoByModelId(long apiModelId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", apiModelId);
        List<ApiModel> apiModelList = apiModelDao.getRecordsByField(params);
        if (apiModelList.size() > 0) {
            return apiModelList.get(0);
        }
        return null;
    }

    @Override
    public ApiModel getApiModelByServiceIdAndModelName(long serviceId, String modelName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceId", serviceId);
        params.put("modelName", modelName);
        List<ApiModel> apiModelList = apiModelDao.getRecordsByField(params);
        if (apiModelList.size() > 0) {
            return apiModelList.get(0);
        }
        return null;
    }

    //    @Transactional
    @Override
    public boolean updateApiModel(CreateApiModelDto createApiModelDto, long modelId, String modelName, boolean flag) {
        if (flag) {
            //修改参数表里的模型名称
//            apiBodyDao.update(modelName, createApiModelDto.getModelName());
            ApiParamType apiParamType = new ApiParamType();
            apiParamType.setModelId(modelId);
            apiParamType.setParamType(createApiModelDto.getModelName());
            apiParamTypeDao.update(apiParamType);
        }

        ApiModel apiModel = getApiModelInfoByModelId(modelId);
        apiModel.setModelName(createApiModelDto.getModelName());
        apiModel.setDescription(createApiModelDto.getDescription());
        apiModel.setId(modelId);
        apiModel.setServiceId(createApiModelDto.getServiceId());
        apiModel.setModifyDate(System.currentTimeMillis());
        apiModel.setProjectId(projectIdService.getProjectId());

        apiModelDao.update(apiModel);

        //删除其参数，然后重新添加
        deleteApiModelByModelId(modelId, false);

        addApiModelParam(createApiModelDto, modelId, null);

        return true;
    }

    /**
     * 如果flag为true表示删除操作，否则用于更新操作
     *
     * @param apiModelId
     * @param flag
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long deleteApiModelByModelId(long apiModelId, boolean flag) {
        Map<String, Object> head = Maps.newHashMap();
        head.put("modelId", apiModelId);

        //参数类型如果包含Object，则首先删除Object包含的参数
        List<ApiModelParam> apiModelParamList = apiModelParamDao.getRecordsByField(head);
        ApiParamObject apiParamObject;
        for (ApiModelParam apiModelParam : apiModelParamList) {
            if (apiModelParam.getObjectId() != 0) {
                apiParamObject = new ApiParamObject();
                apiParamObject.setId(apiModelParam.getObjectId());
                apiParamObjectDao.delete(apiParamObject);
            }
        }

        //删除nce_gateway_model_param表中的记录
        apiModelParamDao.deleteApiModelParamByModelId(apiModelId);

        if (flag) {
            //删除nce_gateway_param_type表中的记录
            ApiParamType apiParamType = new ApiParamType();
            apiParamType.setModelId(apiModelId);
            apiParamTypeDao.delete(apiParamType);

            //删除模型的基本信息
            ApiModel apiModel = new ApiModel();
            apiModel.setId(apiModelId);
            long count = apiModelDao.delete(apiModel);
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public List<ApiParamDto> getApiModelParamsByModelId(long apiModelId) {

        List<ApiParamDto> apiParamDtos = new ArrayList<>();

        //查询所有参数
        HashMap<String, Object> head = Maps.newHashMap();
        head.put("modelId", apiModelId);
        List<ApiModelParam> apiModelParamList = apiModelParamDao.getRecordsByField(head);

        for (ApiModelParam apiModelParam : apiModelParamList) {
            ApiParamDto apiParamDto = new ApiParamDto();
            apiParamDto.setParamName(apiModelParam.getParamName());
            apiParamDto.setDefValue(apiModelParam.getDefValue());
            apiParamDto.setDescription(apiModelParam.getDescription());
            apiParamDto.setArrayDataTypeId(apiModelParam.getArrayDataTypeId());
            apiParamDto.setParamTypeId(apiModelParam.getParamTypeId());
            apiParamDto.setObjectId(apiModelParam.getObjectId());
            apiParamDto.setRequired(apiModelParam.getRequired());

            if (apiModelParam.getArrayDataTypeId() != 0) {
                apiParamDto.setArrayDataTypeName(apiParamTypeDao.get(apiModelParam.getArrayDataTypeId()).getParamType());
            } else {
                apiParamDto.setArrayDataTypeName("");
            }

            //如果参数类型为Object
            if (apiModelParam.getObjectId() != 0) {
                apiParamDto.setParamTypeName(Const.OBJECT_PARAM_TYPE);

                ApiParamObject apiParamObject = apiParamObjectDao.get(apiModelParam.getObjectId());

                String objectValue = changeIdToName(apiParamObject.getObjectValue());

                List<ApiParamDto> apiParamDtoList = new ArrayList<>();
                if (objectValue != null) {
                    apiParamDtoList = JSON.parseObject(objectValue, List.class);
                }

                apiParamDto.setObjectParams(apiParamDtoList);
            } else {
                apiParamDto.setParamTypeName(apiParamTypeDao.get(apiParamDto.getParamTypeId()).getParamType());
            }
            apiParamDtos.add(apiParamDto);
        }

        return apiParamDtos;
    }

    @Override
    public boolean isApiModelExists(String apiModelName, long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("modelName", apiModelName);
        params.put("serviceId", serviceId);
        return apiModelDao.getCountByFields(params) == 0 ? false : true;
    }

    @Override
    public boolean isApiModelExists(long apiModelId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", apiModelId);
        return apiModelDao.getCountByFields(params) == 0 ? false : true;
    }

    //将类型为Object的参数，其value值中的各类Id，如果不为0全部替换成对应的name
    public String changeIdToName(String objectValue) {
        if (objectValue == null || objectValue.equals("null")) {
            return null;
        }

        List<JSONObject> apiParamDtoList = JSON.parseObject(objectValue, List.class);

        List<ApiParamDto> apiParamDtos = new ArrayList<>();

        for (JSONObject temp : apiParamDtoList) {
            ApiParamDto apiParamDto = JSONObject.toJavaObject(temp, ApiParamDto.class);


            if (apiParamDto.getArrayDataTypeId() != 0) {
                String arrayParamType = apiParamTypeDao.get(apiParamDto.getArrayDataTypeId()).getParamType();
                apiParamDto.setArrayDataTypeName(arrayParamType);
            } else {
                //如果不设置为""，则fastjson在序列化的时候会忽略
                apiParamDto.setArrayDataTypeName("");
            }

            //如果参数类型为Object
            String paramType = apiParamTypeDao.get(apiParamDto.getParamTypeId()).getParamType();
            if (Const.OBJECT_PARAM_TYPE.equals(paramType) && apiParamDto.getObjectParams() != null) {
                apiParamDto.setParamTypeName(Const.OBJECT_PARAM_TYPE);

                String objectParams = changeIdToName(JSON.toJSONString(apiParamDto.getObjectParams()));
                List<ApiParamDto> apiParamDtoList1 = new ArrayList<>();
                if (objectParams != null) {
                    apiParamDtoList1 = JSON.parseObject(objectParams, List.class);
                }
                apiParamDto.setObjectParams(apiParamDtoList1);

            } else {
                apiParamDto.setParamTypeName(paramType);
            }
            apiParamDtos.add(apiParamDto);
        }
        return JSON.toJSONString(apiParamDtos);
    }


    @Override
    public List<ApiModel> findAllApiModelByProjectLimit(long serviceId, long project, long offset, long limit, String pattern) {
        if (serviceId == 0) {
            return apiModelDao.findApiModelByProjectLimit(project, offset, limit, pattern);
        } else {
            return apiModelDao.findApiModelByServiceIdLimit(serviceId, offset, limit, pattern);
        }
    }

    @Override
    public long getApiModelCountByProjectOrService(long serviceId, long projectId, String pattern) {
        if (serviceId == 0) {
            return apiModelDao.getApiModelCountByProjectPattern(projectId, pattern);
        } else {
            return apiModelDao.getApiModelCountByServicePattern(serviceId, pattern);
        }
    }

    @Override
    public List<ApiModel> findApiModelBySwaggerSync(long serviceId, long swaggerSync) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("swaggerSync", swaggerSync);
        params.put("serviceId", serviceId);
        return apiModelDao.getRecordsByField(params);
    }

    @Override
    public long updateApiModels(ApiModel apiModel) {
        return apiModelDao.update(apiModel);
    }

    @Override
    public ApiErrorCode checkAddApiModelParam(CreateApiModelDto createApiModelDto) {
        //判断ParamTypeId是否存在
        for (ApiParamDto apiParamDto : createApiModelDto.getParams()) {
            if (!apiParamTypeService.isApiParamTypeExists(apiParamDto.getParamTypeId())) {
                logger.info("创建/修改数据模型，paramTypeId不存在");
                return CommonApiErrorCode.InvalidParameter(String.valueOf(apiParamDto.getParamTypeId()), "ParamTypeId");
            }
        }

        List<ApiParamDto> apiParamDtos = createApiModelDto.getParams();
        if (!CollectionUtils.isEmpty(apiParamDtos) && apiParamDtos.size() != new HashSet<ApiParamDto>(apiParamDtos).size()) {
            logger.info("创建数据模型，存在两个相同的参数");
            return CommonApiErrorCode.RepeatedParamName;
        }

        if (StringUtils.isBlank(createApiModelDto.getDescription())) {
            logger.info("创建/修改数据模型，description为空");
            return CommonApiErrorCode.InvalidParameterValue(createApiModelDto.getDescription(), "Description");
        }

        //ModelName不能和nce_gateway_param_type中Header和Body中基本参数类型相同
        if (apiParamTypeService.listParamTypeInHeaderAndBodyButNotModel().contains(createApiModelDto.getModelName())) {
            return CommonApiErrorCode.InvalidParameter(createApiModelDto.getModelName(), "ModelName");
        }

//        //ModelName中的参数命名不能一样
//        if (apiParamTypeService.listParamTypeInHeaderAndBodyButNotModel().contains(createApiModelDto.getModelName())) {
//            logger.info("创建/修改数据模型，modelName不能和body或者header中名称相同");
//            return CommonErrorCode.InvalidParameterValueModelName(createApiModelDto.getModelName());
//        }

        return CommonApiErrorCode.Success;
    }
}
