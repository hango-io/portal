package com.netease.cloud.nsf.dao;

import com.netease.cloud.nsf.dao.meta.ResourceInfo;

import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public interface ResourceDao {
    boolean contains(String name);

    boolean contains(String name, String version);

    void add(ResourceInfo resourceInfo);

    void delete(String name);

    void update(ResourceInfo resourceInfo);

    ResourceInfo get(String name);

    List<ResourceInfo> list();

    List<ResourceInfo> list(String labelMatch);
}
