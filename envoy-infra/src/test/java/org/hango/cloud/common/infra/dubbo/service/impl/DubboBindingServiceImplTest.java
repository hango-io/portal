package org.hango.cloud.common.infra.dubbo.service.impl;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.impl.RouteServiceImpl;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.route.service.IEnvoyRouteService;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

/**
 * @Author zhufengwei
 * @Date 2023/7/20
 */
@SuppressWarnings({"java:S1192"})
@SpringBootTest
public class DubboBindingServiceImplTest {
    @Autowired
    IDubboBindingService dubboBindingService;

    @MockBean
    IEnvoyRouteService envoyRouteService;

    @MockBean
    RouteServiceImpl routeService;

    @MockBean
    IServiceProxyService serviceProxyService;




    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyRouteService.publishRoute(Mockito.any(RouteDto.class))).thenReturn(true);
        Mockito.when(envoyRouteService.deleteRoute(Mockito.any(RouteDto.class))).thenReturn(true);
        Mockito.when(envoyRouteService.updateRoute(Mockito.any(RouteDto.class))).thenReturn(1L);
        Mockito.when(routeService.get(Mockito.anyLong())).thenReturn(MockUtil.initRoute(1L));
        Mockito.when(serviceProxyService.get(Mockito.anyLong())).thenReturn(MockUtil.initDubboService());

    }

    @Test
    public void create() {
        DubboBindingDto dubboBindingDto = MockUtil.initDubboBindingDto();
        long id = dubboBindingService.create(dubboBindingDto);

        DubboBindingDto dbInfo = dubboBindingService.get(id);
        Assertions.assertEquals(dbInfo.getMethod(), "echoStrAndInt");

        dbInfo.setMethod("echoStr");
        dubboBindingService.update(dbInfo);

        dbInfo = dubboBindingService.get(id);
        Assertions.assertEquals(dbInfo.getMethod(), "echoStr");

        dubboBindingService.deleteDubboInfo(dbInfo.getObjectId(), dbInfo.getObjectType());
        dbInfo = dubboBindingService.get(id);
        Assertions.assertNull(dbInfo);
    }
    @Test
    public void checkAndComplete() {
        DubboBindingDto dubboBindingDto = MockUtil.initDubboBindingDto();
        DubboBindingDto.DubboParam correctParam = dubboBindingDto.getParams().get(0);
        DubboBindingDto.DubboParam checkParam = new DubboBindingDto.DubboParam();
        dubboBindingDto.setParams(Collections.singletonList(checkParam));
        //校验默认值
        ErrorCode errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "参数为空");

        checkParam.setKey(correctParam.getKey());
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "参数为空");

        checkParam.setValue("org.hango.cloud.envoy.infra.pluginmanager.service.impl.PluginManagerServiceImpl");
        checkParam.setDefaultValue("aaa");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "默认值类型不支持");

        checkParam.setValue("int");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "默认值配置错误");

        checkParam.setValue("java.lang.String");
        checkParam.setDefaultValue(true);
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "默认值配置错误");

        checkParam.setValue("java.util.Set");
        checkParam.setDefaultValue("aaa");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "默认值配置错误");

        checkParam.setValue("java.util.Map");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "默认值配置错误");

        checkParam.setValue("java.lang.String");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "处理成功");

        //校验dubbo attahment
        DubboBindingDto.DubboAttachmentDto correctDubboAttachment = dubboBindingDto.getDubboAttachment().get(0);
        DubboBindingDto.DubboAttachmentDto checkDubboAttachment = new DubboBindingDto.DubboAttachmentDto();
        dubboBindingDto.setDubboAttachment(Collections.singletonList(checkDubboAttachment));
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "隐式参数配置错误");

        checkDubboAttachment.setParamPosition("QUERY");
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "隐式参数配置错误");

        checkDubboAttachment.setParamPosition(correctDubboAttachment.getParamPosition());
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "隐式参数配置错误");

        checkDubboAttachment.setClientParamName(correctDubboAttachment.getClientParamName());
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "隐式参数配置错误");

        checkDubboAttachment.setServerParamName(correctDubboAttachment.getServerParamName());
        errorCode = dubboBindingService.checkAndComplete(dubboBindingDto);
        Assertions.assertEquals(errorCode.message, "处理成功");

    }

}
