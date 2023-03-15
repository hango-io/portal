package org.hango.cloud.common.infra.plugin.service.impl;

import org.hango.cloud.BaseServiceImplTest;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.hango.cloud.envoy.infra.plugin.service.impl.EnvoyPluginInfoServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SOAP_JSON_TRANSCODER_PLUGIN;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("java:S2699")
@RunWith(SpringRunner.class)
@SpringBootTest
public class PluginServiceInfoImplTest extends BaseServiceImplTest {
    @Autowired
    private PluginServiceInfoImpl pluginServiceInfo;
    @MockBean
    private EnvoyPluginInfoServiceImpl envoyPluginInfoService;
    @Autowired
    private VirtualGatewayServiceImpl virtualGatewayInfoService;

    public PluginServiceInfoImplTest() {
        // 空参构造方法
    }

    @Before
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
    public void checkDescribePlugin() {
        ErrorCode errorCode1 = pluginServiceInfo.checkDescribePlugin(VIRTUAL_GW_ID);
        assertEquals(errorCode1.getCode(), CommonErrorCode.SUCCESS.getCode());
        ErrorCode errorCode2 = pluginServiceInfo.checkDescribePlugin(999);
        assertEquals(errorCode2.getCode(), CommonErrorCode.NO_SUCH_GATEWAY.getCode());
    }

    @Test
    public void checkUnbindParam() {
        ErrorCode errorCode1 = pluginServiceInfo.checkUnbindParam(PLUGIN_ID);
        assertEquals(errorCode1.getCode(), CommonErrorCode.SUCCESS.getCode());
        ErrorCode errorCode2 = pluginServiceInfo.checkUnbindParam(999);
        assertEquals(errorCode2.getCode(), CommonErrorCode.NO_SUCH_PLUGIN_BINDING.getCode());
    }

    @Test
    public void checkCopyGlobalPluginToGateway() {
        ErrorCode errorCode = pluginServiceInfo.checkCopyGlobalPluginToGateway(copyGlobalPluginDto);
        assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void getPluginInfoFromDataPlane() {
        // 无需单测，由hooker调用数据面实现
    }

    @Test
    public void checkCreateParam() {
        ErrorCode errorCode = pluginServiceInfo.checkCreateParam(pluginBindingDto);
        assertEquals(errorCode.getCode(), CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY.getCode());
    }

    @Test
    public void getBindingInfo() {
        PluginBindingInfo bindingInfo = pluginServiceInfo.getBindingInfo(bindingPluginDto);
        assert (Long.parseLong(bindingInfo.getBindingObjectId()) == bindingPluginDto.getBindingObjectId());
        assert (Long.parseLong(bindingInfo.getBindingObjectId()) == bindingPluginDto.getBindingObjectId());
        assertEquals(bindingInfo.getPluginType(), bindingPluginDto.getPluginType());
    }

    @Test
    public void getInnerPlugins() {
        List<String> innerPlugins = pluginServiceInfo.getInnerPlugins();
        assert (innerPlugins.size() == 1);
        assert (innerPlugins.get(0).equals(SOAP_JSON_TRANSCODER_PLUGIN));
    }

    @Test
    public void getBindingInfoList() {
        List<PluginBindingInfo> bindingInfoList = pluginServiceInfo.getBindingInfoList(bindingPluginDto);
        PluginBindingInfo pluginBindingInfoFromDB = bindingInfoList.get(0);
        assert (Long.parseLong(pluginBindingInfoFromDB.getBindingObjectId()) == bindingPluginDto.getBindingObjectId());
        assert (Long.parseLong(pluginBindingInfoFromDB.getBindingObjectId()) == bindingPluginDto.getBindingObjectId());
        assertEquals(pluginBindingInfoFromDB.getPluginType(), bindingPluginDto.getPluginType());
    }

    /**
     * 拷贝全局会插入一条向缓存数据库插件数据
     */
    @Test
    public void copyGlobalPluginToGatewayByVirtualGwId() {
        Long originalProjectId = copyGlobalPluginDto.getProjectId();
        CopyGlobalPluginDto copyGlobalPluginDtoForJunit = new CopyGlobalPluginDto();
        BeanUtils.copyProperties(copyGlobalPluginDto, copyGlobalPluginDtoForJunit);
        // 设置与目标插件项目ID不一致（理论上跨网关、同跨项目拷贝全局插件）
        copyGlobalPluginDtoForJunit.setProjectId(originalProjectId + 1);
        boolean result = pluginServiceInfo.copyGlobalPluginToGatewayByVirtualGwId(copyGlobalPluginDtoForJunit);
        assert (result);
    }

    @Test
    public void getPluginBindingListByVirtualGwIdAndTypeAndProjectId() {
        List<PluginBindingInfo> PluginBindingList =
                pluginServiceInfo.getPluginBindingListByVirtualGwIdAndTypeAndProjectId(bindingPlugin, PROJECT_ID);
        assert (PluginBindingList.size() >= 1);
        assert (Long.parseLong(PluginBindingList.get(0).getBindingObjectId()) == bindingPlugin.getBindingObjectId());
        assert (Long.parseLong(PluginBindingList.get(0).getBindingObjectId()) == bindingPlugin.getBindingObjectId());
    }

    @Test
    public void checkDescribeBindingPlugins() {
        ErrorCode errorCode = pluginServiceInfo.checkDescribeBindingPlugins(PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE);
        assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void create() {
        PluginBindingDto pluginBindingDtoForCreating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForCreating);
        long id = pluginServiceInfo.create(pluginBindingDtoForCreating);
        // 查询插入的数据
        PluginBindingDto pluginBindingDtoFromDB = pluginServiceInfo.get(id);
        assert (VIRTUAL_GW_ID == pluginBindingDtoFromDB.getVirtualGwId());
        assertEquals(PLUGIN_BINDING_OBJ_TYPE, pluginBindingDtoFromDB.getBindingObjectType());
        assertEquals(PLUGIN_BINDING_OBJ_ID, pluginBindingDtoFromDB.getBindingObjectId());
        // 清理插入的数据
        PluginBindingDto pluginBindingDtoForDeleting = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForDeleting);
        pluginBindingDtoForDeleting.setId(id);
        pluginServiceInfo.delete(pluginBindingDtoForDeleting);
    }

    @Test
    public void update() {
        PluginBindingDto pluginBindingDtoForUpdating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForUpdating);
        pluginBindingDtoForUpdating.setBindingObjectType(PLUGIN_BINDING_OBJ_TYPE);
        pluginBindingDtoForUpdating.setId(1);
        long result = pluginServiceInfo.update(pluginBindingDtoForUpdating);
        assertEquals(1, result);
    }

    @Test
    public void delete() {
        // 新增一条数据
        long id = pluginServiceInfo.create(pluginBindingDto);
        PluginBindingDto pluginBindingDtoForUpdating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForUpdating);
        pluginBindingDtoForUpdating.setId(id);
        // 无返回值，不抛出异常即单测通过
        pluginServiceInfo.delete(pluginBindingDtoForUpdating);
    }

    @Test
    public void getEnablePluginBindingList() {
        List<PluginBindingInfo> enablePluginBindingList = pluginServiceInfo.getEnablePluginBindingList(VIRTUAL_GW_ID);
        // BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (enablePluginBindingList.size() >= 1);
    }

    @Test
    public void getPluginBindingList() {
        List<PluginBindingDto> pluginBindingList
                = pluginServiceInfo.getPluginBindingList(VIRTUAL_GW_ID, PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE);
        // 仅BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (pluginBindingList.size() >= 1);
    }

    @Test
    public void checkUpdateParam() {
        PluginBindingDto pluginBindingDtoForUpdating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForUpdating);
        pluginBindingDtoForUpdating.setId(1);
        ErrorCode errorCode = pluginServiceInfo.checkUpdateParam(pluginBindingDtoForUpdating);
        assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void getBindingPluginCount() {
        long bindingPluginCount = pluginServiceInfo.getBindingPluginCount(VIRTUAL_GW_ID,
                PROJECT_ID, PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE, null, null);
        // 仅BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (bindingPluginCount >= 1);
    }

    @Test
    public void getBindingPluginCountExcludedInnerPlugins() {
        long bindingPluginCount = pluginServiceInfo.getBindingPluginCountExcludedInnerPlugins(VIRTUAL_GW_ID,
                PROJECT_ID, PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE, null);
        // 仅BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (bindingPluginCount >= 1);
    }

    @Test
    public void getBindingPluginListOutSide() {
        List<PluginBindingDto> bindingPluginList = pluginServiceInfo.getBindingPluginListOutSide(VIRTUAL_GW_ID,
                PROJECT_ID, PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE, null, 0, 99, "create_time", "desc");
        // 仅BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (bindingPluginList.size() >= 1);
    }

    @Test
    public void getBindingPluginList() {
        List<PluginBindingDto> bindingPluginList = pluginServiceInfo.getBindingPluginList(VIRTUAL_GW_ID,
                PROJECT_ID, PLUGIN_BINDING_OBJ_ID, PLUGIN_BINDING_OBJ_TYPE, null, 0, 99, "create_time", "desc");
        // 仅BeforeClass中插入的一条预置mock数据（拷贝路由单测也会插入一条）
        assert (bindingPluginList.size() >= 1);
    }

    @Test
    public void deletePluginList() {
        PluginBindingDto pluginBindingDtoForUpdating = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForUpdating);
        pluginBindingDtoForUpdating.setBindingObjectId("999");
        // 插入3条数据
        pluginServiceInfo.create(pluginBindingDtoForUpdating);
        pluginServiceInfo.create(pluginBindingDtoForUpdating);
        pluginServiceInfo.create(pluginBindingDtoForUpdating);
        long deletedNum = pluginServiceInfo.deletePluginList(pluginBindingDtoForUpdating.getVirtualGwId(),
                pluginBindingDtoForUpdating.getBindingObjectId(), pluginBindingDtoForUpdating.getBindingObjectType());
        // 删除数量与插入数量一致
        assert (deletedNum == 3L);
    }

    @Test
    public void checkUpdatePluginBindingStatus() {
        ErrorCode errorCode = pluginServiceInfo.checkUpdatePluginBindingStatus(PLUGIN_ID, PLUGIN_STATUS);
        assertEquals(errorCode.getCode(), CommonErrorCode.SUCCESS.getCode());
    }

    @Test
    public void getBindingListByTemplateId() {
        List<PluginBindingDto> bindingList = pluginServiceInfo.getBindingListByTemplateId(PLUGIN_TEMPLATE_ID);
        assert (bindingList.size() == 1);
    }

    @Test
    public void batchDissociateTemplate() {
        PluginBindingDto pluginBindingDtoForTemplate = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingDtoForTemplate);
        // 插入临时测试数据
        long id = pluginServiceInfo.create(pluginBindingDtoForTemplate);
        List<Long> pluginIdList = new ArrayList<>();
        pluginIdList.add(id);
        boolean result = pluginServiceInfo.batchDissociateTemplate(pluginIdList);
        assert result;
        pluginBindingDtoForTemplate.setId(id);
        // 无返回值，不抛出异常即单测通过
        pluginServiceInfo.delete(pluginBindingDtoForTemplate);
    }

    @Test
    public void batchGetById() {
        List<Long> pluginIdList = new ArrayList<>();
        pluginIdList.add(PLUGIN_ID);
        List<PluginBindingDto> bindingList = pluginServiceInfo.batchGetById(pluginIdList);
        assert (bindingList.size() == 1);
    }

    @Test
    public void isInnerPlugin() {
        PluginBindingDto pluginBindingDto1 = new PluginBindingDto();
        pluginBindingDto1.setPluginType(PLUGIN_TYPE);
        assert !pluginServiceInfo.isInnerPlugin(pluginBindingDto1);
        PluginBindingDto pluginBindingDto2 = new PluginBindingDto();
        pluginBindingDto2.setPluginType(SOAP_JSON_TRANSCODER_PLUGIN);
        assert pluginServiceInfo.isInnerPlugin(pluginBindingDto2);
    }

    @Test
    public void toView() {
        PluginBindingInfo bindingInfo = pluginServiceInfo.getBindingInfo(bindingPluginDto);
        PluginBindingDto pluginBindingDto1 = pluginServiceInfo.toView(bindingInfo);
        assert bindingPluginDto.getBindingObjectId() == Long.parseLong(pluginBindingDto1.getBindingObjectId());
    }

    @Test
    public void toMeta() {
        PluginBindingInfo pluginBindingInfo1 = pluginServiceInfo.toMeta(pluginBindingDto);
        assertEquals(pluginBindingInfo1.getBindingObjectId(), pluginBindingDto.getBindingObjectId());
    }
}