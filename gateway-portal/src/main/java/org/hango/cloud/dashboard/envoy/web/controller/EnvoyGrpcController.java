package org.hango.cloud.dashboard.envoy.web.controller;

import com.alibaba.fastjson.JSON;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobuf;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobufProxy;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.dashboard.envoy.service.impl.EnvoyGrpcProtobufServiceImpl;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceProtobufDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TC_WANG
 */
@RestController
@Validated
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, Const.G_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyGrpcController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcController.class);

    @Autowired
    private IEnvoyGrpcProtobufService grpcProtobufService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;

    /**
     * 查询服务下protobuf文件详情
     *
     * @param serviceId
     * @return
     */
    @RequestMapping(params = {"Action=DescribeServicePbInfo"}, method = RequestMethod.GET)
    public Object describeServicePbInfo(@RequestParam("ServiceId") long serviceId) {
        logger.info("查询服务下protobuf文件详情, serviceId:{} ", serviceId);
        EnvoyServiceProtobuf serviceProtobuf = grpcProtobufService.getServiceProtobuf(serviceId);
        Map<String, Object> result = new HashMap<>(1);
        result.put("PbInfo", serviceProtobuf != null ? new EnvoyServiceProtobufDto(serviceProtobuf) : null);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * pb文件上传时进行校验
     *
     * @param file
     * @param serviceId
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CheckUploadPbFile"}, method = RequestMethod.POST)
    public Object CheckUploadPbFile(@RequestParam("File") MultipartFile file, @RequestParam("ServiceId") long serviceId) {
        logger.info("校验上传的Pb文件,serviceId:{}", serviceId);

        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }

        Map<String, Object> checkResultMap = grpcProtobufService.checkProtobuf(serviceId, 0, file, false);
        ErrorCode errorCode = checkResultMap.get(Const.ERROR_CODE) != null ? (ErrorCode) checkResultMap.get(Const.ERROR_CODE) : CommonErrorCode.Success;
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        EnvoyServiceProtobufDto envoyServiceProtobufDto = (EnvoyServiceProtobufDto) checkResultMap.get(EnvoyGrpcProtobufServiceImpl.SERVICE_PROTOBUF);
        EnvoyServiceProtobuf envoyServiceProtobuf = convert(envoyServiceProtobufDto);

        //上传pb同时保存文件详情供查询
        grpcProtobufService.saveServiceProtobuf(envoyServiceProtobuf);

        Map<String, Object> result = new HashMap<>(1);
        result.put("PbInfo", envoyServiceProtobufDto);
        return apiReturn(CommonErrorCode.Success, result);
    }

    private EnvoyServiceProtobuf convert(EnvoyServiceProtobufDto envoyServiceProtobufDto) {
        EnvoyServiceProtobuf envoyServiceProtobuf = new EnvoyServiceProtobuf();
        envoyServiceProtobuf.setCreateDate(envoyServiceProtobufDto.getCreateDate());
        envoyServiceProtobuf.setModifyDate(envoyServiceProtobufDto.getModifyDate());
        envoyServiceProtobuf.setServiceId(envoyServiceProtobufDto.getServiceId());
        envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(envoyServiceProtobufDto.getPbServiceList()));
        envoyServiceProtobuf.setPbFileContent(envoyServiceProtobufDto.getPbFileContent());
        envoyServiceProtobuf.setPbFileName(envoyServiceProtobufDto.getPbFileName());
        return envoyServiceProtobuf;
    }

    /**
     * 发布pb文件，先保存pb成功后，再进行发布
     *
     * @param serviceId
     * @param pbServiceList
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=SaveAndPublishedPbFile"}, method = RequestMethod.POST)
    @Audit(eventName = "PublishPbFile", description = "发布pb文件")
    public Object publishPbFile(@RequestParam("ServiceId") long serviceId, @RequestParam("GwId") long gwId, @RequestParam("PbServiceList") List<String> pbServiceList, @RequestParam("File") MultipartFile file) {
        logger.info("发布Pb文件, serviceId:{}, 服务列表为: {},网关ID :{}, 文件名: {}", serviceId, pbServiceList, gwId, file.getOriginalFilename());

        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }

        Map<String, Object> checkResultMap = grpcProtobufService.checkUploadOrPublishProtobuf(serviceId, gwId, file, pbServiceList, false);
        ErrorCode errorCode = checkResultMap.get(Const.ERROR_CODE) != null ? (ErrorCode) checkResultMap.get(Const.ERROR_CODE) : CommonErrorCode.Success;
        if (!CommonErrorCode.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        EnvoyServiceProtobufDto envoyServiceProtobufDto = (EnvoyServiceProtobufDto) checkResultMap.get(EnvoyGrpcProtobufServiceImpl.SERVICE_PROTOBUF);
        envoyServiceProtobufDto.setServiceId(serviceId);
        envoyServiceProtobufDto.setPbServiceList(pbServiceList);

        ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
        if (serviceProxyInfo == null) {
            return apiReturn(CommonErrorCode.InvalidPublishOperation);
        }
        errorCode = grpcProtobufService.publishServiceProtobuf(serviceId, gatewayInfo, envoyServiceProtobufDto.toMeta(), pbServiceList);
        return apiReturn(errorCode);
    }

    /**
     * 查询已发布的pb详情
     *
     * @param serviceId
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DescribePublishedPbFileList"}, method = RequestMethod.GET)
    public Object describePublishedPbFileList(@RequestParam("ServiceId") long serviceId) {
        logger.info("查询已发布的pb详情, serviceId:{}", serviceId);
        ServiceInfo serviceInfoDb = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfoDb == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }

        List<EnvoyPublishedServiceProtobufDto> protobufDtoList = grpcProtobufService.listPublishedServiceProtobuf(serviceId);
        Map<String, Object> result = new HashMap<>();
        result.put("PbFileList", protobufDtoList);
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * 下线pb文件
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=OfflinePbFile"}, method = RequestMethod.GET)
    @Audit(eventName = "OfflinePbFile", description = "下线pb文件")
    public Object offlinePbFile(@RequestParam("ServiceId") long serviceId, @RequestParam("GwId") long gwId) {
        logger.info("下线pb文件, gwId:{}, serviceId: {}", gwId, serviceId);
        ServiceInfo serviceInfo = serviceInfoService.getServiceByServiceId(serviceId);
        if (serviceInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchService);
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.NoSuchGateway);
        }

        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = grpcProtobufService.getServiceProtobufProxy(serviceId, gwId);
        if (envoyServiceProtobufProxy == null) {
            return apiReturn(CommonErrorCode.Success);
        }
        ErrorCode errorCode = grpcProtobufService.offlineServiceProtobuf(serviceInfo, gatewayInfo);
        return apiReturn(errorCode);
    }
}
