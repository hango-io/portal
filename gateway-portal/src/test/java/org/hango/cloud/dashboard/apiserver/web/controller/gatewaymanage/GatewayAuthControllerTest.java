package org.hango.cloud.dashboard.apiserver.web.controller.gatewaymanage;

import com.alibaba.fastjson.JSONObject;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.platform.ServiceAuthServiceImpl;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.hango.cloud.gdashboard.api.util.Const;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

public class GatewayAuthControllerTest extends BaseServiceImplTest {

    public static String version = "2018-08-09";
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    @Autowired
    private ServiceAuthServiceImpl serviceAuthService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    private long authId;
    private String authName;
    private GatewayInfo gatewayInfo;

    @Before
    public void initMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestContextHolderFilter()).
                addFilters(new LogUUIDFilter()).addFilters(new ProjectTraceFilter()).build();
        List<GatewayInfo> gwByEnvId = gatewayInfoService.getGwByEnvId(envId);
        if (!CollectionUtils.isEmpty(gwByEnvId)) {
            gatewayInfo = gwByEnvId.get(0);
        }
        AbstractController.ResultWithMessage exterServiceByLimit = serviceAuthService.getExterServiceByLimit(gatewayInfo.getAuthAddr());
        if (exterServiceByLimit != null) {
            JSONObject responseBodyObject = (JSONObject) exterServiceByLimit.getResult();
            String exterServiceList = responseBodyObject.getJSONArray("ExterServiceList").get(0).toString();
            authName = JSONObject.parseObject(exterServiceList).getString("ServiceName");
            authId = JSONObject.parseObject(exterServiceList).getLong("ServiceId");
        }
    }


    @Test
    public void createGatewayAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "CreateGatewayAuthentication").param("AuthId", String.valueOf(authId))
                        .param("GwId", String.valueOf(gatewayInfo.getId()))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeGatewayAuthenticaiton")
                        .param("GwId", String.valueOf(gatewayInfo.getId()))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DeleteGatewayAuthentication").param("AuthId", String.valueOf(authId))
                        .param("GwId", String.valueOf(gatewayInfo.getId()))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }
}