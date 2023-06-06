package org.hango.cloud.envoy.infra.virtualgateway.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.envoy.infra.virtualgateway.dto.KubernetesGatewayInfo;
import org.hango.cloud.envoy.infra.virtualgateway.service.impl.KubernetesGatewayServiceImpl;

import java.util.List;

/**
 * @Author zhufengwei
 * @Date 2023/6/6
 */
public abstract class AbstractKubernetesVgHooker<T extends VirtualGateway, S extends VirtualGatewayDto> extends AbstractInvokeHooker<T, S> {


    @Override
    public Class aimAt() {
        return KubernetesGatewayServiceImpl.class;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("fillGatewayInfo", "doFillProjectId", StringUtils.EMPTY));
        return triples;
    }


    /**
     * 设置gateway info 前置hooker
     */
    @SuppressWarnings("unused")
    public final void doFillProjectId(List<KubernetesGatewayInfo> gatewayInfoList) {
        if (nextHooker != null && nextHooker instanceof AbstractKubernetesVgHooker) {
           ((AbstractKubernetesVgHooker<VirtualGateway, VirtualGatewayDto>) nextHooker).doFillProjectId(gatewayInfoList);
        }
         fillProjectId(gatewayInfoList);
    }

    protected void fillProjectId(List<KubernetesGatewayInfo> gatewayInfoList){
        for (KubernetesGatewayInfo kubernetesGatewayInfo : gatewayInfoList) {
            String projectCode = kubernetesGatewayInfo.getProjectCode();
            if (BaseConst.HANGO.equals(projectCode)){
                kubernetesGatewayInfo.setProjectId(NumberUtils.LONG_ONE);
            }
        }
    }

}
