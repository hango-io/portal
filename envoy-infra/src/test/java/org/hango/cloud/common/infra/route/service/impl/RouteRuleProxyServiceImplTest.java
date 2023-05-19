//package org.hango.cloud.common.infra.route.service.impl;
//
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import org.hango.cloud.BaseServiceImplTest;
//import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCodeEnum;
//import org.hango.cloud.common.infra.route.dao.RouteMapper;
//import org.hango.cloud.common.infra.route.dto.DestinationDto;
//import org.hango.cloud.common.infra.route.dto.RouteQueryDto;
//import org.hango.cloud.common.infra.route.pojo.RouteQuery;
//import org.hango.cloud.common.infra.route.dto.RouteDto;
//import org.hango.cloud.common.infra.route.dto.RouteMirrorDto;
//import org.hango.cloud.common.infra.route.dto.RouteSyncDto;
//import org.hango.cloud.common.infra.route.dto.SyncRouteGwDto;
//import org.hango.cloud.common.infra.route.pojo.RoutePO;
//import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
//import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
//import org.hango.cloud.common.infra.serviceproxy.service.impl.ServiceProxyServiceImpl;
//import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
//import org.hango.cloud.envoy.infra.route.service.impl.EnvoyRouteServiceImpl;
//import org.hango.cloud.envoy.infra.serviceproxy.service.impl.EnvoyServiceProxyServiceImpl;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.jupiter.api.Assertions;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * @Author zhufengwei
// * @Date 2023/1/31
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class RouteRuleProxyServiceImplTest extends BaseServiceImplTest{
//
//    @Autowired
//    RouteServiceImpl routeRuleProxyService;
//
//    @Autowired
//    CopyRouteImpl copyRouteRuleProxy;
//
//    @Autowired
//    SyncRouteProxyServiceImpl syncRouteProxyService;
//
//    @Autowired
//    ServiceProxyServiceImpl serviceProxyService;
//
//    @Autowired
//    RouteMapper routeMapper;
//
//    @MockBean
//    EnvoyRouteServiceImpl envoyRouteRuleProxyService;
//
//    @MockBean
//    EnvoyServiceProxyServiceImpl envoyServiceProxyService;
//
//    @MockBean
//    IVirtualGatewayInfoService virtualGatewayInfoService;
//
//    private ServiceProxyDto targetServiceProxyDto;
//
//
//    @Before
//    public void init(){
//        super.init();
//        mock();
//        long serviceId = serviceInfoService.create(serviceDto);
//        serviceDto.setId(serviceId);
//        routeRuleDto.setServiceId(serviceId);
//        serviceProxyDto.setServiceId(serviceId);
//        routeDto.setServiceId(serviceId);
//        routeDto.getDestinationServices().get(0).setServiceId(serviceId);
//
//
//        long routeId = routeRuleInfoService.create(routeRuleDto);
//        routeRuleDto.setId(routeId);
//        routeDto.setId(routeId);
//
//        long serviceProxyId = serviceProxyService.create(serviceProxyDto);
//        serviceProxyDto.setId(serviceProxyId);
//
//        routeDto.setMirrorSwitch(1);
//        DestinationDto destinationDto = new DestinationDto();
//        destinationDto.setServiceId(serviceId);
//        destinationDto.setPort(80);
//        destinationDto.setMirrorType("application");
//        routeDto.setMirrorTraffic(destinationDto);
//        long routeProxyId = routeRuleProxyService.create(routeDto);
//        routeDto.setId(routeProxyId);
//
//        targetServiceProxyDto = serviceProxyService.get(serviceProxyDto.getId());
//        targetServiceProxyDto.setBackendService("e2e");
//        targetServiceProxyDto.setVirtualGwId(2L);
//        targetServiceProxyDto.setId(0L);
//        long id = serviceProxyService.create(targetServiceProxyDto);
//        targetServiceProxyDto.setId(id);
//
//    }
//
//    private void mock(){
//        MockitoAnnotations.openMocks(this);
//        Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(envoyRouteRuleProxyService.publishRoute(Mockito.any(), Mockito.any())).thenReturn(true);
//        Mockito.when(envoyRouteRuleProxyService.deleteRoute(Mockito.any())).thenReturn(true);
//        Mockito.when(envoyServiceProxyService.updateToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(virtualGatewayInfoService.get(1L)).thenReturn(virtualGatewayDto);
//
//    }
//
//    @After
//    public void clear(){
//        routeDto.setVirtualGwId(null);
//        routeRuleProxyService.delete(routeDto);
//        routeRuleInfoService.delete(routeRuleDto);
//        serviceProxyService.delete(targetServiceProxyDto);
//        serviceProxyService.delete(serviceProxyDto);
//        serviceInfoService.delete(serviceDto);
//    }
//
//
//    @Test
//    public void publishMirrorTraffic() {
//        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
//        routeMirrorDto.setMirrorSwitch(0);
//        routeMirrorDto.setRouteId(routeDto.getId());
//        routeMirrorDto.setVirtualGwId(routeDto.getVirtualGwId());
//        routeRuleProxyService.publishMirrorTraffic(routeMirrorDto);
//        assertEquals(0, routeRuleProxyService.get(routeDto.getId()).getMirrorSwitch());
//    }
//
//
//
//    @Test
//    public void getRouteRuleProxyPage() {
//        RouteQueryDto query = RouteQueryDto.builder().routeRuleIds(Collections.singletonList(routeRuleDto.getId())).build();
//        Page<RoutePO> page = routeRuleProxyService.getRoutePage(query);
//        assertEquals(1, page.getTotal());
//        assertEquals(1, page.getCurrent());
//        assertEquals(routeRuleDto.getId(), page.getRecords().get(0).getId());
//    }
//
//    @Test
//    public void getRouteRuleProxyList() {
//        RouteQuery query = RouteQuery.builder().routeIds(Collections.singletonList(routeDto.getId())).build();
//        List<RouteDto> routeRuleProxyList = routeRuleProxyService.getRouteList(query);
//        assertEquals(1, routeRuleProxyList.size());
//        assertEquals(ROUTE_NAME, routeRuleProxyList.get(0).getName());
//    }
//
//    @Test
//    public void getRouteRuleProxy() {
//        RouteDto routeRuleProxy = routeRuleProxyService.getRoute(routeDto.getVirtualGwId(), routeDto.getId());
//        assertNotNull(routeRuleProxy);
//        assertEquals(ROUTE_NAME, routeRuleProxy.getName());
//    }
//
//    @Test
//    public void getRouteRuleProxyByRouteRuleId() {
//        List<RouteDto> routeDtos = routeRuleProxyService.getRouteById(routeDto.getId());
//        assertEquals(ROUTE_NAME, routeDtos.get(0).getName());
//    }
//
//
//    @Test
//    public void checkDeleteParam() {
//        long routeRuleId = routeDto.getId();
//        routeDto.setId(99L);
//        ErrorCode errorCode = routeRuleProxyService.checkDeleteParam(routeDto);
//        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED.code);
//
//        routeDto.setId(routeRuleId);
//        routeDto.setExtension(Collections.singletonList(99L));
//        errorCode = routeRuleProxyService.checkDeleteParam(routeDto);
//        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_SERVICE_NOT_MATCH.code);
//    }
//
//
//    @Test
//    public void checkCreateParam() {
//        long serviceId = routeDto.getServiceId();
//        Long virtualGwId = routeDto.getVirtualGwId();
//        long routeRuleId = routeDto.getId();
//        routeDto.setServiceId(99L);
//        routeDto.setVirtualGwId(99L);
//        routeDto.setId(99L);
//        ErrorCode errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, CommonErrorCode.NO_SUCH_GATEWAY.code);
//        routeDto.setVirtualGwId(virtualGwId);
//
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY.code);
//        virtualGatewayDto.setProjectIdList(Arrays.asList(1L));
//
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, CommonErrorCode.SERVICE_NOT_PUBLISHED.code);
//
//        routeDto.setServiceId(serviceId);
//        DestinationDto destinationDto = routeDto.getDestinationServices().get(0);
//        destinationDto.setServiceId(99L);
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_SERVICE.getCode());
//        destinationDto.setServiceId(serviceId);
//
//        destinationDto.setWeight(101);
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_PARAMETER_VALUE.getCode());
//
//        destinationDto.setWeight(50);
//
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_TOTAL_WEIGHT.getCode());
//        destinationDto.setWeight(100);
//
//
//        routeDto.setDestinationServices(null);
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.MISSING_PARAMETER.getCode());
//        routeDto.setDestinationServices(Collections.singletonList(destinationDto));
//
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_DOMAIN.getCode());
//
//        routeDto.setId(routeRuleId);
//        errorCode = routeRuleProxyService.checkCreateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.ROUTE_RULE_ALREADY_PUBLISHED_TO_GW.getCode());
//    }
//
//    @Test
//    public void checkUpdateParam() {
//        long routeRuleId = routeDto.getId();
//        routeDto.setId(99L);
//        ErrorCode errorCode = routeRuleProxyService.checkUpdateParam(routeDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_ROUTE_RULE.getCode());
//        routeDto.setId(routeRuleId);
//    }
//
//    @Test
//    public void checkUpdateMirrorTrafficParam() {
//        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
//        routeMirrorDto.setVirtualGwId(99L);
//        routeMirrorDto.setRouteId(99L);
//
//        ErrorCode errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_GATEWAY.getCode());
//        routeMirrorDto.setVirtualGwId(routeDto.getVirtualGwId());
//
//        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_ROUTE_RULE.getCode());
//        routeMirrorDto.setRouteId(routeDto.getId());
//
//        routeMirrorDto.setMirrorSwitch(1);
//        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.MISSING_PARAMETER.getCode());
//
//        DestinationDto destinationDto = new DestinationDto();
//        routeMirrorDto.setMirrorTraffic(destinationDto);
//        destinationDto.setPort(65537);
//        routeMirrorDto.setMirrorTraffic(destinationDto);
//        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_PARAMETER_VALUE.getCode());
//        destinationDto.setPort(80);
//
//        destinationDto.setServiceId(99);
//        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.SERVICE_NOT_PUBLISHED.getCode());
//        destinationDto.setServiceId(routeDto.getServiceId());
//
//        destinationDto.setSubsetName("error-name");
//        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
//        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_SUBSET_NAME.getCode());
//    }
//
//
//    @Test
//    public void fillRouteRuleProxy(){
//        RouteDto testDto = routeRuleProxyService.get(routeDto.getId());
//        testDto.setUriMatchDto(null);
//        routeRuleProxyService.fillRouteInfo(testDto);
//        assertNotNull(testDto);
//    };
//
//
//
//    @Test
//    public void checkCopyRouteRuleProxy() {
//        Long routeId = routeRuleDto.getId();
//        ErrorCode errorCode = copyRouteRuleProxy.checkCopyRoute(routeId, 0, targetServiceProxyDto.getVirtualGwId());
//        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED.code);
//
//        errorCode = copyRouteRuleProxy.checkCopyRoute(routeId, routeDto.getVirtualGwId(), 3L);
//        assertEquals(errorCode.code, CommonErrorCode.SERVICE_NOT_PUBLISHED.code);
//
//        errorCode = copyRouteRuleProxy.checkCopyRoute(routeId, routeDto.getVirtualGwId(), targetServiceProxyDto.getVirtualGwId());
//        assertEquals(errorCode.code, CommonErrorCode.BACKEND_SERVICE_DIFFERENT.code);
//
//        targetServiceProxyDto.setBackendService(serviceProxyDto.getBackendService());
//        serviceProxyService.update(targetServiceProxyDto);
//
//        errorCode = copyRouteRuleProxy.checkCopyRoute(routeId, routeDto.getVirtualGwId(), targetServiceProxyDto.getVirtualGwId());
//        assertEquals(errorCode.code, CommonErrorCode.SUCCESS.code);
//    }
//
//    @Test
//    public void copyRouteRuleProxy() {
//        long routeRuleId = routeDto.getId();
//        Long originGwId = routeDto.getVirtualGwId();
//        Long desVgId = targetServiceProxyDto.getVirtualGwId();
//        boolean res = copyRouteRuleProxy.copyRoute(routeRuleId, originGwId, desVgId);
//        assertTrue(res);
//        RouteQuery query = RouteQuery.builder().routeIds(Collections.singletonList(routeRuleId)).virtualGwId(desVgId).build();
//        List<RouteDto> routeRuleList = routeRuleProxyService.getRouteList(query);
//        assertEquals(1, routeRuleList.size());
//        assertEquals(desVgId, routeRuleList.get(0).getVirtualGwId());
//        routeMapper.deleteById(routeRuleList.get(0).getId());
//    }
//
//    @Test
//    public void syncRouteProxy() {
//        RouteSyncDto syncDto = new RouteSyncDto();
//        syncDto.setRouteRuleId(routeRuleDto.getId());
//        syncDto.setVirtualGwIds(Collections.singletonList(routeDto.getVirtualGwId()));
//        List<String> errorNames = syncRouteProxyService.syncRouteProxy(syncDto);
//        assertEquals(0, errorNames.size());
//    }
//
//    @Test
//    public void checkSyncRouteProxy() {
//        RouteSyncDto syncDto = new RouteSyncDto();
//        syncDto.setRouteRuleId(99L);
//        ErrorCode errorCode = syncRouteProxyService.checkSyncRouteProxy(syncDto);
//        assertEquals(errorCode.code, CommonErrorCode.NO_SUCH_ROUTE_RULE.code);
//
//    }
//
//    @Test
//    public void describeGatewayForSyncRule() {
//        List<SyncRouteGwDto> syncRouteGwDtos = syncRouteProxyService.describeGatewayForSyncRule(routeRuleDto.getId());
//        long virtualGwId = syncRouteGwDtos.get(0).getVirtualGwId();
//        assertEquals(virtualGwId, 1L);
//        Assertions.assertTrue(syncRouteGwDtos.get(0).getIsSameRaw());
//    }
//}