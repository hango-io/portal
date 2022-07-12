package org.hango.cloud.dashboard.apiserver.util;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.model.HealthService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.consul.discovery.ConsulServerUtils;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/8/13
 */
public class ConsulClientUtils {

    public static final Logger logger = LoggerFactory.getLogger(ConsulClientUtils.class);

    public static final String HTTP_PREFIX = "http://";

    public static final String DATA_CENTER = "dc";

    /**
     * 获取ConsulClient
     *
     * @param registryUrl
     * @return
     */
    private static ConsulClient getConsulClient(String registryUrl) throws Exception {
        //如果地址为空，则生成默认ConsulClient: localhost:8500
        if (StringUtils.isBlank(registryUrl)) {
            return new ConsulClient();
        }
        //默认ConsulClient连接端口 ：8500
        int port = 8500;
        URI uri = new URI(registryUrl);
        String host = uri.getScheme() + "://" + uri.getHost();
        port = uri.getPort() == -1 ? port : uri.getPort();
        ConsulClient consulClient = new ConsulClient(host, port);
        Response<String> statusLeaderResp = consulClient.getStatusLeader();
        if (statusLeaderResp == null) {
            throw new Exception("Can't Connect to Consul Cluster");
        }
        return consulClient;
    }

    /**
     * 获取ConsulClient
     *
     * @param registryUrls
     * @return
     */
    public static ConsulClient getDefaultConsulClient(String registryUrls) {

        if (StringUtils.isBlank(registryUrls)) {
            return new ConsulClient();
        }
        String[] split = registryUrls.split(",");
        ConsulClient consulClient = null;
        for (int i = 0; i < split.length; i++) {
            String registryUrl = split[0];
            try {
                consulClient = getConsulClient(registryUrl);
            } catch (Exception e) {
                logger.warn("Create Consul Client Failed! Consul Registry Addr is {} ,Error Message is {}", registryUrl, e.getMessage());
                //如果只有一个地址且连接异常，则返回默认地址
                if (split.length == 1) {
                    logger.warn("Only One Registry Exist , Return Default Consul Client");
                    return new ConsulClient();
                }
                logger.warn("Start to Use The Next Consul Cluster Node Addr : {}", split[1]);
                adjustArrayOrder(split);
                continue;
            }
            break;
        }
        return consulClient == null ? new ConsulClient() : consulClient;
    }

    /**
     * 将数组元素依次提前
     *
     * @param array
     * @return
     */
    private static String[] adjustArrayOrder(String[] array) {
        if (array == null) {
            return null;
        }
        String e1 = array[0];
        for (int i = 0; i < array.length; i++) {
            if (i + 1 == array.length) {
                array[i] = e1;
                break;
            }
            array[i] = array[i + 1];
        }
        return array;
    }

    /**
     * 从Consul获取实例信息
     *
     * @param consulClient
     * @param dataCenter       数据中心
     * @param applicationNames 应用名称
     * @return
     */
    public static List<HealthService> getUpInstanceInfoList(ConsulClient consulClient, String dataCenter, String... applicationNames) {
        if (ArrayUtils.isEmpty(applicationNames)) {
            return Collections.emptyList();
        }
        List<HealthService> instanceInfoList = new ArrayList<>();
        for (String applicationName : applicationNames) {
            instanceInfoList.addAll(getUpInstanceInfoList(consulClient, dataCenter, applicationName));
        }
        return instanceInfoList;
    }

    /**
     * 从Consul获取实例信息
     *
     * @param consulClient
     * @param applicationName 应用名称
     * @return
     */
    public static List<HealthService> getUpInstanceInfoList(ConsulClient consulClient, String dataCenter, String applicationName) {
        if (StringUtils.isBlank(applicationName)) {
            return Collections.emptyList();
        }
        List<Service> instanceInfoList = new ArrayList<>();

        QueryParams queryParams = QueryParams.DEFAULT;
        if (StringUtils.isNotBlank(dataCenter)) {
            queryParams = new QueryParams(dataCenter);
        }
        Response<List<HealthService>> healthServiceResp = consulClient.getHealthServices(applicationName, false, queryParams);
        if (healthServiceResp == null) {
            return Collections.emptyList();
        }
        return healthServiceResp.getValue();
    }


    /**
     * 获取可用的应用
     *
     * @param consulClient
     * @return
     */
    public static List<String> getApplications(ConsulClient consulClient, String dataCenter) {
        List<String> applicationList = Collections.emptyList();
        if (consulClient == null) {
            return applicationList;
        }
        QueryParams queryParams = QueryParams.DEFAULT;
        if (StringUtils.isNotBlank(dataCenter)) {
            queryParams = new QueryParams(dataCenter);
        }
        Response<Map<String, List<String>>> catalogServices = consulClient.getCatalogServices(queryParams);
        if (catalogServices == null) {
            return Collections.emptyList();
        }
        Map<String, List<String>> catalogServicesMap = catalogServices.getValue();
        if (CollectionUtils.isEmpty(catalogServicesMap)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(catalogServicesMap.keySet());
    }


    /**
     * 获取可用的服务实例地址
     *
     * @param consulClient
     * @param serviceType      服务类别
     * @param applicationNames 应用名称
     * @return
     */
    public static List<String> getUpInstanceAddrList(ConsulClient consulClient, String dataCenter, String serviceType, String... applicationNames) {
        List<String> instanceAddrList = Collections.emptyList();
        if (consulClient == null) {
            return instanceAddrList;
        }
        List<HealthService> upInstanceInfoList = getUpInstanceInfoList(consulClient, dataCenter, applicationNames);
        if (CollectionUtils.isEmpty(upInstanceInfoList)) {
            return instanceAddrList;
        }
        instanceAddrList = new ArrayList<>();

        for (HealthService service : upInstanceInfoList) {
            String host = ConsulServerUtils.findHost(service);
            instanceAddrList.add(HTTP_PREFIX + host + ":" + service.getService().getPort());
        }
        return instanceAddrList;
    }


    /**
     * 从Consul实例中获取标签
     *
     * @param consulClient
     * @param applicationNames
     * @return
     */
    public static List<String> getUpInstanceTagList(ConsulClient consulClient, String dataCenter, List<String> tags, String serviceType, String... applicationNames) {
        List<String> tagList = Collections.emptyList();
        List<HealthService> serviceList = getUpInstanceInfoList(consulClient, dataCenter, applicationNames);
        //举例 tag = "zone=hangzhou"
        if (CollectionUtils.isEmpty(serviceList)) {
            return tagList;
        }
        tagList = new ArrayList<>();
        for (HealthService service : serviceList) {
            tagList.addAll(service.getService().getTags());
        }
        return tagList;
    }

    public static String getDataCenter(String registryUrl) {
        if (StringUtils.isBlank(registryUrl)) {
            return null;
        }
        try {
            List<NameValuePair> parse = URLEncodedUtils.parse(new URI(registryUrl), Charset.defaultCharset());
            if (CollectionUtils.isEmpty(parse)) {
                return null;
            }
            for (NameValuePair nameValuePair : parse) {
                if (DATA_CENTER.equalsIgnoreCase(nameValuePair.getName())) {
                    return nameValuePair.getValue();

                }
            }
        } catch (URISyntaxException e) {
            logger.warn("非法的注册中心地址，registryUrl ={}", registryUrl);
        }
        return null;
    }
}
