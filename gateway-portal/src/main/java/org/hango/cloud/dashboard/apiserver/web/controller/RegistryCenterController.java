package org.hango.cloud.dashboard.apiserver.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IRegistryCenterService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/1/14
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2019-01-17"})
public class RegistryCenterController extends AbstractController {

    public static final Logger logger = LoggerFactory.getLogger(RegistryCenterController.class);
    public static final String APPLICATION_NAME_REGEX = "UNKNOWN|NSF-EUREKA-SERVER";
    @Autowired
    private ApiServerConfig apiServerConfig;
    @Autowired
    private IRegistryCenterService registryCenterService;

    private static ErrorCode checkAlive(String path) {
        InetSocketAddress address = null;
        boolean isAlive = false;
        try (Socket socket = new Socket()) {
            URL url = new URL(path);
            address = new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
            socket.connect(address, 2000);
            isAlive = socket.isConnected();
        } catch (Exception e) {
            logger.warn("telnet失败，地址" + path + "不可用");
            return CommonErrorCode.InvalidParameterRegistryAddr(path);
        }
        return isAlive ? CommonErrorCode.Success : CommonErrorCode.HostUnreachable;
    }

    /**
     * 从平台获取注册中心地址
     *
     * @param envId
     * @return
     */
    @RequestMapping(params = {"Action=GetRegistryAddr"}, method = RequestMethod.GET)
    public Object getRegistryCenterAddrFromPlatform(@RequestParam(value = "EnvId") String envId) {
        logger.info("请求从平台获取注册中心地址 envId = {}", envId);
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("EnvId", envId);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());

        String nsfRegistryAddr = StringUtils.EMPTY;
        Map<String, Object> resultMap = new HashMap<>();
        try {
            HttpEntity<String> requestEntity = new HttpEntity(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiServerConfig.getSkiffAuthorityAddr() + "?Action" + "=DescribeFundamentalEnvInfoByEnvId"
                            + "&Version=2019-01-03&EnvId={EnvId}", HttpMethod.GET, requestEntity, String.class, queryMap);
//            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiServerConfig.getSkiffAuthorityAddr
            //            () +
//                    "?Action=DescribeFundamentalEnvInfoByEnvId&Version=2019-01-03&EnvId={EnvId}", String.class,
            //                    queryMap);
            String body = responseEntity.getBody();
            JSONObject jsonObject = JSON.parseObject(body);
            JSONObject result = jsonObject.getJSONObject("Result");
            if (result != null) {
                JSONObject envAddr = result.getJSONObject("EnvAddr");
                if (envAddr != null) {
                    String nsfRegistry = envAddr.getString("NSFRegistry");
                    if (StringUtils.isNotBlank(nsfRegistry)) {
                        String[] nsfRegistryList = nsfRegistry.split(",");
                        for (int i = 0; i < nsfRegistryList.length; i++) {
                            String nsfRegistryTemp = (StringUtils.startsWith(nsfRegistryList[i], Const.HTTP_PREFIX) || StringUtils.startsWith(nsfRegistryList[i], Const.HTTPS_PREFIX)) ? nsfRegistryList[i] : Const.HTTP_PREFIX + nsfRegistryList[i];
                            nsfRegistryAddr += nsfRegistryTemp;
                            if (i != nsfRegistryList.length - 1) {
                                nsfRegistryAddr += ",";
                            }
                        }
                    } else {
                        logger.info("从平台上获取NSFRegistry的值为空");
                    }
                }
            }
            resultMap.put("Result", nsfRegistryAddr);
            return apiReturn(HttpStatus.SC_OK, null, null, resultMap);
        } catch (HttpClientErrorException e) {
            logger.error("acquire serviceInfo from meta-service failed! ErrorMsg= {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return apiReturn(CommonErrorCode.InternalServerError);
    }

}
