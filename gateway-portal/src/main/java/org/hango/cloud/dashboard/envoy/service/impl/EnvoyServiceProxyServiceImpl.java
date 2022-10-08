package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.PublishedDetailDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayEnum;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IDubboMetaService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.dao.IServiceProxyDao;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPublishServiceDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyDestinationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobufProxy;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyWebServiceService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceConnectionPoolDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceConsistentHashDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceLoadBalancerDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * envoy网关发布服务相关，发布服务至指定网关，即将服务与api-plane相关数据进行关联
 *
 * @author hanjiahao
 */
@Service
public class EnvoyServiceProxyServiceImpl implements IServiceProxyService {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyServiceImpl.class);
    /**
     * 负载均衡策略相关
     */
    private static final String SERVICE_LOADBALANCER_SIMPLE = "Simple";
    private static final String SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN = "LEAST_CONN";
    private static final String SERVICE_LOADBALANCER_SIMPLE_RANDOM = "RANDOM";
    private static final String SERVICE_LOADBALANCER_HASH = "ConsistentHash";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME = "HttpHeaderName";
    private static final String SERVICE_LOADBALANCER_HASH_HTTPCOOKIE = "HttpCookie";
    private static final String SERVICE_LOADBALANCER_HASH_USESOURCEIP = "UseSourceIp";
    private static final String STATIC_EP_VERSION = "version";
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceProxyDao serviceProxyDao;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;
    @Autowired
    private IEnvoyHealthCheckService envoyHealthCheckService;
    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;
    @Autowired
    private IRegistryCenterService registryCenterService;
    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;
    @Autowired
    private IGetFromApiPlaneService getFromApiPlaneService;
    @Autowired
    private IEnvoyWebServiceService webServiceService;
    @Autowired
    private IDubboMetaService dubboMetaService;
    @Autowired
    private IGatewayProjectService gatewayProjectService;

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public long publishServiceToGw(ServiceProxyDto serviceProxyDto) {
        if (!publishToDiffTypeGw(serviceProxyDto, null)) {
            return Const.ERROR_RESULT;
        }
        ServiceProxyInfo serviceProxyInfo = ServiceProxyDto.toMeta(serviceProxyDto);
        ServiceInfo serviceInfoInDb = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId());
        try {
            serviceProxyInfo.setProjectId(serviceInfoInDb.getProjectId());
        } catch (Exception e) {
            logger.error("发布服务至网关存在脏数据，服务不存在，e:{}", e);
            return Const.ERROR_RESULT;
        }
        serviceProxyInfo.setCreateTime(System.currentTimeMillis());
        serviceProxyInfo.setUpdateTime(System.currentTimeMillis());
        long publishServiceId = serviceProxyDao.add(serviceProxyInfo);
        if (publishServiceId > NumberUtils.INTEGER_ZERO) {
            //更新发布状态,未发布更新为已发布
            if (serviceInfoInDb != null && serviceInfoInDb.getStatus() == NumberUtils.INTEGER_ZERO) {
                serviceInfoInDb.setStatus(NumberUtils.INTEGER_ONE);
                serviceInfoService.updateService(serviceInfoInDb);
            }
        }
        return publishServiceId;
    }

    /**
     * 根据服务发布信息先获取不同类型的网关
     * 不同的类型的网关存在不同的发布逻辑
     * 因此需要区分调用
     *
     * @param serviceProxyDto          服务发布信息
     * @param envoyHealthCheckRuleInfo 健康检查信息
     * @return
     */
    private boolean publishToDiffTypeGw(ServiceProxyDto serviceProxyDto, EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (gatewayInfo == null) {
            logger.warn("网关信息为空");
            return false;
        }
        boolean publishStatus;

        switch (GatewayEnum.getByType(gatewayInfo.getGwType())) {
            case ENVOY:
                publishStatus = getFromApiPlaneService.publishServiceByApiPlane(serviceProxyDto, envoyHealthCheckRuleInfo);
                break;
            default:
                publishStatus = false;
        }
        return publishStatus;
    }

    /**
     * 根据服务发布信息先获取不同类型的网关
     * 不同的类型的网关存在不同的下线逻辑
     * 因此需要区分调用
     *
     * @param serviceProxyDto 服务发布信息
     * @return
     */
    private boolean offlineToDiffTypeGw(ServiceProxyDto serviceProxyDto) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (gatewayInfo == null) {
            logger.warn("网关信息为空");
            return false;
        }

        boolean offlineStatus;

        switch (GatewayEnum.getByType(gatewayInfo.getGwType())) {
            case ENVOY:
                offlineStatus = getFromApiPlaneService.offlineServiceByApiPlane(gatewayInfo.getApiPlaneAddr(), deleteAllSubset(serviceProxyDto));
                break;
            default:
                offlineStatus = false;
        }
        return offlineStatus;
    }

    @Override
    public long updateServiceToGw(ServiceProxyDto serviceProxyDto) {
        //判断版本有没有删除的，如果有删除的，需要先调用APIPlane删除接口，然后进行新建
        ServiceProxyInfo serviceProxyInfoInDB = getServiceProxyByServiceIdAndGwId(serviceProxyDto.getGwId(), serviceProxyDto.getServiceId());
        List<EnvoySubsetDto> subsets = serviceProxyDto.getSubsets();
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (StringUtils.isNotBlank(serviceProxyInfoInDB.getSubsets()) && GatewayEnum.ENVOY.getType().equals(gatewayInfo.getGwType())) {
            List<String> envoySubsetListInDB = ServiceProxyDto.setSubsetForDto(serviceProxyInfoInDB).stream().map(EnvoySubsetDto::getName).collect(Collectors.toList());
            List<String> subsetNameList = new ArrayList<>();
            if (serviceProxyDto.getSubsets() != null) {
                List<String> envoySubsetList = serviceProxyDto.getSubsets().stream().map(EnvoySubsetDto::getName).collect(Collectors.toList());
                subsetNameList = envoySubsetListInDB.stream().filter(s -> !envoySubsetList.contains(s)).collect(Collectors.toList());
            }
            if (subsetNameList.size() > 0) {
                List<EnvoySubsetDto> needDeleteSubsetList = new ArrayList<>();
                for (String name : subsetNameList) {
                    EnvoySubsetDto envoySubsetDto = new EnvoySubsetDto();
                    envoySubsetDto.setName(name + "-" + gatewayInfo.getGwClusterName());
                    needDeleteSubsetList.add(envoySubsetDto);
                }
                serviceProxyDto.setSubsets(needDeleteSubsetList);
                getFromApiPlaneService.offlineServiceByApiPlane(gatewayInfo.getApiPlaneAddr(), deleteSomeSubset(serviceProxyDto, gatewayInfo.getGwClusterName()));
            }
        }
        serviceProxyDto.setSubsets(subsets);

        if (!publishToDiffTypeGw(serviceProxyDto, null)) {
            return Const.ERROR_RESULT;
        }

        ServiceProxyInfo serviceProxyInfo = ServiceProxyDto.toMeta(serviceProxyDto);
        serviceProxyInfo.setUpdateTime(System.currentTimeMillis());
        return serviceProxyDao.update(serviceProxyInfo);
    }

    @Override
    public ErrorCode checkPublishParam(ServiceProxyDto serviceProxyDto) {
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId());
        if (serviceInfoDb == null) {
            return CommonErrorCode.NoSuchService;
        }
        serviceProxyDto.setCode(serviceInfoDb.getServiceName());
        ErrorCode errorCode = checkPublishServiceAndGw(serviceProxyDto.getServiceId(), serviceProxyDto.getGwId());
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }
        errorCode = checkRegistryCenterInfo(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }

        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyDto.getGwId());
        serviceProxyDto.setGwType(gatewayInfo.getGwType());

        return checkEnvoyServiceProxyDto(serviceProxyDto);
    }

    private ErrorCode checkPublishServiceAndGw(long serviceId, long gwId) {
        GatewayInfo gatewayInDb = gatewayInfoService.get(gwId);
        if (gatewayInDb == null) {
            logger.info("发布服务，指定网关不存在，网关id:{}", gwId);
            return CommonErrorCode.NoSuchGateway;
        }
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        int countByFields = serviceProxyDao.getCountByFields(params);
        if (countByFields > 0) {
            logger.info("发布服务，服务已发布到当前网关,服务id:{},网关id:{}，不允许再次发布", serviceId, gwId);
            return CommonErrorCode.ServiceAlreadyPublished;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkUpdatePublishParam(ServiceProxyDto serviceProxyDto) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceId", serviceProxyDto.getServiceId());
        params.put("gwId", serviceProxyDto.getGwId());
        params.put("id", serviceProxyDto.getId());
        if (serviceProxyDao.getCountByFields(params) == 0) {
            return CommonErrorCode.ServiceNotPublished;
        }
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId());
        if (serviceInfoDb == null) {
            return CommonErrorCode.NoSuchService;
        }
        serviceProxyDto.setCode(serviceInfoDb.getServiceName());
        GatewayInfo gatewayInDb = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (gatewayInDb == null) {
            return CommonErrorCode.NoSuchGateway;
        }
        ErrorCode errorCode = checkRegistryCenterInfo(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }
        //当有流量镜像指向该服务的版本时，该版本不能被删除
        errorCode = checkRouteMirrorSubset(serviceProxyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }

        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyDto.getGwId());
        serviceProxyDto.setGwType(gatewayInfo.getGwType());


        return checkEnvoyServiceProxyDto(serviceProxyDto);
    }

    private ErrorCode checkRegistryCenterInfo(ServiceProxyDto serviceProxyDto) {
        if (!Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return CommonErrorCode.Success;
        }
        List<ServiceProxyInfo> serviceProxyList = getServiceProxyByServiceId(serviceProxyDto.getServiceId());
        if (!CollectionUtils.isEmpty(serviceProxyList)) {
            List<String> backendServices = serviceProxyList.stream().map(ServiceProxyInfo::getBackendService).collect(Collectors.toList());
            if (!backendServices.contains(serviceProxyDto.getBackendService())) {
                logger.info("同一个服务发布/更新到不同的网关，指定后端服务不同，不允许创建");
                return CommonErrorCode.BackendServiceDifferent;
            }
        }

        if (StringUtils.isBlank(serviceProxyDto.getRegistryCenterType()) || RegistryCenterEnum.Kubernetes.getType().equals(serviceProxyDto.getRegistryCenterType())) {
            //默认Kubernetes
            serviceProxyDto.setRegistryCenterType(RegistryCenterEnum.Kubernetes.getType());
            return CommonErrorCode.Success;
        }

        return CommonErrorCode.Success;
    }

    /**
     * 校验服务和版本 负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     *
     * @param serviceProxyDto
     * @return
     */
    public ErrorCode checkEnvoyServiceProxyDto(ServiceProxyDto serviceProxyDto) {
        EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto = serviceProxyDto.getTrafficPolicy();
        ErrorCode errorCode = checkTrafficPolicy(envoyServiceTrafficPolicyDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return errorCode;
        }

        return checkSubsetWhenPublishService(serviceProxyDto);
    }

    /**
     * 校验服务发布时，填写的版本信息
     *
     * @param serviceProxyDto
     * @return
     */
    public ErrorCode checkSubsetWhenPublishService(ServiceProxyDto serviceProxyDto) {
        List<EnvoySubsetDto> envoySubsetDtoList = serviceProxyDto.getSubsets();
        ErrorCode errorCode = CommonErrorCode.Success;
        if (envoySubsetDtoList == null || envoySubsetDtoList.size() == 0) {
            return errorCode;
        }

        Set<String> subsetName = new HashSet<>();
        envoySubsetDtoList.stream().forEach(envoySubsetDto -> {
            subsetName.add(envoySubsetDto.getName());
        });
        if (subsetName.size() < envoySubsetDtoList.size()) {
            return CommonErrorCode.DuplicatedSubsetName;
        }

        if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            //静态发布的服务，版本中包含的地址要在发布地址列表中
            List<String> addrList = Arrays.asList(serviceProxyDto.getBackendService().split(","));
            for (EnvoySubsetDto envoySubsetDto : envoySubsetDtoList) {
                if (envoySubsetDto.getStaticAddrList() == null || envoySubsetDto.getStaticAddrList().size() == 0 || !addrList.containsAll(envoySubsetDto.getStaticAddrList())) {
                    return CommonErrorCode.InvalidSubsetStaticAddr;
                }
                Set<String> addrSet = new HashSet<>();
                addrSet.addAll(envoySubsetDto.getStaticAddrList());
                if (addrSet.size() < envoySubsetDto.getStaticAddrList().size()) {
                    //同一个版本里配置的静态地址不能重复
                    return CommonErrorCode.DuplicatedSubsetStaticAddr;
                }
            }

            //每个地址仅能出现在0或1个版本中
            for (String addr : addrList) {
                int count = 0;
                for (EnvoySubsetDto envoySubsetDto : envoySubsetDtoList) {
                    if (envoySubsetDto.getStaticAddrList().contains(addr)) {
                        count++;
                    }
                }
                if (count > 1) {
                    return CommonErrorCode.DuplicatedStaticAddr;
                }
            }
        }

        for (EnvoySubsetDto envoySubsetDto : envoySubsetDtoList) {
            errorCode = checkTrafficPolicy(envoySubsetDto.getTrafficPolicy());
            if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
                return errorCode;
            }
        }

        return errorCode;
    }


    /**
     * 校验负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     *
     * @param envoyServiceTrafficPolicyDto
     * @return
     */
    private ErrorCode checkTrafficPolicy(EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto) {
        if (envoyServiceTrafficPolicyDto == null) {
            return CommonErrorCode.Success;
        }

        EnvoyServiceLoadBalancerDto envoyServiceLoadBalancerDto = envoyServiceTrafficPolicyDto.getLoadBalancer();
        if (envoyServiceLoadBalancerDto != null) {
            if (SERVICE_LOADBALANCER_SIMPLE.equals(envoyServiceLoadBalancerDto.getType())) {
                //Simple类型，包含ROUND_ROBIN|LEAST_CONN|RANDOM
                final List<String> simpleList = new ArrayList<>();
                simpleList.add(SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN);
                simpleList.add(SERVICE_LOADBALANCER_SIMPLE_LEAST_CONN);
                simpleList.add(SERVICE_LOADBALANCER_SIMPLE_RANDOM);
                if (StringUtils.isBlank(envoyServiceLoadBalancerDto.getSimple()) ||
                        !simpleList.contains(envoyServiceLoadBalancerDto.getSimple())) {
                    return CommonErrorCode.InvalidSimpleLoadBanlanceType;
                }
                //将Cookie相关参数置空
                envoyServiceLoadBalancerDto.setConsistentHash(null);
            } else if (SERVICE_LOADBALANCER_HASH.equals(envoyServiceLoadBalancerDto.getType())) {
                //一致性哈希
                EnvoyServiceConsistentHashDto envoyServiceConsistentHashDto = envoyServiceLoadBalancerDto.getConsistentHash();
                if (envoyServiceConsistentHashDto == null) {
                    //不能为空
                    return CommonErrorCode.InvalidConsistentHashObject;
                }
                final List<String> hashList = new ArrayList<>();
                hashList.add(SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME);
                hashList.add(SERVICE_LOADBALANCER_HASH_HTTPCOOKIE);
                hashList.add(SERVICE_LOADBALANCER_HASH_USESOURCEIP);

                if (StringUtils.isBlank(envoyServiceConsistentHashDto.getType()) ||
                        !hashList.contains(envoyServiceConsistentHashDto.getType())) {
                    return CommonErrorCode.InvalidConsistentHashType;
                }
                if (SERVICE_LOADBALANCER_HASH_HTTPCOOKIE.equals(envoyServiceConsistentHashDto.getType())) {
                    EnvoyServiceConsistentHashDto.EnvoyServiceConsistentHashCookieDto envoyServiceConsistentHashCookieDto =
                            envoyServiceConsistentHashDto.getCookieDto();
                    if (envoyServiceConsistentHashCookieDto == null) {
                        //cookie不能为空
                        return CommonErrorCode.InvalidConsistentHashHttpCookieObject;
                    }
                    String name = envoyServiceConsistentHashCookieDto.getName();
                    if (StringUtils.isBlank(name) || name.length() > 255) {
                        return CommonErrorCode.InvalidConsistentHashHttpCookieName;
                    }
                    Integer ttl = envoyServiceConsistentHashCookieDto.getTtl();
                    if (ttl == null || ttl < 0) {
                        return CommonErrorCode.InvalidConsistentHashHttpCookieTtl;
                    }
                    envoyServiceConsistentHashDto.setHttpHeaderName(null);
                    envoyServiceConsistentHashDto.setUseSourceIp(null);
                }

                if (SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME.equals(envoyServiceConsistentHashDto.getType())) {
                    if (StringUtils.isBlank(envoyServiceConsistentHashDto.getHttpHeaderName())) {
                        return CommonErrorCode.InvalidConsistentHashHttpHeaderName;
                    }
                    envoyServiceConsistentHashDto.setCookieDto(null);
                    envoyServiceConsistentHashDto.setUseSourceIp(null);
                }

                if (SERVICE_LOADBALANCER_HASH_USESOURCEIP.equals(envoyServiceConsistentHashDto.getType())) {
                    if (envoyServiceConsistentHashDto.getUseSourceIp() == null) {
                        return CommonErrorCode.InvalidConsistentHashSourceIP;
                    }
                    envoyServiceConsistentHashDto.setHttpHeaderName(null);
                    envoyServiceConsistentHashDto.setCookieDto(null);
                }
                //将simple类型相关参数置空
                envoyServiceLoadBalancerDto.setSimple(null);
            } else {
                //type不合法
                return CommonErrorCode.InvalidLoadBanlanceType;
            }
        }
        EnvoyServiceConnectionPoolDto envoyServiceConnectionPoolDto = envoyServiceTrafficPolicyDto.getConnectionPoolDto();
        if (envoyServiceConnectionPoolDto != null) {
            EnvoyServiceConnectionPoolDto.EnvoyServiceHttpConnectionPoolDto envoyServiceHttpConnectionPoolDto = envoyServiceConnectionPoolDto.getServiceHttpConnectionPoolDto();
            EnvoyServiceConnectionPoolDto.EnvoyServiceTcpConnectionPoolDto envoyServiceTcpConnectionPoolDto = envoyServiceConnectionPoolDto.getServiceTcpConnectionPoolDto();
            if (envoyServiceHttpConnectionPoolDto != null) {
                Integer http1MaxPendingRequests = envoyServiceHttpConnectionPoolDto.getHttp1MaxPendingRequests();
                Integer http2MaxRequests = envoyServiceHttpConnectionPoolDto.getHttp2MaxRequests();
                Integer idleTimeout = envoyServiceHttpConnectionPoolDto.getIdleTimeout();
                Integer maxRequestsPerConnection = envoyServiceHttpConnectionPoolDto.getMaxRequestsPerConnection();
                if (http1MaxPendingRequests < 0) {
                    return CommonErrorCode.InvalidHttp1MaxPendingRequests;
                }
                if (http2MaxRequests < 0) {
                    return CommonErrorCode.InvalidHttp2MaxRequests;
                }
                if (idleTimeout < 0) {
                    return CommonErrorCode.InvalidIdleTimeout;
                }
                if (maxRequestsPerConnection < 0) {
                    return CommonErrorCode.InvalidMaxRequestsPerConnection;
                }
            }
            if (envoyServiceTcpConnectionPoolDto != null) {
                Integer maxConnections = envoyServiceTcpConnectionPoolDto.getMaxConnections();
                Integer connectTimeout = envoyServiceTcpConnectionPoolDto.getConnectTimeout();
                if (maxConnections < 0) {
                    return CommonErrorCode.InvalidMaxConnections;
                }
                if (connectTimeout < 0) {
                    return CommonErrorCode.InvalidConnectTimeout;
                }
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<ServiceProxyInfo> getEnvoyServiceProxy(long gwId, long serviceId, long projectId, long offset, long limit) {
        return serviceProxyDao.getServiceProxyByLimit(gwId, serviceId, projectId, offset, limit);
    }

    @Override
    public List<ServiceProxyInfo> getEnvoyServiceProxy(long gwId, String pattern, long project, long offset,
                                                       long limit) {
        List<Long> serviceIds = Collections.EMPTY_LIST;
        if (StringUtils.isNotBlank(pattern)) {
            serviceIds = serviceInfoService.findAllServiceByProjectIdLimit(pattern, 0, 1000, project).stream().map(
                    ServiceInfo::getId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(serviceIds)) {
            return Collections.emptyList();
        }
        return serviceProxyDao.getServiceProxyByLimit(gwId, serviceIds, project, offset, limit);
    }

    @Override
    public List<ServiceProxyInfo> getAuthServiceProxyByLimit(long gwId, long serviceId, long projectId,
                                                             long offset, long limit) {
        return serviceProxyDao.getServiceProxyByLimit(gwId, serviceId, projectId, Collections.emptyList(), offset, limit);
    }

    @Override
    public long getServiceProxyCount(long gwId, String pattern, long projectId) {
        if (StringUtils.isBlank(pattern)) {
            return getServiceProxyCount(gwId, 0);
        }
        List<Long> serviceIds = serviceInfoService.findAllServiceByProjectIdLimit(pattern, 0, 1000, projectId).stream()
                .map(ServiceInfo::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serviceIds)) {
            return 0;
        }
        return serviceProxyDao.getCount(gwId, serviceIds, projectId);
    }

    @Override
    public long getServiceProxyCount(long gwId, long serviceId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        //服务id或网关id有一项等于0
        if (NumberUtils.INTEGER_ZERO == serviceId || NumberUtils.INTEGER_ZERO == gwId) {
            params.put("projectId", ProjectTraceHolder.getProId());
        }
        //查询指定的服务发布数量
        if (NumberUtils.INTEGER_ZERO != serviceId) {
            params.put("serviceId", serviceId);
        }
        //查询指定的网关发布数量
        if (NumberUtils.INTEGER_ZERO != gwId) {
            params.put("gwId", gwId);
        }

        return serviceProxyDao.getCountByFields(params);
    }

    @Override
    public long getAuthServiceProxyCount(final long gwId, final long serviceId) {
        return serviceProxyDao.getAuthServiceProxyCount(gwId, serviceId, ProjectTraceHolder.getProId(), Collections.emptyList());
    }

    @Override
    public void deleteServiceProxy(long id) {
        ServiceProxyInfo serviceProxyInfo = serviceProxyDao.get(id);
        if (serviceProxyInfo != null) {
            serviceProxyDao.delete(serviceProxyInfo);
            // 删除wsdl信息
            webServiceService.deleteServiceWsdlInfo(serviceProxyInfo.getGwId(), serviceProxyInfo.getServiceId());
        }
    }

    @Override
    public ErrorCode checkDeleteServiceProxy(long gwId, long serviceId) {
        GatewayInfo gatewayById = gatewayInfoService.get(gwId);
        if (gatewayById == null) {
            return CommonErrorCode.NoSuchGateway;
        }
        ServiceProxyInfo servicePublishInfo = getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (servicePublishInfo == null) {
            return CommonErrorCode.ServiceNotPublished;
        }
        long count = envoyRouteRuleProxyService.getRouteRuleProxyCountByService(gwId, serviceId);
        if (count > 0) {
            return CommonErrorCode.RouteRuleAlreadyPublished;
        }

        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = envoyGrpcProtobufService.getServiceProtobufProxy(serviceId, gwId);
        if (envoyServiceProtobufProxy != null) {
            return CommonErrorCode.CouldNotOfflineService;
        }
        //当有流量镜像指向该服务时，该服务不能下线
        List<RouteRuleProxyInfo> routeRuleProxyList = envoyRouteRuleProxyService.getRouteRuleProxyListByMirrorServiceIdAndGwId(serviceId, gwId);
        if (routeRuleProxyList != null && routeRuleProxyList.size() > 0) {
            return CommonErrorCode.MirrorByRouteRule;
        }
        return CommonErrorCode.Success;
    }

    /**
     * 获取所有版本，用于服务删除
     *
     * @param serviceProxyDto
     * @return
     */
    EnvoyPublishServiceDto deleteAllSubset(ServiceProxyDto serviceProxyDto) {
        GatewayInfo gatewayById = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (gatewayById == null) {
            return null;
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId());
        if (serviceInfo == null) {
            return null;
        }
        EnvoyPublishServiceDto envoyPublishServiceDto = new EnvoyPublishServiceDto();
        envoyPublishServiceDto.setCode(serviceProxyDto.getCode());
        //网关集群名称
        envoyPublishServiceDto.setGateway(gatewayById.getGwClusterName());
        envoyPublishServiceDto.setBackendService(getBackendServiceSendToApiPlane(serviceProxyDto));
        envoyPublishServiceDto.setType(serviceProxyDto.getPublishType());
        envoyPublishServiceDto.setServiceTag(serviceInfo.getServiceName());
        envoyPublishServiceDto.setProtocol(serviceProxyDto.getPublishProtocol());

        //下线服务时同时下线服务下所有的subset，包含默认生成的subset
        List<EnvoySubsetDto> subsetDtoList = new ArrayList<>();
        EnvoySubsetDto envoySubsetDto = new EnvoySubsetDto();
        envoySubsetDto.setName((serviceProxyDto.getCode() + "-" + gatewayById.getGwClusterName()).toLowerCase());
        subsetDtoList.add(envoySubsetDto);
        if (serviceProxyDto.getSubsets() != null) {
            subsetDtoList.addAll(setSubsetForDtoWhenSendToAPIPlane(serviceProxyDto, gatewayById.getGwClusterName()));
        }
        envoyPublishServiceDto.setSubsets(subsetDtoList);
        //网关集群名称
        envoyPublishServiceDto.setGateway(gatewayById.getGwClusterName());
        return envoyPublishServiceDto;
    }

    /**
     * 用于仅删除服务版本，服务更新时
     *
     * @param serviceProxyDto
     * @return
     */
    EnvoyPublishServiceDto deleteSomeSubset(ServiceProxyDto serviceProxyDto, String gwClusterName) {
        GatewayInfo gatewayById = gatewayInfoService.get(serviceProxyDto.getGwId());
        if (gatewayById == null) {
            return null;
        }
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId());
        if (serviceInfo == null) {
            return null;
        }
        EnvoyPublishServiceDto envoyPublishServiceDto = new EnvoyPublishServiceDto();
        envoyPublishServiceDto.setCode(serviceProxyDto.getCode());
        //网关集群名称
        envoyPublishServiceDto.setGateway(gatewayById.getGwClusterName());
        envoyPublishServiceDto.setBackendService(getBackendServiceSendToApiPlane(serviceProxyDto));
        envoyPublishServiceDto.setType(serviceProxyDto.getPublishType());
        envoyPublishServiceDto.setServiceTag(serviceInfo.getServiceName());
        envoyPublishServiceDto.setProtocol(serviceProxyDto.getPublishProtocol());
        envoyPublishServiceDto.setSubsets(serviceProxyDto.getSubsets());
        //网关集群名称
        envoyPublishServiceDto.setGateway(gatewayById.getGwClusterName());
        return envoyPublishServiceDto;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteServiceProxy(long gwId, long serviceId) {
        GatewayInfo gatewayById = gatewayInfoService.get(gwId);
        if (gatewayById == null) {
            logger.info("deleteServiceProxy, gatewayById is null. gwId:{}, serviceId:{}", gwId, serviceId);
            return false;
        }
        ServiceProxyInfo servicePublishInfo = getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (servicePublishInfo == null) {
            logger.info("deleteServiceProxy, servicePublishInfo is null. gwId:{}, serviceId:{}", gwId, serviceId);
            return false;
        }
        String publishType = servicePublishInfo.getPublishType();
        String backendService = servicePublishInfo.getBackendService();
        if (!offlineToDiffTypeGw(ServiceProxyDto.toDto(servicePublishInfo))) {
            return false;
        }
        dubboMetaService.batchDeleteByCondition(gwId, servicePublishInfo.getBackendService());
        return deleteServiceProxyWithTransactional(gwId, serviceId, backendService, publishType);
    }


    /**
     * 服务下线时，需要级联删除的记录
     *
     * @param gwId
     * @param serviceId
     * @param backendService
     * @param publishType
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    boolean deleteServiceProxyWithTransactional(long gwId, long serviceId, String backendService, String publishType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("serviceId", serviceId);
        params.put("backendService", backendService);
        params.put("publishType", publishType);
        List<ServiceProxyInfo> recordsByField = serviceProxyDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(recordsByField)) {
            return true;
        }
        ServiceProxyInfo serviceProxyInfo = recordsByField.get(0);
        serviceProxyDao.delete(serviceProxyInfo);
        //修改服务发布状态为未发布
        if (CollectionUtils.isEmpty(getServiceProxyByServiceId(serviceId)) /*&&
                CollectionUtils.isEmpty(serviceProxyService.getServiceProxy(serviceId))*/) {
            ServiceInfo serviceInfoInDb = serviceInfoService.getServiceByServiceId(serviceId);
            if (serviceInfoInDb != null) {
                serviceInfoInDb.setStatus(NumberUtils.INTEGER_ZERO);
                serviceInfoService.updateService(serviceInfoInDb);
            }
        }

        //服务下线后，需要关闭健康检查功能
        envoyHealthCheckService.shutdownHealthCheck(serviceId, gwId);
        return true;
    }

    @Override
    public ServiceProxyInfo getServiceProxyByServiceIdAndGwId(long gwId, long serviceId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("serviceId", serviceId);
        List<ServiceProxyInfo> serviceProxyInfos = serviceProxyDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(serviceProxyInfos) ? null : serviceProxyInfos.get(0);
    }

    @Override
    public ServiceProxyInfo getServiceProxyInterByServiceIdAndGwIds(List<Long> gwIds, long serviceId) {
        if (CollectionUtils.isEmpty(gwIds)) {
            return null;
        }
        List<ServiceProxyInfo> serviceProxyInfo = gwIds.stream().map(item -> getServiceProxyByServiceIdAndGwId(item, serviceId)).
                filter(CommonUtil.distinctByKey(item -> item.getBackendService())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(serviceProxyInfo) || serviceProxyInfo.size() > 1) {
            logger.info("多网关后端服务发布不同，返回前端无应用数据");
            return null;
        }
        return serviceProxyInfo.get(0);
    }


    @Override
    public ServiceProxyInfo getServiceProxyByServicePublishInfo(long gwId, long serviceId, String backendService, String publishType) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("serviceId", serviceId);
        params.put("backendService", backendService);
        params.put("publishType", publishType);
        List<ServiceProxyInfo> serviceProxyInfos = serviceProxyDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(serviceProxyInfos) ? null : serviceProxyInfos.get(0);
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyByServiceId(long serviceId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("serviceId", serviceId);
        return serviceProxyDao.getRecordsByField(params);
    }

    @Override
    public ServiceProxyDto fromMeta(ServiceProxyInfo serviceProxyInfo) {
        ServiceProxyDto serviceProxyDto = BeanUtil.copy(serviceProxyInfo, ServiceProxyDto.class);
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyInfo.getGwId());
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceProxyInfo.getServiceId());
        ;
        serviceProxyDto.setServiceName(serviceInfo.getDisplayName());
        serviceProxyDto.setServiceTag(serviceInfo.getServiceName());
        serviceProxyDto.setGwClusterName(gatewayInfo.getGwClusterName());
        serviceProxyDto.setGwName(gatewayInfo.getGwName());
        serviceProxyDto.setGwAddr(gatewayInfo.getGwAddr());
        serviceProxyDto.setEnvId(gatewayInfo.getEnvId());
        serviceProxyDto.setServiceType(serviceInfo.getServiceType());

        //增加健康检查
        serviceProxyDto.setHealthyStatus(envoyHealthCheckService.getServiceHealthyStatus(serviceInfo, gatewayInfo));
        //增加版本信息
        serviceProxyDto.setSubsets(ServiceProxyDto.setSubsetForDto(serviceProxyInfo));
        serviceProxyDto.setTrafficPolicy(ServiceProxyDto.setTrafficPolicyForDto(serviceProxyInfo));
        return serviceProxyDto;
    }

    @Override
    public ServiceProxyDto fromMetaWithStatus(ServiceProxyInfo serviceProxyInfo, String querySource) {
        ServiceProxyDto serviceProxyDto = BeanUtil.copy(serviceProxyInfo, ServiceProxyDto.class);
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceProxyInfo.getServiceId());
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyInfo.getGwId());

        if (StringUtils.equals("NSF", querySource)) {
            if (serviceInfo.getExtServiceId() == NumberUtils.LONG_ZERO) {
                logger.info("该服务不属于从NSF知识库同步的服务， ServiceProxyInfo = {}", serviceProxyDto);
                return null;
            }

            if (StringUtils.equals(Const.STATIC_PUBLISH_TYPE, serviceProxyInfo.getPublishType())) {
                logger.info("该服务不属于动态发布的服务， ServiceProxyInfo = {}", serviceProxyDto);
                return null;
            }

            if (!StringUtils.equals(RegistryCenterEnum.Eureka.getType(), serviceProxyInfo.getRegistryCenterType())) {
                logger.info("该服务不属于Eureka注册中心的服务， ServiceProxyInfo = {}", serviceProxyDto);
                return null;
            }
        }

        serviceProxyDto.setServiceName(serviceInfo.getDisplayName());
        serviceProxyDto.setServiceTag(serviceInfo.getServiceName());
        serviceProxyDto.setGwName(gatewayInfo.getGwName());
        serviceProxyDto.setGwClusterName(gatewayInfo.getGwClusterName());
        serviceProxyDto.setGwAddr(gatewayInfo.getGwAddr());
        serviceProxyDto.setEnvId(gatewayInfo.getEnvId());
        serviceProxyDto.setServiceType(serviceInfo.getServiceType());
        serviceProxyDto.setSubsets(ServiceProxyDto.setSubsetForDto(serviceProxyInfo));
        serviceProxyDto.setTrafficPolicy(ServiceProxyDto.setTrafficPolicyForDto(serviceProxyInfo));
        //调用APIPlane查询当前节点状态
        serviceProxyDto.setHealthyStatus(envoyHealthCheckService.getServiceHealthyStatus(serviceInfo, gatewayInfo));
        return serviceProxyDto;
    }

    @Override
    public ServiceProxyDto fromMetaWithPort(ServiceProxyInfo serviceProxyInfo) {
        ServiceProxyDto serviceProxyDto = fromMeta(serviceProxyInfo);
        //静态发布，不获取port信息
        if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
            return serviceProxyDto;
        }
        RegistryCenterDto registry = registryCenterService.findByType(serviceProxyInfo.getRegistryCenterType());
        String registryAlias = registry == null ? StringUtils.EMPTY : registry.getRegistryAlias();
        //dynamic发布，获取port数据
        if (RegistryCenterEnum.Eureka.getType().equals(serviceProxyInfo.getRegistryCenterType()) ||
                RegistryCenterEnum.Consul.getType().equals(serviceProxyInfo.getRegistryCenterType()) ||
                RegistryCenterEnum.Zookeeper.getType().equals(serviceProxyInfo.getRegistryCenterType())) {
            //Eureka和Consul方式发布的服务，其端口号都设置为80
            List<Integer> portList = new ArrayList<>();
            portList.add(80);
            serviceProxyDto.setPort(portList);
        } else {
            List<EnvoyServiceWithPortDto> serviceListFromApiPlane = getFromApiPlaneService.getServiceListFromApiPlane(serviceProxyInfo.getGwId(),
                    serviceProxyInfo.getBackendService(), serviceProxyInfo.getRegistryCenterType(), registryAlias, Collections.EMPTY_MAP);
            if (CollectionUtils.isEmpty(serviceListFromApiPlane)) {
                logger.info("从api-plane获取服务port信息，api-plane返回为空");
                return serviceProxyDto;
            }
            serviceProxyDto.setPort(serviceListFromApiPlane.get(0).getPorts());
        }

        return serviceProxyDto;
    }

    @Override
    public List<GatewayDto> getPublishedServiceGateway(long serviceId) {
        List<ServiceProxyInfo> serviceProxies = getServiceProxyByServiceId(serviceId);
        if (CollectionUtils.isEmpty(serviceProxies)) {
            return new ArrayList<>();
        }
        List<Long> gatewayId = serviceProxies.stream().map(ServiceProxyInfo::getGwId).collect(Collectors.toList());
        List<GatewayInfo> gatewayInfos = gatewayId.stream().map(gatewayInfoService::get).collect(Collectors.toList());
        return BeanUtil.copyList(gatewayInfos, GatewayDto.class);
    }

    @Override
    public ErrorCode getRouteRuleNameWithServiceSubset(ServiceProxyDto serviceProxyDto) {
        long serviceId = serviceProxyDto.getServiceId();
        List<EnvoySubsetDto> envoySubsetDtos = serviceProxyDto.getSubsets();
        if (envoySubsetDtos == null) {
            return CommonErrorCode.Success;
        }
        List<String> subsets = envoySubsetDtos.stream().map(EnvoySubsetDto::getName).collect(Collectors.toList());
        List<RouteRuleProxyInfo> envoyRouteRuleInfoList = envoyRouteRuleProxyService.getRouteRuleProxyList(serviceId);
        for (RouteRuleProxyInfo routeRuleProxyInfo : envoyRouteRuleInfoList) {
            for (EnvoyDestinationInfo destinationInfo : routeRuleProxyInfo.getDestinationServiceList()) {
                if (StringUtils.isNotBlank(destinationInfo.getSubsetName()) && !subsets.contains(destinationInfo.getSubsetName())) {
                    RouteRuleInfo routeRuleInfo = envoyRouteRuleInfoService.getRouteRuleInfoById(routeRuleProxyInfo.getRouteRuleId());
                    return CommonErrorCode.SubsetUsedByRouteRule(routeRuleInfo.getRouteRuleName());
                }
            }
        }
        return CommonErrorCode.Success;
    }

//    /**
//     * 为dto增加版本信息，因为db中存储的是字符串，dto中是list，不能直接用BeanUtil.copy来赋值
//     * 用于前端展示
//     *
//     * @param serviceProxyInfo
//     * @return
//     */
//    @Override
//    public List<EnvoySubsetDto> setSubsetForDto(ServiceProxyInfo serviceProxyInfo) {
//        //增加版本信息
//        if (StringUtils.isNotBlank(serviceProxyInfo.getSubsets())) {
//            List<JSONObject> subsetObject = JSON.parseObject(serviceProxyInfo.getSubsets(), List.class);
//            List<EnvoySubsetDto> subsets = new ArrayList<>();
//            for (JSONObject subsetTemp : subsetObject) {
//                subsets.add(JSONObject.toJavaObject(subsetTemp, EnvoySubsetDto.class));
//            }
//            return subsets;
//        }
//        return null;
//    }

    /**
     * 当需要将版本信息发送到APIPlane时，采用此方法生成subset，因为subset在DR中的名称和用户输入的不同，需要加上-{gwClusterName}
     *
     * @param serviceProxyDto
     * @param gwClusterName
     * @return
     */
    @Override
    public List<EnvoySubsetDto> setSubsetForDtoWhenSendToAPIPlane(ServiceProxyDto serviceProxyDto, String gwClusterName) {
        List<EnvoySubsetDto> envoySubsetDtoList = generateSubsetDtoName(serviceProxyDto, gwClusterName);
        List<EnvoySubsetDto> envoySubsetDtoListForAPIPlane = new ArrayList<>();
        envoySubsetDtoList.stream().forEach(envoySubsetDto -> {
            //静态地址发布，则增加labels
            if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
                HashMap labelMap = new HashMap(1);
                labelMap.put(STATIC_EP_VERSION, envoySubsetDto.getName().toLowerCase());
                envoySubsetDto.setLabels(labelMap);
            }

            //todo subset traffic
            EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto = envoySubsetDto.getTrafficPolicy() == null
                    ? new EnvoyServiceTrafficPolicyDto() : envoySubsetDto.getTrafficPolicy();
            //增加subset健康检查配置
            EnvoyActiveHealthCheckRuleDto envoyActiveHealthCheckRuleDto =
                    envoyHealthCheckService.getActiveHealthCheckRule(serviceProxyDto.getServiceId(), serviceProxyDto.getGwId());
            EnvoyPassiveHealthCheckRuleDto envoyPassiveHealthCheckRuleDto =
                    envoyHealthCheckService.getPassiveHealthCheckRule(serviceProxyDto.getServiceId(), serviceProxyDto.getGwId());
            envoyServiceTrafficPolicyDto.setActiveHealthCheckRule(envoyActiveHealthCheckRuleDto);
            envoyServiceTrafficPolicyDto.setPassiveHealthCheckRule(envoyPassiveHealthCheckRuleDto);
            envoySubsetDtoListForAPIPlane.add(envoySubsetDto);
        });

        return envoySubsetDtoListForAPIPlane;
    }

    private List<EnvoySubsetDto> generateSubsetDtoName(ServiceProxyDto serviceProxyDto, String gwClusterName) {
        if (serviceProxyDto.getSubsets() == null) {
            return Lists.newArrayList();
        }
        String subsetsString = JSON.toJSONString(serviceProxyDto.getSubsets());
        List<EnvoySubsetDto> envoySubsetDtoList = JSON.parseArray(subsetsString, EnvoySubsetDto.class);
        envoySubsetDtoList.stream().map(item -> {
            item.setName((item.getName() + "-" + serviceProxyDto.getServiceId() + "-" + gwClusterName).toLowerCase());
            return item;
        }).collect(Collectors.toList());
        return envoySubsetDtoList;
    }

    @Override
    public List<ServiceProxyInfo> getServiceProxyListByGwId(long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        return serviceProxyDao.getRecordsByField(params);
    }

    @Override
    public List<ServiceProxyInfo> batchGetServiceProxyList(long gwId, List<Long> serviceIdList) {
        return serviceProxyDao.batchGetServiceProxyList(gwId, serviceIdList);
    }

    /**
     * 当需要发送给APIPlane时BackendService，要根据服务注册中心的类型进行调整
     *
     * @param serviceProxyDto
     * @return
     */
    @Override
    public String getBackendServiceSendToApiPlane(ServiceProxyDto serviceProxyDto) {
        String backendService = serviceProxyDto.getBackendService();
        if (!Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
            return backendService;
        }
        RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(serviceProxyDto.getRegistryCenterType());
        if (registryCenterEnum == null) {
            logger.warn("错误的注册中心类型 {}", serviceProxyDto.getRegistryCenterType());
            return backendService;
        }
        if (RegistryCenterEnum.Consul.equals(registryCenterEnum)) {
            RegistryCenterDto registryCenter = registryCenterService.findByType(serviceProxyDto.getRegistryCenterType());
            backendService = String.format(registryCenterEnum.getSuffix(), serviceProxyDto.getBackendService(), registryCenter.getRegistryAlias());
        }

        if (RegistryCenterEnum.Eureka.equals(registryCenterEnum)) {
            //针对nsf eureka做特殊处理，galley上生成se时，其host的格式为{applicationname}.nsf.{projectCode}.eureka
            RegistryCenterDto registryCenterDto = registryCenterService.findByType(serviceProxyDto.getRegistryCenterType());
            if (registryCenterDto != null && registryCenterDto.getRegistryAlias().contains(Const.NSF_EUREKA_ALIAS)) {
                //以nsf eureka发布的服务，其对应的backendservice为{applicationname}.nsf.{projectCode}.eureka
                long projectId = serviceInfoService.getServiceByServiceId(serviceProxyDto.getServiceId()).getProjectId();
                PermissionScopeDto projectScope = gatewayProjectService.getProjectScopeDto(projectId);
                if (projectScope == null || StringUtils.isEmpty(projectScope.getPermissionScopeEnName())) {
                    logger.error("[get backendService] get eureka service projectCode failed, projectScope: {}", projectScope);
                    throw new RuntimeException("未查询到eureka服务所属的projectCode");
                }
                String fullName = serviceProxyDto.getBackendService() +
                        "." +
                        Const.NSF_EUREKA_ALIAS +
                        "." +
                        projectScope.getPermissionScopeEnName();
                backendService = String.format(registryCenterEnum.getSuffix(), fullName).toLowerCase();
            } else {
                backendService = String.format(registryCenterEnum.getSuffix(), serviceProxyDto.getBackendService()).toLowerCase();
            }
        }

        if (RegistryCenterEnum.Zookeeper.equals(registryCenterEnum)) {
            backendService = String.format(registryCenterEnum.getSuffix(), serviceProxyDto.getBackendService());
        }

        if (RegistryCenterEnum.Nacos.equals(registryCenterEnum)) {
            backendService = String.format(registryCenterEnum.getSuffix(), serviceProxyDto.getBackendService());
        }

        /**
         *  fix dns host
         *
         */
        return backendService.replace('_', '-');
    }

    @Override
    public List<String> getSubsetsName(ServiceProxyInfo serviceProxyInfo) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(serviceProxyInfo.getGwId());
        if (gatewayInfo == null) {
            logger.error("获取subsetsname存在脏数据，gwId:{}", serviceProxyInfo.getGwId());
            return Lists.newArrayList();
        }
        List<String> subsetNames = Lists.newArrayList();
        //默认subset
        subsetNames.add((serviceProxyInfo.getCode() + "-" + gatewayInfo.getGwClusterName()).toLowerCase());
        if (StringUtils.isBlank(serviceProxyInfo.getSubsets())) {
            return subsetNames;
        }

        //用户自定义的版本
        List<EnvoySubsetDto> envoySubsetDtos = generateSubsetDtoName(ServiceProxyDto.toDto(serviceProxyInfo), gatewayInfo.getGwClusterName());
        List<String> subSets = envoySubsetDtos.stream().map(EnvoySubsetDto::getName).collect(Collectors.toList());
        subsetNames.addAll(subSets);
        return subsetNames;
    }

    ErrorCode checkRouteMirrorSubset(ServiceProxyDto serviceProxyDto) {
        List<RouteRuleProxyInfo> routeProxyList = envoyRouteRuleProxyService.getRouteRuleProxyListByMirrorServiceIdAndGwId(serviceProxyDto.getServiceId(), serviceProxyDto.getGwId());
        if (routeProxyList == null || routeProxyList.size() == 0) {
            return CommonErrorCode.Success;
        }

        List<EnvoySubsetDto> serviceSubsets = serviceProxyDto.getSubsets();
        Set<String> subsetNameSet = serviceSubsets.stream().map(EnvoySubsetDto::getName).collect(Collectors.toSet());
        for (RouteRuleProxyInfo routeProxyInfo : routeProxyList) {
            if (routeProxyInfo.getMirrorTrafficValue() == null || StringUtils.isBlank(routeProxyInfo.getMirrorTrafficValue().getSubsetName())) {
                continue;
            }
            if (!subsetNameSet.contains(routeProxyInfo.getMirrorTrafficValue().getSubsetName())) {
                RouteRuleInfo routeInfo = envoyRouteRuleInfoService.getRouteRuleInfoById(routeProxyInfo.getRouteRuleId());
                return CommonErrorCode.SubsetUsedByRouteRule(routeInfo.getRouteRuleName());
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<PublishedDetailDto> getPublishedDetailByService(long serviceId) {
        List<ServiceProxyInfo> serviceProxy = getServiceProxyByServiceId(serviceId);
        List<PublishedDetailDto> publishedDetailDtos = new ArrayList<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(serviceProxy)) {
            serviceProxy.forEach(serviceProxyInfo -> {
                PublishedDetailDto publishedDetailDto = new PublishedDetailDto();
                ServiceProxyDto serviceProxyDto = fromMeta(serviceProxyInfo);
                publishedDetailDto.setGwName(serviceProxyDto.getGwName());
                publishedDetailDto.setGwId(serviceProxyDto.getGwId());
                publishedDetailDto.setHealthyStatus(serviceProxyDto.getHealthyStatus());
                publishedDetailDto.setGwType(gatewayInfoService.get(serviceProxyDto.getGwId()).getGwType());
                publishedDetailDto.setServiceAddr(serviceProxyDto.getBackendService().split(","));
                publishedDetailDto.setRegistryCenterAddr(serviceProxyInfo.getRegistryCenterAddr());
                publishedDetailDto.setRegistryCenterType(serviceProxyInfo.getRegistryCenterType());
                publishedDetailDtos.add(publishedDetailDto);
            });
        }
        return publishedDetailDtos;
    }

    /**
     * 服务的过滤条件在本方法中扩展
     * 过滤条件的格式在gportal和api-plane两侧统一，过滤条件的key必须为xxx_的前缀开头，参考"Const.PREFIX_LABEL"
     * 需要对endpoint的什么字段过滤就加上什么前缀，当前共5种前缀，详见"Const.PREFIX_XXX"，过滤Map结构如下
     * {
     * "label_projectCode": "project1", // 过滤label为"projectCode=project1"的endpoint
     * "label_application": "app1",     // 过滤label为"application=app1"的endpoint
     * "action": "function",            // 无效标签，可填写但不使用
     * "host_xxx": "qz.com"             // host值为"qz.com"的endpoint
     * "port_xxx": "8080"               // port值为"8080"的endpoint
     * }
     *
     * @param registry 注册中心
     * @return 服务过滤条件Map
     */
    @Override
    public Map<String, String> createServiceFilters(RegistryCenterDto registry) {
        Map<String, String> filters = new HashMap<>();
        if (registry != null && registry.getRegistryAlias().contains(Const.NSF_EUREKA_ALIAS)) {
            PermissionScopeDto projectScopeDto = gatewayProjectService.getProjectScopeDto(ProjectTraceHolder.getProId());
            filters.put(Const.PREFIX_LABEL + Const.PROJECT_CODE, projectScopeDto.getPermissionScopeEnName());
        }
        return filters;
    }
}
