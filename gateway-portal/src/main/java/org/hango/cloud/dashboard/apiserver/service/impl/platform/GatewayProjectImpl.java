package org.hango.cloud.dashboard.apiserver.service.impl.platform;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.httpclient.HttpStatus;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.util.Const;
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
public class GatewayProjectImpl extends CommonServiceFromPlatform implements IGatewayProjectService {
    private static final Logger logger = LoggerFactory.getLogger(GatewayProjectImpl.class);
    @Autowired
    IApiInfoService apiInfoService;
    @Autowired
    IGatewayInfoService gatewayInfoService;
    @Autowired
    IServiceInfoService serviceInfoService;
    @Autowired
    IApiModelService apiModelService;
    @Autowired
    private ApiServerConfig apiServerConfig;

    @Override
    public PermissionScopeDto getProjectScopeDto(long projectId) {
        String authorityAddr = apiServerConfig.getSkiffAuthorityAddr();
        Map<String, String> params = new HashMap<>();
        params.put("Action", "DescribePermissionScope");
        params.put("Version", "2018-08-09");
        params.put("PermissionScopeId", String.valueOf(projectId));

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());
        HttpClientResponse httpClientResponse = accessFromAuthority(authorityAddr, params, null, headers,
                Const.GET_METHOD);
        PermissionScopeDto permissionScopeDto = new PermissionScopeDto();
        if (httpClientResponse.getStatusCode() == HttpStatus.SC_OK) {
            String responseBody = httpClientResponse.getResponseBody();
            permissionScopeDto.setId(JSONObject.parseObject(responseBody).getLong("Id"));
            permissionScopeDto.setParentId(JSONObject.parseObject(responseBody).getLong("ParentId"));
            permissionScopeDto.setPermissionScopeName(JSONObject.parseObject(responseBody).getString("PermissionScopeName"));
            permissionScopeDto.setPermissionScopeEnName(JSONObject.parseObject(responseBody).getString("PermissionScopeEnName"));
        }
        return permissionScopeDto;
    }

    @Override
    public List<PermissionScopeDto> getProjectScopeList(long tenantId) {
        String authorityAddr = apiServerConfig.getSkiffAuthorityAddr();
        Map<String, String> params = new HashMap<>();
        params.put("Action", "DescribePermissionScopesBrief");
        params.put("Version", "2018-08-09");
        params.put("ParentId", String.valueOf(tenantId));

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());

        List<PermissionScopeDto> projectList = Lists.newArrayList();
        long offset = 0;

        long projectCount = 0;
        do {
            List<PermissionScopeDto> projects = getProjectIdList(authorityAddr, params, headers, 1000L, offset);
            projectList.addAll(projects);
            projectCount = projects.size();
            offset += 1000;
        } while (projectCount == 1000);

        return projectList;
    }

    private List<PermissionScopeDto> getProjectIdList(String authorityAddr, Map<String, String> params, Map<String,
            String> headers, long limit, long offset) {
        params.put("Offset", String.valueOf(offset));
        params.put("Limit", String.valueOf(limit));
        HttpClientResponse httpClientResponse = accessFromAuthority(authorityAddr, params, null, headers, Const.GET_METHOD);
        List<PermissionScopeDto> projectIdList = Lists.newArrayList();
        if (httpClientResponse.getStatusCode() == HttpStatus.SC_OK) {
            JSONObject response = JSONObject.parseObject(httpClientResponse.getResponseBody());
            if (response.containsKey("PermissionScopeInfos")) {
                JSONArray permissionScopes = response.getJSONArray("PermissionScopeInfos");
                for (int i = 0; i < permissionScopes.size(); i++) {
                    JSONObject jsonObject = permissionScopes.getJSONObject(i);
                    PermissionScopeDto permissionScopeDto = new PermissionScopeDto();
                    permissionScopeDto.setId(jsonObject.getLong("Id"));
                    permissionScopeDto.setParentId(jsonObject.getLong("ParentId"));
                    permissionScopeDto.setPermissionScopeName(jsonObject.getString("PermissionScopeName"));
                    permissionScopeDto.setPermissionScopeEnName(jsonObject.getString("PermissionScopeEnName"));
                    projectIdList.add(permissionScopeDto);
                }
            }
        }
        return projectIdList;
    }
}
