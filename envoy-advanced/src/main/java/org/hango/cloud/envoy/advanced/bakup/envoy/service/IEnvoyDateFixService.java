package org.hango.cloud.envoy.advanced.bakup.envoy.service;


import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;

import java.util.List;

public interface IEnvoyDateFixService {
    /**
     * 重新发布已发布服务的参数校验
     *
     * @param virtualGwId                网关id
     * @param rePublishAllService 是否重新发布该网关中所有的服务
     * @param serviceIdList       需要重新发布的服务id列表，非必填，只有当rePublishAllService为false时才使用该参数
     * @return {@link ErrorCode} 参数校验结果，当返回 {@link CommonErrorCode#Success} 时参数校验成功，否则参数校验失败并返回对应的错误信息
     */
    ErrorCode checkRePublishServiceParam(long virtualGwId, boolean rePublishAllService, List<Long> serviceIdList);

    /**
     * 重新发布已发布服务
     *
     * @param virtualGwId                网关id
     * @param rePublishAllService 是否重新发布该网关中所有的服务
     * @param serviceProxyIdList       需要发布的服务id列表，非必填，只有当rePublishAllService为false时才使用该参数
     * @return 重新发布失败的服务id列表
     */
    List<Long> rePublishService(long virtualGwId, boolean rePublishAllService, List<Long> serviceProxyIdList);

    /**
     * 重新发布已发布路由规则的参数校验
     *
     * @param virtualGwId                  网关id
     * @param rePublishAllRouteRule 是否重新发布该网关中所有的路由规则
     * @param serviceIdList         需要重新发布的服务id列表，非必填，只有当rePublishAllRouteRule为false时才使用该参数，会发布指定服务下的全部路由规则
     * @param routeRuleIdList       需要重新发布的路由规则id列表，非必填，只有当rePublishAllRouteRule为false且serviceIdList为空时才使用该参数
     * @return {@link ErrorCode} 参数校验结果，当返回 {@link CommonErrorCode#Success} 时参数校验成功，否则参数校验失败并返回对应的错误信息
     */
    ErrorCode checkRePublishRouteRuleParam(long virtualGwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList);

    /**
     * 重新发布已发布路由规则
     *
     * @param virtualGwId                  网关id
     * @param rePublishAllRouteRule 是否重新发布该网关中所有的路由规则
     * @param serviceIdList         需要重新发布的服务id列表，非必填，只有当rePublishAllRouteRule为false时才使用该参数，会发布指定服务下的全部路由规则
     * @param routeRuleIdList       需要重新发布的路由规则id列表，非必填，只有当rePublishAllRouteRule为false且serviceIdList为空时才使用该参数
     * @return 重新发布失败的路由规则id列表
     */
    List<Long> rePublishRouteRule(long virtualGwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList);

    List<Long> reFixPublishedRouteDao(long virtualGwId);

    Boolean fixAuthPluginConfig(long virtualGwId);

}
