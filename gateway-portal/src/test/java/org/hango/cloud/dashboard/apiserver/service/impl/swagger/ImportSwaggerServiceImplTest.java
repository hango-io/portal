package org.hango.cloud.dashboard.apiserver.service.impl.swagger;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.gdashboard.api.service.swagger.ImportSwaggerService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ImportSwaggerServiceImplTest extends BaseServiceImplTest {

    @Autowired
    IServiceInfoService serviceInfoService;
    @Autowired
    private ImportSwaggerService importSwaggerService;
    private ServiceInfo serviceInfo;

    @PostConstruct
    public void init() {
        //初始化ServiceInfo
        serviceInfo = new ServiceInfo();
        serviceInfo.setDisplayName(displayName);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setContacts(user);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setServiceType(serviceType);
    }

    @Test
    @Rollback
    public void getSwaggerDetails() {
        long id = serviceInfoService.add(serviceInfo);
        Swagger swagger = new SwaggerParser().read("./swagger.json");
        Map<String, Object> swaggerDetails = importSwaggerService.getSwaggerDetails(swagger, id, serviceName);
        assertTrue(swaggerDetails.keySet().size() == 8);
    }

    @Test
    @Rollback
    public void insertSwagger() {
        long id = serviceInfoService.add(serviceInfo);
        Swagger swagger = new SwaggerParser().read("./swagger.json");
        boolean b = importSwaggerService.insertSwagger(swagger, id);
        assertTrue(b);
    }
}