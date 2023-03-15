package org.hango.cloud.common.infra.serviceproxy.convert;

import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.SubsetDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SYMBOL_HYPHEN;

/**
 * @Author zhufengwei
 * @Date 2023/2/9
 */
public class ServiceProxyConvert {

    public static String getCode(String publishType, Long serviceId){
        return publishType + SYMBOL_HYPHEN + serviceId;
    }

    public static String getGateway(String gwClusterName, String code){
        return gwClusterName + SYMBOL_HYPHEN + code;
    }

    public static String getName(String publishType, Long serviceId, String gwClusterName, String code){
        String name = getCode(publishType, serviceId) + SYMBOL_HYPHEN + getGateway(gwClusterName, code);
        return name.toLowerCase();
    }

    public static List<SubsetDto> buildSubset(ServiceProxyDto serviceProxyDto, VirtualGatewayDto virtualGatewayDto){
        List<SubsetDto> subsets = serviceProxyDto.getSubsets();
        if (subsets == null){
            return new ArrayList<>();
        }
        //不修改原有subset
        List<SubsetDto> targetSubsets = new ArrayList<>();
        for (SubsetDto subset : subsets) {
            SubsetDto targetSubset = new SubsetDto();
            String name = getName(subset.getName(), serviceProxyDto.getServiceId(), virtualGatewayDto.getGwClusterName(), virtualGatewayDto.getCode());
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

}
