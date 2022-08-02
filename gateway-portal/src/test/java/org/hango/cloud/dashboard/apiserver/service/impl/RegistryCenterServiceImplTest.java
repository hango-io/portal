package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.BaseServiceImplTest;
import org.hango.cloud.dashboard.apiserver.dto.RegistryCenterDto;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.ServiceType;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author xin li
 * @date 2022/8/16 09:23
 */
public class RegistryCenterServiceImplTest extends BaseServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(RegistryCenterServiceImplTest.class);

    @Autowired
    private IRegistryCenterService registryCenterService;

    @Autowired
    private List<RegistryCenterDto> registryCenterDtoList;

    @PostConstruct
    public void init() {
        registryCenterDtoList = registryCenterService.findAll();
        registryCenterDtoList.stream().map(RegistryCenterDto::getId).forEach(registryCenterService::deleteRegistryCenter);
    }

    public void testDescribeRegistryTypesByServiceType() {
        List<String> httpRegistryList = registryCenterService.describeRegistryTypesByServiceType(ServiceType.http.name());
        Assert.assertEquals(1, httpRegistryList.size());
        Assert.assertEquals(RegistryCenterEnum.Kubernetes.getType(), httpRegistryList.get(0));
        List<String> dubboRegistryList = registryCenterService.describeRegistryTypesByServiceType(ServiceType.dubbo.name());
        Assert.assertEquals(0, dubboRegistryList.size());
        List<String> webserviceRegistryList = registryCenterService.describeRegistryTypesByServiceType(ServiceType.webservice.name());
        Assert.assertEquals(1, webserviceRegistryList.size());
        Assert.assertEquals(RegistryCenterEnum.Kubernetes.getType(), webserviceRegistryList.get(0));
        List<String> grpcRegistryList = registryCenterService.describeRegistryTypesByServiceType(ServiceType.grpc.name());
        Assert.assertEquals(0, grpcRegistryList.size());
    }

    @PreDestroy
    public void destroy() {
        if (!CollectionUtils.isEmpty(registryCenterDtoList)) {
            registryCenterDtoList.forEach(registryCenterService::saveRegistryCenter);
        }
    }
}
