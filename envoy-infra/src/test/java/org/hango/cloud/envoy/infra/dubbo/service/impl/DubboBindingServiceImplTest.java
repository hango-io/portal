//package org.hango.cloud.envoy.infra.dubbo.service.impl;
//
//import org.apache.commons.lang3.math.NumberUtils;
//import org.assertj.core.util.Lists;
//import org.hango.cloud.BaseServiceImplTest;
//import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.base.meta.BaseConst;
//import org.hango.cloud.common.infra.route.service.impl.RouteServiceImpl;
//import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
//import org.hango.cloud.common.infra.serviceproxy.service.impl.ServiceProxyServiceImpl;
//import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
//import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
//import org.hango.cloud.envoy.infra.route.service.impl.EnvoyRouteServiceImpl;
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
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//
///**
// * @author zhangbj
// * @version 1.0
// * @Type
// * @Desc
// * @date 2023/1/13
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class DubboBindingServiceImplTest extends BaseServiceImplTest {
//
//    @Autowired
//    private ServiceInfoServiceImpl serviceInfoService;
//
//    @Autowired
//    private ServiceProxyServiceImpl serviceProxyService;
//
//    @Autowired
//    private RouteServiceImpl routeRuleProxyService;
//
//    @MockBean
//    EnvoyServiceProxyServiceImpl envoyServiceProxyService;
//
//    @MockBean
//    EnvoyRouteServiceImpl envoyRouteRuleProxyService;
//
//
//    public static final Long DEFAULT_VG_ID = 1L;
//    @Autowired
//    private DubboBindingServiceImpl dubboBindingService;
//
//    private Long serviceId = 0L;
//
//    private DubboBindingDto dubboBindingDto = new DubboBindingDto();
//
//    @Before
//    public void setUp() {
//        super.init();
//        MockitoAnnotations.openMocks(this);
//        Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(envoyRouteRuleProxyService.publishRoute(Mockito.any(), Mockito.any())).thenReturn(true);
//        Mockito.when(envoyRouteRuleProxyService.deleteRoute(Mockito.any())).thenReturn(true);
//        //创建服务
//        serviceDto.setServiceType("dubbo");
//        long serviceId = serviceInfoService.create(serviceDto);
//        serviceDto.setId(serviceId);
//        routeRuleDto.setServiceId(serviceId);
//        //创建路由
//        long routeRuleId = routeRuleInfoService.create(routeRuleDto);
//        routeRuleDto.setId(routeRuleId);
//
//        //发布服务
//        serviceProxyDto.setServiceId(serviceId);
//        serviceProxyDto.setPublishType(BaseConst.DYNAMIC_PUBLISH_TYPE);
//        serviceProxyDto.setRegistryCenterType(RegistryCenterEnum.Zookeeper.getType());
//        long serviceProxyId = serviceProxyService.create(serviceProxyDto);
//        serviceProxyDto.setId(serviceProxyId);
//
//        //发布路由
//        routeDto.setServiceId(serviceId);
//        routeDto.setRouteRuleId(routeRuleId);
//        long routeProxyId = routeRuleProxyService.create(routeDto);
//        routeDto.setId(routeProxyId);
//
//        dubboBindingDto.setObjectType(BaseConst.ROUTE);
//        dubboBindingDto.setObjectId(routeProxyId);
//        //dubboBindingDto.setDubboAttachment();
//        dubboBindingDto.setGroup("group-a");
//        dubboBindingDto.setCustomParamMapping(false);
//        dubboBindingDto.setMethod("echoStr()");
//        DubboBindingDto.DubboParam param = new DubboBindingDto.DubboParam();
//        param.setDefaultValue(0);
//        param.setKey("str");
//        param.setRequired(false);
//        param.setValue("111");
//        dubboBindingDto.setParams(Lists.newArrayList(param));
//        dubboBindingDto.setInterfaceName("com.netease.apigateway.dubbo.api.GatewayEchoService");
//        dubboBindingDto.setVersion("0.0.0");
//        dubboBindingDto.setMethodWorks(true);
//    }
//
//    @After
//    public void after() throws Exception {
//        //下线路由
//        routeRuleProxyService.delete(routeDto);
//        //删除路由
//        routeRuleInfoService.delete(routeRuleDto);
//        //下线服务
//        serviceProxyService.delete(serviceProxyDto);
//        //删除服务
//        serviceInfoService.delete(serviceDto);
//    }
//
//    @Test
//    public void create() {
//        assertTrue(dubboBindingService.create(dubboBindingDto) > NumberUtils.INTEGER_ZERO);
//    }
//
//    @Test
//    public void update() {
//        dubboBindingService.create(dubboBindingDto);
//        assertTrue(dubboBindingService.update(dubboBindingDto) > NumberUtils.INTEGER_ZERO);
//    }
//
//    @Test
//    public void delete() {
//        long l = dubboBindingService.create(dubboBindingDto);
//        dubboBindingService.delete(dubboBindingDto);
//        assertNull(dubboBindingService.get(l));
//    }
//
//    @Test
//    public void deleteDubboInfo() {
//        long l = dubboBindingService.create(dubboBindingDto);
//        dubboBindingService.deleteDubboInfo(dubboBindingDto.getObjectId(), dubboBindingDto.getObjectType());
//        assertNull(dubboBindingService.get(l));
//    }
//
//    @Test
//    public void get() {
//        long l = dubboBindingService.create(dubboBindingDto);
//        assertNotNull(dubboBindingService.get(l));
//
//    }
//
//    @Test
//    public void getByIdAndType() {
//        dubboBindingService.create(dubboBindingDto);
//        assertNotNull(dubboBindingService.getByIdAndType(dubboBindingDto.getObjectId(), dubboBindingDto.getObjectType()));
//    }
//
//    @Test
//    public void processMethodWorks() {
//        dubboBindingService.processMethodWorks(dubboBindingDto);
//        assertTrue(!dubboBindingDto.getMethodWorks());
//    }
//
//    @Test
//    public void checkAndComplete() {
//        ErrorCode errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
//        assertTrue(CommonErrorCode.SUCCESS.equals(errorCode));
//    }
//}