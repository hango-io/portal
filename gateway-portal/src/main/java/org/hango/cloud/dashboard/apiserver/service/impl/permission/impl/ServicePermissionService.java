package org.hango.cloud.dashboard.apiserver.service.impl.permission.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.config.ApiServerConfig;
import org.hango.cloud.dashboard.apiserver.meta.HttpClientResponse;
import org.hango.cloud.dashboard.apiserver.meta.enums.permission.ActionPermissionEnum;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.IServicePermissionService;
import org.hango.cloud.dashboard.apiserver.service.impl.permission.handler.BaseSpecResourceHandler;
import org.hango.cloud.dashboard.apiserver.util.AccessUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanjiahao
 * 判断是否具有访问权限service
 */
@Service
public class ServicePermissionService implements IServicePermissionService {

    private static final Logger logger = LoggerFactory.getLogger(ServicePermissionService.class);

    @Autowired
    private ApiServerConfig apiServerConfig;


    @Override
    public boolean hasRole(String token, HttpServletRequest request) {
        return hasRole(token, StringUtils.EMPTY, String.valueOf(ProjectTraceHolder.getProId()), ActionPermissionEnum.getActionPermissionEnum(request.getParameter(Const.ACTION)), request);
    }

    @Override
    public boolean hasRoleWithToken(String token, String permissionScopeId, ActionPermissionEnum actionPermissionEnum) {
        return hasRole(token, StringUtils.EMPTY, permissionScopeId, actionPermissionEnum, null);
    }

    @Override
    public boolean hasRoleWithAccount(String account, String permissionScopeId, ActionPermissionEnum actionPermissionEnum) {
        return hasRole(StringUtils.EMPTY, account, permissionScopeId, actionPermissionEnum, null);
    }


    @Override
    public boolean hasAccessAuthority(String account, long tenantId, long projectId, long offset) {
        Map<String, String> params = new HashMap<>();

        params.put("ParentId", String.valueOf(tenantId));
        params.put("AccountId", account);
        params.put("Action", "DescribeUserPermissionScopes");
        params.put("Version", "2018-08-09");
        params.put("Offset", String.valueOf(offset));
        params.put("Limit", String.valueOf(1000));

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());

        HttpClientResponse authResponse;
        try {
            authResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getSkiffAuthorityAddr(), params, null, headers, Const.GET_METHOD);
            JSONObject jsonObject = JSONObject.parseObject(authResponse.getResponseBody());
            int count = jsonObject.getInteger("TotalCount");
            if (NumberUtils.INTEGER_ZERO.equals(count) && NumberUtils.LONG_ZERO.equals(projectId)) {
                logger.info("租户 {} 下无项目，验证成功，放行", tenantId);
                return true;
            }
            JSONArray permissionScopeInfos = jsonObject.getJSONArray("PermissionScopeInfos");
            if (CollectionUtils.isEmpty(permissionScopeInfos)) {
                return false;
            }
            for (Object childJson : permissionScopeInfos) {
                if (JSON.parseObject(String.valueOf(childJson)).getLong("Id") == projectId) {
                    return true;
                }
            }
            offset += permissionScopeInfos.size();
            if (count > offset) {
                return hasAccessAuthority(account, tenantId, projectId, offset);
            }
            return false;
        } catch (Exception e) {
            logger.info("UserAuth Exception :", e.getMessage());
            return false;
        }
    }

    /**
     * 判断是否具有操作权限
     *
     * @param token
     * @param accountId
     * @param permissionScopeId
     * @param actionPermissionEnum
     * @return
     */
    private boolean hasRole(String token, String accountId, String permissionScopeId, ActionPermissionEnum actionPermissionEnum, HttpServletRequest request) {
        //需要鉴权的action
        if (actionPermissionEnum == null) {
            return true;
        }
        Map<String, String> params = new HashMap<>();

        params.put("PermissionScopeId", permissionScopeId);
        params.put("JWT", token);
        params.put("AccountId", accountId);
        params.put("Action", "Authentication");
        params.put("Version", "2018-08-09");
        params.put("ServiceModule", Const.SERVICE_MODULE);

        params.put("ResourceType", actionPermissionEnum.getResource());
        params.put("OperationType", actionPermissionEnum.getOperation());

        Map<String, String> headers = new HashMap<>();
        headers.put("x-auth-accountId", apiServerConfig.getPermissionScopeAccount());

        BaseSpecResourceHandler specResourceHandler = actionPermissionEnum.getSpecResourceHandler();
        if (specResourceHandler != null) {
            Object resourceNameObj = specResourceHandler.handle(request);
            List<String> resourceNameList = new ArrayList();
            if (resourceNameObj instanceof Collection) {
                for (Object gwIdOb : ((Collection) resourceNameObj)) {
                    resourceNameList.add(String.valueOf(gwIdOb));
                }
            } else {
                resourceNameList.add(String.valueOf(resourceNameObj));
            }
            params.put("ResourceNames", JSON.toJSONString(resourceNameList));
        }
        HttpClientResponse authResponse;
        try {
            authResponse = AccessUtil.accessFromOtherPlat(apiServerConfig.getSkiffAuthorityAddr(), params, null,
                    headers, Const.GET_METHOD);
            boolean hasRole = JSONObject.parseObject(authResponse.getResponseBody()).getBoolean("HasRole");
            if (hasRole) {
                accountId = JSONObject.parseObject(authResponse.getResponseBody()).getString("AccountId");
                UserPermissionHolder.setAccountId(accountId);
            }
            return hasRole;
        } catch (Exception e) {
            logger.info("UserAuth Exception :", e.getMessage());
            return false;
        }
    }

}
