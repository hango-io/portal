package org.hango.cloud.envoy.infra.serviceproxy.controller;

import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/5
 */
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2019-09-01"})
@Validated
public class EnvoyServiceProxyController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(EnvoyServiceProxyController.class);
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @RequestMapping(params = {"Action=DescribeServiceRoute"}, method = RequestMethod.GET)
    public Object getPublishedServiceInfoById(@RequestParam(value = "ServiceId") @NotNull long serviceId) {
        logger.info("查询服务的具体发布详情，serviceId:{}", serviceId);
        if (!serviceInfoService.isServiceExists(serviceId)) {
            logger.info("查询服务的具体发布详情，服务不存在");
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        List<ServiceProxyDto> serviceProxyDtos = envoyServiceProxyService.getServiceWithHealthStatus(null, serviceId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("PublishDetails", serviceProxyDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    @RequestMapping(params = {"Action=DescribeServiceProxy"}, method = RequestMethod.GET)
    public Object describeServiceProxy(@Min(1) @RequestParam(value = "ServiceId") long serviceId,
                                       @Min(1) @RequestParam(value = "VirtualGwId") long virtualGwId) {
        logger.info("根据服务id：{},网关id：{}，查询服务发布信息", serviceId, virtualGwId);
        List<ServiceProxyDto> serviceProxyDtos = envoyServiceProxyService.getServiceWithHealthStatus(virtualGwId, serviceId);
        if (CollectionUtils.isEmpty(serviceProxyDtos)){
            return apiReturnSuccess(new Result());
        }
        ServiceProxyDto serviceProxyDto = serviceProxyDtos.get(0);
        serviceProxyService.fillServicePort(serviceProxyDto);
        return apiReturn(new Result(serviceProxyDto));
    }
}
