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

}
