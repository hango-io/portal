package org.hango.cloud.dashboard.apiserver.web.controller.apimanage;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.hango.cloud.dashboard.apiserver.web.filter.LogUUIDFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.ProjectTraceFilter;
import org.hango.cloud.dashboard.apiserver.web.filter.RequestContextHolderFilter;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hango.cloud.gdashboard.api.meta.ApiInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public class ApiBasicInfoControllerTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(ApiBasicInfoControllerTest.class);
    public static String version = "2018-08-09";
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    @Autowired
    private IServiceInfoService serviceInfoService;
    private ApiInfoBasicDto apiInfoBasicDto;
    private ApiInfo apiInfo;
    private ServiceInfo serviceInfo;
    private long serviceId = 0;

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

        apiInfoBasicDto = new ApiInfoBasicDto();
        apiInfoBasicDto.setApiMethod("GET");
        apiInfoBasicDto.setApiName("testUnit");
        apiInfoBasicDto.setApiPath("/testUnit");
        apiInfoBasicDto.setServiceId(serviceId);
        apiInfoBasicDto.setType("RESTFUL");
        apiInfo = BeanUtil.copy(apiInfoBasicDto, ApiInfo.class);
    }

    @After
    public void tearDownClass() {
        logger.info("tear down class .... ApiProxyServiceImplTest");
        //清除service
        serviceInfoService.delete(serviceId);
    }

    @Test
    @Rollback
    public void addApi() throws Exception {
//        String apiInfoBasicDtoJson = JSONObject.toJSONString(apiInfoBasicDto);
//        mockMvc.perform(MockMvcRequestBuilders.post(Const.G_DASHBOARD_PREFIX).param("Version",version)
//                .param("Action","CreateApi").contentType(MediaType.APPLICATION_JSON)
//                .content(apiInfoBasicDtoJson)
//                .header("x-auth-projectId",projectId).header("x-auth-tenantId",tenantId)
//                .header("x-auth-accountId",accountId).header("x-auth-token",tenantId))
//                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void getApiInfo() {
    }

    @Test
    public void updateApi() {
    }

    @Test
    public void deleteApi() {
    }

    @Test
    public void apiList() {
    }
}