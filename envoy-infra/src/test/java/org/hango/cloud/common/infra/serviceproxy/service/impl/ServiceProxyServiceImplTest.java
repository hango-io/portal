package org.hango.cloud.common.infra.serviceproxy.service.impl;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.impl.DomainInfoServiceImpl;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.*;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceType;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.hango.cloud.gdashboard.api.meta.ApiModel;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hango.cloud.common.infra.base.meta.BaseConst.STATIC_PUBLISH_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"java:S1192"})
@SpringBootTest
public class ServiceProxyServiceImplTest {

    @Autowired
    ServiceProxyServiceImpl serviceProxyService;

    @Autowired
    IVirtualGatewayInfoService virtualGatewayService;


    @Autowired
    DomainInfoServiceImpl domainInfoService;


    @MockBean
    IEnvoyServiceProxyService envoyServiceProxyService;

    @MockBean
    private IApiModelService apiModelService;

    @MockBean
    private IApiInfoService apiInfoService;

    @MockBean
    private IRouteService routeService;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.updateToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);

    }


    @Test
    public void checkCreateParam() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);


        ErrorCode errorCode = serviceProxyService.checkCreateParam(serviceProxyDto);
        Assertions.assertEquals(errorCode.getCode(), "AlreadyExist");

        ServiceProxyDto checkDto = new ServiceProxyDto();
        checkDto.setVirtualGwId(99L);
        checkDto.setName("test-create");
        errorCode = serviceProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "服务类型不合法");

        checkDto.setProtocol("http");

        errorCode = serviceProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的网关不存在");

        checkDto.setVirtualGwId(vgId);

        errorCode = serviceProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "域名不存在");

        checkDto.setHosts("service.com");

        errorCode = serviceProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "域名不存在");

        checkDto.setHosts(domainInfoDTO.getHost());
        errorCode = serviceProxyService.checkCreateParam(checkDto);
        assertEquals("处理成功", errorCode.message);

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);

    }

    @Test
    public void checkUpdateParam() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        ServiceProxyDto checkDto = new ServiceProxyDto();
        checkDto.setId(99L);
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的服务不存在");
        checkDto.setId(serviceId);
        checkDto.setName("test-update");

        errorCode = serviceProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "服务名称不支持修改");

        checkDto.setName(serviceProxyDto.getName());

        errorCode = serviceProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "服务类型不合法");

        checkDto.setProtocol("http");

        errorCode = serviceProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的网关不存在");

        checkDto.setVirtualGwId(vgId);

        errorCode = serviceProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "域名不存在");

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void checkDeleteParam() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        ServiceProxyDto checkDto = new ServiceProxyDto();
        checkDto.setId(serviceId);
        checkDto.setVirtualGwId(99L);
        ErrorCode errorCode = serviceProxyService.checkDeleteParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的网关不存在");

        checkDto.setVirtualGwId(vgId);
        mockRoute();
        errorCode = serviceProxyService.checkDeleteParam(checkDto);
        Assertions.assertEquals(errorCode.message, "路由规则已经发布至该网关");

        Mockito.when(apiModelService.getApiModelByServiceId(Mockito.anyLong())).thenReturn(Collections.singletonList(new ApiModel()));
        errorCode = serviceProxyService.checkDeleteParam(checkDto);
        Assertions.assertEquals(errorCode.message, "在删除api前，不能删除服务");

        Mockito.when(apiInfoService.getApiCountByServiceId(Mockito.anyLong())).thenReturn(1L);
        errorCode = serviceProxyService.checkDeleteParam(checkDto);
        Assertions.assertEquals(errorCode.message, "在删除api前，不能删除服务");


        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void checkRouteMirrorSubset() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setHosts("service.com");
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);


        serviceProxyDto.setId(serviceId);
        mockRoute();

        SubsetDto subsetDto = new SubsetDto();
        subsetDto.setName("subset-service");
        serviceProxyDto.setSubsets(Collections.singletonList(subsetDto));

        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        Assertions.assertEquals(errorCode.code, "SubsetUsedByRouteRule");


        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void checkSubset() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setHosts("service.com");
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);
        RouteDto routeDto = MockUtil.initRouteDtoWithServiceMeta();
        Mockito.when(routeService.getRouteList(Mockito.any())).thenReturn(Collections.singletonList(routeDto));

        SubsetDto subsetDto = new SubsetDto();
        subsetDto.setName("subset-service");
        serviceProxyDto.setSubsets(Collections.singletonList(subsetDto));
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        Assertions.assertEquals(errorCode.code, "SubsetUsedByRouteRule");
        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void checkRegistryCenterInfo() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        serviceProxyDto.setRegistryCenterType(null);
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("处理成功", errorCode.message);

        serviceProxyDto.setProtocol(ServiceType.dubbo.name());
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("NotSupportedRegistryType", errorCode.code);

        serviceProxyDto.setProtocol(ServiceType.grpc.name());
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("PublishTypeNotSupport", errorCode.code);


        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }

    @Test
    public void checkTrafficPolicy() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        ServiceLoadBalancerDto serviceLoadBalancerDto = new ServiceLoadBalancerDto();
        ServiceTrafficPolicyDto trafficPolicyDto = new ServiceTrafficPolicyDto();
        trafficPolicyDto.setLoadBalancer(serviceLoadBalancerDto);
        serviceProxyDto.setTrafficPolicy(trafficPolicyDto);

        serviceLoadBalancerDto.setType(BaseConst.SERVICE_LOADBALANCER_HASH);
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象格式非法", errorCode.message);

        ServiceConsistentHashDto serviceConsistentHashDto = new ServiceConsistentHashDto();
        serviceLoadBalancerDto.setConsistentHash(serviceConsistentHashDto);


        serviceConsistentHashDto.setType(BaseConst.SERVICE_LOADBALANCER_HASH_HTTPCOOKIE);
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象使用cookie时，cookie对象不能为空", errorCode.message);
        ServiceConsistentHashDto.ServiceConsistentHashCookieDto serviceConsistentHashCookieDto = new ServiceConsistentHashDto.ServiceConsistentHashCookieDto();
        serviceConsistentHashDto.setCookieDto(serviceConsistentHashCookieDto);

        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象使用cookie时，cookie名称不能为空", errorCode.message);

        serviceConsistentHashCookieDto.setName("cookieName");
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象使用cookie时，cookie ttl不能小于0", errorCode.message);

        serviceConsistentHashDto.setType(BaseConst.SERVICE_LOADBALANCER_HASH_HTTPHEADERNAME);
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象使用HttpHeaderName时，HttpHeaderName不能为空", errorCode.message);
        serviceConsistentHashDto.setHttpHeaderName("HttpHeaderName");
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("处理成功", errorCode.message);

        serviceConsistentHashDto.setType(BaseConst.SERVICE_LOADBALANCER_HASH_USESOURCEIP);
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一致性哈希对象使用源IP时，源IP不能为空", errorCode.message);
        serviceConsistentHashDto.setUseSourceIp(true);
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("处理成功", errorCode.message);

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }


    @Test
    public void checkSubsetWhenPublishService() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);

        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        SubsetDto subsetDto1 = new SubsetDto();
        subsetDto1.setName("subset1");
        Map<String, String> labels = new HashMap<>();
        labels.put("version", "!@v1");
        subsetDto1.setLabels(labels);
        SubsetDto subsetDto2 = new SubsetDto();
        subsetDto2.setName("subset1");
        serviceProxyDto.setSubsets(Arrays.asList(subsetDto1, subsetDto2));

        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("版本名称不能重复", errorCode.message);
        subsetDto2.setName("subset2");

        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("标签不合法", errorCode.message);


        serviceProxyDto.setPublishType(STATIC_PUBLISH_TYPE);
        subsetDto1.setStaticAddrList(Collections.singletonList("127.0.0.1:80"));
        serviceProxyDto.setBackendService("127.0.0.2:80");
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("版本中的地址不合法，不能为空且需要包含在服务发布地址列表中", errorCode.message);

        subsetDto2.setStaticAddrList(Collections.singletonList("127.0.0.1:80"));
        serviceProxyDto.setBackendService("127.0.0.1:80");
        errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        assertEquals("一个地址仅能属于一个版本", errorCode.message);

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        domainInfoService.delete(domainInfoDTO);
    }

    @Test
    public void updateServiceHost() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setHosts("service.com");
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);
        serviceProxyService.updateServiceHost(serviceId, "service2.com");
        ServiceProxyDto updateDto = serviceProxyService.get(serviceId);
        Assertions.assertEquals(updateDto.getHosts(), "service2.com");

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }


    @Test
    public void getAllServiceTag() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setHosts("service.com");
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        List<String> allServiceTag = serviceProxyService.getAllServiceTag(vgId);
        Assertions.assertEquals(allServiceTag.size(), 1);

        Assertions.assertEquals(allServiceTag.get(0), serviceProxyDto.getName());

        serviceProxyService.delete(serviceProxyDto);
        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void fillTrafficPolicy() {
        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        ServiceProxyConvert.fillTrafficPolicy(serviceProxyDto);
        Assertions.assertEquals(serviceProxyDto.getTrafficPolicy().getLoadBalancer().getSimple(), "ROUND_ROBIN");
        Assertions.assertEquals(serviceProxyDto.getTrafficPolicy().getConnectionPoolDto().getServiceHttpConnectionPoolDto().getHttp2MaxRequests(), 1024);

    }


    private void mockRoute(){
        RouteDto routeDto = new RouteDto();
        routeDto.setMirrorSwitch(1);
        DestinationDto dto = new DestinationDto();
        dto.setSubsetName("test-subset-service");
        routeDto.setMirrorTraffic(dto);
        Mockito.when(routeService.getRouteList(Mockito.any())).thenReturn(Collections.singletonList(routeDto));
    }


}