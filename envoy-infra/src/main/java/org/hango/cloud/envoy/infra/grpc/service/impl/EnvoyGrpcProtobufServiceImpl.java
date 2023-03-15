package org.hango.cloud.envoy.infra.grpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.Pair;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.grpc.dao.EnvoyServiceProtobufDao;
import org.hango.cloud.envoy.infra.grpc.dao.EnvoyServiceProtobufProxyDao;
import org.hango.cloud.envoy.infra.grpc.dao.PbServiceDao;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyPublishedServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.EnvoyServiceProtobufDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbCompileResultDto;
import org.hango.cloud.envoy.infra.grpc.dto.PbServiceDto;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobuf;
import org.hango.cloud.envoy.infra.grpc.meta.EnvoyServiceProtobufProxy;
import org.hango.cloud.envoy.infra.grpc.meta.PbService;
import org.hango.cloud.envoy.infra.grpc.remote.GrpcProtobufRemoteClient;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufCompileService;
import org.hango.cloud.envoy.infra.grpc.service.IEnvoyGrpcProtobufService;
import org.hango.cloud.gdashboard.api.util.BeanUtil;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author TC_WANG
 * @date 2019/7/2
 */
@Service
public class EnvoyGrpcProtobufServiceImpl implements IEnvoyGrpcProtobufService {
    private static final Logger logger = LoggerFactory.getLogger(EnvoyGrpcProtobufServiceImpl.class);

    @Autowired
    private EnvoyServiceProtobufDao serviceProtobufDao;
    @Autowired
    private EnvoyServiceProtobufProxyDao serviceProtobufProxyDao;
    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private PbServiceDao pbServiceDao;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IEnvoyGrpcProtobufCompileService envoyGrpcProtobufCompileService;

    @Autowired
    private IServiceInfoService serviceInfoService;

    @Autowired
    private GrpcProtobufRemoteClient grpcProtobufRemoteClient;

    @Override
    public EnvoyServiceProtobuf getServiceProtobuf(long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.SERVICE_ID, serviceId);

        List<EnvoyServiceProtobuf> serviceProtobufList = serviceProtobufDao.getRecordsByField(params);

        if (serviceProtobufList.size() == 1) {
            EnvoyServiceProtobuf envoyServiceProtobuf = serviceProtobufList.get(0);
            //pb服务列表优先从新表获取
            fillPbServiceList(envoyServiceProtobuf);
            return envoyServiceProtobuf;
        }
        if (serviceProtobufList.size() > 1) {
            logger.warn("该服务下存在多个pb文件，请手动删除，仅保留一个，serviceId为{}", serviceId);
        }
        return null;
    }

    private void fillPbServiceList(EnvoyServiceProtobuf envoyServiceProtobuf) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pbId", envoyServiceProtobuf.getId());
        List<PbService> pbServiceList = pbServiceDao.getRecordsByField(params);
        if (!CollectionUtils.isEmpty(pbServiceList)) {
            envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(pbServiceList.stream().map(PbService::getServiceName).collect(Collectors.toList())));
        }
    }

    private EnvoyServiceProtobuf getServiceProtobufById(long pbId) {
        return serviceProtobufDao.get(pbId);
    }

    @Override
    public EnvoyServiceProtobufProxy getServiceProtobufProxy(long serviceId, long virtualGwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.SERVICE_ID, serviceId);
        params.put("virtualGwId", virtualGwId);
        List<EnvoyServiceProtobufProxy> serviceProtobufProxyList = serviceProtobufProxyDao.getRecordsByField(params);

        if (serviceProtobufProxyList.size() == 1) {
            return serviceProtobufProxyList.get(0);
        }
        if (serviceProtobufProxyList.size() > 1) {
            logger.warn("该服务在该网关下发布了多个pb文件，请手动删除，仅保留一个，serviceId为{}, gwId为{}", serviceId, virtualGwId);
        }
        return null;
    }

    @Override
    public List<EnvoyServiceProtobuf> listServiceProtobuf() {
        return serviceProtobufDao.findAll();
    }

    @Override
    public List<EnvoyServiceProtobufProxy> listServiceProtobufProxy(long virtualGwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("virtualGwId", virtualGwId);
        return serviceProtobufProxyDao.getRecordsByField(params);
    }

    @Override
    @Transactional
    public long saveServiceProtobuf(EnvoyServiceProtobuf envoyServiceProtobuf) {
        EnvoyServiceProtobuf envoyServiceProtobufInDB = getServiceProtobuf(envoyServiceProtobuf.getServiceId());
        envoyServiceProtobuf.setModifyDate(System.currentTimeMillis());
        if (envoyServiceProtobufInDB == null) {
            //新建
            envoyServiceProtobuf.setCreateDate(System.currentTimeMillis());
            long pbId = serviceProtobufDao.add(envoyServiceProtobuf);
            //字段pbServiceList冗余暂不删除，额外写新表
            //@NotEmpty
            String pbServiceList = envoyServiceProtobuf.getPbServiceList();
            for (String pbServiceName : JSON.parseArray(pbServiceList, String.class)) {
                PbService pbService = new PbService();
                pbService.setServiceName(pbServiceName);
                pbService.setPbId(pbId);
                pbService.setPublishStatus(PbService.PUBLISH_STATUS_NOT_PUBLISHED);
                pbService.setPbProxyId(PbService.NOT_PUBLISHED_PB_PROXY_ID);
                pbServiceDao.add(pbService);
            }
            return pbId;
        } else {
            //更新
            envoyServiceProtobuf.setId(envoyServiceProtobufInDB.getId());
            envoyServiceProtobuf.setCreateDate(envoyServiceProtobufInDB.getCreateDate());
            serviceProtobufDao.update(envoyServiceProtobuf);
            return envoyServiceProtobufInDB.getId();
        }
    }

    @Override
    public long saveServiceProtobufProxy(EnvoyServiceProtobufProxy serviceProtobufProxy) {
        EnvoyServiceProtobufProxy envoyServiceProtobufProxyInDB = getServiceProtobufProxy(serviceProtobufProxy.getServiceId(), serviceProtobufProxy.getVirtualGwId());
        serviceProtobufProxy.setModifyDate(System.currentTimeMillis());
        long pbProxyId;
        if (envoyServiceProtobufProxyInDB == null) {
            //新建
            serviceProtobufProxy.setCreateDate(System.currentTimeMillis());
            pbProxyId = serviceProtobufProxyDao.add(serviceProtobufProxy);
        } else {
            //更新
            serviceProtobufProxy.setId(envoyServiceProtobufProxyInDB.getId());
            serviceProtobufProxy.setCreateDate(envoyServiceProtobufProxyInDB.getCreateDate());
            serviceProtobufProxyDao.update(serviceProtobufProxy);
            pbProxyId = envoyServiceProtobufProxyInDB.getId();
        }
        savePbServicePublishStatus(serviceProtobufProxy.getServiceId(), pbProxyId, JSON.parseArray(serviceProtobufProxy.getPbServiceList(), String.class));
        return pbProxyId;
    }

    @Override
    @Transactional
    public void deleteServiceProtobuf(long serviceId) {
        Map<String, Object> params = new HashMap<>();
        params.put(BaseConst.SERVICE_ID, serviceId);
        List<EnvoyServiceProtobuf> envoyServiceProtobufList = serviceProtobufDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(envoyServiceProtobufList)) {
            return;
        }
        EnvoyServiceProtobuf envoyServiceProtobuf = envoyServiceProtobufList.get(0);
        params = new HashMap<>();

        params.put("pbId", envoyServiceProtobuf.getId());
        List<PbService> pbServiceList = pbServiceDao.getRecordsByField(params);
        if (!CollectionUtils.isEmpty(pbServiceList)) {
            pbServiceList.forEach(pbService -> pbServiceDao.delete(pbService));
        }
        serviceProtobufDao.delete(envoyServiceProtobuf);
    }

    private void savePbServicePublishStatus(long serviceId, long pbProxyId, List<String> pbServiceList) {
        EnvoyServiceProtobuf envoyServiceProtobuf = getServiceProtobuf(serviceId);
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pbId", envoyServiceProtobuf.getId());
        List<PbService> pbServiceListInDB = pbServiceDao.getRecordsByField(params);
        for (PbService pbService : pbServiceListInDB) {
            //serviceName是惟一的
            pbService.setPbProxyId(pbProxyId);
            if (pbServiceList.contains(pbService.getServiceName())) {
                pbService.setPublishStatus(PbService.PUBLISH_STATUS_PUBLISHED);
            }
            pbServiceDao.update(pbService);
        }
    }

    private ErrorCode publishServiceProtobuf(long serviceId, long virtualGatewayId, List<String> pbServiceList, String pbFileName, String pbFileContent, String protoDescriptorBin, List<String> publishServiceList) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (!grpcProtobufRemoteClient.publishGrpcEnvoyFilterToAPIPlane(virtualGatewayDto.getPort(), gatewayDto.getConfAddr(), gatewayDto.getGwClusterName(), protoDescriptorBin, publishServiceList)) {
            return EnvoyErrorCode.PUBLISH_PROTOBUF_FAILED;
        }
        saveServiceProtobufProxy(serviceId, virtualGatewayId, pbServiceList, pbFileName, pbFileContent);
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 入库
     *
     * @param serviceId
     * @param virtualGatewayId
     * @param pbServiceList
     * @param pbFileName
     * @param pbFileContent
     */
    private void saveServiceProtobufProxy(long serviceId, long virtualGatewayId, List<String> pbServiceList, String pbFileName, String pbFileContent) {
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = new EnvoyServiceProtobufProxy();
        envoyServiceProtobufProxy.setServiceId(serviceId);
        envoyServiceProtobufProxy.setVirtualGwId(virtualGatewayId);
        envoyServiceProtobufProxy.setPbFileName(pbFileName);
        envoyServiceProtobufProxy.setPbFileContent(pbFileContent);
        envoyServiceProtobufProxy.setPbServiceList(JSON.toJSONString(pbServiceList));
        saveServiceProtobufProxy(envoyServiceProtobufProxy);
    }

    @Override
    public ErrorCode publishServiceProtobuf(long serviceId, long virtualGatewayId, EnvoyServiceProtobuf envoyServiceProtobuf, List<String> pbServiceList) {
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(virtualGatewayId);
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();

        //处理该网关下已发布的pb，支持重复发布，所以先过滤掉自身，否则会编译失败
        final Map<String, Object> map = Maps.newHashMap();
        List<String> publishServiceList = new ArrayList<>(pbServiceList);
        map.put(EnvoyGrpcProtobufCompileServiceImpl.RESULT, true);
        envoyServiceProtobufProxyList.stream().filter(serviceProtobufProxy -> serviceProtobufProxy.getServiceId() != serviceId).forEach(serviceProtobufProxy -> {
            publishServiceList.addAll(JSON.parseArray(serviceProtobufProxy.getPbServiceList(), String.class));
            envoyGrpcProtobufCompileService.pbFileContentToFile(serviceId, serviceProtobufProxy.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
        });

        //处理当前的pb
        envoyGrpcProtobufCompileService.pbFileContentToFile(serviceId, envoyServiceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);

        if (!(boolean) map.get(EnvoyGrpcProtobufCompileServiceImpl.RESULT)) {
            return EnvoyErrorCode.PROCESS_PROTOBUF_FAILED;
        }
        //多个pb一起编译
        Map<String, Object> checkResultMap = envoyGrpcProtobufCompileService.compilePbFile(sourcePathList, pbFileList);
        if (!(boolean) map.get(EnvoyGrpcProtobufCompileServiceImpl.RESULT)) {
            return EnvoyErrorCode.PROCESS_PROTOBUF_FAILED;
        }
        checkResultMap.put(EnvoyGrpcProtobufCompileServiceImpl.PB_FILE_NAME_FOR_MAP, envoyServiceProtobuf.getPbFileName());
        checkResultMap.put(EnvoyGrpcProtobufCompileServiceImpl.PB_FILE_CONTENT_FOR_MAP, envoyServiceProtobuf.getPbFileContent());

        return publishServiceProtobuf(serviceId, virtualGatewayId, pbServiceList, (String) checkResultMap.get(EnvoyGrpcProtobufCompileServiceImpl.PB_FILE_NAME_FOR_MAP), (String) checkResultMap.get(EnvoyGrpcProtobufCompileServiceImpl.PB_FILE_CONTENT_FOR_MAP), (String) checkResultMap.get(EnvoyGrpcProtobufCompileServiceImpl.DESC_FILE_BASE64), publishServiceList);
    }


    @Override
    public List<PbServiceDto> describePbServiceList(long pbId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pbId", pbId);
        List<PbService> pbServiceList = pbServiceDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pbServiceList) ? Collections.emptyList() : pbServiceList.stream().map(this::toView).collect(Collectors.toList());
    }

    private PbServiceDto toView(PbService pbService) {
        return BeanUtil.copy(pbService, PbServiceDto.class);
    }

    @Override
    public ErrorCode checkPublicPbService(long pbServiceId, long virtualGwId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGatewayDto) {
            logger.error("checkPublicPbService gwId[{}] not exist! ", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        PbService pbService = pbServiceDao.get(pbServiceId);
        if (null == pbService) {
            logger.error("checkPublicPbService pbServiceId[{}] not exist! ", pbServiceId);
            return EnvoyErrorCode.NO_SUCH_TRAFFIC_COLOR_RULE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    @Transactional
    public ErrorCode publicPbService(long pbServiceId, long virtualGwId) {
        return processPbServicePublishStatus(pbServiceId, virtualGwId, true);
    }

    /**
     * @param pbServiceId grpc服务id
     * @param virtualGwId 虚拟网关id
     * @param isPublish   发布(true) or 下线(false)
     * @return
     */
    private ErrorCode processPbServicePublishStatus(long pbServiceId, long virtualGwId, boolean isPublish) {
        //获取当前网关所有已发布pb
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(virtualGwId);
        PbService pbService = pbServiceDao.get(pbServiceId);
        List<String> sourcePathList = new ArrayList<>();
        final Map<String, Object> map = new HashMap<>();
        List<String> publishServiceList = new ArrayList<>();
        map.put(EnvoyGrpcProtobufCompileServiceImpl.RESULT, true);
        List<File> pbFileList = new ArrayList<>();

        //处理已发布pb
        envoyServiceProtobufProxyList.forEach(serviceProtobufProxy -> {
            List<String> publishServiceByPbProxyId = getPublishServiceByPbProxyId(serviceProtobufProxy.getId());
            publishServiceList.addAll(publishServiceByPbProxyId);
            envoyGrpcProtobufCompileService.pbFileContentToFile(serviceProtobufProxy.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
        });
        //处理当前pb
        EnvoyServiceProtobuf serviceProtobufById = getServiceProtobufById(pbService.getPbId());
        Set<Long> protobufProxyServiceIds = envoyServiceProtobufProxyList.stream().map(EnvoyServiceProtobufProxy::getServiceId).collect(Collectors.toSet());
        if (!protobufProxyServiceIds.contains(serviceProtobufById.getServiceId())) {
            //若当前pbservice是初次上线，需要额外再次编译pb（针对先下线pb再上线pbService场景）
            envoyGrpcProtobufCompileService.pbFileContentToFile(serviceProtobufById.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
        }
        if (!(boolean) map.get(EnvoyGrpcProtobufCompileServiceImpl.RESULT)) {
            return EnvoyErrorCode.PROCESS_PROTOBUF_FAILED;
        }
        //多个pb一起编译
        Map<String, Object> checkResultMap = envoyGrpcProtobufCompileService.compilePbFile(sourcePathList, pbFileList);
        if (!(boolean) map.get(EnvoyGrpcProtobufCompileServiceImpl.RESULT)) {
            return EnvoyErrorCode.PROCESS_PROTOBUF_FAILED;
        }

        String protoDescriptorBin = (String) checkResultMap.get(EnvoyGrpcProtobufCompileServiceImpl.DESC_FILE_BASE64);
        if (isPublish) {
            publishServiceList.add(pbService.getServiceName());
        } else {
            publishServiceList.remove(pbService.getServiceName());
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        if (!grpcProtobufRemoteClient.publishGrpcEnvoyFilterToAPIPlane(virtualGatewayDto.getPort(), gatewayDto.getConfAddr(), gatewayDto.getGwClusterName(), protoDescriptorBin, publishServiceList)) {
            return EnvoyErrorCode.PUBLISH_PROTOBUF_FAILED;
        }
        pbService.setPublishStatus(isPublish ? PbService.PUBLISH_STATUS_PUBLISHED : PbService.PUBLISH_STATUS_NOT_PUBLISHED);
        pbServiceDao.update(pbService);
        if (isPublish) {
            saveServiceProtobufProxy(virtualGwId, pbService);
        }
        updateServiceProtobufProxy(isPublish, pbService);
        return CommonErrorCode.SUCCESS;
    }

    /**
     * 下线pb后需要对EnvoyServiceProtobufProxy进行补偿
     *
     * @param gwId
     * @param pbService
     */
    private void saveServiceProtobufProxy(long gwId, PbService pbService) {
        EnvoyServiceProtobuf serviceProtobufById = getServiceProtobufById(pbService.getPbId());
        EnvoyServiceProtobufProxy serviceProtobufProxy = getServiceProtobufProxy(serviceProtobufById.getServiceId(), gwId);
        if (serviceProtobufProxy == null) {
            EnvoyServiceProtobufProxy envoyServiceProtobufProxy = new EnvoyServiceProtobufProxy();
            envoyServiceProtobufProxy.setServiceId(serviceProtobufById.getServiceId());
            envoyServiceProtobufProxy.setVirtualGwId(gwId);
            envoyServiceProtobufProxy.setPbFileName(serviceProtobufById.getPbFileName());
            envoyServiceProtobufProxy.setPbFileContent(serviceProtobufById.getPbFileContent());
            envoyServiceProtobufProxy.setPbServiceList("[]");
            long newPbProxyId = saveServiceProtobufProxy(envoyServiceProtobufProxy);
            pbService.setPbProxyId(newPbProxyId);
        }
    }

    private void updateServiceProtobufProxy(boolean isPublish, PbService pbService) {
        Long pbProxyId = pbService.getPbProxyId();
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = serviceProtobufProxyDao.get(pbProxyId);
        String pbServiceListStr = envoyServiceProtobufProxy.getPbServiceList();
        List<String> pbServiceList = StringUtils.isEmpty(pbServiceListStr) ? new ArrayList<>() : JSON.parseArray(pbServiceListStr, String.class);
        if (isPublish) {
            pbServiceList.add(pbService.getServiceName());
        } else {
            pbServiceList.remove(pbService.getServiceName());
        }
        envoyServiceProtobufProxy.setPbServiceList(JSON.toJSONString(pbServiceList));
        serviceProtobufProxyDao.update(envoyServiceProtobufProxy);
    }

    private List<String> getPublishServiceByPbProxyId(long pbProxyId) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("pbProxyId", pbProxyId);
        params.put("publishStatus", PbService.PUBLISH_STATUS_PUBLISHED);
        List<String> publishServiceByPbProxyId;
        List<PbService> pbServiceList = pbServiceDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(pbServiceList)) {
            publishServiceByPbProxyId = Collections.emptyList();
        } else {
            publishServiceByPbProxyId = pbServiceList.stream().map(PbService::getServiceName).collect(Collectors.toList());
        }
        return publishServiceByPbProxyId;
    }

    @Override
    public ErrorCode checkOfflinePbService(Long pbServiceId, Long virtualGwId) {
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGatewayDto) {
            logger.error("checkOfflinePbService gwId[{}] not exist! ", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        PbService pbService = pbServiceDao.get(pbServiceId);
        if (null == pbService) {
            logger.error("checkOfflinePbService pbServiceId[{}] not exist! ", pbServiceId);
            return EnvoyErrorCode.NO_SUCH_TRAFFIC_COLOR_RULE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    @Transactional
    public ErrorCode offlinePbService(long pbServiceId, long virtualGwId) {
        return processPbServicePublishStatus(pbServiceId, virtualGwId, false);
    }

    @Override
    public Pair<ErrorCode, PbCompileResultDto> checkUploadPbFile(long serviceId, MultipartFile file) {
        ServiceDto serviceInfoDb = serviceInfoService.get(serviceId);
        if (serviceInfoDb == null) {
            return new Pair<>(CommonErrorCode.NO_SUCH_SERVICE, null);
        }
        return envoyGrpcProtobufCompileService.checkUploadCompileResult(serviceId, file);
    }

    @Override
    public Pair<ErrorCode, PbCompileResultDto> checkPublishPbFile(long serviceId, long virtualGwId, MultipartFile file, List<String> pbServiceList) {
        ServiceDto serviceInfoDb = serviceInfoService.get(serviceId);
        if (serviceInfoDb == null) {
            return new Pair<>(CommonErrorCode.NO_SUCH_SERVICE, null);
        }
        ServiceProxyDto serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(virtualGwId, serviceId);
        if (serviceProxyInfo == null) {
            return new Pair<>(EnvoyErrorCode.INVALID_PUBLISH_OPERATION, null);
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return new Pair<>(CommonErrorCode.NO_SUCH_GATEWAY, null);
        }
        return envoyGrpcProtobufCompileService.checkPublishCompileResult(serviceId, virtualGwId, file, pbServiceList);
    }

    @Override
    public EnvoyServiceProtobuf toMeta(EnvoyServiceProtobufDto envoyServiceProtobufDto) {
        EnvoyServiceProtobuf envoyServiceProtobuf = BeanUtil.copy(envoyServiceProtobufDto, EnvoyServiceProtobuf.class);
        envoyServiceProtobuf.setPbServiceList(JSON.toJSONString(envoyServiceProtobufDto.getPbServiceList()));
        return envoyServiceProtobuf;
    }

    @Override
    public EnvoyServiceProtobufDto toView(EnvoyServiceProtobuf envoyServiceProtobuf) {
        EnvoyServiceProtobufDto envoyServiceProtobufDto = BeanUtil.copy(envoyServiceProtobuf, EnvoyServiceProtobufDto.class);
        envoyServiceProtobufDto.setPbServiceList(JSON.parseArray(envoyServiceProtobuf.getPbServiceList(), String.class));
        return envoyServiceProtobufDto;
    }

    @Override
    public ErrorCode checkOfflinePbFile(long serviceId, long virtualGwId) {
        ServiceDto serviceDto = serviceInfoService.get(serviceId);
        if (serviceDto == null) {
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (virtualGateway == null) {
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    @Transactional
    public ErrorCode offlineServiceProtobuf(long serviceId, long virtualGatewayId) {
        //幂等
        EnvoyServiceProtobufProxy envoyServiceProtobufProxy = getServiceProtobufProxy(serviceId, virtualGatewayId);
        if (envoyServiceProtobufProxy == null) {
            return CommonErrorCode.SUCCESS;
        }
        ErrorCode errorCode = offlineServiceProtobufToApiPlane(serviceId, virtualGatewayId);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        deleteServiceProtobufProxy(serviceId, virtualGatewayId);
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode offlineServiceProtobufToApiPlane(long serviceId, long virtualGatewayId) {
        ServiceDto serviceDto = serviceInfoService.get(serviceId);
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(virtualGatewayId);
        GatewayDto gatewayDto = gatewayService.get(virtualGatewayDto.getGwId());
        //获取当前已发布的服务
        List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList = listServiceProtobufProxy(virtualGatewayId)
                .stream().filter(e -> e.getServiceId() != serviceDto.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(envoyServiceProtobufProxyList)) {
            return doDeleteGrpcEnvoyFilter(virtualGatewayDto.getPort(), gatewayDto.getConfAddr(), gatewayDto.getGwClusterName());
        }
        return doOfflineServiceProtobuf(serviceId, virtualGatewayDto.getPort(), gatewayDto.getConfAddr(),
                gatewayDto.getGwClusterName(), envoyServiceProtobufProxyList);
    }

    private ErrorCode doDeleteGrpcEnvoyFilter(int listenerPort, String apiPlaneAddr, String gwClusterName) {
        return grpcProtobufRemoteClient.deleteGrpcEnvoyFilterToAPIPlane(listenerPort, apiPlaneAddr, gwClusterName)
                ? CommonErrorCode.SUCCESS : EnvoyErrorCode.OFFLINE_PROTOBUF_FAILED;
    }

    private ErrorCode doOfflineServiceProtobuf(long serviceId, int port, String apiPlaneAddr, String gwClusterName, List<EnvoyServiceProtobufProxy> envoyServiceProtobufProxyList) {
        List<String> sourcePathList = new ArrayList<>();
        List<File> pbFileList = new ArrayList<>();
        List<String> publishServiceList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        envoyServiceProtobufProxyList.forEach(serviceProtobuf -> {
            publishServiceList.addAll(JSON.parseArray(serviceProtobuf.getPbServiceList(), String.class));
            envoyGrpcProtobufCompileService.pbFileContentToFile(serviceId, serviceProtobuf.getPbFileContent().getBytes(), sourcePathList, pbFileList, map);
        });
        if (!(boolean) map.get(EnvoyGrpcProtobufCompileServiceImpl.RESULT)) {
            return EnvoyErrorCode.PROCESS_PROTOBUF_FAILED;
        }
        Map<String, Object> resultMap = envoyGrpcProtobufCompileService.compilePbFile(sourcePathList, pbFileList);
        //更新
        boolean result = grpcProtobufRemoteClient.publishGrpcEnvoyFilterToAPIPlane(port, apiPlaneAddr, gwClusterName, (String) resultMap.get(EnvoyGrpcProtobufCompileServiceImpl.DESC_FILE_BASE64), publishServiceList);
        if (!result) {
            return EnvoyErrorCode.PUBLISH_PROTOBUF_FAILED;
        }
        return CommonErrorCode.SUCCESS;
    }

    private void deleteServiceProtobufProxy(long serviceId, long virtualGwId) {
        EnvoyServiceProtobufProxy serviceProtobufProxy = getServiceProtobufProxy(serviceId, virtualGwId);
        if (serviceProtobufProxy != null) {
            serviceProtobufProxyDao.delete(serviceProtobufProxy);
            Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
            params.put("pbProxyId", serviceProtobufProxy.getId());
            List<PbService> pbServiceListInDB = pbServiceDao.getRecordsByField(params);
            if (!CollectionUtils.isEmpty(pbServiceListInDB)) {
                pbServiceListInDB.forEach(pbService -> {
                    pbService.setPbProxyId(PbService.NOT_PUBLISHED_PB_PROXY_ID);
                    pbService.setPublishStatus(PbService.PUBLISH_STATUS_NOT_PUBLISHED);
                    pbServiceDao.update(pbService);
                });
            }
        }
    }

    @Override
    public List<EnvoyPublishedServiceProtobufDto> listPublishedServiceProtobuf(long serviceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.SERVICE_ID, serviceId);

        List<EnvoyServiceProtobufProxy> serviceProtobufProxyList = serviceProtobufProxyDao.getRecordsByField(params);
        List<EnvoyPublishedServiceProtobufDto> publishedServiceProtobufDtoList = new ArrayList<>();

        serviceProtobufProxyList.forEach(serviceProtobufProxy -> {
            long virtualGwId = serviceProtobufProxy.getVirtualGwId();
            VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
            ServiceProxyDto serviceProxyInfo = serviceProxyService.getServiceProxyByServiceIdAndGwId(virtualGwId, serviceId);
            publishedServiceProtobufDtoList.add(new EnvoyPublishedServiceProtobufDto(serviceProtobufProxy, virtualGwId, virtualGateway != null ? virtualGateway.getName() : null, serviceProxyInfo != null ? serviceProxyInfo.getBackendService().split(",") : null));
        });
        return publishedServiceProtobufDtoList;
    }

}
