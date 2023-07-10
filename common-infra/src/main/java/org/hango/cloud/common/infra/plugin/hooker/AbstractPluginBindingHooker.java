package org.hango.cloud.common.infra.plugin.hooker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.service.impl.PluginServiceInfoImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xin li
 * @date 2022/9/21 17:21
 */
public abstract class AbstractPluginBindingHooker<T extends PluginBindingInfo,S extends PluginBindingDto> extends AbstractInvokeHooker<T,S> {
    @Override
    public Class aimAt() {
        return PluginServiceInfoImpl.class;
    }

    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = new ArrayList<>();
        triples.add(MutableTriple.of("copyGlobalPluginToGatewayByVirtualGwId", "doPreCopyGlobalPluginToGatewayByVirtualGwId", StringUtils.EMPTY));
        return triples;
    }
    @Override
    public int getOrder() {
        return 0;
    }

    @SuppressWarnings("unused")
    public boolean doPreCopyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPlugin) {
        boolean result = preCopyGlobalPluginToGatewayByVirtualGwId(copyGlobalPlugin);
        if (nextHooker != null && nextHooker instanceof AbstractPluginBindingHooker) {
            result = ((AbstractPluginBindingHooker) nextHooker).doPreCopyGlobalPluginToGatewayByVirtualGwId(copyGlobalPlugin);
        }
        return result;
    }

    protected abstract boolean preCopyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPlugin);

}
