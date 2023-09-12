package org.hango.cloud.common.infra.route.service.impl;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.route.dto.*;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.serviceproxy.service.impl.ServiceProxyServiceImpl;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.route.service.impl.EnvoyRouteServiceImpl;
import org.hango.cloud.envoy.infra.serviceproxy.service.impl.EnvoyServiceProxyServiceImpl;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.URI_TYPE_EXACT;
import static org.hango.cloud.common.infra.base.meta.BaseConst.URI_TYPE_REGEX;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author zhufengwei
 * @Date 2023/1/31
 */
@SuppressWarnings({"java:S1192"})
@SpringBootTest
public class RouteRuleServiceImplTest {

    @Autowired
    RouteServiceImpl routeRuleProxyService;

    @Autowired
    VirtualGatewayServiceImpl virtualGatewayService;

    @Autowired
    ServiceProxyServiceImpl serviceProxyService;

    @MockBean
    EnvoyRouteServiceImpl envoyRouteRuleProxyService;

    @MockBean
    EnvoyServiceProxyServiceImpl envoyServiceProxyService;



    @BeforeEach
    public void mock(){
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyServiceProxyService.publishToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.offlineToGateway(Mockito.any())).thenReturn(true);

        Mockito.when(envoyRouteRuleProxyService.publishRoute(Mockito.any())).thenReturn(true);
        Mockito.when(envoyRouteRuleProxyService.deleteRoute(Mockito.any())).thenReturn(true);
        Mockito.when(envoyServiceProxyService.updateToGateway(Mockito.any())).thenReturn(true);

    }


    @Test
    public void checkCreateParam() {
        RouteDto routeDto = preCreate();

        RouteDto checkDto = new RouteDto();
        checkDto.setVirtualGwId(99L);
        ErrorCode errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的网关不存在");

        checkDto.setVirtualGwId(routeDto.getVirtualGwId());
        checkDto.setName(routeDto.getName());
        checkDto.setProjectId(routeDto.getProjectId());
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "当前项目网关已存在同名路由，无法重复创建");

        //校验uri
        checkDto.setName("route-test");
        checkDto.setServiceMetaForRouteDtos(routeDto.getServiceMetaForRouteDtos());
        RouteStringMatchDto uri = new RouteStringMatchDto();
        checkDto.setUriMatchDto(uri);
        uri.setType(URI_TYPE_EXACT);
        uri.setValue(Collections.singletonList("test"));
        checkDto.setUriMatchDto(uri);
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "路由path不合法");

        uri.setType(URI_TYPE_REGEX);
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "路由path不合法");
        checkDto.setUriMatchDto(routeDto.getUriMatchDto());

        //校验Header
        RouteMapMatchDto header = new RouteMapMatchDto();
        checkDto.setHeaders(Collections.singletonList(header));
        header.setType(URI_TYPE_EXACT);
        header.setValue(Collections.singletonList("_test"));
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "参数 header 非法[header值格式错误]");

        header.setType(URI_TYPE_REGEX);
        header.setValue(Collections.singletonList(buildString(101)));
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "参数 header 非法[header值格式错误]");
        checkDto.setHeaders(routeDto.getHeaders());

        //校验query
        RouteMapMatchDto query = new RouteMapMatchDto();
        checkDto.setQueryParams(Collections.singletonList(query));
        query.setType(URI_TYPE_EXACT);
        query.setValue(Collections.singletonList("_test"));
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "参数 param 非法[param值格式错误]");

        query.setType(URI_TYPE_REGEX);
        query.setValue(Collections.singletonList(buildString(101)));
        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "参数 param 非法[param值格式错误]");
        checkDto.setQueryParams(routeDto.getQueryParams());

        errorCode = routeRuleProxyService.checkCreateParam(checkDto);
        assertEquals("处理成功", errorCode.message);

        postDelete(routeDto);
    }

    @Test
    public void checkUpdateParam() {
        RouteDto routeDto = preCreate();

        RouteDto checkDto = new RouteDto();
        ErrorCode errorCode = routeRuleProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的路由规则不存在");

        checkDto.setId(99L);
        errorCode = routeRuleProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的路由规则不存在");
        checkDto.setId(routeDto.getId());
        errorCode = routeRuleProxyService.checkUpdateParam(checkDto);
        Assertions.assertEquals(errorCode.message, "指定的服务不存在");
        checkDto.setServiceMetaForRouteDtos(routeDto.getServiceMetaForRouteDtos());

        errorCode = routeRuleProxyService.checkUpdateParam(routeDto);
        Assertions.assertEquals(errorCode.message, "处理成功");

        postDelete(routeDto);
    }


    @Test
    public void fillUpdateInfo() {
        //创建网关
        RouteDto routeDto = preCreate();

        UpdateRouteDto updateRouteDto = new UpdateRouteDto();
        updateRouteDto.setId(routeDto.getId());
        RouteDto updateRoute = routeRuleProxyService.fillUpdateInfo(updateRouteDto);
        Assertions.assertEquals(updateRoute.getName(), routeDto.getName());
        postDelete(routeDto);
    }

    @Test
    public void fillRouteMirrorDto() {
        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
        DestinationDto dto = new DestinationDto();
        routeMirrorDto.setMirrorTraffic(dto);
        routeRuleProxyService.fillRouteMirrorDto(routeMirrorDto);
        Assertions.assertEquals(dto.getPort(), 80);

        dto.setPort(66666);
        ErrorCode errorCode = routeRuleProxyService.fillRouteMirrorDto(routeMirrorDto);
        Assertions.assertEquals(errorCode.message, "服务端口非法");
    }

    @Test
    public void checkUpdateMirrorTrafficParam() {
        //创建网关
        RouteDto routeDto = preCreate();
        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
        routeMirrorDto.setVirtualGwId(99L);
        routeMirrorDto.setRouteId(99L);
        routeMirrorDto.setMirrorSwitch(1);
        ErrorCode errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.message, "指定的网关不存在");

        routeMirrorDto.setVirtualGwId(routeDto.getVirtualGwId());
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.message, "指定的路由规则不存在");

        routeMirrorDto.setRouteId(routeDto.getId());
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.getCode(), "MissingParameter");

        DestinationDto dto = new DestinationDto();
        routeMirrorDto.setMirrorTraffic(dto);
        dto.setServiceId(routeDto.getServiceIds().get(0));
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.getCode(), "ServiceSameWhenPublishMirror");

        dto.setServiceId(99L);
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.message, "指定的服务不存在");

        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(routeDto.getVirtualGwId());
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);
        dto.setServiceId(serviceId);
        errorCode = routeRuleProxyService.checkUpdateMirrorTrafficParam(routeMirrorDto);
        Assertions.assertEquals(errorCode.message, "处理成功");
        serviceProxyService.delete(serviceProxyDto);
        postDelete(routeDto);
    }

    @Test
    public void checkServiceAndSubsetExist() {
        //创建网关
        ServiceMetaForRouteDto serviceMetaForRouteDto = new ServiceMetaForRouteDto();
        serviceMetaForRouteDto.setServiceId(1L);
        DestinationDto destinationDto1 = new DestinationDto();
        destinationDto1.setSubsetName("v1");
        DestinationDto destinationDto2 = new DestinationDto();
        destinationDto2.setSubsetName("v1");
        serviceMetaForRouteDto.setDestinationServices(Arrays.asList(destinationDto1, destinationDto2));
        List<ServiceMetaForRouteDto> serviceMetaForRouteDtos = Collections.singletonList(serviceMetaForRouteDto);
        ErrorCode errorCode = routeRuleProxyService.checkServiceAndSubsetExist(serviceMetaForRouteDtos, null);
        Assertions.assertEquals(errorCode.message, "指定的服务不存在");

        ServiceProxyDto serviceProxyDto = new ServiceProxyDto();
        serviceProxyDto.setId(1L);

        errorCode = routeRuleProxyService.checkServiceAndSubsetExist(serviceMetaForRouteDtos, serviceProxyDto);
        Assertions.assertEquals(errorCode.message, "路由关联服务版本信息存在重复，不允许创建!");

        destinationDto2.setSubsetName("v2");
        errorCode = routeRuleProxyService.checkServiceAndSubsetExist(serviceMetaForRouteDtos, serviceProxyDto);
        Assertions.assertEquals(errorCode.message, "服务下不存在版本信息");

        SubsetDto subsetDto1 = new SubsetDto();
        subsetDto1.setName("v1");
        serviceProxyDto.setSubsets(Collections.singletonList(subsetDto1));

        errorCode = routeRuleProxyService.checkServiceAndSubsetExist(serviceMetaForRouteDtos, serviceProxyDto);
        Assertions.assertEquals(errorCode.message, "版本名称不存在");

    }

    @Test
    public void checkServiceMeta() {
        //创建网关
        ServiceMetaForRouteDto serviceMetaForRouteDto = new ServiceMetaForRouteDto();
        serviceMetaForRouteDto.setServiceId(1L);
        serviceMetaForRouteDto.setWeight(90);
        DestinationDto destinationDto1 = new DestinationDto();
        destinationDto1.setSubsetName("v1");
        destinationDto1.setWeight(30);
        DestinationDto destinationDto2 = new DestinationDto();
        destinationDto2.setWeight(60);
        serviceMetaForRouteDto.setDestinationServices(Arrays.asList(destinationDto1, destinationDto2));
        List<ServiceMetaForRouteDto> serviceMetaForRouteDtos = Collections.singletonList(serviceMetaForRouteDto);
        ErrorCode errorCode = routeRuleProxyService.checkServiceMeta(serviceMetaForRouteDtos);
        Assertions.assertEquals(errorCode.message, "权重之和必须为100");

        serviceMetaForRouteDto.setWeight(100);
        errorCode = routeRuleProxyService.checkServiceMeta(serviceMetaForRouteDtos);
        Assertions.assertEquals(errorCode.message, "权重之和必须为100");

        destinationDto2.setWeight(70);
        errorCode = routeRuleProxyService.checkServiceMeta(serviceMetaForRouteDtos);
        Assertions.assertEquals(errorCode.message, "处理成功");
    }

    @Test
    public void publishMirrorTraffic() {
        //创建网关
        RouteDto routeDto = preCreate();
        RouteMirrorDto routeMirrorDto = new RouteMirrorDto();
        routeMirrorDto.setVirtualGwId(routeDto.getVirtualGwId());
        routeMirrorDto.setRouteId(routeDto.getId());
        routeMirrorDto.setMirrorSwitch(1);
        DestinationDto dto = new DestinationDto();
        dto.setServiceId(routeDto.getServiceIds().get(0));
        dto.setWeight(100);
        dto.setPort(80);
        routeMirrorDto.setMirrorTraffic(dto);
        routeRuleProxyService.publishMirrorTraffic(routeMirrorDto);

        RouteDto updateRoute = routeRuleProxyService.get(routeDto.getId());

        Assertions.assertEquals(updateRoute.getMirrorTraffic().getServiceId(), routeDto.getServiceIds().get(0));
        postDelete(routeDto);
    }


    private RouteDto preCreate(){
        //创建网关
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        //创建服务
        ServiceProxyDto serviceProxyDto = MockUtil.initServiceProxy();
        serviceProxyDto.setVirtualGwId(vgId);
        long serviceId = serviceProxyService.create(serviceProxyDto);
        serviceProxyDto.setId(serviceId);

        RouteDto routeDto = MockUtil.initRoute(serviceId);
        routeDto.setVirtualGwId(vgId);

        long id = routeRuleProxyService.create(routeDto);
        routeDto.setId(id);
        return routeDto;
    }

    private void postDelete(RouteDto routeDto){
        routeRuleProxyService.delete(routeDto);
        List<ServiceMetaForRouteDto> serviceMetaForRouteDtos = routeDto.getServiceMetaForRouteDtos();
        if (CollectionUtils.isNotEmpty(serviceMetaForRouteDtos)) {
            for (ServiceMetaForRouteDto serviceMetaForRouteDto : serviceMetaForRouteDtos) {
                ServiceProxyDto serviceProxyDto = serviceProxyService.get(serviceMetaForRouteDto.getServiceId());
                if (serviceProxyDto == null) {
                    continue;
                }
                serviceProxyService.delete(serviceProxyDto);
            }
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayService.get(routeDto.getVirtualGwId());
        if (virtualGatewayDto != null){
            virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
        }
    }

    private String buildString(int length){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }
}