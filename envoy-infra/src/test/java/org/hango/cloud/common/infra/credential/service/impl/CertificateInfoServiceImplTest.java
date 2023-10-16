package org.hango.cloud.common.infra.credential.service.impl;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.mapper.CertificateInfoMapper;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoDTO;
import org.hango.cloud.common.infra.credential.dto.CertificateInfoViewDTO;
import org.hango.cloud.common.infra.credential.service.ICertificateInfoService;
import org.hango.cloud.envoy.infra.credential.service.impl.EnvoySecretProxyServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @Author zhufengwei
 * @Date 2023/2/1
 */
@SpringBootTest
public class CertificateInfoServiceImplTest{

    @Autowired
    ICertificateInfoService certificateInfoService;

    @Autowired
    CertificateInfoMapper certificateInfoMapper;

    @MockBean
    EnvoySecretProxyServiceImpl envoySecretProxyService;

    private String CERT_NAME = "httpbin";

    public static CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();

    @BeforeAll
    public static void setUpBeforeClass(){
        certificateInfoDTO.setName("httpbin");
        certificateInfoDTO.setType("serverCert");
        certificateInfoDTO.setContent("-----BEGIN CERTIFICATE-----\n" +
                "MIIC3jCCAcYCAQAwDQYJKoZIhvcNAQELBQAwLTEVMBMGA1UECgwMZXhhbXBsZSBJ\n" +
                "bmMuMRQwEgYDVQQDDAtleGFtcGxlLmNvbTAeFw0yMjEyMjcwMTEzMDRaFw0yMzEy\n" +
                "MjcwMTEzMDRaMD0xHDAaBgNVBAMME2h0dHBiaW4uZXhhbXBsZS5jb20xHTAbBgNV\n" +
                "BAoMFGh0dHBiaW4gb3JnYW5pemF0aW9uMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n" +
                "MIIBCgKCAQEAynlg91GGLeLCeMhZ04NJ14BUW0T4HOEzlDMAefDJAdh4YRtLHUkA\n" +
                "Ae2QHhMkuA0wh6BNfW4T/34Az0dAPIFDneC+KvrNAqw6EVS1vHuAHbUH1Dzbz0J3\n" +
                "60he82FobnFK1ki2bXeAnP5ir+xVj0ZgWiC1zPxyVVJiKguh9+5X92zvN1+MqEgq\n" +
                "5i0emGdBr7NAB46AcMhQjNRAmDHl9QC+kJFU3qumRHUbSwxk0MfE+NPT5cdr2/jS\n" +
                "ju6kvuuh/UI0vQTgpdsSyZUNf+wwTzxFKLYqeUSQp6qzI6rEIqCEIGjFj66ZIpuW\n" +
                "FBmy+0BF7LhqU86GeCFjfr4Ck/9E33dqCwIDAQABMA0GCSqGSIb3DQEBCwUAA4IB\n" +
                "AQCpI4IfC8SOzGunRI6Vslf1/e+AeAg0T2CvXQVXBFPCmjsoVa4MtXl9Ac++snCV\n" +
                "8ZBoy5lY2+gZUBaU/oZBc2GsFnSr+yRRWhgVMTFOAQdqXED2kQTAT2vlCALrjL3o\n" +
                "AKZv3Ofz+307ZXoknWRpgKlozP4rlgj8NZYmvRuNqiO1c5OXxhvlenHjp35pvvyk\n" +
                "+OPXAqOqFyUJmxpBF5wNUtnVtAvMyGqMNN2MfZuixe5PPu4YKn4CDcB9+qEzF1e3\n" +
                "rkhNzJLZvXcS8hCN2kEmslTEeCBXI9lrLzk3eMuky3XoEEwhw8WZ2uc5Lt4BCt9n\n" +
                "/j/niarBTSC74jAykdETyW8j\n" +
                "-----END CERTIFICATE-----\n");
        certificateInfoDTO.setPrivateKey("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDKeWD3UYYt4sJ4\n" +
                "yFnTg0nXgFRbRPgc4TOUMwB58MkB2HhhG0sdSQAB7ZAeEyS4DTCHoE19bhP/fgDP\n" +
                "R0A8gUOd4L4q+s0CrDoRVLW8e4AdtQfUPNvPQnfrSF7zYWhucUrWSLZtd4Cc/mKv\n" +
                "7FWPRmBaILXM/HJVUmIqC6H37lf3bO83X4yoSCrmLR6YZ0Gvs0AHjoBwyFCM1ECY\n" +
                "MeX1AL6QkVTeq6ZEdRtLDGTQx8T409Plx2vb+NKO7qS+66H9QjS9BOCl2xLJlQ1/\n" +
                "7DBPPEUotip5RJCnqrMjqsQioIQgaMWPrpkim5YUGbL7QEXsuGpTzoZ4IWN+vgKT\n" +
                "/0Tfd2oLAgMBAAECggEBAJgkySZOzkLgqD1WX5k+iFg1CC5lDwO0fauY/3yh4IlQ\n" +
                "fnSJq9Hfru/D8Y2H2Qmsb5EadeAQvVo49qbK5YwRnhuea4ekWjTt8dxgCQgOzWCF\n" +
                "TdK/wHRnf/D+usqroWuMGi4XCW4MZEqUJNgJAOneejx/Y90avVc7xqPNhfHl2ZgD\n" +
                "nwCliOOnb6XEHAj3h1HMvgzO6f8xRYs40V/jEoN2sdZID3mCKtEmmlG7iRNr2Jdn\n" +
                "Imeqc/pvzRuOLfLdns2aL9el3jKPqnFlWpE8iffyv0EepJMpKL76jkNw7bCrJj5A\n" +
                "lZSFYftxFtf6yJkq/1TbbobxODwQDkOUGI6s9929EpECgYEA7FB3Y5c861dk6Ii4\n" +
                "4hw5+oLRJXp5T4bAmOa7uxMbK6O65BLeRo7yexlIjTt+q7Rsf0LyKxhEJDUqBeiy\n" +
                "wk9mHs5gk3iFndvsh6PjC1WcjchWmUgml6swzGOwixNCwuc+hQSNN7oGtYjdrRFa\n" +
                "EA5dDR5tiQJ1hdc1pFbkC/+NGwUCgYEA21dAcSGCwKX6gwiPxizOHthF1xFoC8gm\n" +
                "0ti6WPfz1OCUlAh1fPgYIl7rQzSnLdtTJBwwsc3YAv6E6VsDGnIgAsBAu3bEWqEe\n" +
                "RENdFPDTEfbeBlqvT0oKMR49JsjZ00PL8Zr4V38AeNHslAm3LDtE27bx5PKg9+Mt\n" +
                "Wp3Z0tZDHc8CgYBIU1BXAMC6XqMZsE61lWIBU+xjBhr+xgVIRsYKOYzFTHU0vLme\n" +
                "6r04A8L6xsy8DJpwULMBkYm5czQLXu7nHZnpr8xLFkX+zIKV+QjSPT+O/VPT0gtN\n" +
                "vC1RQOmVhy2VrWSrHXuDJMJM6Ti/cWIZK+w6yadImaISdo2KYLWnHm23aQKBgQCr\n" +
                "ySovcI93UodhGNOB4pWMktYgGwHiOGAvwo4wIAJYN4wmZHmf5q8APFcFy7cjsyLq\n" +
                "pSl+GDmWHsD6As3raHapsOkB6YLfeFC8JgZA4FxvNWmukFe9Qb+5uHUsayIu1Gpm\n" +
                "T3kxDcbQ2ZjwWKudeM31RtMs/NoVS1e6IOb4udbXAQKBgQDdCe4SHqDjWn/3Jx/W\n" +
                "aO2MC+eDIkHvyOo/3XAobX3sLhFu763dtnnAa2oKky/Ltptt7Qr5kJZdavlzKEtA\n" +
                "fvywtwyItm1osKJIYBOwptrrI5lzlWrXyHI+whRNP9H9eVgdZ7HkxBzpvA/I+nKN\n" +
                "g5AFr4hrXlcSQtWnTehLUtKA9Q==\n" +
                "-----END PRIVATE KEY-----\n");
    }

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        Mockito.when(envoySecretProxyService.publishToGateway(Mockito.any())).thenReturn(true);
        Mockito.when(envoySecretProxyService.offlineToGateway(Mockito.any())).thenReturn(true);

    }

    @Test
    public void create() {
        long id = certificateInfoService.create(certificateInfoDTO);
        CertificateInfoViewDTO viewDTO = certificateInfoService.getCertificateInfoById(id);
        assertEquals(CERT_NAME, viewDTO.getName());
        assertEquals("httpbin.example.com", viewDTO.getDomain());
        certificateInfoDTO.setCertificateId(id);
        certificateInfoService.delete(certificateInfoDTO);
        certificateInfoDTO.setCertificateId(null);
        viewDTO = certificateInfoService.getCertificateInfoById(id);
        assertNull(viewDTO);
    }


    @Test
    public void getCertificateInfos() {
        long id = certificateInfoService.create(certificateInfoDTO);
        List<CertificateInfoViewDTO> certificateInfos = certificateInfoService.getCertificateInfos("example.com");
        assertEquals(CERT_NAME, certificateInfos.get(0).getName());
        assertEquals("httpbin.example.com", certificateInfos.get(0).getDomain());
        certificateInfoMapper.deleteById(id);
    }

    @Test
    public void checkCreateParam() {
        certificateInfoDTO.setType("errorType");
        ErrorCode errorCode = certificateInfoService.checkCreateParam(certificateInfoDTO);
        assertEquals(errorCode.message, "证书类型错误");

        certificateInfoDTO.setType("serverCert");
        String content = certificateInfoDTO.getContent();
        certificateInfoDTO.setContent("errrorContent");
        errorCode = certificateInfoService.checkCreateParam(certificateInfoDTO);
        assertEquals(errorCode.message, "证书格式错误");
        certificateInfoDTO.setContent(content);

        String privateKey = certificateInfoDTO.getPrivateKey();
        certificateInfoDTO.setPrivateKey(null);
        errorCode = certificateInfoService.checkCreateParam(certificateInfoDTO);
        assertEquals(errorCode.message, "服务器私钥不能为空");
        certificateInfoDTO.setPrivateKey(privateKey);

        long id = certificateInfoService.create(certificateInfoDTO);

        errorCode = certificateInfoService.checkCreateParam(certificateInfoDTO);
        assertEquals(errorCode.message, "证书名已存在，不允许重复创建");

        certificateInfoMapper.deleteById(id);
    }


    @Test
    public void checkDeleteParam() {
        certificateInfoDTO.setCertificateId(99L);
        ErrorCode errorCode = certificateInfoService.checkDeleteParam(certificateInfoDTO);
        assertEquals(errorCode.message, "未找到需要删除的证书");
    }
}