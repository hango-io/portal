package org.hango.cloud.common.infra.serviceproxy.controller;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.service.IRegistryCenterService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务元数据发布至网关相关controller
 * 指服务元数据和网关服务进行关联，便于控制面展示
 *
 * @author hanjiahao
 */
@RestController
@Validated
@RequestMapping(value = {BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class ServiceProxyController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyController.class);

    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IRegistryCenterService registryCenterService;
    @Autowired
    private IServiceInfoService serviceInfoService;

    @RequestMapping(params = {"Action=DescribeServiceListByGw"}, method = RequestMethod.GET)
    public String describeServiceList(@RequestParam(value = "VirtualGwId") long virtualGwId,
                                      @RequestParam(value = "Name", required = false) String name,
                                      @RequestParam(value = "RegistryCenterType") String registryCenterType) {
        logger.info("查询网关id：{}下的所有发布服务name:{}", virtualGwId, name);
        Map<String, Object> result = Maps.newHashMap();
        List<BackendServiceWithPortDto> serviceListFromDataPlane =
                serviceProxyService.getBackendServicesFromDataPlane(virtualGwId, name, registryCenterType);
        result.put("ServiceList", serviceListFromDataPlane.stream().limit(Const.MAX_COUNT).map(BackendServiceWithPortDto::getName).collect(Collectors.toList()));
        return apiReturnSuccess(result);
    }

    @MethodReentrantLock
    @Audit(eventName = "PublishService", description = "发布服务")
    @RequestMapping(params = {"Action=PublishService"}, method = RequestMethod.POST)
    public String publishService(@Validated @RequestBody ServiceProxyDto serviceProxyDto) {
        logger.info("发布服务至网关，服务发布信息ServiceProxyDto:{}", serviceProxyDto);
        ErrorCode errorCode = serviceProxyService.checkCreateParam(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long id = serviceProxyService.create(serviceProxyDto);
        Map<String, Object> result = Maps.newHashMap();
        result.put("Id", id);
        return apiReturnSuccess(result);
    }

    @MethodReentrantLock
    @Audit(eventName = "UpdatePublishService", description = "更新服务发布信息")
    @RequestMapping(params = {"Action=UpdatePublishService"}, method = RequestMethod.POST)
    public String updatePublishService(@Validated @RequestBody ServiceProxyDto serviceProxyDto) {
        logger.info("更新服务发布信息ServiceProxyDto:{}", serviceProxyDto);
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //更新时，如果发现已被路由规则引用的版本被删除则不允许删除
        errorCode = serviceProxyService.getRouteRuleNameWithServiceSubset(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long id = serviceProxyService.update(serviceProxyDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("Id", serviceProxyDto.getId());
        return apiReturnSuccess(result);
    }

    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteServiceProxy"}, method = RequestMethod.GET)
    public String deleteServiceProxy(@Min(1) @RequestParam(value = "VirtualGwId") long virtualGwId,
                                     @Min(1) @RequestParam(value = "ServiceId") long serviceId) {
        logger.info("下线已经关联的服务，virtualGwId:{},serviceId:{}", new Object[]{virtualGwId, serviceId});
        ServiceProxyDto serviceProxyDto = serviceProxyService.getServiceProxyByServiceIdAndGwId(virtualGwId, serviceId);
        if (serviceProxyDto == null) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }

        ErrorCode errorCode = serviceProxyService.checkDeleteParam(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        serviceProxyService.delete(serviceProxyDto);
        return apiReturn(CommonErrorCode.SUCCESS);
    }

    @RequestMapping(params = {"Action=DescribeServiceProxyList"}, method = RequestMethod.GET)
    public Object serviceProxyList(@RequestParam(value = "VirtualGwId", required = false, defaultValue = "0") long virtualGwId,
                                   @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                   @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                   @RequestParam(value = "Pattern", required = false) String pattern) {
        logger.info("分页查询service proxy list,gatewayId:{}, pattern:{}", virtualGwId, pattern);
        long projectId = ProjectTraceHolder.getProId();
        List<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxyWithPort(virtualGwId, pattern, projectId, offset, limit);
        long serviceProxyCount = serviceProxyService.getServiceProxyCount(virtualGwId, pattern, projectId);
        Map<String, Object> result = Maps.newHashMap();
        result.put(TOTAL_COUNT, serviceProxyCount);
        result.put("ServiceProxyList", serviceProxy);
        return apiReturn(HttpStatus.SC_OK, StringUtils.EMPTY, StringUtils.EMPTY, result);
    }



    /**
     * 获取项目下所有服务
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllService"})
    public String getServiceList(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        logger.info("开始查询接入网关的所有服务");
        List<String> services = serviceProxyService.getAllServiceTag(virtualGwId);
        return apiReturnSuccess(services);
    }
}
