//package org.hango.cloud.common.infra.serviceproxy.service.impl;
//
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.util.List;
//import org.hango.cloud.BaseServiceImplTest;
//import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
//import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
//import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
//import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
//import org.hango.cloud.envoy.infra.serviceproxy.service.impl.EnvoyServiceProxyServiceImpl;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class ServiceProxyServiceImplTest extends BaseServiceImplTest {
//
//  @Autowired
//  ServiceProxyServiceImpl serviceProxyService;
//  @Autowired
//  ServiceInfoServiceImpl serviceInfoService;
//  @MockBean
//  EnvoyServiceProxyServiceImpl envoyServiceProxyService;
//  @MockBean // 定义Mock类
//  VirtualGatewayServiceImpl virtualGatewayService;
//
//
//
//  @Before
//  public void init(){
//    super.init();
//    MockitoAnnotations.openMocks(this);
//    long id = serviceInfoService.create(serviceDto);
//    serviceProxyDto.setServiceId(id);
//    Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
//    serviceProxyService.create(serviceProxyDto);
//  }
//
//  @After
//  public void tearDown() {
//    serviceInfoService.delete(serviceDto);
//    Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);
//    serviceProxyService.delete(serviceProxyDto);
//  }
//
//  @Test
//  public void create() {
//    assertTrue(serviceProxyService.get(serviceProxyDto.getId()).getServiceName().equals(SERVICE_NAME));
//    }
//
//  @Test
//  public void update() {
//    Mockito.when(envoyServiceProxyService.updateToGateway(Mockito.any())).thenReturn(true);
//    serviceProxyDto.setBackendService("httpbin.ns2.svc.cluster.local");
//    serviceProxyService.update(serviceProxyDto);
//    assertTrue(serviceProxyService.get(serviceProxyDto.getId()).getBackendService().equals("httpbin.ns2.svc.cluster.local"));
//  }
//
//  @Test
//  public void delete() {
//    Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);
//    serviceProxyService.delete(serviceProxyDto);
//  }
//
//  @Test
//  public void findAll() {
//    List<? extends ServiceProxyDto> serviceProxyList = serviceProxyService.findAll();
//    assertEquals(SERVICE_NAME, serviceProxyList.get(0).getServiceName());
//  }
//
//  @Test
//  public void testFindAll() {
//    List<? extends ServiceProxyDto> serviceProxyList = serviceProxyService.findAll(0, 1);
//    assertEquals(1, serviceProxyList.size());
//    assertEquals(SERVICE_NAME, serviceProxyList.get(0).getServiceName());
//  }
//
//  @Test
//  public void countAll() {
//    long count = serviceProxyService.countAll();
//    assertEquals(1, count);
//  }
//
//
//  @Test
//  public void checkCreateParam() {
//    ErrorCode errorCode = serviceProxyService.checkCreateParam(serviceProxyDto);
//    assertEquals(errorCode, CommonErrorCode.SERVICE_ALREADY_PUBLISHED);
//  }
//
//  @Test
//  public void checkUpdateParam() {
//    Mockito.when(virtualGatewayService.get(1L)).thenReturn(virtualGatewayDto);
//    ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
//    assertEquals(errorCode, CommonErrorCode.SUCCESS);
//  }
//
//  @Test
//  public void checkRouteMirrorSubset() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void checkDeleteParam() {
//    ErrorCode errorCode = serviceProxyService.checkDeleteParam(serviceProxyDto);
//    assertTrue(errorCode.equals(CommonErrorCode.NO_SUCH_GATEWAY));
//    Mockito.when(virtualGatewayService.get(Long.parseLong("1"))).thenReturn(virtualGatewayDto);
//    ErrorCode errorCode1 = serviceProxyService.checkDeleteParam(serviceProxyDto);
//    assertTrue(errorCode1.equals(CommonErrorCode.SUCCESS));
//
//  }
//
//  @Test
//  public void getBackendServicesFromDataPlane() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxy() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxy() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxy1() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxy2() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyWithPort() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyCount() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxyCount() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxyCount1() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void testGetServiceProxyCount2() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyByServiceIdAndGwId() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyInterByServiceIdAndGwIds() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyByServicePublishInfo() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyByServiceId() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getPublishedServiceGateway() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getRouteRuleNameWithServiceSubset() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void setSubsetForDtoWhenSendToDataPlane() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceProxyListByVirtualGwId() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void batchGetServiceProxyList() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getSubsetsName() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getServiceCode() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void getAllServiceTag() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void findAllServiceIdListByServiceName() {
//    // 待补充单元测试
//  }
//
//  @Test
//  public void checkSubsetWhenPublishService() {
//    // 待补充单元测试
//  }
//}