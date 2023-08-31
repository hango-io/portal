package org.hango.cloud.envoy.advanced.manager.service;

import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

/**
 * @Author zhufengwei
 * @Date 2023/8/31
 */
public interface ICleanupService {
    /**
     * 资源清理校验
     */
    ErrorCode checkCleanupParam(String name);
    /**
     * 资源清理
     */
    void cleanup(String name);
}
