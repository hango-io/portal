package org.hango.cloud.common.infra.route.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.RouteRuleInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.CopyRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.impl.ServiceInfoServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings({"java:S1192"})
@RunWith(SpringRunner.class)
@SpringBootTest
public class RouteRuleInfoServiceImplTest extends BaseServiceImplTest {

  @Autowired // 定义被测试类的对象
  RouteRuleInfoServiceImpl routeRuleInfoService;
  @Autowired // service测试类
  ServiceInfoServiceImpl serviceInfoService;

  @Autowired
  RouteRuleInfoMapper routeRuleInfoMapper;
  @MockBean
  IRouteRuleProxyService routeRuleProxyService;


  @Before
  public void init(){
    super.init();
    mock();
    long serviceId = serviceInfoService.create(serviceDto);
    serviceDto.setId(serviceId);
    routeRuleDto.setServiceId(serviceId);

    long routeId = routeRuleInfoService.create(routeRuleDto);
    routeRuleDto.setId(routeId);
  }

  @After
  public void clear(){
    serviceInfoService.delete(serviceDto);
    routeRuleInfoService.delete(routeRuleDto);
  }

  private void mock(){
    MockitoAnnotations.openMocks(this);
    Mockito.when(routeRuleProxyService.getRouteRuleProxyByRouteRuleId(Mockito.anyInt())).thenReturn(Arrays.asList(new RouteRuleProxyDto()));
  }


  @Test
  public void update() {
    routeRuleDto.setDescription("update-route");
    routeRuleInfoService.update(routeRuleDto);
    assertEquals("update-route", routeRuleInfoService.get(routeRuleDto.getId()).getDescription());
  }

  @Test
  public void updatePublishStatus() {
    Long id = routeRuleDto.getId();
    assertEquals(0, routeRuleInfoService.get(id).getPublishStatus());
    routeRuleInfoService.updatePublishStatus(id, 1);
    assertEquals(1, routeRuleInfoService.get(id).getPublishStatus());
  }

  @Test
  public void delete() {
    routeRuleInfoService.delete(routeRuleDto);
    RouteRuleDto routeRule = routeRuleInfoService.get(routeRuleDto.getId());
    assertNull(routeRule);
  }


  @Test
  public void checkCreateParam() {
    Long id = routeRuleDto.getId();
    RouteRuleDto errorRoute = routeRuleInfoService.get(id);
    errorRoute.setId(null);
    //同名校验
    ErrorCode errorCode = routeRuleInfoService.checkCreateParam(errorRoute);
    assertEquals(errorCode.code, CommonErrorCode.SAME_NAME_ROUTE_RULE_EXIST.code);
    errorRoute.setRouteRuleName("create-route");
    //相同匹配条件校验
    errorCode = routeRuleInfoService.checkCreateParam(errorRoute);
    assertEquals(errorCode.code, CommonErrorCode.SAME_PARAM_ROUTE_RULE_EXIST.code);
    //method校验
    errorRoute.getMethodMatchDto().setValue(Collections.singletonList("POST1"));
    errorCode = routeRuleInfoService.checkCreateParam(errorRoute);
    assertEquals(errorCode.code, CommonErrorCode.ROUTE_RULE_METHOD_INVALID.code);
    errorRoute.getMethodMatchDto().setValue(Collections.singletonList("GET"));
    //uri path空校验
    errorRoute.getUriMatchDto().setValue(null);
    errorCode = routeRuleInfoService.checkCreateParam(errorRoute);
    assertEquals(errorCode.code, CommonErrorCode.NO_ROUTE_RULE_PATH.code);
    errorRoute.getUriMatchDto().setType(BaseConst.URI_TYPE_REGEX);
  }

  @Test
  public void checkUpdateParam() {
    Long id = routeRuleDto.getId();
    routeRuleDto.setId(-1L);
    ErrorCode errorCode = routeRuleInfoService.checkUpdateParam(routeRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.NO_SUCH_ROUTE_RULE.code);
   routeRuleDto.setId(id);
  }

  @Test
  public void checkDeleteParam() {
    routeRuleDto.setPublishStatus(1);
    ErrorCode errorCode = routeRuleInfoService.checkDeleteParam(routeRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED.code);

    routeRuleDto.setPublishStatus(0);
    assertEquals(errorCode.getCode(), CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED.code);
  }

  @Test
  public void checkCopyParam() {
    CopyRuleDto copyRuleDto = new CopyRuleDto();
    copyRuleDto.setRouteRuleId(-1);
    ErrorCode errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.NO_SUCH_ROUTE_RULE.code);
    copyRuleDto.setRouteRuleId(routeRuleDto.getId());
    copyRuleDto.setPriority(50);
    errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.NOT_MODIFY_PRIORITY.code);
    copyRuleDto.setPriority(10);
    copyRuleDto.setRouteRuleName(ROUTE_NAME);
    errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.NOT_MODIFY_ROUTE_RULE_NAME.code);
    copyRuleDto.setRouteRuleName("test-copy");
    errorCode = routeRuleInfoService.checkCopyParam(copyRuleDto);
    assertEquals(errorCode.getCode(), CommonErrorCode.NO_SUCH_SERVICE.code);
  }

  @Test
  public void getRouteRuleList() {
    Long id = routeRuleDto.getId();
    List<RouteRuleDto> routeRuleList = routeRuleInfoService.getRouteRuleList(Collections.singletonList(id));
    assertEquals(ROUTE_NAME, routeRuleList.get(0).getRouteRuleName());
    RouteRuleQuery query = RouteRuleQuery.builder().routeRuleIds(Collections.singletonList(id)).build();
    List<RouteRuleInfoPO> routeRuleInfoPOS = routeRuleInfoService.getRouteRuleList(query);
    assertEquals(ROUTE_NAME, routeRuleInfoPOS.get(0).getRouteRuleName());
  }

  @Test
  public void getRouteRulePage() {
    RouteRuleQueryDto queryDto = RouteRuleQueryDto.builder().routeRuleIds(Collections.singletonList(routeRuleDto.getId())).limit(100).offset(0).build();
    Page<RouteRuleInfoPO> page = routeRuleInfoService.getRouteRulePage(queryDto);
    assertEquals(1, page.getTotal());
    assertEquals(1, page.getCurrent());
    assertEquals(ROUTE_NAME, page.getRecords().get(0).getRouteRuleName());
  }


  @Test
  public void copyRouteRule(){
    ServiceDto targetService = new ServiceDto();
    String name = "test-copy-service";
    targetService.setServiceName(name);
    targetService.setDisplayName(name);
    targetService.setServiceType("http");
    long serviceId = serviceInfoService.create(targetService);
    CopyRuleDto copyRuleDto = new CopyRuleDto();
    copyRuleDto.setServiceId(serviceId);
    copyRuleDto.setRouteRuleName("test-copy");
    copyRuleDto.setPriority(10);
    copyRuleDto.setRouteRuleId(routeRuleDto.getId());
    long id = routeRuleInfoService.copyRouteRule(copyRuleDto);
    RouteRuleDto targetRoute = routeRuleInfoService.get(id);
    assertEquals("test-copy", targetRoute.getRouteRuleName());
    targetService.setId(serviceId);
    serviceInfoService.delete(targetService);
    routeRuleInfoMapper.deleteById(id);
  }
}