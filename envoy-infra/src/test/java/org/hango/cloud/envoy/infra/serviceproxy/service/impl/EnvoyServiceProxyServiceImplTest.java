//package org.hango.cloud.envoy.infra.serviceproxy.service.impl;
//
//
//import static org.junit.Assert.assertTrue;
//
//import org.hango.cloud.BaseServiceImplTest;
//import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
//import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
//import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
//import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
//import org.hango.cloud.envoy.infra.serviceproxy.dto.DpServiceProxyDto;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.mockito.Spy;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class EnvoyServiceProxyServiceImplTest extends BaseServiceImplTest {
//
//  @Autowired // service测试类
//  ServiceInfoServiceImpl serviceInfoService;
//
//  @Spy
//  @Autowired
//  EnvoyServiceProxyServiceImpl envoyServiceProxyService;
//  @MockBean // 定义Mock类
//  VirtualGatewayServiceImpl virtualGatewayService;
//  VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
//  ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
//
//  @Before
//  public void init(){
//    super.init();
//    MockitoAnnotations.openMocks(this);
//
//    virtualGatewayDto.setConfAddr("http://127.0.0.1");
//    virtualGatewayDto.setGwName("gateway");
//    virtualGatewayDto.setGwClusterName("test-gateway");
//
//    long id = serviceInfoService.create(serviceDto);
//    serviceProxyDto.setServiceId(id);
//    serviceProxyDto.setServiceName(SERVICE_NAME);
//    serviceProxyDto.setServiceTag(SERVICE_NAME);
//    serviceProxyDto.setCode(SERVICE_NAME);
//    serviceProxyDto.setVirtualGwId(1);
//    serviceProxyDto.setBackendService("istio-e2e.apigw-demo.cluster.svc.local");
//  }
//
//  @After
//  public void tearDown(){
//    serviceInfoService.delete(serviceDto);
//  }
//
//
//  @Test
//  public void toView() {
//    //envoyServiceProxyService.getBackendServiceSendToApiPlane() 已覆盖
//    DpServiceProxyDto dpServiceProxyDto = envoyServiceProxyService
//        .toView(serviceProxyDto, virtualGatewayDto);
//    assertTrue(dpServiceProxyDto.getServiceTag() ==  SERVICE_NAME);
//  }
//
//  @Test
//  public void updateToGateway() {
//////    Mockito.when(virtualGatewayService.get(Long.parseLong("1"))).thenReturn(virtualGatewayDto);
//////    Mockito.when(envoyServiceProxyService.publishToGateway(serviceProxyDto)).thenReturn(true);
////    assertTrue(envoyServiceProxyService.updateToGateway(serviceProxyDto));
//  }
//
//  @Test
//  public void deleteSomeSubset() {
//    Mockito.when(virtualGatewayService.get(Long.parseLong("1"))).thenReturn(virtualGatewayDto);
//    String serviceTag = envoyServiceProxyService.deleteSomeSubset(serviceProxyDto).getServiceTag();
//    assertTrue(SERVICE_NAME.equals(serviceTag));
//  }
//}