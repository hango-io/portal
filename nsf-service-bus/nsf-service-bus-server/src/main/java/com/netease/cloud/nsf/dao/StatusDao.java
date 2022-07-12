package com.netease.cloud.nsf.dao;


import com.netease.cloud.nsf.dao.meta.StatusInfo;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/14
 **/
public interface StatusDao {
    String get(String name);

    void update(StatusInfo statusInfo);

    List<StatusInfo> list();
}
