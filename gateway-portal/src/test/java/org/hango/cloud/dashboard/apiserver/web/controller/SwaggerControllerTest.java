package org.hango.cloud.dashboard.apiserver.web.controller;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class SwaggerControllerTest extends BaseServiceImplTest {

    public static String version = "2018-08-09";
    private static long serviceId;
    private static long apiId;
    @Autowired
    IServiceInfoService serviceInfoService;
    @Autowired
    IApiInfoService apiInfoService;
    MockMvc mockMvc;
    @Autowired
    WebApplicationContext context;
    private ServiceInfo serviceInfo;
    private ApiInfo apiInfo;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new RequestContextHolderFilter()).
                addFilters(new LogUUIDFilter()).addFilters(new ProjectTraceFilter()).build();
        //初始化ServiceInfo
        serviceInfo = new ServiceInfo();
        serviceInfo.setDisplayName(displayName);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setContacts(user);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setServiceType(serviceType);
        //创建service
        serviceId = serviceInfoService.add(serviceInfo);

        //构造API Info
        ApiInfoBasicDto apiInfoBasicDto = new ApiInfoBasicDto();
        apiInfoBasicDto.setApiMethod("GET");
        apiInfoBasicDto.setApiName("testUnitSwagger");
        apiInfoBasicDto.setApiPath("/testUnitSwagger");
        apiInfoBasicDto.setServiceId(serviceId);
        apiInfoBasicDto.setType("RESTFUL");
        apiInfo = BeanUtil.copy(apiInfoBasicDto, ApiInfo.class);
        apiInfo.setProjectId(projectId);
        apiId = apiInfoService.addDubboOrWebServiceApi(apiInfo, "RESTFUL");
    }

    @After
    public void tear() {
        //清除api
        apiInfoService.deleteApi(apiId);
        //清除service
        serviceInfoService.delete(serviceId);
    }

    @Test
    public void getServicesDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeSwaggerServiceById").param("ServiceId", String.valueOf(serviceId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void getInterfacesDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeSwaggerApiById").param("InterfaceId", String.valueOf(apiId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void getApiMarkDownDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeMarkdownApiById").param("InterfaceId", String.valueOf(apiId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void getServiceMarkDownDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DescribeMarkdownServiceById").param("ServiceId", String.valueOf(serviceId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void downloadApiMarkDownDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DownloadMarkdownApiById").param("InterfaceId", String.valueOf(apiId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void downloadServiceMarkDownDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "DownloadMarkdownServiceById").param("ServiceId", String.valueOf(serviceId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }
}