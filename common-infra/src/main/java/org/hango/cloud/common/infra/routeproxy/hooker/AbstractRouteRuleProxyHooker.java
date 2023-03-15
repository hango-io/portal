package org.hango.cloud.common.infra.routeproxy.hooker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.meta.RouteRuleProxyInfo;
import org.hango.cloud.common.infra.routeproxy.service.impl.RouteRuleProxyServiceImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRouteRuleProxyHooker<T extends RouteRuleProxyInfo, S extends RouteRuleProxyDto> extends AbstractInvokeHooker<T, S> {
    @Override
    public Class aimAt() {
        return RouteRuleProxyServiceImpl.class;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}