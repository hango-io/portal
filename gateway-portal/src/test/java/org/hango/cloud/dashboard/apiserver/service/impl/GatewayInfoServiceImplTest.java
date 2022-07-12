package org.hango.cloud.dashboard.apiserver.service.impl;

import org.assertj.core.util.Lists;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayAddrConfigInfo;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(AccessUtil.class)
//@PowerMockIgnore({"javax.management.*"})
public class GatewayInfoServiceImplTest extends BaseServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInfoImplTest.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;

    private GatewayDto gatewayDto;

//    @Before
//    public void before(){
//        PowerMockito.mockStatic(AccessUtil.class);
//    }

    @PostConstruct
    public void init() {
        //初始化gatewayInfoMetaDto
        gatewayDto = new GatewayDto();
        gatewayDto.setGwName(gwName);
        gatewayDto.setGwAddr(gwAddr);
        List<Long> projectIdList = new ArrayList<>();
        projectIdList.add(projectId);
        gatewayDto.setProjectIdList(projectIdList);
        //构造GatewayAddrConfigInfo
        GatewayInfo gatewayByName = gatewayInfoService.getGatewayByName(gwNameReal);
        GatewayAddrConfigInfo gatewayAddrConfigInfo = new GatewayAddrConfigInfo();
        gatewayAddrConfigInfo.setAuditDatasourceSwitch(gatewayByName.getAuditDatasourceSwitch());
        gatewayAddrConfigInfo.setAuthAddr(gatewayByName.getAuthAddr());
        gatewayAddrConfigInfo.setEnvId(gatewayByName.getEnvId());
        gatewayAddrConfigInfo.setAuditDbConfig(gatewayByName.getAuditDbConfig());
        gatewayDto.setGatewayAddrConfigInfo(gatewayAddrConfigInfo);
    }

    @Test
    @Rollback
    public void updateGwInfo() {
        long gwId = gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        gatewayInfo.setGwName("testUnit2");
        gatewayInfoService.updateGwInfo(gatewayInfo, true);
        assertTrue(gatewayInfoService.get(gwId).getGwName().equals("testUnit2"));
    }

    //    @Test
//    @Rollback
    public void delete() {
        HttpClientResponse httpClientResponse = new HttpClientResponse(200, Lists.newArrayList(), "");
        PowerMockito.when(AccessUtil.accessFromOtherPlat(anyString(), anyMap(), anyString(), anyMap(), anyString())).thenReturn(httpClientResponse);
        long gwId = gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        gatewayInfoService.delete(gwId, new ArrayList<>());
        assertTrue(!gatewayInfoService.isGwExists(gwId));
    }

    @Test
    @Rollback
    public void findAll() {
        List<GatewayInfo> all = gatewayInfoService.findAll();
        assertTrue(all.size() > 0);
    }

    @Test
    @Rollback
    public void findGatewayByLimit() {
        gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        List<GatewayInfo> gatewayByLimit = gatewayInfoService.findGatewayByLimit(gwName, 0, 20);
        assertTrue(gatewayByLimit.size() == 1);
    }

//    @Test
//    @Rollback
//    public void findGatwayByProjectIdAndLimit() {
//        gatewayInfoService.addGatewayByMetaDto(gatewayDto);
//        List<GatewayInfo> projectIdAndLimit = gatewayInfoService.findGatwayByProjectIdAndLimit(gwName, 0, 20, projectId);
//        assertTrue(projectIdAndLimit.size() == 1);
//    }

    @Test
    @Rollback
    public void getGatewayCount() {
        gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        assertTrue(gatewayInfoService.getGatewayCount(gwName) == 1);
    }

//    @Test
//    @Rollback
//    public void getGatewayCountByProjectId() {
//        gatewayInfoService.addGatewayByMetaDto(gatewayDto);
//        assertTrue(gatewayInfoService.getGatewayCountByProjectId(gwName,projectId) == 1);
//    }

    @Test
    public void checkGwIdParam() {
        gatewayInfoService.checkGwIdParam("");
        gatewayInfoService.checkGwIdParam(String.valueOf(System.currentTimeMillis()));
    }

//    @Test
//    @Rollback
//    public void getGwEnvByProjectId() {
//        gatewayInfoService.addGatewayByMetaDto(gatewayDto);
//        assertTrue(gatewayInfoService.getGwEnvByProjectId(projectId).size() > 0);
//    }

    @Test
    @Rollback
    public void addGatewayByMetaDto() {
        //isExistGwInstance单元测试
        assertTrue(!gatewayInfoService.isExistGwInstance(gwName));
        //addGatewayByMetaDto单元测试
        long gwId = gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        //get 单元测试
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        //isGwEnvExists单元测试
        assertTrue(gatewayInfoService.isGwExists(gwId));
        //getGatewayByName单元测试
        GatewayInfo gatewayByName = gatewayInfoService.getGatewayByName(gwName);
        assertTrue(gatewayInfo.getGwName().equals(gwName));
        assertTrue(gatewayByName.getGwName().equals(gwName));
    }
}