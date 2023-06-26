package org.hango.cloud.envoy.infra.webservice.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyRouteWsParamInfo;
import org.hango.cloud.envoy.infra.webservice.meta.EnvoyServiceWsdlInfo;
import org.hango.cloud.envoy.infra.webservice.service.IEnvoyWebServiceService;
import org.hango.cloud.envoy.infra.webservice.service.impl.EnvoyWebServiceServiceImpl;
import org.hango.cloud.envoy.infra.webservice.dto.EnvoyRenderWsRequestDto;
import org.hango.cloud.envoy.infra.webservice.dto.EnvoyRouteWsParamDto;
import org.hango.cloud.envoy.infra.webservice.dto.EnvoyServiceWsdlDto;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyWebServiceController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyWebServiceController.class);

    @Autowired
    private IEnvoyWebServiceService envoyWebServiceService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRouteService routeService;

    /**
     * wsdl文件上传并进行校验
     * 一个服务下只有一个wsdl文件，如果服务下已存在wsdl文件，则会进行覆盖
     *
     * @param file        wsdlFile
     * @param virtualGwId 网关id
     * @param serviceId   服务id
     * @return 成功码或错误码
     */
    @RequestMapping(params = {"Action=CheckUploadWsdlFile"}, method = RequestMethod.POST)
    public Object checkUploadWsdlFile(@RequestParam("File") MultipartFile file, @RequestParam("VirtualGwId") long virtualGwId, @RequestParam("ServiceId") long serviceId) {
        logger.info("上传并校验wsdl文件,virtualGwId:{}, serviceId:{}", virtualGwId, serviceId);
        // 校验网关是否存在
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_GATEWAY);
        }
        // 校验服务是否存在
        ServiceProxyDto serviceProxyInfo = serviceProxyService.get(serviceId);
        if (serviceProxyInfo == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 上传并校验wsdl文件
        ErrorCode errorCode = envoyWebServiceService.checkUploadWsdlFile(virtualGwId, serviceId, file);
        return apiReturn(errorCode);
    }

    /**
     * 根据服务id查询服务上绑定的wsdl解析后的信息，包括包含的:
     * 1. portType
     * 2. operation
     * 3. binding
     * 4. input
     * 5. output
     *
     * @param serviceId   服务id
     * @return 解析后的wsdl信息，或错误码
     */
    @RequestMapping(params = {"Action=DescribeUploadWsdlFile"}, method = RequestMethod.GET)
    public Object describeUploadWsdlFile(@RequestParam("ServiceId") long serviceId) {
        logger.info("查询wsdl解析后的信息, serviceId:{}", serviceId);
        // 校验布服务是否存在
        ServiceProxyDto serviceInfoDb = serviceProxyService.get(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 校验服务下的webservice信息是否存在
        EnvoyServiceWsdlInfo envoyServiceWsdlInfo = envoyWebServiceService.getServiceWsdlInfo(serviceId);
        if (envoyServiceWsdlInfo == null) {
            return apiReturn(CommonErrorCode.SUCCESS);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("WsdlInfo", new EnvoyServiceWsdlDto(envoyServiceWsdlInfo));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 新增或更新路由上webservice转rest的配置
     *
     * @return 成功码或错误码
     */
    @RequestMapping(params = {"Action=UpdateRouteWsParam"}, method = RequestMethod.POST)
    public Object updateRouteWsParam(@Validated @RequestBody EnvoyRouteWsParamDto wsParam) {
        logger.info("新增或更新路由上webservice转rest的配置, virtualGwId:{}, serviceId:{}, routeId:{}, wsPortType:{}, wsOperation:{}, wsBinding:{}",
                wsParam.getVirtualGwId(), wsParam.getServiceId(), wsParam.getRouteId(), wsParam.getWsPortType(), wsParam.getWsOperation(), wsParam.getWsBinding());
        // 校验网关是否存在
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(wsParam.getVirtualGwId());
        if (virtualGateway == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_GATEWAY);
        }
        // 校验服务是否存在
        ServiceProxyDto serviceInfoDb = serviceProxyService.get(wsParam.getServiceId());
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 校验路由是否存在
        RouteDto routeRuleInfo = routeService.get(wsParam.getRouteId());
        if (routeRuleInfo == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_ROUTE_RULE);
        }
        // 校验已发布服务下是否有wsdl信息
        EnvoyServiceWsdlInfo serviceWsInfo = envoyWebServiceService.getServiceWsdlInfo(wsParam.getServiceId());
        if (serviceWsInfo == null) {
            return apiReturn(EnvoyErrorCode.NO_SUCH_WSDL_INFO);
        }
        // 校验webservice信息里是否包含指定binding参数
        boolean existBinding = serviceWsInfo.getWsdlBindingList().stream().anyMatch(item ->
                Objects.equals(wsParam.getWsPortType(), item.getPortType()) &&
                        Objects.equals(wsParam.getWsOperation(), item.getOperation()) &&
                        Objects.equals(wsParam.getWsBinding(), item.getBinding())
        );
        if (!existBinding) {
            return apiReturn(EnvoyErrorCode.ILLEGAL_BINDING_PARAM);
        }
        // 更新webservice配置
        ErrorCode errorCode = envoyWebServiceService.updateRouteProxyWsParam(wsParam.toMeta());
        return apiReturn(errorCode);
    }

    /**
     * 新增或更新路由上webservice转rest的配置
     *
     * @param virtualGwId 网关id
     * @param serviceId   服务id
     * @param routeId     路由id
     * @return 成功码或错误码
     */
    @RequestMapping(params = {"Action=DescribeRouteWsParam"}, method = RequestMethod.GET)
    public Object describeRouteWsParam(@RequestParam("VirtualGwId") long virtualGwId,
                                       @RequestParam("ServiceId") long serviceId,
                                       @RequestParam("RouteId") long routeId
    ) {
        logger.info("获取路由上webservice转rest的配置, virtualGwId:{}, serviceId:{}, routeId:{}", virtualGwId, serviceId, routeId);
        // 校验网关是否存在
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_GATEWAY);
        }
        // 校验服务是否存在
        ServiceProxyDto serviceInfoDb = serviceProxyService.get(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 校验路由是否存在
        RouteDto routeRuleDto = routeService.get(routeId);
        if (routeRuleDto == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_ROUTE_RULE);
        }
        EnvoyRouteWsParamInfo wsParamInfo = envoyWebServiceService.getRouteProxyWsParam(virtualGwId, serviceId, routeId);

        Map<String, Object> result = Maps.newHashMap();
        result.put("WsParamInfo", wsParamInfo == null ? null : new EnvoyRouteWsParamDto(wsParamInfo));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 删除路由上webservice转rest的配置
     *
     * @param virtualGwId 网关id
     * @param serviceId   服务id
     * @param routeId     路由id
     * @return 成功码或错误码
     */
    @RequestMapping(params = {"Action=DeleteRouteWsParam"}, method = RequestMethod.GET)
    public Object deleteRouteWsParam(@RequestParam("VirtualGwId") long virtualGwId,
                                     @RequestParam("ServiceId") long serviceId,
                                     @RequestParam("RouteId") long routeId
    ) {
        logger.info("获取路由上webservice转rest的配置, virtualGwId:{}, serviceId:{}, routeId:{}", virtualGwId, serviceId, routeId);
        // 校验网关是否存在
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_GATEWAY);
        }
        // 校验服务是否存在
        ServiceProxyDto serviceInfoDb = serviceProxyService.get(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 校验路由是否存在
        RouteDto routeRuleDto = routeService.get(routeId);
        if (routeRuleDto == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_ROUTE_RULE);
        }
        // 校验WsParam是否存在
        EnvoyRouteWsParamInfo wsParamInfo = envoyWebServiceService.getRouteProxyWsParam(virtualGwId, serviceId, routeId);
        if (wsParamInfo == null) {
            return apiReturn(EnvoyErrorCode.NO_SUCH_WS_PARAM);
        }
        ErrorCode errorCode = envoyWebServiceService.deleteRouteProxyWsParam(virtualGwId, serviceId, routeId);

        return apiReturn(errorCode);
    }

    /**
     * 生成webservice请求模板
     *
     * @param virtualGwId 网关id
     * @param serviceId   服务id
     * @param wsPortType  webservice的portType
     * @param wsOperation webservice的operation
     * @param wsBinding   webservice的binding
     * @return 请求模板或错误码
     */
    @RequestMapping(params = {"Action=CreateWsRequestTemplate"}, method = RequestMethod.GET)
    public Object createWsRequestTemplate(@RequestParam("VirtualGwId") long virtualGwId,
                                          @RequestParam("ServiceId") long serviceId,
                                          @RequestParam("WsPortType") String wsPortType,
                                          @RequestParam("WsOperation") String wsOperation,
                                          @RequestParam("WsBinding") String wsBinding
    ) {
        logger.info("根据服务下绑定的wsdl定义生成webservice请求模板，virtualGwId:{}, serviceId:{}", virtualGwId, serviceId);
        // 校验网关是否存在
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_GATEWAY);
        }
        // 校验服务是否存在
        ServiceProxyDto serviceInfoDb = serviceProxyService.get(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NO_SUCH_SERVICE);
        }
        // 校验服务下是否有webservice信息
        EnvoyServiceWsdlInfo envoyServiceWsdlInfo = envoyWebServiceService.getServiceWsdlInfo(serviceId);
        if (envoyServiceWsdlInfo == null) {
            return apiReturn(EnvoyErrorCode.NO_SUCH_WSDL_INFO);
        }
        // 校验webservice信息里是否包含指定binding参数
        boolean existBinding = envoyServiceWsdlInfo.getWsdlBindingList().stream().anyMatch(item ->
                Objects.equals(wsPortType, item.getPortType()) &&
                        Objects.equals(wsOperation, item.getOperation()) &&
                        Objects.equals(wsBinding, item.getBinding())
        );
        if (!existBinding) {
            return apiReturn(EnvoyErrorCode.ILLEGAL_BINDING_PARAM);
        }
        // 创建webservice请求模板
        Map<String, Object> templateAndErrorCode = envoyWebServiceService.createWsRequestTemplate(virtualGwId, serviceId, wsPortType, wsOperation, wsBinding);
        ErrorCode errorCode = templateAndErrorCode.get(EnvoyWebServiceServiceImpl.ERROR_CODE) != null ?
                (ErrorCode) templateAndErrorCode.get(EnvoyWebServiceServiceImpl.ERROR_CODE) : CommonErrorCode.SUCCESS;
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("WsRequestTemplate", templateAndErrorCode.get(EnvoyWebServiceServiceImpl.WS_REQUEST_TEMPLATE));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    /**
     * 使用body与param测试渲染template模板
     *
     * @param renderWsRequestDto 请求模板
     * @return 请求模板或错误码
     */
    @RequestMapping(params = {"Action=RenderWsRequestTemplate"}, method = RequestMethod.POST)
    public Object renderWsRequestTemplate(@Validated @RequestBody EnvoyRenderWsRequestDto renderWsRequestDto) {
        logger.info("测试渲染webservice请求模板");
        // 测试渲染webservice请求模板
        Map<String, Object> context = Maps.newHashMap();
        if (StringUtils.isNotEmpty(renderWsRequestDto.getBody())) {
            context.put("body", JSON.parse(renderWsRequestDto.getBody()));
        }
        if (StringUtils.isNotEmpty(renderWsRequestDto.getParam())) {
            context.put("param", JSON.parse(renderWsRequestDto.getParam()));
        }
        Map<String, Object> templateAndErrorCode = envoyWebServiceService.renderWsRequestTemplate(renderWsRequestDto.getRequestTemplate(), context);
        ErrorCode errorCode = templateAndErrorCode.get(EnvoyWebServiceServiceImpl.ERROR_CODE) != null ?
                (ErrorCode) templateAndErrorCode.get(EnvoyWebServiceServiceImpl.ERROR_CODE) : CommonErrorCode.SUCCESS;
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("WsRenderResult", templateAndErrorCode.get(EnvoyWebServiceServiceImpl.WS_TEMPLATE_RENDER_RESULT));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }
}
