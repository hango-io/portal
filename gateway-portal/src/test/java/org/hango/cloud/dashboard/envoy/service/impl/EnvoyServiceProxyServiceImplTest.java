package org.hango.cloud.dashboard.envoy.service.impl;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyActiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyPassiveHealthCheckRuleDto;
import org.hango.cloud.dashboard.envoy.innerdto.EnvoyServiceWithPortDto;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.service.IGetFromApiPlaneService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceConnectionPoolDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceLoadBalancerDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceTrafficPolicyDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoySubsetDto;
import org.hango.cloud.dashboard.envoy.web.dto.ServiceProxyDto;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(HttpCommonUtil.class)
////@AutoConfigureMockMvc
//@PowerMockIgnore("javax.management.*")
//@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class EnvoyServiceProxyServiceImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyServiceImplTest.class);

    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    @InjectMocks
    private EnvoyServiceProxyServiceImpl serviceProxyService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @MockBean
    private IGetFromApiPlaneService getFromApiPlaneService;

    private ServiceInfo serviceInfo;
    private GatewayInfo gatewayInfo;
    private ServiceProxyDto serviceProxyDto;
    private EnvoySubsetDto subsetDto;
    private ServiceProxyDto envoyStaticServiceProxyDto;
    private EnvoySubsetDto staticSubsetDto;

    //负载均衡&连接池 相关类
    private EnvoyServiceTrafficPolicyDto envoyServiceTrafficPolicyDto;
    private EnvoyServiceLoadBalancerDto loadBalancer;
    private EnvoyActiveHealthCheckRuleDto activeHealthCheckRule;
    private EnvoyPassiveHealthCheckRuleDto passiveHealthCheckRule;
    private EnvoyServiceConnectionPoolDto connectionPoolDto;
    private EnvoyServiceConnectionPoolDto.EnvoyServiceTcpConnectionPoolDto serviceTcpConnectionPoolDto;
    private EnvoyServiceConnectionPoolDto.EnvoyServiceHttpConnectionPoolDto serviceHttpConnectionPoolDto;

    private long gwId;
    private long serviceId;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(getFromApiPlaneService.publishServiceByApiPlane(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.doReturn(true).when(getFromApiPlaneService).offlineServiceByApiPlane(Mockito.any(), Mockito.any());
//        Mockito.doReturn(Lists.newArrayList()).when(getFromApiPlaneService).getServiceListFromApiPlane(Mockito.any(),
//                Mockito.any(), Mockito.any(), Mockito.any());

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

        //k8s方式发布
        serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setServiceId(serviceId);
        serviceProxyDto.setBackendService("a.pilot-test.svc.cluster.local");
        serviceProxyDto.setGwId(gwId);
        serviceProxyDto.setPublishType(Const.DYNAMIC_PUBLISH_TYPE);
        serviceProxyDto.setRegistryCenterType("Kubernetes");
        //版本
        subsetDto = new EnvoySubsetDto();
        subsetDto.setName("testSubset");
        Map<String, String> label = new HashMap<>();
        label.put("aaa", "bbb");
        subsetDto.setLabels(label);
        serviceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{subsetDto}));

        //静态地址发布
        envoyStaticServiceProxyDto = new ServiceProxyDto();
        envoyStaticServiceProxyDto.setServiceId(serviceId);
        envoyStaticServiceProxyDto.setBackendService("127.0.0.1:8888,127.0.0.2:8888");
        envoyStaticServiceProxyDto.setGwId(gwId);
        envoyStaticServiceProxyDto.setPublishType(Const.STATIC_PUBLISH_TYPE);
        envoyStaticServiceProxyDto.setPublishProtocol("HTTP");
        //版本
        staticSubsetDto = new EnvoySubsetDto();
        staticSubsetDto.setName("testStaticSubset");
        List<String> staticAddrList = new ArrayList<>();
        staticAddrList.add("127.0.0.1:8888");
        staticSubsetDto.setStaticAddrList(staticAddrList);
        envoyStaticServiceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{staticSubsetDto}));

        //负载均衡&连接池 相关类
        loadBalancer = new EnvoyServiceLoadBalancerDto();
        loadBalancer.setType("Simple");
        loadBalancer.setSimple("ROUND_ROBIN");

        activeHealthCheckRule = new EnvoyActiveHealthCheckRuleDto();
        activeHealthCheckRule.setPath("/healthcheck");
        activeHealthCheckRule.setHealthyThreshold(1);
        activeHealthCheckRule.setUnhealthyThreshold(1);
        activeHealthCheckRule.setHealthyInterval(1);
        activeHealthCheckRule.setUnhealthyInterval(1);
        activeHealthCheckRule.setTimeout(10);
        List<Integer> status = new ArrayList<>();
        status.add(200);
        activeHealthCheckRule.setExpectedStatuses(status);

        passiveHealthCheckRule = new EnvoyPassiveHealthCheckRuleDto();
        passiveHealthCheckRule.setMaxEjectionPercent(10);
        passiveHealthCheckRule.setConsecutiveErrors(2);
        passiveHealthCheckRule.setBaseEjectionTime(10);

        connectionPoolDto = new EnvoyServiceConnectionPoolDto();
        serviceTcpConnectionPoolDto = new EnvoyServiceConnectionPoolDto.EnvoyServiceTcpConnectionPoolDto();
        serviceHttpConnectionPoolDto = new EnvoyServiceConnectionPoolDto.EnvoyServiceHttpConnectionPoolDto();
        connectionPoolDto.setServiceHttpConnectionPoolDto(serviceHttpConnectionPoolDto);
        connectionPoolDto.setServiceTcpConnectionPoolDto(serviceTcpConnectionPoolDto);
        serviceTcpConnectionPoolDto.setConnectTimeout(10);
        serviceTcpConnectionPoolDto.setMaxConnections(100);
        serviceHttpConnectionPoolDto.setMaxRequestsPerConnection(0);
        serviceHttpConnectionPoolDto.setHttp1MaxPendingRequests(1024);
        serviceHttpConnectionPoolDto.setHttp2MaxRequests(1024);
        serviceHttpConnectionPoolDto.setIdleTimeout(1000);

        envoyServiceTrafficPolicyDto = new EnvoyServiceTrafficPolicyDto();
        envoyServiceTrafficPolicyDto.setLoadBalancer(loadBalancer);
        envoyServiceTrafficPolicyDto.setActiveHealthCheckRule(activeHealthCheckRule);
        envoyServiceTrafficPolicyDto.setPassiveHealthCheckRule(passiveHealthCheckRule);
        envoyServiceTrafficPolicyDto.setConnectionPoolDto(connectionPoolDto);
        serviceProxyDto.setTrafficPolicy(envoyServiceTrafficPolicyDto);
    }

    @After
    public void tearDownClass() {
        logger.info("tear down class .... ServiceProxyServiceImplTest");
        //清除service
        serviceInfoService.delete(serviceId);
    }


    @Test
    public void getServiceListFromApiPlane() {
        List<EnvoyServiceWithPortDto> serviceListFromApiPlane = getFromApiPlaneService.getServiceListFromApiPlane(gwId,
                "", "Kubernetes", "", Collections.EMPTY_MAP);
        assertTrue(serviceListFromApiPlane.size() == 0);

        serviceListFromApiPlane = getFromApiPlaneService.getServiceListFromApiPlane(gwId,
                "", "Nacos", "", Collections.EMPTY_MAP);
        assertTrue(serviceListFromApiPlane.size() == 0);
    }

    /**
     * 校验服务和版本 负载均衡策略 & 连接池 且 根据Type字段将冗余字段置空不处理
     */
    @Test
    public void checkEnvoyServiceProxyDto() {
        ErrorCode errorCode = serviceProxyService.checkEnvoyServiceProxyDto(serviceProxyDto);
        assertEquals(CommonErrorCode.Success.getCode(), errorCode.getCode());

        //校验静态发布
        errorCode = serviceProxyService.checkEnvoyServiceProxyDto(envoyStaticServiceProxyDto);
        assertEquals(CommonErrorCode.Success.getCode(), errorCode.getCode());

        //静态发布其版本中配置的地址要包含在服务发布的地址中
        List<String> staticAddrList = new ArrayList<>();
        staticAddrList.add("127.0.0.1:9999");
        staticSubsetDto.setStaticAddrList(staticAddrList);
        envoyStaticServiceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{staticSubsetDto}));
        errorCode = serviceProxyService.checkEnvoyServiceProxyDto(envoyStaticServiceProxyDto);
        assertEquals(CommonErrorCode.InvalidSubsetStaticAddr.getCode(), errorCode.getCode());

        //同一个版本里配置的静态地址不能重复
        staticAddrList = new ArrayList<>();
        staticAddrList.add("127.0.0.1:8888");
        staticAddrList.add("127.0.0.1:8888");
        staticSubsetDto.setStaticAddrList(staticAddrList);
        envoyStaticServiceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{staticSubsetDto}));
        errorCode = serviceProxyService.checkEnvoyServiceProxyDto(envoyStaticServiceProxyDto);
        assertEquals(CommonErrorCode.DuplicatedSubsetStaticAddr.getCode(), errorCode.getCode());

        //每个地址仅能出现在0或1个版本中
        staticAddrList = new ArrayList<>();
        staticAddrList.add("127.0.0.1:8888");
        staticSubsetDto.setStaticAddrList(staticAddrList);

        staticAddrList = new ArrayList<>();
        staticAddrList.add("127.0.0.1:8888");
        EnvoySubsetDto staticSubsetDtoNew = new EnvoySubsetDto();
        staticSubsetDtoNew.setStaticAddrList(staticAddrList);
        envoyStaticServiceProxyDto.setSubsets(Arrays.asList(new EnvoySubsetDto[]{staticSubsetDto, staticSubsetDtoNew}));
        errorCode = serviceProxyService.checkEnvoyServiceProxyDto(envoyStaticServiceProxyDto);
        assertEquals(CommonErrorCode.DuplicatedStaticAddr.getCode(), errorCode.getCode());
    }

    @Test
    public void publishServiceToGw() {
        serviceProxyService.publishServiceToGw(serviceProxyDto);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }

    @Test
    public void updateServiceToGw() {
        serviceProxyService.publishServiceToGw(serviceProxyDto);
        serviceProxyService.updateServiceToGw(serviceProxyDto);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }

    @Test
    public void checkPublishParam() {
        ErrorCode errorCode = serviceProxyService.checkPublishParam(serviceProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }

    @Test
    public void checkUpdatePublishParam() {
        serviceProxyDto.setId(serviceProxyService.publishServiceToGw(serviceProxyDto));
        ErrorCode errorCode = serviceProxyService.checkUpdatePublishParam(serviceProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }

    @Test
    public void getEnvoyServiceProxyByLimit() {
        long l = serviceProxyService.publishServiceToGw(serviceProxyDto);
        long projectid = serviceInfoService.getServiceByServiceId(serviceId).getProjectId();
        List<ServiceProxyInfo> envoyServiceProxyByLimit = serviceProxyService.getEnvoyServiceProxy(gwId, serviceId, projectId, 0, 100);
        assertTrue(envoyServiceProxyByLimit.size() > 0);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }

    @Test
    public void getServiceProxyCountByLimit() {
        serviceProxyService.publishServiceToGw(serviceProxyDto);
        long count = serviceProxyService.getServiceProxyCount(gwId, serviceId);
        assertTrue(count > 0);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }


    @Test
    public void checkDeleteServiceProxy() {
        ErrorCode errorCode = serviceProxyService.checkDeleteServiceProxy(gwId, serviceId);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.ServiceNotPublished.getCode()));
    }


    @Test
    public void getServiceProxyByServiceIdAndGwId() {
        serviceProxyService.publishServiceToGw(serviceProxyDto);
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        assertTrue(serviceProxyInfo.getServiceId() == serviceId);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }

    @Test
    public void getServiceProxyInterByServiceIdAndGwIds() {
        serviceProxyService.publishServiceToGw(serviceProxyDto);
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.
                getServiceProxyInterByServiceIdAndGwIds(Arrays.asList(new Long[]{gwId}), serviceId);
        assertTrue(serviceProxyInfo.getServiceId() == serviceId);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }


    @Test
    public void getServiceProxyByServiceId() {
        List<ServiceProxyInfo> serviceProxyByServiceId = serviceProxyService.getServiceProxyByServiceId(serviceId);
        assertTrue(CollectionUtils.isEmpty(serviceProxyByServiceId));
    }

    @Test
    public void fromMeta() {
        serviceProxyService.publishServiceToGw(this.serviceProxyDto);
        ServiceProxyDto serviceProxyDto = serviceProxyService.
                fromMeta(serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId));
        assertTrue(serviceProxyDto.getServiceId() == serviceId);
        assertTrue(serviceProxyService.deleteServiceProxy(gwId, serviceId));
    }


    @Test
    public void getPublishedServiceGateway() {
        List<GatewayDto> publishedServiceGateway = serviceProxyService.getPublishedServiceGateway(serviceId);
        assertTrue(CollectionUtils.isEmpty(publishedServiceGateway));
    }

    @Test
    public void getRouteRuleNameWithServiceSubset() {
        ErrorCode errorCode = serviceProxyService.getRouteRuleNameWithServiceSubset(serviceProxyDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }
}