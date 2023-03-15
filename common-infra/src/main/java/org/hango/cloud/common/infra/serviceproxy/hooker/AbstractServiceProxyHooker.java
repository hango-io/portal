package org.hango.cloud.common.infra.serviceproxy.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyInfo;
import org.hango.cloud.common.infra.serviceproxy.service.impl.ServiceProxyServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/7
 */
public abstract class AbstractServiceProxyHooker<T extends ServiceProxyInfo, S extends ServiceProxyDto> extends AbstractInvokeHooker<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServiceProxyHooker.class);

    @Override
    public Class aimAt() {
        return ServiceProxyServiceImpl.class;
    }


    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("getBackendServicesFromDataPlane", "doPreGetBackendServicesHook", "doPostGetBackendServicesHook"));
        return triples;
    }


    /**
     * 获取数据面对应集群中的服务前置Hook
     *
     * @param name                模糊查询服务名
     * @param virtualGwId                网关id（根据id查询网关所属的api-plane）
     * @param registryCenterType  注册中心类型
     * @return
     */
    protected void preGetBackendServicesHook(long virtualGwId, String name, String registryCenterType) {
    }

    /**
     * 执行获取数据面对应集群中的服务前置Hook
     *
     * @param name                模糊查询服务名
     * @param virtualGwId                网关id（根据id查询网关所属的api-plane）
     * @param registryCenterType  注册中心类型
     * @return
     */
    public final void doPreGetBackendServicesHook(long virtualGwId, String name, String registryCenterType) {
        logger.debug("execute pre create hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null && nextHooker instanceof AbstractServiceProxyHooker) {
            ((AbstractServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto>) nextHooker).doPreGetBackendServicesHook(virtualGwId, name, registryCenterType);
        }
        preGetBackendServicesHook(virtualGwId, name, registryCenterType);
    }

    /**
     * 获取数据面对应集群中的服务后置Hook
     *
     * @param l
     * @return
     */
    protected List<? extends BackendServiceWithPortDto> postGetBackendServicesHook(List l) {
        return l;
    }

    /**
     * 执行获取数据面对应集群中的服务后置Hook
     *
     * @param l
     * @return
     */
    public final List<? extends BackendServiceWithPortDto> doPostGetBackendServicesHook(List l) {
        logger.debug("execute post create hook ,hook is {}", this.getClass().getName());
        if (nextHooker != null && nextHooker instanceof AbstractServiceProxyHooker) {
           l = ((AbstractServiceProxyHooker<ServiceProxyInfo, ServiceProxyDto>) nextHooker).doPostGetBackendServicesHook(l);
        }
        return postGetBackendServicesHook(l);
    }
}
