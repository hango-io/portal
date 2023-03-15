package org.hango.cloud.common.infra.api.service;

import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.gdashboard.api.service.IGetProjectIdService;
import org.springframework.stereotype.Service;

@Service
public class GetProjectIdServiceImpl implements IGetProjectIdService {
    @Override
    public long getProjectId() {
        return ProjectTraceHolder.getProId();
    }
}
