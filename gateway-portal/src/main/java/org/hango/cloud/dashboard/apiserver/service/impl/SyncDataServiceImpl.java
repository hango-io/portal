package org.hango.cloud.dashboard.apiserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.dto.MicroServiceDto;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.meta.SyncServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.ISyncDataService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.dto.ApiListDto;
import org.hango.cloud.gdashboard.api.dto.CreateApiModelDto;
import org.hango.cloud.gdashboard.api.meta.ApiBody;
import org.hango.cloud.gdashboard.api.meta.ApiHeader;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.meta.ApiStatusCode;
import org.hango.cloud.gdashboard.api.meta.DubboParamInfo;
import org.hango.cloud.gdashboard.api.service.IApiBodyService;
import org.hango.cloud.gdashboard.api.service.IApiHeaderService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelExportService;
import org.hango.cloud.gdashboard.api.service.IApiParamTypeService;
import org.hango.cloud.gdashboard.api.service.IDubboParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/23
 */
@Service
public class SyncDataServiceImpl implements ISyncDataService {

    private static final Logger logger = LoggerFactory.getLogger(SyncDataServiceImpl.class);
    private final String split = "_";
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private ApiServerConfig apiServerConfig;
    @Autowired
    private IApiInfoService apiInfoService;
    @Autowired
    private IApiHeaderService apiHeaderService;
    @Autowired
    private IApiBodyService apiBodyService;
    @Autowired
    private IDubboParamService dubboParamService;
    @Autowired
    private IApiModelExportService apiModelExportService;
    @Autowired
    private IApiParamTypeService apiParamTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncServiceInfo(List<SyncServiceInfo> syncList) {

        if (CollectionUtils.isEmpty(syncList)) {
            logger.info("acquire syncList is empty");
        }
        for (SyncServiceInfo serviceInfo : syncList) {
            long serviceId = serviceInfo.getId();
            switch (SyncServiceInfo.PreSyncStatusEnum.get(serviceInfo.getPreSyncStatus())) {
                case CONFLICT_SERVICE:
                    logger.info("Service PreSyncStatus is CONFLICT, Should't be Sync ,ServiceName = {}", serviceInfo.getDisplayName());
                    break;
                case CONVERT_SERVICE:
                    serviceInfoService.updateService(serviceInfo);
                    break;
                case NEW_SERVICE:
                    serviceId = serviceInfoService.add(serviceInfo);
                    break;
                default:
                    logger.info("Service PreSyncStatus is UNDEFINE, Should't be Sync ,ServiceName = {}", serviceInfo.getDisplayName());
                    break;
            }

            //获取nsf服务模型信息
            List<CreateApiModelDto> apiApiModelFromMeta = getApiApiModelFromMeta(serviceInfo.getExtServiceId());
            if (!CollectionUtils.isEmpty(apiApiModelFromMeta)) {
                apiModelExportService.addApiModel(apiApiModelFromMeta, serviceId);
            }
        }
        logger.info("sync serviceInfo finished!");
    }

    @Override
    public List<SyncServiceInfo> getSyncListByProjectId(Long projectId) {
        List<MicroServiceDto> microServiceInfoFromMeta = getSyncServiceInfoFromMeta(projectId);
        List<ServiceInfo> originList = serviceInfoService.findAllServiceByProjectId(projectId);
        List<SyncServiceInfo> syncList = new ArrayList<>();
        Map<String, ServiceInfo> originMap = originList.stream().collect(Collectors.toMap(ServiceInfo::getDisplayName, Function.identity()));
        //处理元数据存在的数据
        for (MicroServiceDto microServiceDto : microServiceInfoFromMeta) {
            //以双方服务名为判断条件
            ServiceInfo serviceInfo = originMap.get(microServiceDto.getName());
            //数据未冲突情况 : 元数据存在 , 本地不存在 , 同步服务 --> preSyncStatus=0(新服务) SyncStatus=1(同步)
            //将数据库数据转换为SyncServiceInfo
            SyncServiceInfo syncServiceInfo = castToSyncServiceInfo(microServiceDto, serviceInfo);
            syncList.add(syncServiceInfo);
            if (serviceInfo == null) {
                logger.info("数据未冲突情况 : 元数据存在 , 本地不存在 , 同步服务 --> preSyncStatus=0(新服务) SyncStatus=1(同步),ServiceName={}", syncServiceInfo.getDisplayName());
                continue;
            }
            //以下为数据冲突情况，慎重改动
            originMap.remove(microServiceDto.getName());
            //数据冲突情况 : 本地存在 , 为本地创建 , 元数据存在 , 保持不变 --> preSyncStatus=2(冲突服务) SyncStatus=0(本地创建)
            if (serviceInfo.getSyncStatus() == 0) {
                syncServiceInfo.setPreSyncStatus(SyncServiceInfo.PreSyncStatusEnum.CONFLICT_SERVICE.getPreSyncStatus());
                logger.info("数据冲突情况 : 本地存在 , 为本地创建 , 元数据存在 , 保持不变 --> preSyncStatus=2(冲突服务) SyncStatus=0(本地创建),ServiceName={}", syncServiceInfo.getDisplayName());
                continue;
            }
            //数据冲突情况 : 本地存在 , 为同步数据 , 元数据存在 , 修正数据 --> preSyncStatus=1(覆盖服务) SyncStatus=1(同步服务)
            syncServiceInfo.setPreSyncStatus(SyncServiceInfo.PreSyncStatusEnum.CONVERT_SERVICE.getPreSyncStatus());
            syncServiceInfo.setSyncStatus(1);
            logger.info("数据冲突情况 : 本地存在 , 为同步数据 , 元数据存在 , 修正数据 --> preSyncStatus=1(覆盖服务) SyncStatus=1(同步服务),ServiceName={}", syncServiceInfo.getDisplayName());
        }
        //处理本地存在的数据
        for (ServiceInfo serviceInfo : originMap.values()) {
            //数据冲突情况 : 本地存在 , 为同步数据 , 元数据不存在 , 修正数据 --> preSyncStatus=1(覆盖服务) SyncStatus=2(失步服务)
            if (serviceInfo.getSyncStatus() != 0) {
                serviceInfo.setSyncStatus(2);
                syncList.add(new SyncServiceInfo(serviceInfo, SyncServiceInfo.PreSyncStatusEnum.CONVERT_SERVICE.getPreSyncStatus()));
                logger.info("本地数据存在该服务 ，元数据不存在该服务 ： 设置为失步状态,ServiceName={}", serviceInfo.getDisplayName());
            }
        }
        return syncList;
    }

    @Override
    public Map<String, List<String>> getServiceSyncStatistics(List<SyncServiceInfo> syncServiceInfoList) {
        return syncServiceInfoList.stream().collect(Collectors.groupingBy(
                sync -> SyncServiceInfo.PreSyncStatusEnum.getName(sync.getPreSyncStatus())
                , Collectors.mapping(SyncServiceInfo::getDisplayName, Collectors.toList())));
    }

    @Override
    public List<MicroServiceDto> getServiceInfoFromMetaService(Long projectId) {
        Map<String, String> queryStringMap = new HashMap<>();
        queryStringMap.put("Action", "GetHttpServiceListForGateway");
        queryStringMap.put("Version", "2018-11-1");
        queryStringMap.put("ProjectId", String.valueOf(projectId));
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(ProjectTraceHolder.PROJECT_TRACE_ID, String.valueOf(projectId));
        headerMap.put(ProjectTraceHolder.TENANT_TRACE_ID, String.valueOf(ProjectTraceHolder.getTenantId()));
        headerMap.put(UserPermissionHolder.USER_PERMISSION, String.valueOf(UserPermissionHolder.getJwt()));
        headerMap.put(UserPermissionHolder.USER_ACCOUNTID, String.valueOf(UserPermissionHolder.getAccountId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getNsfMetaUrl() + "/api/metadata", queryStringMap, null, headerMap, Const.GET_METHOD);
        if (httpClientResponse == null || HttpStatus.OK.value() != httpClientResponse.getStatusCode()) {
            logger.error("acquire serviceInfo from meta-service failed! projectId = {}", projectId);
            return Collections.emptyList();
        }
        String result = httpClientResponse.getResponseBody();
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject == null) {
            logger.error("acquire serviceInfo from meta-service failed! projectId = {},response = {}", projectId, result);
            return Collections.emptyList();
        }
        String meta = jsonObject.getString("Result");
        List<MicroServiceDto> microServiceDtoList = JSON.parseArray(meta, MicroServiceDto.class);

        if (CollectionUtils.isEmpty(microServiceDtoList)) {
            logger.error("acquire serviceInfo from meta-service failed! projectId = {},response = {},meta = {}", projectId, result, meta);
            return Collections.emptyList();
        }
        microServiceDtoList.stream().forEach(r -> r.setServiceType(ServiceType.http.name()));
        return microServiceDtoList;
    }

    @Override
    public Integer syncApiInfo(Long projectId) {
        Integer totalCount = NumberUtils.INTEGER_ZERO;
        if (null == projectId) {
            logger.info("sync serviceInfo error ,projectId is null");
            return totalCount;
        }
        List<ServiceInfo> allServiceByProjectId = serviceInfoService.findAllServiceByProjectId(projectId);
        return syncApiInfo(allServiceByProjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer syncApiInfo(List<? extends ServiceInfo> serviceInfoList) {
        if (CollectionUtils.isEmpty(serviceInfoList)) {
            logger.info("Service List is empty !");
        }
        List<ApiInfo> syncList = new ArrayList<>();
        for (ServiceInfo serviceInfo : serviceInfoList) {
            //本地数据 ： 服务下所有API皆为本地数据
            if (serviceInfo.getSyncStatus() == 0) {
                logger.info("{} 服务为本地创建 , 若NSF存在同名服务 , 不同步NSF同名服务下的API", serviceInfo.getDisplayName());
                continue;
            }
            //Dubbo服务由于双方Dubbo API定义不同，暂不同步Dubbo API
            if (!ServiceType.http.name().equals(serviceInfo.getServiceType())) {
                logger.info("{} 不为为http服务, 由于双方Dubbo API定义不同 , 暂不同步Dubbo API", serviceInfo.getDisplayName());
                continue;
            }
            //失步数据 ：失步服务下所有API皆为失步API
            if (serviceInfo.getSyncStatus() == 2) {
                logger.info("{} 服务为本地创建 , 若NSF存在同名服务 , 不同步NSF同名服务下的API", serviceInfo.getDisplayName());
                List<ApiInfo> allApiByServiceId = apiInfoService.findAllApiByServiceId(serviceInfo.getId());
                for (ApiInfo apiInfo : allApiByServiceId) {
                    apiInfo.setSyncStatus(2);
                    syncList.add(apiInfo);
                }
                continue;
            }
            //获取同步数据
            List<ApiInfo> syncListByService = getSyncListByService(serviceInfo, serviceInfo.getProjectId());
            if (CollectionUtils.isEmpty(syncListByService)) {
                logger.info("acquire api info list is empty ! serviceId={}", serviceInfo.getExtServiceId());
                continue;
            }
            syncList.addAll(syncListByService);
        }
        //保存数据
        saveSyncApiData(syncList);
        return syncList.size();
    }

    /**
     * 保存同步数据至数据库
     *
     * @param apiInfoList
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSyncApiData(List<ApiInfo> apiInfoList) {
        if (CollectionUtils.isEmpty(apiInfoList)) {
            logger.info("no api data be saved into database");
            return;
        }
        for (ApiInfo apiInfo : apiInfoList) {
            long id = apiInfo.getId();
            //数据库操作
            Map<String, Object> apiDetailsFromKnowledge = getApiDetailsFromKnowledge(apiInfo.getExtApiId());
            List<ApiHeader> apiHeaderList = JSON.parseArray(String.valueOf(apiDetailsFromKnowledge.get(Const.POSITION_HEADER)), ApiHeader.class);
            List<ApiBody> apiBodyList = JSON.parseArray(String.valueOf(apiDetailsFromKnowledge.get(Const.POSITION_BODY)), ApiBody.class);
            List<ApiStatusCode> apiStatusCodeList = JSON.parseArray(String.valueOf(apiDetailsFromKnowledge.get(Const.STATUS_CODE)), ApiStatusCode.class);
            List<DubboParamInfo> dubboParamInfoList = JSON.parseArray(String.valueOf(apiDetailsFromKnowledge.get(Const.POSITION_DUBBO_PARAM)), DubboParamInfo.class);
            String requestExample = (String) apiDetailsFromKnowledge.get(Const.REQUEST_EXAMPLE);
            String responseExample = (String) apiDetailsFromKnowledge.get(Const.RESPONSE_EXAMPLE);
            apiInfo.setRequestExampleValue(requestExample);
            apiInfo.setResponseExampleValue(responseExample);
            if (id == 0L) {
                id = apiInfoService.addApi(apiInfo);
            } else {
                apiInfoService.updateApi(apiInfo);
            }
            if (apiInfo.getSyncStatus() == 2) {
                continue;
            }
            //删除api对应的请求信息
            apiHeaderService.deleteHeader(id);
            //删除api对应的响应信息
            apiBodyService.deleteBody(id);
            apiBodyService.deleteStatusCode(id, Const.API);

            BeanUtil.replaceValueWithBeans(apiHeaderList, Const.API_ID, id);
            //处理body信息
            if (!CollectionUtils.isEmpty(apiBodyList)) {
                for (ApiBody apiBody : apiBodyList) {
                    apiBody.setApiId(id);
                    apiBody.setParamTypeId(apiParamTypeService.generateExactByService(apiBody.getParamType(), apiInfo.getServiceId()));
                    apiBody.setArrayDataTypeId(apiParamTypeService.generateExactByService(apiBody.getArrayDataTypeName(), apiInfo.getServiceId()));
                }
            }
            BeanUtil.replaceValueWithBeans(apiStatusCodeList, Const.OBJECT_ID, id);
            BeanUtil.replaceValueWithBeans(dubboParamInfoList, Const.API_ID, id);

            if (!CollectionUtils.isEmpty(apiHeaderList)) {
                apiHeaderService.addHeader(apiHeaderList);
            }
            if (!CollectionUtils.isEmpty(apiBodyList)) {
                apiBodyService.addBody(apiBodyList);
            }
            if (!CollectionUtils.isEmpty(apiStatusCodeList)) {
                apiBodyService.addStatusCode(apiStatusCodeList);
            }
            //dubbo数据的处理只能放在最后
            if (!CollectionUtils.isEmpty(dubboParamInfoList)) {
                dubboParamService.batchAdd(dubboParamInfoList);
            }
        }
    }

    /**
     * 从知识库获取API数据
     *
     * @param extServiceId
     * @return
     */
    private List<ApiListDto> getApiInfoFromKnowledge(Long extServiceId) {
        logger.info("start acquire apiInfo from knowledge, extServiceId={}", extServiceId);
        Map<String, String> queryStringMap = new HashMap<>();
        queryStringMap.put("Action", "DescribeApiListByLimit");
        queryStringMap.put("Version", "2018-08-09");
        queryStringMap.put("ServiceId", String.valueOf(extServiceId));
        queryStringMap.put("Limit", "1000");
        Map<String, String> headerMap = new HashMap<>();
        long projectId = ProjectTraceHolder.getProId();
        headerMap.put(ProjectTraceHolder.PROJECT_TRACE_ID, String.valueOf(projectId));
        headerMap.put(ProjectTraceHolder.TENANT_TRACE_ID, String.valueOf(ProjectTraceHolder.getTenantId()));
        headerMap.put(UserPermissionHolder.USER_PERMISSION, String.valueOf(UserPermissionHolder.getAccountId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getNsfMetaUrl() + "/api/metadata", queryStringMap, null, headerMap, Const.GET_METHOD);
        if (httpClientResponse == null || HttpStatus.OK.value() != httpClientResponse.getStatusCode()) {
            logger.error("acquire apiInfo from meta-service failed! projectId = {}", projectId);
            return Collections.emptyList();
        }
        String result = httpClientResponse.getResponseBody();
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject == null) {
            logger.error("acquire apiInfo from meta-service failed! projectId = {},response = {}", projectId, result);
            return Collections.emptyList();
        }
        String meta = jsonObject.getString("ApiList");
        List<ApiListDto> apiListDtoList = JSON.parseArray(meta, ApiListDto.class);
        if (CollectionUtils.isEmpty(apiListDtoList)) {
            logger.error("acquire apiInfo from meta-service failed! projectId = {},response = {},meta = {}", projectId, result, meta);
            return Collections.emptyList();
        }
        return apiListDtoList;
    }

    /**
     * 获取API同步列表
     *
     * @param serviceInfo
     * @return
     */
    private List<ApiInfo> getSyncListByService(ServiceInfo serviceInfo, Long projectId) {
        //从数据库获取信息
        long serviceId = serviceInfo.getId();
        List<ApiInfo> originList = apiInfoService.findAllApiByServiceId(serviceId);
        //从知识库获取信息
        List<ApiListDto> apiInfoFromKnowledge = getApiInfoFromKnowledge(serviceInfo.getExtServiceId());
        if (CollectionUtils.isEmpty(apiInfoFromKnowledge)) {
            logger.info("sync apiInfo from meta-knowledge is empty，extServiceId = {}", serviceInfo.getExtServiceId());
        }
        Map<String, ApiInfo> originMap = originList.stream().collect(Collectors.toMap(api -> getValidString(api.getApiPath(), api.getApiMethod(), api.getServiceId()), Function.identity()));
        List<ApiInfo> syncList = new ArrayList<>();
        //处理知识库存在的数据
        for (ApiListDto apiListDto : apiInfoFromKnowledge) {
            //以知识库服务ID及API相关信息为判重条件
            String validSting = getValidString(apiListDto.getApiInfoBasicDto().getApiPath()
                    , apiListDto.getApiInfoBasicDto().getApiMethod(), serviceId);
            ApiInfo originInfo = originMap.get(validSting);
            ApiInfo syncApiInfo = castToApiInfo(originInfo, apiListDto, serviceId, projectId);
            //数据未冲突情况 : 知识库存在 , 本地不存在 , 同步服务 --> SyncStatus=1(同步)
            if (originInfo == null) {
                syncList.add(syncApiInfo);
                continue;
            }//以下为数据冲突情况，慎重改动
            originMap.remove(validSting);
            //数据冲突情况 : 本地存在 , 为本地创建 , 知识库存在 , 保持不变 -->  SyncStatus=0(本地创建)
            if (originInfo.getSyncStatus() == 0) {
                logger.info("数据冲突情况 : 本地存在 , 为本地创建 , 知识库存在 , 保持不变 --> SyncStatus=0(本地创建),ApiId={}", syncApiInfo.getId());
                continue;
            }
            //数据冲突情况 : 本地存在 , 为同步数据 , 知识库存在 , 修正数据 --> SyncStatus=1(同步)
            syncApiInfo.setSyncStatus(1);
            syncList.add(syncApiInfo);
            logger.info("数据冲突情况 : 本地存在 , 为同步数据 , 知识库存在 , 修正数据 --> SyncStatus=1(同步),ApiId={}", syncApiInfo.getId());
        }
        for (ApiInfo syncInfo : originMap.values()) {
            //数据冲突情况 : 本地存在 , 为同步数据 , 知识库不存在 , 修正数据 --> SyncStatus=2(失步)
            if (syncInfo.getSyncStatus() != 0) {
                syncInfo.setSyncStatus(2);
                syncList.add(syncInfo);
                logger.info("本地数据存在 ，知识库不存在 ： 设置为失步状态,ApiId={}", syncInfo.getId());
            }
        }
        return syncList;
    }

    /**
     * 从知识库获取数据
     *
     * @param extApiId
     * @return
     */
    private Map<String, Object> getApiDetailsFromKnowledge(Long extApiId) {
        logger.info("start acquire apiInfo detail from meta, extApiId={}", extApiId);
        Map<String, String> queryStringMap = new HashMap<>();
        queryStringMap.put("Action", "GetAllInfoForAPI");
        queryStringMap.put("Version", "2018-08-09");
        queryStringMap.put("ApiId", String.valueOf(extApiId));
        Map<String, String> headerMap = new HashMap<>();
        long projectId = ProjectTraceHolder.getProId();
        headerMap.put(ProjectTraceHolder.PROJECT_TRACE_ID, String.valueOf(projectId));
        headerMap.put(ProjectTraceHolder.TENANT_TRACE_ID, String.valueOf(ProjectTraceHolder.getTenantId()));
        headerMap.put(UserPermissionHolder.USER_PERMISSION, String.valueOf(UserPermissionHolder.getAccountId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getNsfMetaUrl() + "/api/metadata", queryStringMap, null, headerMap, Const.GET_METHOD);
        if (httpClientResponse == null || HttpStatus.OK.value() != httpClientResponse.getStatusCode()) {
            logger.error("acquire apiInfo from meta-service failed! projectId = {}", projectId);
            return Collections.emptyMap();
        }
        String result = httpClientResponse.getResponseBody();
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject == null) {
            logger.error("acquire apiInfo from meta-service failed! projectId = {},response = {}", projectId, result);
            return Collections.emptyMap();
        }
        String meta = jsonObject.getString("Result");
        Map map = JSON.parseObject(meta, Map.class);
        return CollectionUtils.isEmpty(map) ? Collections.emptyMap() : map;
    }

    /**
     * 通过外部ID获取模型信息
     *
     * @param extServiceId
     * @return
     */
    private List<CreateApiModelDto> getApiApiModelFromMeta(Long extServiceId) {
        logger.info("start acquire api model detail from meta, extServiceId={}", extServiceId);
        Map<String, String> queryStringMap = new HashMap<>();
        queryStringMap.put("Action", "DescribeAllModelList");
        queryStringMap.put("Version", "2018-08-09");
        queryStringMap.put("ServiceId", String.valueOf(extServiceId));
        queryStringMap.put("Limit", String.valueOf(1000));
        Map<String, String> headerMap = new HashMap<>();
        long projectId = ProjectTraceHolder.getProId();
        headerMap.put(ProjectTraceHolder.PROJECT_TRACE_ID, String.valueOf(projectId));
        headerMap.put(ProjectTraceHolder.TENANT_TRACE_ID, String.valueOf(ProjectTraceHolder.getTenantId()));
        headerMap.put(UserPermissionHolder.USER_PERMISSION, String.valueOf(UserPermissionHolder.getAccountId()));
        HttpClientResponse httpClientResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getNsfMetaUrl() + "/api/metadata", queryStringMap, null, headerMap, Const.GET_METHOD);
        if (httpClientResponse == null || HttpStatus.OK.value() != httpClientResponse.getStatusCode()) {
            logger.error("acquire api model from meta-service failed! projectId = {}", projectId);
            return Collections.emptyList();
        }
        String result = httpClientResponse.getResponseBody();
        JSONObject jsonObject = JSON.parseObject(result);
        if (jsonObject == null) {
            logger.error("acquire api model from meta-service failed! projectId = {},response = {}", projectId, result);
            return Collections.emptyList();
        }
        return JSON.parseArray(jsonObject.getString("Result"), CreateApiModelDto.class);
    }

    /**
     * 转换服务数据
     *
     * @param serviceDto
     * @return
     */
    private SyncServiceInfo castToSyncServiceInfo(MicroServiceDto serviceDto) {
        SyncServiceInfo syncServiceInfo = new SyncServiceInfo();
        syncServiceInfo.setProjectId(serviceDto.getProjectId());
        syncServiceInfo.setDisplayName(serviceDto.getName());
        syncServiceInfo.setContacts(serviceDto.getOwner());
        syncServiceInfo.setServiceName(generatServiceTag());
        syncServiceInfo.setCreateDate(serviceDto.getCreateTime());
        syncServiceInfo.setModifyDate(System.currentTimeMillis());
        syncServiceInfo.setDescription(serviceDto.getDesc());
        syncServiceInfo.setServiceType(serviceDto.getServiceType());
        //设置为同步
        syncServiceInfo.setSyncStatus(1);
        //设置外部服务ID
        syncServiceInfo.setExtServiceId(serviceDto.getId());
        //默认 同步前服务状态 设为 0 - 新服务
        syncServiceInfo.setPreSyncStatus(SyncServiceInfo.PreSyncStatusEnum.NEW_SERVICE.getPreSyncStatus());
        return syncServiceInfo;
    }

    /**
     * 转换服务数据
     *
     * @param serviceDto
     * @return
     */
    private SyncServiceInfo castToSyncServiceInfo(MicroServiceDto serviceDto, ServiceInfo serviceInfo) {
        SyncServiceInfo syncServiceInfo = null;
        if (serviceInfo == null) {
            return castToSyncServiceInfo(serviceDto);
        }
        syncServiceInfo = new SyncServiceInfo(serviceInfo);

        syncServiceInfo.setContacts(serviceDto.getOwner());
        syncServiceInfo.setCreateDate(serviceDto.getCreateTime());
        syncServiceInfo.setModifyDate(System.currentTimeMillis());
        syncServiceInfo.setDescription(serviceDto.getDesc());
        syncServiceInfo.setExtServiceId(serviceDto.getId());


        return syncServiceInfo;
    }


    /**
     * 获取元数据服务列表并转换为SyncServiceInfoList
     *
     * @param projectId
     */
    private List<MicroServiceDto> getSyncServiceInfoFromMeta(Long projectId) {
        if (null == projectId) {
            logger.info("sync serviceInfo error ,projectId is null");
            return Collections.emptyList();
        }
        List<MicroServiceDto> serviceInfoFromMetaService = getServiceInfoFromMetaService(projectId);
        return serviceInfoFromMetaService;
    }

    /**
     * @return
     */
    private String generatServiceTag() {
        String serviceTag = Const.SERVICE + UUID.randomUUID();
        ServiceInfo service = serviceInfoService.getServiceByServiceName(serviceTag);
        if (service == null) {
            return serviceTag;
        }
        return generatServiceTag();
    }

    /**
     * 转换API信息
     *
     * @param apiInfo
     * @param apiListDto
     * @param serviceId
     * @param projectId
     * @return
     */
    private ApiInfo castToApiInfo(ApiInfo apiInfo, ApiListDto apiListDto, Long serviceId, Long projectId) {
        if (apiInfo == null) {
            apiInfo = castToApiInfo(apiListDto, serviceId, projectId);
        }
        apiInfo.setCreateDate(apiListDto.getCreateDate());
        apiInfo.setModifyDate(System.currentTimeMillis());
        ApiInfoBasicDto apiInfoBasicDto = apiListDto.getApiInfoBasicDto();
        apiInfo.setApiName(apiInfoBasicDto.getApiName());
        apiInfo.setApiMethod(apiInfoBasicDto.getApiMethod());
        apiInfo.setApiPath(apiInfoBasicDto.getApiPath());
        apiInfo.setType(apiInfoBasicDto.getType());
        apiInfo.setAliasName(apiInfoBasicDto.getAliasName());
        apiInfo.setDescription(apiInfoBasicDto.getDescription());
        apiInfo.setDocumentStatusId(apiInfoBasicDto.getDocumentStatusId());
        //将知识库中apiId设置为外部apiId
        apiInfo.setExtApiId(apiInfoBasicDto.getId());
        return apiInfo;
    }

    private ApiInfo castToApiInfo(ApiListDto apiListDto, Long serviceId, Long projectId) {
        ApiInfoBasicDto apiInfoBasicDto = apiListDto.getApiInfoBasicDto();
        ApiInfo apiInfo = BeanUtil.copy(apiInfoBasicDto, ApiInfo.class);
        apiInfo.setId(0);
        apiInfo.setCreateDate(apiListDto.getCreateDate());
        apiInfo.setModifyDate(System.currentTimeMillis());
        apiInfo.setServiceId(serviceId);
        apiInfo.setProjectId(projectId);
        apiInfo.setDocumentStatusId(apiInfoBasicDto.getDocumentStatusId());
        //设置为同步
        apiInfo.setSyncStatus(1);
        //将知识库中apiId设置为外部apiId
        apiInfo.setExtApiId(apiInfoBasicDto.getId());
        return apiInfo;
    }

    String getValidString(String apiPath, String apiMethod, Long serviceId) {
        return new StringBuilder().append(apiPath).append(split).append(apiMethod).append(split)
                .append(serviceId).toString();
    }
}
