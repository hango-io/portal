package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.handler.PluginHandler;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyVirtualHostInfoDao;
import org.hango.cloud.dashboard.envoy.dao.IServiceProxyDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPluginManagerDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostUpdateDto;
import org.hango.cloud.dashboard.envoy.web.dto.GrpcEnvoyFilterDto;
import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderDto;
import org.hango.cloud.dashboard.envoy.web.dto.PluginOrderItemDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Envoy网关Service层实现类
 *
 * @author hzchenzhongyang 2020-01-08
 */
@Service
public class EnvoyGatewayServiceImpl implements IEnvoyGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGatewayServiceImpl.class);

    @Autowired
    private IEnvoyVirtualHostInfoDao envoyVirtualHostInfoDao;

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    @Autowired
    private IGatewayProjectService gatewayProjectService;

    @Autowired
    private ApiServerConfig apiServerConfig;

    @Autowired
    private IServiceProxyDao serviceProxyDao;

    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;


    @Override
    public ErrorCode checkVirtualHostList(long gwId, List<EnvoyVirtualHostInfo> vhList) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (null == gatewayInfo || !Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            logger.info("gwId指定的网关不存在，或者不是Envoy网关! gwId:{}", gwId);
            return CommonErrorCode.NoSuchGateway;
        }
        for (EnvoyVirtualHostInfo vhInfo : vhList) {
            ErrorCode checkResult = checkVirtualHost(vhInfo);
            if (!CommonErrorCode.Success.getCode().equals(checkResult.getCode())) {
                return checkResult;
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkCreateVirtualHost(EnvoyVirtualHostInfo vhInfo) {
        EnvoyVirtualHostInfo virtualHostInfo = getVirtualHostByGwIdAndProjectId(vhInfo.getGwId(), vhInfo.getProjectId());
        if (null != virtualHostInfo) {
            logger.info("对应的virtual host已存在! gwId:{}, projectId:{}", vhInfo.getGwId(), vhInfo.getProjectId());
            return CommonErrorCode.VirtualHostAlreadyExist;
        }
        return checkVirtualHost(vhInfo);
    }

    @Override
    public boolean createVirtualHost(EnvoyVirtualHostInfo vhInfo) {
        vhInfo.setCreateTime(System.currentTimeMillis());
        vhInfo.setUpdateTime(System.currentTimeMillis());
        PermissionScopeDto project = gatewayProjectService.getProjectScopeDto(vhInfo.getProjectId());
        if (null == project || 0 == project.getId()) {
            throw new RuntimeException("项目id对应的项目不存在!");
        }
        vhInfo.setVirtualHostCode(project.getPermissionScopeEnName() + "-" + vhInfo.getProjectId() + "-" + vhInfo.getGwId());
        boolean addProjectId = updateProjectId(vhInfo.getGwId(), vhInfo.getProjectId(), "add");
        if (!addProjectId) {
            return false;
        }
        envoyVirtualHostInfoDao.add(vhInfo);
        return true;
    }

    private boolean updateProjectId(long gwId, long projectId, String action) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (null == gatewayInfo) {
            return false;
        }
        List<Long> projectIdList = Lists.newArrayList();
        if (!StringUtils.isEmpty(gatewayInfo.getProjectId())) {
            projectIdList = gatewayInfo.getProjectIdList();
        }
        if ("add".equals(action) && !projectIdList.contains(projectId)) {
            projectIdList.add(projectId);
        } else if ("delete".equals(action)) {
            projectIdList.remove(Long.valueOf(projectId));
        }
        gatewayInfo.setProjectId(String.join(",", projectIdList.stream().map(String::valueOf).collect(Collectors.toList())));
        gatewayInfoService.updateGwInfo(gatewayInfo, true);
        return true;
    }

    private ErrorCode checkVirtualHost(EnvoyVirtualHostInfo vhInfo) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(vhInfo.getGwId());
        if (null == gatewayInfo) {
            logger.info("传入网关Id对应的网关不存在！ gwId:{}", vhInfo.getGwId());
            return CommonErrorCode.NoSuchGateway;
        }

//        PermissionScopeDto project = gatewayProjectService.getProjectScopeDto(vhInfo.getProjectId());
//        if (null == project || 0 == project.getId()) {
//            logger.info("传入的项目id对应的项目不存在! projectId:{}", vhInfo.getProjectId());
//            return CommonErrorCode.NoSuchProject;
//        }

        return checkVirtualHost(vhInfo.getBindType(), vhInfo.getHostList(), vhInfo.getProjectList(), vhInfo.getGwId(), vhInfo.getProjectId());
    }

    @Override
    public ErrorCode checkUpdateVirtualHost(long virtualHostId, List<String> hostList, String bindType, List<Long> projectList) {
        EnvoyVirtualHostInfo vhInfo = getVirtualHost(virtualHostId);
        if (null == vhInfo) {
            logger.info("vh id指定的vh不存在! virtualHostId:{}", virtualHostId);
            return CommonErrorCode.NoSuchVirtualHost;
        }
        return checkVirtualHost(bindType, hostList, projectList, vhInfo.getGwId(), vhInfo.getProjectId());
    }

    private ErrorCode checkVirtualHost(String bindType, List<String> hostList, List<Long> projectList, long gwId, long projectId) {
        if ("host".equals(bindType)) {
            if (CollectionUtils.isEmpty(hostList)) {
                logger.info("域名列表不允许为空!");
                return CommonErrorCode.InvalidParameterValue(hostList, "Hosts", "域名列表不允许为空");
            }

            for (String host : hostList) {
                if (host.startsWith("*") || host.contains(":")) {
                    logger.info("域名不正确! 不允许最左侧为*（不支持泛域名）, host:{}", host);
                    return CommonErrorCode.InvalidParameterValue(hostList, "Hosts", "不支持泛域名，host: " + host);
                }
            }

            List<String> repetitiveHosts = findExistHosts(hostList, gwId, projectId);
            if (!CollectionUtils.isEmpty(repetitiveHosts)) {
                logger.info("存在域名冲突，冲突域名列表, repetitiveHosts:{}", repetitiveHosts);
                return CommonErrorCode.InvalidParameterValue(hostList, "Hosts", "域名已被其他项目关联：" + repetitiveHosts);
            }
        }
        if ("project".equals(bindType)) {
            if (CollectionUtils.isEmpty(projectList)) {
                logger.info("关联项目列表不允许为空!");
                return CommonErrorCode.InvalidParameterValue(projectList, "Projects", "项目列表不允许为空");
            }
            Set<Long> hostTypeVhProject = getHostTypeVhProject(gwId);
            for (long id : projectList) {
                if (!hostTypeVhProject.contains(id)) {
                    logger.info("关联项目非域名绑定类型，不允许关联");
                    return CommonErrorCode.InvalidParameterValue(id, "Projects", "项目id非域名绑定类型");
                }
            }
        }
        return CommonErrorCode.Success;
    }

    @Override
    public List<EnvoyVirtualHostInfo> getVirtualHostListByGwId(long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        return envoyVirtualHostInfoDao.getRecordsByField(params).stream().
                map(envoyVirtualHostInfo -> getVirtualHost(envoyVirtualHostInfo)).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateVirtualHostList(long gwId, List<EnvoyVirtualHostInfo> vhList) {
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (null == gatewayInfo) {
            return false;
        }
        List<EnvoyVirtualHostInfo> vhListInDb = getVirtualHostListByGwId(gwId);

        Set<Long> projectIdSet = vhList.stream().map(EnvoyVirtualHostInfo::getProjectId).collect(Collectors.toSet());
        Set<Long> projectIdSetInDb = vhListInDb.stream().map(EnvoyVirtualHostInfo::getProjectId).collect(Collectors.toSet());
        Map<Long, EnvoyVirtualHostInfo> vhMapInDb = vhListInDb.stream().collect(Collectors.toMap(EnvoyVirtualHostInfo::getProjectId, item -> item));

        List<EnvoyVirtualHostInfo> vhListToAdd = vhList.stream().filter(item -> !projectIdSetInDb.contains(item.getProjectId())).collect(Collectors.toList());
        vhListToAdd.forEach(item -> {
            item.setCreateTime(System.currentTimeMillis());
            item.setUpdateTime(System.currentTimeMillis());
            PermissionScopeDto project = gatewayProjectService.getProjectScopeDto(item.getProjectId());
            if (null == project || 0 == project.getId()) {
                throw new RuntimeException("项目id对应的项目不存在!");
            }
            item.setVirtualHostCode(project.getPermissionScopeEnName() + "-" + item.getProjectId() + "-" + gwId);
            envoyVirtualHostInfoDao.add(item);
        });

        List<EnvoyVirtualHostInfo> vhListToDelete = vhListInDb.stream().filter(item -> !projectIdSet.contains(item.getProjectId())).collect(Collectors.toList());
        vhListToDelete.forEach(item -> envoyVirtualHostInfoDao.delete(item));

        List<EnvoyVirtualHostInfo> vhListToUpdate = vhList.stream().filter(item -> projectIdSetInDb.contains(item.getProjectId())).collect(Collectors.toList());
        vhListToUpdate.forEach(item -> {
            EnvoyVirtualHostInfo vhInDb = vhMapInDb.get(item.getProjectId());
            vhInDb.setUpdateTime(System.currentTimeMillis());
            vhInDb.setHosts(item.getHosts());
            envoyVirtualHostInfoDao.update(vhInDb);
        });

        gatewayInfo.setProjectId(String.join(",", projectIdSet.stream().map(String::valueOf).collect(Collectors.toList())));
        gatewayInfoService.updateGwInfo(gatewayInfo, true);

        return true;
    }

    @Override
    public EnvoyVirtualHostInfo getVirtualHostByGwIdAndProjectId(long gwId, long projectId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("projectId", projectId);
        List<EnvoyVirtualHostInfo> virtualHostInfoList = envoyVirtualHostInfoDao.getRecordsByField(params);
        EnvoyVirtualHostInfo envoyVirtualHostInfo = CollectionUtils.isEmpty(virtualHostInfoList) ? null : virtualHostInfoList.get(0);
        return getVirtualHost(envoyVirtualHostInfo);
    }

    @Override
    public boolean updateVirtualHost(EnvoyVirtualHostUpdateDto envoyVirtualHostUpdateDto) {
        EnvoyVirtualHostInfo vhInfo = getVirtualHost(envoyVirtualHostUpdateDto.getVirtualHostId());
        if (null == vhInfo) {
            return false;
        }
        vhInfo.setBindType(envoyVirtualHostUpdateDto.getBindType());
        vhInfo.setHosts(JSON.toJSONString(envoyVirtualHostUpdateDto.getHostList()));
        vhInfo.setProjects(JSON.toJSONString(envoyVirtualHostUpdateDto.getProjectList()));
        vhInfo.setUpdateTime(System.currentTimeMillis());
        envoyVirtualHostInfoDao.update(vhInfo);
        return true;
    }


    @Override
    public List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, List<Long> projectIdList, String domain) {
        //基于projectId查询vh
        List<EnvoyVirtualHostInfo> virtualHostList1 = envoyVirtualHostInfoDao.getVirtualHostList(gwId, projectIdList, null, null);
        /**
         * 基于domain查询vh
         * 1.查询hosts like domain 的所有host类型vh virtualHostList1
         * 2.基于virtualHostList1查询关联的project类型的vh  virtualHostList2
         */
        List<EnvoyVirtualHostInfo> virtualHostList2 = envoyVirtualHostInfoDao.getVirtualHostList(gwId, null, domain, "host");
        if (!CollectionUtils.isEmpty(virtualHostList2)) {
            List<Long> projectIds = virtualHostList2.stream().map(EnvoyVirtualHostInfo::getProjectId).collect(Collectors.toList());
            List<EnvoyVirtualHostInfo> virtualHostList3 = getVirtualHostList(gwId, projectIds);
            if (!CollectionUtils.isEmpty(virtualHostList3)) {
                virtualHostList2.addAll(virtualHostList3);
            }
        }
        //基于id取projectId和domain交集
        List<EnvoyVirtualHostInfo> virtualHostList = virtualHostList1.stream()
                .filter(item -> virtualHostList2.stream().map(EnvoyVirtualHostInfo::getId)
                        .collect(Collectors.toList()).contains(item.getId()))
                .collect(Collectors.toList());
        return virtualHostList.stream().map(this::getVirtualHost).collect(Collectors.toList());
    }


    private List<EnvoyVirtualHostInfo> getVirtualHostList(long gwId, List<Long> projects) {
        List<EnvoyVirtualHostInfo> virtualHostList = envoyVirtualHostInfoDao.getVirtualHostList(gwId, "project");
        return virtualHostList.stream().filter(o -> !Collections.disjoint(o.getProjectList(), projects)).collect(Collectors.toList());
    }

    @Override
    public EnvoyVirtualHostInfo getVirtualHost(long virtualHostId) {
        EnvoyVirtualHostInfo envoyVirtualHostInfo = envoyVirtualHostInfoDao.get(virtualHostId);
        return getVirtualHost(envoyVirtualHostInfo);
    }

    private EnvoyVirtualHostInfo getVirtualHost(EnvoyVirtualHostInfo envoyVirtualHostInfo) {
        if (envoyVirtualHostInfo == null || Const.HOST_BINDING_TYPE.equals(envoyVirtualHostInfo.getBindType()))
            return envoyVirtualHostInfo;
        List<String> hostList = getHostsFromHostBindingType(envoyVirtualHostInfo.getProjectList(), envoyVirtualHostInfo.getGwId());
        envoyVirtualHostInfo.setHostList(hostList);
        envoyVirtualHostInfo.setHosts(JSON.toJSONString(hostList));
        return envoyVirtualHostInfo;
    }

    private List<String> getHostsFromHostBindingType(List<Long> projects, long gwId) {
        List<EnvoyVirtualHostInfo> envoyVirtualHostInfos = projects.stream().
                map(id -> envoyVirtualHostInfoDao.getVirtualHostInfo(gwId, id)).collect(Collectors.toList());
        List<String> hosts = Lists.newArrayList();
        envoyVirtualHostInfos.forEach(envoyVirtualHostInfo -> {
            hosts.addAll(envoyVirtualHostInfo.getHostList());
        });
        return hosts;
    }

    @Override
    public ErrorCode checkDeleteVirtualHost(long virtualHostId) {
        EnvoyVirtualHostInfo virtualHostInfo = getVirtualHost(virtualHostId);
        if (null == virtualHostInfo) {
            return CommonErrorCode.NoSuchVirtualHost;
        }
        long projectId = virtualHostInfo.getProjectId();
        if (projectId <= 0) {
            return CommonErrorCode.EmptyProjectId;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("projectId", projectId);
        int publishedServiced = serviceProxyDao.getCountByFields(params);
        if (publishedServiced > 0) {
            return CommonErrorCode.ExistPublishedService;
        }
        //如果当前项目被其他项目关联 则当前关联关系不允许取消
        List<EnvoyVirtualHostInfo> all = envoyVirtualHostInfoDao.findAll();
        List<Long> projectLists = all.stream().filter(vs -> !CollectionUtils.isEmpty(vs.getProjectList())).flatMap(vs -> vs.getProjectList().stream()).collect(Collectors.toList());
        if (projectLists.contains(projectId)) {
            return CommonErrorCode.ExistRelationWithOtherProject;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode deleteVirtualHost(long virtualHostId) {
        EnvoyVirtualHostInfo virtualHostInfo = getVirtualHost(virtualHostId);
        if (null == virtualHostInfo) {
            return CommonErrorCode.Success;
        }

        boolean deleteProjectId = updateProjectId(virtualHostInfo.getGwId(), virtualHostInfo.getProjectId(), "delete");
        if (!deleteProjectId) {
            return CommonErrorCode.InternalServerError;
        }

        envoyVirtualHostInfoDao.delete(virtualHostInfo);
        return CommonErrorCode.Success;
    }

    private List<String> findExistHosts(List<String> hosts, long gwId, long projectId) {
        if (CollectionUtils.isEmpty(hosts)) {
            return Lists.newArrayList();
        }

        // 由于流量灰度时会两个物理网关共用一个域名，所以virtual host的域名配置冲突也仅在同一个网关中校验
        List<EnvoyVirtualHostInfo> allVirtualHost = getVirtualHostListByGwId(gwId);
        if (CollectionUtils.isEmpty(allVirtualHost)) {
            return Lists.newArrayList();
        }

        Set<String> alreadyHosts = allVirtualHost.stream()
                .filter(item -> item.getProjectId() != projectId)
                .filter(item -> item.getBindType().equals(Const.HOST_BINDING_TYPE))
                .flatMap(item -> item.getHostList().stream())
                .collect(Collectors.toSet());

        return hosts.stream()
                .filter(alreadyHosts::contains)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnvoyVirtualHostInfo> getProjectTypeVhByProjectId(Long projectId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", projectId);
        params.put("bindType", "project");
        return envoyVirtualHostInfoDao.getRecordsByField(params);
    }

    @Override
    public Set<Long> getHostTypeVhProject(long gwId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("gwId", gwId);
        params.put("bindType", "host");
        List<EnvoyVirtualHostInfo> envoyVirtualHostInfos = envoyVirtualHostInfoDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(envoyVirtualHostInfos)) return new HashSet<>(0);
        return envoyVirtualHostInfos.stream().map(EnvoyVirtualHostInfo::getProjectId).collect(Collectors.toSet());
    }


    @Override
    public List<EnvoyPluginManagerDto> getEnvoyPluginManager(GatewayInfo gatewayInfo) {
        if (gatewayInfo == null) {
            return Collections.emptyList();
        }
        List<PluginOrderItemDto> envoyPluginManager = getEnvoyPluginManager(gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName());
        return envoyPluginManager.stream().map(e -> toPluginManagerDto(e, apiServerConfig.getPluginManagerMap())).filter(e -> !PluginHandler.pluginIgnoreList.contains(e.getName())).collect(Collectors.toList());
    }

    public EnvoyPluginManagerDto toPluginManagerDto(PluginOrderItemDto item, Map<String, String> extra) {
        EnvoyPluginManagerDto envoyPluginManagerDto = new EnvoyPluginManagerDto();
        envoyPluginManagerDto.setEnable(item.getEnable());
        String name = PluginHandler.pluginUseSubNameList.containsKey(item.getName()) ?
                PluginHandler.pluginUseSubNameList.get(item.getName()).getName(item) : item.getName();
        envoyPluginManagerDto.setName(name);
        String displayName = extra.get(name);
        envoyPluginManagerDto.setDisplayName(StringUtils.isEmpty(displayName) ? name : displayName);
        return envoyPluginManagerDto;
    }

    @Override
    public List<PluginOrderItemDto> getEnvoyPluginManager(String apiPlaneAddr, String gwClusterName) {

        PluginOrderDto pluginOrderDto = new PluginOrderDto();
        HashMap<String, String> gatewayLabels = Maps.newHashMap();
        gatewayLabels.put("gw_cluster", gwClusterName);
        pluginOrderDto.setGatewayLabels(gatewayLabels);
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "GetPluginOrder");
        params.put("Version", "2019-07-25");

        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(apiPlaneAddr + "/api", params, JSON.toJSONString(pluginOrderDto), null, HttpMethod.POST.name());
        if (null == response) {
            return Collections.emptyList();
        }

        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("获取网关插件配置失败，返回http status code非2xx，httpStatusCode:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSON.parseObject(response.getResponseBody());
        PluginOrderDto result = jsonObject.getObject("Result", PluginOrderDto.class);
        if (result == null) {
            logger.info("未能找到对应网关的插件配置");
            return Collections.emptyList();
        }
        List<PluginOrderItemDto> plugins = result.getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            logger.info("网关对应的插件配置为空");
            return Collections.emptyList();
        }
        return result.getPlugins();
    }

    @Override
    public ErrorCode checkEnvoyPluginManager(GatewayInfo gatewayInfo, String name, boolean enable) {
        if (gatewayInfo == null) {
            return CommonErrorCode.NoSuchGateway;
        }
        List<PluginOrderItemDto> envoyPluginManager = getEnvoyPluginManager(gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName());
        Optional<PluginOrderItemDto> plugin = envoyPluginManager.stream().filter(e -> (PluginHandler.pluginUseSubNameList.containsKey(e.getName()) ?
                PluginHandler.pluginUseSubNameList.get(e.getName()).getName(e) : e.getName()).equals(name)).findFirst();
        if (!plugin.isPresent()) {
            return CommonErrorCode.InvalidPluginName;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean updateEnvoyPluginManager(GatewayInfo gatewayInfo, String name, boolean enable) {
        if (gatewayInfo == null) {
            logger.info("网关信息为空");
            return false;
        }
        List<PluginOrderItemDto> envoyPluginManager = getEnvoyPluginManager(gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName());
        envoyPluginManager.stream().filter(e -> {
                    String itemName = PluginHandler.pluginUseSubNameList.containsKey(e.getName()) ?
                            PluginHandler.pluginUseSubNameList.get(e.getName()).getName(e) : e.getName();
                    if (itemName.equals(name)) {
                        e.setEnable(enable);
                    }
                    return true;
                }
        ).collect(Collectors.toList());

        return publishPluginToAPIPlane(gatewayInfo, envoyPluginManager);
    }

    @Override
    public Boolean publishPluginToAPIPlane(GatewayInfo gatewayInfo, List<PluginOrderItemDto> envoyPluginManager) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishPluginOrder");
        params.put("Version", "2019-07-25");

        PluginOrderDto pluginOrderDto = new PluginOrderDto();
        HashMap<String, String> gatewayLabels = Maps.newHashMap();
        gatewayLabels.put("gw_cluster", gatewayInfo.getGwClusterName());
        pluginOrderDto.setGatewayLabels(gatewayLabels);
        pluginOrderDto.setPlugins(envoyPluginManager);

        String body = JSON.toJSONString(pluginOrderDto);
        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api", params, body, null, HttpMethod.POST.name());
        if (null == response) {
            return false;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane删除服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

    @Override
    public boolean publishGrpcEnvoyFilterToAPIPlane(int listenerPort, String apiPlaneAddr, String gwClusterName, String protoDescriptorBin, List<String> services) {
        Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "PublishGrpcEnvoyFilter");
        params.put("Version", "2019-07-25");

        GrpcEnvoyFilterDto grpcEnvoyFilterDto = new GrpcEnvoyFilterDto(null, gwClusterName, listenerPort, protoDescriptorBin, services);
        String body = JSON.toJSONString(grpcEnvoyFilterDto);

        HttpClientResponse response = HttpCommonUtil.getFromApiPlane(apiPlaneAddr + "/api", params, body, null, HttpMethod.POST.name());
        if (null == response) {
            return false;
        }
        if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
            logger.error("调用api-plane发布grpc EnvoyFilter接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return false;
        }
        return true;
    }

}
