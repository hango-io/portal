package org.hango.cloud;

import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dao.IPluginTemplateDao;
import org.hango.cloud.common.infra.plugin.service.impl.PluginTemplateServiceImpl;
import org.hango.cloud.envoy.infra.pluginmanager.service.impl.PluginManagerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * 公共mock数据初始化，测试类继承该类可使用共享mock数据，主要包括以下2部分
 * 1.各流程（服务、路由、插件等）内存对象
 * 2.个流程数据库数据
 *
 * 使用方法
 * 1.继承该类
 * 2.Before阶段调用该类的init方法（各方法执行前都会执行一次init）
 */
@SuppressWarnings({ "java:S1226"})
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

  // 插件模块mock


//  public void init() {
//    Mockito.when(pluginManagerService.publishPluginManager(Mockito.any())).thenReturn(true);
//    Mockito.when(pluginManagerService.offlinePluginManager(Mockito.any())).thenReturn(true);
//  }




}
