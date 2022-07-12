package org.hango.cloud.dashboard.apiserver.service;

import com.google.protobuf.Descriptors;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayInfoForGrpcDto;
import org.hango.cloud.dashboard.apiserver.dto.grpcdto.ServiceProtobufDto;
import org.hango.cloud.dashboard.apiserver.dto.servicedto.PublishedDetailDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ApiGrpcParam;
import org.hango.cloud.dashboard.apiserver.meta.grpc.ServiceProtobuf;
import org.hango.cloud.gdashboard.api.dto.ApiBodysDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author: TC_WANG
 * @date: 2019/7/2
 */
public interface IGrpcProtobufService {

    /**
     * 查询pb信息
     *
     * @param serviceId
     * @param pbName
     * @return
     */
    ServiceProtobuf getServiceProtobuf(long serviceId, String pbName);

    /**
     * 根据serviceId查询其对应的所有pb
     *
     * @param serviceId
     * @return
     */
    List<ServiceProtobuf> getServiceProtobufByServiceId(long serviceId);

    /**
     * 查询该服务下是否包含已发布的服务
     * 用于判断服务是否能被删除
     *
     * @param serviceId
     * @return
     */
    boolean existsPublishedServiceProtobuf(long serviceId);

    /**
     * 查询pb信息
     *
     * @param pbId
     * @return
     */
    ServiceProtobuf getServiceProtobuf(long pbId);

    /**
     * 查询所有pb信息
     *
     * @param serviceId
     * @return
     */
    List<ServiceProtobufDto> listServiceProtobuf(long serviceId);

    /**
     * 下线pb文件：下线其中所有的API
     *
     * @param serviceProtobuf
     * @return
     */
    Object offlineServiceProtobuf(ServiceProtobuf serviceProtobuf, long gwId);

    /**
     * 删除pb文件
     *
     * @param serviceProtobuf
     * @return
     */
    ErrorCode deleteServiceProtobuf(ServiceProtobuf serviceProtobuf);

    /**
     * 删除服务下的所有pb
     *
     * @param serviceId
     * @return
     */
    void deleteServiceProtobuf(long serviceId);

    /**
     * 删除grpc类型的API一定要采用此方法
     * 因为ApiGrpcParam对象没有封装到api模块中，所以调用apiInfoService中的deleteApi并不能删除grpc参数
     * 要额外删除
     *
     * @param apiGrpcParam
     */
    void deleteGrpcApi(long apiId, ApiGrpcParam apiGrpcParam);

    /**
     * 发布pb后更新pb的发布状态
     *
     * @param serviceProtobuf
     * @param gatewayInfo
     */
    void updateServiceProtobufProxyForPublish(ServiceProtobuf serviceProtobuf, GatewayInfo gatewayInfo);

    /**
     * 下线pb后更新pb的发布状态
     *
     * @param serviceProtobuf
     * @param gatewayInfo
     */
    void updateServiceProtobufProxyForOffline(ServiceProtobuf serviceProtobuf, GatewayInfo gatewayInfo);

    /**
     * 根据pbId查询其发布的所有环境
     *
     * @param pbId
     * @return
     */
    List<Long> getServiceProtobufProxyByPbId(long pbId);

    /**
     * 查询pb当前发布的网关个数
     *
     * @param serviceProtobuf
     * @return
     */
    int getPbPublishedCount(ServiceProtobuf serviceProtobuf);

    /**
     * 获取pb发布详情：网关名称和
     *
     * @param serviceProtobuf
     * @return
     */
    List<PublishedDetailDto> getPbPublishedDetail(ServiceProtobuf serviceProtobuf);

    /**
     * 查找API对应的grpc参数
     *
     * @param pbName
     * @param packageName
     * @param serviceName
     * @param methodName
     * @return
     */
    ApiGrpcParam getApiGrpcParam(String pbName, String packageName, String serviceName, String methodName);

    /**
     * 根据pbName查询其对应的所有api
     *
     * @param pbName
     * @param serviceId
     * @return
     */
    List<ApiGrpcParam> getApiGrpcParamList(String pbName, long serviceId);

    /**
     * 根据apiId查询其对应的grpcparam
     *
     * @param apiId
     * @return
     */
    ApiGrpcParam getApiGrpcParam(long apiId);

    /**
     * 删除api对应的grpc param
     *
     * @param apiGrpcParam
     */
    void deleteApiGrpcParam(ApiGrpcParam apiGrpcParam);

    /**
     * 删除api对应的grpc param
     *
     * @param apiId
     */
    void deleteApiGrpcParam(long apiId);

    /**
     * 从pb中定义的httpRule创建API
     *
     * @param checkResultMap
     * @return
     */
    void createApiFromProtobuf(Map<String, Object> checkResultMap);


    /**
     * 在真正创建API之前，判断是否和现有API存在冲突
     *
     * @param checkResultMap
     * @return
     */
    String attemptCreateApiFromProtobuf(Map<String, Object> checkResultMap);


    /**
     * 从pb中发布API
     *
     * @param serviceProtobuf
     * @param gwId
     */
    Object publishApiFromProtobuf(ServiceProtobuf serviceProtobuf, long gwId);

    /**
     * 使用protoc编译pb，返回此次编译的结果，用Map存放编译后的description文件等，防止重复劳动
     * result: true/false
     * descFileBase64: description文件base64的编码
     * descFileDescriptorSet: description文件对应的fileDescriptorSet对象
     *
     * @param file
     * @param pbName
     * @return
     */
    Map<String, Object> compilePbFile(MultipartFile file, String pbName);

    /**
     * 处理pb上传的逻辑，包含自动创建API
     *
     * @param checkResultMap
     * @return
     */
    void uploadProtobuf(Map<String, Object> checkResultMap, ServiceProtobuf serviceProtobuf);

    /**
     * 更新pb文件
     *
     * @param serviceProtobuf
     * @return
     */
    void updateProtobuf(ServiceProtobuf serviceProtobuf);

    /**
     * 更新pb文件
     *
     * @param checkResultMap
     * @param serviceProtobuf
     */
    void updateProtobuf(Map<String, Object> checkResultMap, ServiceProtobuf serviceProtobuf);

    /**
     * 查询pb发布到的网关列表
     *
     * @param pbId
     * @return
     */
    List<GatewayInfoForGrpcDto> listGwInfoWithPbId(long pbId);

    /**
     * 上传pb时参数校验，
     * 由于pb文件需要编译且未防止重复产生临时文件，所以在参数校验过程中返回编译结果
     *
     * @param serviceProtobuf
     * @param file
     * @return
     */
    Map<String, Object> checkUploadProtobuf(ServiceProtobuf serviceProtobuf, MultipartFile file);

    /**
     * 更新pb时参数校验
     *
     * @param serviceProtobuf
     * @param file
     * @param checkPbFile
     * @return
     */
    Map<String, Object> checkUpdateProtobuf(ServiceProtobuf serviceProtobuf, MultipartFile file, boolean checkPbFile);

    /**
     * 尝试从pb中自动创建API时参数校验，
     * 由于pb文件需要编译且未防止重复产生临时文件，所以在参数校验过程中返回编译结果
     *
     * @param pbName
     * @param serviceId
     * @param file
     * @param validServiceId
     * @return
     */
    Map<String, Object> checkCreateApiFromPb(String pbName, long serviceId, MultipartFile file, boolean validServiceId);

    /**
     * 对上传的pb文件进行参数校验
     *
     * @param file
     * @return
     */
    Map<String, Object> checkPbFile(MultipartFile file);

    /**
     * 校验pbId
     *
     * @param pbId
     * @return
     */
    ServiceProtobuf checkPbId(long pbId);

    /**
     * 针对get方法，将其入参转换为queryString
     *
     * @param apiId
     * @param apiPath
     * @param requestMessage
     * @return
     */
    ApiBodysDto generateQueryStringByDescriptor(long apiId, String apiPath, Descriptors.Descriptor requestMessage);
}
