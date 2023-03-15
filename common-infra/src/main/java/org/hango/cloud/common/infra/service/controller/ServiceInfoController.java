package org.hango.cloud.common.infra.service.controller;

import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Modified hanjiahao
 * 服务基本管理，包括服务创建，查询，修改
 * 服务元数据管理
 */
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class ServiceInfoController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(ServiceInfoController.class);

    @Autowired
    private IServiceInfoService serviceInfoService;


    /**
     * 添加Service
     *
     * @throws URISyntaxException
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateService"}, method = RequestMethod.POST)
    public Object addService(@Validated @RequestBody ServiceDto serviceInfoDto) {
        logger.info("创建服务，serviceInfo:{}", serviceInfoDto);
        ErrorCode errorCode = serviceInfoService.checkCreateParam(serviceInfoDto);
        //参数校验
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        long id = serviceInfoService.create(serviceInfoDto);
        return apiReturnSuccess(id);

    }

    /**
     * 根据Id查询服务
     *
     * @param serviceId
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=DescribeServiceById"}, method = RequestMethod.GET)
    public Object getService(@RequestParam(value = "ServiceId") long serviceId) {
        logger.info("查询serviceId:{}服务", serviceId);
        ServiceDto serviceDto = serviceInfoService.get(serviceId);
        if (serviceDto == null) {
            logger.info("不存在当前serviceId的服务");
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put("ServiceInfoBasic", serviceDto);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 修改Service
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateService"}, method = RequestMethod.POST)
    public Object updateService(@Validated @RequestBody ServiceDto serviceInfoDto) {
        logger.info("更新服务基本信息，serviceInfoFrontDto:{}", serviceInfoDto);
        ErrorCode errorCode = serviceInfoService.checkUpdateParam(serviceInfoDto);
        //参数校验
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        serviceInfoService.update(serviceInfoDto);
        return apiReturn(CommonErrorCode.SUCCESS);

    }

    /**
     * 查询Service列表，创建API时
     */
    @RequestMapping(params = {"Action=DescribeServiceForApi"}, method = RequestMethod.GET)
    public Object serviceListForCreateApiOrModel() {
        logger.info("创建API，请求查询service列表");
        List<ServiceDto> serviceInfoDtos = serviceInfoService.findAllServiceByProjectId(ProjectTraceHolder.getProId());
        Map<String, Object> result = Maps.newHashMap();
        result.put("ServiceInfoList", serviceInfoDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 查询服务列表，返回前端当前项目下的所有服务信息
     *
     * @throws URISyntaxException
     */
    @RequestMapping(params = {"Action=DescribeServiceList"}, method = RequestMethod.GET)
    public Object serviceList(@RequestParam(value = "Pattern", required = false) String pattern,
                              @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                              @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        logger.info("获取当前项目下的service列表，projectId：{}", ProjectTraceHolder.getProId());
        List<ServiceDto> serviceInfoDtos = serviceInfoService.findAllServiceByProjectIdLimit(pattern, offset, limit, ProjectTraceHolder.getProId());
        Map<String, Object> result = Maps.newHashMap();
        result.put("ServiceCount", serviceInfoService.getServiceCountByProjectId(pattern, ProjectTraceHolder.getProId()));
        result.put("ServiceInfoList", serviceInfoDtos);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 根据服务ID删除服务
     *
     * @param serviceId，服务id
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteService"}, method = RequestMethod.GET)
    public Object deleteService(@RequestParam(value = "ServiceId") long serviceId) {
        logger.info("删除serviceId：{}下的服务", serviceId);
        ServiceDto serviceDto = serviceInfoService.get(serviceId);
        if (serviceDto == null) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }
        ErrorCode errorCode = serviceInfoService.checkDeleteParam(serviceDto);
        if(!CommonErrorCode.SUCCESS.equals(errorCode)){
            return apiReturn(errorCode);
        }
        serviceInfoService.delete(serviceDto);
        return apiReturnSuccess(serviceDto.getDisplayName());
    }
}
