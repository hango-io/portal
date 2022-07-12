package org.hango.cloud.gdashboard.api.service.impl;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.hango.cloud.gdashboard.api.dao.ApiDocumentStatusDao;
import org.hango.cloud.gdashboard.api.dao.ApiParamTypeDao;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.meta.ApiParamType;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/1/2 16:20.
 */
@Service
public class ApiParamTypeServiceImpl implements IApiParamTypeService {

    @Autowired
    private ApiParamTypeDao apiParamTypeDao;
    @Autowired
    private ApiDocumentStatusDao apiDocumentStatusDao;
    @Autowired
    private IApiModelService apiModelService;

    //设置参数类型的缓存
    private LoadingCache<Long, Optional<ApiParamType>> apiParamTypeCache = CacheBuilder.newBuilder().maximumSize(5000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, Optional<ApiParamType>>() {

                @Override
                public Optional<ApiParamType> load(Long paramTypeId) throws Exception {
                    if (paramTypeId <= 0) {
                        return Optional.fromNullable(null);
                    }
                    Map<String, Object> head = new HashMap<>();
                    head.put("id", paramTypeId);
                    List<ApiParamType> modelParamTypeList = apiParamTypeDao.getRecordsByField(head);

                    if (modelParamTypeList.size() > 0) {
                        return Optional.fromNullable(modelParamTypeList.get(0));
                    } else {
                        return Optional.fromNullable(null);
                    }
                }

            });

    @Override
    public List<String> findAll(String location) {
        List<ApiParamType> list = apiParamTypeDao.findAll(location);
        List<String> paramType = new ArrayList<>();
        for (ApiParamType apiParamType : list) {
            paramType.add(apiParamType.getParamType());
        }
        return paramType;
    }

    @Override
    public List<ApiParamType> listParamTypeInHeader() {
        Map<String, Object> head = new HashMap<>();
        //TODO 拆分API服务时将常量抽象出来
        head.put("location", "HEADER");
        List<ApiParamType> paramTypeList = apiParamTypeDao.getRecordsByField(head);
        return paramTypeList;
    }

    @Override
    public List<ApiParamType> listParamTypeInBody() {
        Map<String, Object> head = new HashMap<>();

        //TODO 拆分API服务时将常量抽象出来
        head.put("location", "BODY");
        List<ApiParamType> basicParamTypeList = apiParamTypeDao.getRecordsByField(head);

//        head.put("location", "MODEL");
//        List<ApiParamType> modelList = apiParamTypeDao.getRecordsByField(head);

        List<ApiParamType> paramTypeList = new ArrayList<>();
        paramTypeList.addAll(basicParamTypeList);
//        paramTypeList.addAll(modelList);

        return paramTypeList;
    }

    @Override
    public List<ApiParamType> listParamTypeInHeaderAndBodyButNotModel() {

        Map<String, Object> head = new HashMap<>();

        //TODO 拆分API服务时将常量抽象出来
        head.put("location", "BODY");
        List<ApiParamType> basicParamTypeList = apiParamTypeDao.getRecordsByField(head);

        head.put("location", "HEADER");
        List<ApiParamType> modelList = apiParamTypeDao.getRecordsByField(head);

        List<ApiParamType> paramTypeList = new ArrayList<>();
        paramTypeList.addAll(basicParamTypeList);
        paramTypeList.addAll(modelList);

        return paramTypeList;
    }

    @Override
    public List<ApiParamType> listModleParamType(List<Long> modelIdList) {
        List<ApiParamType> list = new ArrayList<>();
        for (long modelId : modelIdList) {

            Map<String, Object> head = new HashMap<>();
            head.put("modelId", modelId);
            List<ApiParamType> modelParamTypeList = apiParamTypeDao.getRecordsByField(head);
            if (modelParamTypeList.size() > 0) {
                list.add(modelParamTypeList.get(0));
            }

        }
        return list;
    }

    @Override
    public ApiParamType listModleParamType(long modelId) {
        Map<String, Object> head = new HashMap<>();
        head.put("modelId", modelId);
        List<ApiParamType> modelParamTypeList = apiParamTypeDao.getRecordsByField(head);
        if (modelParamTypeList.size() > 0) {
            return modelParamTypeList.get(0);
        }
        return null;
    }

    @Override
    public List<ApiDocumentStatus> listApiDocumentStatus() {
        return apiDocumentStatusDao.findAll();
    }


    @Override
    public boolean isApiParamTypeExists(long paramTypeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", paramTypeId);
        return apiParamTypeDao.getCountByFields(params) == 0 ? false : true;

    }

    /**
     * 从缓存读取
     *
     * @param paramTypeId
     * @return
     */
    @Override
    public ApiParamType listApiParamType(long paramTypeId) {

        return apiParamTypeCache.getUnchecked(paramTypeId).orNull();
    }

    @Override
    public ApiParamType listApiParamType(String paramType) {
        Map<String, Object> head = new HashMap<>();
        head.put("paramType", paramType);
        List<ApiParamType> modelParamTypeList = apiParamTypeDao.getRecordsByField(head);
        if (modelParamTypeList.size() > 0) {
            return modelParamTypeList.get(0);
        }
        return null;
    }

    @Override
    public ApiParamType listApiParamTypeByModelId(String paramType, long modelId) {
        Map<String, Object> head = new HashMap<>();
        head.put("paramType", paramType);
        head.put("modelId", modelId);
        List<ApiParamType> modelParamTypeList = apiParamTypeDao.getRecordsByField(head);
        if (modelParamTypeList.size() > 0) {
            return modelParamTypeList.get(0);
        }
        return null;
    }

    @Override
    public long generateExactByService(String type, long serviceId) {
        ApiParamType apiParamType = listApiParamTypeByModelId(type, 0);
        if (apiParamType != null) {
            return apiParamType.getId();
        }
        ApiModel apiModel = apiModelService.getApiModelByServiceIdAndModelName(serviceId, type);
        if (apiModel != null) {
            apiParamType = listApiParamTypeByModelId(type, apiModel.getId());
            if (apiParamType != null) {
                return apiParamType.getId();
            }
        }
        return 0;
    }


}
