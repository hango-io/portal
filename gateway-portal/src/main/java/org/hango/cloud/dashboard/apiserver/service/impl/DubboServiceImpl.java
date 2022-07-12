package org.hango.cloud.dashboard.apiserver.service.impl;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.IDubboDao;
import org.hango.cloud.dashboard.apiserver.dto.DubboInfoDto;
import org.hango.cloud.dashboard.apiserver.meta.DubboInfo;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IDubboService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.ZkClientUtils;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyRouteRuleHeaderOperationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
@Service
public class DubboServiceImpl implements IDubboService {

    private static final Logger logger = LoggerFactory.getLogger(DubboServiceImpl.class);

    @Autowired
    private IDubboDao dubboDao;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IRouteRuleProxyService envoyRouteRuleProxyService;

    @Autowired
    private IRouteRuleInfoService envoyRouteRuleInfoService;

    @Override
    public long addDubboInfo(DubboInfoDto dto) {
        if (dto == null) {
            return 0;
        }
        boolean result = false;
        //publish to envoy
        if (Const.ROUTE.equals(dto.getObjectType())) {
            result = publishToEnvoy(dto, false);
        }
        //g0 暂未实现

        if (!result) {
            return Const.ERROR_RESULT;
        }
        DubboInfo dubboInfo = dto.toMeta();
        return dubboDao.add(dubboInfo);
    }


    @Override
    public boolean deleteDubboInfo(long objectId, String objectType) {
        DubboInfo dubboInfo = getDubboInfo(objectId, objectType);
        if (dubboInfo == null) {
            return true;
        }
        //publish to envoy
        boolean result = false;
        if (Const.ROUTE.equals(dubboInfo.getObjectType())) {
            result = publishToEnvoy(DubboInfoDto.toDto(dubboInfo), true);
        }
        //g0 暂未实现
        if (!result) {
            return false;
        }
        dubboDao.delete(dubboInfo);
        return true;
    }

    @Override
    public void delete(long objectId, String objectType) {
        DubboInfo dubboInfo = getDubboInfo(objectId, objectType);
        if (dubboInfo == null) {
            return;
        }
        dubboDao.delete(dubboInfo);
    }

    @Override
    public DubboInfoDto getDubboDto(long objectId, String objectType) {
        return DubboInfoDto.toDto(getDubboInfo(objectId, objectType));
    }

    @Override
    public DubboInfo getDubboInfo(long objectId, String objectType) {
        Map<String, Object> param = new HashMap<>();
        param.put("objectId", objectId);
        param.put("objectType", objectType);
        List<DubboInfo> infoList = dubboDao.getRecordsByField(param);
        if (CollectionUtils.isEmpty(infoList)) {
            logger.info("未找到Dubbo数据，查询条件为 objectId = {}, objectType = {}", objectId, objectType);
            return null;
        }
        return infoList.get(0);
    }

    @Override
    public long updateDubboInfo(DubboInfoDto dto) {
        DubboInfo dtoFromDb = getDubboInfo(dto.getObjectId(), dto.getObjectType());
        if (dtoFromDb == null) {
            return NumberUtils.LONG_ZERO;
        }
        boolean result = false;
        //publish to envoy
        if (Const.ROUTE.equals(dto.getObjectType())) {
            result = publishToEnvoy(dto, false);
        }
        //g0 暂未实现

        if (!result) {
            return Const.ERROR_RESULT;
        }
        DubboInfo updateInfo = dto.toMeta();
        updateInfo.setId(dtoFromDb.getId());
        return dubboDao.update(updateInfo);
    }

    @Override
    public long saveDubboInfo(DubboInfoDto dto) {
        return getDubboInfo(dto.getObjectId(), dto.getObjectType()) == null ? addDubboInfo(dto) : updateDubboInfo(dto);
    }

    @Override
    public ErrorCode checkAndComplete(DubboInfoDto dto) {
        return Const.ROUTE.equals(dto.getObjectType()) ? checkAndCompleteEnvoyDubbo(dto) : checkAndCompleteG0Dubbo(dto);
    }

    private ErrorCode checkAndCompleteEnvoyDubbo(DubboInfoDto dto) {
        RouteRuleProxyInfo routeRuleProxy = envoyRouteRuleProxyService.getRouteRuleProxy(dto.getObjectId());
        if (routeRuleProxy == null) {
            logger.info("参数校验失败，未找到已发布的路由，已发布路由ID为 {}", dto.getObjectId());
            return CommonErrorCode.RouteRuleNotPublished;
        }
        ServiceProxyInfo serviceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(routeRuleProxy.getGwId()
                , routeRuleProxy.getServiceId());
        if (serviceProxy == null) {
            logger.info("参数校验失败，未找到已发布的服务，服务ID为 {}", routeRuleProxy.getServiceId());
            return CommonErrorCode.ServiceNotPublished;
        }
        if (!RegistryCenterEnum.Zookeeper.getType().equals(serviceProxy.getRegistryCenterType())) {
            logger.info("参数校验失败，该服务的发布方式并不支持本操作, 服务发布方式为 {}", serviceProxy.getRegistryCenterType());
            return CommonErrorCode.PublishTypeNotSupport;
        }
        List<DubboInfoDto.DubboParam> paramWithNoneKeyList = dto.getParams().stream().filter(f -> StringUtils.isBlank(f.getKey())).collect(Collectors.toList());
        //开启自定义参数映射开关后， 不允许自定义名称为空的存在
        if (dto.getCustomParamMapping() && !CollectionUtils.isEmpty(paramWithNoneKeyList)) {
            logger.info("开启自定义参数映射开关后， 不允许自定义名称为空的存在 : {}", JSON.toJSONString(paramWithNoneKeyList));
            return CommonErrorCode.ParameterNull;
        }
        //关闭自定义参数映射开关后， 所有自定义名称必须为空
        //当dto.getParams()元素数与paramWithNoneKeyList 元素数相等，代表所有自定义名称为空
        if (!dto.getCustomParamMapping() && dto.getParams().size() != paramWithNoneKeyList.size()) {
            logger.info("关闭自定义参数映射开关后， 所有自定义名称必须为空 : {}", JSON.toJSONString(dto));
            return CommonErrorCode.CustomParamMappingInvalid;
        }
        String backendService = serviceProxy.getBackendService();
        String[] meta = ZkClientUtils.splitIgv(backendService);
        dto.setInterfaceName(meta[0]);
        dto.setGroup(meta[1]);
        dto.setVersion(meta[2]);
        return CommonErrorCode.Success;
    }

    private ErrorCode checkAndCompleteG0Dubbo(DubboInfoDto dto) {
        //暂未实现
        return CommonErrorCode.Success;
    }

    private boolean publishToEnvoy(DubboInfoDto dto, boolean isDelete) {
        RouteRuleProxyInfo routeRuleProxy = envoyRouteRuleProxyService.getRouteRuleProxy(dto.getObjectId());
        if (routeRuleProxy == null) {
            logger.info("发布失败，未找到已发布的路由，已发布路由ID为 {}", dto.getObjectId());
            return false;
        }
        RouteRuleInfo routeRule = envoyRouteRuleInfoService.getRouteRuleInfoById(routeRuleProxy.getRouteRuleId());
        if (routeRule == null) {
            logger.info("发布失败，未找到路由，路由ID为 {}", routeRuleProxy.getRouteRuleId());
            return false;
        }
        EnvoyRouteRuleHeaderOperationDto headerOperation = JSON.parseObject(routeRule.getHeaderOperation(), EnvoyRouteRuleHeaderOperationDto.class);
        headerOperation = getDubboHeaderOperation(isDelete ? null : dto, headerOperation);
        routeRuleProxy.setHeaderOperation(headerOperation);
        return Const.ERROR_RESULT != envoyRouteRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxy);
    }


    @Override
    public EnvoyRouteRuleHeaderOperationDto getDubboHeaderOperation(DubboInfoDto dto, EnvoyRouteRuleHeaderOperationDto headerOperation) {
        if (headerOperation == null) {
            headerOperation = new EnvoyRouteRuleHeaderOperationDto();
        }
        if (dto == null) {
            dto = new DubboInfoDto();
        }
        EnvoyRouteRuleHeaderOperationDto.RequestOperation requestOperation = headerOperation.getRequestOperation();
        if (requestOperation == null) {
            requestOperation = headerOperation.new RequestOperation();
            headerOperation.setRequestOperation(requestOperation);
        }
        Map<String, String> add = requestOperation.getAdd();
        if (add == null) {
            add = new HashMap<>();
            requestOperation.setAdd(add);
        }

        add.put(Const.HEADER_DUBBO_INTERFACE, dto.getInterfaceName());
        add.put(Const.HEADER_DUBBO_METHOD, dto.getMethod());
        add.put(Const.HEADER_DUBBO_GROUP, dto.getGroup());
        add.put(Const.HEADER_DUBBO_VERSION, dto.getVersion());
        add.put(Const.HEADER_DUBBO_PARAMS, dto.getParamToStr());
        add.put(Const.HEADER_DUBBO_CUSTOM_PARAMS_MAPPING_SWITCH, BooleanUtils.toStringTrueFalse(dto.getCustomParamMapping()));
        return headerOperation;
    }
}
