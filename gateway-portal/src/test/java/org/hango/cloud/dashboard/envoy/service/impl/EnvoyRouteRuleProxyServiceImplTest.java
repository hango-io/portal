package org.hango.cloud.dashboard.envoy.service.impl;

import com.google.common.collect.Lists;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.RouteRuleProxyServiceImpl;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyDestinationDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleMapMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteStringMatchDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.HttpRetryDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleDto;
import org.hango.cloud.dashboard.envoy.web.dto.RouteRuleProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
import org.hango.cloud.dashboard.envoy.web.dto.SyncRouteRuleGwDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnvoyRouteRuleProxyServiceImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyRouteRuleProxyServiceImplTest.class);

    @Autowired
    public IServiceInfoService serviceInfoService;
    @Autowired
    @InjectMocks
    public EnvoyServiceProxyServiceImpl serviceProxyService;
    @Autowired
    public IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    public IGatewayInfoService gatewayInfoService;
    @Autowired
    @InjectMocks
    public RouteRuleProxyServiceImpl routeRuleProxyService;
    public ServiceInfo serviceInfo;
    public GatewayInfo gatewayInfo;
    public ServiceProxyDto serviceProxyDto;
    public RouteRuleDto routeRuleDto;
    public RouteRuleProxyDto routeRuleProxyDto = new RouteRuleProxyDto();
    public RouteRuleProxyDto syncRouteProxyDto = new RouteRuleProxyDto();
    public long gwId;
    public long serviceId;
    public long routeId;
    @Autowired
    @InjectMocks
    private SyncRouteProxyServiceImpl syncRouteProxyService;
    @MockBean
    private GetFromApiPlaneServiceImpl getFromApiPlaneService;

    private EnvoyDestinationDto envoyDestinationDto;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(getFromApiPlaneService.publishServiceByApiPlane(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.doReturn(true).when(getFromApiPlaneService).offlineServiceByApiPlane(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(getFromApiPlaneService).publishRouteRuleByApiPlane(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(getFromApiPlaneService).deleteRouteRuleByApiPlane(Mockito.any());

        routeRuleDto = new RouteRuleDto();

        //初始化ServiceInfo
        serviceInfo = new ServiceInfo();
        serviceInfo.setDisplayName(displayName);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setContacts(user);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setServiceType(serviceType);
        //创建service
        serviceId = serviceInfoService.add(serviceInfo);

        gatewayInfo = gatewayInfoService.getGatewayByName(envoyGwName);
        gwId = gatewayInfo.getId();

        serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setServiceId(serviceId);
        serviceProxyDto.setBackendService("a.pilot-test.svc.cluster.local");
        serviceProxyDto.setGwId(gwId);
        serviceProxyDto.setPublishType(Const.DYNAMIC_PUBLISH_TYPE);
        serviceProxyDto.setRegistryCenterType("Kubernetes");
        EnvoySubsetDto subsetDto = new EnvoySubsetDto();
        subsetDto.setName("testSubset");
        Map<String, String> label = new HashMap<>();
        label.put("aaa", "bbb");
        subsetDto.setLabels(label);
        serviceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{subsetDto}));

        serviceProxyService.publishServiceToGw(serviceProxyDto);

        //构造路由规则
        routeRuleDto.setServiceId(serviceId);
        routeRuleDto.setRouteRuleName(routeName);
        routeRuleDto.setDescription(description);
        EnvoyRouteRuleMapMatchDto headers = new EnvoyRouteRuleMapMatchDto();
        headers.setKey("abc");
        headers.setType(Const.URI_TYPE_EXACT);
        headers.setValue(Arrays.asList(new String[]{"abc"}));
        routeRuleDto.setHeaders(Arrays.asList(new EnvoyRouteRuleMapMatchDto[]{headers}));

        EnvoyRouteRuleMapMatchDto querys = new EnvoyRouteRuleMapMatchDto();
        querys.setKey("aaa");
        querys.setType(Const.URI_TYPE_EXACT);
        querys.setValue(Arrays.asList(new String[]{"caa"}));
        routeRuleDto.setQueryParams(Arrays.asList(new EnvoyRouteRuleMapMatchDto[]{querys}));

        EnvoyRouteStringMatchDto host = new EnvoyRouteStringMatchDto();
        host.setType("exact");
        host.setValue(Arrays.asList(new String[]{"abc.com"}));
        routeRuleDto.setHostMatchDto(host);

        EnvoyRouteStringMatchDto method = new EnvoyRouteStringMatchDto();
        method.setType("exact");
        method.setValue(Arrays.asList(new String[]{"GET"}));
        routeRuleDto.setMethodMatchDto(method);

        EnvoyRouteStringMatchDto uri = new EnvoyRouteStringMatchDto();
        uri.setType("exact");
        uri.setValue(Arrays.asList(new String[]{"/abc"}));
        routeRuleDto.setUriMatchDto(uri);

        routeRuleDto.setPriority(50);
        RouteRuleInfo routeRuleInfo = routeRuleDto.toMeta();
        routeRuleInfo.setProjectId(projectId);

        routeId = routeRuleInfoService.addRouteRule(routeRuleInfo);

        routeRuleProxyDto.setRouteRuleId(routeId);
        routeRuleProxyDto.setServiceId(serviceId);
        routeRuleProxyDto.setGwIds(Arrays.asList(new Long[]{gwId}));
        routeRuleProxyDto.setGwId(gwId);
        routeRuleProxyDto.setTimeout(60000);
        routeRuleProxyDto.setEnableState(Const.ROUTE_RULE_ENABLE_STATE);

        envoyDestinationDto = new EnvoyDestinationDto();
        envoyDestinationDto.setPort(80);
        envoyDestinationDto.setServiceId(serviceId);
        envoyDestinationDto.setApplicationName("a.powerful-v13.svc.cluster.local");
        envoyDestinationDto.setSubsetName("testSubset");
        envoyDestinationDto.setWeight(100);
        routeRuleProxyDto.setDestinationServices(Arrays.asList(new EnvoyDestinationDto[]{envoyDestinationDto}));
        HttpRetryDto httpRetryDto = new HttpRetryDto();
        httpRetryDto.setAttempts(2);
        httpRetryDto.setPerTryTimeout(60000);
        httpRetryDto.setRetryOn("5xx");
        routeRuleProxyDto.setHttpRetryDto(httpRetryDto);

        syncRouteProxyDto.setRouteRuleId(routeId);
        syncRouteProxyDto.setServiceId(serviceId);
        syncRouteProxyDto.setGwIds(Arrays.asList(new Long[]{gwId}));
        syncRouteProxyDto.setPriority(55);
        EnvoyRouteStringMatchDto uriSync = new EnvoyRouteStringMatchDto();
        uriSync.setType("prefix");
        uriSync.setValue(Arrays.asList(new String[]{"/aaa"}));
        syncRouteProxyDto.setUriMatchDto(uriSync);
        EnvoyRouteStringMatchDto hostSync = new EnvoyRouteStringMatchDto();
        hostSync.setType("exact");
        hostSync.setValue(Arrays.asList(new String[]{"sync.com"}));
        syncRouteProxyDto.setHostMatchDto(hostSync);

    }

    @After
    public void tearDownClass() {
        logger.info("tear down class .... ServiceProxyServiceImplTest");
        //清除service
        serviceProxyService.deleteServiceProxy(gwId, serviceId);
        serviceInfoService.delete(serviceId);
        routeRuleInfoService.deleteRouteRule(routeId);
    }

    @Test
    public void checkPublishParam() {
        ErrorCode errorCode = routeRuleProxyService.checkPublishParam(routeRuleProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));

        EnvoyDestinationDto envoyStaticDestinationDto = new EnvoyDestinationDto();
        envoyStaticDestinationDto.setServiceId(serviceId);
        envoyStaticDestinationDto.setSubsetName("testSubset1");
        envoyStaticDestinationDto.setWeight(100);
        routeRuleProxyDto.setDestinationServices(Arrays.asList(new EnvoyDestinationDto[]{envoyStaticDestinationDto}));
        errorCode = routeRuleProxyService.checkPublishParam(routeRuleProxyDto);
        assertEquals(CommonErrorCode.InvalidSubsetName.getCode(), errorCode.getCode());
    }

    @Test
    public void checkUpdateParam() {
        routeRuleProxyDto.setTimeout(70000);
        ErrorCode errorCode = routeRuleProxyService.checkUpdateParam(routeRuleProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }

    @Test
    @Rollback
    public void publishRouteRule() {
        routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRuleProxyDto), Lists.newArrayList(), true);
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(gwId, routeId);
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.fromMeta(routeRuleProxy);
        assertTrue(routeRuleProxyDto.getUriMatchDto().getValue().contains("/abc"));
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }

    @Test
    @Rollback
    public void publishRouteRuleBatch() {
        List<String> strings = routeRuleProxyService.publishRouteRuleBatch(Arrays.asList(new Long[]{gwId}), routeRuleProxyDto);
        assertTrue(CollectionUtils.isEmpty(strings));
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }

    @Test
    @Rollback
    public void addRouteRuleProxy() {
        long id = routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        assertTrue(id > 0);
    }

    @Test
    @Rollback
    public void getRouteRuleProxyList() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        List<RouteRuleProxyInfo> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyList(serviceId);
        assertTrue(routeRuleProxyList.size() > 0);
    }

    @Test
    @Rollback
    public void getRouteRuleProxyList1() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        List<RouteRuleProxyInfo> routeRuleProxyList = routeRuleProxyService.getRouteRuleProxyList(gwId,
                serviceId, "",
                "create_time", "desc", 0, 100);
        assertTrue(routeRuleProxyList.size() > 0);
    }

    @Test
    @Rollback
    public void getRouteRuleProxyCountByService() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        long count = routeRuleProxyService.getRouteRuleProxyCountByService(gwId, serviceId);
        assertTrue(count > 0);
    }

//    @Test
//    @Rollback
//    public void getRouteRuleProxyCount() {
//        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(envoyRouteRuleProxyDto));
//        long count = routeRuleProxyService.getRouteRuleProxyCount(gwId, routeId);
//        assertTrue(count > 0);
//    }

    @Test
    @Rollback
    public void getRouteRuleProxy() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        RouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(gwId, routeId);
        assertTrue(routeRuleProxy.getServiceId() == serviceId);
    }

    @Test
    @Rollback
    public void deleteRouteRuleProxy() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }

    @Test
    @Rollback
    public void checkDeleteRouteRuleProxy() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        ErrorCode errorCode = routeRuleProxyService.checkDeleteRouteRuleProxy(gwId, routeId, Lists.newArrayList());
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }

    @Test
    @Rollback
    public void checkUpdateEnableState() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        ErrorCode errorCode = routeRuleProxyService.checkUpdateEnableState(gwId, routeId, Const.ROUTE_RULE_ENABLE_STATE);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }

    @Test
    @Rollback
    public void updateEnableState() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        long count = routeRuleProxyService.updateEnableState(gwId, routeId, Const.ROUTE_RULE_DISABLE_STATE);
        assertTrue(count > 0);
    }

//    @Test
//    @Rollback
//    public void updateEnvoyRouteRuleProxy() {
//        EnvoyRouteRuleProxyInfo envoyRouteRuleProxyInfo = routeRuleProxyService.toMeta(envoyRouteRuleProxyDto);
//        envoyRouteRuleProxyInfo.setProjectId(projectId);
//        routeRuleProxyService.addRouteRuleProxy(envoyRouteRuleProxyInfo);
//        EnvoyRouteRuleProxyInfo routeRuleProxy = routeRuleProxyService.getRouteRuleProxy(gwId, serviceId);
//        routeRuleProxy.setProjectId(projectId);
//        long count = routeRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxy);
//        assertTrue(count > 0);
//        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
//    }


    @Test
    @Rollback
    public void getRouteRuleProxyByRouteRuleId() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        List<RouteRuleProxyInfo> routeRuleProxyByRouteRuleId = routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeId);
        assertTrue(routeRuleProxyByRouteRuleId.size() > 0);
    }

    @Test
    @Rollback
    public void fromMeta() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(this.routeRuleProxyDto));
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.fromMeta(routeRuleProxyService.getRouteRuleProxy(gwId, routeId));
        assertTrue(routeRuleProxyDto.getRouteRuleId() == routeId);
    }


    @Test
    @Rollback
    public void getRouteRuleProxyListByServiceId() {
        routeRuleProxyService.addRouteRuleProxy(routeRuleProxyService.toMeta(routeRuleProxyDto));
        List<RouteRuleProxyInfo> routeList = routeRuleProxyService.getRouteRuleProxyListByServiceId(gwId, serviceId);
        assertTrue(routeList.size() > 0);
    }


    @Test
    @Rollback
    public void checkSyncRouteProxy() {
        routeRuleProxyDto.setEnableState(Const.ROUTE_RULE_DISABLE_STATE);
        routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRuleProxyDto), Lists.newArrayList(), true);
        ErrorCode errorCode = syncRouteProxyService.checkSyncRouteProxy(syncRouteProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }

    @Test
    @Rollback
    public void syncRouteRuleBatch() {
        routeRuleProxyDto.setEnableState(Const.ROUTE_RULE_DISABLE_STATE);
        routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRuleProxyDto), Lists.newArrayList(), true);
        List<String> strings = syncRouteProxyService.syncRouteRuleBatch(syncRouteProxyDto.getGwIds(), syncRouteProxyDto);
        assertTrue(CollectionUtils.isEmpty(strings));
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }

    @Test
    @Rollback
    public void describeGatewayForSyncRule() {
        routeRuleProxyDto.setEnableState(Const.ROUTE_RULE_DISABLE_STATE);
        routeRuleProxyService.publishRouteRule(routeRuleProxyService.toMeta(routeRuleProxyDto), Lists.newArrayList(), true);
        List<SyncRouteRuleGwDto> syncRouteRuleGwDtos = syncRouteProxyService.describeGatewayForSyncRule(routeId);
        assertTrue(syncRouteRuleGwDtos.size() > 0);
        assertTrue(syncRouteRuleGwDtos.get(0).getSameRaw());
        assertTrue(routeRuleProxyService.deleteRouteRuleProxy(gwId, routeId));
    }
}