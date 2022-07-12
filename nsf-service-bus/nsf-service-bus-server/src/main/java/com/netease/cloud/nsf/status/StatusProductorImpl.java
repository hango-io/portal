package com.netease.cloud.nsf.status;

import com.netease.cloud.nsf.dao.StatusDao;
import com.netease.cloud.nsf.dao.meta.StatusInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusProductorImpl implements StatusProductor {
    private StatusDao dao;

    public StatusProductorImpl(StatusDao dao) {
        this.dao = dao;
    }

    @Override
    public Status product() {
        List<StatusInfo> statusInfos = dao.list();
        List<Status.Property> properties = new ArrayList<>();
        statusInfos.forEach(item -> {
            properties.add(new Status.Property(item.getName(), item.getValue()));
        });
        return new Status(properties.toArray(new Status.Property[0]));
    }
}
