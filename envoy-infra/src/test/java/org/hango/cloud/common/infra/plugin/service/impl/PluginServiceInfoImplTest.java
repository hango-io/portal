package org.hango.cloud.common.infra.plugin.service.impl;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.plugin.service.impl.EnvoyPluginInfoServiceImpl;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"java:S1192"})
@SpringBootTest
public class PluginServiceInfoImplTest{
    @Autowired
    private PluginServiceInfoImpl pluginServiceInfo;
    @Autowired
    private VirtualGatewayServiceImpl virtualGatewayService;
    @MockBean
    private EnvoyPluginInfoServiceImpl envoyPluginInfoService;
    @Autowired
    private VirtualGatewayServiceImpl virtualGatewayInfoService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IDomainInfoService domainInfoService;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyPluginInfoService.updateGatewayPlugin(Mockito.any(BindingPluginDto.class), Mockito.anyLong())).thenReturn(true);
        Mockito.when(envoyPluginInfoService.publishGatewayPlugin(Mockito.any())).thenReturn(true);
        Mockito.when(envoyPluginInfoService.deleteGatewayPlugin(Mockito.any(BindingPluginDto.class), Mockito.anyLong())).thenReturn(true);
    }

    @Test
    public void checkDescribePlugin() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        virtualGatewayDto.setId(1L);
        long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
        ErrorCode errorCode = pluginServiceInfo.checkDescribePlugin(99L);
        assertEquals(errorCode.message, "指定的网关不存在");

        errorCode = pluginServiceInfo.checkDescribePlugin(vgId);
        assertEquals(errorCode.message, "当前网关未绑定域名，不允许发布");

        virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
    }


    @Test
    public void checkUnbindParam() {
        VirtualGatewayDto virtualGatewayDto = preCreate();
        long vgId = virtualGatewayDto.getId();
        PluginBindingDto pluginBindingDto = MockUtil.initProjectPluginBindingDto(vgId);
        long id = pluginServiceInfo.create(pluginBindingDto);
        pluginBindingDto.setId(id);

        ErrorCode errorCode = pluginServiceInfo.checkUnbindParam(99L);
        assertEquals(errorCode.message, "指定的插件绑定关系不存在");

        pluginServiceInfo.delete(pluginBindingDto);
        postDelete(virtualGatewayDto);
    }

    @Test
    public void checkCopyGlobalPluginToGateway() {
        VirtualGatewayDto virtualGatewayDto = preCreate();
        PluginBindingDto pluginBindingDto = MockUtil.initProjectPluginBindingDto(virtualGatewayDto.getId());
        long id = pluginServiceInfo.create(pluginBindingDto);
        CopyGlobalPluginDto copyGlobalPluginDto = new CopyGlobalPluginDto();
        copyGlobalPluginDto.setVirtualGwId(99L);
        copyGlobalPluginDto.setPluginId(99L);
        ErrorCode errorCode = pluginServiceInfo.checkCopyGlobalPluginToGateway(copyGlobalPluginDto);
        assertEquals(errorCode.message, "指定的网关不存在");

        copyGlobalPluginDto.setVirtualGwId(virtualGatewayDto.getId());
        errorCode = pluginServiceInfo.checkCopyGlobalPluginToGateway(copyGlobalPluginDto);
        assertEquals(errorCode.message, "指定的插件绑定关系不存在");


        copyGlobalPluginDto.setPluginId(id);
        errorCode = pluginServiceInfo.checkCopyGlobalPluginToGateway(copyGlobalPluginDto);
        assertEquals(errorCode.message, "处理成功");

        pluginServiceInfo.delete(pluginBindingDto);
        postDelete(virtualGatewayDto);
    }

    @Test
    public void checkCreateParam() {
        VirtualGatewayDto virtualGatewayDto = preCreate();
        PluginBindingDto pluginBindingDto = MockUtil.initProjectPluginBindingDto(virtualGatewayDto.getId());
        long id = pluginServiceInfo.create(pluginBindingDto);

        PluginBindingDto checkDto = new PluginBindingDto();
        checkDto.setVirtualGwId(99L);
        checkDto.setBindingObjectId("99");

        ErrorCode errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.message, "指定的网关不存在");

        checkDto.setVirtualGwId(virtualGatewayDto.getId());
        checkDto.setBindingObjectType(BINDING_OBJECT_TYPE_ROUTE_RULE);
        errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.message, "路由规则未发布");

        checkDto.setBindingObjectType(BINDING_OBJECT_TYPE_GLOBAL);
        errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.message, "当前网关未绑定域名，不允许发布");

        checkDto.setBindingObjectType(BINDING_OBJECT_TYPE_HOST);
        errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.getCode(), "InvalidParameter");

        checkDto.setBindingObjectType(BINDING_OBJECT_TYPE_SERVICE);
        checkDto.setVirtualGwId(virtualGatewayDto.getId());
        errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.message, "参数  PluginType 缺失");

        checkDto.setTemplateId(99L);
        checkDto.setPluginType(pluginBindingDto.getPluginType());
        errorCode = pluginServiceInfo.checkCreateParam(checkDto);
        assertEquals(errorCode.message, "指定的插件模板不存在");


        pluginServiceInfo.delete(pluginBindingDto);
        postDelete(virtualGatewayDto);
    }

    @Test
    public void getBindingInfoList() {
        VirtualGatewayDto virtualGatewayDto = preCreate();
        PluginBindingDto pluginBindingDto = MockUtil.initProjectPluginBindingDto(virtualGatewayDto.getId());
        long id = pluginServiceInfo.create(pluginBindingDto);

        BindingPluginDto bindingPluginDto = new BindingPluginDto();
        bindingPluginDto.setVirtualGwId(virtualGatewayDto.getId());
        bindingPluginDto.setBindingObjectId(Long.valueOf(pluginBindingDto.getBindingObjectId()));
        bindingPluginDto.setPluginType(pluginBindingDto.getPluginType());
        bindingPluginDto.setBindingObjectType(pluginBindingDto.getBindingObjectType());

        PluginBindingInfo plugin = pluginServiceInfo.getBindingInfo(bindingPluginDto);
        assertEquals(plugin.getPluginType(), pluginBindingDto.getPluginType());

        pluginServiceInfo.delete(pluginBindingDto);
        postDelete(virtualGatewayDto);
    }

     private VirtualGatewayDto preCreate(){
         DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
         domainInfoService.create(domainInfoDTO);
         GatewayDto gatewayDto = MockUtil.initGatewayDto();
         long gwId = gatewayService.create(gatewayDto);
         VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
         virtualGatewayDto.setDomainInfos(Collections.singletonList(domainInfoDTO));

         virtualGatewayDto.setGwId(gwId);
         long vgId = virtualGatewayService.createWithoutHooker(virtualGatewayDto);
         return virtualGatewayDto;
     }

     private void postDelete(VirtualGatewayDto virtualGatewayDto){
         List<DomainInfoDTO> domainInfos = virtualGatewayDto.getDomainInfos();
         if (domainInfos != null){
             for (DomainInfoDTO domainInfo : domainInfos) {
                 domainInfoService.delete(domainInfo);
             }
         }
         virtualGatewayService.deleteWithoutHooker(virtualGatewayDto);
         long gwId = virtualGatewayDto.getGwId();
         GatewayDto gatewayDto = gatewayService.get(gwId);
         if (gatewayDto != null){
             gatewayService.delete(gatewayDto);
         }

     }
}