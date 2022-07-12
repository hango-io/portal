package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import com.netease.cloud.ncegdashboard.envoy.web.dto.PublishResultDto;
import com.netease.cloud.ncegdashboard.envoy.web.dto.RePublishPluginDto;

import java.util.List;

public interface IEnvoyDateFixService {
    /**
     * 重新发布已发布服务的参数校验
     *
     * @param gwId                网关id
     * @param rePublishAllService 是否重新发布该网关中所有的服务
     * @param serviceIdList       需要重新发布的服务id列表，非必填，只有当rePublishAllService为false时才使用该参数
     * @return {@link ErrorCode} 参数校验结果，当返回 {@link CommonErrorCode#Success} 时参数校验成功，否则参数校验失败并返回对应的错误信息
     */
    ErrorCode checkRePublishServiceParam(long gwId, boolean rePublishAllService, List<Long> serviceIdList);

    /**
     * 重新发布已发布服务
     *
     * @param gwId                网关id
     * @param rePublishAllService 是否重新发布该网关中所有的服务
     * @param serviceIdList       需要发布的服务id列表，非必填，只有当rePublishAllService为false时才使用该参数
     * @return 重新发布失败的服务id列表
     */
    List<Long> rePublishService(long gwId, boolean rePublishAllService, List<Long> serviceIdList);

    /**
     * 重新发布已发布路由规则的参数校验
     *
     * @param gwId                  网关id
     * @param rePublishAllRouteRule 是否重新发布该网关中所有的路由规则
     * @param serviceIdList         需要重新发布的服务id列表，非必填，只有当rePublishAllRouteRule为false时才使用该参数，会发布指定服务下的全部路由规则
     * @param routeRuleIdList       需要重新发布的路由规则id列表，非必填，只有当rePublishAllRouteRule为false且serviceIdList为空时才使用该参数
     * @return {@link ErrorCode} 参数校验结果，当返回 {@link CommonErrorCode#Success} 时参数校验成功，否则参数校验失败并返回对应的错误信息
     */
    ErrorCode checkRePublishRouteRuleParam(long gwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList);

    /**
     * 重新发布已发布路由规则
     *
     * @param gwId                  网关id
     * @param rePublishAllRouteRule 是否重新发布该网关中所有的路由规则
     * @param serviceIdList         需要重新发布的服务id列表，非必填，只有当rePublishAllRouteRule为false时才使用该参数，会发布指定服务下的全部路由规则
     * @param routeRuleIdList       需要重新发布的路由规则id列表，非必填，只有当rePublishAllRouteRule为false且serviceIdList为空时才使用该参数
     * @return 重新发布失败的路由规则id列表
     */
    List<Long> rePublishRouteRule(long gwId, boolean rePublishAllRouteRule, List<Long> serviceIdList, List<Long> routeRuleIdList);

    List<Long> reFixPublishedRouteDao(long gwId);

    Boolean fixAuthPluginConfig(long gwId);

    /**
     * 重新发布插件
     * @param rePublishPluginDto 需要重新发布的插件
     * @return 发布结果
     */
    PublishResultDto rePublishPlugin(RePublishPluginDto rePublishPluginDto);
}
