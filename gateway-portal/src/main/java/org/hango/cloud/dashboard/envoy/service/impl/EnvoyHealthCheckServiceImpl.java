package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.common.collect.Lists;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.envoy.meta.GatewayInfo;
import org.hango.cloud.dashboard.envoy.meta.common.HttpClientResponse;
import org.hango.cloud.dashboard.envoy.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.envoy.meta.ServiceInfo;
import org.hango.cloud.dashboard.envoy.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.envoy.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.service.IGatewayInfoService;
import org.hango.cloud.dashboard.envoy.service.IServiceInfoService;
import org.hango.cloud.dashboard.envoy.util.Const;
import org.hango.cloud.dashboard.envoy.dao.EnvoyHealthCheckRuleDao;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyServiceProxyService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceInstanceDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ????????????Service
 *
 * @date 2019/11/19 ??????3:30.
 */
@Service
public class EnvoyHealthCheckServiceImpl implements IEnvoyHealthCheckService {

	private static final Logger logger = LoggerFactory.getLogger(EnvoyHealthCheckServiceImpl.class);

	private final String HEALTHY = "HEALTHY";

	private final String UNHEALTHY = "UNHEALTHY";

	@Autowired
	private EnvoyHealthCheckRuleDao envoyHealthCheckRuleDao;

	@Autowired
	private IGatewayInfoService gatewayInfoService;

	@Autowired
	private IServiceInfoService serviceInfoService;

	@Autowired
	private IEnvoyServiceProxyService envoyServiceProxyService;

	@Autowired
	private IGetFromApiPlaneService getFromApiPlaneService;

	@Override
	public ErrorCode updateHealthCheckRuleParam(EnvoyHealthCheckRuleInfo newEnvoyHealthCheckRuleInfo) {
		long serviceId = newEnvoyHealthCheckRuleInfo.getServiceId();
		long gwId = newEnvoyHealthCheckRuleInfo.getGwId();
		ErrorCode errorCode = CommonErrorCode.Success;

		Map<String, Object> params = new HashMap<>(2);
		params.put("serviceId", serviceId);
		params.put("gwId", gwId);
		List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList =
			envoyHealthCheckRuleDao.getRecordsByField(params);
		if (envoyHealthCheckRuleInfoList.size() == 0) {
			//????????????????????????????????????????????????
			newEnvoyHealthCheckRuleInfo.setCreateTime(System.currentTimeMillis());
			newEnvoyHealthCheckRuleInfo.setUpdateTime(System.currentTimeMillis());

			//??????APIPlane????????????
			if (!getFromApiPlaneService.publishServiceByApiPlane(null, newEnvoyHealthCheckRuleInfo)) {
				logger.warn("??????APIPlane??????????????????????????????");
				errorCode = CommonErrorCode.InternalServerError;
				return errorCode;
			}
			//?????????????????????G-Portal???
			envoyHealthCheckRuleDao.add(newEnvoyHealthCheckRuleInfo);
		} else {
			//??????????????????????????????????????????????????????????????????
			EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfoBefore = envoyHealthCheckRuleInfoList.get(0);
			envoyHealthCheckRuleInfoBefore.setUpdateTime(System.currentTimeMillis());
			envoyHealthCheckRuleInfoBefore.setActiveSwitch(newEnvoyHealthCheckRuleInfo.getActiveSwitch());
			envoyHealthCheckRuleInfoBefore.setPassiveSwitch(newEnvoyHealthCheckRuleInfo.getPassiveSwitch());

			//?????????????????????????????????????????????????????????????????????????????????????????????????????????
			if (newEnvoyHealthCheckRuleInfo.getActiveSwitch() == 1) {
				envoyHealthCheckRuleInfoBefore.setPath(newEnvoyHealthCheckRuleInfo.getPath());
				envoyHealthCheckRuleInfoBefore.setTimeout(newEnvoyHealthCheckRuleInfo.getTimeout());
				envoyHealthCheckRuleInfoBefore.setExpectedStatuses(newEnvoyHealthCheckRuleInfo.getExpectedStatuses());
				envoyHealthCheckRuleInfoBefore.setHealthyInterval(newEnvoyHealthCheckRuleInfo.getHealthyInterval());
				envoyHealthCheckRuleInfoBefore.setHealthyThreshold(newEnvoyHealthCheckRuleInfo.getHealthyThreshold());
				envoyHealthCheckRuleInfoBefore.setUnhealthyInterval(newEnvoyHealthCheckRuleInfo.getUnhealthyInterval());
				envoyHealthCheckRuleInfoBefore.setUnhealthyThreshold(
					newEnvoyHealthCheckRuleInfo.getUnhealthyThreshold());
			}

			if (newEnvoyHealthCheckRuleInfo.getPassiveSwitch() == 1) {
				envoyHealthCheckRuleInfoBefore.setConsecutiveErrors(newEnvoyHealthCheckRuleInfo.getConsecutiveErrors());
				envoyHealthCheckRuleInfoBefore.setBaseEjectionTime(newEnvoyHealthCheckRuleInfo.getBaseEjectionTime());
				envoyHealthCheckRuleInfoBefore.setMaxEjectionPercent(
					newEnvoyHealthCheckRuleInfo.getMaxEjectionPercent());
				envoyHealthCheckRuleInfoBefore.setMinHealthPercent(newEnvoyHealthCheckRuleInfo.getMinHealthPercent());
			}

			//??????APIPlane????????????
			if (!getFromApiPlaneService.publishServiceByApiPlane(null, envoyHealthCheckRuleInfoBefore)) {
				logger.warn("??????APIPlane??????????????????????????????");
				errorCode = CommonErrorCode.InternalServerError;
				return errorCode;
			}
			//??????
			envoyHealthCheckRuleDao.update(envoyHealthCheckRuleInfoBefore);
		}
		return CommonErrorCode.Success;
	}

	@Override
	public void shutdownHealthCheck(long serviceId, long gwId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("serviceId", serviceId);
		params.put("gwId", gwId);
		List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList =
			envoyHealthCheckRuleDao.getRecordsByField(params);
		if (envoyHealthCheckRuleInfoList.size() != 0) {
			EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfoBefore = envoyHealthCheckRuleInfoList.get(0);
			envoyHealthCheckRuleInfoBefore.setActiveSwitch(0);
			envoyHealthCheckRuleInfoBefore.setPassiveSwitch(0);
			//??????
			envoyHealthCheckRuleDao.update(envoyHealthCheckRuleInfoBefore);
		}
	}

	@Override
	public void deleteHealthCheckRule(long serviceId) {
		envoyHealthCheckRuleDao.deleteByServiceId(serviceId);
	}

	@Override
	public EnvoyHealthCheckRuleDto getHealthCheckRule(long serviceId, long gwId) {
		Map<String, Object> params = new HashMap<>(2);
		params.put("serviceId", serviceId);
		params.put("gwId", gwId);
		List<EnvoyHealthCheckRuleInfo> envoyHealthCheckRuleInfoList =
			envoyHealthCheckRuleDao.getRecordsByField(params);
		if (envoyHealthCheckRuleInfoList.size() != 0) {
			return EnvoyHealthCheckRuleDto.metaToDto(envoyHealthCheckRuleInfoList.get(0));
		}
		return null;
	}

	/**
	 * ??????????????????????????????
	 */
	@Override
	public EnvoyPassiveHealthCheckRuleDto getPassiveHealthCheckRule(long serviceId, long gwId) {
		EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceId, gwId);
		if (envoyHealthCheckRuleDto == null || envoyHealthCheckRuleDto.getPassiveSwitch() == 0) {
			return null;
		}
		return new EnvoyPassiveHealthCheckRuleDto(envoyHealthCheckRuleDto);
	}

	/**
	 * ??????????????????????????????
	 */
	@Override
	public EnvoyActiveHealthCheckRuleDto getActiveHealthCheckRule(long serviceId, long gwId) {
		EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceId, gwId);
		if (envoyHealthCheckRuleDto == null || envoyHealthCheckRuleDto.getActiveSwitch() == 0) {
			return null;
		}
		return new EnvoyActiveHealthCheckRuleDto(envoyHealthCheckRuleDto);
	}

	@Override
	public List<EnvoyServiceInstanceDto> getServiceInstanceList(ServiceInfo serviceInfo, GatewayInfo gatewayInfo) {
		long serviceId = serviceInfo.getId();
		long gwId = gatewayInfo.getId();

		EnvoyServiceProxyInfo envoyServiceProxyInfo = envoyServiceProxyService.getServiceProxyByServiceIdAndGwId(gwId,
		                                                                                                         serviceId);
		if (envoyServiceProxyInfo == null) {
			logger.info("???????????????????????????????????????");
			return Collections.emptyList();
		}

		//??????APIPlane???????????????????????????????????????
		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "GetServiceHealthList");
		params.put("Version", "2019-07-25");
		params.put("Code", envoyServiceProxyInfo.getCode());
		//??????????????????????????????subset
		params.put("Gateway", gatewayInfo.getGwClusterName());
		params.put("Subsets", envoyServiceProxyService.getSubsetsName(envoyServiceProxyInfo).stream()
		                                              .collect(Collectors.joining(",")));
		RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(envoyServiceProxyInfo.getRegistryCenterType());
		String backendService = envoyServiceProxyInfo.getBackendService();
		if (Const.DYNAMIC_PUBLISH_TYPE.equals(envoyServiceProxyInfo.getPublishType())) {
			params.put("Host", backendService);
		}
		HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal",
		                                                             params, null, null, HttpMethod.GET.name());
		if (response == null) {
			logger.info("??????api-plane??????????????????????????????????????????Response?????????serviceId:{}???gwId:{}", serviceId, gwId);
			return Collections.emptyList();
		}
		if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
			logger.info("??????api-plane???????????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
			            response.getStatusCode(), response.getResponseBody());
			return Collections.emptyList();
		}

		List<EnvoyServiceInstanceDto> envoyServiceInstanceDtoList = new ArrayList<>();

		try {
			EnvoyServiceInstanceListObject envoyServiceInstanceListObject = JSONObject.parseObject(
				response.getResponseBody(), EnvoyServiceInstanceListObject.class);
			for (InnerEnvoyServiceInstance innerEnvoyServiceInstance : envoyServiceInstanceListObject
				                                                           .getServiceInstanceList()) {
				//??????????????????1
				for (EnvoyEndPoint envoyEndPoint : innerEnvoyServiceInstance.getEnvoyEndPointList()) {
					EnvoyServiceInstanceDto envoyServiceInstanceDto = new EnvoyServiceInstanceDto();
					envoyServiceInstanceDto.setInstanceAddr(envoyEndPoint.getAddress());
					envoyServiceInstanceDto.setStatus(HEALTHY.equalsIgnoreCase(envoyEndPoint.getStatus()) ? 1 : 0);
					envoyServiceInstanceDtoList.add(envoyServiceInstanceDto);
				}
			}
		} catch (Exception e) {
			logger.info("??????api-plane??????????????????????????????????????????body?????? ", e);
			return Collections.emptyList();
		}
		return envoyServiceInstanceDtoList;
	}

	@Override
	public Integer getServiceHealthyStatus(ServiceInfo serviceInfo, GatewayInfo gatewayInfo) {
		long serviceId = serviceInfo.getId();
		long gwId = gatewayInfo.getId();

		//???????????????????????????????????????????????????
		EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = getHealthCheckRule(serviceInfo.getId(), gatewayInfo.getId());
		boolean healthCheckSwitch = envoyHealthCheckRuleDto == null || (envoyHealthCheckRuleDto.getActiveSwitch() == 0
		                                                                && envoyHealthCheckRuleDto.getPassiveSwitch()
		                                                                   == 0);
		if (healthCheckSwitch) {
			logger.info("???????????????????????????????????????serviceId:{}, gwId:{}", serviceId, gwId);
			return 1;
		}

		//??????APIPlane???????????????????????????????????????
		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "GetServiceHealthList");
		params.put("Version", "2019-07-25");
		EnvoyServiceProxyInfo envoyServiceProxyInfo = envoyServiceProxyService.getServiceProxyByServiceIdAndGwId(gwId,
		                                                                                                         serviceId);
		if (envoyServiceProxyInfo == null) {
			logger.info("???????????????????????????????????????, serviceId:{}, gwId:{}", serviceId, gwId);
			return 1;
		}
		//??????????????????????????????subset
		params.put("Gateway", gatewayInfo.getGwClusterName());
		params.put("Code", envoyServiceProxyInfo.getCode());
		params.put("Subsets", envoyServiceProxyService.getSubsetsName(envoyServiceProxyInfo).stream()
		                                              .collect(Collectors.joining(",")));
		String backendService = envoyServiceProxyInfo.getBackendService();
		RegistryCenterEnum registryCenterEnum = RegistryCenterEnum.get(envoyServiceProxyInfo.getRegistryCenterType());
		if (Const.DYNAMIC_PUBLISH_TYPE.equals(envoyServiceProxyInfo.getPublishType())) {
			params.put("Host", backendService);
		}

		HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal",
		                                                             params, null, null, HttpMethod.GET.name());
		if (response == null) {
			logger.info("??????api-plane????????????????????????????????????????????????Response?????????serviceId:{}???gwId:{}", serviceId, gwId);
			return -1;
		}
		if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
			logger.info("??????api-plane?????????????????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
			            response.getStatusCode(), response.getResponseBody());
			return -1;
		}

		try {
			EnvoyServiceInstanceListObject envoyServiceInstanceListObject = JSONObject.parseObject(
				response.getResponseBody(), EnvoyServiceInstanceListObject.class);
			for (InnerEnvoyServiceInstance innerEnvoyServiceInstance : envoyServiceInstanceListObject
				                                                           .getServiceInstanceList()) {
				//??????????????????1
				int healthNum = 0;
				int unHealthNum = 0;
				for (EnvoyEndPoint envoyEndPoint : innerEnvoyServiceInstance.getEnvoyEndPointList()) {
					if (HEALTHY.equalsIgnoreCase(envoyEndPoint.getStatus())) {
						healthNum++;
					} else {
						unHealthNum++;
					}
				}

				if (healthNum == innerEnvoyServiceInstance.getEnvoyEndPointList().size()) {
					//??????????????????1
					return 1;
				} else if (unHealthNum == innerEnvoyServiceInstance.getEnvoyEndPointList().size()) {
					//?????????????????????0
					return 0;
				} else {
					//??????????????????2
					return 2;
				}
			}
		} catch (Exception e) {
			logger.info("??????api-plane????????????????????????????????????????????????body?????? ", e);
			return -1;
		}

		return 1;
	}

	@Override
	public ErrorCode checkUpdateHealthCheckRuleParam(EnvoyHealthCheckRuleDto dto) {
		long serviceId = dto.getServiceId();
		long gwId = dto.getGwId();

		if (!gatewayInfoService.isGwExists(gwId)) {
			return CommonErrorCode.InvalidParameterGwId(String.valueOf(gwId));
		}

		if (!serviceInfoService.isServiceExists(serviceId)) {
			return CommonErrorCode.InvalidParameterServiceId(String.valueOf(serviceId));
		}

		long activeSwitch = dto.getActiveSwitch();
		if (activeSwitch != 0 && activeSwitch != 1) {
			return CommonErrorCode.InvalidActiveSwitch;
		}

		if (activeSwitch == 1) {
			//?????????????????????/??????
			String path = dto.getPath();
			if (StringUtils.isBlank(path) || path.length() > 200 || !path.startsWith("/")) {
				return CommonErrorCode.InvalidApiPath;
			}

			long timeout = dto.getTimeout();
			if (timeout <= 0) {
				//?????????????????????
				return CommonErrorCode.InvalidTimeout;
			}

			List<Integer> expectedStatuses = dto.getExpectedStatuses();
			if (expectedStatuses.size() > 10 || expectedStatuses.size() == 0) {
				//????????????????????????
				return CommonErrorCode.InvalidHttpStatusCode;
			}
			for (int expectStatus : expectedStatuses) {
				if (expectStatus <= 0 || expectStatus >= 1000) {
					//????????????????????????
					return CommonErrorCode.InvalidHttpStatusCode;
				}
			}

			long healthyInterval = dto.getHealthyInterval();
			if (healthyInterval <= 0 || healthyInterval > 1000000000) {
				//?????????????????????????????????
				return CommonErrorCode.InvalidHealthyInterval;
			}

			long healthyThreshold = dto.getHealthyThreshold();
			if (healthyThreshold <= 0 || healthyThreshold > 1000000000) {
				//?????????????????????
				return CommonErrorCode.InvalidHealthyThreshold;
			}

			long unHealthyInterval = dto.getUnhealthyInterval();
			if (unHealthyInterval <= 0 || unHealthyInterval > 1000000000) {
				//?????????????????????????????????
				return CommonErrorCode.InvalidUnHealthyInterval;
			}

			long unHealthyThreshold = dto.getUnhealthyThreshold();
			if (unHealthyThreshold <= 0 || unHealthyThreshold > 1000000000) {
				//?????????????????????
				return CommonErrorCode.InvalidUnHealthyThreshold;
			}
		}

		long passiveSwitch = dto.getPassiveSwitch();
		if (passiveSwitch != 0 && passiveSwitch != 1) {
			//???????????????????????????
			return CommonErrorCode.InvalidPassiveSwitch;
		}

		if (passiveSwitch == 1) {
			long consecutiveErrors = dto.getConsecutiveErrors();
			if (consecutiveErrors <= 0 || consecutiveErrors > 1000000000) {
				//???????????????????????????
				return CommonErrorCode.InvalidConsecutiveErrors;
			}

			long baseEjectionTime = dto.getBaseEjectionTime();
			if (baseEjectionTime <= 0 || baseEjectionTime > 1000000000) {
				//?????????????????????
				return CommonErrorCode.InvalidBaseEjectionTime;
			}

			long maxEjectionPercent = dto.getMaxEjectionPercent();
			if (maxEjectionPercent <= 0 || maxEjectionPercent > 100) {
				//???????????????????????????
				return CommonErrorCode.InvalidMaxEjectionPercent;
			}
		}
		return CommonErrorCode.Success;
	}

	@Override
	public EnvoyServiceTrafficPolicyDto setHealthCheck(EnvoyServiceTrafficPolicyDto trafficPolicy,
	                                                   EnvoyHealthCheckRuleInfo healthCheckRuleInfo) {
		trafficPolicy = trafficPolicy == null ? new EnvoyServiceTrafficPolicyDto() : trafficPolicy;
		trafficPolicy.setPassiveHealthCheckRule(
			healthCheckRuleInfo.getPassiveSwitch() == 1 ? new EnvoyPassiveHealthCheckRuleDto(healthCheckRuleInfo)
			                                            : null);
		trafficPolicy.setActiveHealthCheckRule(
			healthCheckRuleInfo.getActiveSwitch() == 1 ? new EnvoyActiveHealthCheckRuleDto(healthCheckRuleInfo) :
			null);
		return trafficPolicy;
	}

	@Override
	public List<EnvoySubsetDto> setSubsetHealthCheck(List<EnvoySubsetDto> subsetDtos,
	                                                 EnvoyHealthCheckRuleInfo healthCheckRuleInfo) {
		if (CollectionUtils.isEmpty(subsetDtos)) {
			return Lists.newArrayList();
		}
		return subsetDtos.stream().map(item -> {
			item.setTrafficPolicy(setHealthCheck(item.getTrafficPolicy(), healthCheckRuleInfo));
			return item;
		}).collect(Collectors.toList());
	}

	static class EnvoyServiceInstanceListObject implements Serializable {

		@JSONField(name = "RequestId")
		String requestId;

		/**
		 * ????????????
		 */
		@JSONField(name = "List")
		List<InnerEnvoyServiceInstance> serviceInstanceList;

		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}

		public List<InnerEnvoyServiceInstance> getServiceInstanceList() {
			return serviceInstanceList;
		}

		public void setServiceInstanceList(List<InnerEnvoyServiceInstance> serviceInstanceList) {
			this.serviceInstanceList = serviceInstanceList;
		}

	}

	static class InnerEnvoyServiceInstance implements Serializable {

		/**
		 * ???????????????????????????service????????????????????????????????????org.hango.static-{serviceId}
		 */
		@JSONField(name = "Name")
		private String Name;

		/**
		 * ????????????
		 */
		@JSONField(name = "Endpoints")
		private List<EnvoyEndPoint> envoyEndPointList;

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public List<EnvoyEndPoint> getEnvoyEndPointList() {
			return envoyEndPointList;
		}

		public void setEnvoyEndPointList(List<EnvoyEndPoint> envoyEndPointList) {
			this.envoyEndPointList = envoyEndPointList;
		}

	}

	static class EnvoyEndPoint implements Serializable {

		/**
		 * ip + port
		 */
		@JSONField(name = "address")
		private String address;

		/**
		 * ?????????????????????HEALTHY???????????????UNHEALTHY???????????????
		 */
		@JSONField(name = "Status")
		private String status;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}

}
