package org.hango.cloud.envoy.advanced.manager.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.envoy.advanced.manager.dto.RepublishResult;

import java.util.List;

public interface IRepublishService {

    /**
     * 资源发布校验
     */
    ErrorCode checkRepublishParam(List<Long> vgId);
    /**
     * 重新发布资源
     */
    List<RepublishResult> republish(List<Long> vgId);

}
