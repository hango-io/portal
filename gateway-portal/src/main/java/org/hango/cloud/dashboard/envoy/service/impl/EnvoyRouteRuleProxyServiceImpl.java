package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.envoy.meta.GatewayInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceInfo;
import org.hango.cloud.dashboard.envoy.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.envoy.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.envoy.service.IGatewayInfoService;
import org.hango.cloud.dashboard.envoy.service.IServiceInfoService;
import org.hango.cloud.dashboard.envoy.util.Const;
import org.hango.cloud.dashboard.envoy.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.envoy.dao.IEnvoyRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.EnvoyDestinationInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyPluginBindingInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyPluginInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyRouteRuleInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyRouteRuleProxyService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyServiceProxyService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.VirtualClusterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ??????????????????Service????????????
 * <p>
 * 2019-09-18
 */
@Service
public class EnvoyRouteRuleProxyServiceImpl implements IEnvoyRouteRuleProxyService {

	private static final Logger logger = LoggerFactory.getLogger(EnvoyRouteRuleProxyServiceImpl.class);

	@Autowired
	private IEnvoyRouteRuleProxyDao routeRuleProxyDao;

	@Autowired
	private IGatewayInfoService gatewayInfoService;

	@Autowired
	private IEnvoyRouteRuleInfoService routeRuleInfoService;

	@Autowired
	private IEnvoyServiceProxyService serviceProxyService;

	@Autowired
	private IServiceInfoService serviceInfoService;

	@Autowired
	private IEnvoyPluginInfoService envoyPluginInfoService;

	@Autowired
	private IEnvoyGatewayService envoyGatewayService;

	@Autowired
	private IGetFromApiPlaneService getFromApiPlaneService;

	@Override
	public ErrorCode checkPublishParam(EnvoyRouteRuleProxyDto routeRuleProxyDto) {
		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyDto.getRouteRuleId());
		if (null == routeRuleInfo) {
			logger.info("???????????????????????????????????????????????????! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
			return CommonErrorCode.NoSuchRouteRule;
		}

		if (routeRuleProxyDto.getDestinationServices() == null
		    || routeRuleProxyDto.getDestinationServices().size() == 0) {
			logger.info("????????????????????????????????????????????????! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
			return CommonErrorCode.InvalidDestinationService;
		}

		routeRuleProxyDto.setServiceId(routeRuleInfo.getServiceId());
		//???????????????????????????
		if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getGwIds())) {
			List<ErrorCode> errorCodes = routeRuleProxyDto.getGwIds().stream().map(
				item -> checkPublishRouteRuleAndGw(item, routeRuleProxyDto)).collect(Collectors.toList());
			List<ErrorCode> errorParams = errorCodes.stream().filter(item -> !item.equals(CommonErrorCode.Success))
			                                        .collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(errorParams)) {
				return errorParams.get(0);
			}
		} else {
			ErrorCode errorCode = checkPublishRouteRuleAndGw(routeRuleProxyDto.getGwId(), routeRuleProxyDto);
			if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
				return errorCode;
			}
		}
		return CommonErrorCode.Success;
	}

	@Override
	public ErrorCode checkUpdateParam(EnvoyRouteRuleProxyDto routeRuleProxyDto) {
		GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyDto.getGwId());
		if (null == gatewayInfo) {
			logger.info("?????????????????????????????????????????????! gwId:{}", routeRuleProxyDto.getGwId());
			return CommonErrorCode.NoSuchGateway;
		}

		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyDto.getRouteRuleId());
		if (null == routeRuleInfo) {
			logger.info("????????????????????????????????????????????????! routeRuleId:{}", routeRuleProxyDto.getRouteRuleId());
			return CommonErrorCode.NoSuchRouteRule;
		}

		List<EnvoyDestinationDto> destinationServices = routeRuleProxyDto.getDestinationServices();
		if (CollectionUtils.isEmpty(destinationServices)) {
			logger.info("?????????????????????????????????????????????!");
			return CommonErrorCode.MissingParameter("ProxyServices");
		}
		for (EnvoyDestinationDto destinationService : destinationServices) {
			//?????????????????????????????????id?????????????????????????????????????????????
			if (routeRuleInfo.getServiceId() != destinationService.getServiceId()) {
				logger.info("?????????????????????????????????id?????????");
				return CommonErrorCode.NoSuchService;
			}
		}

		EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(
			routeRuleProxyDto.getGwId(), routeRuleInfo.getProjectId());
		if (null == virtualHostInfo) {
			logger.info("?????????????????????????????????virtual host??????????????????????????? gwId:{}, projectId:{}", routeRuleProxyDto.getGwId(),
			            routeRuleInfo.getProjectId());
			return CommonErrorCode.ProjectNotAssociatedGateway;
		}
		return CommonErrorCode.Success;
	}

	@Override
	public long publishRouteRule(EnvoyRouteRuleProxyInfo routeRuleProxyInfo, List<String> pluginConfigurations,
	                             boolean updateHosts) {
		EnvoyRouteRuleInfo routeRuleInfoDb = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyInfo.getRouteRuleId());
		if (null == routeRuleInfoDb) {
			logger.error("????????????????????????????????????????????????????????????????????????routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
			return Const.ERROR_RESULT;
		}
		routeRuleProxyInfo.setProjectId(routeRuleInfoDb.getProjectId());
		if (updateHosts) {
			EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(
				routeRuleProxyInfo.getGwId(), routeRuleInfoDb.getProjectId());
			if (null == virtualHostInfo) {
				logger.info("???????????????virtualHostInfo?????????????????????");
				return Const.ERROR_RESULT;
			}
			routeRuleProxyInfo.setHosts(virtualHostInfo.getHosts());
		}
		//???????????????enable?????????????????????????????????????????????api-plane???????????????????????????????????????
		if (Const.ROUTE_RULE_ENABLE_STATE.equals(routeRuleProxyInfo.getEnableState())) {
			if (!getFromApiPlaneService.publishRouteRuleByApiPlane(routeRuleProxyInfo, pluginConfigurations)) {
				return Const.ERROR_RESULT;
			}
		}

		routeRuleProxyInfo.setUpdateTime(System.currentTimeMillis());
		// ??????routeRuleProxyInfo???id??????0??????????????????????????????????????????????????????????????????????????????routeRuleProxyInfo?????????routeRuleProxyInfo
		if (routeRuleProxyInfo.getId() > 0) {
			routeRuleProxyDao.update(routeRuleProxyInfo);
			return routeRuleProxyInfo.getId();
		}

		//???????????????/??????????????????????????????
		routeRuleProxyInfo.setCreateTime(System.currentTimeMillis());
		long publishRouteRuleId = addRouteRuleProxy(routeRuleProxyInfo);
		//??????route rule???????????????????????????????????????
		if (publishRouteRuleId > NumberUtils.INTEGER_ZERO) {
			//???????????????????????????????????????????????????
			if (routeRuleInfoDb != null && routeRuleInfoDb.getPublishStatus() == NumberUtils.INTEGER_ZERO) {
				routeRuleInfoDb.setPublishStatus(NumberUtils.INTEGER_ONE);
				routeRuleInfoService.updateRouteRule(routeRuleInfoDb);
			}
		}
		return publishRouteRuleId;
	}

	@Override
	public List<String> publishRouteRuleBatch(List<Long> gwIds, EnvoyRouteRuleProxyDto routeRuleProxyDto) {
		if (CollectionUtils.isEmpty(gwIds)) {
			return new ArrayList<>();
		}
		return gwIds.stream().filter(item -> {
			routeRuleProxyDto.setGwId(item);
			return Const.ERROR_RESULT == publishRouteRule(toMeta(routeRuleProxyDto), new ArrayList<>(), true);
		}).map(item -> gatewayInfoService.get(item).getGwName()).collect(Collectors.toList());
	}

	@Override
	public long addRouteRuleProxy(EnvoyRouteRuleProxyInfo routeRuleProxyInfo) {
		return routeRuleProxyDao.add(routeRuleProxyInfo);
	}

	@Override
	public List<EnvoyRouteRuleProxyInfo> getRouteRuleProxyList(long gwId, long serviceId, String sortKey,
	                                                           String sortValue, long offset, long limit) {
		return routeRuleProxyDao.getRouteRuleProxyList(gwId, serviceId, ProjectTraceHolder.getProId(), sortKey,
		                                               sortValue, offset, limit);
	}

	@Override
	public List<EnvoyRouteRuleProxyInfo> getRouteRuleProxyList(long serviceId) {
		return routeRuleProxyDao.getRouteRuleProxyList(serviceId);
	}

	@Override
	public long getRouteRuleProxyCountByService(long gwId, long serviceId) {
		return routeRuleProxyDao.getRouteRuleProxyCount(gwId, serviceId, ProjectTraceHolder.getProId());
	}

	@Override
	public long getRouteRuleProxyCount(long gwId, long routeRuleId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		if (0 != gwId) {
			params.put("gwId", gwId);
		}
		if (0 != routeRuleId) {
			params.put("routeRuleId", routeRuleId);
		}
		params.put("projectId", ProjectTraceHolder.getProId());
		return routeRuleProxyDao.getCountByFields(params);
	}

	@Override
	public EnvoyRouteRuleProxyInfo getRouteRuleProxy(long gwId, long routeRuleId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("gwId", gwId);
		params.put("routeRuleId", routeRuleId);
		List<EnvoyRouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
		return CollectionUtils.isEmpty(routeRuleProxyInfos) ? null : routeRuleProxyInfos.get(0);
	}

	@Override
	public boolean deleteRouteRuleProxy(long gwId, long routeRuleId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("gwId", gwId);
		params.put("routeRuleId", routeRuleId);
		List<EnvoyRouteRuleProxyInfo> envoyRouteRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
		//????????????
		if (CollectionUtils.isEmpty(envoyRouteRuleProxyInfos)) {
			return true;
		}
		EnvoyRouteRuleProxyInfo envoyRouteRuleProxyInfo = envoyRouteRuleProxyInfos.get(0);
		boolean deleteSuccess = getFromApiPlaneService.deleteRouteRuleByApiPlane(envoyRouteRuleProxyInfo);
		if (!deleteSuccess) {
			return false;
		}
		//?????????????????????????????????
		envoyPluginInfoService.deletePluginList(gwId, String.valueOf(routeRuleId),
		                                        EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);

		//????????????????????????
		routeRuleProxyDao.delete(envoyRouteRuleProxyInfo);
		//??????????????????????????????
		updateRouteRulePublishStatus(routeRuleId);
		return true;
		//        List<Long> desServiceIdList = envoyRouteRuleProxyInfo.getDestinationServiceList().stream().map
		//        (EnvoyDestinationInfo::getServiceId).collect(Collectors.toList());
		//        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
		//        //??????????????????????????????????????????????????????
		//        if (CollectionUtils.isEmpty(serviceIds) || (desServiceIdList.containsAll(serviceIds) && serviceIds
		//        .containsAll(desServiceIdList))){
		//            routeRuleProxyDao.delete(envoyRouteRuleProxyInfo);
		//            updateRouteRulePublishStatus(routeRuleId);
		//            return true;
		//        }
		//        //???????????????API-plane?????????????????????????????????
		//        desServiceIdList.removeAll(serviceIds);
		//        List<EnvoyDestinationInfo> envoyDestinationInfos = generateDestinationWithWeight(desServiceIdList);
		//        envoyRouteRuleProxyInfo.setDestinationServiceList(envoyDestinationInfos);
		//        envoyRouteRuleProxyInfo.setDestinationServices(JSON.toJSONString(envoyDestinationInfos));
		//        envoyRouteRuleProxyInfo.setUpdateTime(System.currentTimeMillis());
		//        envoyRouteRuleProxyInfo.setServiceId(Lists.transform(desServiceIdList, Functions.toStringFunction())
		//                .stream().collect(Collectors.joining(",")));
		//        if (!publishRouteRuleByApiPlane(envoyRouteRuleProxyInfo)) {
		//            logger.info("???????????????????????????????????????????????????service??????????????????api-plane???????????????");
		//            return false;
		//        }
		//        routeRuleProxyDao.update(envoyRouteRuleProxyInfo);
		//        return true;
	}

	@Override
	public ErrorCode checkDeleteRouteRuleProxy(long gwId, long routeRuleId, List<Long> serviceIds) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("gwId", gwId);
		params.put("routeRuleId", routeRuleId);
		List<EnvoyRouteRuleProxyInfo> envoyRouteRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
		if (CollectionUtils.isEmpty(envoyRouteRuleProxyInfos)) {
			logger.info("??????????????????????????????????????????");
			return CommonErrorCode.RouteRuleNotPublished;
		}
		if (CollectionUtils.isEmpty(serviceIds)) {
			return CommonErrorCode.Success;
		}
		EnvoyRouteRuleProxyInfo envoyRouteRuleProxyInfo = envoyRouteRuleProxyInfos.get(0);
		List<EnvoyDestinationInfo> destinationServiceList = envoyRouteRuleProxyInfo.getDestinationServiceList();
		List<Long> desServiceIdList = destinationServiceList.stream().map(EnvoyDestinationInfo::getServiceId).collect(
			Collectors.toList());
		for (Long serviceId : serviceIds) {
			if (!desServiceIdList.contains(serviceId)) {
				return CommonErrorCode.RouteRuleServiceNotMatch;
			}
		}
		return CommonErrorCode.Success;
	}

	@Override
	public ErrorCode checkUpdateEnableState(long gwId, long routeRuleId, String enableState) {
		EnvoyRouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
		if (routeRuleProxyInDb == null) {
			logger.info("????????????????????????????????????????????????????????????");
			return CommonErrorCode.RouteRuleNotPublished;
		}
		if (!Const.ROUTE_RULE_ENABLE_STATE.equals(enableState) && !Const.ROUTE_RULE_DISABLE_STATE.equals(enableState)) {
			logger.info("???????????????????????????????????????????????????????????????enable???disable");
			return CommonErrorCode.InvalidParameter(enableState, "EnableState");
		}
		return CommonErrorCode.Success;
	}

	@Override
	public long updateEnableState(long gwId, long routeRuleId, String enableState) {
		EnvoyRouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
		if (routeRuleProxyInDb == null) {
			logger.error("????????????????????????????????????,routeRuleId:{}", routeRuleId);
			return Const.ERROR_RESULT;
		}
		//?????????????????????enable
		if (Const.ROUTE_RULE_ENABLE_STATE.equals(enableState)) {
			List<EnvoyPluginBindingInfo> alreadyBindingPlugins =
				envoyPluginInfoService.getEnablePluginBindingList(gwId,
			                                                                                                       String
				                                                                                                       .valueOf(
					                                                                                                       routeRuleId),
			                                                                                                       EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
			List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(
				EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
			if (!getFromApiPlaneService.publishRouteRuleByApiPlane(routeRuleProxyInDb, newPluginConfigurations)) {
				return Const.ERROR_RESULT;
			}
		}
		//?????????????????????disable
		if (Const.ROUTE_RULE_DISABLE_STATE.equals(enableState)) {
			if (!getFromApiPlaneService.deleteRouteRuleByApiPlane(routeRuleProxyInDb)) {
				return Const.ERROR_RESULT;
			}
		}
		//??????????????????????????????
		if (!enableState.equals(routeRuleProxyInDb.getEnableState())) {
			routeRuleProxyInDb.setEnableState(enableState);
			routeRuleProxyInDb.setUpdateTime(System.currentTimeMillis());
			return routeRuleProxyDao.update(routeRuleProxyInDb);
		}
		return NumberUtils.INTEGER_ZERO;
	}

	@Override
	public long updateEnvoyRouteRuleProxy(EnvoyRouteRuleProxyInfo proxyInfo) {
		long gwId = proxyInfo.getGwId();
		long routeRuleId = proxyInfo.getRouteRuleId();
		EnvoyRouteRuleProxyInfo routeRuleProxyInDb = getRouteRuleProxy(gwId, routeRuleId);
		if (routeRuleProxyInDb == null) {
			logger.error("????????????????????????????????????,routeRuleId:{}", routeRuleId);
			return Const.ERROR_RESULT;
		}
		EnvoyVirtualHostInfo virtualHostInfo =
			envoyGatewayService.getVirtualHostByGwIdAndProjectId(proxyInfo.getGwId(),
		                                                                                            routeRuleProxyInDb
			                                                                                            .getProjectId());
		if (null == virtualHostInfo) {
			return Const.ERROR_RESULT;
		}
		proxyInfo.setHosts(virtualHostInfo.getHosts());
		proxyInfo.setId(routeRuleProxyInDb.getId());
		//???????????????enable
		if (Const.ROUTE_RULE_ENABLE_STATE.equals(proxyInfo.getEnableState())) {
			List<EnvoyPluginBindingInfo> alreadyBindingPlugins =
				envoyPluginInfoService.getEnablePluginBindingList(gwId,
			                                                                                                       String
				                                                                                                       .valueOf(
					                                                                                                       routeRuleId),
			                                                                                                       EnvoyPluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE);
			List<String> newPluginConfigurations = alreadyBindingPlugins.stream().map(
				EnvoyPluginBindingInfo::getPluginConfiguration).collect(Collectors.toList());
			if (!getFromApiPlaneService.publishRouteRuleByApiPlane(proxyInfo, newPluginConfigurations)) {
				return Const.ERROR_RESULT;
			}
		}
		//?????????????????????disable
		if (Const.ROUTE_RULE_DISABLE_STATE.equals(proxyInfo.getEnableState())) {
			//???API-plane???????????????vs
			if (!getFromApiPlaneService.deleteRouteRuleByApiPlane(routeRuleProxyInDb)) {
				return Const.ERROR_RESULT;
			}
		}
		proxyInfo.setUpdateTime(System.currentTimeMillis());
		routeRuleProxyDao.update(proxyInfo);
		return proxyInfo.getId();
	}

	@Override
	public EnvoyRouteRuleProxyInfo getRouteRuleProxy(long id) {
		return routeRuleProxyDao.get(id);
	}

	@Override
	public List<EnvoyRouteRuleProxyInfo> getRouteRuleProxyByRouteRuleId(long routeRuleId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("routeRuleId", routeRuleId);
		return routeRuleProxyDao.getRecordsByField(params);
	}

	@Override
	public EnvoyRouteRuleProxyDto fromMeta(EnvoyRouteRuleProxyInfo proxyInfo) {
		EnvoyRouteRuleProxyDto proxyDto = new EnvoyRouteRuleProxyDto();
		proxyDto.setCreateTime(proxyInfo.getCreateTime());
		proxyDto.setUpdateTime(proxyInfo.getUpdateTime());
		proxyDto.setGwId(proxyInfo.getGwId());
		proxyDto.setId(proxyInfo.getId());
		proxyDto.setRouteRuleId(proxyInfo.getRouteRuleId());
		proxyDto.setPriority(proxyInfo.getPriority());
		proxyDto.setEnableState(proxyInfo.getEnableState());
		GatewayInfo gatewayInfo = gatewayInfoService.get(proxyInfo.getGwId());
		if (gatewayInfo != null) {
			proxyDto.setGwName(gatewayInfo.getGwName());
			proxyDto.setGwAddr(gatewayInfo.getGwAddr());
		}
		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(proxyInfo.getRouteRuleId());
		if (routeRuleInfo == null) {
			logger.info("??????????????????????????????????????????");
			return null;
		}
		ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(proxyInfo.getServiceId());
		EnvoyServiceProxyInfo proxyByServiceIdAndGwId = serviceProxyService.getServiceProxyByServiceIdAndGwId(
			proxyInfo.getGwId(), proxyInfo.getServiceId());
		proxyDto.setRouteRuleName(routeRuleInfo.getRouteRuleName());
		proxyDto.setServiceId(routeRuleInfo.getServiceId());
		proxyDto.setServiceName(serviceInfoDb.getDisplayName());
		proxyDto.setRouteRuleSource(routeRuleInfo.getRouteRuleSource());
		proxyDto.setHeaderOperation(
			JSON.parseObject(routeRuleInfo.getHeaderOperation(), EnvoyRouteRuleHeaderOperationDto.class));
		List<EnvoyDestinationDto> envoyDestinationDtos = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(proxyInfo.getDestinationServiceList())) {
			//???????????????for????????????????????????????????????
			for (EnvoyDestinationInfo info : proxyInfo.getDestinationServiceList()) {
				EnvoyDestinationDto destinationDto = info.fromMeta();
				destinationDto.setApplicationName(proxyByServiceIdAndGwId.getBackendService());
				envoyDestinationDtos.add(destinationDto);
			}
			proxyDto.setDestinationServices(envoyDestinationDtos);
		}
		proxyDto.setServiceType(serviceInfoDb.getServiceType());
		proxyDto.setHosts(JSON.parseArray(proxyInfo.getHosts(), String.class));

		//??????match??????
		proxyDto.fromRouteMeta(proxyInfo);

		proxyDto.setTimeout(proxyInfo.getTimeout());
		proxyDto.setHttpRetryDto(proxyInfo.getHttpRetryDto());
		//??????????????????
		if (StringUtils.isNotEmpty(proxyInfo.getVirtualCluster())) {
			proxyDto.setVirtualClusterDto(JSON.parseObject(proxyInfo.getVirtualCluster(), VirtualClusterDto.class));
		}
		return proxyDto;
	}

	@Override
	public EnvoyRouteRuleProxyInfo toMeta(EnvoyRouteRuleProxyDto proxyDto) {
		EnvoyRouteRuleProxyInfo routeRuleProxyInfo = new EnvoyRouteRuleProxyInfo();
		routeRuleProxyInfo.setRouteRuleId(proxyDto.getRouteRuleId());
		routeRuleProxyInfo.setGwId(proxyDto.getGwId());

		EnvoyRouteRuleInfo routeRuleInDb = routeRuleInfoService.getRouteRuleInfoById(proxyDto.getRouteRuleId());
		//????????????id?????????????????????id??????
		routeRuleProxyInfo.setServiceId(routeRuleInDb.getServiceId());

		//?????????????????????routeProxy
		EnvoyServiceProxyInfo proxyByServiceIdAndGwId = serviceProxyService.getServiceProxyByServiceIdAndGwId(
			proxyDto.getGwId(), routeRuleInDb.getServiceId());
		List<EnvoyDestinationInfo> destinationInfos = proxyDto.getDestinationServices().stream().map(
			EnvoyDestinationDto::toMeta).collect(Collectors.toList());
		if (Const.STATIC_PUBLISH_TYPE.equals(proxyByServiceIdAndGwId.getPublishType())) {
			//??????????????????????????????80
			destinationInfos.stream().forEach(envoyDestinationInfo -> {
				envoyDestinationInfo.setPort(80);
			});
		}

		routeRuleProxyInfo.setDestinationServiceList(destinationInfos);
		routeRuleProxyInfo.setDestinationServices(JSON.toJSONString(routeRuleProxyInfo.getDestinationServiceList()));

		routeRuleProxyInfo.setEnableState(proxyDto.getEnableState());
		//????????????
		routeRuleProxyInfo.setTimeout(proxyDto.getTimeout());
		//????????????
		if (proxyDto.getHttpRetryDto() != null) {
			routeRuleProxyInfo.setHttpRetryDto(proxyDto.getHttpRetryDto());
			routeRuleProxyInfo.setHttpRetry(JSON.toJSONString(proxyDto.getHttpRetryDto()));
		}

		if (proxyDto.getVirtualClusterDto() != null && StringUtils.isEmpty(
			proxyDto.getVirtualClusterDto().getVirtualClusterName())) {
			//??????VirtualCluster
			proxyDto.getVirtualClusterDto().setVirtualClusterName("vc-" + routeRuleInDb.getId());
			routeRuleProxyInfo.setVirtualCluster(JSON.toJSONString(proxyDto.getVirtualClusterDto()));
		}
		//??????uriMatch
		if (proxyDto.getUriMatchDto() == null) {
			proxyDto.fromRouteMeta(routeRuleInDb);
		}
		//??????routeMeta
		proxyDto.toRouteMeta(routeRuleProxyInfo);
		return routeRuleProxyInfo;
	}

	@Override
	public List<EnvoyRouteRuleProxyInfo> getRouteRuleProxyListByServiceId(long gwId, long serviceId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("GwId", gwId);
		params.put("ServiceId", serviceId);
		return routeRuleProxyDao.getRecordsByField(params);
	}

	@Override
	public List<EnvoyRouteRuleProxyInfo> getRouteRuleProxyListByGwId(long gwId) {
		Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("GwId", gwId);
		return routeRuleProxyDao.getRecordsByField(params);
	}

	/**
	 * ???????????????????????????????????????
	 */
	public ErrorCode checkSubsetWhenPublishRouteRuleAndGw(EnvoyServiceProxyInfo serviceProxyInfo,
	                                                      EnvoyRouteRuleProxyDto routeRuleProxyDto) {
		if (routeRuleProxyDto.getDestinationServices() == null
		    || routeRuleProxyDto.getDestinationServices().size() == 0) {
			return CommonErrorCode.Success;
		}
		List<String> subsetNameList = new ArrayList<>();
		if (StringUtils.isNotBlank(serviceProxyInfo.getSubsets())) {
			List<EnvoySubsetDto> envoySubsetDtoObjectList = JSON.parseArray(serviceProxyInfo.getSubsets(),
			                                                                EnvoySubsetDto.class);
			envoySubsetDtoObjectList.stream().forEach(envoySubsetDto -> {
				String subsetName = envoySubsetDto.getName();
				subsetNameList.add(subsetName);
			});
		}

		for (EnvoyDestinationDto envoyDestinationDto : routeRuleProxyDto.getDestinationServices()) {
			if (StringUtils.isNotBlank(envoyDestinationDto.getSubsetName()) && !subsetNameList.contains(
				envoyDestinationDto.getSubsetName())) {
				return CommonErrorCode.InvalidSubsetName;
			}
		}
		return CommonErrorCode.Success;
	}

	private ErrorCode checkPublishRouteRuleAndGw(long gwId, EnvoyRouteRuleProxyDto routeRuleProxyDto) {
		GatewayInfo gatewayInDb = gatewayInfoService.get(gwId);
		if (gatewayInDb == null) {
			logger.info("?????????????????????????????????????????????id:{}", gwId);
			return CommonErrorCode.NoSuchGateway;
		}
		long routeRuleProxyCount = getRouteRuleProxyCount(gwId, routeRuleProxyDto.getRouteRuleId());
		if (routeRuleProxyCount > 0) {
			logger.info("????????????????????????????????????????????????????????????");
			return CommonErrorCode.RouteRuleAlreadyPublished(gatewayInDb.getGwName());
		}

		EnvoyServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId,
		                                                                                               routeRuleProxyDto
			                                                                                               .getServiceId());
		if (null == serviceProxyInfo) {
			logger.info("????????????????????????????????????????????????serviceId:{}, gwId:{}", routeRuleProxyDto.getServiceId(), gwId);
			return CommonErrorCode.ServiceNotPublished;
		}

		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyDto.getRouteRuleId());
		EnvoyVirtualHostInfo virtualHostInfo = envoyGatewayService.getVirtualHostByGwIdAndProjectId(gwId, routeRuleInfo
			                                                                                                  .getProjectId());
		if (null == virtualHostInfo) {
			logger.info("???????????????????????????????????????????????????????????????????????????, gwId:{}, projectId:{}", gwId, routeRuleInfo.getProjectId());
			return CommonErrorCode.ProjectNotAssociatedGateway;
		}

		//??????????????????????????????
		if (Const.DYNAMIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
			List<EnvoyDestinationDto> destinationServices = routeRuleProxyDto.getDestinationServices();
			if (CollectionUtils.isEmpty(destinationServices)) {
				logger.info("????????????????????????????????????????????????!");
				return CommonErrorCode.MissingParameter("ProxyServices");
			}

			int totalweight = 0;
			for (EnvoyDestinationDto destinationService : destinationServices) {
				//?????????????????????????????????id?????????????????????????????????????????????
				if (routeRuleProxyDto.getServiceId() != destinationService.getServiceId()) {
					logger.info("????????????????????????????????????id?????????");
					return CommonErrorCode.NoSuchService;
				}

				if (destinationService.getWeight() < 0 || destinationService.getWeight() > 100) {
					logger.info("????????????????????????????????????????????????: {}", destinationService.getWeight());
					return CommonErrorCode.InvalidParameter(String.valueOf(destinationService.getWeight()), "Weight");
				}
				totalweight += destinationService.getWeight();
			}

			if (totalweight != 100) {
				return CommonErrorCode.InvalidTotalWeight;
			}
		}

		//???????????????????????????????????????
		return checkSubsetWhenPublishRouteRuleAndGw(serviceProxyInfo, routeRuleProxyDto);
	}

	private ErrorCode checkOneProxyService(long gwId, EnvoyDestinationDto destinationService) {
		EnvoyServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId,
		                                                                                               destinationService
			                                                                                               .getServiceId());
		if (null == serviceProxyInfo) {
			logger.info("??????????????????????????????????????????????????????destinationServiceId:{}, gwId:{}", destinationService.getServiceId(), gwId);
			return CommonErrorCode.ServiceNotPublished;
		}
		return CommonErrorCode.Success;
	}

	private List<EnvoyDestinationInfo> generateDestinationWithWeight(List<Long> serviceIds) {
		if (CollectionUtils.isEmpty(serviceIds) || serviceIds.size() == 0) {
			return new ArrayList<>();
		}
		List<EnvoyDestinationInfo> envoyDestinationInfos = new ArrayList<>();
		int weight = 100 / serviceIds.size();
		for (long serviceId : serviceIds) {
			EnvoyDestinationInfo envoyDestinationInfo = new EnvoyDestinationInfo();
			envoyDestinationInfo.setServiceId(serviceId);
			envoyDestinationInfo.setWeight(weight);
			envoyDestinationInfos.add(envoyDestinationInfo);
		}
		//??????????????????????????????????????????????????????
		if ((weight * serviceIds.size()) != 100) {
			envoyDestinationInfos.remove(serviceIds.size() - 1);
			weight = 100 - (weight * (serviceIds.size() - 1));
			EnvoyDestinationInfo envoyDestinationInfo = new EnvoyDestinationInfo();
			envoyDestinationInfo.setServiceId(serviceIds.get(serviceIds.size() - 1));
			envoyDestinationInfo.setWeight(weight);
			envoyDestinationInfos.add(envoyDestinationInfo);
		}
		return envoyDestinationInfos;
	}

	private void updateRouteRulePublishStatus(long routeRuleId) {
		//??????????????????????????????????????????
		Map<String, Object> paramRouteRule = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		paramRouteRule.put("routeRuleId", routeRuleId);
		List<EnvoyRouteRuleProxyInfo> records = routeRuleProxyDao.getRecordsByField(paramRouteRule);
		if (!CollectionUtils.isEmpty(records)) {
			return;
		}
		EnvoyRouteRuleInfo routeRuleInfoDb = routeRuleInfoService.getRouteRuleInfoById(routeRuleId);
		if (routeRuleInfoDb != null) {
			routeRuleInfoDb.setPublishStatus(NumberUtils.INTEGER_ZERO);
			routeRuleInfoService.updateRouteRule(routeRuleInfoDb);
		}
	}

}
