package org.hango.cloud.common.infra.route.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName CopyRouteDTO
 * @Description 路由复制返回复制后的服务ID与路由ID
 * @Author xianyanglin
 * @Date 2023/4/27 14:35
 */
public class CopyRouteDTO {
    private long routeId;
    private Set<Long> serviceIdList = new HashSet<>();

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public Set<Long> getServiceIdList() {
        return serviceIdList;
    }

    public void setServiceIdList(Set<Long> serviceIdList) {
        this.serviceIdList = serviceIdList;
    }

    public boolean isCopySuccess() {
        return this.routeId != 0;
    }
}
