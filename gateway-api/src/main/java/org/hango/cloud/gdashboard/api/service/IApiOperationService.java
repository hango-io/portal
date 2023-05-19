package org.hango.cloud.gdashboard.api.service;

import org.hango.cloud.gdashboard.api.meta.ApiInfo;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/15
 */
public interface IApiOperationService {

    /**
     * 一键复制API功能
     *
     * @param originInfo
     * @return
     */
    long copyApiInfo(ApiInfo originInfo);
}
