package org.hango.cloud.common.advanced.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.advanced.authentication.holder.UserPermissionHolder;
import org.hango.cloud.common.advanced.base.config.CommonAdvanceConfig;
import org.hango.cloud.common.advanced.base.meta.AdvancedConst;
import org.hango.cloud.common.advanced.project.service.IPlatformPermissionScopeService;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/10/28
 */
@Service
public class PlatformPermissionScopeServiceImpl implements IPlatformPermissionScopeService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformPermissionScopeServiceImpl.class);

    public static final String PERMISSION_SCOPE_RETURN = "PermissionScopeInfos";
    @Autowired
    private CommonAdvanceConfig advanceConfig;

    private LoadingCache<Long, PermissionScopeDto> permissionScopeCache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterWrite(NumberUtils.INTEGER_ONE, TimeUnit.MINUTES).build(new CacheLoader<Long, PermissionScopeDto>() {
                @Override
                public PermissionScopeDto load(Long key) throws Exception {
                    //此处不进行load，该cache中的数据完全通过permissionScopeCache.put()设置
                    return null;
                }
            });

    @Override
    public List<PermissionScopeDto> getPermissionScope(Collection<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        List<PermissionScopeDto> permissionScopeList = Lists.newArrayList();
        List<Long> noneDataIdList = Lists.newArrayList();
        for (Long projectId : projectIds) {
            PermissionScopeDto permissionScopeDto = permissionScopeCache.getIfPresent(projectId);
            if (permissionScopeDto == null) {
                noneDataIdList.add(projectId);
                continue;
            }
            permissionScopeList.add(permissionScopeDto);
        }
        if (!CollectionUtils.isEmpty(noneDataIdList)){
            List<PermissionScopeDto> permissionScopes = getPermissionScopeFromPlatform(noneDataIdList);
            permissionScopeList.addAll(permissionScopes);
            permissionScopes.forEach(p -> permissionScopeCache.put(p.getId(), p));
        }
        return permissionScopeList;
    }


    private List<PermissionScopeDto> getPermissionScopeFromPlatform(Collection<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(AdvancedConst.USER_ACCOUNT_ID, UserPermissionHolder.getAccountId());
        headers.set(AdvancedConst.USER_PERMISSION, UserPermissionHolder.getJwt());

        Map<String, Object> params = Maps.newHashMap();
        params.put("Version", "2018-08-09");
        params.put("Action", "BatchDescribePermissionScopes");

        Map<String, Object> body = Maps.newHashMap();
        body.put("ScopeIdList", projectIds);
        HttpClientResponse authResponse = HttpClientUtil.postRequest(advanceConfig.getSkiffAuthorityAddr(), JSON.toJSONString(body), params, headers, AdvancedConst.MODULE_PLATFORM_USER_AUTH);
        if (HttpStatus.SC_OK != authResponse.getStatusCode()) {
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSON.parseObject(authResponse.getResponseBody());
        if (!jsonObject.containsKey(PERMISSION_SCOPE_RETURN)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(jsonObject.getString(PERMISSION_SCOPE_RETURN), PermissionScopeDto.class);
    }

    @Override
    public PermissionScopeDto getPermissionScope(String projectCode) {
        if (StringUtils.isBlank(projectCode)){
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(AdvancedConst.USER_ACCOUNT_ID, UserPermissionHolder.getAccountId());
        headers.set(AdvancedConst.USER_PERMISSION, UserPermissionHolder.getJwt());

        Map<String, Object> params = Maps.newHashMap();
        params.put("Version", "2018-08-09");
        params.put("Action", "DescribePermissionScope");
        params.put("PermissionScopeEnName", projectCode);

        HttpClientResponse authResponse = HttpClientUtil.getRequest(advanceConfig.getSkiffAuthorityAddr(), params, headers, AdvancedConst.MODULE_PLATFORM_USER_AUTH);
        if (HttpStatus.SC_OK != authResponse.getStatusCode()) {
            return null;
        }
        if (StringUtils.isBlank(authResponse.getResponseBody())){
            return null;
        }
        return JSONObject.parseObject(authResponse.getResponseBody(), PermissionScopeDto.class);
    }
}
