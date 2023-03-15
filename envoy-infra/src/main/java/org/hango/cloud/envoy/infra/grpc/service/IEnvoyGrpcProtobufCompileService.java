package org.hango.cloud.envoy.infra.grpc.service;

import com.google.protobuf.DescriptorProtos;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbCompileResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 拆分出使用GrpcUtil解析pb相关操作
 *
 * @author Xin Li
 * @date 2022/12/7 14:24
 */
public interface IEnvoyGrpcProtobufCompileService {


    Map<String, Object> checkPbFile(MultipartFile file);
    /**
     * 编译多个pb文件到sourcePathList
     *
     * @param sourcePathList
     * @param pbFileList
     * @return
     */
    Map<String, Object> compilePbFile(List<String> sourcePathList, List<File> pbFileList);

    /**
     * 使用protoc编译多个pb，返回此次编译的结果，用Map存放编译后的description文件等，防止重复劳动
     * RESULT: true/false
     * DESC_FILE_BASE64: description文件base64的编码
     * DESC_FILE_DESCRIPTOR_SET: description文件对应的fileDescriptorSet对象
     *
     * @param serviceId
     * @param multipartFile
     * @return
     */
    Map<String, Object> compileMultiPbFile(long serviceId, long gwId, MultipartFile multipartFile, boolean serviceProtobufProxyFlag);

    void pbFileContentToFile(byte[] fileContent, List<String> sourcePathList, List<File> pbFileList, Map<String, Object> resultMap);

    //TODO 重构
    boolean pbFileContentToFile(long serviceId, byte[] fileContent, List<String> sourcePathList, List<File> pbFileList, Map<String, Object> resultMap);

    List<String> getPackageServiceList(DescriptorProtos.FileDescriptorSet descriptorSet, String destProtoName);

    /**
     * 上传pb文件编译校验
     * @param serviceId
     * @param file
     * @return
     */
    Pair<ErrorCode, PbCompileResultDto> checkUploadCompileResult(long serviceId, MultipartFile file);

    /**
     * 发布pb文件编译校验
     *
     * @param serviceId
     * @param virtualGatewayId
     * @param file
     * @param pbServiceList
     * @return
     */
    Pair<ErrorCode, PbCompileResultDto> checkPublishCompileResult(long serviceId, long virtualGatewayId, MultipartFile file, List<String> pbServiceList);
}
