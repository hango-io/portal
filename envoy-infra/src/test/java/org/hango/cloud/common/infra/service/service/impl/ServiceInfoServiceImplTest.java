//package org.hango.cloud.common.infra.service.service.impl;
//
//import org.hango.cloud.BaseServiceImplTest;
//import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
//import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
//import org.hango.cloud.common.infra.service.dto.ServiceDto;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.Assert.assertTrue;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class ServiceInfoServiceImplTest extends BaseServiceImplTest {
//
//  @Autowired // service测试类
//  ServiceInfoServiceImpl serviceInfoService;
//
//  @Before
//  public void init(){
//    super.init();
//    MockitoAnnotations.openMocks(this);
//  }
//
//  @After
//  public void tearDown(){
//    serviceInfoService.delete(serviceDto);
//  }
//
//
//  @Test
//  public void create() {
//    long id = serviceInfoService.create(serviceDto);
//    assertTrue(id > 0);
//    //覆盖@test get方法
//    assertTrue(SERVICE_NAME.equals(serviceInfoService.get(id).getServiceName()));
//  }
//
//  @Test
//  public void update() {
//    long id = serviceInfoService.create(serviceDto);
//    serviceDto.setId(id);
//    serviceDto.setServiceName("test-update");
//    serviceInfoService.update(serviceDto);
//    assertTrue("test-update".equals(serviceInfoService.get(id).getServiceName()));
//    // service_name 修改为原始值
//    serviceDto.setServiceName(SERVICE_NAME);
//  }
//
//  @Test
//  public void delete() {
//    long id = serviceInfoService.create(serviceDto);
//    serviceDto.setId(id);
//    serviceInfoService.delete(serviceDto);
//    assertTrue(serviceInfoService.get(id) == null);
//  }
//
//  @Test
//  public void findAll() {
//    serviceInfoService.create(serviceDto);
//    List<? extends ServiceDto> all = serviceInfoService.findAll();
//    assertTrue(SERVICE_NAME.equals(all.get(0).getServiceName()));
//  }
//
//  @Test
//  public void testFindAll() {
//    serviceInfoService.create(serviceDto);
//    List<? extends ServiceDto> all = serviceInfoService.findAll(0, 1);
//    assertTrue(SERVICE_NAME.equals(all.get(0).getServiceName()));
//  }
//
//  @Test
//  public void countAll() {
//    serviceInfoService.create(serviceDto);
//    long count = serviceInfoService.countAll();
//    assertTrue(count == 1);
//  }
//
//  @Test
//  public void checkCreateParam() {
//    ErrorCode errorCode = serviceInfoService.checkCreateParam(serviceDto);
//    assertTrue(errorCode.equals(CommonErrorCode.SUCCESS));
//    long id = serviceInfoService.create(serviceDto);
//    serviceDto.setServiceName(SERVICE_NAME);
//    ErrorCode errorCode1 = serviceInfoService.checkCreateParam(serviceDto);
//    assertTrue(errorCode1.equals(CommonErrorCode.SERVICE_NAME_ALREADY_EXIST));
//  }
//
//  @Test
//  public void checkUpdateParam() {
//    long id = serviceInfoService.create(serviceDto);
//    serviceDto.setServiceName(SERVICE_NAME);
//    serviceDto.setId(0);
//    ErrorCode errorCode = serviceInfoService.checkUpdateParam(serviceDto);
//    assertTrue(errorCode.equals(CommonErrorCode.NO_SUCH_SERVICE));
//    serviceDto.setId(id);
//    serviceDto.setServiceName(SERVICE_NAME);
//    ErrorCode errorCode1 = serviceInfoService.checkUpdateParam(serviceDto);
//    assertTrue(errorCode1.equals(CommonErrorCode.SUCCESS));
//  }
//
//  @Test
//  public void checkDeleteParam() {
//    serviceInfoService.create(serviceDto);
//    ErrorCode errorCode = serviceInfoService.checkDeleteParam(serviceDto);
//    assertTrue(errorCode.equals(CommonErrorCode.SUCCESS));
//  }
//
//  @Test
//  public void findAllServiceByProjectId() {
//    serviceInfoService.create(serviceDto);
//    List<ServiceDto> serviceList = serviceInfoService.findAllServiceByProjectId(0);
//    assertTrue(serviceList.get(0).getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void findAllServiceByProjectIdLimit() {
//    serviceInfoService.create(serviceDto);
//    List<ServiceDto> serviceList = serviceInfoService.findAllServiceByProjectIdLimit("test", 0, 1, 0);
//    assertTrue(serviceList.get(0).getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void findAllServiceByDisplayName() {
//    serviceInfoService.create(serviceDto);
//    List<ServiceDto> serviceList = serviceInfoService.findAllServiceByDisplayName("test", 0, 0);
//    assertTrue(serviceList.get(0).getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void getServiceCountByProjectId() {
//    long id = serviceInfoService.create(serviceDto);
//    long count = serviceInfoService.getServiceCountByProjectId("test", 0);
//    assertTrue(count == 1);
//  }
//
//  @Test
//  public void getServiceByServiceName() {
//    serviceInfoService.create(serviceDto);
//    ServiceDto service = serviceInfoService.getServiceByServiceName(SERVICE_NAME);
//    assertTrue(service.getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void getServiceByServiceNameAndProject() {
//    serviceInfoService.create(serviceDto);
//    ServiceDto service = serviceInfoService
//        .getServiceByServiceNameAndProject(SERVICE_NAME, 0);
//    assertTrue(service.getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void isDisplayNameExists() {
//    serviceInfoService.create(serviceDto);
//    assertTrue(serviceInfoService.isDisplayNameExists(SERVICE_NAME));
//  }
//
//  @Test
//  public void describeDisplayName() {
//    serviceInfoService.create(serviceDto);
//    ServiceDto serviceDto = serviceInfoService.describeDisplayName(SERVICE_NAME, 0);
//    assertTrue(serviceDto.getServiceName().equals(SERVICE_NAME));
//  }
//
//  @Test
//  public void getServiceIdListByDisplayNameFuzzy() {
//    long id = serviceInfoService.create(serviceDto);
//    List<Long> ids = serviceInfoService.getServiceIdListByDisplayNameFuzzy("test", 0);
//    assertTrue(ids.get(0) == id);
//  }
//
//  @Test
//  public void getServiceDtoList() {
//    long id = serviceInfoService.create(serviceDto);
//    List<ServiceDto> serviceList = serviceInfoService.getServiceDtoList(Arrays.asList(id));
//    assertTrue(serviceList.get(0).getServiceName().equals(SERVICE_NAME));
//  }
//}