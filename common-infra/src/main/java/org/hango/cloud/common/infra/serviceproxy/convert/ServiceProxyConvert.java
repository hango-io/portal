package org.hango.cloud.common.infra.serviceproxy.convert;

import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceproxy.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

/**
 * @Author zhufengwei
 * @Date 2023/2/9
 */
public class ServiceProxyConvert {

    private static String getCode(String publishType, Long projectId, String name) {
        return publishType + SYMBOL_HYPHEN + projectId + SYMBOL_HYPHEN + name;
    }

    public static String getCode(ServiceProxyDto serviceProxyDto) {
        return getCode(serviceProxyDto.getPublishType(), serviceProxyDto.getProjectId(), serviceProxyDto.getName());
    }

    public static String getGateway(String gwClusterName, String code) {
        return gwClusterName + SYMBOL_HYPHEN + code;
    }

    public static String getSubSetName(String subSetName, ServiceProxyDto serviceProxyDto) {
        String name = subSetName + SYMBOL_HYPHEN + getCode(serviceProxyDto) + SYMBOL_HYPHEN +
                getGateway(serviceProxyDto.getGwClusterName(), serviceProxyDto.getVirtualGwCode());
        return name.toLowerCase();
    }

    public static List<SubsetDto> buildSubset(ServiceProxyDto serviceProxyDto) {
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        if (subsets == null) {
            return new ArrayList<>();
        }
        //不修改原有subset
        List<SubsetDto> targetSubsets = new ArrayList<>();
        for (SubsetDto subset : subsets) {
            SubsetDto targetSubset = new SubsetDto();
            String name = getSubSetName(subset.getName(), serviceProxyDto);
            targetSubset.setName(name);
            targetSubset.setLabels(subset.getLabels());
            targetSubset.setStaticAddrList(subset.getStaticAddrList());
            targetSubset.setTrafficPolicy(subset.getTrafficPolicy());
            if (BaseConst.STATIC_PUBLISH_TYPE.equals(serviceProxyDto.getPublishType())) {
                HashMap<String, String> labelMap = new HashMap<>();
                labelMap.put(BaseConst.STATIC_EP_VERSION, name);
                targetSubset.setLabels(labelMap);
            }
            targetSubsets.add(targetSubset);
        }
        return targetSubsets;
    }


    public static void fillTrafficPolicy(ServiceProxyDto serviceProxyDto) {
        if (serviceProxyDto.getTrafficPolicy() == null){
            serviceProxyDto.setTrafficPolicy(new ServiceTrafficPolicyDto());
        }
        //处理服务TrafficPolicy
        fillTrafficPolicy(serviceProxyDto.getTrafficPolicy());
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        if (subsets != null){
            //处理subset TrafficPolicy
            for (SubsetDto subset : subsets) {
                if (subset.getTrafficPolicy() == null){
                    subset.setTrafficPolicy(new ServiceTrafficPolicyDto());
                }
                fillTrafficPolicy(subset.getTrafficPolicy());
            }
        }
    }


    public static void fillTrafficPolicy(ServiceTrafficPolicyDto trafficPolicy) {
        if (trafficPolicy.getLoadBalancer() == null){
            ServiceLoadBalancerDto serviceLoadBalancerDto = new ServiceLoadBalancerDto();
            serviceLoadBalancerDto.setSimple(SERVICE_LOADBALANCER_SIMPLE_ROUND_ROBIN);
            serviceLoadBalancerDto.setType(SERVICE_LOADBALANCER_SIMPLE);
            trafficPolicy.setLoadBalancer(serviceLoadBalancerDto);
        }

        clearLoadBalancer(trafficPolicy.getLoadBalancer());

        if (trafficPolicy.getConnectionPoolDto() == null){
            trafficPolicy.setConnectionPoolDto(new ServiceConnectionPoolDto());
        }
        ServiceConnectionPoolDto connectionPoolDto = trafficPolicy.getConnectionPoolDto();
        if (connectionPoolDto.getServiceHttpConnectionPoolDto() == null){
            ServiceConnectionPoolDto.ServiceHttpConnectionPoolDto  httpConnectionPoolDto= new ServiceConnectionPoolDto.ServiceHttpConnectionPoolDto();
            httpConnectionPoolDto.setHttp1MaxPendingRequests(1024);
            httpConnectionPoolDto.setHttp2MaxRequests(1024);
            httpConnectionPoolDto.setIdleTimeout(3000);
            httpConnectionPoolDto.setMaxRequestsPerConnection(0);
            connectionPoolDto.setServiceHttpConnectionPoolDto(httpConnectionPoolDto);
        }
        if (connectionPoolDto.getServiceTcpConnectionPoolDto() == null){
            ServiceConnectionPoolDto.ServiceTcpConnectionPoolDto tcpConnectionPoolDto = new ServiceConnectionPoolDto.ServiceTcpConnectionPoolDto();
            tcpConnectionPoolDto.setMaxConnections(1024);
            tcpConnectionPoolDto.setConnectTimeout(60000);
            connectionPoolDto.setServiceTcpConnectionPoolDto(tcpConnectionPoolDto);
        }
        ServiceConnectionPoolDto.ServiceHttpConnectionPoolDto httpConnectionPoolDto = connectionPoolDto.getServiceHttpConnectionPoolDto();
        if (httpConnectionPoolDto.getHttp2MaxRequests() == null){
            httpConnectionPoolDto.setHttp2MaxRequests(1024);
        }
        if (httpConnectionPoolDto.getHttp1MaxPendingRequests() == null){
            httpConnectionPoolDto.setHttp1MaxPendingRequests(1024);
        }
        if (httpConnectionPoolDto.getIdleTimeout() == null){
            httpConnectionPoolDto.setIdleTimeout(3000);
        }
        if (httpConnectionPoolDto.getMaxRequestsPerConnection() == null){
            httpConnectionPoolDto.setMaxRequestsPerConnection(0);
        }

        ServiceConnectionPoolDto.ServiceTcpConnectionPoolDto tcpConnectionPoolDto = connectionPoolDto.getServiceTcpConnectionPoolDto();
        if (tcpConnectionPoolDto.getConnectTimeout() == null){
            tcpConnectionPoolDto.setConnectTimeout(60000);
        }
        if (tcpConnectionPoolDto.getMaxConnections() == null){
            tcpConnectionPoolDto.setMaxConnections(1024);
        }
    }



    private static void clearLoadBalancer(ServiceLoadBalancerDto loadBalancer){
        if (BaseConst.SERVICE_LOADBALANCER_SIMPLE.equals(loadBalancer.getType())) {
            loadBalancer.setConsistentHash(null);
        }
    }
}
