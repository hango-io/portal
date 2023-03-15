package org.hango.cloud.envoy.infra.virtualgateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.fabric8.kubernetes.api.model.gatewayapi.v1beta1.HTTPRoute;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayHttpRouteDTO;
import org.hango.cloud.envoy.infra.virtualgateway.service.impl.KubernetesGatewayServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KubernetesGatewayV1ControllerTests.class)
public class KubernetesGatewayV1ControllerTests {

    @Mock
    private IGatewayService gatewayService;

    @Mock
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Mock
    private IDomainInfoService domainInfoService;

    @Mock
    private IPluginInfoService pluginInfoService;

    @Mock
    private IVirtualGatewayDao virtualGatewayDao;

    @InjectMocks // 定义被测试类的对象
    KubernetesGatewayServiceImpl kubernetesGatewayService = new KubernetesGatewayServiceImpl();
    @InjectMocks // 定义被测试类的对象
    IVirtualGatewayInfoService virtualGatewayService = new VirtualGatewayServiceImpl();

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testGetKubernetesGatewayList() {
        VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
        virtualGatewayDto.setConfAddr("http://127.0.0.1");
        virtualGatewayDto.setGwName("gateway");
        String rsp = "{\"RequestId\":\"8360c425-58c1-48f5-9c64-501de461fd05\",\"List\":[{\"apiVersion\":\"gateway.networking.k8s.io/v1alpha2\",\"kind\":\"HTTPRoute\",\"metadata\":{\"name\":\"http\",\"namespace\":\"gateway-system\"},\"spec\":{\"hostnames\":[\"httpbin.example.com\"],\"parentRefs\":[{\"kind\":\"Gateway\",\"group\":\"gateway.networking.k8s.io\",\"name\":\"gateway\",\"namespace\":\"gateway-system\"}],\"rules\":[{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status\"}}]},{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"filters\":[{\"requestHeaderModifier\":{\"add\":[{\"name\":\"my-header\",\"value\":\"foo\"}]},\"type\":\"RequestHeaderModifier\"}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status/200\"}}]}]}},{\"apiVersion\":\"gateway.networking.k8s.io/v1alpha2\",\"kind\":\"HTTPRoute\",\"metadata\":{\"name\":\"http2\",\"namespace\":\"gateway-system\"},\"spec\":{\"hostnames\":[\"httpbin.example.com\"],\"parentRefs\":[{\"kind\":\"Gateway\",\"group\":\"gateway.networking.k8s.io\",\"name\":\"gateway\",\"namespace\":\"gateway-system\"}],\"rules\":[{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status\"}}]},{\"backendRefs\":[{\"kind\":\"Service\",\"group\":\"\",\"name\":\"httpbin2\",\"namespace\":\"apigw-demo\",\"port\":80,\"weight\":1}],\"filters\":[{\"requestHeaderModifier\":{\"add\":[{\"name\":\"my-header\",\"value\":\"foo\"}]},\"type\":\"RequestHeaderModifier\"},{\"requestHeaderModifier\":{\"add\":[{\"name\":\"my-header\",\"value\":\"foo\"}]},\"type\":\"RequestHeaderModifier\"}],\"matches\":[{\"path\":{\"type\":\"PathPrefix\",\"value\":\"/status/200\"}}]}]}}],\"Code\":\"Success\"}";
        JSONObject jsonObject = JSON.parseObject(rsp);
        List<HTTPRoute> kubernetesGatewayHttpRouteList = JSON
            .parseArray(jsonObject.getString(BaseConst.RESULT_LIST), HTTPRoute.class);
        Mockito.when(virtualGatewayService.get(Long.parseLong("1"))).thenReturn(virtualGatewayDto);
        List<KubernetesGatewayHttpRouteDTO> httpRouteDTOS = kubernetesGatewayService
            .httpRouteListToView(kubernetesGatewayHttpRouteList);
        assertTrue(httpRouteDTOS.size() == 2);
    }
}
