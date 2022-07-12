package com.netease.cloud.nsf.server.resource;

import com.netease.cloud.nsf.dao.ResourceDao;
import com.netease.cloud.nsf.dao.meta.ResourceInfo;
import nsb.route.ResourceOuterClass;
import nsb.route.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/8/20
 **/
public class DBSnapshotBuilder implements SnapshotBuilder {

    private ResourceDao resourceDao;

    public DBSnapshotBuilder(ResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }

    @Override
    public Service.Resources build() {
        Service.Resources.Builder builder = Service.Resources.newBuilder();

        List<ResourceInfo> resourceInfos = resourceDao.list();
        List<ResourceOuterClass.Resource> resources = resourceInfos.parallelStream().map(this::getResource).collect(Collectors.toList());
        builder.addAllResources(resources);
        builder.setNonce(new Date().toString());
        return builder.build();
    }

    private ResourceOuterClass.Resource getResource(ResourceInfo info) {
        ResourceOuterClass.Resource.Builder builder = ResourceOuterClass.Resource.newBuilder();
        builder.setMetadata(ResourceOuterClass.Metadata.newBuilder().setName(info.getName()).setVersion(info.getVersion())).setBody(info.getBody());
        return builder.build();
    }
}
