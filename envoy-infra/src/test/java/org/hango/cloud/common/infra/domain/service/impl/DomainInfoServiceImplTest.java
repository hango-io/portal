package org.hango.cloud.common.infra.domain.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.domain.dto.DomainInfoDTO;
import org.hango.cloud.common.infra.domain.dto.DomainQueryDTO;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.util.MockUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author zhufengwei
 * @Date 2023/2/1
 */
@SpringBootTest
@SuppressWarnings({"java:S1192"})
public class DomainInfoServiceImplTest {

    @Autowired
    DomainInfoServiceImpl domainInfoService;

    @Autowired
    IVirtualGatewayInfoService virtualGatewayInfoService;


    @Test
    public void getDomainInfoPage() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        long id = domainInfoService.create(domainInfoDTO);
        DomainQueryDTO queryDTO = new DomainQueryDTO();
        queryDTO.setProtocol("HTTP");
        Page<DomainInfo> page = domainInfoService.getDomainInfoPage(queryDTO);
        assertEquals(1, page.getTotal());
        assertEquals(1, page.getCurrent());
        assertEquals(MockUtil.HOST, page.getRecords().get(0).getHost());
        domainInfoService.delete(domainInfoDTO);
    }



    @Test
    public void checkCreateParam() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        long id = domainInfoService.create(domainInfoDTO);
        ErrorCode errorCode = domainInfoService.checkCreateParam(domainInfoDTO);
        assertEquals("域名已存在，不允许重复创建", errorCode.message);

        domainInfoDTO.setHost("*.com");
        errorCode = domainInfoService.checkCreateParam(domainInfoDTO);
        assertEquals(errorCode.message, "不支持泛域名 *.com");

        domainInfoDTO.setHost("www.com");
        domainInfoDTO.setProtocol("HTTPS");
        errorCode = domainInfoService.checkCreateParam(domainInfoDTO);
        assertEquals(errorCode.message, "HTTPS域名必须携带证书");

        domainInfoDTO.setCertificateId(1L);
        errorCode = domainInfoService.checkCreateParam(domainInfoDTO);
        assertEquals(errorCode.message, "无效的证书id");

        domainInfoService.delete(domainInfoDTO);

    }

    @Test
    public void checkUpdateParam() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        long id = domainInfoService.create(domainInfoDTO);

        DomainInfoDTO checkDTO = new DomainInfoDTO();
        ErrorCode errorCode = domainInfoService.checkUpdateParam(checkDTO);
        assertEquals(errorCode.message, "域名id不能为空");


        checkDTO.setId(99L);
        errorCode = domainInfoService.checkUpdateParam(checkDTO);
        assertEquals(errorCode.message, "域名不存在，更新域名信息失败");

        domainInfoService.delete(domainInfoDTO);

    }

    @Test
    public void checkDeleteParam() {
        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        long id = domainInfoService.create(domainInfoDTO);

        DomainInfoDTO checkDTO = new DomainInfoDTO();
        ErrorCode errorCode = domainInfoService.checkDeleteParam(checkDTO);
        assertEquals(errorCode.message, "未找到需要删除的域名");
        domainInfoService.delete(domainInfoDTO);
    }

    @Test
    public void getBindDomainInfoList(){
        VirtualGatewayDto virtualGatewayDto = MockUtil.initVirtualGateway("HTTP", null);
        long vgId = virtualGatewayInfoService.createWithoutHooker(virtualGatewayDto);

        DomainInfoDTO domainInfoDTO = MockUtil.initHttpDomainInfo();
        domainInfoService.create(domainInfoDTO);


        DomainQueryDTO queryDTO = new DomainQueryDTO();
        queryDTO.setVirtualGwId(vgId);
        List<DomainInfoDTO> bindDomainInfoList = domainInfoService.getBindDomainInfoList(queryDTO);
        assertEquals(1, bindDomainInfoList.size());
        assertEquals(MockUtil.HOST, bindDomainInfoList.get(0).getHost());
        domainInfoService.delete(domainInfoDTO);
        virtualGatewayInfoService.deleteWithoutHooker(virtualGatewayDto);
    }

}