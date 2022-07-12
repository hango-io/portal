package org.hango.cloud.dashboard.envoy.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobuf;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobufProxy;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPublishedServiceProtobufDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
     * 查询pb信息
     *
     * @param pbId
     * @return
     */
    EnvoyServiceProtobuf getServiceProtobufById(long pbId);

    /**
     * 查询已发布的pb信息
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    EnvoyServiceProtobufProxy getServiceProtobufProxy(long serviceId, long gwId);

    /**
     * 查询所有pb信息
     *
     * @return
     */
    List<EnvoyServiceProtobuf> listServiceProtobuf();

    /**
     * 删除pb
     *
     * @param serviceId
     * @return
     */
    void deleteServiceProtobuf(long serviceId);


    /**
     * 上传pb时参数校验
     *
     * @param serviceId
     * @param file
     * @return
     */
    Map<String, Object> checkProtobuf(long serviceId, long gwId, MultipartFile file, boolean serviceProtobufProxyFlag);

    /**
     * 上传pb时参数校验，相比方法checkProtobuf，增加对service列表的校验
     *
     * @param serviceId
     * @param multipartFile
     * @param pbServiceList
     * @return
     */
    Map<String, Object> checkUploadOrPublishProtobuf(long serviceId, long gwId, MultipartFile multipartFile, List<String> pbServiceList, boolean serviceProtobufProxyFlag);

    /**
     * 使用protoc编译多个pb，返回此次编译的结果，用Map存放编译后的description文件等，防止重复劳动
     * RESULT: true/false
     * DESC_FILE_BASE64: description文件base64的编码
     * DESC_FILE_DESCRIPTOR_SET: description文件对应的fileDescriptorSet对象
     *
     * @param serviceId
     * @param file
     * @return
     */
    Map<String, Object> compileMultiPbFile(long serviceId, long gwId, MultipartFile file, boolean serviceProtobufProxyFlag);

    /**
     * 处理pb上传，包括更新
     *
     * @param envoyServiceProtobuf
     * @return
     */
    void saveServiceProtobuf(EnvoyServiceProtobuf envoyServiceProtobuf);

    /**
     * 更新pb发布详情
     *
     * @param serviceProtobufProxy
     */
    void saveServiceProtobufProxy(EnvoyServiceProtobufProxy serviceProtobufProxy);

    /**
     * 发布pb
     *
     * @param serviceId
     * @param gatewayInfo
     * @param envoyServiceProtobuf
     * @param pbServiceList
     * @return
     */
    ErrorCode publishServiceProtobuf(long serviceId, GatewayInfo gatewayInfo, EnvoyServiceProtobuf envoyServiceProtobuf, List<String> pbServiceList);

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
    List<EnvoyServiceProtobufProxy> listServiceProtobufProxy(long gwId);

    /**
     * 删除pb发布信息
     *
     * @param serviceId
     * @param gwId
     * @return
     */
    void deleteServiceProtobufProxy(long serviceId, long gwId);

    /**
     * 下线pb
     *
     * @param serviceInfo
     * @param gatewayInfo
     * @return
     */
    ErrorCode offlineServiceProtobuf(ServiceInfo serviceInfo, GatewayInfo gatewayInfo);
}
