package org.hango.cloud;

import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.dto.RouteMapMatchDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.junit.jupiter.api.BeforeAll;

import java.util.Arrays;

@SuppressWarnings({"java:S1192"})
public class BaseServiceImplTest {
  public static RouteDto routeDto;
  public static ServiceProxyDto serviceDto;

  public static final String SERVICE_NAME = "test-service";
  public static final String ROUTE_NAME = "test-route";
  public static final String EXACT = "exact";

  @BeforeAll
  public static void setUpBeforeClass() {
    //服务信息
    serviceDto = new ServiceProxyDto();
    serviceDto.setName(SERVICE_NAME);
    serviceDto.setAlias(SERVICE_NAME);
    serviceDto.setProtocol("http");

    //路由信息
    routeDto = new RouteDto();
    //构造路由规则
    routeDto.setName(ROUTE_NAME);
    routeDto.setDescription(ROUTE_NAME);

    RouteMapMatchDto headers = new RouteMapMatchDto();
    headers.setKey("abc");
    headers.setType(EXACT);
    headers.setValue(Arrays.asList(new String[]{"abc"}));
    routeDto.setHeaders(Arrays.asList(new RouteMapMatchDto[]{headers}));

    RouteMapMatchDto querys = new RouteMapMatchDto();
    querys.setKey("aaa");
    querys.setType(EXACT);
    querys.setValue(Arrays.asList(new String[]{"caa"}));
    routeDto.setQueryParams(Arrays.asList(new RouteMapMatchDto[]{querys}));

    RouteStringMatchDto host = new RouteStringMatchDto();
    host.setType(EXACT);
    host.setValue(Arrays.asList(new String[]{"abc.com"}));
//    routeDto.setHostMatchDto(host);

    routeDto.setMethod(Arrays.asList("GET"));

    RouteStringMatchDto uri = new RouteStringMatchDto();
    uri.setType(EXACT);
    uri.setValue(Arrays.asList(new String[]{"/abc"}));
    routeDto.setUriMatchDto(uri);

    routeDto.setPriority(50);
  }
}
