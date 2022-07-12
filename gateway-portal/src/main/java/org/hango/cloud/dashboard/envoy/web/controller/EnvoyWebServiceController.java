package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyRouteWsParamInfo;
import org.hango.cloud.dashboard.envoy.meta.webservice.EnvoyServiceWsdlInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyWebServiceService;
import org.hango.cloud.dashboard.envoy.service.impl.EnvoyWebServiceServiceImpl;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRenderWsRequestDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteWsParamDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceWsdlDto;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyWebServiceController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyWebServiceController.class);

    @Autowired
    private IEnvoyWebServiceService envoyWebServiceService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRouteRuleInfoService routeRuleInfoService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    /**
     * wsdl文件上传并进行校验
     * 一个服务下只有一个wsdl文件，如果服务下已存在wsdl文件，则会进行覆盖
     *
     * @param file      wsdlFile
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return 成功码或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CheckUploadWsdlFile"}, method = RequestMethod.POST)
    public Object checkUploadWsdlFile(@RequestParam("File") MultipartFile file, @RequestParam("GwId") long gwId, @RequestParam("ServiceId") long serviceId) {
        logger.info("上传并校验wsdl文件,gwId:{}, serviceId:{}", gwId, serviceId);
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验已发布服务是否存在
        if (serviceProxyInfo == null) {
            return apiReturn(CommonErrorCode.NotPublishedService);
        }
        // 上传并校验wsdl文件
        ErrorCode errorCode = envoyWebServiceService.checkUploadWsdlFile(gwId, serviceId, file);
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
     * @param gwId      网关id
     * @param serviceId 服务id
     * @return 解析后的wsdl信息，或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeUploadWsdlFile"}, method = RequestMethod.GET)
    public Object describeUploadWsdlFile(@RequestParam("GwId") long gwId, @RequestParam("ServiceId") long serviceId) {
        logger.info("查询wsdl解析后的信息，gwId:{}, serviceId:{}", gwId, serviceId);
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验布服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验服务下的webservice信息是否存在
        EnvoyServiceWsdlInfo envoyServiceWsdlInfo = envoyWebServiceService.getServiceWsdlInfo(gwId, serviceId);
        if (envoyServiceWsdlInfo == null) {
            return apiReturn(CommonErrorCode.Success);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("WsdlInfo", new EnvoyServiceWsdlDto(envoyServiceWsdlInfo));
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 新增或更新路由上webservice转rest的配置
     *
     * @return 成功码或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateRouteWsParam"}, method = RequestMethod.POST)
    public Object updateRouteWsParam(@Validated @RequestBody EnvoyRouteWsParamDto wsParam) {
        logger.info("新增或更新路由上webservice转rest的配置, gwId:{}, serviceId:{}, routeId:{}, wsPortType:{}, wsOperation:{}, wsBinding:{}",
                wsParam.getGwId(), wsParam.getServiceId(), wsParam.getRouteId(), wsParam.getWsPortType(), wsParam.getWsOperation(), wsParam.getWsBinding());
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(wsParam.getGwId());
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(wsParam.getServiceId());
        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(wsParam.getGwId(), wsParam.getServiceId());
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验已发布服务是否存在
        if (serviceProxyInfo == null) {
            return apiReturn(CommonErrorCode.NotPublishedService);
        }
        // 校验路由是否存在
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(wsParam.getRouteId());
        RouteRuleProxyInfo routeRuleProxyInfo = routeRuleProxyService.getRouteRuleProxy(wsParam.getGwId(), wsParam.getRouteId());
        if (routeRuleInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchRouteRule);
        }
        // 校验已发布路由是否存在
        if (routeRuleProxyInfo == null) {
            return apiReturn(CommonErrorCode.NotPublishedApi);
        }
        // 校验已发布服务下是否有wsdl信息
        EnvoyServiceWsdlInfo serviceWsInfo = envoyWebServiceService.getServiceWsdlInfo(wsParam.getGwId(), wsParam.getServiceId());
        if (serviceWsInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchWsdlInfo);
        }
        // 校验webservice信息里是否包含指定binding参数
        boolean existBinding = serviceWsInfo.getWsdlBindingList().stream().anyMatch(item ->
                Objects.equals(wsParam.getWsPortType(), item.getPortType()) &&
                        Objects.equals(wsParam.getWsOperation(), item.getOperation()) &&
                        Objects.equals(wsParam.getWsBinding(), item.getBinding())
        );
        if (!existBinding) {
            return apiReturn(CommonErrorCode.IllegalBindingParam);
        }
        // 更新webservice配置
        ErrorCode errorCode = envoyWebServiceService.updateRouteProxyWsParam(wsParam.toMeta());
        return apiReturn(errorCode);
    }

    /**
     * 新增或更新路由上webservice转rest的配置
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param routeId   路由id
     * @return 成功码或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribeRouteWsParam"}, method = RequestMethod.GET)
    public Object describeRouteWsParam(@RequestParam("GwId") long gwId,
                                       @RequestParam("ServiceId") long serviceId,
                                       @RequestParam("RouteId") long routeId
    ) {
        logger.info("获取路由上webservice转rest的配置, gwId:{}, serviceId:{}, routeId:{}", gwId, serviceId, routeId);
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验路由是否存在
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeId);
        if (routeRuleInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchRouteRule);
        }
        EnvoyRouteWsParamInfo wsParamInfo = envoyWebServiceService.getRouteProxyWsParam(gwId, serviceId, routeId);

        Map<String, Object> result = new HashMap<>();
        result.put("WsParamInfo", wsParamInfo == null ? null : new EnvoyRouteWsParamDto(wsParamInfo));
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 删除路由上webservice转rest的配置
     *
     * @param gwId      网关id
     * @param serviceId 服务id
     * @param routeId   路由id
     * @return 成功码或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteRouteWsParam"}, method = RequestMethod.GET)
    public Object deleteRouteWsParam(@RequestParam("GwId") long gwId,
                                     @RequestParam("ServiceId") long serviceId,
                                     @RequestParam("RouteId") long routeId
    ) {
        logger.info("获取路由上webservice转rest的配置, gwId:{}, serviceId:{}, routeId:{}", gwId, serviceId, routeId);
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验路由是否存在
        RouteRuleInfo routeRuleInfo = routeRuleInfoService.getRouteRuleInfoById(routeId);
        if (routeRuleInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchRouteRule);
        }
        // 校验WsParam是否存在
        EnvoyRouteWsParamInfo wsParamInfo = envoyWebServiceService.getRouteProxyWsParam(gwId, serviceId, routeId);
        if (wsParamInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchWsParam);
        }
        ErrorCode errorCode = envoyWebServiceService.deleteRouteProxyWsParam(gwId, serviceId, routeId);

        return apiReturn(errorCode);
    }

    /**
     * 生成webservice请求模板
     *
     * @param gwId        网关id
     * @param serviceId   服务id
     * @param wsPortType  webservice的portType
     * @param wsOperation webservice的operation
     * @param wsBinding   webservice的binding
     * @return 请求模板或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateWsRequestTemplate"}, method = RequestMethod.GET)
    public Object createWsRequestTemplate(@RequestParam("GwId") long gwId,
                                          @RequestParam("ServiceId") long serviceId,
                                          @RequestParam("WsPortType") String wsPortType,
                                          @RequestParam("WsOperation") String wsOperation,
                                          @RequestParam("WsBinding") String wsBinding
    ) {
        logger.info("根据服务下绑定的wsdl定义生成webservice请求模板，gwId:{}, serviceId:{}", gwId, serviceId);
        // 校验网关是否存在
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }
        // 校验服务是否存在
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        // 校验服务下是否有webservice信息
        EnvoyServiceWsdlInfo envoyServiceWsdlInfo = envoyWebServiceService.getServiceWsdlInfo(gwId, serviceId);
        if (envoyServiceWsdlInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchWsdlInfo);
        }
        // 校验webservice信息里是否包含指定binding参数
        boolean existBinding = envoyServiceWsdlInfo.getWsdlBindingList().stream().anyMatch(item ->
                Objects.equals(wsPortType, item.getPortType()) &&
                        Objects.equals(wsOperation, item.getOperation()) &&
                        Objects.equals(wsBinding, item.getBinding())
        );
        if (!existBinding) {
            return apiReturn(CommonErrorCode.IllegalBindingParam);
        }
        // 创建webservice请求模板
        Map<String, Object> templateAndErrorCode = envoyWebServiceService.createWsRequestTemplate(gwId, serviceId, wsPortType, wsOperation, wsBinding);
        ErrorCode errorCode = templateAndErrorCode.get(EnvoyWebServiceServiceImpl.errorCode) != null ?
                (ErrorCode) templateAndErrorCode.get(EnvoyWebServiceServiceImpl.errorCode) : CommonErrorCode.Success;
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("WsRequestTemplate", templateAndErrorCode.get(EnvoyWebServiceServiceImpl.WS_REQUEST_TEMPLATE));
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 使用body与param测试渲染template模板
     *
     * @param renderWsRequestDto 请求模板
     * @return 请求模板或错误码
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=RenderWsRequestTemplate"}, method = RequestMethod.POST)
    public Object renderWsRequestTemplate(@Validated @RequestBody EnvoyRenderWsRequestDto renderWsRequestDto) {
        logger.info("测试渲染webservice请求模板");
        // 测试渲染webservice请求模板
        Map<String, Object> context = new HashMap<>();
        if (StringUtils.isNotEmpty(renderWsRequestDto.getBody())) {
            context.put("body", JSON.parse(renderWsRequestDto.getBody()));
        }
        if (StringUtils.isNotEmpty(renderWsRequestDto.getParam())) {
            context.put("param", JSON.parse(renderWsRequestDto.getParam()));
        }
        Map<String, Object> templateAndErrorCode = envoyWebServiceService.renderWsRequestTemplate(renderWsRequestDto.getRequestTemplate(), context);
        ErrorCode errorCode = templateAndErrorCode.get(EnvoyWebServiceServiceImpl.errorCode) != null ?
                (ErrorCode) templateAndErrorCode.get(EnvoyWebServiceServiceImpl.errorCode) : CommonErrorCode.Success;
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("WsRenderResult", templateAndErrorCode.get(EnvoyWebServiceServiceImpl.WS_TEMPLATE_RENDER_RESULT));
        return apiReturn(CommonErrorCode.Success, result);
    }
}
