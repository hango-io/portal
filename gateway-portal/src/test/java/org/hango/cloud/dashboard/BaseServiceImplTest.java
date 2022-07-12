package org.hango.cloud.dashboard;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = NceGdashboardApplication.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource("classpath:unit-test.properties")
public abstract class BaseServiceImplTest {
    public static String health_interface = "/ngw?Action=Health&Version=2017-11-16";
    @Value("${projectId}")
    public long projectId;
    @Value("${gwAddr}")
    public String gwAddr;
    @Value("${tenantId}")
    public long tenantId;
    @Value("${tokenId}")
    public String tokenId;
    @Value("${accountId}")
    public String accountId;
    @Value("${gwUniId}")
    public String gwUniId;
    @Value("${gwNameReal}")
    public String gwNameReal;
    @Value("${envId}")
    public String envId;
    @Value("${serviceAuthAddr}")
    public String serviceAuthAddr;
    public String serviceName = "serviceNameUnitTest";
    public String displayName = "displayNameUnitTest";
    public String user = "admin";
    public String serviceType = "http";
    public String gwName = "testUnit";
    public String routeName = "testRouteUnit";
    public String description = "desc";
    public String envoyGwName = "envoy";

}
