package org.hango.cloud.dashboard.envoy.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.DescriptorProtos;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.ServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.GrpcUtil;
import org.hango.cloud.dashboard.envoy.dao.EnvoyServiceProtobufDao;
import org.hango.cloud.dashboard.envoy.dao.EnvoyServiceProtobufProxyDao;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobuf;
import org.hango.cloud.dashboard.envoy.meta.grpc.EnvoyServiceProtobufProxy;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyServiceProtobufDto;
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
 * @author TC_WANG
 * @date 2019/7/2
 */
@Service
public class EnvoyGrpcProtobufServiceImpl implements IEnvoyGrpcProtobufService {

    public static final String errorCode = "errorCode";
    /**
     * 存放pb编译的结果
     */
    public static final String RESULT = "result";
    public static final String PB_FILE_NAME_FOR_MAP = "pbFileName";
    public static final String PB_FILE_CONTENT_FOR_MAP = "pbFileContent";
    public static final String DESC_FILE_BASE64 = "descFileBase64";
    public static final String DESC_FILE_DESCRIPTOR_SET = "descFileDescriptorSet";
    public static final String SERVICE_PROTOBUF = "serviceProtobuf";
    /**
     * protoc编译时如果增加--include_imports --include_source_info，则编译出来的descriptorSet会包含依赖的proto，
     * 此时去读取package时可能会报错。所以仅处理descriptorSet中的目标proto
     */
    public static final String DEST_PB_FILE_NAME = "DestPbFileName";
    public static final Map<String, Object> PRINT_OPTIONS = new HashMap<>();
    public static final Map<String, Object> SETTINGS = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcProtobufServiceImpl.class);

    static {
        //grpc协议转换插件 默认配置
        PRINT_OPTIONS.put("add_whitespace", true);
        PRINT_OPTIONS.put("always_print_enums_as_ints", false);
        PRINT_OPTIONS.put("always_print_primitive_fields", true);
        PRINT_OPTIONS.put("preserve_proto_field_names", false);
        SETTINGS.put("auto_mapping", true);
        SETTINGS.put("print_options", PRINT_OPTIONS);
    }

    @Autowired
    private EnvoyServiceProtobufDao serviceProtobufDao;
    @Autowired
    private EnvoyServiceProtobufProxyDao serviceProtobufProxyDao;
    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IEnvoyGatewayService envoyGatewayService;

    @Override
    public EnvoyServiceProtobuf getServiceProtobuf(long serviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", serviceId);

        List<EnvoyServiceProtobuf> serviceProtobufList = serviceProtobufDao.getRecordsByField(params);

        if (serviceProtobufList.size() == 1) {
            return serviceProtobufList.get(0);
        }
        if (serviceProtobufList.size() > 1) {
            logger.warn("该服务下存在多个pb文件，请手动删除，仅保留一个，serviceId为{}", serviceId);
        }
        return null;
    }

    @Override
    public EnvoyServiceProtobuf getServiceProtobufById(long pbId) {
        return serviceProtobufDao.get(pbId);
    }

    @Override
    public EnvoyServiceProtobufProxy getServiceProtobufProxy(long serviceId, long gwId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", serviceId);
        params.put("gwId", gwId);
        List<EnvoyServiceProtobufProxy> serviceProtobufProxyList = serviceProtobufProxyDao.getRecordsByField(params);

        if (serviceProtobufProxyList.size() == 1) {
            return serviceProtobufProxyList.get(0);
        }
        if (serviceProtobufProxyList.size() > 1) {
            logger.warn("该服务在该网关下发布了多个pb文件，请手动删除，仅保留一个，serviceId为{}, gwId为{}", serviceId, gwId);
        }
        return null;
    }

    @Override
    public List<EnvoyServiceProtobuf> listServiceProtobuf() {
        return serviceProtobufDao.findAll();
    }

    @Override
    public void deleteServiceProtobuf(long serviceId) {
        logger.info("删除服务: {} 下的pb文件", serviceId);
        serviceProtobufDao.delete(serviceId);
    }

    @Override
    public List<EnvoyServiceProtobufProxy> listServiceProtobufProxy(long gwId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwId", gwId);
        return serviceProtobufProxyDao.getRecordsByField(params);
    }


    @Override
    public Map<String, Object> checkProtobuf(long serviceId, long gwId, MultipartFile file, boolean serviceProtobufProxyFlag) {
        Map<String, Object> map = new HashMap<>();

        //1.校验file命名，是否为空等
        Map<String, Object> compileResultMap = checkPbFile(file);
        if (!((ErrorCode) compileResultMap.get(errorCode)).getCode().equals(CommonErrorCode.Success.getCode())) {
            return compileResultMap;
        }

        EnvoyServiceProtobufDto envoyServiceProtobufDto = new EnvoyServiceProtobufDto();
        envoyServiceProtobufDto.setCreateDate(System.currentTimeMillis());
        envoyServiceProtobufDto.setModifyDate(System.currentTimeMillis());
        envoyServiceProtobufDto.setServiceId(serviceId);
        envoyServiceProtobufDto.setPbFileName((String) compileResultMap.get(PB_FILE_NAME_FOR_MAP));
        envoyServiceProtobufDto.setPbFileContent((String) compileResultMap.get(PB_FILE_CONTENT_FOR_MAP));
        map.putAll(compileResultMap);

        //2.通过编译校验内容
        Map<String, Object> multiCompileResultMap = compileMultiPbFile(serviceId, gwId, file, serviceProtobufProxyFlag);
        if (!(Boolean) multiCompileResultMap.get(RESULT)) {
            //编译存在问题
            map.put(errorCode, CommonErrorCode.ParseProtobufFailed);
            return map;
        }
        map.putAll(multiCompileResultMap);

        envoyServiceProtobufDto.setPbServiceList(GrpcUtil.getPackageServiceList((DescriptorProtos.FileDescriptorSet) multiCompileResultMap.get(DESC_FILE_DESCRIPTOR_SET), (String) multiCompileResultMap.get(DEST_PB_FILE_NAME)));
        map.put(SERVICE_PROTOBUF, envoyServiceProtobufDto);
        return map;
    }

    /**
     * 包含校验pb格式 & 服务列表是否合法 两部分
     *
     * @param serviceId
     * @param multipartFile
     * @param pbServiceList
     * @return
     */
    @Override
    public Map<String, Object> checkUploadOrPublishProtobuf(long serviceId, long gwId, MultipartFile multipartFile, List<String> pbServiceList, boolean serviceProtobufProxyFlag) {
        Map<String, Object> resultMap = checkProtobuf(serviceId, gwId, multipartFile, serviceProtobufProxyFlag);

        if (resultMap.get(SERVICE_PROTOBUF) != null) {
            //当前pb文件中要包含传入的服务列表
            EnvoyServiceProtobufDto envoyServiceProtobufDto = (EnvoyServiceProtobufDto) resultMap.get(SERVICE_PROTOBUF);
            if (pbServiceList == null || pbServiceList.size() == 0 || !envoyServiceProtobufDto.getPbServiceList().containsAll(pbServiceList)) {
                resultMap.put(errorCode, CommonErrorCode.InvalidPbServiceList);
            }
        }
        return resultMap;
    }

    /**
     * 区分是同已上传的pb一同编译（上传需要）  or 同已发布的pb一同编译（发布需要）
     * <p>
     * 如果支持上传直接发布功能，则参数校验就很复杂，要校验上述两种情况，限制发布时只允许传pbId，而不允许传MultipartFile
     *
     * @param serviceId
     * @param multipartFile
     * @return
     */
    @Override
    public Map<String, Object> compileMultiPbFile(long serviceId, long gwId, MultipartFile multipartFile, boolean serviceProtobufProxyFlag) {
        //获取所有服务的pb文件，统一编译
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();

        final Map<String, Object> map = new HashMap<>();
        map.put(RESULT, true);
        if (serviceProtobufProxyFlag) {
            //发布接口调用
            List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(gwId);
            envoyServiceProtobufProxyList.stream().filter(serviceProtobufProxy -> serviceProtobufProxy.getServiceId() != serviceId).forEach(serviceProtobufProxy -> {
                PbFileContentToFile(serviceId, serviceProtobufProxy.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
            });
        } else {
            //上传接口调用
            List<EnvoyServiceProtobuf> envoyServiceProtobufList = listServiceProtobuf();
            envoyServiceProtobufList.stream().filter(serviceProtobuf -> serviceProtobuf.getServiceId() != serviceId).forEach(serviceProtobuf -> {
                PbFileContentToFile(serviceId, serviceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
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
            resultMap.put(DESC_FILE_DESCRIPTOR_SET, compilePbFile(singleSourcePathList, singlePbFileList).get(DESC_FILE_DESCRIPTOR_SET));
        }
        return resultMap;
    }

    public void PbFileContentToFile(long serviceId, byte[] fileContent, List<String> sourcePathList, List<File> pbFileList, Map<String, Object> resultMap) {
        String sourcePath = GrpcUtil.PROTO_PATH + "_" + serviceId + "_" + UUID.randomUUID() + ".proto";
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(sourcePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            fop = new FileOutputStream(file);
            fop.write(fileContent);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            logger.info("从数据库生成pb文件生成失败 ", e);
            resultMap.put(RESULT, false);
            return;
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                logger.info("文件流关闭失败 ", e);
                resultMap.put(RESULT, false);
                return;
            }
        }
        resultMap.put(RESULT, true);
        sourcePathList.add(sourcePath);
        pbFileList.add(file);
    }

    @Override
    public void saveServiceProtobuf(EnvoyServiceProtobuf envoyServiceProtobuf) {
        EnvoyServiceProtobuf envoyServiceProtobufInDB = getServiceProtobuf(envoyServiceProtobuf.getServiceId());
        envoyServiceProtobuf.setModifyDate(System.currentTimeMillis());
        if (envoyServiceProtobufInDB == null) {
            //新建
            envoyServiceProtobuf.setCreateDate(System.currentTimeMillis());
            serviceProtobufDao.add(envoyServiceProtobuf);
        } else {
            //更新
            envoyServiceProtobuf.setId(envoyServiceProtobufInDB.getId());
            envoyServiceProtobuf.setCreateDate(envoyServiceProtobufInDB.getCreateDate());
            serviceProtobufDao.update(envoyServiceProtobuf);
        }
    }

    @Override
    public void saveServiceProtobufProxy(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        EnvoyServiceProtobufProxy envoyServiceProtobufProxyInDB = getServiceProtobufProxy(serviceProtobufProxy.getServiceId(), serviceProtobufProxy.getGwId());
        serviceProtobufProxy.setModifyDate(System.currentTimeMillis());
        if (envoyServiceProtobufProxyInDB == null) {
            //新建
            serviceProtobufProxy.setCreateDate(System.currentTimeMillis());
            serviceProtobufProxyDao.add(serviceProtobufProxy);
        } else {
            //更新
            serviceProtobufProxy.setId(envoyServiceProtobufProxyInDB.getId());
            serviceProtobufProxy.setCreateDate(envoyServiceProtobufProxyInDB.getCreateDate());
            serviceProtobufProxyDao.update(serviceProtobufProxy);
        }

    }


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

    public Map<String, Object> checkPbFile(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        map.put(errorCode, CommonErrorCode.Success);

        if (file == null || file.isEmpty()) {
            map.put(errorCode, CommonErrorCode.FileIsEmpty);
            return map;
        }
        String fileName = file.getOriginalFilename();
        String fileType = null;
        if (fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf("."));
        }
        //文件格式校验
        if (!".proto".equals(fileType)) {
            map.put(errorCode, CommonErrorCode.IllegalFileFormat);
            return map;
        }
        String fileContent;
        try {
            byte[] bytes = file.getBytes();
            fileContent = new String(bytes, Const.DEFAULT_ENCODING);
            if (StringUtils.isBlank(fileContent)) {
                map.put(errorCode, CommonErrorCode.InvalidProtobufContent);
                return map;
            } else {
                map.put(PB_FILE_NAME_FOR_MAP, fileName);
                map.put(PB_FILE_CONTENT_FOR_MAP, fileContent);
            }
        } catch (IOException e) {
            logger.warn("读取pb文件内容时发生异常", e);
            map.put(errorCode, CommonErrorCode.InternalServerError);
            return map;
        }
        return map;
    }

    private ErrorCode publishServiceProtobuf(long serviceId, GatewayInfo gatewayInfo, List<String> pbServiceList, String pbFileName, String pbFileContent, String protoDescriptorBin, List<String> publishServiceList) {
        if (!envoyGatewayService.publishGrpcEnvoyFilterToAPIPlane(gatewayInfo.getListenerPort(), gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName(), protoDescriptorBin, publishServiceList)) {
            return CommonErrorCode.PublishProtobufFailed;
        }
        saveServiceProtobufProxy(serviceId, gatewayInfo, pbServiceList, pbFileName, pbFileContent);
        return CommonErrorCode.Success;
    }

    /**
     * 入库
     *
     * @param serviceId
     * @param gatewayInfo
     * @param pbServiceList
     * @param pbFileName
     * @param pbFileContent
     */
    private void saveServiceProtobufProxy(long serviceId, GatewayInfo gatewayInfo, List<String> pbServiceList, String pbFileName, String pbFileContent) {
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = new EnvoyServiceProtobufProxy();
        envoyServiceProtobufProxy.setServiceId(serviceId);
        envoyServiceProtobufProxy.setGwId(gatewayInfo.getId());
        envoyServiceProtobufProxy.setPbFileName(pbFileName);
        envoyServiceProtobufProxy.setPbFileContent(pbFileContent);
        envoyServiceProtobufProxy.setPbServiceList(JSON.toJSONString(pbServiceList));
        saveServiceProtobufProxy(envoyServiceProtobufProxy);
    }

    @Override
    public ErrorCode publishServiceProtobuf(long serviceId, GatewayInfo gatewayInfo, EnvoyServiceProtobuf envoyServiceProtobuf, List<String> pbServiceList) {
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(gatewayInfo.getId());
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();

        //处理该网关下已发布的pb，支持重复发布，所以先过滤掉自身，否则会编译失败
        final Map<String, Object> map = new HashMap<>();
        List<String> publishServiceList = new ArrayList<>(pbServiceList);
        map.put(RESULT, true);
        envoyServiceProtobufProxyList.stream().filter(serviceProtobufProxy -> serviceProtobufProxy.getServiceId() != serviceId).forEach(serviceProtobufProxy -> {
            publishServiceList.addAll(JSON.parseArray(serviceProtobufProxy.getPbServiceList(), String.class));
            PbFileContentToFile(serviceId, serviceProtobufProxy.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
        });

        //处理当前的pb
        PbFileContentToFile(serviceId, envoyServiceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);

        if (!(boolean) map.get(RESULT)) {
            return CommonErrorCode.ProcessProtobufFailed;
        }
        //多个pb一起编译
        Map<String, Object> checkResultMap = compilePbFile(sourcePathList, pbFileList);
        if (!(boolean) map.get(RESULT)) {
            return CommonErrorCode.ProcessProtobufFailed;
        }
        checkResultMap.put(PB_FILE_NAME_FOR_MAP, envoyServiceProtobuf.getPbFileName());
        checkResultMap.put(PB_FILE_CONTENT_FOR_MAP, envoyServiceProtobuf.getPbFileContent());

        return publishServiceProtobuf(serviceId, gatewayInfo, pbServiceList, (String) checkResultMap.get(PB_FILE_NAME_FOR_MAP), (String) checkResultMap.get(PB_FILE_CONTENT_FOR_MAP), (String) checkResultMap.get(DESC_FILE_BASE64), publishServiceList);
    }


    @Override
    public ErrorCode offlineServiceProtobuf(ServiceInfo serviceInfo, GatewayInfo gatewayInfo) {
        //获取当前已发布的服务
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(gatewayInfo.getId()).stream().filter(envoyServiceProtobufProxy -> envoyServiceProtobufProxy.getServiceId() != serviceInfo.getId()).collect(Collectors.toList());
        List<String> publishServiceList = new ArrayList<>();
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(envoyServiceProtobufProxyList)) {
            Map<String, Object> map = new HashMap<>();
            envoyServiceProtobufProxyList.forEach(serviceProtobuf -> {
                publishServiceList.addAll(JSON.parseArray(serviceProtobuf.getPbServiceList(), String.class));
                PbFileContentToFile(serviceInfo.getId(), serviceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
            });
            if (!(boolean) map.get(RESULT)) {
                return CommonErrorCode.ProcessProtobufFailed;
            }
        }
        Map<String, Object> resultMap = compilePbFile(sourcePathList, pbFileList);
        //更新
        boolean result = envoyGatewayService.publishGrpcEnvoyFilterToAPIPlane(gatewayInfo.getListenerPort(), gatewayInfo.getApiPlaneAddr(), gatewayInfo.getGwClusterName(), (String) resultMap.get(DESC_FILE_BASE64), publishServiceList);
        if (!result) {
            return CommonErrorCode.PublishProtobufFailed;
        }
        deleteServiceProtobufProxy(serviceInfo.getId(), gatewayInfo.getId());
        return CommonErrorCode.Success;
    }

    @Override
    public void deleteServiceProtobufProxy(long serviceId, long gwId) {
        EnvoyServiceProtobufProxy serviceProtobufProxy = getServiceProtobufProxy(serviceId, gwId);
        if (serviceProtobufProxy != null) {
            serviceProtobufProxyDao.delete(serviceProtobufProxy);
        }
    }

    @Override
    public List<EnvoyPublishedServiceProtobufDto> listPublishedServiceProtobuf(long serviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", serviceId);

        List<EnvoyServiceProtobufProxy> serviceProtobufProxyList = serviceProtobufProxyDao.getRecordsByField(params);
        List<EnvoyPublishedServiceProtobufDto> publishedServiceProtobufDtoList = new ArrayList<>();

        serviceProtobufProxyList.forEach(serviceProtobufProxy -> {
            long gwId = serviceProtobufProxy.getGwId();
            GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
            ServiceProxyInfo serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(gwId, serviceId);
            publishedServiceProtobufDtoList.add(new EnvoyPublishedServiceProtobufDto(serviceProtobufProxy, gwId, gatewayInfo != null ? gatewayInfo.getGwName() : null, serviceProxyInfo != null ? serviceProxyInfo.getBackendService().split(",") : null));
        });
        return publishedServiceProtobufDtoList;
    }

}
