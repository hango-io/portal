package org.hango.cloud.dashboard.apiserver.service.impl.apigateway;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.service.apigateway.IGetInfoFromGatewayService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class GetInfoFromGatewayServiceImplTest extends BaseServiceImplTest {

    @Autowired
    private IGetInfoFromGatewayService getInfoFromGatewayService;

    @Test
    public void getDataSourceAddr() {
        Map<String, String> dataSourceAddr = getInfoFromGatewayService.getDataSourceAddr(gwAddr);
        String auditDatasourceSwitch = dataSourceAddr.get("AuditDatasourceSwitch");
        assertTrue(StringUtils.isNotBlank(auditDatasourceSwitch));
    }

    @Test
    public void getAuthEnv() {
        Map<String, String> authEnv = getInfoFromGatewayService.getAuthEnv(gwAddr);
        assertTrue(StringUtils.isNotBlank(authEnv.get("AuthAddr")));
    }

    @Test
    public void getHealthFromGateway() {
        int healthFromGateway = getInfoFromGatewayService.getHealthFromGateway(gwAddr, health_interface);
        assertTrue(healthFromGateway == 1);
    }

    @Test
    public void checkAndUpdateFromGateway() {
        getInfoFromGatewayService.checkAndUpdateFromGateway(0, gwAddr, health_interface);
    }

    @Test
    public void checkAuthConfig() {
        boolean b = getInfoFromGatewayService.checkAuthConfig(gwAddr);
        assertTrue(b);
    }
}