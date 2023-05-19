package org.hango.cloud.common.infra.plugin.service.impl;

import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.plugin.service.impl.EnvoyPluginInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SuppressWarnings("java:S2699")
@SpringBootTest
public class PluginTemplateServiceImplTest extends BaseServiceImplTest {
    @Autowired
    private PluginTemplateServiceImpl pluginTemplateService;
    @MockBean
    private EnvoyPluginInfoServiceImpl envoyPluginInfoService;
    @Autowired
    private PluginServiceInfoImpl pluginServiceInfo;
    @Autowired
    private VirtualGatewayServiceImpl virtualGatewayInfoService;


    public PluginTemplateServiceImplTest() {
        // 空参构造方法
    }

    @BeforeEach
    public void init() {
        super.init();
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoyPluginInfoService.updateGatewayPlugin(Mockito.any(BindingPluginDto.class), Mockito.anyLong())).thenReturn(true);
        Mockito.when(envoyPluginInfoService.publishGatewayPlugin(Mockito.any())).thenReturn(true);
        Mockito.when(envoyPluginInfoService.deleteGatewayPlugin(Mockito.any(BindingPluginDto.class), Mockito.anyLong())).thenReturn(true);
        // virtualGatewayInfoService放在BaseServiceImplTest会导致其他单测异常，故放在子类操作
        if (virtualGatewayInfoService.get(1) == null) {
            virtualGatewayInfoService.create(virtualGatewayDto);
        }
    }

    @Test
    public void checkCreateParam() {
        ErrorCode errorCode1 = pluginTemplateService.checkCreateParam(templateInfo);
        assertEquals(errorCode1.getCode(), CommonErrorCode.SAME_NAME_PLUGIN_TEMPLATE_EXIST.getCode());
        PluginTemplateDto templateInfoForCheck = new PluginTemplateDto();
        BeanUtils.copyProperties(templateInfo, templateInfoForCheck);
        templateInfoForCheck.setTemplateName("test-for-check");
        ErrorCode errorCode2 = pluginTemplateService.checkCreateParam(templateInfoForCheck);
        assertEquals(errorCode2.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void checkUpdateParam() {
        ErrorCode errorCode1 = pluginTemplateService.checkUpdateParam(templateInfo);
        assertEquals(errorCode1.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void checkDeleteParam() {
        ErrorCode errorCode1 = pluginTemplateService.checkDeleteParam(templateInfo);
        assertEquals(errorCode1.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void create() {
        PluginTemplateDto templateInfoForCreating = new PluginTemplateDto();
        BeanUtils.copyProperties(templateInfo, templateInfoForCreating);
        long id = pluginTemplateService.create(templateInfoForCreating);
        PluginTemplateDto pluginTemplateDtoFromDB = pluginTemplateService.get(id);
        assertEquals(pluginTemplateDtoFromDB.getTemplateName(), templateInfoForCreating.getTemplateName());
        templateInfoForCreating.setId(id);
        pluginTemplateService.delete(templateInfoForCreating);
    }

    @Test
    public void get() {
        PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(templateInfo.getId());
        assertEquals(pluginTemplateDto.getTemplateName(), templateInfo.getTemplateName());
    }

    @Test
    public void update() {
        PluginTemplateDto templateInfoForUpdating = new PluginTemplateDto();
        BeanUtils.copyProperties(templateInfo, templateInfoForUpdating);
        templateInfoForUpdating.setTemplateNotes("after changing");
        long update = pluginTemplateService.update(templateInfoForUpdating);
        assert (update == 1L);
    }

    @Test
    public void delete() {
        // delete方法无返回值，未抛出异常即成功
        PluginTemplateDto templateInfoForCreating = new PluginTemplateDto();
        BeanUtils.copyProperties(templateInfo, templateInfoForCreating);
        long id = pluginTemplateService.create(templateInfoForCreating);
        templateInfoForCreating.setId(id);
        pluginTemplateService.delete(templateInfoForCreating);
    }

    @Test
    public void getPluginTemplateInfoCount() {
        long pluginTemplateInfoCount = pluginTemplateService.getPluginTemplateInfoCount(templateInfo.getProjectId(), templateInfo.getPluginType());
        assert (pluginTemplateInfoCount == 1);
    }

    @Test
    public void getPluginTemplateInfoList() {
        List<PluginTemplateDto> pluginTemplateInfoList = pluginTemplateService.getPluginTemplateInfoList(templateInfo.getProjectId(), templateInfo.getPluginType(), 0, 99);
        assert (pluginTemplateInfoList.size() == 1);
        assertEquals(pluginTemplateInfoList.get(0).getTemplateName(), templateInfo.getTemplateName());
    }

    @Test
    public void checkSyncTemplate() {
         // 构建新的插件模板
        PluginTemplateDto templateInfoForCreating = new PluginTemplateDto();
        BeanUtils.copyProperties(templateInfo, templateInfoForCreating);
        long templateId = pluginTemplateService.create(templateInfoForCreating);

        // 构建新的插件
        PluginBindingDto pluginBindingDtoForCreating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForCreating);
        pluginBindingDtoForCreating.setTemplateId(templateId);
        long pluginId = pluginServiceInfo.create(pluginBindingDtoForCreating);

        List<Long> pluginBindingInfoIds = new ArrayList<>();
        pluginBindingInfoIds.add(pluginId);
        ErrorCode errorCode = pluginTemplateService.checkSyncTemplate(templateId, pluginBindingInfoIds);
        assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());

        // 清理mock数据
        pluginBindingDtoForCreating.setId(pluginId);
        pluginServiceInfo.delete(pluginBindingDtoForCreating);

        templateInfoForCreating.setId(templateId);
        pluginTemplateService.delete(templateInfoForCreating);
    }

    @Test
    public void syncTemplate() {
        List<Long> pluginBindingInfoIds = new ArrayList<>();
        pluginBindingInfoIds.add(PLUGIN_ID);
        List<PluginBindingDto> pluginBindingDtos
                = pluginTemplateService.syncTemplate(templateInfo.getId(), pluginBindingInfoIds);
        assert (pluginBindingDtos.size() == 1);
    }

    @Test
    public void batchGet() {
        List<Long> idList = new ArrayList<>();
        idList.add(templateInfo.getId());
        List<PluginTemplateDto> pluginTemplateInfoList = pluginTemplateService.batchGet(idList);
        assert (pluginTemplateInfoList.size() == 1);
    }

    @Test
    public void getPluginTemplateByType() {
        List<PluginTemplateInfo> pluginTemplateByType = pluginTemplateService.getPluginTemplateByType(PLUGIN_TYPE);
        assert (pluginTemplateByType.size() == 1);
    }

    @Test
    public void toView() {
        PluginTemplateInfo pluginTemplateInfo = new PluginTemplateInfo();
        BeanUtils.copyProperties(templateInfo, pluginTemplateInfo);
        PluginTemplateDto pluginTemplateDto = pluginTemplateService.toView(pluginTemplateInfo);
        assertEquals(pluginTemplateDto.getTemplateName(), templateInfo.getTemplateName());
        assertEquals(pluginTemplateDto.getTemplateNotes(), templateInfo.getTemplateNotes());
    }

    @Test
    public void toMeta() {
        PluginTemplateInfo pluginTemplateInfo = pluginTemplateService.toMeta(templateInfo);
        assertEquals(pluginTemplateInfo.getTemplateName(), templateInfo.getTemplateName());
        assertEquals(pluginTemplateInfo.getTemplateNotes(), templateInfo.getTemplateNotes());
    }
}