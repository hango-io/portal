package org.hango.cloud.dashboard.apiserver.service.impl;

import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.springframework.stereotype.Service;

@Service
public class GetProjectIdServiceImpl implements IGetProjectIdService {
    @Override
    public long getProjectId() {
        return ProjectTraceHolder.getProId();
    }
}
