package org.hango.cloud.grpc.impl;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.gateway.dao.IGatewayDao;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.meta.Gateway;
import org.hango.cloud.common.infra.gateway.service.impl.GatewayServiceImpl;
import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbServiceDto;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.hango.cloud.envoy.infra.grpc.meta.PbService;
import org.hango.cloud.envoy.infra.grpc.remote.GrpcProtobufRemoteClient;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufCompileService;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.envoy.infra.grpc.service.impl.EnvoyGrpcProtobufCompileServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Xin Li
 * @date 2022/12/7 09:53
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EnvoyGrpcProtobufServiceImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcProtobufServiceImplTest.class);

    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;

    @MockBean
    private IEnvoyGrpcProtobufCompileService envoyGrpcProtobufCompileService;

    @MockBean
    private GrpcProtobufRemoteClient grpcProtobufRemoteClient;

    @Autowired
    private GatewayServiceImpl gatewayService;

    @Autowired
    private VirtualGatewayServiceImpl virtualGatewayInfoService;

    @Autowired
    private IGatewayDao gatewayDao;

    @Autowired
    private IVirtualGatewayDao virtualGatewayDao;

    private long mockServiceId = 1L;

    private long mockGwId = 1L;
    private long mockVirtualGwId = 1L;

    private String mockFileName = "mockFileName";

    private String mockPbFileContent = "mockPbFileContent";

    private long mockDate = System.currentTimeMillis();

    private final String GREETER0_SERVICE = "helloword.Greeter0";
    private final String GREETER1_SERVICE = "helloword.Greeter1";
    private final String GREETER2_SERVICE = "helloword.Greeter2";
    private List<String> mockPbServiceList = Arrays.asList(GREETER0_SERVICE, GREETER1_SERVICE, GREETER2_SERVICE);
    private List<String> mockPbServiceProxyList = Collections.singletonList(GREETER2_SERVICE);

    private EnvoyServiceProtobuf envoyServiceProtobuf;
    private EnvoyServiceProtobufProxy envoyServiceProtobufProxy;


    @Before
    public void init() {
        super.init();
        //数据准备：proto文件信息
        envoyServiceProtobuf = new EnvoyServiceProtobuf();
        envoyServiceProtobuf.setServiceId(mockServiceId);
        envoyServiceProtobuf.setCreateDate(mockDate);
        envoyServiceProtobuf.setModifyDate(mockDate);
        envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(mockPbServiceList));
        envoyServiceProtobuf.setPbFileName(mockFileName);
        envoyServiceProtobuf.setPbFileContent(mockPbFileContent);
        long pbId = envoyGrpcProtobufService.saveServiceProtobuf(envoyServiceProtobuf);
        envoyServiceProtobuf.setId(pbId);
        //数据准备：proto文件发布信息
        envoyServiceProtobufProxy = new EnvoyServiceProtobufProxy();
        envoyServiceProtobufProxy.setServiceId(mockServiceId);
        envoyServiceProtobufProxy.setCreateDate(mockDate);
        envoyServiceProtobufProxy.setModifyDate(mockDate);
        envoyServiceProtobufProxy.setPbServiceList(JSON.toJSONString(mockPbServiceProxyList));
        envoyServiceProtobufProxy.setVirtualGwId(mockVirtualGwId);
        envoyServiceProtobufProxy.setPbFileName(mockFileName);
        envoyServiceProtobufProxy.setPbFileContent(mockPbFileContent);
        envoyGrpcProtobufService.saveServiceProtobufProxy(envoyServiceProtobufProxy);
        MockitoAnnotations.openMocks(this);
        Mockito.when(grpcProtobufRemoteClient.publishGrpcEnvoyFilterToAPIPlane(Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(grpcProtobufRemoteClient.deleteGrpcEnvoyFilterToAPIPlane(Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(true);
        //Mock get 无效
//        Mockito.when(gatewayService.get(Mockito.anyInt())).thenReturn(new GatewayDto());
//        Mockito.when(virtualGatewayInfoService.get(Mockito.anyInt())).thenReturn(new VirtualGatewayDto());

        //create gateway
        Gateway gateway = new Gateway();
        gateway.setId(mockGwId);
        gateway.setName("envoy网关");
        gateway.setEnvId("prod");
        gateway.setSvcType("ClusterIP");
        gateway.setSvcName("gateway-proxy-svc");
        gateway.setType("envoy");
        gateway.setGwClusterName("prod-gateway");
        gateway.setConfAddr("http://api-plane.test-qingzhou.com");
        gateway.setDescription("aaaaa11");
        gateway.setCreateTime(System.currentTimeMillis());
        gateway.setModifyTime(System.currentTimeMillis());
        gatewayDao.add(gateway);

        //create virtual gateway
        VirtualGateway virtualGateway = new VirtualGateway();
        virtualGateway.setId(mockVirtualGwId);
        virtualGateway.setGwId(mockGwId);
        virtualGateway.setName("commongateway");
        virtualGateway.setCode("commongateway");
        virtualGateway.setProjectId("3");
        virtualGateway.setDescription("123");
        virtualGateway.setType("NetworkProxy");
        virtualGateway.setProtocol("HTTP");
        virtualGateway.setPort(8080);
        virtualGateway.setCreateTime(System.currentTimeMillis());
        virtualGateway.setModifyTime(System.currentTimeMillis());
        virtualGatewayDao.add(virtualGateway);

        Map<String, Object> checkResult = new HashMap<>();
        checkResult.put(EnvoyGrpcProtobufCompileServiceImpl.RESULT, true);
        checkResult.put(EnvoyGrpcProtobufCompileServiceImpl.DESC_FILE_BASE64, mockPbFileContent);
        Mockito.when(envoyGrpcProtobufCompileService.compilePbFile(Mockito.any(), Mockito.any())).thenReturn(checkResult);

    }

    @Test
    public void test_get_EnvoyServiceProtobuf() {
        EnvoyServiceProtobuf serviceProtobuf = envoyGrpcProtobufService.getServiceProtobuf(mockServiceId);
        Assert.assertEquals(serviceProtobuf.getPbFileName(), mockFileName);
    }

    @Test
    public void test_get_EnvoyServiceProtobufProxy() {
        EnvoyServiceProtobufProxy serviceProtobufProxy = envoyGrpcProtobufService.getServiceProtobufProxy(mockServiceId, mockVirtualGwId);
        Assert.assertEquals(serviceProtobufProxy.getPbFileName(), mockFileName);
    }

    @Test
    public void test_list_EnvoyServiceProtobuf() {
        List<EnvoyServiceProtobuf> envoyServiceProtobufList = envoyGrpcProtobufService.listServiceProtobuf();
        Assert.assertTrue(envoyServiceProtobufList.size() > 0);
    }

    public void test_list_EnvoyServiceProtobufProxy() {
        List<EnvoyPublishedServiceProtobufDto> envoyPublishedServiceProtobufDtoList = envoyGrpcProtobufService.listPublishedServiceProtobuf(mockServiceId);
        Assert.assertTrue(envoyPublishedServiceProtobufDtoList.size() > 0);
    }

    @Test
    public void test_pb_service_list() {
        //查询pb服务列表
        List<PbServiceDto> pbServiceDtoList = envoyGrpcProtobufService.describePbServiceList(envoyServiceProtobuf.getId());
        Assert.assertEquals(3, pbServiceDtoList.size());
        List<String> pbServiceListInDB = pbServiceDtoList.stream().map(PbServiceDto::getServiceName).collect(Collectors.toList());
        Assert.assertEquals(mockPbServiceList, pbServiceListInDB);

        long pbService0Id = 0, pbService2Id = 0;
        for (PbServiceDto pbServiceDto : pbServiceDtoList) {
            switch (pbServiceDto.getServiceName()) {
                case GREETER0_SERVICE:
                    pbService0Id = pbServiceDto.getId();
                    break;
                case GREETER2_SERVICE:
                    pbService2Id = pbServiceDto.getId();
                    break;
                default:
                    break;
            }
        }

        //发布单个pb服务
        envoyGrpcProtobufService.publicPbService(pbService0Id, mockVirtualGwId);
        pbServiceDtoList = envoyGrpcProtobufService.describePbServiceList(envoyServiceProtobuf.getId());

        //验证上线后pb服务状态
        for (PbServiceDto pbServiceDto : pbServiceDtoList) {
            switch (pbServiceDto.getServiceName()) {
                case GREETER1_SERVICE:
                    Assert.assertEquals(PbService.PUBLISH_STATUS_NOT_PUBLISHED, pbServiceDto.getPublishStatus());
                    break;
                case GREETER0_SERVICE:
                case GREETER2_SERVICE:
                default:
                    Assert.assertEquals(PbService.PUBLISH_STATUS_PUBLISHED, pbServiceDto.getPublishStatus());
                    break;
            }
        }

        //下线单个pb服务
        envoyGrpcProtobufService.offlinePbService(pbService2Id, mockVirtualGwId);
        pbServiceDtoList = envoyGrpcProtobufService.describePbServiceList(envoyServiceProtobuf.getId());

        //验证下线后pb服务状态
        for (PbServiceDto pbServiceDto : pbServiceDtoList) {
            switch (pbServiceDto.getServiceName()) {
                case GREETER1_SERVICE:
                case GREETER2_SERVICE:
                    Assert.assertEquals(PbService.PUBLISH_STATUS_NOT_PUBLISHED, pbServiceDto.getPublishStatus());
                    break;
                case GREETER0_SERVICE:
                default:
                    Assert.assertEquals(PbService.PUBLISH_STATUS_PUBLISHED, pbServiceDto.getPublishStatus());
                    break;
            }
        }

    }

    public void destroy() {
        GatewayDto gatewayDto = gatewayService.get(mockGwId);
        gatewayService.delete(gatewayDto);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(mockVirtualGwId);
        virtualGatewayInfoService.delete(virtualGatewayDto);
    }
}
