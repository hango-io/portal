package org.hango.cloud.gdashboard.api.service.impl;

import org.hango.cloud.gdashboard.api.dao.ApiHeaderDao;
import org.hango.cloud.gdashboard.api.dto.ApiHeaderBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiHeadersDto;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ApiHeaderServiceImpl implements IApiHeaderService {

    private static Logger logger = LoggerFactory.getLogger(ApiHeaderServiceImpl.class);

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private ApiHeaderDao apiHeaderDao;

    @Override
    public ApiErrorCode checkCreateOrUpdateHeader(ApiHeadersDto apiHeadersDto) {
        ApiInfo apiInfo = apiInfoService.getApiById(apiHeadersDto.getId());
        if (apiInfo == null) {
            logger.info("创建 api header，不存在api id");
            return CommonApiErrorCode.NoSuchApiInterface;
        } else {
            List<ApiHeaderBasicDto> apiHeaderBasicDtos = apiHeadersDto.getApiHeaderBasicDtoList();
            Set<String> tmpSet = new HashSet<>();
            if (!CollectionUtils.isEmpty(apiHeaderBasicDtos)) {
                apiHeaderBasicDtos.forEach(apiHeaderBasicDto -> {
                    tmpSet.add(apiHeaderBasicDto.getParamName());
                });
            }
            if (tmpSet.size() < apiHeaderBasicDtos.size()) {
                logger.info("创建api Header，存在重复的paramName");
                return CommonApiErrorCode.RepeatedParamName;
            }
        }
        return CommonApiErrorCode.Success;
    }

    @Override
    public List<ApiHeader> generateApiHeaderFromApiHeaderList(ApiHeadersDto apiHeadersDto, String type) {
        List<ApiHeader> apiHeaders = new ArrayList<>();
        long apiId = apiHeadersDto.getId();
        List<ApiHeaderBasicDto> apiHeaderBasicDtos = apiHeadersDto.getApiHeaderBasicDtoList();
        if (!CollectionUtils.isEmpty(apiHeaderBasicDtos)) {
            apiHeaders = BeanUtil.copyList(apiHeaderBasicDtos, ApiHeader.class);
            apiHeaders.forEach(apiHeader -> {
                apiHeader.setApiId(apiId);
                apiHeader.setType(type);
                apiHeader.setCreateDate(System.currentTimeMillis());
                apiHeader.setModifyDate(System.currentTimeMillis());
            });
        }
        return apiHeaders;
    }

    @Override
    public long addHeader(ApiHeader apiHeader) {
        return apiHeaderDao.add(apiHeader);
    }

    @Override
    public List<ApiHeader> getHeader(long apiId, String type) {
        return apiHeaderDao.getHeader(apiId, type);
    }

    @Override
    public void deleteHeaderParam(long paramId) {
        apiHeaderDao.deleteHeaderParam(paramId);
    }

    @Override
    public List<Long> addHeader(List<ApiHeader> apiHeaderList) {
        List<Long> longList = new ArrayList<>();
        for (ApiHeader apiHeader : apiHeaderList) {
            longList.add(apiHeaderDao.add(apiHeader));
        }
        return longList;
    }

    @Override
    public boolean getHeaderParam(String paramName, String type, String apiId) {
        List<ApiHeader> apiHeaderList = apiHeaderDao.getHeaderParam(paramName, type, Long.parseLong(apiId));
        return apiHeaderList.size() == 0 ? false : true;
    }

    @Override
    public void deleteHeader(long apiId, String type) {
        apiHeaderDao.deleteHeader(apiId, type);
    }

    @Override
    public void deleteHeader(long apiId) {
        apiHeaderDao.deleteHeader(apiId);
    }

    @Override
    public List<ApiHeader> getHeader(String apiId) {
        return apiHeaderDao.getHeaderByApiId(Long.parseLong(apiId));
    }
}
