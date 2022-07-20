package org.hango.cloud.dashboard.apiserver.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/9/15
 */
public class ZkClientUtils {

    public static final String DEFAULT_KEY_PREFIX = "default.";
    /**
     * dubbo arch
     */
    private static final String INTERFACE_ARCH_ROOT = "/dubbo";
    private static final String APP_ARCH_ROOT = "/services";


    private static final String PROVIDERS = "/providers";
    private static final String APPLICATION = "application";
    private static final String GROUP = "group";
    private static final String VERSION = "version";
    private static final String INTERFACE = "interface";
    private static final String ZK_PROTOCOL = "zookeeper://";
    private static Logger logger = LoggerFactory.getLogger(ZkClientUtils.class);

    /**
     * 获取Zookeeper Client ，未设置连接超时，session超时默认30S
     *
     * @param registryCenterAddr
     * @return
     */
    public static ZkClient getDefaultZkClient(String... registryCenterAddr) {
        if (ArrayUtils.isEmpty(registryCenterAddr)) {
            throw new RuntimeException("Empty Zookeeper Registry Addr !!");
        }
        StringBuilder builder = new StringBuilder();
        for (String r : registryCenterAddr) {
            builder.append(StringUtils.removeIgnoreCase(r, ZK_PROTOCOL)).append(",");
        }
        //connectionTimeout: Int MAX ;  sessionTimeOut:30000
        return new ZkClient(builder.substring(0, builder.length() - 1), 30000);
    }


    public static List<String> getApplications(ZkClient zkClient) {
        List<String> applications = new ArrayList<>();
        //for interfaceArch
        applications.addAll(getApplicationsForInterface(zkClient));
        //for applicationArch
        applications.addAll(getApplicationArchMeta(zkClient));
        List<String> distinctApps = applications.stream().distinct().collect(Collectors.toList());
        return distinctApps;
    }

    public static List<String> getUpInstanceAddrList(ZkClient zkClient, String... interfaceNames) {
        if (ArrayUtils.isEmpty(interfaceNames)) {
            return Collections.emptyList();
        }
        Set<String> instanceAddrSet = Sets.newHashSet();
        Map<String, Set<String>> interfaceWithVAndG = getInterfaceWithVAndG(zkClient);
        for (String interfaceName : interfaceNames) {
            if (interfaceWithVAndG.containsKey(interfaceName)) {
                instanceAddrSet.addAll(interfaceWithVAndG.get(interfaceName));
            }
        }
        zkClient.close();
        return Lists.newArrayList(instanceAddrSet);
    }

    public static List<String> getInterfaces(ZkClient zkClient) {
        List<String> distinctInterfaces = Lists.newArrayList();
        //for interfaceArch
        distinctInterfaces.addAll(getInterfaceWithVAndG(zkClient).keySet());
        //for applicationArch
        zkClient.close();
        return distinctInterfaces;
    }

    /**
     * 获取ZK中dubbo接口(拼接分组和版本)
     *
     * @param zkClient
     * @return
     */
    private static Map<String, Set<String>> getInterfaceWithVAndG(ZkClient zkClient) {
        Map<String, Set<String>> interfaces = Maps.newHashMap();
        List<String> leafNodes = getInterfaceArchMeta(zkClient);
        try {
            for (String leafNode : leafNodes) {
                String decodeUrl = URLDecoder.decode(leafNode, Charset.defaultCharset().name());
                UriComponents components = UriComponentsBuilder.fromUriString(decodeUrl).build();
                MultiValueMap<String, String> queryMap = components.getQueryParams();
                String interfaceName = queryMap.getFirst(INTERFACE);
                interfaceName = StringUtils.joinWith(":", interfaceName, getParameter(queryMap, GROUP), getParameter(queryMap, VERSION));
                interfaceName = CommonUtil.removeEnd(":", interfaceName);
                String serviceAddr = components.getScheme() + "://" + components.getHost() + ":" + components.getPort();
                Set<String> addrSet = null;
                if (interfaces.containsKey(interfaceName)) {
                    addrSet = interfaces.get(interfaceName);
                    addrSet.add(serviceAddr);
                } else {
                    addrSet = Sets.newHashSet(serviceAddr);
                }
                interfaces.put(interfaceName, addrSet);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return interfaces;
    }

    /**
     * 分离igv{interface:group:version}
     * <p>
     * xxxService ===> new String[]{"xxxService","",""}
     * xxxService:xxxGroup:xxxVersion ===> new String[]{"xxxService","xxxGroup","xxxVersion"}
     * xxxService:xxxGroup ===> new String[]{"xxxService","xxxGroup",""}
     * xxxService::xxxVersion ===> new String[]{"xxxService","","xxxVersion"}
     *
     * @param igv interface:group:version
     * @return String[]{@interfaceName, @group, @version}
     */
    public static String[] splitIgv(String igv) {
        String[] result = new String[3];
        if (StringUtils.isBlank(igv)) {
            return result;
        }
        String[] split = igv.split(":");
        for (int i = 0; i < result.length; i++) {
            result[i] = split.length > i ? split[i] : StringUtils.EMPTY;
        }
        return result;
    }


    /**
     * 获取Dubbo 元数据信息
     *
     * @param queryMap
     * @param key
     * @return
     */
    private static String getParameter(MultiValueMap<String, String> queryMap, String key) {
        //对于2.6.x版本，dubbo url中可以存在 default.xxx(全局配置)和xxx(接口级配置)， 优先使用接口级配置，当接口级配置不存在时，使用全局配置
        //对于2.7.x版本之后，dubbo url 仅存在 xxx 配置，Dubbo在注册时默认将全局配置放置 xxx 中， 如果某接口存在接口级配置，则替换 xxx 的值
        String value = queryMap.containsKey(key) ? queryMap.getFirst(key) : null;
        //for
        if (StringUtils.isBlank(value)) {
            value = queryMap.containsKey(DEFAULT_KEY_PREFIX + key) ? queryMap.getFirst(DEFAULT_KEY_PREFIX + key) : null;
        }
        return value;
    }


    /**
     * 针对与Interface架构，当该Interface下存在具体实例时，才会通过URL进行APP的聚合
     *
     * @param zkClient
     * @return
     */
    public static List<String> getApplicationsForInterface(ZkClient zkClient) {
        List<String> applications = Lists.newArrayList();
        List<String> leafNodes = getInterfaceArchMeta(zkClient);
        try {
            for (String leafNode : leafNodes) {
                String decodeUrl = URLDecoder.decode(leafNode, Charset.defaultCharset().name());
                MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(decodeUrl).build().getQueryParams();
                List<String> subApps = queryParams.get(APPLICATION);
                applications.addAll(subApps);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Lists.newArrayList(applications);
    }


    /**
     * 针对于Application架构Meta数据,判断该APP下是否存在可用实例，如果不存在，筛除
     *
     * @param zkClient
     * @return
     */
    private static List<String> getApplicationArchMeta(ZkClient zkClient) {
        List<String> apps = getSubNode(APP_ARCH_ROOT, zkClient);
        List<String> aliveApps = apps.stream().filter(a -> CollectionUtils.isNotEmpty(getSubNode(APP_ARCH_ROOT + "/" + a, zkClient))).collect(Collectors.toList());
        return aliveApps;
    }

    /**
     * 获取Interface架构Meta数据
     *
     * @param zkClient
     * @return
     */
    private static List<String> getInterfaceArchMeta(ZkClient zkClient) {
        Set<String> applications = Sets.newHashSet();
        List<String> interfaceNodes = getSubNode(INTERFACE_ARCH_ROOT, zkClient);
        if (CollectionUtils.isEmpty(interfaceNodes)) {
            logger.info("Not find sub node in {}  path", INTERFACE_ARCH_ROOT);
            return Collections.emptyList();
        }
        List<String> leafNodes = new ArrayList<>();
        for (String iNode : interfaceNodes) {
            //eg. /dubbo/org.hango.cloud.nsf.demo.stock.api.EchoExtraService/providers/{url}
            leafNodes.addAll(getSubNode(INTERFACE_ARCH_ROOT + "/" + iNode + PROVIDERS, zkClient));
        }
        return leafNodes;
    }


    private static List<String> getSubNode(String path, ZkClient zkClient) {
        try {
            return zkClient.getChildren(path);

        } catch (Throwable e) {
            logger.info("Search Zk Node fail , errMsg = {}", e.getMessage());
        }
        return Collections.emptyList();
    }

}
