package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.ServiceInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.BeanUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ServiceInfoImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInfoImplTest.class);

    @Autowired
    IServiceInfoService serviceInfoService;

    private ServiceInfo serviceInfo;
    private ServiceInfoDto serviceInfoDto;

    @PostConstruct
    public void init() {
        //初始化ServiceInfo
        serviceInfo = new ServiceInfo();
        serviceInfo.setDisplayName(displayName);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setContacts(user);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setServiceType(serviceType);
        //初始化ServiceInfoBasicDto
        serviceInfoDto = BeanUtil.copy(serviceInfo, ServiceInfoDto.class);
    }

    @Test
    @Rollback
    public void add() {
        long id = serviceInfoService.add(serviceInfo);
        assertTrue(serviceInfoService.getServiceByServiceId(id).getServiceName().equals(serviceName));
    }

    @Test
    @Rollback
    public void addServiceInfo() {
        ServiceInfo addServiceInfo = serviceInfoService.addServiceInfo(serviceInfoDto, projectId);
        assertTrue(addServiceInfo.getServiceName().equals(serviceName));
    }

    @Test
    @Rollback
    public void findAllServiceByProjectId() {
        List<ServiceInfo> allServiceByProjectId = serviceInfoService.findAllServiceByProjectId(projectId);
        serviceInfoService.add(serviceInfo);
        List<ServiceInfo> allServiceByProjectId1 = serviceInfoService.findAllServiceByProjectId(projectId);
        assertTrue((allServiceByProjectId1.size() - allServiceByProjectId.size()) == 1);
    }

    @Test
    @Rollback
    public void findAllServiceByProjectIdLimit() {
        serviceInfoService.add(serviceInfo);
        List<ServiceInfo> allServiceByProjectIdLimit = serviceInfoService.findAllServiceByProjectIdLimit(serviceName.trim(), 0, 20, projectId);
        assertTrue(allServiceByProjectIdLimit.size() == 1);
    }

    @Test
    @Rollback
    public void isServiceExists() {
        long id = serviceInfoService.add(serviceInfo);
        assertTrue(serviceInfoService.isServiceExists(id));
    }

    @Test
    @Rollback
    public void getServiceByServiceName() {
        serviceInfoService.add(serviceInfo);
        ServiceInfo serviceByServiceName = serviceInfoService.getServiceByServiceName(serviceName);
        assertTrue(serviceByServiceName.getDisplayName().equals(displayName));
    }

    @Test
    @Rollback
    public void getServiceByServiceNameAndProject() {
        serviceInfoService.add(serviceInfo);
        ServiceInfo serviceByServiceNameAndProject = serviceInfoService.getServiceByServiceNameAndProject(serviceName, projectId);
        assertTrue(serviceByServiceNameAndProject.getDisplayName().equals(displayName));
    }

    @Test
    public void checkCreateServiceParam() {
        ErrorCode errorCode = serviceInfoService.checkCreateServiceParam(serviceInfoDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }

    @Test
    @Rollback
    public void checkUpdateServiceParam() {
        long id = serviceInfoService.add(serviceInfo);
        serviceInfoDto.setId(id);
        ErrorCode errorCode = serviceInfoService.checkUpdateServiceParam(serviceInfoDto);
        assertTrue(errorCode.getCode().equals(CommonErrorCode.Success.getCode()));
    }


    @Test
    @Rollback
    public void delete() {
        long id = serviceInfoService.add(serviceInfo);
        serviceInfoService.delete(id);
    }

}