package org.hango.cloud.envoy.infra.grpc.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.annotation.MethodReentrantLock;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.common.infra.operationaudit.annotation.Audit;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbCompileResultDto;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping(value = {Const.ENVOY_GATEWAY_PREFIX, BaseConst.HANGO_DASHBOARD_PREFIX}, params = {"Version=2019-09-01"})
public class EnvoyGrpcController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcController.class);

    @Autowired
    private IEnvoyGrpcProtobufService grpcProtobufService;
    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
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
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 上传pb文件
     *
     * @param file
     * @param serviceId
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=CheckUploadPbFile"}, method = RequestMethod.POST)
    public Object uploadPbFile(@RequestParam("File") MultipartFile file, @RequestParam("ServiceId") long serviceId) {
        logger.info("校验上传的Pb文件,serviceId:{}", serviceId);

        Pair<ErrorCode, PbCompileResultDto> checkResult = grpcProtobufService.checkUploadPbFile(serviceId, file);
        if (!CommonErrorCode.SUCCESS.equals(checkResult.getKey())) {
            return apiReturn(checkResult.getKey());
        }
        EnvoyServiceProtobuf envoyServiceProtobuf = generateEnvoyServiceProtobufByPbCompileResultDto(checkResult.getValue(), serviceId);
        //上传pb同时保存文件详情供查询
        long id = grpcProtobufService.saveServiceProtobuf(envoyServiceProtobuf);
        envoyServiceProtobuf.setId(id);
        Map<String, Object> result = new HashMap<>(1);
        result.put("PbInfo", grpcProtobufService.toView(envoyServiceProtobuf));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    private EnvoyServiceProtobuf generateEnvoyServiceProtobufByPbCompileResultDto(PbCompileResultDto pbCompileResultDto, long serviceId) {
        EnvoyServiceProtobuf envoyServiceProtobuf = new EnvoyServiceProtobuf();
        long currentTimeMillis = System.currentTimeMillis();
        envoyServiceProtobuf.setCreateDate(currentTimeMillis);
        envoyServiceProtobuf.setModifyDate(currentTimeMillis);
        envoyServiceProtobuf.setServiceId(serviceId);
        envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(pbCompileResultDto.getPbServiceList()));
        envoyServiceProtobuf.setPbFileContent(pbCompileResultDto.getPbFileContent());
        envoyServiceProtobuf.setPbFileName(pbCompileResultDto.getPbFileName());
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
    public Object publishPbFile(@RequestParam("ServiceId") long serviceId, @RequestParam("VirtualGwId") long virtualGwId, @RequestParam("PbServiceList") List<String> pbServiceList, @RequestParam("File") MultipartFile file) {
        logger.info("发布Pb文件, serviceId:{}, 服务列表为: {},网关ID :{}, 文件名: {}", serviceId, pbServiceList, virtualGwId, file.getOriginalFilename());
        Pair<ErrorCode, PbCompileResultDto> checkResult = grpcProtobufService.checkPublishPbFile(serviceId, virtualGwId, file, pbServiceList);
        if (!CommonErrorCode.SUCCESS.equals(checkResult.getKey())) {
            return apiReturn(checkResult.getKey());
        }
        EnvoyServiceProtobuf envoyServiceProtobuf = generateEnvoyServiceProtobufByPbCompileResultDto(checkResult.getValue(), serviceId);
        return apiReturn(grpcProtobufService.publishServiceProtobuf(serviceId, virtualGwId, envoyServiceProtobuf, pbServiceList));
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
        List<EnvoyPublishedServiceProtobufDto> protobufDtoList = grpcProtobufService.listPublishedServiceProtobuf(serviceId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("PbFileList", protobufDtoList);
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }


    /**
     * 下线pb文件
     *
     * @param serviceId
     * @param virtualGwId
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=OfflinePbFile"}, method = RequestMethod.GET)
    @Audit(eventName = "OfflinePbFile", description = "下线pb文件")
    public Object offlinePbFile(@RequestParam("ServiceId") long serviceId, @RequestParam("VirtualGwId") long virtualGwId) {
        logger.info("下线pb文件, virtualGwId:{}, serviceId: {}", virtualGwId, serviceId);
        ErrorCode errorCode = grpcProtobufService.checkOfflinePbFile(serviceId, virtualGwId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        errorCode = grpcProtobufService.offlineServiceProtobuf(serviceId, virtualGwId);
        return apiReturn(errorCode);
    }

    @GetMapping(params = {"Action=DescribePbServiceList"})
    public Object describePbServiceList(@RequestParam("PbId") Long pbId) {
        Map<String, Object> result = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        result.put("PbServiceList", grpcProtobufService.describePbServiceList(pbId));
        return apiReturn(CommonErrorCode.SUCCESS, result);
    }

    @GetMapping(params = {"Action=PublicPbService"})
    public Object publicPbService(@RequestParam("PbServiceId") Long pbServiceId, @RequestParam("VirtualGwId") Long virtualGwId) {
        ErrorCode errorCode = grpcProtobufService.checkPublicPbService(pbServiceId, virtualGwId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        errorCode = grpcProtobufService.publicPbService(pbServiceId, virtualGwId);
        return apiReturn(errorCode);
    }

    @GetMapping(params = {"Action=OfflinePbService"})
    public Object offlinePbService(@RequestParam("PbServiceId") Long pbServiceId, @RequestParam("VirtualGwId") Long virtualGwId) {
        ErrorCode errorCode = grpcProtobufService.checkOfflinePbService(pbServiceId, virtualGwId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(errorCode);
        }
        errorCode = grpcProtobufService.offlinePbService(pbServiceId, virtualGwId);
        return apiReturn(errorCode);
    }
}
