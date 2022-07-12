package org.hango.cloud.dashboard.apiserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.CommonUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleInfoDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyCopyRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyCopyRulePortDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteStringMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 路由规则管理Service层实现类
 *
 * @author hzchenzhongyang 2019-09-11
 * @modified hanjiahao
 */
@Service
public class RouteRuleInfoServiceImpl implements IRouteRuleInfoService {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoServiceImpl.class);

    @Autowired
    private IRouteRuleInfoDao routeRuleInfoDao;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IEnvoyPluginInfoService envoyPluginInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Override
    public ErrorCode checkAddParam(RouteRuleDto ruleDto) {
        RouteRuleInfo sameNameRule = getRouteRuleInfoByName(ruleDto.getRouteRuleName().trim());
        if (null != sameNameRule) {
            logger.info("同名规则已存在，不允许重复创建");
            return CommonErrorCode.SameNameRouteRuleExist;
        }
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(ruleDto.getServiceId());
        if (serviceInfoDb == null) {
            logger.info("创建路由规则，所属服务不存在");
            return CommonErrorCode.NoSuchService;
        }
        EnvoyRouteStringMatchDto uriMatchDto = ruleDto.getUriMatchDto();
        if (CollectionUtils.isEmpty(uriMatchDto.getValue())) {
            logger.info("创建路由规则，uri path为空");
            return CommonErrorCode.NoRouteRulePath;
        }
        //正则中不允许出现nginx捕获正则
        if (!Const.URI_TYPE_EXACT.equals(uriMatchDto.getType()) &&
                uriMatchDto.getValue().stream().anyMatch(path -> Pattern.matches(Const.NGINX_CAPTURE_REGEX, path))) {
            logger.info("创建路由，path中包含nginx 捕获正则，不允许创建");
            return CommonErrorCode.RouteRuleContainsNginxCapture;
        }
        if (ruleDto.getMethodMatchDto() != null && !Const.CONST_METHODS.containsAll(ruleDto.getMethodMatchDto().getValue())) {
            return CommonErrorCode.RouteRuleMethodInvalid;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean isSameRouteRuleInfo(RouteRuleInfo routeRuleInfo) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("uri", routeRuleInfo.getUri());
        if (StringUtils.isNotBlank(routeRuleInfo.getMethod())) {
            params.put("method", routeRuleInfo.getMethod());
        }
        if (StringUtils.isNotBlank(routeRuleInfo.getHost())) {
            params.put("host", routeRuleInfo.getHost());
        }
        if (StringUtils.isNotBlank(routeRuleInfo.getHeader())) {
            params.put("header", routeRuleInfo.getHeader());
        }
        if (StringUtils.isNotBlank(routeRuleInfo.getQueryParam())) {
            params.put("queryParam", routeRuleInfo.getQueryParam());
        }
        params.put("projectId", routeRuleInfo.getProjectId());
        params.put("priority", routeRuleInfo.getPriority());
        List<RouteRuleInfo> routeRuleInfos = routeRuleInfoDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(routeRuleInfos)) {
            return false;
        }
        List<RouteRuleInfo> equalsRouteRule = routeRuleInfos.stream()
                .filter(item -> item.isSame(routeRuleInfo))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(equalsRouteRule)) {
            return false;
        }
        //更新路由规则,如果两个对象routeId相同，则认为不重复，允许更新
        if (CollectionUtils.isNotEmpty(equalsRouteRule) && equalsRouteRule.size() == 1) {
            return routeRuleInfo.getId() != equalsRouteRule.get(0).getId();
        }
        return true;
    }

    @Override
    public long addRouteRule(RouteRuleInfo routeRuleInfo) {
        if (null == routeRuleInfo) {
            logger.error("添加路由规则时传入的路由规则为空!");
            return Const.ERROR_RESULT;
        }
        routeRuleInfo.setCreateTime(System.currentTimeMillis());
        routeRuleInfo.setUpdateTime(System.currentTimeMillis());
        return routeRuleInfoDao.add(routeRuleInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long copyRouteRule(EnvoyCopyRuleDto copyRuleDto) {
        RouteRuleInfo routRuleDb = getRouteRuleInfoById(copyRuleDto.getRouteRuleId());
        routRuleDb.setServiceId(copyRuleDto.getServiceId());
        routRuleDb.setCreateTime(System.currentTimeMillis());
        routRuleDb.setUpdateTime(System.currentTimeMillis());
        long orders = routRuleDb.getOrders() + (copyRuleDto.getPriority() - routRuleDb.getPriority()) * 1000000;
        routRuleDb.setOrders(orders);
        routRuleDb.setPriority(copyRuleDto.getPriority());
        routRuleDb.setRouteRuleName(copyRuleDto.getRouteRuleName());
        routRuleDb.setDescription(copyRuleDto.getDescription());
        routRuleDb.setRouteRuleSource("Gateway");
        //复制的路由，均属于未发布
        routRuleDb.setPublishStatus(0);
        long destinationRouteId = routeRuleInfoDao.add(routRuleDb);
        if (!CollectionUtils.isEmpty(copyRuleDto.getDestinationPort())) {
            try {
                copyPluginInfo(copyRuleDto.getRouteRuleId(), destinationRouteId, copyRuleDto.getServiceId(), copyRuleDto.getDestinationPort());
            } catch (Exception e) {
                logger.info("复制路由规则，携带插件发布至对应网关，出现异常，仅复制元信息");
            }
        }
        return destinationRouteId;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean copyPluginInfo(long originRouteId, long destinationRouteId, long serviceId, List<EnvoyCopyRulePortDto> copyRulePortDtos) throws Exception {
        //不传port，gwId直接返回false
        if (CollectionUtils.isEmpty(copyRulePortDtos)) {
            return true;
        }
        try {
            for (EnvoyCopyRulePortDto copyRulePortDto : copyRulePortDtos) {

                RouteRuleProxyDto routeRulePublishDto = new RouteRuleProxyDto();
                routeRulePublishDto.setRouteRuleId(destinationRouteId);
                routeRulePublishDto.setServiceId(serviceId);
                //设置为非使能
                routeRulePublishDto.setEnableState(Const.ROUTE_RULE_DISABLE_STATE);
                routeRulePublishDto.setGwId(copyRulePortDto.getGwId());
                List<EnvoyDestinationDto> destinationServices = new ArrayList<>();
                EnvoyDestinationDto envoyDestinationDto = new EnvoyDestinationDto();
                envoyDestinationDto.setServiceId(serviceId);
                envoyDestinationDto.setPort(copyRulePortDto.getPort());
                destinationServices.add(envoyDestinationDto);
                routeRulePublishDto.setDestinationServices(destinationServices);
                //源路由未发布到该网关，不携带插件
                if (routeRuleProxyService.getRouteRuleProxyCount(copyRulePortDto.getGwId(), originRouteId) == 0) {
                    routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRulePublishDto), Lists.newArrayList(), true);
                } else {
                    List<EnvoyPluginBindingInfo> alreadyBindingPlugins = envoyPluginInfoService.getPluginBindingList(copyRulePortDto.getGwId(), String.valueOf(originRouteId),
                            EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
                    if (CollectionUtils.isNotEmpty(alreadyBindingPlugins)) {
                        alreadyBindingPlugins.forEach(envoyPluginBindingInfo -> {
                            envoyPluginBindingInfo.setBindingObjectId(String.valueOf(destinationRouteId));
                            envoyPluginBindingInfo.setCreateTime(System.currentTimeMillis());
                            envoyPluginBindingInfo.setUpdateTime(System.currentTimeMillis());
                            envoyPluginInfoService.bindingPluginToDb(envoyPluginBindingInfo);
                        });
                    }
                    List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
                    routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRulePublishDto), newPluginConfigurations, true);
                }
            }
        } catch (Exception e) {
            logger.info("复制路由规则，发布网关，出现异常，e:{}", e);
            throw new Exception();
        }
        return true;
    }

    @Override
    public RouteRuleInfo getRouteRuleInfoByName(String routeRuleName) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("routeRuleName", routeRuleName);
        params.put("projectId", ProjectTraceHolder.getProId());
        List<RouteRuleInfo> routeRuleInfos = routeRuleInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(routeRuleInfos) ? null : routeRuleInfos.get(0);
    }

    @Override
    public ErrorCode checkUpdateParam(RouteRuleDto routeRuleDto) {
        RouteRuleInfo ruleInDB = getRouteRuleInfoById(routeRuleDto.getId());
        if (null == ruleInDB) {
            logger.info("指定的路由规则不存在，id:{}", routeRuleDto.getId());
            return CommonErrorCode.NoSuchRouteRule;
        }
        RouteRuleInfo sameNameRule = getRouteRuleInfoByName(routeRuleDto.getRouteRuleName().trim());
        if (null != sameNameRule && routeRuleDto.getId() != sameNameRule.getId()) {
            logger.info("同名规则已存在，不允许重复创建");
            return CommonErrorCode.SameNameRouteRuleExist;
        }
        EnvoyRouteStringMatchDto uriMatchDto = routeRuleDto.getUriMatchDto();
        if (CollectionUtils.isEmpty(uriMatchDto.getValue())) {
            logger.info("更新路由规则，uri path为空，不允许更新");
            return CommonErrorCode.NoRouteRulePath;
        }
        //正则中不允许出现nginx捕获正则
        if (!Const.URI_TYPE_EXACT.equals(uriMatchDto.getType()) &&
                uriMatchDto.getValue().stream().anyMatch(path -> Pattern.matches(Const.NGINX_CAPTURE_REGEX, path))) {
            logger.info("更新路由，path中包含nginx 捕获正则，不允许更新");
            return CommonErrorCode.RouteRuleContainsNginxCapture;
        }
        if (routeRuleDto.getMethodMatchDto() != null && !Const.CONST_METHODS.containsAll(routeRuleDto.getMethodMatchDto().getValue())) {
            logger.info("更新路由，method参数填写不正确");
            return CommonErrorCode.RouteRuleMethodInvalid;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkDeleteParam(long id) {
        RouteRuleInfo ruleInDB = getRouteRuleInfoById(id);
        if (CollectionUtils.isNotEmpty(routeRuleProxyService.getRouteRuleProxyByRouteRuleId(id))) {
            logger.info("删除路由规则，已发布路由列表中仍然存在数据");
            return CommonErrorCode.RouteRuleAlreadyPublished;
        }
        if (null != ruleInDB && ruleInDB.getPublishStatus() == NumberUtils.INTEGER_ONE) {
            logger.info("删除路由规则，已发布路由列表不存在数据，路由列表存在脏数据，需要fix数据");
            return CommonErrorCode.RouteRuleAlreadyPublished;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public ErrorCode checkCopyParam(EnvoyCopyRuleDto copyRuleDto) {
        RouteRuleInfo ruleInDB = getRouteRuleInfoById(copyRuleDto.getRouteRuleId());
        if (ruleInDB == null) {
            logger.info("复制路由规则，路由规则不存在");
            return CommonErrorCode.NoSuchRouteRule;
        }
        if (ruleInDB.getPriority() == copyRuleDto.getPriority()) {
            logger.info("复制路由规则，未修改路由规则优先级，不允许复制，priority:{}", copyRuleDto.getPriority());
            return CommonErrorCode.NotModifyPriority;
        }
        if (ruleInDB.getRouteRuleName().equals(copyRuleDto.getRouteRuleName())) {
            logger.info("复制路由规则，未修改路由规则名称，不允许复制，routeRuleName:{}", copyRuleDto.getRouteRuleName());
            return CommonErrorCode.NotModifyRouteRuleName;
        }
        if (getRouteRuleInfoByName(copyRuleDto.getRouteRuleName()) != null) {
            logger.info("复制路由规则，route名称冲突，不允许复制");
            return CommonErrorCode.SameNameRouteRuleExist;
        }
        if (serviceInfoService.getServiceByServiceId(copyRuleDto.getServiceId()) == null) {
            logger.info("复制路由规则，目的服务不存在，serviceId:{}", copyRuleDto.getServiceId());
            return CommonErrorCode.NoSuchService;
        }
        ruleInDB.setPriority(copyRuleDto.getPriority());
        if (isSameRouteRuleInfo(ruleInDB)) {
            logger.info("复制路由规则，存在完全相同的路由，不允许复制");
            return CommonErrorCode.SameParamRouteRuleExist;
        }

        List<EnvoyCopyRulePortDto> destinationPort = copyRuleDto.getDestinationPort();
        if (CollectionUtils.isEmpty(destinationPort)) {
            return CommonErrorCode.Success;
        }
        List<ServiceProxyInfo> serviceProxyInfos = serviceProxyService.getServiceProxyByServiceId(copyRuleDto.getServiceId());
        if (CollectionUtils.isEmpty(serviceProxyInfos)) {
            logger.info("复制路由规则，服务未发布，仍填写复制网关信息");
            return CommonErrorCode.ServiceNotPublished;
        }
        List<Long> gwIds = serviceProxyInfos.stream().map(ServiceProxyInfo::getGwId).collect(Collectors.toList());
        List<Long> destionGwIds = destinationPort.stream().map(EnvoyCopyRulePortDto::getGwId).collect(Collectors.toList());
        if (!gwIds.containsAll(destionGwIds)) {
            logger.info("复制路由规则，存在服务未发布的网关信息，不允许复制");
            return CommonErrorCode.ServiceNotPublished;
        }
        return CommonErrorCode.Success;
    }

    @Override
    public boolean updateRouteRule(RouteRuleInfo routeRuleInfo) {
        RouteRuleInfo ruleInDB = getRouteRuleInfoById(routeRuleInfo.getId());
        if (null == ruleInDB) {
            logger.error("更新路由规则时id指定的路由规则已不存在，请检查! id:{}", routeRuleInfo.getId());
            return false;
        }
        routeRuleInfo.setProjectId(ruleInDB.getProjectId());
        routeRuleInfo.setUpdateTime(System.currentTimeMillis());
        routeRuleInfoDao.update(routeRuleInfo);
        return true;
    }

    @Override
    public RouteRuleInfo getRouteRuleInfoById(long id) {
        return routeRuleInfoDao.get(id);
    }

    @Override
    public ErrorCode checkDescribeParam(String sortKey, String sortValue, long offset, long limit) {
        if (StringUtils.isNotBlank(sortKey) && !Const.SORT_KEY.contains(sortKey)) {
            return CommonErrorCode.SortKeyInvalid;
        }
        if (StringUtils.isNotBlank(sortValue) && !Const.SORT_VALUE.contains(sortValue)) {
            return CommonErrorCode.SortValueInvalid;
        }
        return CommonUtil.checkOffsetAndLimit(offset, limit);
    }

    @Override
    public List<RouteRuleInfo> getRouteRuleInfoByPattern(String pattern, int publishStatus, long serviceId, long projectId,
                                                         String sortKey, String sortValue, long offset, long limit) {

        List<RouteRuleInfo> routeRuleInfos;
        //查询所有服务下的路由规则
        if (serviceId == 0) {
            routeRuleInfos = routeRuleInfoDao.getRuleInfoByLimit(pattern, publishStatus, projectId, sortKey, sortValue, offset, limit);
        } else {
            routeRuleInfos = routeRuleInfoDao.getRuleInfoByServiceLimit(pattern, publishStatus, serviceId, sortKey, sortValue, offset, limit);
        }
        return routeRuleInfos;
    }

    @Override
    public long getRouteRuleInfoCount(String pattern, int publishStatus, long serviceId, long projectId) {
        //查询所有服务下的路由规则数量
        if (serviceId == 0) {
            return routeRuleInfoDao.getRuleInfoCount(pattern, publishStatus, projectId);
        }
        return routeRuleInfoDao.getRuleInfoByServiceCount(pattern, publishStatus, serviceId);
    }

    @Override
    public boolean deleteRouteRule(long id) {
        RouteRuleInfo routeRuleInfo = routeRuleInfoDao.get(id);
        if (routeRuleInfo != null) {
            routeRuleInfoDao.delete(routeRuleInfo);
        }
        return true;
    }

    @Override
    public RouteRuleDto fromMeta(RouteRuleInfo ruleInfo) {
        RouteRuleDto ruleDto = new RouteRuleDto();
        ruleDto.setId(ruleInfo.getId());
        ruleDto.setServiceId(ruleInfo.getServiceId());
        ruleDto.setServiceName(serviceInfoService.getServiceByServiceId(ruleInfo.getServiceId()).getDisplayName());
        ruleDto.setServiceType(serviceInfoService.getServiceByServiceId(ruleInfo.getServiceId()).getServiceType());
        ruleDto.setCreateTime(ruleInfo.getCreateTime());
        ruleDto.setUpdateTime(ruleInfo.getUpdateTime());
        ruleDto.setRouteRuleName(ruleInfo.getRouteRuleName());
        ruleDto.setPublishStatus(ruleInfo.getPublishStatus());
        ruleDto.setPriority(ruleInfo.getPriority());
        ruleDto.setRouteRuleSource(ruleInfo.getRouteRuleSource());

        if (StringUtils.isNotBlank(ruleInfo.getHeaderOperation())) {
            ruleDto.setHeaderOperation(JSON.parseObject(ruleInfo.getHeaderOperation(), EnvoyRouteRuleHeaderOperationDto.class));
        }

        ruleDto.fromRouteMeta(ruleInfo);
        ruleDto.setDescription(ruleInfo.getDescription());
        return ruleDto;
    }

    @Override
    public List<Long> getRouteRuleIdListByNameFuzzy(String routeRuleName, long projectId) {
        List<Long> routeRuleIdList = routeRuleInfoDao.getRouteRuleIdListByNameFuzzy(routeRuleName, projectId);
        return CollectionUtils.isEmpty(routeRuleIdList) ? Lists.newArrayList() : routeRuleIdList;
    }

    @Override
    public List<RouteRuleInfo> getRouteRuleList(List<Long> routeRuleIdList) {
        if (CollectionUtils.isEmpty(routeRuleIdList)) {
            return Lists.newArrayList();
        }
        List<RouteRuleInfo> routeRuleInfoList = routeRuleInfoDao.getRouteRuleList(routeRuleIdList);
        return CollectionUtils.isEmpty(routeRuleIdList) ? Lists.newArrayList() : routeRuleInfoList;
    }
}
