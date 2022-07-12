package org.hango.cloud.dashboard.apiserver.web.controller;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.hango.cloud.gdashboard.api.util.Const;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;

public class SyncDataControllerTest extends BaseServiceImplTest {

    public static String version = "2018-08-09";
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;

    @PostConstruct
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestContextHolderFilter()).
                addFilters(new LogUUIDFilter()).addFilters(new ProjectTraceFilter()).build();
    }

    @Test
    public void syncData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "SyncData")
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void getSyncInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "GetSyncStatistics")
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }
}