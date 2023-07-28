package org.hango.cloud.common.infra.virtualgateway.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.meta.VirtualGateway;
import org.hango.cloud.common.infra.virtualgateway.service.impl.VirtualGatewayServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/5/6
 */
public abstract class AbstractVirtualGatewayHooker<T extends VirtualGateway, S extends VirtualGatewayDto> extends AbstractInvokeHooker<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVirtualGatewayHooker.class);

    @Override
    public Class aimAt() {
        return VirtualGatewayServiceImpl.class;
    }

    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        triples.add(MutableTriple.of("getVirtualGatewayList", StringUtils.EMPTY, "doFindMultiEnhancement"));
        triples.add(MutableTriple.of("getVirtualGatewayPage", StringUtils.EMPTY, "doFindMultiEnhancementPage"));
        triples.add(MutableTriple.of("fillVirtualGatewayInfo", "doFillVirtualGatewayInfo", StringUtils.EMPTY));
        return triples;
    }

    /**
     * 设置gateway info 前置hooker
     */
    @SuppressWarnings("unused")
    public final void doFillVirtualGatewayInfo(VirtualGatewayDto virtualGatewayDto) {
        if (nextHooker != null && nextHooker instanceof AbstractVirtualGatewayHooker) {
            ((AbstractVirtualGatewayHooker<VirtualGateway, VirtualGatewayDto>) nextHooker).doFillVirtualGatewayInfo(virtualGatewayDto);
        }
        fillVirtualGatewayInfo(virtualGatewayDto);
    }

    protected void fillVirtualGatewayInfo(VirtualGatewayDto virtualGatewayDto){
    }
}
