package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.common.collect.Lists;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.envoy.meta.GatewayInfo;
import org.hango.cloud.dashboard.envoy.meta.common.HttpClientResponse;
import org.hango.cloud.dashboard.envoy.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.envoy.service.IGatewayInfoService;
import org.hango.cloud.dashboard.envoy.service.IServiceInfoService;
import org.hango.cloud.dashboard.envoy.util.Const;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPublishServiceDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyStringMatchDto;
import org.hango.cloud.dashboard.envoy.meta.EnvoyHealthCheckRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyRouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.EnvoyServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyHealthCheckService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyRouteRuleInfoService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyRouteRuleProxyService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyServiceProxyService;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleMapMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.util.HttpCommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ApiPlane??????????????????????????????service???route???
 */
@Service
public class GetFromApiPlaneServiceImpl implements IGetFromApiPlaneService {

	private static final Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyServiceImpl.class);

	@Autowired
	private IGatewayInfoService gatewayInfoService;

	@Autowired
	private IServiceInfoService serviceInfoService;

	@Autowired
	private IEnvoyHealthCheckService envoyHealthCheckService;

	@Autowired
	private IEnvoyServiceProxyService envoyServiceProxyService;

	@Autowired
	private IEnvoyRouteRuleInfoService routeRuleInfoService;

	@Autowired
	private IEnvoyRouteRuleProxyService envoyRouteRuleProxyService;

	@Override
	public List<EnvoyServiceWithPortDto> getServiceListFromApiPlane(long gwId, String name, String registryCenterType) {
		GatewayInfo gatewayById = gatewayInfoService.get(gwId);
		if (gatewayById == null) {
			return new ArrayList<>();
		}
		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "GetServiceAndPortList");
		params.put("Version", "2019-07-25");
		if (StringUtils.isNotBlank(name)) {
			params.put("Name", name);
		}
		params.put("Type", registryCenterType);
		HttpClientResponse response = HttpCommonUtil.getFromApiPlane(gatewayById.getApiPlaneAddr() + "/api", params,
		                                                             StringUtils.EMPTY, null, HttpMethod.GET.name());
		if (null == response) {
			return null;
		}
		if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
			logger.error("??????api-plane????????????????????????????????????http status code???2xx, httpStatuCode:{}, errMsg:{}",
			             response.getStatusCode(), response.getResponseBody());
			return null;
		}
		JSONObject jsonResult = JSONObject.parseObject(response.getResponseBody());
		JSONArray services = jsonResult.getJSONArray("ServiceList");
		List<EnvoyServiceWithPortDto> serviceNameList = JSONObject.parseArray(services.toJSONString(),
		                                                                      EnvoyServiceWithPortDto.class);
		if (CollectionUtils.isEmpty(serviceNameList) || RegistryCenterEnum.Kubernetes.getType().equals(
			registryCenterType)) {
			return serviceNameList;
		}
		for (EnvoyServiceWithPortDto envoyServiceWithPortDto : serviceNameList) {
			envoyServiceWithPortDto.setName(StringUtils.substring(envoyServiceWithPortDto.getName(), 0, StringUtils
				                                                                                            .indexOf(
					                                                                                            envoyServiceWithPortDto
						                                                                                            .getName(),
					                                                                                            ".")));
		}
		return serviceNameList;
	}

	/**
	 * ??????????????????????????????????????????????????????publishHealthCheckRuleByApiPlane????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @param envoyServiceProxyDto ??????????????????dto
	 */
	@Override
	public boolean publishServiceByApiPlane(EnvoyServiceProxyDto envoyServiceProxyDto,
	                                        EnvoyHealthCheckRuleInfo envoyHealthCheckRuleInfo) {
		if (envoyServiceProxyDto == null) {
			//??????????????????????????????: ?????????envoyServiceProxyDto???null
			EnvoyServiceProxyInfo envoyServiceProxyInfo = envoyServiceProxyService.getServiceProxyByServiceIdAndGwId(
				envoyHealthCheckRuleInfo.getGwId(), envoyHealthCheckRuleInfo.getServiceId());
			envoyServiceProxyDto = EnvoyServiceProxyDto.toDto(envoyServiceProxyInfo);
		}
		long gwId = envoyServiceProxyDto.getGwId();
		long serviceId = envoyServiceProxyDto.getServiceId();

		String code = new StringBuilder().append(envoyServiceProxyDto.getPublishType()).append("-").append(
			envoyServiceProxyDto.getServiceId()).toString();

		GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "PublishService");
		params.put("Version", "2019-07-25");

		EnvoyPublishServiceDto envoyPublishServiceDto = new EnvoyPublishServiceDto();
		envoyPublishServiceDto.setCode(code);
		if (gatewayInfo != null) {
			//??????????????????
			envoyPublishServiceDto.setGateway(gatewayInfo.getGwClusterName());
		}

		String backendService = envoyServiceProxyDto.getBackendService();

		envoyPublishServiceDto.setBackendService(backendService);
		envoyPublishServiceDto.setType(envoyServiceProxyDto.getPublishType());
		envoyPublishServiceDto.setProtocol(envoyServiceProxyDto.getPublishProtocol());

		//??????????????????????????????
		envoyPublishServiceDto.setServiceTag(
			serviceInfoService.getServiceByServiceId(envoyServiceProxyDto.getServiceId()).getServiceName());
		envoyPublishServiceDto.setLoadBalancer(envoyServiceProxyDto.getLoadBalancer());

		//??????????????????
		envoyPublishServiceDto.setSubsets(envoyServiceProxyService
			                                  .setSubsetForDtoWhenSendToAPIPlane(envoyServiceProxyDto,
			                                                                     gatewayInfo.getGwClusterName()));

		//??????????????????
		EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto = envoyServiceProxyDto.getTrafficPolicy() == null
		                                                            ? new EnvoyServiceTrafficPolicyDto()
		                                                            : envoyServiceProxyDto.getTrafficPolicy();
		//??????????????????
		EnvoyHealthCheckRuleDto envoyHealthCheckRuleDto = envoyHealthCheckService.getHealthCheckRule(serviceId, gwId);
		if (envoyHealthCheckRuleInfo != null) {
			if (envoyHealthCheckRuleDto != null) {
				//??????????????????????????????: ?????????envoyHealthCheckRuleInfo??????null
				envoyServiceTrafficPolicyDto = envoyHealthCheckService.setHealthCheck(envoyServiceTrafficPolicyDto,
				                                                                      envoyHealthCheckRuleInfo);
				//??????subset???????????????
				List<EnvoySubsetDto> envoySubsetDtos = envoyServiceProxyService.setSubsetForDtoWhenSendToAPIPlane(
					envoyServiceProxyDto, gatewayInfo.getGwClusterName());
				envoyPublishServiceDto.setSubsets(
					envoyHealthCheckService.setSubsetHealthCheck(envoySubsetDtos, envoyHealthCheckRuleInfo));
			}
		} else {
			if (envoyHealthCheckRuleDto != null) {
				envoyServiceTrafficPolicyDto = envoyHealthCheckService.setHealthCheck(envoyServiceTrafficPolicyDto,
				                                                                      EnvoyHealthCheckRuleDto.dtoToMeta(
					                                                                      envoyHealthCheckRuleDto));
				//??????subset???????????????
				List<EnvoySubsetDto> envoySubsetDtos = envoyServiceProxyService.setSubsetForDtoWhenSendToAPIPlane(
					envoyServiceProxyDto, gatewayInfo.getGwClusterName());
				envoyPublishServiceDto.setSubsets(envoyHealthCheckService.setSubsetHealthCheck(envoySubsetDtos,
				                                                                               EnvoyHealthCheckRuleDto
					                                                                               .dtoToMeta(
						                                                                               envoyHealthCheckRuleDto)));
			}
		}

		//????????????????????????????????????
		envoyPublishServiceDto.setTrafficPolicy(envoyServiceTrafficPolicyDto);

		try {
			HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params,
			                                                     JSONObject.toJSONString(envoyPublishServiceDto), null,
			                                                     HttpMethod.POST.name());
			if (response == null) {
				return false;
			}
			if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
				logger.error("??????api-plane?????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
				             response.getStatusCode(), response.getResponseBody());
				return false;
			}
		} catch (Exception e) {
			logger.error("??????api-plane?????????????????????e:{}", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean offlineServiceByApiPlane(String apiPlaneAddr, EnvoyPublishServiceDto envoyPublishServiceDto) {
		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "DeleteService");
		params.put("Version", "2019-07-25");
		HttpClientResponse response = publishProxyToApiPlane(apiPlaneAddr + "/api/portal", params,
		                                                     JSONObject.toJSONString(envoyPublishServiceDto), null,
		                                                     HttpMethod.POST.name());
		if (response == null) {
			return false;
		}
		if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
			logger.error("??????api-plane?????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
			             response.getStatusCode(), response.getResponseBody());
			return false;
		}
		return true;
	}

	@Override
	public boolean publishRouteRuleByApiPlane(EnvoyRouteRuleProxyInfo routeRuleProxyInfo,
	                                          List<String> pluginConfigurations) {
		if (null == routeRuleProxyInfo) {
			logger.error("???????????????????????????????????????????????????????????????!");
			return false;
		}
		GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
		if (null == gatewayInfo) {
			logger.error("?????????????????????????????????????????????! gwId:{}", routeRuleProxyInfo.getGwId());
			return false;
		}

		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyInfo.getRouteRuleId());
		if (null == routeRuleInfo) {
			logger.error("???????????????????????????????????????????????????! routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
			return false;
		}

		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "PublishAPI");
		params.put("Version", "2019-07-25");

		Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		headers.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

		JSONObject body = generateBodyForPublishOrDelete(gatewayInfo, routeRuleInfo, routeRuleProxyInfo,
		                                                 pluginConfigurations, routeRuleProxyInfo.getHosts());
		try {
			HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params,
			                                                     body.toJSONString(), headers, HttpMethod.POST.name());
			if (null == response) {
				return false;
			}

			if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
				logger.error("??????api-plane?????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
				             response.getStatusCode(), response.getResponseBody());
				return false;
			}
		} catch (Exception e) {
			logger.error("??????API-plane??????API??????????????????,e{:}", e);
			return false;
		}

		return true;
	}

	@Override
	public boolean deleteRouteRuleByApiPlane(EnvoyRouteRuleProxyInfo routeRuleProxyInfo) {
		GatewayInfo gatewayInfo = gatewayInfoService.get(routeRuleProxyInfo.getGwId());
		if (null == gatewayInfo) {
			logger.error("?????????????????????????????????????????????! gwId:{}", routeRuleProxyInfo.getGwId());
			return false;
		}

		EnvoyRouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(
			routeRuleProxyInfo.getRouteRuleId());
		if (null == routeRuleInfo) {
			logger.error("???????????????????????????????????????????????????! routeRuleId:{}", routeRuleProxyInfo.getRouteRuleId());
			return false;
		}

		Map<String, String> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Action", "DeleteAPI");
		params.put("Version", "2019-07-25");

		Map<String, String> headers = new HashMap<>(Const.DEFAULT_MAP_SIZE);
		params.put("Content-type", Const.DEFAULT_CONTENT_TYPE);

		JSONObject body = generateBodyForPublishOrDelete(gatewayInfo, routeRuleInfo, routeRuleProxyInfo,
		                                                 Lists.newArrayList(), routeRuleProxyInfo.getHosts());
		HttpClientResponse response = publishProxyToApiPlane(gatewayInfo.getApiPlaneAddr() + "/api/portal", params,
		                                                     body.toJSONString(), headers, HttpMethod.POST.name());
		if (null == response) {
			return false;
		}

		if (!HttpCommonUtil.isNormalCode(response.getStatusCode())) {
			logger.error("??????api-plane?????????????????????????????????http status code???2xx???httpStatusCoed:{},errMsg:{}",
			             response.getStatusCode(), response.getResponseBody());
			return false;
		}

		return true;
	}

	public HttpClientResponse publishProxyToApiPlane(String apiPlaneUrl, Map<String, String> params, String body,
	                                                 Map<String, String> headers, String methodType) {
		return HttpCommonUtil.getFromApiPlane(apiPlaneUrl, params, body, headers, methodType);
	}

	public JSONObject generateBodyForPublishOrDelete(GatewayInfo gatewayInfo, EnvoyRouteRuleInfo routeRuleInfo,
	                                                 EnvoyRouteRuleProxyInfo routeRuleProxyInfo,

	                                                 List<String> pluginConfigurations, String hosts) {
		EnvoyRouteRuleProxyDto routeRuleProxyDto = envoyRouteRuleProxyService.fromMeta(routeRuleProxyInfo);
		JSONObject body = new JSONObject();
		body.put("Gateway", gatewayInfo.getGwClusterName());
		body.put("Code", String.valueOf(routeRuleInfo.getId()));
		body.put("Hosts", JSON.parseArray(hosts, String.class));

		body.put("RequestUris", routeRuleProxyDto.getUriMatchDto().getValue());
		body.put("UriMatch", routeRuleProxyDto.getUriMatchDto().getType());
		body.put("Plugins", pluginConfigurations);
		body.put("Order", routeRuleProxyInfo.getOrders());
		body.put("ProjectId", routeRuleInfo.getProjectId());
		if (StringUtils.isNotBlank(routeRuleInfo.getHeaderOperation())) {
			EnvoyRouteRuleHeaderOperationDto headerOperation = JSON.parseObject(routeRuleInfo.getHeaderOperation(),
			                                                                    EnvoyRouteRuleHeaderOperationDto.class);
			body.put("RequestOperation", headerOperation.getRequestOperation());
		}

		//?????????*
		if (routeRuleProxyDto.getMethodMatchDto() == null) {
			body.put("Methods", new JSONArray().fluentAddAll(Lists.newArrayList("*")));
		} else {
			body.put("Methods", routeRuleProxyDto.getMethodMatchDto().getValue());
		}

		// EnvoyRouteRuleDto ??????host????????????key??? :authority ???header match
		if (null != routeRuleProxyDto.getHostMatchDto()) {
			EnvoyRouteRuleMapMatchDto hostHeader = new EnvoyRouteRuleMapMatchDto();
			hostHeader.setKey(":authority");
			hostHeader.setType(routeRuleProxyDto.getHostMatchDto().getType());
			hostHeader.setValue(routeRuleProxyDto.getHostMatchDto().getValue());
			routeRuleProxyDto.getHeaders().add(hostHeader);
		}

		if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getHeaders())) {
			body.put("Headers", EnvoyStringMatchDto.generateDtoFromRouteRuleDto(routeRuleProxyDto.getHeaders()));
		}

		if (CollectionUtils.isNotEmpty(routeRuleProxyDto.getQueryParams())) {
			body.put("QueryParams",
			         EnvoyStringMatchDto.generateDtoFromRouteRuleDto(routeRuleProxyDto.getQueryParams()));
		}

		body.put("ServiceTag",
		         serviceInfoService.getServiceByServiceId(routeRuleInfo.getServiceId()).getServiceName());
		body.put("RouteId", routeRuleInfo.getId());
		body.put("RouteName", routeRuleInfo.getRouteRuleName());
		body.put("Timeout", routeRuleProxyDto.getTimeout());

		List<JSONObject> proxyServices = routeRuleProxyInfo.getDestinationServiceList().stream().map(
			destinationInfo -> {
				JSONObject proxyService = new JSONObject();
				EnvoyServiceProxyInfo serviceProxyInfo = envoyServiceProxyService.getServiceProxyByServiceIdAndGwId(
					gatewayInfo.getId(), destinationInfo.getServiceId());
				proxyService.put("Code", serviceProxyInfo.getCode());
				proxyService.put("Weight", destinationInfo.getWeight());
				proxyService.put("Port", destinationInfo.getPort());

				//??????????????????????????????????????????????????????????????????80
				if (Const.STATIC_PUBLISH_TYPE.equals(serviceProxyInfo.getPublishType())) {
					proxyService.put("Port", 80);
				}

				String backendService = serviceProxyInfo.getBackendService();

				proxyService.put("BackendService", backendService);
				proxyService.put("Type", serviceProxyInfo.getPublishType());
				//??????????????????
				if (StringUtils.isNotBlank(destinationInfo.getSubsetName())) {
					proxyService.put("Subset", (destinationInfo.getSubsetName() + "-" + gatewayInfo.getGwClusterName())
						                           .toLowerCase());
				}
				return proxyService;
			}).collect(Collectors.toList());

		body.put("ProxyServices", new JSONArray().fluentAddAll(proxyServices));
		body.put("HttpRetry", JSONObject.parseObject(routeRuleProxyInfo.getHttpRetry()));

		return body;
	}

}
