package org.hango.cloud.dashboard.apiserver.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.ServiceInfoDao;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2017/12/26 17:05.
 */
@Service
public class ServiceInfoImpl implements IServiceInfoService {

    private static Logger logger = LoggerFactory.getLogger(ServiceInfoImpl.class);

    @Autowired
    private ServiceInfoDao serviceInfoDao;
    @Autowired
    private IApiModelService apiModelService;
    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;
    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;

    @Override
    public long add(ServiceInfo serviceInfo) {
        return serviceInfoDao.add(serviceInfo);
    }

    @Override
    public ServiceInfo addServiceInfo(ServiceInfoDto serviceInfoDto, long projectId) {
        ServiceInfo serviceInfo = ServiceInfoDto.toMeta(serviceInfoDto);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setCreateDate(System.currentTimeMillis());
        serviceInfo.setModifyDate(System.currentTimeMillis());
        serviceInfo.setId(serviceInfoDao.add(serviceInfo));
        return serviceInfo;
    }

    /**
     * 查询project下所有的服务
     *
     * @param projectId
     * @return
     */
    @Override
    public List<ServiceInfo> findAllServiceByProjectId(long projectId) {
        return serviceInfoDao.getServiceByProjectId(projectId);
    }

    @Override
    public List<ServiceInfo> findAllServiceByProjectIdLimit(String pattern, long offset, long limit, long projectId) {
        return serviceInfoDao.getServiceByProjectIdLimit(pattern, offset, limit, projectId);
    }

    @Override
    public long getServiceCountByProjectId(String pattern, long projectId) {
        if (StringUtils.isNotBlank(pattern)) {
            return serviceInfoDao.getServiceCountByProjectId(pattern, projectId);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("projectId", projectId);
            return serviceInfoDao.getCountByFields(params);
        }
    }

    @Override
    public List<ServiceInfo> findAll() {
        return serviceInfoDao.findAll();
    }

    /**
     * 设置缓存
     *
     * @param id
     * @return
     */
    @Override
    public ServiceInfo getServiceById(String id) {

        return serviceInfoDao.get(NumberUtils.toLong(id));
    }

    @Override
    public ServiceInfo getServiceByServiceId(long serviceId) {
        return serviceInfoDao.get(serviceId);
    }

    @Override
    public boolean isServiceExists(long serviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", serviceId);
        return serviceInfoDao.getCountByFields(params) == 0 ? false : true;
    }

    @Override
    public ServiceInfo getServiceByServiceName(String serviceName) {
        List<ServiceInfo> list = serviceInfoDao.getServiceByServiceName(serviceName);

        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public ServiceInfo getServiceByServiceNameAndProject(String serviceName, long projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceName", serviceName);
        params.put("projectId", projectId);
        List<ServiceInfo> serviceInfos = serviceInfoDao.getRecordsByField(params);
        return serviceInfos == null || serviceInfos.size() == 0 ? null : serviceInfos.get(0);
    }

    @Override
    public boolean isDisplayNameExists(String displayName) {
        Map<String, Object> params = new HashMap<>();
        params.put("displayName", displayName);
        params.put("projectId", ProjectTraceHolder.getProId());

        return serviceInfoDao.getCountByFields(params) == 0 ? false : true;

    }

    @Override
    public ErrorCode checkCreateServiceParam(ServiceInfoDto serviceInfoDto) {
        //判断服务名称是否已经存在
        if (isDisplayNameExists(serviceInfoDto.getDisplayName().trim())) {
            logger.info("创建服务，当前项目已存在同名服务，项目下不允许创建同名服务");
            return CommonErrorCode.ServiceNameAlreadyExist;
        }
        //判断服务标识是否存在
        if (StringUtils.isNotBlank(serviceInfoDto.getServiceName()) &&
                getServiceByServiceName(serviceInfoDto.getServiceName().trim()) != null) {
            logger.info("创建服务，服务标识已经存在");
            return CommonErrorCode.ServiceTagAlreadyExist;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkUpdateServiceParam(ServiceInfoDto serviceInfoDto) {
        //参数校验
        if (!isServiceExists(serviceInfoDto.getId())) {
            logger.info("更新服务，服务不存在");
            return CommonErrorCode.NoSuchService;
        }
        ServiceInfo serviceInfo = getServiceByServiceId(serviceInfoDto.getId());
        //服务已经发布，服务标识不允许修改
        boolean serviceNameEqualsFlag = serviceInfoDto.getServiceName().equals(serviceInfo.getServiceName());
        if (serviceInfo.getStatus() == 1 && !serviceNameEqualsFlag) {
            return CommonErrorCode.CannotModifyService;
        }

        //同步服务不能修改服务名称
        boolean displayNameEqualsFlag = serviceInfo.getDisplayName().equals(serviceInfoDto.getDisplayName());
        if (serviceInfo.getSyncStatus() != 0 && !displayNameEqualsFlag) {
            return CommonErrorCode.CannotUpdateServiceName;
        }

        //服务名称已经存在
        if (!displayNameEqualsFlag && isDisplayNameExists(serviceInfoDto.getDisplayName())) {
            logger.info("更新服务，服务名称已经存在");
            return CommonErrorCode.ServiceNameAlreadyExist;
        }
        if (!serviceNameEqualsFlag && getServiceByServiceName(serviceInfoDto.getServiceName()) != null) {
            logger.info("更新服务，服务标识已经存在");
            return CommonErrorCode.ServiceTagAlreadyExist;
        }
        return CommonErrorCode.Success;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(long serviceId) {
        //删除服务信息
        serviceInfoDao.delete(serviceId);
        //删除服务模型
        apiModelService.deleteApiModel(serviceId);
        //删除服务健康检查配置
        envoyHealthCheckService.deleteHealthCheckRule(serviceId);
        //删除pb文件
        envoyGrpcProtobufService.deleteServiceProtobuf(serviceId);
    }

    @Override
    public boolean updateService(ServiceInfo service) {
        if (null == service) {
            return false;
        }
        service.setModifyDate(System.currentTimeMillis());
        return 1 == serviceInfoDao.update(service);
    }


    @Override
    public ServiceInfo describeDisplayName(String displayName, long projectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("displayName", displayName);
        params.put("projectId", projectId);

        List<ServiceInfo> recordsByField = serviceInfoDao.getRecordsByField(params);

        if (CollectionUtils.isEmpty(recordsByField)) {
            return null;
        }
        return recordsByField.get(0);
    }

    @Override
    public List<Long> getServiceIdListByDisplayNameFuzzy(String serviceName, long projectId) {
        List<Long> serviceIdList = serviceInfoDao.getServiceIdListByDisplayNameFuzzy(serviceName, projectId);
        return CollectionUtils.isEmpty(serviceIdList) ? Lists.newArrayList() : serviceIdList;
    }

    @Override
    public List<ServiceInfo> getServiceInfoList(List<Long> serviceIdList) {
        if (CollectionUtils.isEmpty(serviceIdList)) {
            return Lists.newArrayList();
        }

        List<ServiceInfo> serviceInfoList = serviceInfoDao.getServiceInfoList(serviceIdList);
        return CollectionUtils.isEmpty(serviceIdList) ? Lists.newArrayList() : serviceInfoList;
    }

    @Override
    public ErrorCode checkServiceType(String serviceType) {
        if (ServiceType.getServiceTypeByName(serviceType) == null) {
            return CommonErrorCode.ServiceTypeInvalid;
        }
        return CommonErrorCode.Success;
    }
}