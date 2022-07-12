package com.netease.cloud.nsf.config;

import com.netease.cloud.nsf.step.Step;

/**
 * config管理接口
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public interface ConfigStore {
    /**
     * 发布集成
     * @param integrationId 集成ID
     * @param step 集成step
     */
    void publish(String integrationId, Step step);

    /**
     * 删除集成
     * @param integrationId 集成ID
     */
    void delete(String integrationId);
}
