package org.hango.cloud.util;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.HttpRetryDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.RouteMapMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.dto.ServiceMetaForRouteDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceTrafficPolicyDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hango.cloud.common.infra.base.meta.BaseConst.DYNAMIC_PUBLISH_TYPE;

/**
 * @Author zhufengwei
 * @Date 2023/7/14
 */
@SuppressWarnings({"java:S1192"})
public class MockUtil {

    /****************************** 参数 ******************************/
    public static final String SERVICE_NAME = "test-service";
    public static final String ROUTE_NAME = "test-route";
    public static final String EXACT_MATCH_TYPE = "exact";
    public static final String HOST = "test.com";
    public static final String VIRTUAL_GW_NAME = "test-virtual-gw";
    public static final String VIRTUAL_GW_CODE = VIRTUAL_GW_NAME;
    public static final String VIRTUAL_GW_TYPE = "NetworkProxy";
    public static final String GW_TYPE = "envoy";
    // 以下参数为插件绑定信息mock参数
    public static final String PLUGIN_TYPE = "ip-restriction";
    public static final String PLUGIN_BINDING_OBJ_TYPE = "global";
    public static final String PLUGIN_STATUS = "enable";
    public static final String PLUGIN_CONFIG = "{\"kind\":\"ip-restriction\",\"type\":\"0\",\"list\":[\"1.1.1.1\",\"3.3.3.3\"]}";
    public static final Long PROJECT_ID = 3L;


    private PluginTemplateDto ofTemplateInfo() {
        PluginTemplateDto templateInfo = new PluginTemplateDto();
        templateInfo.setPluginType(PLUGIN_TYPE);
        templateInfo.setPluginConfiguration(PLUGIN_CONFIG);
        templateInfo.setProjectId(PROJECT_ID);
        templateInfo.setTemplateVersion(1);
        templateInfo.setTemplateNotes("-");
        templateInfo.setTemplateName("test-plugin-template");
        return templateInfo;
    }

    public static GatewayDto initGatewayDto() {
        // mock物理网关对象
        GatewayDto gatewayDto = new GatewayDto();
        gatewayDto.setName("test-gw");
        gatewayDto.setEnvId("test");
        gatewayDto.setSvcName("-");
        gatewayDto.setSvcType("-");
        gatewayDto.setType(GW_TYPE);
        gatewayDto.setGwClusterName("test-gw");
        gatewayDto.setConfAddr("-");
        return gatewayDto;
    }

    public static VirtualGatewayDto initVirtualGateway(String protocol, GatewayDto gatewayDto) {
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setConfAddr("http://127.0.0.1");
        if (gatewayDto != null){
            virtualGatewayDto.setGwId(gatewayDto.getId());
            virtualGatewayDto.setGwName(gatewayDto.getName());
            virtualGatewayDto.setGwClusterName(gatewayDto.getGwClusterName());
        }
        virtualGatewayDto.setName(VIRTUAL_GW_NAME);
        virtualGatewayDto.setCode(VIRTUAL_GW_CODE);
        virtualGatewayDto.setType(VIRTUAL_GW_TYPE);
        virtualGatewayDto.setProjectIdList(Collections.singletonList(PROJECT_ID));
        virtualGatewayDto.setProtocol(protocol);
        return virtualGatewayDto;
    }

    public static DomainInfoDTO initHttpDomainInfo() {
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setHost(HOST);
        domainInfoDTO.setDescription("test");
        domainInfoDTO.setProjectId(PROJECT_ID);
        domainInfoDTO.setProtocol("HTTP");
        return domainInfoDTO;
    }


    public static RouteDto initRouteDtoWithServiceMeta(){
        RouteDto routeDto = new RouteDto();
        ServiceMetaForRouteDto serviceMetaForRouteDto = new ServiceMetaForRouteDto();

        DestinationDto dto = new DestinationDto();
        dto.setSubsetName("test-subset-service");
        serviceMetaForRouteDto.setDestinationServices(Collections.singletonList(dto));
        routeDto.setServiceMetaForRouteDtos(Collections.singletonList(serviceMetaForRouteDto));
        return routeDto;
    }

    public static DomainInfoDTO initHttpsDomainInfo(Long certId, String certName) {
        DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
        domainInfoDTO.setHost(HOST);
        domainInfoDTO.setDescription("test");
        domainInfoDTO.setProjectId(PROJECT_ID);
        domainInfoDTO.setProtocol("HTTPS");
        domainInfoDTO.setCertificateId(certId);
        domainInfoDTO.setCertificateName(certName);
        return domainInfoDTO;
    }


    public static ServiceProxyDto initServiceProxy(){
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setName(SERVICE_NAME);
        serviceProxyDto.setAlias("测试服务");
        serviceProxyDto.setPublishType(DYNAMIC_PUBLISH_TYPE);
        serviceProxyDto.setProtocol("http");
        serviceProxyDto.setRegistryCenterType("Kubernetes");
        serviceProxyDto.setBackendService("httpbin.apigw-demo.svc.cluster.local");
        serviceProxyDto.setHosts(HOST);
        serviceProxyDto.setTrafficPolicy(initTraffic());

        return serviceProxyDto;
    }

    public static ServiceProxyDto initDubboService(){
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setName(SERVICE_NAME);
        serviceProxyDto.setAlias("测试服务");
        serviceProxyDto.setPublishType(DYNAMIC_PUBLISH_TYPE);
        serviceProxyDto.setProtocol("http");
        serviceProxyDto.setRegistryCenterType("Zookeeper");
        serviceProxyDto.setBackendService("com.netease.apigateway.dubbo.api.GatewayEchoService:group-a:v1.dubbo");
        serviceProxyDto.setHosts(HOST);
        serviceProxyDto.setTrafficPolicy(initTraffic());

        return serviceProxyDto;
    }

    public static ServiceTrafficPolicyDto initTraffic(){
        String str = "{\n" +
                "    \"LoadBalancer\": {\n" +
                "      \"Type\": \"Simple\",\n" +
                "      \"Simple\": \"ROUND_ROBIN\",\n" +
                "      \"ConsistentHash\": {\n" +
                "        \"Type\": \"HttpHeaderName\",\n" +
                "        \"HttpCookie\": {\n" +
                "          \"Name\": \"\",\n" +
                "          \"TTL\": \"\"\n" +
                "        },\n" +
                "        \"UseSourceIp\": true\n" +
                "      },\n" +
                "      \"LocalitySetting\": {\n" +
                "        \"Enable\": true\n" +
                "      }\n" +
                "    },\n" +
                "    \"ConnectionPool\": {\n" +
                "      \"HTTP\": {\n" +
                "        \"Http1MaxPendingRequests\": \"1024\",\n" +
                "        \"Http2MaxRequests\": \"1024\",\n" +
                "        \"MaxRequestsPerConnection\": \"0\",\n" +
                "        \"IdleTimeout\": \"3000\",\n" +
                "        \"_formTableKey\": 1689305614704\n" +
                "      },\n" +
                "      \"TCP\": {\n" +
                "        \"MaxConnections\": \"1024\",\n" +
                "        \"ConnectTimeout\": \"60000\",\n" +
                "        \"_formTableKey\": 1689305614702\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        return JSONObject.parseObject(str, ServiceTrafficPolicyDto.class);
    }


    public static RouteDto initRoute(Long serviceId){
        //路由信息
        RouteDto routeDto = new RouteDto();
        //构造路由规则
        routeDto.setName(ROUTE_NAME);
        routeDto.setDescription(ROUTE_NAME);

        RouteMapMatchDto headers = new RouteMapMatchDto();
        headers.setKey("abc");
        headers.setType(EXACT_MATCH_TYPE);
        headers.setValue(asList("abc"));
        routeDto.setHeaders(asList(headers));

        RouteMapMatchDto querys = new RouteMapMatchDto();
        querys.setKey("aaa");
        querys.setType(EXACT_MATCH_TYPE);
        querys.setValue(asList("caa"));
        routeDto.setQueryParams(asList(querys));

        RouteStringMatchDto host = new RouteStringMatchDto();
        host.setType(EXACT_MATCH_TYPE);
        host.setValue(asList("abc.com"));

        routeDto.setMethod(Arrays.asList("GET"));

        RouteStringMatchDto uri = new RouteStringMatchDto();
        uri.setType(EXACT_MATCH_TYPE);
        uri.setValue(Collections.singletonList("/abc"));
        routeDto.setUriMatchDto(uri);
        HttpRetryDto retryDto = new HttpRetryDto();
        retryDto.setRetry(false);
        routeDto.setHttpRetryDto(retryDto);
        routeDto.setPriority(50);
        ServiceMetaForRouteDto serviceMetaForRouteDto = new ServiceMetaForRouteDto();
        serviceMetaForRouteDto.setServiceId(serviceId);
        serviceMetaForRouteDto.setWeight(100);
        serviceMetaForRouteDto.setPort(80);
        routeDto.setServiceMetaForRouteDtos(Collections.singletonList(serviceMetaForRouteDto));
        if (serviceId != null) {
            routeDto.getServiceIds().add(serviceId);
        }
        return routeDto;
    }



    public static PluginBindingDto initProjectPluginBindingDto(Long vgId) {
        // mock插件信息
        PluginBindingDto pluginBindingDto = new PluginBindingDto();
        pluginBindingDto.setPluginType(PLUGIN_TYPE);
        pluginBindingDto.setPluginConfiguration(PLUGIN_CONFIG);
        pluginBindingDto.setBindingObjectId(String.valueOf(vgId));
        pluginBindingDto.setProjectId(PROJECT_ID);
        pluginBindingDto.setBindingStatus(PLUGIN_STATUS);
        pluginBindingDto.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
        pluginBindingDto.setGwType(GW_TYPE);
        pluginBindingDto.setVirtualGwId(vgId);
        return pluginBindingDto;
    }

    public static DubboBindingDto initDubboBindingDto() {
        String str = "{\n" +
                "  \"ObjectType\": \"route\",\n" +
                "  \"ObjectId\": 4450,\n" +
                "  \"Params\": [\n" +
                "    {\n" +
                "      \"Key\": \"str\",\n" +
                "      \"Value\": \"java.lang.String\",\n" +
                "      \"Required\": false,\n" +
                "      \"DefaultValue\": \"aaa\",\n" +
                "      \"_formTableKey\": 1689834753780,\n" +
                "      \"index\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"Key\": \"int\",\n" +
                "      \"Value\": \"java.lang.Integer\",\n" +
                "      \"DefaultValue\": null,\n" +
                "      \"_formTableKey\": 1689834753780\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Method\": \"echoStrAndInt\",\n" +
                "  \"CustomParamMapping\": true,\n" +
                "  \"ParamSource\": \"body\",\n" +
                "  \"Attachment\": [\n" +
                "    {\n" +
                "      \"ClientParamName\": \"abc\",\n" +
                "      \"Description\": \"\",\n" +
                "      \"Enable\": true,\n" +
                "      \"ParamPosition\": \"Cookie\",\n" +
                "      \"ServerParamName\": \"acx\",\n" +
                "      \"_formTableKey\": 1689834783589\n" +
                "    }\n" +
                "  ],\n" +
                "  \"MethodWorks\": true\n" +
                "}";
        return JSONObject.parseObject(str, DubboBindingDto.class);
    }

}
