package org.hango.cloud.envoy.infra.dubbo.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.util.Lists;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/1/13
 */
@SpringBootTest
@SuppressWarnings({"java:S1192"})
public class DubboMetaServiceImplTest {

    public static final Long DEFAULT_VG_ID = 1L;
    @Autowired
    private DubboMetaServiceImpl dubboMetaService;
    private DubboMetaDto dubboMetaDto = new DubboMetaDto();

    String interfaceName = "com.netease.apigateway.dubbo.api.GatewayEchoService";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);

        dubboMetaDto.setApplicationName("spring-cloud-dubbo");
        dubboMetaDto.setInterfaceName(interfaceName);
        dubboMetaDto.setGroup("group-a");
        dubboMetaDto.setVersion("0.0.0");
        dubboMetaDto.setMethod("echoForchar");
        dubboMetaDto.setParams(Lists.newArrayList("java.lang.String", "org.hango.apigateway.dubbo.Entity.Gateway"));
        dubboMetaDto.setReturns("org.hango.apigateway.dubbo.Entity.Gateway");
        dubboMetaDto.setVirtualGwId(1L);
    }


    @Test
    public void create() {
        long id = dubboMetaService.create(dubboMetaDto);
        assertTrue(id > 0);
    }


    @Test
    public void update() {
        long id = dubboMetaService.create(dubboMetaDto);
        dubboMetaDto.setId(id);
        dubboMetaDto.setVirtualGwId(2L);
        dubboMetaService.update(dubboMetaDto);
        assertEquals(2, (long) dubboMetaService.get(id).getVirtualGwId());
        dubboMetaDto.setVirtualGwId(DEFAULT_VG_ID);
        dubboMetaService.update(dubboMetaDto);
        assertSame(DEFAULT_VG_ID, dubboMetaService.get(id).getVirtualGwId());

    }

    @Test
    public void delete() {
        long id = dubboMetaService.create(dubboMetaDto);
        dubboMetaDto.setId(id);
        dubboMetaService.delete(dubboMetaDto);
        assertNull(dubboMetaService.get(id));
    }

    @Test
    public void findAll() {
        dubboMetaService.create(dubboMetaDto);
        assertSame(DEFAULT_VG_ID, dubboMetaService.findAll().get(0).getVirtualGwId());
    }

    @Test
    public void countAll() {
        dubboMetaService.findAll().forEach(d->dubboMetaService.delete(d));
        dubboMetaService.create(dubboMetaDto);
        assertSame(DEFAULT_VG_ID, dubboMetaService.findAll().get(0).getVirtualGwId());
        assertEquals((int) NumberUtils.INTEGER_ONE, dubboMetaService.countAll());
    }

    @Test
    public void batchCreateDubboMeta() {
        dubboMetaService.findAll().forEach(d -> dubboMetaService.delete(d));
        dubboMetaService.batchCreateDubboMeta(Lists.newArrayList(dubboMetaDto, dubboMetaDto));
        assertEquals(2, dubboMetaService.countAll());
    }

    @Test
    public void get() {
        long id = dubboMetaService.create(dubboMetaDto);
        assertNotNull(dubboMetaService.get(id));
    }



    @Test
    public void findByIgv() {
        dubboMetaService.findAll().forEach(d->dubboMetaService.delete(d));
        dubboMetaService.create(dubboMetaDto);
        List<DubboMetaDto> byIgv = dubboMetaService.findByIgv(DEFAULT_VG_ID, "com.netease.apigateway.dubbo.api.GatewayEchoService:group-a:0.0.0");
        assertEquals((int) NumberUtils.INTEGER_ONE, byIgv.size());
    }

    @Test
    public void batchDeleteByCondition() {
        dubboMetaService.findAll().forEach(d->dubboMetaService.delete(d));
        dubboMetaService.create(dubboMetaDto);
        dubboMetaService.batchDeleteByCondition(DEFAULT_VG_ID,"com.netease.apigateway.dubbo.api.GatewayEchoService:group-a:0.0.0");
        assertEquals(dubboMetaService.countAll(), (int) NumberUtils.INTEGER_ZERO);
    }

    @Test
    public void saveDubboMeta() {
        dubboMetaService.findAll().forEach(d->dubboMetaService.delete(d));
        dubboMetaService.saveDubboMeta(DEFAULT_VG_ID,"com.netease.apigateway.dubbo.api.GatewayEchoService:group-a:0.0.0",Lists.newArrayList(dubboMetaDto));
        assertEquals(dubboMetaService.findAll().size(), (int) NumberUtils.INTEGER_ONE);
    }

    @Test
    public void refreshDubboMeta() {
        dubboMetaService.refreshDubboMeta(DEFAULT_VG_ID,"com.netease.apigateway.dubbo.api.GatewayEchoService:group-a:0.0.0");
    }

    @Test
    public void checkCreateParam() {
        ErrorCode errorCode = dubboMetaService.checkCreateParam(dubboMetaDto);
        assertEquals(CommonErrorCode.SUCCESS, errorCode);
    }

    @Test
    public void checkUpdateParam() {
        ErrorCode errorCode = dubboMetaService.checkUpdateParam(dubboMetaDto);
        assertEquals(CommonErrorCode.SUCCESS, errorCode);
    }

    @Test
    public void checkDeleteParam() {
        ErrorCode errorCode = dubboMetaService.checkCreateParam(dubboMetaDto);
        assertEquals(CommonErrorCode.SUCCESS, errorCode);
    }
}