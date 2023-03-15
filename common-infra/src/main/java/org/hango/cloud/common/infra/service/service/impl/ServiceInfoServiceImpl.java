package org.hango.cloud.common.infra.service.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.service.dao.IServiceInfoDao;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.meta.ServiceInfo;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/5
 */
@Service
public class ServiceInfoServiceImpl implements IServiceInfoService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInfoServiceImpl.class);

    @Autowired
    private IServiceInfoDao serviceInfoDao;

    @Autowired
    private IApiModelService apiModelService;

    @Autowired
    private IApiInfoService apiInfoService;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long create(ServiceDto serviceDto) {
        long proId = ProjectTraceHolder.getProId();
        if (!NumberUtils.LONG_ZERO.equals(proId)) {
            serviceDto.setProjectId(proId);
        }
        ServiceInfo info = toMeta(serviceDto);
        info.setCreateDate(System.currentTimeMillis());
        info.setModifyDate(System.currentTimeMillis());
        long id = serviceInfoDao.add(info);
        serviceDto.setId(id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long update(ServiceDto serviceDto) {
        ServiceInfo info = toMeta(serviceDto);
        info.setModifyDate(System.currentTimeMillis());
        serviceInfoDao.update(info);
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ServiceDto serviceDto) {
        ServiceInfo info = toMeta(serviceDto);
        serviceInfoDao.delete(info);
        //删除服务模型
        apiModelService.deleteApiModel(info.getId());
        //TODO zbj
    }

    @Override
    public List<? extends ServiceDto> findAll() {
        return serviceInfoDao.findAll().stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<? extends ServiceDto> findAll(long offset, long limit) {
        List<ServiceInfo> serviceInfoList = serviceInfoDao.getRecordsByField(Maps.newHashMap(), offset, limit);
        if (CollectionUtils.isEmpty(serviceInfoList)) {
            return Collections.emptyList();
        }
        return serviceInfoList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        Map<String, Object> params = Maps.newHashMap();
        return serviceInfoDao.getCountByFields(params);
    }

    @Override
    public ServiceDto get(long id) {
        return toView(serviceInfoDao.get(id));
    }

    @Override
    public ServiceDto toView(ServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return null;
        }
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setServiceType(serviceInfo.getServiceType());
        serviceDto.setExtensionInfo(serviceInfo.getExtensionInfo());
        serviceDto.setModifyDate(serviceInfo.getModifyDate());
        serviceDto.setDisplayName(serviceInfo.getDisplayName());
        serviceDto.setDescription(serviceInfo.getDescription());
        serviceDto.setId(serviceInfo.getId());
        serviceDto.setServiceName(serviceInfo.getServiceName());
        serviceDto.setProjectId(serviceInfo.getProjectId());
        serviceDto.setContacts(serviceInfo.getContacts());
        serviceDto.setStatus(serviceInfo.getStatus());
        serviceDto.setCreateDate(serviceInfo.getCreateDate());
        return serviceDto;
    }

    @Override
    public ServiceInfo toMeta(ServiceDto serviceDto) {
        if (serviceDto == null) {
            return null;
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceType(serviceDto.getServiceType());
        serviceInfo.setExtensionInfo(serviceDto.getExtensionInfo());
        serviceInfo.setModifyDate(serviceDto.getModifyDate());
        serviceInfo.setDisplayName(serviceDto.getDisplayName());
        serviceInfo.setDescription(serviceDto.getDescription());
        serviceInfo.setId(serviceDto.getId());
        serviceInfo.setServiceName(serviceDto.getServiceName());
        serviceInfo.setProjectId(serviceDto.getProjectId());
        serviceInfo.setContacts(serviceDto.getContacts());
        serviceInfo.setStatus(serviceDto.getStatus());
        serviceInfo.setCreateDate(serviceDto.getCreateDate());
        return serviceInfo;
    }

    @Override
    public ErrorCode checkCreateParam(ServiceDto serviceDto) {
        //判断服务名称是否已经存在
        if (isDisplayNameExists(serviceDto.getDisplayName().trim())) {
            logger.info("创建服务，当前项目已存在同名服务，项目下不允许创建同名服务");
            return CommonErrorCode.SERVICE_NAME_ALREADY_EXIST;
        }
        //判断服务标识是否存在
        if (StringUtils.isNotBlank(serviceDto.getServiceName()) &&
                getServiceByServiceName(serviceDto.getServiceName().trim()) != null) {
            logger.info("创建服务，服务标识已经存在");
            return CommonErrorCode.SERVICE_TAG_ALREADY_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateParam(ServiceDto serviceDto) {
        ServiceDto originService = get(serviceDto.getId());
        if (originService == null){
            logger.info("更新服务，服务不存在");
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        //服务已经发布，服务标识不允许修改
        boolean serviceNameEqualsFlag = serviceDto.getServiceName().equals(originService.getServiceName());
        if (originService.getStatus() == 1 && !serviceNameEqualsFlag) {
            return CommonErrorCode.CANNOT_MODIFY_SERVICE;
        }
        if (!serviceNameEqualsFlag && getServiceByServiceName(serviceDto.getServiceName()) != null) {
            logger.info("更新服务，服务标识已经存在");
            return CommonErrorCode.SERVICE_TAG_ALREADY_EXIST;
        }
        boolean displayNameEqualsFlag = serviceDto.getDisplayName().equals(originService.getDisplayName());
        //服务名称已经存在
        if (!displayNameEqualsFlag && isDisplayNameExists(serviceDto.getDisplayName())) {
            logger.info("更新服务，服务名称已经存在");
            return CommonErrorCode.SERVICE_NAME_ALREADY_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(ServiceDto serviceDto) {
        if (serviceDto == null) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        if (serviceDto.getStatus() == 1) {
            logger.info("服务已发布，不允许删除");
            return CommonErrorCode.CANNOT_DELETE_ONLINE_SERVICE;
        }

        if (apiInfoService.getApiCountByServiceId(serviceDto.getId()) > 0) {
            logger.info("服务下存在API，不允许删除服务");
            return CommonErrorCode.CANNOT_DELETE_API_SERVICE;
        }
        RouteRuleQuery query = RouteRuleQuery.builder().serviceId(serviceDto.getId()).build();
        List<RouteRuleInfoPO> routeRuleList = routeRuleInfoService.getRouteRuleList(query);
        if (!CollectionUtils.isEmpty(routeRuleList)) {
            logger.info("服务下存在路由，不允许删除服务");
            return CommonErrorCode.CANNOT_DELETE_API_SERVICE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public List<ServiceDto> findAllServiceByProjectId(long projectId) {
        return serviceInfoDao.getServiceByProjectId(projectId).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceDto> findAllServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId) {
        return serviceInfoDao.getServiceByProjectIdLimit(pattern, offset, limit, projectId).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public List<ServiceDto> findAllServiceByDisplayName(String pattern, int status, long projectId) {
        return serviceInfoDao.findAllServiceByDisplayName(pattern, status, projectId).stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long getServiceCountByProjectId(String pattern, long projectId) {
        if (StringUtils.isNotBlank(pattern)) {
            return serviceInfoDao.getServiceCountByProjectId(pattern, projectId);
        } else {
            Map<String, Object> params = Maps.newHashMap();
            params.put(BaseConst.PROJECT_ID, projectId);
            return serviceInfoDao.getCountByFields(params);
        }
    }

    @Override
    public boolean isServiceExists(long serviceId) {
        return serviceInfoDao.get(serviceId) != null;
    }

    @Override
    public ServiceDto getServiceByServiceName(String serviceName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceName", serviceName);
        List<ServiceInfo> serviceInfoList = serviceInfoDao.getRecordsByField(params);
        return toView(Iterables.getFirst(serviceInfoList, null));
    }

    @Override
    public ServiceDto getServiceByServiceNameAndProject(String serviceName, long projectId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("serviceName", serviceName);
        params.put(BaseConst.PROJECT_ID, projectId);
        List<ServiceInfo> serviceInfos = serviceInfoDao.getRecordsByField(params);
        return toView(Iterables.getFirst(serviceInfos, null));
    }

    @Override
    public boolean isDisplayNameExists(String displayName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("displayName", displayName);
        params.put(BaseConst.PROJECT_ID, ProjectTraceHolder.getProId());

        return serviceInfoDao.getCountByFields(params) != 0;
    }

    @Override
    public ServiceDto describeDisplayName(String displayName, long projectId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("displayName", displayName);
        params.put(BaseConst.PROJECT_ID, projectId);

        List<ServiceInfo> recordsByField = serviceInfoDao.getRecordsByField(params);

        return toView(Iterables.getFirst(recordsByField, null));
    }

    @Override
    public List<Long> getServiceIdListByDisplayNameFuzzy(String serviceName, long projectId) {
        List<Long> serviceIdList = serviceInfoDao.getServiceIdListByDisplayNameFuzzy(serviceName, projectId);
        return serviceIdList;
    }

    @Override
    public List<ServiceDto> getServiceDtoList(List<Long> serviceIdList) {
        if (CollectionUtils.isEmpty(serviceIdList)) {
            return Lists.newArrayList();
        }

        List<ServiceInfo> serviceInfoList = serviceInfoDao.getServiceInfoList(serviceIdList);
        return serviceInfoList.stream().map(this::toView).collect(Collectors.toList());
    }
}
