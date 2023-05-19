package org.hango.cloud.common.infra.gateway.hooker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.meta.Gateway;
import org.hango.cloud.common.infra.gateway.service.impl.GatewayServiceImpl;
import org.hango.cloud.common.infra.virtualgateway.hooker.AbstractVirtualGatewayHooker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/25
 */
public abstract class AbstractGatewayHooker<T extends Gateway, S extends GatewayDto> extends AbstractInvokeHooker<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVirtualGatewayHooker.class);

    @Override
    public Class aimAt() {
        return GatewayServiceImpl.class;
    }

    @Override
    protected List<Triple<String, String, String>> put() {
        List<Triple<String, String, String>> triples = Lists.newArrayList();
        return triples;
    }
}
