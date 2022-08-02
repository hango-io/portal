package org.hango.cloud.dashboard.apiserver.service.impl;

import com.ecwid.consul.v1.ConsulClient;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.IRegistryCenterDao;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.util.ConsulClientUtils;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/1/12
 */
@Service
public class RegistryCenterServiceImpl implements IRegistryCenterService {

    public static final String APPLICATION_NAME_REGEX = "UNKNOWN|NSF-EUREKA-SERVER";
    private static final Logger logger = LoggerFactory.getLogger(RegistryCenterServiceImpl.class);
    @Autowired
    IRegistryCenterService registryCenterService;
    @Autowired
    private IRegistryCenterDao registryCenterDao;

    private static final Map<String, Set<RegistryCenterEnum>> SERVICE_TYPE_2_REGISTER_TYPES_MAP = new HashMap<>();

    static {
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.dubbo.name(), Sets.newHashSet(RegistryCenterEnum.Zookeeper));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.webservice.name(), Sets.newHashSet(RegistryCenterEnum.Kubernetes));
        SERVICE_TYPE_2_REGISTER_TYPES_MAP.put(ServiceType.http.name(), Sets.newHashSet(RegistryCenterEnum.Nacos, RegistryCenterEnum.Kubernetes, RegistryCenterEnum.Eureka));
    }
    @Override
    public void saveRegistryCenter(RegistryCenterDto registryCenter) {
        RegistryCenterInfo centerInfo = RegistryCenterDto.trans(registryCenter);
        if (NumberUtils.LONG_ZERO.equals(centerInfo.getId())) {
            registryCenterDao.add(centerInfo);
            return;
        }
        registryCenterDao.update(centerInfo);
    }

    @Override
    public void deleteRegistryCenter(long id) {
        RegistryCenterInfo registryCenter = registryCenterDao.get(id);
        if (registryCenter != null) {
            registryCenterDao.delete(registryCenter);
        }
    }

    @Override
    public List<RegistryCenterDto> findAll() {
        List<RegistryCenterInfo> list = registryCenterDao.findAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(RegistryCenterDto::trans).collect(Collectors.toList());
    }

    @Override
    public List<RegistryCenterDto> findByType(String registryType, long gwId) {
        if (RegistryCenterEnum.get(registryType) == null) {
            logger.info("Error  Registry Type {}", registryType);
            return Collections.emptyList();
        }
        //获取该项目下非共享的注册中心
        List<RegistryCenterInfo> records = findRegistry(registryType, gwId, ProjectTraceHolder.getProId());
        if (CollectionUtils.isEmpty(records)) {
            //获取共享注册中心，一个网关仅允许一个共享注册中心
            records = findRegistry(registryType, gwId);
        }
        return records.stream().map(RegistryCenterDto::trans).collect(Collectors.toList());
    }

    private List<RegistryCenterInfo> findRegistry(String registryType, long gwId) {
        if (RegistryCenterEnum.get(registryType) == null) {
            logger.info("Error  Registry Type {}", registryType);
            return Collections.emptyList();
        }
        //共享的注册中心
        HashMap<String, Object> param = Maps.newHashMap();
        param.put("registryType", registryType);
        param.put("gwId", gwId);
        param.put("isShared", NumberUtils.INTEGER_ONE);
        return registryCenterDao.getRecordsByField(param);
    }

    private List<RegistryCenterInfo> findRegistry(String registryType, long gwId, long projectId) {
        if (RegistryCenterEnum.get(registryType) == null) {
            logger.info("Error  Registry Type {}", registryType);
            return Collections.emptyList();
        }
        HashMap<String, Object> param = Maps.newHashMap();
        param.put("registryType", registryType);
        param.put("projectId", projectId);
        param.put("gwId", gwId);

        List<RegistryCenterInfo> records = registryCenterDao.getRecordsByField(param);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        //不共享的网关，项目下的registCenter
        return records.stream().filter(registryCenterInfo -> registryCenterInfo.getIsShared() == NumberUtils.INTEGER_ZERO).collect(Collectors.toList());
    }


    @Override
    public List<RegistryCenterDto> findByTypeAndProject(String registryType, long projectId) {
        if (RegistryCenterEnum.get(registryType) == null) {
            logger.info("Error  Registry Type {}", registryType);
            return Collections.emptyList();
        }
        HashMap<String, Object> param = Maps.newHashMap();
        param.put("registryType", registryType);
        param.put("projectId", projectId);

        List<RegistryCenterInfo> records = registryCenterDao.getRecordsByField(param);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        return records.stream().map(RegistryCenterDto::trans).collect(Collectors.toList());
    }

    @Override
    public RegistryCenterDto findByTypeAndAddr(String registryType, String registryAddr) {
        RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(registryType);
        if (registryCenterEnum == null) {
            logger.info("Error  Registry Type {}", registryType);
            return null;
        }

        if (RegistryCenterEnum.Kubernetes.equals(registryCenterEnum)) {
            return new RegistryCenterDto(registryCenterEnum.getType(), StringUtils.EMPTY);
        }

        HashMap<String, Object> param = Maps.newHashMap();
        param.put("registryType", registryType);
        if (StringUtils.isNotBlank(registryAddr)) {
            param.put("registryAddr", registryAddr);
        }

        List<RegistryCenterInfo> records = registryCenterDao.getRecordsByField(param);
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }
        return RegistryCenterDto.trans(records.get(0));
    }

    @Override
    public RegistryCenterDto getRegistryCenter(long id) {
        return RegistryCenterDto.trans(registryCenterDao.get(id));
    }

    @Override
    public ErrorCode checkParam(RegistryCenterDto registryCenter) {
        if (registryCenter == null) {
            return CommonErrorCode.InvalidBodyFormat;
        }
        if (RegistryCenterEnum.get(registryCenter.getRegistryType()) == null) {
            return CommonErrorCode.InvalidParameterRegistryCenterType(registryCenter.getRegistryType());
        }
        if (!NumberUtils.LONG_ZERO.equals(registryCenter.getId())) {
            RegistryCenterDto origin = getRegistryCenter(registryCenter.getId());
            if (origin == null) {
                return CommonErrorCode.InvalidParameterValueId(String.valueOf(registryCenter.getId()));
            }
        }
        return CommonErrorCode.Success;
    }

    /**
     * 从Eureka获取应用
     *
     * @param registryCenter
     * @return
     */
    @Override
    public List<String> getApplicationsFromConsul(String registryCenter) {
        ConsulClient defaultConsulClient = ConsulClientUtils.getDefaultConsulClient(registryCenter);
        return ConsulClientUtils.getApplications(defaultConsulClient, ConsulClientUtils.getDataCenter(registryCenter));
    }

    @Override
    public List<String> describeRegistryTypesByServiceType(String serviceType) {
        List<RegistryCenterInfo> registryCenterInfos = registryCenterDao.findAll();
        List<String> currentRegistryList = CollectionUtils.isEmpty(registryCenterInfos) ? new ArrayList<>()
                : registryCenterInfos.stream().map(RegistryCenterInfo::getRegistryType).collect(Collectors.toList());
        //k8s注册中心不在注册中心表中，且默认支持
        currentRegistryList.add(RegistryCenterEnum.Kubernetes.getType());
        return filterAndSort(serviceType, currentRegistryList);
    }

    private static List<String> filterAndSort(String serviceType, List<String> registryList) {
        Set<RegistryCenterEnum> registryTypeSet = registryList.stream()
                .map(RegistryCenterEnum::get)
                .collect(Collectors.toSet());

        registryTypeSet.add(RegistryCenterEnum.Kubernetes);
        return registryTypeSet.stream()
                .filter(registryType -> SERVICE_TYPE_2_REGISTER_TYPES_MAP.containsKey(serviceType)
                        && SERVICE_TYPE_2_REGISTER_TYPES_MAP.get(serviceType).contains(registryType))
                .sorted(Comparator.comparingInt(RegistryCenterEnum::getOrder))
                .map(RegistryCenterEnum::getType)
                .collect(Collectors.toList());
    }
}
