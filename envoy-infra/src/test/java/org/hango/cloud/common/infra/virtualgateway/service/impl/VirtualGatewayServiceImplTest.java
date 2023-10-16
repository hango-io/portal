package org.hango.cloud.common.infra.virtualgateway.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.virtualgateway.dto.GatewaySettingDTO;
import org.hango.cloud.common.infra.virtualgateway.dto.QueryVirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hango.cloud.gdashboard.api.util.Const.KUBERNETES_GATEWAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @Author zhufengwei
 * @Date 2023/7/14
 */
@SpringBootTest
@SuppressWarnings({"java:S1192"})
public class VirtualGatewayServiceImplTest {
    @Autowired
    IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    IGatewayService gatewayService;

    @Test
    public void testWithoutHooker() {
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", null);
        long id = virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);
        VirtualGatewayDto existed = virtualGatewayInfoService.get(id);
        assertEquals(virtualGatewayDto.getName(), existed.getName());

        existed.setDescription("update test");
        virtualGatewayInfoService.updateWithoutHooker(existed);
        assertEquals("update test", virtualGatewayInfoService.get(id).getDescription());

        virtualGatewayInfoService.deleteWithoutHooker(existed);
        assertNull(virtualGatewayInfoService.get(id));
    }

    @Test
    public void testQueryGatewayList(){
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        long gwId = gatewayService.create(gatewayDto);
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", gatewayDto);
        virtualGatewayDto.setType(KUBERNETES_GATEWAY);
        long id = virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        List<VirtualGatewayDto> virtualGatewayDtos = virtualGatewayInfoService.getKubernetesGatewayList(gwId);
        assertEquals(1, virtualGatewayDtos.size());
        assertEquals(id, virtualGatewayDtos.get(0).getId());

        virtualGatewayDtos = virtualGatewayInfoService.getVirtualGatewayList(Arrays.asList(gwId));
        assertEquals(1, virtualGatewayDtos.size());
        assertEquals(id, virtualGatewayDtos.get(0).getId());

        List<? extends VirtualGatewayDto> all = virtualGatewayInfoService.findAll();
        assertEquals(1, all.size());
        assertEquals(id, all.get(0).getId());

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);

        gatewayService.delete(gatewayDto);
    }

    @Test
    public void testQueryVirtualGatewayPage(){
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        long gwId = gatewayService.create(gatewayDto);
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", gatewayDto);
        virtualGatewayDto.setType(KUBERNETES_GATEWAY);
        long id = virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        QueryVirtualGatewayDto query = new QueryVirtualGatewayDto();
        query.setGwId(gwId);
        Page<VirtualGatewayDto> page = virtualGatewayInfoService.getVirtualGatewayPage(query);
        assertEquals(1, page.getTotal());
        assertEquals(id, page.getRecords().get(0).getId());

        query.setProjectIdList(new ArrayList<>());
        page = virtualGatewayInfoService.getVirtualGatewayPage(query);
        assertEquals(0, page.getTotal());

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
        gatewayService.delete(gatewayDto);
    }

    @Test
    public void updateGatewaySetting(){
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", null);
        long id = virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);
        GatewaySettingDTO settingDTO = new GatewaySettingDTO();
        settingDTO.setXffNumTrustedHops(2);
        settingDTO.setVirtualGwId(id);
        virtualGatewayInfoService.updateGatewaySetting(settingDTO);

        GatewaySettingDTO gatewaySetting = virtualGatewayInfoService.getGatewaySetting(id);
        assertEquals(2, gatewaySetting.getXffNumTrustedHops());

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
    }

    @Test
    public void checkCreateParam(){
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        gatewayService.create(gatewayDto);
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", gatewayDto);
        virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        ErrorCode errorCode = virtualGatewayInfoService.checkCreateParam(virtualGatewayDto);
        assertEquals("虚拟网关名称已经存在", errorCode.message);

        virtualGatewayDto.setName("test-create");
        errorCode = virtualGatewayInfoService.checkCreateParam(virtualGatewayDto);
        assertEquals("虚拟网关标识已经存在", errorCode.message);

        virtualGatewayDto.setCode("test-create");
        errorCode = virtualGatewayInfoService.checkCreateParam(virtualGatewayDto);
        assertEquals("虚拟网关端口已经存在", errorCode.message);

        virtualGatewayDto.setPort(81);
        errorCode = virtualGatewayInfoService.checkCreateParam(virtualGatewayDto);
        assertEquals("处理成功", errorCode.message);

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
        gatewayService.delete(gatewayDto);
    }

    @Test
    public void checkUpdateParam(){
        GatewayDto gatewayDto = MockUtil.initGatewayDto();
        gatewayService.create(gatewayDto);
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", gatewayDto);
        virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        VirtualGatewayDto check = new VirtualGatewayDto();
        check.setId(99L);
        check.setGwId(99L);
        check.setName("test-create");
        ErrorCode errorCode = virtualGatewayInfoService.checkUpdateParam(check);
        assertEquals("指定的虚拟网关不存在", errorCode.message);

        check.setId(virtualGatewayDto.getId());
        errorCode = virtualGatewayInfoService.checkUpdateParam(check);
        assertEquals("指定的网关不存在", errorCode.message);

        check.setGwId(gatewayDto.getId());
        errorCode = virtualGatewayInfoService.checkUpdateParam(check);
        assertEquals("处理成功", errorCode.message);

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
        gatewayService.delete(gatewayDto);
    }

    @Test
    public void checkDeleteParam(){
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("http", null);
        virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        ErrorCode errorCode = virtualGatewayInfoService.checkDeleteParam(null);
        assertEquals("指定的虚拟网关不存在", errorCode.message);

        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
    }
}
