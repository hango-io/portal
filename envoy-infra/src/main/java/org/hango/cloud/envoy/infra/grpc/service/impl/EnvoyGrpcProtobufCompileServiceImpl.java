package org.hango.cloud.envoy.infra.grpc.service.impl;

import com.google.protobuf.DescriptorProtos;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.base.util.GrpcUtil;
import org.hango.cloud.envoy.infra.grpc.dto.PbCompileResultDto;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufCompileService;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Xin Li
 * @date 2022/12/7 14:25
 */
@Service
public class EnvoyGrpcProtobufCompileServiceImpl implements IEnvoyGrpcProtobufCompileService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcProtobufCompileServiceImpl.class);

    public static final String ERROR_CODE = "errorCode";
    /**
     * 存放pb编译的结果
     */
    public static final String RESULT = "result";
    public static final String PB_FILE_NAME_FOR_MAP = "pbFileName";
    public static final String PB_FILE_CONTENT_FOR_MAP = "pbFileContent";
    public static final String DESC_FILE_BASE64 = "descFileBase64";
    public static final String DESC_FILE_DESCRIPTOR_SET = "descFileDescriptorSet";
    /**
     * protoc编译时如果增加--include_imports --include_source_info，则编译出来的descriptorSet会包含依赖的proto，
     * 此时去读取package时可能会报错。所以仅处理descriptorSet中的目标proto
     */
    public static final String DEST_PB_FILE_NAME = "DestPbFileName";

    @Autowired
    private IEnvoyGrpcProtobufService envoyGrpcProtobufService;


    public Map<String, Object> checkPbFile(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        map.put(ERROR_CODE, CommonErrorCode.SUCCESS);

        if (file == null || file.isEmpty()) {
            map.put(ERROR_CODE, CommonErrorCode.FILE_IS_EMPTY);
            return map;
        }
        String fileName = file.getOriginalFilename();
        String fileType = null;
        if (fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf("."));
        }
        //文件格式校验
        if (!".proto".equals(fileType)) {
            map.put(ERROR_CODE, CommonErrorCode.ILLEGAL_FILE_FORMAT);
            return map;
        }
        String fileContent;
        try {
            byte[] bytes = file.getBytes();
            fileContent = new String(bytes, Const.DEFAULT_ENCODING);
            if (StringUtils.isBlank(fileContent)) {
                map.put(ERROR_CODE, EnvoyErrorCode.INVALID_PROTOBUF_CONTENT);
                return map;
            } else {
                map.put(PB_FILE_NAME_FOR_MAP, fileName);
                map.put(PB_FILE_CONTENT_FOR_MAP, fileContent);
            }
        } catch (IOException e) {
            logger.warn("读取pb文件内容时发生异常", e);
            map.put(ERROR_CODE, CommonErrorCode.INTERNAL_SERVER_ERROR);
            return map;
        }
        return map;
    }

    @Override
    public Map<String, Object> compilePbFile(List<String> sourcePathList, List<File> pbFileList) {
        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, true);

        String targetPath = GrpcUtil.PROTO_PATH + UUID.randomUUID() + ".description";
        File descFile = new File(targetPath);
        try {
            boolean flag = GrpcUtil.compilePBToDescription(targetPath, sourcePathList);
            if (flag) {
                //编译成功
                DescriptorProtos.FileDescriptorSet fileDescriptorSet = GrpcUtil.readDescriptionToDescriptor(targetPath);
                String base64 = GrpcUtil.fileToBase64(descFile);
                map.put(DESC_FILE_BASE64, base64);
                map.put(DESC_FILE_DESCRIPTOR_SET, fileDescriptorSet);
            } else {
                map.put(RESULT, false);
            }
        } catch (Exception e) {
            logger.warn("尝试编译pb时发生异常", e);
            map.put(RESULT, false);
        } finally {
            pbFileList.forEach(file -> {
                if (file != null && file.exists()) {
                    file.delete();
                }
            });
            if (descFile.exists()) {
                descFile.delete();
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> compileMultiPbFile(long serviceId, long gwId, MultipartFile multipartFile, boolean serviceProtobufProxyFlag) {
        //获取所有服务的pb文件，统一编译
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();

        final Map<String, Object> map = new HashMap<>();
        map.put(RESULT, true);
        if (serviceProtobufProxyFlag) {
            //发布接口调用
            List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = envoyGrpcProtobufService.listServiceProtobufProxy(gwId);
            envoyServiceProtobufProxyList.stream().filter(serviceProtobufProxy -> serviceProtobufProxy.getServiceId() != serviceId).forEach(serviceProtobufProxy -> {
                pbFileContentToFile(serviceId, serviceProtobufProxy.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
            });
        } else {
            //上传接口调用
            List<EnvoyServiceProtobuf> envoyServiceProtobufList = envoyGrpcProtobufService.listServiceProtobuf();
            envoyServiceProtobufList.stream().filter(serviceProtobuf -> serviceProtobuf.getServiceId() != serviceId).forEach(serviceProtobuf -> {
                pbFileContentToFile(serviceId, serviceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
            });
        }

        if (!(boolean) map.get(RESULT)) {
            return map;
        }

        //当前上传的pb文件
        String currentSourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        File pbFile = new File(currentSourcePath);
        try {
            //MultipartFile转换为File
            multipartFile.transferTo(pbFile);
            sourcePathList.add(currentSourcePath);
            pbFileList.add(pbFile);
        } catch (IOException e) {
            logger.warn("将MultipartFile转换为File失败 ", e);
            map.put(RESULT, false);
            return map;
        }

        //复制当前上传的pb文件
        String copyFileSourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        File copyPbFile = new File(copyFileSourcePath);
        try {
            Files.copy(pbFile.toPath(), copyPbFile.toPath());
        } catch (IOException e) {
            logger.warn("复制pb失败", e);
            map.put(RESULT, false);
            return map;
        }

        //多个pb一起编译
        Map<String, Object> resultMap = compilePbFile(sourcePathList, pbFileList);
        if (!(boolean) resultMap.get(RESULT)) {
            return resultMap;
        } else {
            //编译单个pb
            List<String> singleSourcePathList = new ArrayList<>();
            List<File> singlePbFileList = new ArrayList<>();
            singleSourcePathList.add(copyFileSourcePath);
            resultMap.put(DEST_PB_FILE_NAME, copyFileSourcePath.replaceFirst(GrpcUtil.PROTO_PATH, ""));
            singlePbFileList.add(copyPbFile);
        }
        return resultMap;
    }

    public boolean pbFileContentToFile(long serviceId, byte[] fileContent, List<String> sourcePathList, List<File> pbFileList, Map<String, Object> resultMap) {
        String sourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        File file = new File(sourcePath);
        try (FileOutputStream fop = new FileOutputStream(file)) {
            if (!file.exists()) {
                file.createNewFile();
            }
            fop.write(fileContent);
            fop.flush();
            resultMap.put(RESULT, true);
            sourcePathList.add(sourcePath);
            pbFileList.add(file);
            return true;
        } catch (IOException e) {
            resultMap.put(RESULT, false);
            logger.info("从数据库生成pb文件生成失败 ", e);
            return false;
        } catch (Exception e) {
            resultMap.put(RESULT, false);
            logger.info("文件流关闭失败 ", e);
            return false;
        }
    }

    @Override
    public List<String> getPackageServiceList(DescriptorProtos.FileDescriptorSet descriptorSet, String destProtoName) {
        return GrpcUtil.getPackageServiceList(descriptorSet, destProtoName);
    }

    @Override
    public Pair<ErrorCode, PbCompileResultDto> checkUploadCompileResult(long serviceId, MultipartFile file) {
        Map<String, Object> compileResultMap = checkPbFile(file);
        if (!CommonErrorCode.SUCCESS.equals(compileResultMap.get(ERROR_CODE))) {
            return new Pair<>(((ErrorCode) compileResultMap.get(ERROR_CODE)), null);
        }
        List<EnvoyServiceProtobuf> envoyServiceProtobufList = envoyGrpcProtobufService.listServiceProtobuf();
        List<String> pbFileContentList = envoyServiceProtobufList.stream()
                .filter(envoyServiceProtobuf -> envoyServiceProtobuf.getServiceId() != serviceId)
                .map(EnvoyServiceProtobuf::getPbFileContent).collect(Collectors.toList());
        return compileMultiPb(serviceId, file, (String) compileResultMap.get(PB_FILE_NAME_FOR_MAP),
                (String) compileResultMap.get(PB_FILE_CONTENT_FOR_MAP), pbFileContentList);
    }

    @Override
    public Pair<ErrorCode, PbCompileResultDto> checkPublishCompileResult(long serviceId, long virtualGatewayId, MultipartFile file, List<String> pbServiceList) {
        Map<String, Object> compileResultMap = checkPbFile(file);
        if (!CommonErrorCode.SUCCESS.equals(compileResultMap.get(ERROR_CODE))) {
            return new Pair<>(((ErrorCode) compileResultMap.get(ERROR_CODE)), null);
        }
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = envoyGrpcProtobufService.listServiceProtobufProxy(virtualGatewayId);
        List<String> pbFileContentList = envoyServiceProtobufProxyList.stream()
                .filter(envoyServiceProtobufProxy -> envoyServiceProtobufProxy.getServiceId() != serviceId)
                .map(EnvoyServiceProtobufProxy::getPbFileContent).collect(Collectors.toList());
        Pair<ErrorCode, PbCompileResultDto> compileMultiPbResult = compileMultiPb(serviceId, file, (String) compileResultMap.get(PB_FILE_NAME_FOR_MAP),
                (String) compileResultMap.get(PB_FILE_CONTENT_FOR_MAP), pbFileContentList);
        if (CommonErrorCode.SUCCESS.equals(compileMultiPbResult.getKey())) {
            PbCompileResultDto compileResultDto = compileMultiPbResult.getValue();
            if (CollectionUtils.isEmpty(compileResultDto.getPbServiceList()) || !new HashSet<>(compileResultDto.getPbServiceList()).containsAll(pbServiceList)) {
                //若pb文件中的serviceList不完全覆盖待发布服务，返回错误信息
                return new Pair<>(EnvoyErrorCode.INVALID_PB_SERVICE_LIST, null);
            }
        }
        return compileMultiPbResult;
    }

    @NotNull
    private Pair<ErrorCode, PbCompileResultDto> compileMultiPb(long serviceId, MultipartFile file, String pbFileName, String pbFileContent, List<String> pbFileContentList) {
        //获取所有服务的pb文件，统一编译
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();

        final Map<String, Object> map = new HashMap<>();
        map.put(RESULT, true);
        for (String currentPbFileContent : pbFileContentList) {
            if (!pbFileContentToFile(serviceId, currentPbFileContent.getBytes(), sourcePathList, pbFileList, map)) {
                logger.warn("compileMultiPb 已上传/已发布pb内容写文件失败");
                return new Pair<>(EnvoyErrorCode.PARSE_PROTOBUF_FAILED, null);
            }
        }
        //当前上传的pb文件
        String currentSourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        File pbFile = new File(currentSourcePath);
        try {
            //MultipartFile转换为File
            file.transferTo(pbFile);
            sourcePathList.add(currentSourcePath);
            pbFileList.add(pbFile);
        } catch (IOException e) {
            logger.warn("将MultipartFile转换为File失败 ", e);
            map.put(RESULT, false);
            return new Pair<>(EnvoyErrorCode.PARSE_PROTOBUF_FAILED, null);
        }

        //复制当前上传的pb文件
        String copyFileSourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        File copyPbFile = new File(copyFileSourcePath);
        try {
            Files.copy(pbFile.toPath(), copyPbFile.toPath());
        } catch (IOException e) {
            logger.warn("复制pb失败", e);
            map.put(RESULT, false);
            return new Pair<>(EnvoyErrorCode.PARSE_PROTOBUF_FAILED, null);
        }

        //多个pb一起编译，校验当前上传pb与已有pb是否存在冲突
        Map<String, Object> resultMap = compilePbFile(sourcePathList, pbFileList);
        if (!(boolean) resultMap.get(RESULT)) {
            logger.warn("compileMultiPb 当前上传pb与已有pb是否存在冲突");
            return new Pair<>(EnvoyErrorCode.PARSE_PROTOBUF_FAILED, null);
        }
        //编译当前pb获取descFileDescriptorSet
        DescriptorProtos.FileDescriptorSet descFileDescriptorSet = (DescriptorProtos.FileDescriptorSet) compilePbFile(Collections.singletonList(copyFileSourcePath), Collections.singletonList(copyPbFile)).get(DESC_FILE_DESCRIPTOR_SET);
        List<String> packageServiceList = getPackageServiceList(descFileDescriptorSet, copyFileSourcePath.replaceFirst(GrpcUtil.PROTO_PATH, ""));
        return new Pair<>(CommonErrorCode.SUCCESS, new PbCompileResultDto(pbFileName, pbFileContent, packageServiceList));
    }

    public void pbFileContentToFile(byte[] fileContent, List<String> sourcePathList, List<File> pbFileList, Map<String, Object> resultMap) {
        long invalidServiceId = -1;
        //FIXME 文件名设置serviceId意义不大，UUID即可保证不重复。非下线整个pb（service）场景不传serviceId
        pbFileContentToFile(invalidServiceId, fileContent, sourcePathList, pbFileList, resultMap);
    }

}
