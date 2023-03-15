package org.hango.cloud.common.infra.routeproxy.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCodeEnum;
import org.hango.cloud.common.infra.base.mapper.RouteRuleProxyMapper;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.impl.RouteRuleInfoServiceImpl;
import org.hango.cloud.common.infra.routeproxy.dto.RouteMirrorDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteProxySyncDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.dto.SyncRouteRuleGwDto;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyPO;
import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.impl.ServiceProxyServiceImpl;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.routeproxy.service.impl.EnvoyRouteRuleProxyServiceImpl;
import org.hango.cloud.envoy.infra.serviceproxy.service.impl.EnvoyServiceProxyServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.DYNAMIC_PUBLISH_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author zhufengwei
 * @Date 2023/1/31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RouteRuleProxyServiceImplTest extends BaseServiceImplTest{

    @Autowired
    RouteRuleProxyServiceImpl routeRuleProxyService;

    @Autowired
    CopyRouteRuleProxyImpl copyRouteRuleProxy;

    @Autowired
    SyncRouteProxyServiceImpl syncRouteProxyService;

    @Autowired // 定义被测试类的对象
    RouteRuleInfoServiceImpl routeRuleInfoService;
    @Autowired // service测试类
    ServiceInfoServiceImpl serviceInfoService;
    @Autowired
    ServiceProxyServiceImpl serviceProxyService;

    @Autowired
    RouteRuleProxyMapper routeRuleProxyMapper;

    @MockBean
    EnvoyRouteRuleProxyServiceImpl envoyRouteRuleProxyService;

    @MockBean
    EnvoyServiceProxyServiceImpl envoyServiceProxyService;

    @MockBean
    IVirtualGatewayInfoService virtualGatewayInfoService;

    private ServiceProxyDto targetServiceProxyDto;


    @Before
    public void init(){
        super.init();
        mock();
        long serviceId = serviceInfoService.create(serviceDto);
        serviceDto.setId(serviceId);
        routeRuleDto.setServiceId(serviceId);
        serviceProxyDto.setServiceId(serviceId);
        routeRuleProxyDto.setServiceId(serviceId);
        routeRuleProxyDto.getDestinationServices().get(0).setServiceId(serviceId);


        long routeId = routeRuleInfoService.create(routeRuleDto);
        routeRuleDto.setId(routeId);
        routeRuleProxyDto.setRouteRuleId(routeId);

        long serviceProxyId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceProxyId);

        routeRuleProxyDto.setMirrorSwitch(1);
        DestinationDto destinationDto = new DestinationDto();
        destinationDto.setServiceId(serviceId);
        destinationDto.setPort(80);
        destinationDto.setMirrorType("application");
        routeRuleProxyDto.setMirrorTraffic(destinationDto);
        long routeProxyId = routeRuleProxyService.create(routeRuleProxyDto);
        routeRuleProxyDto.setId(routeProxyId);

        targetServiceProxyDto = serviceProxyService.get(serviceProxyDto.getId());
        targetServiceProxyDto.setBackendService("e2e");
        targetServiceProxyDto.setVirtualGwId(2L);
        targetServiceProxyDto.setId(0L);
        long id = serviceProxyService.create(targetServiceProxyDto);
        targetServiceProxyDto.setId(id);

    }

    private void mock(){
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoyRouteRuleProxyService.publishRouteProxy(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(envoyRouteRuleProxyService.deleteRouteProxy(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.updateToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(virtualGatewayInfoService.get(1L)).thenReturn(virtualGatewayDto);

    }

    @After
    public void clear(){
        routeRuleProxyDto.setVirtualGwId(null);
        routeRuleProxyService.delete(routeRuleProxyDto);
        routeRuleInfoService.delete(routeRuleDto);
        serviceProxyService.delete(targetServiceProxyDto);
        serviceProxyService.delete(serviceProxyDto);
        serviceInfoService.delete(serviceDto);
    }


    @Test
    public void publishMirrorTraffic() {
        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
        routeMirrorDto.setMirrorSwitch(0);
        routeMirrorDto.setRouteRuleId(routeRuleProxyDto.getRouteRuleId());
        routeMirrorDto.setVirtualGwId(routeRuleProxyDto.getVirtualGwId());
        routeRuleProxyService.publishMirrorTraffic(routeMirrorDto);
        assertEquals(0, routeRuleProxyService.get(routeRuleProxyDto.getId()).getMirrorSwitch());
    }



    @Test
    public void getRouteRuleProxyPage() {
        RouteRuleQueryDto query = RouteRuleQueryDto.builder().routeRuleIds(Collections.singletonList(routeRuleDto.getId())).build();
        Page<RouteRuleProxyPO> page = routeRuleProxyService.getRouteRuleProxyPage(query);
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getCurrent());
        assertEquals(routeRuleDto.getId(), page.getRecords().get(0).getRouteRuleId());
    }

    @Test
    public void getRouteRuleProxyList() {
        RouteRuleQuery query = RouteRuleQuery.builder().routeRuleIds(Collections.singletonList(routeRuleProxyDto.getRouteRuleId())).build();
        List<RouteRuleProxyDto> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyList(query);
        assertEquals(1, routeRuleProxyList.size());
        assertEquals(ROUTE_NAME, routeRuleProxyList.get(0).getRouteRuleName());
    }

    @Test
    public void getRouteRuleProxy() {
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(routeRuleProxyDto.getVirtualGwId(), routeRuleProxyDto.getRouteRuleId());
        assertNotNull(routeRuleProxy);
        assertEquals(ROUTE_NAME, routeRuleProxy.getRouteRuleName());
    }

    @Test
    public void getRouteRuleProxyByRouteRuleId() {
        List<RouteRuleProxyDto> routeRuleProxyDtos = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeRuleProxyDto.getRouteRuleId());
        assertEquals(ROUTE_NAME, routeRuleProxyDtos.get(0).getRouteRuleName());
    }


    @Test
    public void checkDeleteParam() {
        long routeRuleId = routeRuleProxyDto.getRouteRuleId();
        routeRuleProxyDto.setRouteRuleId(99L);
        ErrorCode errorCode = routeRuleProxyService.checkDeleteParam(routeRuleProxyDto);
        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED.code);

        routeRuleProxyDto.setRouteRuleId(routeRuleId);
        routeRuleProxyDto.setExtension(Collections.singletonList(99L));
        errorCode = routeRuleProxyService.checkDeleteParam(routeRuleProxyDto);
        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_SERVICE_NOT_MATCH.code);
    }


    @Test
    public void checkCreateParam() {
        long serviceId = routeRuleProxyDto.getServiceId();
        Long virtualGwId = routeRuleProxyDto.getVirtualGwId();
        long routeRuleId = routeRuleProxyDto.getRouteRuleId();
        routeRuleProxyDto.setServiceId(99L);
        routeRuleProxyDto.setVirtualGwId(99L);
        routeRuleProxyDto.setRouteRuleId(99L);
        ErrorCode errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, CommonErrorCode.NO_SUCH_GATEWAY.code);
        routeRuleProxyDto.setVirtualGwId(virtualGwId);

        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY.code);
        virtualGatewayDto.setProjectIdList(Arrays.asList(1L));

        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, CommonErrorCode.SERVICE_NOT_PUBLISHED.code);

        routeRuleProxyDto.setServiceId(serviceId);
        DestinationDto destinationDto = routeRuleProxyDto.getDestinationServices().get(0);
        destinationDto.setServiceId(99L);
        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_SERVICE.getCode());
        destinationDto.setServiceId(serviceId);

        destinationDto.setWeight(101);
        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_PARAMETER_VALUE.getCode());

        destinationDto.setWeight(50);

        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_TOTAL_WEIGHT.getCode());
        destinationDto.setWeight(100);


        routeRuleProxyDto.setDestinationServices(null);
        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.MISSING_PARAMETER.getCode());
        routeRuleProxyDto.setDestinationServices(Collections.singletonList(destinationDto));

        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_DOMAIN.getCode());

        routeRuleProxyDto.setRouteRuleId(routeRuleId);
        errorCode = routeRuleProxyService.checkCreateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.ROUTE_RULE_ALREADY_PUBLISHED_TO_GW.getCode());
    }

    @Test
    public void checkUpdateParam() {
        long routeRuleId = routeRuleProxyDto.getRouteRuleId();
        routeRuleProxyDto.setRouteRuleId(99L);
        ErrorCode errorCode = routeRuleProxyService.checkUpdateParam(routeRuleProxyDto);
        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_ROUTE_RULE.getCode());
        routeRuleProxyDto.setRouteRuleId(routeRuleId);
    }

    @Test
    public void checkUpdateMirrorTrafficParam() {
        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
        routeMirrorDto.setVirtualGwId(99L);
        routeMirrorDto.setRouteRuleId(99L);

        ErrorCode errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_GATEWAY.getCode());
        routeMirrorDto.setVirtualGwId(routeRuleProxyDto.getVirtualGwId());

        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.NO_SUCH_ROUTE_RULE.getCode());
        routeMirrorDto.setRouteRuleId(routeRuleProxyDto.getRouteRuleId());

        routeMirrorDto.setMirrorSwitch(1);
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.MISSING_PARAMETER.getCode());

        DestinationDto destinationDto = new DestinationDto();
        routeMirrorDto.setMirrorTraffic(destinationDto);
        destinationDto.setPort(65537);
        routeMirrorDto.setMirrorTraffic(destinationDto);
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_PARAMETER_VALUE.getCode());
        destinationDto.setPort(80);

        destinationDto.setServiceId(99);
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.SERVICE_NOT_PUBLISHED.getCode());
        destinationDto.setServiceId(routeRuleProxyDto.getServiceId());

        destinationDto.setSubsetName("error-name");
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        assertEquals(errorCode.code, ErrorCodeEnum.INVALID_SUBSET_NAME.getCode());
    }


    @Test
    public void fillRouteRuleProxy(){
        RouteRuleProxyDto testDto = routeRuleProxyService.get(routeRuleProxyDto.getId());
        testDto.setUriMatchDto(null);
        routeRuleProxyService.fillRouteRuleProxy(testDto);
        assertNotNull(testDto);
    };



    @Test
    public void checkCopyRouteRuleProxy() {
        Long routeId = routeRuleDto.getId();
        ErrorCode errorCode = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeId, 0, targetServiceProxyDto.getVirtualGwId());
        assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED.code);

        errorCode = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeId, routeRuleProxyDto.getVirtualGwId(), 3L);
        assertEquals(errorCode.code, CommonErrorCode.SERVICE_NOT_PUBLISHED.code);

        errorCode = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeId, routeRuleProxyDto.getVirtualGwId(), targetServiceProxyDto.getVirtualGwId());
        assertEquals(errorCode.code, CommonErrorCode.BACKEND_SERVICE_DIFFERENT.code);

        targetServiceProxyDto.setBackendService(serviceProxyDto.getBackendService());
        serviceProxyService.update(targetServiceProxyDto);

        errorCode = copyRouteRuleProxy.checkCopyRouteRuleProxy(routeId, routeRuleProxyDto.getVirtualGwId(), targetServiceProxyDto.getVirtualGwId());
        assertEquals(errorCode.code, CommonErrorCode.SUCCESS.code);
    }

    @Test
    public void copyRouteRuleProxy() {
        long routeRuleId = routeRuleProxyDto.getRouteRuleId();
        Long originGwId = routeRuleProxyDto.getVirtualGwId();
        Long desVgId = targetServiceProxyDto.getVirtualGwId();
        boolean res = copyRouteRuleProxy.copyRouteRuleProxy(routeRuleId, originGwId, desVgId);
        assertTrue(res);
        RouteRuleQuery query = RouteRuleQuery.builder().routeRuleIds(Collections.singletonList(routeRuleId)).virtualGwId(desVgId).build();
        List<RouteRuleProxyDto> routeRuleList = routeRuleProxyService.getRouteRuleProxyList(query);
        assertEquals(1, routeRuleList.size());
        assertEquals(desVgId, routeRuleList.get(0).getVirtualGwId());
        routeRuleProxyMapper.deleteById(routeRuleList.get(0).getId());
    }

    @Test
    public void syncRouteProxy() {
        RouteProxySyncDto syncDto = new RouteProxySyncDto();
        syncDto.setRouteRuleId(routeRuleDto.getId());
        syncDto.setVirtualGwIds(Collections.singletonList(routeRuleProxyDto.getVirtualGwId()));
        List<String> errorNames = syncRouteProxyService.syncRouteProxy(syncDto);
        assertEquals(0, errorNames.size());
    }

    @Test
    public void checkSyncRouteProxy() {
        RouteProxySyncDto syncDto = new RouteProxySyncDto();
        syncDto.setRouteRuleId(99L);
        ErrorCode errorCode = syncRouteProxyService.checkSyncRouteProxy(syncDto);
        assertEquals(errorCode.code, CommonErrorCode.NO_SUCH_ROUTE_RULE.code);

    }

    @Test
    public void describeGatewayForSyncRule() {
        List<SyncRouteRuleGwDto> syncRouteRuleGwDtos = syncRouteProxyService.describeGatewayForSyncRule(routeRuleDto.getId());
        long virtualGwId = syncRouteRuleGwDtos.get(0).getVirtualGwId();
        assertEquals(virtualGwId, 1L);
        Assertions.assertTrue(syncRouteRuleGwDtos.get(0).getIsSameRaw());
    }
}