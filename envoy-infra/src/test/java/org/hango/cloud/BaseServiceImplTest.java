package org.hango.cloud;

import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.impl.PluginTemplateServiceImpl;
import org.hango.cloud.common.infra.route.common.RouteRuleMapMatchDto;
import org.hango.cloud.common.infra.route.dto.DestinationDto;
import org.hango.cloud.common.infra.route.dto.HttpRetryDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.pluginmanager.service.impl.PluginManagerServiceImpl;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hango.cloud.common.infra.base.meta.BaseConst.DYNAMIC_PUBLISH_TYPE;

/**
 * 公共mock数据初始化，测试类继承该类可使用共享mock数据，主要包括以下2部分
 * 1.各流程（服务、路由、插件等）内存对象
 * 2.个流程数据库数据
 *
 * 使用方法
 * 1.继承该类
 * 2.Before阶段调用该类的init方法（各方法执行前都会执行一次init）
 */
public class BaseServiceImplTest {
  @Autowired
  IPluginBindingInfoDao pluginBindingInfoDao;
  @Autowired
  IPluginTemplateDao pluginTemplateDao;
  @Autowired
  private IGatewayService gatewayService;
  @Autowired
  private PluginTemplateServiceImpl pluginTemplateService;
  @MockBean
  private PluginManagerServiceImpl pluginManagerService;

  /****************************** 共享对象（继承此类的单元测试类公用） ******************************/
  public  RouteRuleDto routeRuleDto;
  public  ServiceDto serviceDto;
  public  VirtualGatewayDto virtualGatewayDto;
  public  ServiceProxyDto serviceProxyDto;
  public  RouteRuleProxyDto routeRuleProxyDto;
  public PluginBindingInfo pluginBindingInfo;
  public PluginTemplateDto templateInfo;
  public GatewayDto gatewayDto;
  public CopyGlobalPluginDto copyGlobalPluginDto;
  public BindingPluginDto bindingPluginDto;
  public PluginBindingDto pluginBindingDto;
  public BindingPluginDto bindingPlugin;

  /****************************** 参数 ******************************/
  public static final String SERVICE_NAME = "test-service";
  public static final String ROUTE_NAME = "test-route";
  public static final String EXACT_MATCH_TYPE = "exact";
  // 以下为虚拟网关mock参数
  public static final Long VIRTUAL_GW_ID = 1L;
  public static final Integer GW_ID = 1;
  public static final String VIRTUAL_GW_NAME = "test-virtual-gw";
  public static final String VIRTUAL_GW_CODE = VIRTUAL_GW_NAME;
  public static final String VIRTUAL_GW_TYPE = "NetworkProxy";
  public static final String GW_TYPE = "envoy";
  public static final String VIRTUAL_GW_PROTOCOL = "HTTP";
  // 以下参数为插件绑定信息mock参数
  public static final Long PLUGIN_ID = 1L;
  public static final String PLUGIN_TYPE = "ip-restriction";
  public static final String PLUGIN_BINDING_OBJ_TYPE = "global";
  public static final String PLUGIN_BINDING_OBJ_ID = "1";
  public static final String PLUGIN_STATUS = "enable";
  public static final Long PLUGIN_TEMPLATE_ID = 1L;
  public static final String PLUGIN_CONFIG = "{\"kind\":\"ip-restriction\",\"type\":\"0\",\"list\":[\"1.1.1.1\",\"3.3.3.3\"]}";
  public static final Long PROJECT_ID = 3L;
  // 插件模块mock


  public void init() {
    Mockito.when(pluginManagerService.publishPluginManager(Mockito.any())).thenReturn(true);
    Mockito.when(pluginManagerService.offlinePluginManager(Mockito.any())).thenReturn(true);

    initVirtualGateway();
    initService();
    initServiceProxy();
    initRoute();
    initRouteProxy(routeRuleDto);
    initPluginBindingInfo();
    initCopyGlobalPluginDto();
    initBindingPluginDto();
    initPluginBindingDto();
    initGatewayDto();
    initBindingPlugin();
    initTemplateInfo();

    // 存缓存数据库
    if (gatewayService.get(1) == null) {
      gatewayService.create(gatewayDto);
    }

    if (pluginBindingInfoDao.get(1) == null) {
      pluginBindingInfoDao.add(pluginBindingInfo);
    }
    if (pluginTemplateDao.get(1) == null) {
      pluginTemplateDao.add(pluginTemplateService.toMeta(templateInfo));
    }
  }

  private void initTemplateInfo() {
    templateInfo = new PluginTemplateDto();
    templateInfo.setId(1);
    templateInfo.setCreateTime(10000L);
    templateInfo.setUpdateTime(10000L);
    templateInfo.setPluginType(PLUGIN_TYPE);
    templateInfo.setPluginConfiguration(PLUGIN_CONFIG);
    templateInfo.setProjectId(PROJECT_ID);
    templateInfo.setTemplateVersion(1);
    templateInfo.setTemplateNotes("-");
    templateInfo.setTemplateName("test-plugin-template");
  }

  private void initBindingPlugin() {
    bindingPlugin = new BindingPluginDto();
    bindingPlugin.setVirtualGwId(VIRTUAL_GW_ID);
    bindingPlugin.setPluginType(PLUGIN_TYPE);
    bindingPlugin.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
    bindingPlugin.setBindingObjectId(Long.parseLong(PLUGIN_BINDING_OBJ_ID));
  }

  private void initGatewayDto() {
    // mock物理网关对象
    gatewayDto = new GatewayDto();
    gatewayDto.setId(1);
    gatewayDto.setName("test-gw");
    gatewayDto.setEnvId("-");
    gatewayDto.setSvcName("-");
    gatewayDto.setSvcType("-");
    gatewayDto.setType("-");
    gatewayDto.setGwClusterName("-");
    gatewayDto.setConfAddr("-");
  }

  private void initPluginBindingDto() {
    // mock插件信息
    pluginBindingDto = new PluginBindingDto();
    pluginBindingDto.setPluginType(PLUGIN_TYPE);
    pluginBindingDto.setPluginConfiguration(PLUGIN_CONFIG);
    pluginBindingDto.setBindingObjectId(PLUGIN_BINDING_OBJ_ID);
    pluginBindingDto.setCreateTime(100000L);
    pluginBindingDto.setUpdateTime(100000L);
    pluginBindingDto.setProjectId(PROJECT_ID);
    pluginBindingDto.setBindingStatus(PLUGIN_STATUS);
    pluginBindingDto.setVirtualGwId(VIRTUAL_GW_ID);
    pluginBindingDto.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
    pluginBindingDto.setGwType(GW_TYPE);
  }

  private void initBindingPluginDto() {
    // mock插件信息
    bindingPluginDto = new BindingPluginDto();
    bindingPluginDto.setBindingObjectId(Long.parseLong(PLUGIN_BINDING_OBJ_ID));
    bindingPluginDto.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
    bindingPluginDto.setVirtualGwId(VIRTUAL_GW_ID);
    bindingPluginDto.setPluginType(PLUGIN_TYPE);
  }

  private void initCopyGlobalPluginDto() {
    // mock拷贝全局插件对象
    copyGlobalPluginDto = new CopyGlobalPluginDto();
    copyGlobalPluginDto.setVirtualGwId(VIRTUAL_GW_ID);
    copyGlobalPluginDto.setPluginId(PLUGIN_ID);
    copyGlobalPluginDto.setProjectId(PROJECT_ID);
  }

  private void initPluginBindingInfo() {
    pluginBindingInfo = new PluginBindingInfo();
    pluginBindingInfo.setId(PLUGIN_ID);
    pluginBindingInfo.setTemplateId(PLUGIN_TEMPLATE_ID);
    pluginBindingInfo.setPluginType(PLUGIN_TYPE);
    pluginBindingInfo.setBindingStatus(PLUGIN_STATUS);
    pluginBindingInfo.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
    pluginBindingInfo.setBindingObjectId(PLUGIN_BINDING_OBJ_ID);
    pluginBindingInfo.setPluginConfiguration(PLUGIN_CONFIG);
    pluginBindingInfo.setGwType(GW_TYPE);
    pluginBindingInfo.setProjectId(PROJECT_ID);
    pluginBindingInfo.setCreateTime(1000000L);
    pluginBindingInfo.setUpdateTime(1000000L);
    pluginBindingInfo.setVirtualGwId(VIRTUAL_GW_ID);
  }

  private void initService(){
    //服务信息
    serviceDto = new ServiceDto();
    serviceDto.setServiceName(SERVICE_NAME);
    serviceDto.setDisplayName(SERVICE_NAME);
    serviceDto.setServiceType("http");
  }

  private void initServiceProxy(){
    serviceProxyDto = new ServiceProxyDto();
    serviceProxyDto.setServiceName(SERVICE_NAME);
    serviceProxyDto.setServiceTag(SERVICE_NAME);
    serviceProxyDto.setCode(SERVICE_NAME);
    serviceProxyDto.setVirtualGwId(1);
    serviceProxyDto.setPublishType(DYNAMIC_PUBLISH_TYPE);
    serviceProxyDto.setBackendService("istio-e2e.apigw-demo.cluster.svc.local");
  }


  private void initRoute(){
    //路由信息
    routeRuleDto = new RouteRuleDto();
    //构造路由规则
    routeRuleDto.setRouteRuleName(ROUTE_NAME);
    routeRuleDto.setDescription(ROUTE_NAME);

    RouteRuleMapMatchDto headers = new RouteRuleMapMatchDto();
    headers.setKey("abc");
    headers.setType(EXACT_MATCH_TYPE);
    headers.setValue(asList("abc"));
    routeRuleDto.setHeaders(asList(headers));

    RouteRuleMapMatchDto querys = new RouteRuleMapMatchDto();
    querys.setKey("aaa");
    querys.setType(EXACT_MATCH_TYPE);
    querys.setValue(asList("caa"));
    routeRuleDto.setQueryParams(asList(querys));

    RouteStringMatchDto host = new RouteStringMatchDto();
    host.setType(EXACT_MATCH_TYPE);
    host.setValue(asList("abc.com"));
    routeRuleDto.setHostMatchDto(host);

    RouteStringMatchDto method = new RouteStringMatchDto();
    method.setType(EXACT_MATCH_TYPE);
    method.setValue(Collections.singletonList("GET"));
    routeRuleDto.setMethodMatchDto(method);

    RouteStringMatchDto uri = new RouteStringMatchDto();
    uri.setType(EXACT_MATCH_TYPE);
    uri.setValue(Collections.singletonList("/abc"));
    routeRuleDto.setUriMatchDto(uri);

    routeRuleDto.setPriority(50);
  }

  public void initRouteProxy(RouteRuleDto routeRuleDto){
    routeRuleProxyDto = new RouteRuleProxyDto();
    routeRuleProxyDto.setVirtualGwId(1L);
    routeRuleProxyDto.setEnableState(PLUGIN_STATUS);
    HttpRetryDto httpRetryDto = new HttpRetryDto();
    httpRetryDto.setRetry(false);
    httpRetryDto.setRetryOn("");
    httpRetryDto.setAttempts(2);
    httpRetryDto.setPerTryTimeout(60000);
    routeRuleProxyDto.setHttpRetryDto(httpRetryDto);
    routeRuleProxyDto.setHeaders(routeRuleDto.getHeaders());
    routeRuleProxyDto.setQueryParams(routeRuleDto.getQueryParams());
    routeRuleProxyDto.setHostMatchDto(routeRuleDto.getHostMatchDto());
    routeRuleProxyDto.setMethodMatchDto(routeRuleDto.getMethodMatchDto());
    routeRuleProxyDto.setUriMatchDto(routeRuleDto.getUriMatchDto());
    DestinationDto destinationDto = new DestinationDto();
    destinationDto.setPort(80);
    destinationDto.setWeight(100);
    routeRuleProxyDto.setDestinationServices(Collections.singletonList(destinationDto));
  }

  private void initVirtualGateway(){
    virtualGatewayDto = new VirtualGatewayDto();
    virtualGatewayDto.setConfAddr("http://127.0.0.1");
    virtualGatewayDto.setId(VIRTUAL_GW_ID);
    virtualGatewayDto.setGwId(GW_ID);
    virtualGatewayDto.setGwName("gateway");
    virtualGatewayDto.setGwClusterName("test-gateway");
    virtualGatewayDto.setName(VIRTUAL_GW_NAME);
    virtualGatewayDto.setCode(VIRTUAL_GW_CODE);
    virtualGatewayDto.setType(VIRTUAL_GW_TYPE);
    virtualGatewayDto.setProtocol(VIRTUAL_GW_PROTOCOL);
  }
}
