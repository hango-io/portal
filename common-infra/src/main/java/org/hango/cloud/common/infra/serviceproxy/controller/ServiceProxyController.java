package org.hango.cloud.common.infra.serviceproxy.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.ApiConst;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.BackendServiceWithPortDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyUpdateDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
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
@RequestMapping(value = {ApiConst.HANGO_SERICE_V1_PREFIX})
public class ServiceProxyController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProxyController.class);

    @Autowired
    private IServiceProxyService serviceProxyService;

    @RequestMapping(params = {"Action=DescribeServiceListByGw", "Version=2019-09-01"}, method = RequestMethod.GET)
    public String describeServiceList(@RequestParam(value = "VirtualGwId") long virtualGwId,
                                      @RequestParam(value = "Name", required = false) String name,
                                      @RequestParam(value = "RegistryCenterType") String registryCenterType) {
        logger.info("查询网关id：{}下的所有发布服务name:{}", virtualGwId, name);
        Map<String, Object> result = Maps.newHashMap();
        List<BackendServiceWithPortDto> serviceListFromDataPlane =
                serviceProxyService.getBackendServicesFromDataPlane(virtualGwId, name, registryCenterType);
        result.put(RESULT, serviceListFromDataPlane.stream().limit(Const.MAX_COUNT).map(BackendServiceWithPortDto::getName).collect(Collectors.toList()));
        return apiReturnSuccess(result);
    }

    @Audit(eventName = "CreateService", description = "创建服务")
    @RequestMapping(params = {"Action=CreateService"}, method = RequestMethod.POST)
    public String publishService(@Validated @RequestBody ServiceProxyDto serviceProxyDto) {
        logger.info("发布服务至网关，服务发布信息ServiceProxyDto:{}", serviceProxyDto);
        ErrorCode errorCode = serviceProxyService.checkCreateParam(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //设置默认值
        ServiceProxyConvert.fillTrafficPolicy(serviceProxyDto);
        long id = serviceProxyService.create(serviceProxyDto);
        return apiReturnSuccess(id);
    }

    @Audit(eventName = "UpdateService", description = "更新服务")
    @RequestMapping(params = {"Action=UpdateService"}, method = RequestMethod.POST)
    public String updatePublishService(@Validated @RequestBody ServiceProxyUpdateDto serviceProxyUpdateDto) {
        logger.info("更新服务发布信息ServiceProxyDto:{}", serviceProxyUpdateDto);
        ServiceProxyDto serviceProxyDto = serviceProxyService.fillServiceProxy(serviceProxyUpdateDto);
        ErrorCode errorCode = serviceProxyService.checkUpdateParam(serviceProxyDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        //设置默认值
        ServiceProxyConvert.fillTrafficPolicy(serviceProxyDto);
        //服务更新
        long id = serviceProxyService.update(serviceProxyDto);
        if (id == BaseConst.ERROR_RESULT) {
            return apiReturn(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("Id", serviceProxyDto.getId());
        return apiReturnSuccess(result);
    }

    @RequestMapping(params = {"Action=DeleteService"}, method = RequestMethod.GET)
    public String deleteServiceProxy(@Min(1) @RequestParam(value = "Id") long id) {
        logger.info("下线已经关联的服务，id:{}", id);
        ServiceProxyDto serviceProxyDto = serviceProxyService.get(id);
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

    @RequestMapping(params = {"Action=DescribeServicePage"}, method = RequestMethod.GET)
    public Object describeServiceProxyPage(@Validated ServiceProxyQuery query) {
        logger.info("分页查询service proxy list,查询条件为 {}", JSON.toJSONString(query));
        Page<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxyLimited(query);
        Map<String, Object> result = Maps.newHashMap();
        result.put(TOTAL_COUNT, serviceProxy.getTotal());
        result.put(RESULT, serviceProxy.getRecords());
        return apiReturnSuccess(result);
    }


    @RequestMapping(params = {"Action=DescribeService"}, method = RequestMethod.GET)
    public Object serviceProxyList(@Min(1) @RequestParam(value = "Id") long id) {
        logger.info("查询service proxy 详情,查询条件为 {}", id);
        ServiceProxyDto serviceProxyDto = serviceProxyService.get(id);
        serviceProxyService.fillServiceHealthStatus(serviceProxyDto);
        serviceProxyService.fillServicePort(serviceProxyDto);
        return apiReturnSuccess(serviceProxyDto);
    }

    /**
     * 获取项目下所有服务
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllService", "Version=2019-09-01"})
    public String getServiceList(@RequestParam(name = "VirtualGwId") long virtualGwId) {
        logger.info("开始查询接入网关的所有服务");
        List<String> services = serviceProxyService.getAllServiceTag(virtualGwId);
        return apiReturnSuccess(services);
    }
}
