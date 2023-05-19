package org.hango.cloud.common.infra.route.hooker;

import org.hango.cloud.common.infra.base.invoker.AbstractInvokeHooker;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.service.impl.RouteServiceImpl;

/**
 * @author yutao04
 */
public abstract class AbstractRouteHooker<T extends RoutePO, S extends RouteDto> extends AbstractInvokeHooker<T, S> {
    @Override
    public Class aimAt() {
        return RouteServiceImpl.class;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}