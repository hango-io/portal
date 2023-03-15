package org.hango.cloud.envoy.infra.serviceproxy.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.base.invoker.MethodAroundHolder;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.hooker.AbstractServiceProxyHooker;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.envoy.infra.serviceproxy.service.impl.EnvoyServiceProxyServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractEnvoyServiceProxyHooker<T extends ServiceProxyInfo, S extends ServiceProxyDto> extends AbstractInvokeHooker<T, S> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEnvoyServiceProxyHooker.class);

    @Override
    public Class aimAt() {
        return EnvoyServiceProxyServiceImpl.class;
    }


    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("getExtraServiceParams", StringUtils.EMPTY, "doPostGetExtraServiceParamsHook"));
        return triples;
    }

    /**
     * 查询服务获取额外参数方法的后置Hook，具体实现见各增强包
     *
     * @param registry 注册中心类型
     * @return 额外的参数
     */
    protected Map<String, String> postGetExtraServiceParamsHook(String registry) {
        return Collections.emptyMap();
    }

    /**
     * 查询服务获取额外参数方法的后置Hook
     *
     * @param paramsMap 额外的参数
     * @return 额外的参数
     */
    public final Map<String, String> doPostGetExtraServiceParamsHook(Map<String, String> paramsMap) {
        if (nextHooker != null && nextHooker instanceof AbstractServiceProxyHooker) {
            paramsMap = ((AbstractEnvoyServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto>) nextHooker)
                    .doPostGetExtraServiceParamsHook(paramsMap);
        }
        String registry = (String) MethodAroundHolder.getParam();
        return postGetExtraServiceParamsHook(registry);
    }
}
