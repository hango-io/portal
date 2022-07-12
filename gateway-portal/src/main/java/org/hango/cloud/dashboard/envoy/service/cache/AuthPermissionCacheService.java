package org.hango.cloud.dashboard.envoy.service.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.envoy.service.IAuthPermissionService;
import org.hango.cloud.dashboard.envoy.web.dto.auth.AuthPermissionObjectDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthPermissionCacheService {

    private static final Logger logger = LoggerFactory.getLogger(AuthPermissionCacheService.class);

    @Autowired
    private IAuthPermissionService authPermissionService;


    private LoadingCache<Long, Map<String, AuthPermissionObjectDto>> authServicePermissionCache =
            CacheBuilder.newBuilder().maximumSize(10000).refreshAfterWrite(3, TimeUnit.MINUTES).build(
                    new CacheLoader<Long, Map<String, AuthPermissionObjectDto>>() {
                        @Override
                        public Map<String, AuthPermissionObjectDto> load(final Long gwId) throws Exception {
                            List<AuthPermissionObjectDto> authPermissionObjectDtos =
                                    authPermissionService.describeAuthPermissionList(gwId, Lists.newArrayList(), Const.AUTH_GATEWAY_SERVICE);
                            if (CollectionUtils.isEmpty(authPermissionObjectDtos)) {
                                return Collections.emptyMap();
                            }
                            return authPermissionObjectDtos.stream().collect(
                                    Collectors.toMap(item -> item.getAuthorizationObjectId(), item -> item));
                        }
                    });

    private LoadingCache<Long, Map<String, AuthPermissionObjectDto>> authRoutePermissionCache =
            CacheBuilder.newBuilder().maximumSize(10000).refreshAfterWrite(3, TimeUnit.MINUTES).build(
                    new CacheLoader<Long, Map<String, AuthPermissionObjectDto>>() {
                        @Override
                        public Map<String, AuthPermissionObjectDto> load(final Long gwId) throws Exception {
                            List<AuthPermissionObjectDto> authPermissionObjectDtos =
                                    authPermissionService.describeAuthPermissionList(gwId, Lists.newArrayList(), Const.AUTH_GATEWAY_ROUTE);
                            if (CollectionUtils.isEmpty(authPermissionObjectDtos)) {
                                return Collections.emptyMap();
                            }
                            return authPermissionObjectDtos.stream().collect(
                                    Collectors.toMap(item -> item.getAuthorizationObjectId(), item -> item));
                        }
                    });

    public void invalidAuthServiceCache(long gwId) {
        authServicePermissionCache.invalidate(gwId);
    }

    public void invalidAuthRouteCache(long gwId) {
        authRoutePermissionCache.invalidate(gwId);
    }

    public Map<String, AuthPermissionObjectDto> getAuthServiceFromCache(long gwId) {
        try {
            logger.info("通过缓存获取auth_service_permission， gw_id:{}", gwId);
            return authServicePermissionCache.get(gwId);
        } catch (ExecutionException e) {
            logger.error("get authservice from cache error, e:{}", e);
            return Collections.emptyMap();
        }
    }

    public Map<String, AuthPermissionObjectDto> getAuthRouteFromCache(long gwId) {
        try {
            logger.info("通过缓存获取auth_route_permission，gw_id:{}", gwId);
            return authRoutePermissionCache.get(gwId);
        } catch (ExecutionException e) {
            logger.error("get authroute from cache error, e:{}", e);
            return Collections.emptyMap();
        }
    }
}
