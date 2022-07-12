package org.hango.cloud.dashboard.apiserver.web.controller;

import org.apache.commons.io.IOUtils;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.dto.exportImport.ExportImportDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.impl.exportImport.JsonExportImportService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ExportImportJsonControllerTest extends BaseServiceImplTest {

    public static String version = "2018-08-09";
    private static long serviceId;
    private static long apiId;
    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;
    @Autowired
    IServiceInfoService serviceInfoService;
    @Autowired
    IApiInfoService apiInfoService;
    @Autowired
    private JsonExportImportService jsonExportImportService;
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
        apiInfoBasicDto.setApiName("testUnitExport");
        apiInfoBasicDto.setApiPath("/testUnitExport");
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
    public void exportData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Const.G_DASHBOARD_PREFIX).param("Version", version)
                        .param("Action", "ExportData").param("ServiceId", String.valueOf(serviceId))
                        .header("x-auth-projectId", projectId).header("x-auth-tenantId", tenantId)
                        .header("x-auth-accountId", accountId).header("x-auth-token", tenantId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    @Rollback
    public void importData() throws IOException, InterruptedException {
        File fileRaw = new File("./src/test/resources/exportfile.json");
        FileInputStream input = new FileInputStream(fileRaw);
        MultipartFile file = new MockMultipartFile("file",
                fileRaw.getName(), "text/plain", IOUtils.toByteArray(input));
        String jsonStringFromUploadedFile = jsonExportImportService.getJsonStringFromUploadedFile(file);
        List<ExportImportDto> exportImportDtos = null;
        try {
            exportImportDtos = jsonExportImportService.importFromJsonArray(jsonStringFromUploadedFile, ExportImportDto.class);
        } catch (Exception e) {
        }
        jsonExportImportService.importDataFromJson(exportImportDtos, 3);
    }
}