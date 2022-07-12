package org.hango.cloud.gdashboard.api.service.impl;

import org.hango.cloud.gdashboard.api.dao.ApiBodyDao;
import org.hango.cloud.gdashboard.api.dao.ApiStatusCodeDao;
import org.hango.cloud.gdashboard.api.dto.ApiBodyBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiBodysDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodeBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiStatusCodesDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.hango.cloud.gdashboard.api.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiBodyServiceImpl implements IApiBodyService {

    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private ApiBodyDao apiBodyDao;
    @Autowired
    private ApiStatusCodeDao apiStatusCodeDao;
    @Autowired
    private IApiInfoService apiInfoService;

    @Override
    public List<ApiBody> generateApiBodyFromApiBodyList(ApiBodysDto apiBodysDto, String type) {
        List<ApiBody> apiBodies = new ArrayList<>();
        long apiId = apiBodysDto.getId();
        List<ApiBodyBasicDto> apiBodyBasicDtos = apiBodysDto.getApiBodyBasicDtoList();
        if (!CollectionUtils.isEmpty(apiBodyBasicDtos)) {
            apiBodies = BeanUtil.copyList(apiBodyBasicDtos, ApiBody.class);
            apiBodies.forEach(apiBody -> {
                apiBody.setApiId(apiId);
                apiBody.setType(type);
                apiBody.setParamType(apiParamTypeService.listApiParamType(apiBody.getParamTypeId()).getParamType());
                //存在array类型
                if (apiBody.getArrayDataTypeId() != 0) {
                    apiBody.setArrayDataTypeName(apiParamTypeService.listApiParamType(apiBody.getArrayDataTypeId()).getParamType());
                }
                apiBody.setCreateDate(System.currentTimeMillis());
                apiBody.setModifyDate(System.currentTimeMillis());
            });
        }
        return apiBodies;
    }


    @Override
    public List<ApiStatusCode> generateApiStatusCodeFromCodeList(ApiStatusCodesDto apiStatusCodesDto) {
        List<ApiStatusCode> apiStatusCodes = new ArrayList<>();
        long apiId = apiStatusCodesDto.getId();
        List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtos = apiStatusCodesDto.getApiStatusCodeBasicDtoList();
        if (!CollectionUtils.isEmpty(apiStatusCodeBasicDtos)) {
            apiStatusCodes = BeanUtil.copyList(apiStatusCodeBasicDtos, ApiStatusCode.class);
            apiStatusCodes.forEach(apiStatusCode -> {
                apiStatusCode.setObjectId(apiId);
                apiStatusCode.setType("api");
                apiStatusCode.setCreateDate(System.currentTimeMillis());
                apiStatusCode.setModifyDate(System.currentTimeMillis());
            });
        }
        return apiStatusCodes;
    }

    @Override
    public ApiErrorCode checkApiBodyBasicInfo(ApiBodysDto apiBodysDto) {
        ApiInfo apiInfo = apiInfoService.getApiById(apiBodysDto.getId());
        if (apiInfo == null) {
            return CommonApiErrorCode.NoSuchApiInterface;
        }
        //对参数类型以及数组类型是否合法进行校验
        if (!CollectionUtils.isEmpty(apiBodysDto.getApiBodyBasicDtoList())) {
            for (ApiBodyBasicDto apiBodyBasicDto : apiBodysDto.getApiBodyBasicDtoList()) {
                if (apiParamTypeService.listApiParamType(apiBodyBasicDto.getParamTypeId()) == null) {
                    return CommonApiErrorCode.NoSuchParamType;
                }
                if (apiBodyBasicDto.getArrayDataTypeId() != 0 && apiParamTypeService.listApiParamType(apiBodyBasicDto.getArrayDataTypeId()) == null) {
                    return CommonApiErrorCode.NoSuchArrayDataType;
                }
                String paramType = apiParamTypeService.listApiParamType(apiBodyBasicDto.getParamTypeId()).getParamType();
                if (Const.BLANK_ARRAY_CONST.equals(apiBodyBasicDto.getParamName()) && !"Array".equalsIgnoreCase(paramType)) {
                    return CommonApiErrorCode.InvalidConstBlank(Const.BLANK_ARRAY_CONST);
                }
                if (Const.BLANK_CONST.equals(apiBodyBasicDto.getParamName()) && "Array".equalsIgnoreCase(paramType)) {
                    return CommonApiErrorCode.InvalidConstBlank(Const.BLANK_CONST);
                }
            }

            //创建数据Body，存在相同的paramName
            List<String> apiBodyParamName = apiBodysDto.getApiBodyBasicDtoList().stream().map(ApiBodyBasicDto::getParamName).collect(Collectors.toList());
            if (apiBodyParamName.size() != new HashSet<>(apiBodyParamName).size()) {
                return CommonApiErrorCode.RepeatedParamName;
            }
        }
        return CommonApiErrorCode.Success;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addStatusCodes(List<ApiStatusCode> apiStatusCodes, long objectId, String type) {
        //先删除，后添加
        apiStatusCodeDao.delete(objectId, type);

        for (ApiStatusCode apiStatusCode : apiStatusCodes) {
            apiStatusCodeDao.add(apiStatusCode);
        }
        return apiStatusCodes.size();
    }

    @Override
    public List<ApiStatusCode> listStatusCode(long objectId, String type) {

        //查询的对象是服务
        Map<String, Object> params = new HashMap<>();
        List<ApiStatusCode> apiStatusCodeList = new ArrayList<>();

        params.put("objectId", objectId);
        params.put("type", type);

        if (type.equals("service")) {
            apiStatusCodeList = apiStatusCodeDao.getRecordsByField(params);
        } else {
            //查询对象是API
            ApiInfo apiInfo = apiInfoService.getApiById(objectId);
            if ("ACTION".equals(apiInfo.getType())) {
                //Action风格需要增加
                long serviceId = apiInfo.getServiceId();
                params.put("objectId", serviceId);
                params.put("type", "service");
                apiStatusCodeList.addAll(apiStatusCodeDao.getRecordsByField(params));
                params.put("objectId", objectId);
                params.put("type", type);
                apiStatusCodeList.addAll(apiStatusCodeDao.getRecordsByField(params));
            } else {
                apiStatusCodeList = apiStatusCodeDao.getRecordsByField(params);
            }
        }
        return apiStatusCodeList;
    }

    @Override
    public List<ApiStatusCode> listStatusCode(long apiId) {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", apiId);
        params.put("type", "api");
        return apiStatusCodeDao.getRecordsByField(params);
    }


    @Override
    public long addStatusCode(ApiStatusCode apiStatusCode) {
        return apiStatusCodeDao.add(apiStatusCode);
    }

    @Override
    public List<Long> addStatusCode(List<ApiStatusCode> apiStatusCodeList) {
        List<Long> longList = new ArrayList<>();
        for (ApiStatusCode apiStatusCode : apiStatusCodeList) {
            longList.add(apiStatusCodeDao.add(apiStatusCode));
        }
        return longList;
    }

    @Override
    public void deleteStatusCode(long objectId, String type) {
        apiStatusCodeDao.delete(objectId, type);
    }


    @Override
    public long addBody(ApiBody apiBody) {
        return apiBodyDao.add(apiBody);
    }

    @Override
    public List<ApiBody> getBody(long apiId, String type) {
        return apiBodyDao.getBody(apiId, type);
    }

    @Override
    public void deleteBody(long apiId, String type) {
        apiBodyDao.deleteBody(apiId, type);
    }

    @Override
    public void deleteBody(long apiId) {
        apiBodyDao.deleteBody(apiId);
    }

    @Override
    public void deleteBodyParam(long paramId) {
        apiBodyDao.delete(paramId);
    }

    @Override
    public List<Long> addBody(List<ApiBody> apiBodyList) {
        List<Long> longList = new ArrayList<>();
        for (ApiBody apiBody : apiBodyList) {
            longList.add(apiBodyDao.add(apiBody));
        }
        return longList;
    }

    @Override
    public boolean getBodyParam(String paramName, String type, String apiId) {
        List<ApiBody> apiBodyList = apiBodyDao.getBodyParam(paramName, type, Long.parseLong(apiId));
        return apiBodyList.size() == 0 ? false : true;
    }

    @Override
    public List<ApiBody> listBodyParam() {
        List<ApiBody> apiBodyList = apiBodyDao.findAll();
        return apiBodyList;
    }

    @Override
    public long updateBodyParam(ApiBody apiBody) {
        return apiBodyDao.update(apiBody);
    }

    @Override
    public List<ApiBody> getBody(String apiId) {
        return apiBodyDao.getBodyByApiId(Long.parseLong(apiId));
    }
}
