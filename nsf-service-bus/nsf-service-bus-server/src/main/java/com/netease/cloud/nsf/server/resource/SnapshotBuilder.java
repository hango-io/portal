package com.netease.cloud.nsf.server.resource;

import nsb.route.Service;

/**
 * 配置快照生成类
 *
 * @author wupenghuai@corp.netease.com
 * @date 2020/8/3
 **/
public interface SnapshotBuilder {
    Service.Resources build();
}
