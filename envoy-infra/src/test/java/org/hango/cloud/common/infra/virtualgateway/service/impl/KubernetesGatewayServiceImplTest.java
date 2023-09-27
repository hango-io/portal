package org.hango.cloud.common.infra.virtualgateway.service.impl;

import com.alibaba.fastjson.JSONArray;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.impl.DomainInfoServiceImpl;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.pluginmanager.service.impl.PluginManagerServiceImpl;
import org.hango.cloud.envoy.infra.virtualgateway.dto.*;
import org.hango.cloud.envoy.infra.virtualgateway.rpc.VirtualGatewayRpcService;
import org.hango.cloud.envoy.infra.virtualgateway.service.IKubernetesGatewayService;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_GATEWAY;
import static org.hango.cloud.util.MockUtil.HOST;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Author zhufengwei
 * @Date 2023/7/18
 */
@SuppressWarnings({"java:S1192"})
@SpringBootTest
public class KubernetesGatewayServiceImplTest {

    @Autowired
    IKubernetesGatewayService iKubernetesGatewayService;

    @Autowired
    IGatewayService gatewayService;

    @Autowired
    VirtualGatewayServiceImpl virtualGatewayService;

    @Autowired
    DomainInfoServiceImpl domainInfoService;

    @MockBean
    PluginManagerServiceImpl pluginManagerService;

    @MockBean
    VirtualGatewayRpcService virtualGatewayRpcService;


    public static final String INGRESS_NAME = "test/gateway-system";

    public static final String K8S_GATEWAY_PATH = "api/gatewayapi";

    public static final String K8S_GATEWAY_NAME = "k8s-gateway";



    @BeforeEach
    public void mock(){
        MockitoAnnotations.openMocks(this);
        Mockito.when(pluginManagerService.publishPluginManager(Mockito.any())).thenReturn(true);
        Mockito.when(virtualGatewayRpcService.getKubernetesIngress(Mockito.any(), Mockito.any())).thenReturn(mockIngres());
        Mockito.when(virtualGatewayRpcService.getKubernetesGateway(Mockito.any(), Mockito.any())).thenReturn(mockK8sGateway());
        Mockito.when(virtualGatewayRpcService.getKubernetesGatewayHttpRoute(Mockito.any(), Mockito.any())).thenReturn(mockHttpRoute());

    }

    @Test
    public void getIngress() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        virtualGatewayDto.setName(INGRESS_NAME);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        IngressViewDTO ingress = iKubernetesGatewayService.getIngress(vgId);
        Assertions.assertEquals(ingress.getName(), INGRESS_NAME);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }

    @Test
    public void getKubernetesGatewayList() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        virtualGatewayDto.setName(K8S_GATEWAY_NAME);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        List<KubernetesGatewayDTO> kubernetesGatewayList = iKubernetesGatewayService.getKubernetesGatewayList(vgId);
        Assertions.assertEquals(kubernetesGatewayList.size(), 1);
        Assertions.assertEquals(kubernetesGatewayList.get(0).getHostname(), HOST);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }

    @Test
    public void getKubernetesHTTPRoute() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        virtualGatewayDto.setName(K8S_GATEWAY_NAME);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        List<KubernetesGatewayHttpRouteDTO> httpRouteDTOS = iKubernetesGatewayService.getKubernetesGatewayHTTPRouteList(vgId);
        Assertions.assertEquals(httpRouteDTOS.size(), 1);
        Assertions.assertEquals(httpRouteDTOS.get(0).getRouteName(), "httproute");
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }


    @Test
    public void getKubernetesGatewayYaml() {
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        long gwId = gatewayService.create(gatewayDto);
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        virtualGatewayDto.setName(K8S_GATEWAY_PATH);
        virtualGatewayDto.setGwId(gwId);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);


        String kubernetesGatewayYaml = iKubernetesGatewayService.getKubernetesGatewayYaml(vgId);
        Assertions.assertEquals(kubernetesGatewayYaml, mockK8sGatewayYaml());

        gatewayService.delete(gatewayDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }


    @Test
    public void refreshK8sGateway() {
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        gatewayService.create(gatewayDto);

        ErrorCode errorCode = iKubernetesGatewayService.refreshK8sGateway();
        Assertions.assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayService.getByCode("k8s-gateway");
        assertNotNull(virtualGatewayDto);

        errorCode = iKubernetesGatewayService.refreshK8sGateway();
        Assertions.assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        gatewayService.delete(gatewayDto);
    }


    private List<IngressDTO> mockIngres(){
        String str = "[\n" +
                "        {\n" +
                "            \"Name\":\"test\",\n" +
                "            \"Namespace\":\"gateway-system\",\n" +
                "            \"ProjectCode\":\"project1\",\n" +
                "            \"ProjectId\":\"3\",\n" +
                "            \"Port\":\"80\",\n" +
                "            \"IngressRule\":[\n" +
                "                {\n" +
                "                    \"Host\":\"test.com\",\n" +
                "                    \"HTTPRules\":[\n" +
                "                        {\n" +
                "                            \"Path\":\"/test\",\n" +
                "                            \"PathType\":\"Prefix\",\n" +
                "                            \"ServiceName\":\"istio-e2e-app\",\n" +
                "                            \"ServicePort\":80\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ],\n" +
                "            \"Content\":\"---\\napiVersion: \\\"networking.k8s.io/v1\\\"\\nkind: \\\"Ingress\\\"\\nmetadata:\\n  name: \\\"test\\\"\\n  namespace: \\\"gateway-system\\\"\\nspec:\\n  rules:\\n  - host: \\\"apigw-gportal.test320-qingzhou.com\\\"\\n    http:\\n      paths:\\n      - backend:\\n          service:\\n            name: \\\"istio-e2e-app\\\"\\n            port:\\n              number: 80\\n        path: \\\"/test\\\"\\n        pathType: \\\"Prefix\\\"\\n\"\n" +
                "        }\n" +
                "    ]";
        return JSONArray.parseArray(str, IngressDTO.class);
    }



    private List<KubernetesGatewayInfo> mockK8sGateway(){
        String str = "[\n" +
                "        {\n" +
                "            \"Name\":\"k8s-gateway\",\n" +
                "            \"ProjectCode\":\"project1\",\n" +
                "            \"ProjectId\":\"3\",\n" +
                "            \"Protocol\":\"HTTP\",\n" +
                "            \"Host\":\"*.example.com\",\n" +
                "            \"RouteHosts\":[\n" +
                "                \"httpbin.example.com\"\n" +
                "            ],\n" +
                "            \"Port\":88,\n" +
                "            \"Content\":\"---\\napiVersion: \\\"gateway.networking.k8s.io/v1alpha2\\\"\\nkind: \\\"Gateway\\\"\\nmetadata:\\n  name: \\\"k8s-gateway\\\"\\n  namespace: \\\"gateway-system\\\"\\nspec:\\n  addresses:\\n  - type: \\\"Hostname\\\"\\n    value: \\\"gateway-proxy.gateway-system.svc.cluster.local\\\"\\n  gatewayClassName: \\\"istio\\\"\\n  listeners:\\n  - allowedRoutes:\\n      namespaces:\\n        from: \\\"Same\\\"\\n    hostname: \\\"*.example.com\\\"\\n    name: \\\"http88\\\"\\n    port: 88\\n    protocol: \\\"HTTP\\\"\\n\"\n" +
                "        }\n" +
                "    ]";
        List<KubernetesGatewayInfo> kubernetesGatewayInfos = JSONArray.parseArray(str, KubernetesGatewayInfo.class);
        kubernetesGatewayInfos.forEach(o -> o.setType(KUBERNETES_GATEWAY));
        return kubernetesGatewayInfos;
    }

    private String mockK8sGatewayYaml(){
        return "---\n" +
                "apiVersion: \"gateway.networking.k8s.io/v1alpha2\"\n" +
                "kind: \"Gateway\"\n" +
                "metadata:\n" +
                "  name: \"k8s-gateway\"\n" +
                "  namespace: \"gateway-system\"\n" +
                "spec:\n" +
                "  addresses:\n" +
                "  - type: \"Hostname\"\n" +
                "    value: \"gateway-proxy.gateway-system.svc.cluster.local\"\n" +
                "  gatewayClassName: \"istio\"\n" +
                "  listeners:\n" +
                "  - allowedRoutes:\n" +
                "      namespaces:\n" +
                "        from: \"Same\"\n" +
                "    hostname: \"*.example.com\"\n" +
                "    name: \"http88\"\n" +
                "    port: 88\n" +
                "    protocol: \"HTTP\"\n";
    }

    private List<HTTPRoute> mockHttpRoute(){
        String str = "[{\"apiVersion\":\"gateway.networking.k8s.io/v1alpha2\",\"kind\":\"HTTPRoute\",\"metadata\":{\"name\":\"httproute\",\"namespace\":\"gateway-system\"},\"spec\":{\"hostnames\":[\"httpbin.example.com\"],\"parentRefs\":[{\"kind\":\"Gateway\",\"group\":\"gateway.networking.k8s.io\",\"name\":\"k8s-gateway\",\"namespace\":\"gateway-system\"}],\"rules\":[{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status\"}}]},{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status/200\"}}]}]}}]";
        return JSONArray.parseArray(str, HTTPRoute.class);
    }
}
