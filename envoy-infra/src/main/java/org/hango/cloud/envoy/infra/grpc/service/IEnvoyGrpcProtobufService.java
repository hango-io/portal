package org.hango.cloud.envoy.infra.grpc.service;


import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbCompileResultDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbServiceDto;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: TC_WANG
 */
public interface IEnvoyGrpcProtobufService {

    /**
     * 查询pb信息
     *
     * @param serviceId
     * @return
     */
    EnvoyServiceProtobuf getServiceProtobuf(long serviceId);

    /**
     * 查询已发布的pb信息
     *
     * @param serviceId
     * @return
     */
    EnvoyServiceProtobufProxy getServiceProtobufProxy(long serviceId);

    /**
     * 查询所有pb信息
     *
     * @return
     */
    List<EnvoyServiceProtobuf> listServiceProtobuf();

    /**
     * 处理pb上传，包括更新
     *
     * @param envoyServiceProtobuf
     * @return
     */
    long saveServiceProtobuf(EnvoyServiceProtobuf envoyServiceProtobuf);

    /**
     * 更新pb发布详情
     *
     * @param serviceProtobufProxy
     */
    long saveServiceProtobufProxy(EnvoyServiceProtobufProxy serviceProtobufProxy);

    /**
     * 删除pb
     *
     * @param serviceId
     * @return
     */
    void deleteServiceProtobuf(long serviceId);

    /**
     * 发布pb
     *
     * @param serviceId
     * @param virtualGatewayId
     * @param envoyServiceProtobuf
     * @param pbServiceList
     * @return
     */
    ErrorCode publishServiceProtobuf(long serviceId, long virtualGatewayId, EnvoyServiceProtobuf envoyServiceProtobuf, List<String> pbServiceList);

    /**
     * 查询pb发布详情
     *
     * @param serviceId
     * @return
     */
    List<EnvoyPublishedServiceProtobufDto> listPublishedServiceProtobuf(long serviceId);

    /**
     * 查询已发布的pb信息
     *
     * @return
     */
    List<EnvoyServiceProtobufProxy> listServiceProtobufProxy(long virtualGwId);

    /**
     * 下线pb
     *
     * @param serviceId
     * @return
     */
    ErrorCode offlineServiceProtobuf(long serviceId);

    /**
     * 根据pb文件id查询grpc服务信息
     *
     * @param pbId hango_service_protobuf.id
     * @return
     */
    List<PbServiceDto> describePbServiceList(long pbId);

    /**
     * 发布pb文件下的grpc服务参数校验
     *
     * @param pbServiceId grpc服务id
     * @param virtualGwId 网关id
     * @return
     */
    ErrorCode checkPublicPbService(long pbServiceId, long virtualGwId);

    /**
     * 发布pb文件下的grpc服务
     *
     * @param pbServiceId grpc服务id
     * @param virtualGwId 虚拟网关id
     */
    ErrorCode publicPbService(long pbServiceId, long virtualGwId);

    /**
     * 下线pb文件下的grpc服务参数校验
     *
     * @param pbServiceId grpc服务id
     * @param virtualGwId 网关id
     * @return
     */
    ErrorCode checkOfflinePbService(Long pbServiceId, Long virtualGwId);

    /**
     * 下线pb文件下的grpc服务
     *
     * @param pbServiceId grpc服务id
     * @param virtualGwId 网关id
     */
    ErrorCode offlinePbService(long pbServiceId, long virtualGwId);

    /**
     * 校验pb文件上传，与常规check不同的是需要执行protoc编译check同时返回校验结果，故返回ErrorCode和结果
     *
     * @param serviceId
     * @param file
     * @return
     */
    Pair<ErrorCode, PbCompileResultDto> checkUploadPbFile(long serviceId, MultipartFile file);

    /**
     * 校验pb文件发布，同上
     *
     * @param serviceId
     * @param file
     * @param pbServiceList
     * @return
     */
    Pair<ErrorCode, PbCompileResultDto> checkPublishPbFile(long serviceId, MultipartFile file, List<String> pbServiceList);

    EnvoyServiceProtobuf toMeta(EnvoyServiceProtobufDto envoyServiceProtobufDto);

    EnvoyServiceProtobufDto toView(EnvoyServiceProtobuf envoyServiceProtobuf);

    ErrorCode checkOfflinePbFile(long serviceId);
}
