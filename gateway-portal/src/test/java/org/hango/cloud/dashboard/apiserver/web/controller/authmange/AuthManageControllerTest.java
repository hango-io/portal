package org.hango.cloud.dashboard.apiserver.web.controller.authmange;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.hango.cloud.gdashboard.api.util.Const;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Rollback
public class AuthManageControllerTest extends BaseServiceImplTest {
    public static String version = "2018-08-09";
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;

    @Before
    public void initMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestContextHolderFilter()).
                addFilters(new LogUUIDFilter()).addFilters(new ProjectTraceFilter()).build();
    }


    @Test
    public void createAuthManage() {
    }

    @Test
    public void deleteAuthManage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DeleteAuthManage").param("EnvId", envId)
                        .param("AuthId", String.valueOf(System.currentTimeMillis()))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    public void describeAuthInfoByAuthId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeAuthInfoByAuthId").param("EnvId", envId)
                        .param("AuthId", String.valueOf(System.currentTimeMillis()))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    //    @Test
    public void describePublishedInfoByEnvId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribePublishedInfoByEnvId").param("EnvId", envId)
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    //    @Test
    public void describePublishedServiceByEnvId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribePublishedServiceByEnvId").param("EnvId", envId)
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void authList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeAuthList").param("EnvId", envId)
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }
}