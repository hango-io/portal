package org.hango.cloud.dashboard.apiserver.service.impl.platform;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.gdashboard.api.service.IApiInfoService;
import org.hango.cloud.gdashboard.api.service.IApiModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GatewayProjectImpl implements IGatewayProjectService {
    private static final Logger logger = LoggerFactory.getLogger(GatewayProjectImpl.class);
    @Autowired
    IApiInfoService apiInfoService;
    @Autowired
    IGatewayInfoService gatewayInfoService;
    @Autowired
    IServiceInfoService serviceInfoService;
    @Autowired
    IApiModelService apiModelService;

    @Override
    public PermissionScopeDto getProjectScopeDto(long projectId) {
        PermissionScopeDto permissionScopeDto = new PermissionScopeDto();
        permissionScopeDto.setId(ProjectTraceHolder.getProId());
        logger.info("project id is {}",ProjectTraceHolder.getProId());
        return permissionScopeDto;
    }

    @Override
    public List<PermissionScopeDto> getProjectScopeList(long tenantId) {
        List<PermissionScopeDto> projectList = Lists.newArrayList();
        PermissionScopeDto permissionScopeDto = new PermissionScopeDto();
        permissionScopeDto.setId(ProjectTraceHolder.getProId());
        projectList.add(permissionScopeDto);
        return projectList;
    }
}
