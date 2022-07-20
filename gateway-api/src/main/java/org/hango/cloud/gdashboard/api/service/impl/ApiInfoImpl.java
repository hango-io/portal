package org.hango.cloud.gdashboard.api.service.impl;


import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.gdashboard.api.dao.ApiBodyDao;
import org.hango.cloud.gdashboard.api.dao.ApiDocumentStatusDao;
import org.hango.cloud.gdashboard.api.dao.ApiHeaderDao;
import org.hango.cloud.gdashboard.api.dao.ApiInfoDao;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.meta.ApiDocumentStatus;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.ServiceType;
import org.hango.cloud.gdashboard.api.meta.errorcode.ApiErrorCode;
import org.hango.cloud.gdashboard.api.meta.errorcode.CommonApiErrorCode;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IDubboParamService;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Modified hanjiahao
 * @Date: 创建时间: 2018/1/2 10:50.
 */
@Service
public class ApiInfoImpl implements IApiInfoService {

    private static Logger logger = LoggerFactory.getLogger(ApiInfoImpl.class);

    @Autowired
    private ApiInfoDao apiInfoDao;
    @Autowired
    private ApiHeaderDao apiHeaderDao;
    @Autowired
    private ApiBodyDao apiBodyDao;
    @Autowired
    private ApiDocumentStatusDao apiDocumentStatusDao;

    @Autowired
    private IApiParamTypeService apiParamTypeService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IDubboParamService dubboParamService;
    @Autowired
    private IGetProjectIdService projectIdService;

    @Override
    public long addApi(ApiInfo apiInfo) {
        return apiInfoDao.add(apiInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addDubboOrWebServiceApi(ApiInfo apiInfo, String type) {

        long apiId = apiInfoDao.add(apiInfo);

        //Dubbo服务下所有接口自动创建200/500的statusCode
        if (ServiceType.dubbo.name().equals(type)) {
            List<ApiStatusCode> apiStatusCodeList = new ArrayList<>();
            ApiStatusCode errorCode = new ApiStatusCode();
            errorCode.setStatusCode(500L);
            errorCode.setObjectId(apiId);
            errorCode.setErrorCode("DUBBO FAILED");
            errorCode.setMessage("DUBBO调用失败");
            errorCode.setDescription("DUBBO调用失败");
            errorCode.setType("api");
            apiStatusCodeList.add(errorCode);

            apiBodyService.addStatusCode(apiStatusCodeList);

        } else if (ServiceType.webservice.name().equals(type)) {
            List<ApiStatusCode> apiStatusCodeList = new ArrayList<>();
            ApiStatusCode errorCode = new ApiStatusCode();
            errorCode.setStatusCode(500L);
            errorCode.setObjectId(apiId);
            errorCode.setErrorCode("WebService FAILED");
            errorCode.setMessage("WebService调用失败");
            errorCode.setDescription("WebService调用失败");
            errorCode.setType("api");
            apiStatusCodeList.add(errorCode);

            apiBodyService.addStatusCode(apiStatusCodeList);

        }
        return apiId;

    }

    @Override
    public long addApiInfos(ApiInfoBasicDto apiInfoBasicDto, String type) {
        ApiInfo apiInfo = BeanUtil.copy(apiInfoBasicDto, ApiInfo.class);
        apiInfo.setCreateDate(System.currentTimeMillis());
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setStatus("0");
        apiInfo.setProjectId(projectIdService.getProjectId());
        String regex = apiInfo.getApiPath().replaceAll("\\{[^}]*\\}", "*");
        apiInfo.setRegex(regex);
        return addDubboOrWebServiceApi(apiInfo, type);
    }

    @Override
    public ApiErrorCode checkParamApiBasicDto(ApiInfoBasicDto apiInfoBasicDto) {
        //FIXME 拆分，服务serviceInfo能否在其他地方判断
//        if (serviceInfoService.getServiceByServiceId(apiInfoBasicDto.getServiceId()) == null){
//            logger.info("创建或修改API，服务id不存在");
//            return CommonApiErrorCode.NoSuchService;
//        }

        //判断document ID是否合法
        Long apiDocumentStatus = apiInfoBasicDto.getDocumentStatusId();
        List<ApiDocumentStatus> apiDocumentStatuses = apiParamTypeService.listApiDocumentStatus();
        List<Long> documentIds = new ArrayList<>();
        if (apiDocumentStatuses != null && apiDocumentStatuses.size() > 0) {
            apiDocumentStatuses.forEach(apiDocumentStatus1 -> {
                documentIds.add(apiDocumentStatus1.getId());
            });
        }
        if (!documentIds.contains(apiDocumentStatus)) {
            return CommonApiErrorCode.InvalidParameter(String.valueOf(apiDocumentStatus), "ApiDocumentStatus");
        }

        //同一服务不能存在相同的服务标识
        if (StringUtils.isNotBlank(apiInfoBasicDto.getAliasName()) &&
                !isExistAliasName(String.valueOf(apiInfoBasicDto.getServiceId()), apiInfoBasicDto.getAliasName(), apiInfoBasicDto.getId())) {
            logger.info("同一服务下不能存在相同的API标识,aliasName:{}", apiInfoBasicDto.getAliasName());
            return CommonApiErrorCode.AlreadyExistAliasName;
        }

        //判断API是否已经存在
        if (isRestfulApiExists(apiInfoBasicDto.getApiPath(), apiInfoBasicDto.getApiMethod(), apiInfoBasicDto.getServiceId(), apiInfoBasicDto.getId())) {
            logger.info("该API已经存在");
            return CommonApiErrorCode.ApiAlreadyExist;
        }
        return CommonApiErrorCode.Success;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addApiList(List<ApiInfo> apiInfos) {
        for (ApiInfo apiInfo : apiInfos) {
            addApi(apiInfo);
        }
    }

    @Override
    public ApiInfo getApi(String apiId) {
        try {
            return apiInfoDao.get(Long.parseLong(apiId));
        } catch (Exception e) {
            logger.warn("Id为{}的API 不存在", apiId);
//            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ApiInfo getApiById(long apiId) {
        return apiInfoDao.get(apiId);
    }

    @Override
    public ApiInfo getApiInfo(String apiPath, String method, long serviceId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("apiPath", apiPath);
        params.put("apiMethod", method);
        params.put("serviceId", serviceId);
        List<ApiInfo> apiInfoList = apiInfoDao.getRecordsByField(params);
        if (apiInfoList.size() != 0) {
            return apiInfoList.get(0);
        }
        return null;
    }

    @Override
    public boolean isApiExists(String action, String version, long serviceId, long apiId) {
        Map<String, Object> params = new HashMap<>();
        params.put("action", action);
        params.put("version", version);
        params.put("serviceId", serviceId);
        return isApiExists(apiId, params);
    }

    @Override
    public boolean isRestfulApiExists(String apiPath, String method, long serviceId, long apiId) {
        //1.去除path中带的参数
        if (apiPath.contains("?")) {
            String pathString[] = apiPath.split("\\?");
            apiPath = pathString[0];
        }
        //2.将path中的变量如{tenantId}替换成*
        String regex = apiPath.replaceAll("\\{[^}]*\\}", "*");

        Map<String, Object> params = new HashMap<>();
        params.put("regex", regex);
        params.put("apiMethod", method);
        params.put("serviceId", serviceId);

        return isApiExists(apiId, params);
    }

    @Override
    public boolean isApiExists(long apiId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", apiId);
        return apiInfoDao.getCountByFields(params) == 0 ? false : true;
    }


    @Override
    public List<ApiInfo> findAllApi() {
        return apiInfoDao.find();
    }

    @Override
    public List<ApiInfo> findAllApi(long projectId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("projectId", projectId);
        return apiInfoDao.getRecordsByField(params);
    }


    @Override
    public List<ApiInfo> findAllApiByServiceId(long serviceId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        return apiInfoDao.getRecordsByField(params);
    }

    @Override
    public List<ApiInfo> findAllApiBySwaggerSync(long serviceId, int swaggerSync) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        params.put("swaggerSync", swaggerSync);
        return apiInfoDao.getRecordsByField(params);
    }

    @Override
    public long getApiCountByServiceId(long serviceId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serviceId", serviceId);
        return apiInfoDao.getCountByFields(params);
    }


    @Override
    public void deleteApi(long apiId) {
        //删除dubbo param service
        dubboParamService.delete(apiId);
//        if (apiInfo != null && Const.WEBSERVICE.equals(apiInfo.getType())){
//            webServiceParamService.deleteWebserviceParam(apiId);
//        }
        //删除api对应的请求信息
        apiHeaderDao.deleteHeader(apiId);
        //删除api对应的响应信息
        apiBodyDao.deleteBody(apiId);
        //删除api status code
        apiBodyService.deleteStatusCode(apiId, Const.API);
        //删除api的基本信息
        apiInfoDao.deleteApi(apiId);
        //删除api对应的grpc_param, 未封装到这里来
    }

    @Override
    public void deleteApiHeaderBody(long apiId) {
        dubboParamService.delete(apiId);
        //删除api对应的请求信息
        apiHeaderDao.deleteHeader(apiId);
        //删除api对应的响应信息
        apiBodyDao.deleteBody(apiId);
        //删除api status code
        apiBodyService.deleteStatusCode(apiId, Const.API);
    }

    @Override
    public long updateApiStatus(String apiId, String status) {
        return apiInfoDao.update(Long.parseLong(apiId), status);
    }

    @Override
    public ApiDocumentStatus getApiDocumentStatusById(long id) {
        try {
            return apiDocumentStatusDao.get(id);
        } catch (Exception e) {
            logger.warn("查询API状态时，发生异常，可忽略");
//            e.printStackTrace();
        }
        return new ApiDocumentStatus();
    }

    @Override
    public ApiDocumentStatus getApiDocumentStatus(String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        List<ApiDocumentStatus> apiDocumentStatusList = apiDocumentStatusDao.getRecordsByField(params);
        if (apiDocumentStatusList.size() > 0) {
            return apiDocumentStatusList.get(0);
        }
        return new ApiDocumentStatus();
    }

    /**
     * 判断是否存在相同的服务标识
     * 不相同返回true，存在相同的返回false
     *
     * @param serviceId
     * @param aliasName
     * @param apiId
     * @return
     */
    @Override
    public boolean isExistAliasName(String serviceId, String aliasName, long apiId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", serviceId);
        params.put("aliasName", aliasName);
        if (apiId != 0) {
            List<ApiInfo> apiInfos = apiInfoDao.getRecordsByField(params);
            if (apiInfos != null && apiInfos.size() > 0) {
                if (apiId != apiInfos.get(0).getId()) {
                    return false;
                }
            }

        } else {
            if (apiInfoDao.getCountByFields(params) > 0) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean updateApi(ApiInfo apiInfo) {
        if (null == apiInfo) {
            return false;
        }
        return 1 == apiInfoDao.update(apiInfo);
    }

    @Override
    public long addApiWithId(ApiInfo apiInfo) {
        return apiInfoDao.addWithId(apiInfo);
    }

    /**
     * 大小驼峰方式转换成下划线命名
     *
     * @param name
     * @return
     */
    public String camel2UnderScode(String name) {
        StringBuilder result = new StringBuilder();
        if (name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());

            for (int i = 1; i < name.length(); i++) {
                String str = name.substring(i, i + 1);
                if (str.equals(str.toUpperCase()) && !Character.isDigit(str.charAt(0))) {
                    result.append("_");
                }
                result.append(str.toLowerCase());
            }
        }
        return result.toString();
    }

    private boolean isApiExists(long apiId, Map<String, Object> params) {
        if (apiId == 0) {
            //添加的时候判断API是否存在
            return apiInfoDao.getCountByFields(params) == 0 ? false : true;
        } else {
            //修改的时候判断API是否存在
            List<ApiInfo> apiInfoList = apiInfoDao.getRecordsByField(params);
            if (apiInfoList.size() == 0) {
                return false;
            } else {
                //判断是否是自身
                return apiInfoList.get(0).getId() == apiId ? false : true;
            }
        }
    }


    @Override
    public List<ApiInfo> findAllApiByProjectLimit(long projectId, long serviceId, long apiDocumentId, String pattern, long offset, long limit) {
        if (serviceId == 0) {
            return apiInfoDao.findAllApiInfoByProjectLimit(projectId, apiDocumentId, pattern, offset, limit);
        } else {
            return apiInfoDao.findAllApiInfoByServiceLimit(serviceId, apiDocumentId, pattern, offset, limit);
        }
    }

    @Override
    public long getCountByProjectOrService(long projectId, long serviceId, long apiDocumentId, String pattern) {
        if (serviceId == 0) {
            return apiInfoDao.getApiCountByProject(projectId, apiDocumentId, pattern);
        } else {
            return apiInfoDao.getApiCountByService(serviceId, apiDocumentId, pattern);
        }
    }

    @Override
    public ApiInfo getApiInfoByApiPathAndService(String apiPath, String apiMethod, long serviceId) {
        Map<String, Object> params = new HashMap<>();
        //1.去除path中带的参数
        if (apiPath.contains("?")) {
            String pathString[] = apiPath.split("\\?");
            apiPath = pathString[0];
        }
        //2.将path中的变量如{tenantId}替换成*，构造regex
        String regex = apiPath.replaceAll("\\{[^}]*\\}", "*");

        params.put("regex", regex);
        params.put("apiMethod", apiMethod);
        params.put("serviceId", serviceId);
        List<ApiInfo> apiInfos = apiInfoDao.getRecordsByField(params);
        if (!CollectionUtils.isEmpty(apiInfos)) {
            return apiInfos.get(0);
        }
        return null;
    }

}
