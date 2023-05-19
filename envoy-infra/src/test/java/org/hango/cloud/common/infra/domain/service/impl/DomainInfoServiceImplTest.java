//package org.hango.cloud.common.infra.domain.service.impl;
//
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
//import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
//import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
//import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
//import org.hango.cloud.common.infra.domain.dto.DomainRefreshResult;
//import org.hango.cloud.common.infra.domain.enums.DomainStatusEnum;
//import org.hango.cloud.common.infra.domain.meta.DomainInfo;
//import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
//import org.hango.cloud.common.infra.gateway.service.IGatewayService;
//import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
//import org.hango.cloud.common.infra.virtualgateway.dao.IVirtualGatewayDao;
//import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
//import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
//import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
//import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
//import org.hango.cloud.envoy.infra.routeproxy.service.IEnvoyRouteRuleProxyService;
//import org.hango.cloud.envoy.infra.virtualgateway.service.impl.EnvoyVgServiceImpl;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.hango.cloud.common.infra.domain.enums.DomainStatusEnum.WaitDelete;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.anyLong;
//
///**
// * @Author zhufengwei
// * @Date 2023/2/1
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@SuppressWarnings({"java:S1192"})
//public class DomainInfoServiceImplTest {
//
//    @Autowired
//    DomainInfoServiceImpl domainInfoService;
//
//    @Autowired
//    DomainInfoMapper domainInfoMapper;
//
//    @MockBean
//    IVirtualGatewayDao virtualGatewayDao;
//
//    @MockBean
//    CertificateInfoMapper certificateInfoMapper;
//
//    @MockBean
//    IGatewayService gatewayService;
//
//    @MockBean
//    EnvoyVgServiceImpl envoyVgService;
//
//    @MockBean
//    IEnvoyRouteRuleProxyService envoyRouteRuleProxyService;
//
//
//    @MockBean
//    IVirtualGatewayInfoService virtualGatewayInfoService;
//
//    @MockBean
//    IVirtualGatewayProjectService virtualGatewayProjectService;
//
//    public static DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
//
//    public static VirtualGateway virtualGateway = new VirtualGateway();
//
//    public static VirtualGatewayDto virtualGatewayDto = new VirtualGatewayDto();
//
//    public static RouteRuleProxyDto routeRuleProxyDto = new RouteRuleProxyDto();
//
//    public static GatewayDto gatewayDto = new GatewayDto();
//
//    public static CertificateInfoPO certificateInfoPO = new CertificateInfoPO();
//
//
//
//
//    private static String HOST = "httpbin.com";
//
//    @BeforeClass
//    public static void setUpBeforeClass(){
//        domainInfoDTO.setHost(HOST);
//        domainInfoDTO.setProtocol("HTTP");
//        domainInfoDTO.setDescription("test");
//        domainInfoDTO.setEnv("prod");
//        domainInfoDTO.setProjectId(1);
//        virtualGateway.setId(1L);
//        virtualGateway.setProtocol("HTTP");
//        gatewayDto.setEnvId("prod");
//        certificateInfoPO.setContent("test");
//
//        virtualGatewayDto.setId(1);
//        virtualGatewayDto.setName("gateway");
//        virtualGatewayDto.setEnvId("proId");
//        routeRuleProxyDto.setRouteRuleName("route");
//    }
//
//    @Before
//    public void init(){
//        MockitoAnnotations.openMocks(this);
//        Mockito.when(envoyVgService.refreshToGateway(Mockito.any())).thenReturn(true);
//        Mockito.when(virtualGatewayDao.get(anyLong())).thenReturn(virtualGateway);
//        Mockito.when(gatewayService.get(anyLong())).thenReturn(gatewayDto);
//        Mockito.when(certificateInfoMapper.selectById(1L)).thenReturn(certificateInfoPO);
////        Mockito.when(virtualGatewayInfoService.getGwEnvByProjectId(1L)).thenReturn(Collections.singletonList(virtualGatewayDto));
//        Mockito.when(virtualGatewayProjectService.bindProject(Mockito.any())).thenReturn(-1L);
//        Mockito.when(envoyRouteRuleProxyService.updateRouteProxy(Mockito.any())).thenReturn(1L);
//
//        domainInfoDTO.setId(null);
//        long id = domainInfoService.create(domainInfoDTO);
//        domainInfoDTO.setId(id);
//    }
//
//    @After
//    public void tearDown(){
//        domainInfoService.delete(domainInfoDTO);
//    }
//
//    @Test
//    public void create() {
//        DomainInfo domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
//        assertEquals(HOST, domainInfoPO.getHost());
//    }
//
//    @Test
//    public void update() {
//        domainInfoDTO.setDescription("update test");
//        domainInfoService.update(domainInfoDTO);
//        DomainInfo domainInfoPO = domainInfoMapper.selectById(domainInfoDTO.getId());
//        assertEquals("update test", domainInfoPO.getDescription());
//    }
//
//
//    @Test
//    public void getDomainInfoPage() {
//        Page<DomainInfo> page = domainInfoService.getDomainInfoPage(1, HOST, 0, 1);
//        assertEquals(1, page.getTotal());
//        assertEquals(1, page.getCurrent());
//        assertEquals(HOST, page.getRecords().get(0).getHost());
//    }
//
//
//    @Test
//    public void getRelevanceOnlyDomainInfos() {
//        domainInfoDTO.setStatus(DomainStatusEnum.RelevanceOnly.name());
//        domainInfoDTO.setRelevanceId(1L);
//        domainInfoService.update(domainInfoDTO);
//        List<DomainInfoDTO> domainInfos = domainInfoService.getRelevanceOnlyDomainInfos(1L);
//        assertEquals(HOST, domainInfos.get(0).getHost());
//        domainInfoDTO.setStatus(null);
//        domainInfoDTO.setRelevanceId(null);
//    }
//
//    @Test
//    public void getHosts() {
//        List<String> hosts = domainInfoService.getHosts(1, 1);
//        assertEquals(HOST, hosts.get(0));
//    }
//
//    @Test
//    public void getEnableHosts() {
//        List<String> hosts = domainInfoService.getEnableHosts(1, 1);
//        assertEquals(HOST, hosts.get(0));
//    }
//
//    @Test
//    public void checkCreateParam() {
//        DomainInfoDTO checkDTO = domainInfoService.toView(domainInfoMapper.selectById(domainInfoDTO.getId()));
//        ErrorCode errorCode = domainInfoService.checkCreateParam(checkDTO);
//        assertEquals(errorCode.message, "域名已存在，不允许重复创建");
//
//        checkDTO.setHost("*.com");
//        errorCode = domainInfoService.checkCreateParam(checkDTO);
//        assertEquals(errorCode.message, "不支持泛域名 *.com");
//
//
//        checkDTO.setCertificateId(2L);
//        errorCode = domainInfoService.checkCreateParam(checkDTO);
//        assertEquals(errorCode.message, "无效的证书id");
//
//        checkDTO.setCertificateId(1L);
//        errorCode = domainInfoService.checkCreateParam(checkDTO);
//        assertEquals(errorCode.message, "未上传服务器私钥");
//
//        checkDTO.setCertificateId(null);
//        checkDTO.setProtocol("https");
//        checkDTO.setHost("httpbin.com");
//        errorCode = domainInfoService.checkCreateParam(checkDTO);
//        assertEquals(errorCode.message, "HTTPS域名必须携带证书");
//    }
//
//    @Test
//    public void checkUpdateParam() {
//        DomainInfoDTO checkDTO = new DomainInfoDTO();
//        ErrorCode errorCode = domainInfoService.checkUpdateParam(checkDTO);
//        assertEquals(errorCode.message, "域名id不能为空");
//
//        checkDTO.setId(99L);
//        errorCode = domainInfoService.checkUpdateParam(checkDTO);
//        assertEquals(errorCode.message, "域名不存在，更新域名信息失败");
//
//    }
//
//    @Test
//    public void checkDeleteParam() {
//        DomainInfoDTO checkDTO = new DomainInfoDTO();
//        ErrorCode errorCode = domainInfoService.checkDeleteParam(checkDTO);
//        assertEquals(errorCode.message, "未找到需要删除的域名");
//
//        domainInfoDTO.setStatus(WaitDelete.name());
//        domainInfoService.update(domainInfoDTO);
//        checkDTO.setId(domainInfoDTO.getId());
//        errorCode = domainInfoService.checkDeleteParam(checkDTO);
//        assertEquals(errorCode.message, "域名已等待下线，请勿重复操作");
//    }
//
//    @Test
//    public void getDomainRefreshResult() {
//        List<DomainRefreshResult> domainRefreshResult = domainInfoService.getDomainRefreshResult(1L);
//        assertEquals("gateway", domainRefreshResult.get(0).getVirtualGwName());
//
//    }
//
////    @Test
//    public void refreshDomain() {
//        List<DomainRefreshResult> domainRefreshResult = domainInfoService.refreshDomain(1L);
//        assertEquals("gateway", domainRefreshResult.get(0).getVirtualGwName());
//        Mockito.when(virtualGatewayProjectService.bindProject(Mockito.any())).thenReturn(0L);
//        domainRefreshResult = domainInfoService.refreshDomain(1L);
//        assertEquals(0, domainRefreshResult.size());
//    }
//}