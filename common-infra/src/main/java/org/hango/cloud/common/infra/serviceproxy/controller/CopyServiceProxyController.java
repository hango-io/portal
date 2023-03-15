package org.hango.cloud.common.infra.serviceproxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.ICopyServiceProxy;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/9/10
 */
@RestController
@Validated
@RequestMapping(value = { BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class CopyServiceProxyController  extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(CopyServiceProxyController.class);

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private ICopyServiceProxy copyServiceProxy;




    @RequestMapping(params = {"Action=CopyServiceProxy"}, method = RequestMethod.GET)
    public Object copyServiceProxy(@RequestParam(value = "ServiceId", required = false) String serviceId,
                                   @Min(1) @RequestParam(value = "OriginGwId") long originGwId,
                                   @Min(1) @RequestParam(value = "DesGwId") long desGwId) {
        logger.info("一键复制服务，服务id，serviceId:{},originGwId:{},desGwId:{}", new Object[]{serviceId, originGwId, desGwId});
        Map<Long, List<Long>> failedRouteIdList = Maps.newHashMap();
        List<Long> serviceIds = new ArrayList<>();
        if (StringUtils.isBlank(serviceId)) {
            List<ServiceProxyDto> serviceProxyList = serviceProxyService.getServiceProxyListByVirtualGwId(originGwId);
            if (!CollectionUtils.isEmpty(serviceProxyList)) {
                serviceIds = serviceProxyList.stream().map(ServiceProxyDto::getServiceId).collect(Collectors.toList());
            }
        } else {
            serviceIds = Arrays.asList(serviceId.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        }
        ErrorCode checkResult = copyServiceProxy.checkCopyServiceProxy(serviceIds, originGwId, desGwId);
        if (!CommonErrorCode.SUCCESS.equals(checkResult)) {
            return apiReturn(checkResult);
        }
        failedRouteIdList = copyServiceProxy.copyServiceProxy(serviceIds, originGwId, desGwId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("FailedRouteIdList", JSONObject.toJSONString(failedRouteIdList));
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }


}
