package org.hango.cloud.envoy.infra.dubbo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.exception.ErrorCodeException;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.ClassTypeUtil;
import org.hango.cloud.common.infra.routeproxy.dto.RouteRuleProxyDto;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.serviceregistry.meta.RegistryCenterEnum;
import org.hango.cloud.envoy.infra.base.meta.EnvoyErrorCode;
import org.hango.cloud.envoy.infra.base.util.EnvoyCommonUtil;
import org.hango.cloud.envoy.infra.dubbo.dao.IDubboBindingDao;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboBindingDto;
import org.hango.cloud.envoy.infra.dubbo.dto.DubboMetaDto;
import org.hango.cloud.envoy.infra.dubbo.meta.DubboBindingInfo;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboBindingService;
import org.hango.cloud.envoy.infra.dubbo.service.IDubboMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
@Service
public class DubboBindingServiceImpl implements IDubboBindingService {

    private static final Logger logger = LoggerFactory.getLogger(DubboBindingServiceImpl.class);

    @Autowired
    private IDubboBindingDao dubboBindingDao;

    @Autowired
    private IDubboMetaService dubboMetaService;

    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Override
    public long saveDubboInfo(DubboBindingDto dto) {
        return getByIdAndType(dto.getObjectId(), dto.getObjectType()) == null ? create(dto) : update(dto);
    }

    @Override
    public long create(DubboBindingDto dubboBindingDto) {
        if (dubboBindingDto == null) {
            return 0;
        }
        boolean result = false;
        //publish to envoy
        if (BaseConst.ROUTE.equals(dubboBindingDto.getObjectType())) {
            result = publishToEnvoy(dubboBindingDto, false);
        }
        //g0 暂未实现

        if (!result) {
            return BaseConst.ERROR_RESULT;
        }
        return dubboBindingDao.add(toMeta(dubboBindingDto));
    }

    @Override
    public long update(DubboBindingDto dubboBindingDto) {
        DubboBindingDto dtoFromDb = getByIdAndType(dubboBindingDto.getObjectId(), dubboBindingDto.getObjectType());
        if (dtoFromDb == null) {
            return NumberUtils.LONG_ZERO;
        }
        boolean result = false;
        //publish to envoy
        if (BaseConst.ROUTE.equals(dubboBindingDto.getObjectType())) {
            result = publishToEnvoy(dubboBindingDto, false);
        }
        if (!result) {
            return BaseConst.ERROR_RESULT;
        }
        return dubboBindingDao.update(toMeta(dubboBindingDto));
    }

    @Override
    public void delete(DubboBindingDto dubboBindingDto) {
        if (dubboBindingDto == null) {
            return;
        }
        //publish to envoy
        if (BaseConst.ROUTE.equals(dubboBindingDto.getObjectType())
                && !publishToEnvoy(dubboBindingDto, true)) {
            throw new ErrorCodeException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        dubboBindingDao.delete(toMeta(dubboBindingDto));
    }

    @Override
    public boolean deleteDubboInfo(long objectId, String objectType) {
        DubboBindingDto dubboBindingDto = getByIdAndType(objectId, objectType);
        if (dubboBindingDto == null) {
            return true;
        }
        try {
            delete(dubboBindingDto);
        } catch (ErrorCodeException e) {
            return false;
        }
        return true;
    }

    @Override
    public DubboBindingDto get(long id) {
        return toView(dubboBindingDao.get(id));
    }


    @Override
    public DubboBindingDto getByIdAndType(long objectId, String objectType) {
        Map<String, Object> param = new HashMap<>();
        param.put("objectId", objectId);
        param.put("objectType", objectType);
        List<DubboBindingInfo> infoList = dubboBindingDao.getRecordsByField(param);
        if (CollectionUtils.isEmpty(infoList)) {
            logger.info("未找到Dubbo数据，查询条件为 objectId = {}, objectType = {}", objectId, objectType);
            return null;
        }
        return toView(infoList.get(0));
    }


    @Override
    public DubboBindingDto toView(DubboBindingInfo info) {
        if (info == null) {
            return null;
        }
        DubboBindingDto dubboBindingDto = new DubboBindingDto();
        dubboBindingDto.setObjectId(info.getObjectId());
        dubboBindingDto.setObjectType(info.getObjectType());
        DubboBindingDto.DubboMeta meta = JSON.parseObject(info.getDubboInfo(), DubboBindingDto.DubboMeta.class);
        dubboBindingDto.setMethod(meta.getMethod());
        dubboBindingDto.setInterfaceName(meta.getInterfaceName());
        dubboBindingDto.setVersion(meta.getVersion());
        dubboBindingDto.setGroup(meta.getGroup());
        dubboBindingDto.setParams(meta.getParams());
        dubboBindingDto.setParamSource(meta.getParamSource());
        dubboBindingDto.setCustomParamMapping(meta.getCustomParamMapping());
        dubboBindingDto.setDubboAttachment(meta.getAttachmentInfo());
        //默认值json格式转换为json string以适配前端api
        if (!CollectionUtils.isEmpty(dubboBindingDto.getParams())) {
            dubboBindingDto.getParams().forEach(p -> p.setDefaultValue(JSONObject.toJSONString(p.getDefaultValue())));
        }
        return dubboBindingDto;
    }


    @Override
    public DubboBindingInfo toMeta(DubboBindingDto dubboBindingDto) {
        DubboBindingInfo dubboBindingInfo = new DubboBindingInfo();
        dubboBindingInfo.setObjectId(dubboBindingDto.getObjectId());
        dubboBindingInfo.setObjectType(dubboBindingDto.getObjectType());
        DubboBindingDto.DubboMeta meta = new DubboBindingDto.DubboMeta();
        meta.setGroup(dubboBindingDto.getGroup());
        meta.setVersion(dubboBindingDto.getVersion());
        meta.setMethod(dubboBindingDto.getMethod());
        meta.setInterfaceName(dubboBindingDto.getInterfaceName());
        meta.setParams(dubboBindingDto.getParams());
        meta.setParamSource(dubboBindingDto.getParamSource());
        meta.setCustomParamMapping(dubboBindingDto.getCustomParamMapping());
        meta.setAttachmentInfo(dubboBindingDto.getDubboAttachment());
        dubboBindingInfo.setDubboInfo(JSON.toJSONString(meta));
        return dubboBindingInfo;
    }

    @Override
    public void processMethodWorks(DubboBindingDto dto) {
        if (dto == null) {
            return;
        }
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.get(dto.getObjectId());
        if (routeRuleProxyDto == null) {
            logger.error("未找到已发布路由信息，查询条件为 routeRuleProxyId:{}", dto.getObjectId());
            return;
        }
        //获取dubbo meta信息
        List<DubboMetaDto> dubboMetaDtos = dubboMetaService.findByCondition(routeRuleProxyDto.getVirtualGwId(), dto.getInterfaceName(), dto.getGroup(), dto.getVersion());
        //判断方法是否失效
        for (DubboMetaDto dubboMetaDto : dubboMetaDtos) {
            if (dto.getMethod().equals(dubboMetaDto.getMethod()) && equalParams(dto.getParams(), dubboMetaDto.getParams())) {
                dto.setMethodWorks(true);
                return;
            }
        }
        dto.setMethodWorks(false);
    }

    private boolean equalParams(List<DubboBindingDto.DubboParam> dubboParams, List<String> params) {
        List<String> dubboParamInfo = CollectionUtils.isEmpty(dubboParams) ?
                new ArrayList<>() :
                dubboParams.stream().map(DubboBindingDto.DubboParam::getValue).collect(Collectors.toList());
        List<String> paramInfo = CollectionUtils.isEmpty(params) ? new ArrayList<>() : params;
        return dubboParamInfo.equals(paramInfo);
    }

    @Override
    public ErrorCode checkAndComplete(DubboBindingDto dto) {
        RouteRuleProxyDto routeRuleProxyDto = routeRuleProxyService.get(dto.getObjectId());
        if (routeRuleProxyDto == null) {
            logger.info("参数校验失败，未找到已发布的路由，已发布路由ID为 {}", dto.getObjectId());
            return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
        }
        ServiceProxyDto serviceProxy = serviceProxyService.getServiceProxyByServiceIdAndGwId(
                routeRuleProxyDto.getVirtualGwId(), routeRuleProxyDto.getServiceId());
        if (serviceProxy == null) {
            logger.info("参数校验失败，未找到已发布的服务，服务ID为 {}", routeRuleProxyDto.getServiceId());
            return CommonErrorCode.SERVICE_NOT_PUBLISHED;
        }
        if (!RegistryCenterEnum.Zookeeper.getType().equals(serviceProxy.getRegistryCenterType())) {
            logger.info("参数校验失败，该服务的发布方式并不支持本操作, 服务发布方式为 {}", serviceProxy.getRegistryCenterType());
            return CommonErrorCode.PUBLISH_TYPE_NOT_SUPPORT;
        }
        //自定义参数校验
        if (dto.getCustomParamMapping()) {
            ErrorCode errorCode = checkCustomParamMapping(dto);
            if (CommonErrorCode.SUCCESS != errorCode) {
                return errorCode;
            }
        }
        String backendService = serviceProxy.getBackendService();
        String[] meta = EnvoyCommonUtil.splitIgv(backendService);
        dto.setInterfaceName(meta[0]);
        dto.setGroup(meta[1]);
        dto.setVersion(meta[2]);
        return CommonErrorCode.SUCCESS;
    }


    /**
     * 自定义参数映射校验
     *
     * @param dto
     * @return
     */
    private ErrorCode checkCustomParamMapping(DubboBindingDto dto) {
        List<DubboBindingDto.DubboParam> params = dto.getParams();
        for (DubboBindingDto.DubboParam param : params) {
            //参数名不能为空
            if (StringUtils.isBlank(param.getKey())) {
                logger.error("自定义参数映射不允许参数名为空 : {}", JSON.toJSONString(dto));
                return CommonErrorCode.PARAMETER_NULL;
            }
            //参数类型不能为空
            if (StringUtils.isBlank(param.getValue())) {
                logger.error("自定义参数映射不允许参数类型为空 : {}", JSON.toJSONString(dto));
                return CommonErrorCode.PARAMETER_NULL;
            }
            //默认值类型校验
            if (param.getDefaultValue() != null) {
                ErrorCode errorCode = checkDefaultValue(param.getValue(), param.getDefaultValue());
                if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
                    return errorCode;
                }
            }
            //泛型校验
            Boolean checkResult = checkGenericInfo(param.getGenericInfo());
            if (!checkResult) {
                logger.error("无效的泛型配置 : {}", JSON.toJSONString(dto));
                return EnvoyErrorCode.GENERIC_INFO_INVALID;
            }
        }
        //隐式参数校验
        if (!checkDubboAttachment(dto.getDubboAttachment())) {
            return EnvoyErrorCode.DUBBO_ATTACHMENT_CONFIG_INVAILD;
        }
        return CommonErrorCode.SUCCESS;
    }

    private Boolean checkDubboAttachment(List<DubboBindingDto.DubboAttachmentDto> dubboAttachmentDtos) {
        if (CollectionUtils.isEmpty(dubboAttachmentDtos)) {
            return Boolean.TRUE;
        }
        for (DubboBindingDto.DubboAttachmentDto dubboAttachmentDto : dubboAttachmentDtos) {
            String paramPosition = dubboAttachmentDto.getParamPosition();
            if (StringUtils.isBlank(paramPosition)) {
                logger.error("隐式参数来源不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }
            if (!BaseConst.POSITION_COOKIE.equalsIgnoreCase(paramPosition) && !BaseConst.POSITION_HEADER.equalsIgnoreCase(paramPosition)) {
                logger.error("隐式参数来源错误 : {}", paramPosition);
                return Boolean.FALSE;
            }
            if (StringUtils.isBlank(dubboAttachmentDto.getClientParamName())) {
                logger.error("参数名不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }

            if (StringUtils.isBlank(dubboAttachmentDto.getServerParamName())) {
                logger.error("隐式参数名不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }
        }
        long clientCount = dubboAttachmentDtos.stream().map(o -> o.getParamPosition() + o.getClientParamName()).distinct().count();
        if (clientCount != dubboAttachmentDtos.size()) {
            logger.error("参数名重复 : {}", JSON.toJSONString(dubboAttachmentDtos));

            return Boolean.FALSE;
        }
        long serverCount = dubboAttachmentDtos.stream().map(DubboBindingDto.DubboAttachmentDto::getServerParamName).distinct().count();
        if (serverCount != dubboAttachmentDtos.size()) {
            logger.error("隐式参数名重复 : {}", JSON.toJSONString(dubboAttachmentDtos));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private ErrorCode checkDefaultValue(String javaType, Object defaultValue) {
        if (!ClassTypeUtil.isMapClass(javaType) && !ClassTypeUtil.isCollectionClass(javaType) && !ClassTypeUtil.isPrimitive(javaType) && !ClassTypeUtil.isWrapperClass(javaType)) {
            logger.error("默认值类型不支持 : {}", javaType);
            return CommonErrorCode.UN_SUPPORTED_DEFAULT_VALUE_TYPE;
        }
        //boolean类型校验
        if (ClassTypeUtil.isBooleanClass(javaType) && !(defaultValue instanceof Boolean)) {
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        //数值类型校验
        if (ClassTypeUtil.isNumberClass(javaType) && !(defaultValue instanceof Number)) {
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        //字符串类型校验
        if (ClassTypeUtil.isStringClass(javaType) && !(defaultValue instanceof String)) {
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        //集合类型校验
        if (ClassTypeUtil.isCollectionClass(javaType) && !(defaultValue instanceof Collection)) {
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        //字典类型校验
        if (ClassTypeUtil.isMapClass(javaType) && !(defaultValue instanceof Map)) {
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        //兜底校验
        String str = JSONObject.toJSONString(defaultValue);
        try {
            JSONObject.parseObject(str, ClassTypeUtil.getClassForName(javaType));
        } catch (Exception e) {
            logger.error("默认值配置错误，type:{}, defaultValue:{}", javaType, str);
            return CommonErrorCode.DEFAULT_VALUE_CONFIG_INVALID;
        }
        return CommonErrorCode.SUCCESS;
    }

    public Boolean checkGenericInfo(String genericInfo) {
        if (StringUtils.isBlank(genericInfo)) {
            return Boolean.TRUE;
        }
        String[] genericConfigs = genericInfo.split(",");
        for (String genericConfig : genericConfigs) {
            if (StringUtils.isBlank(genericConfig)) {
                return Boolean.FALSE;
            }
            if (!genericConfig.startsWith(".")) {
                return Boolean.FALSE;
            }
            String[] split = genericConfig.split(":");
            if (split.length != 2) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }


    private boolean publishToEnvoy(DubboBindingDto dto, boolean isDelete) {
        RouteRuleProxyDto routeRuleProxy = routeRuleProxyService.get(dto.getObjectId());
        if (routeRuleProxy == null) {
            logger.info("发布失败，未找到已发布的路由，已发布路由ID为 {}", dto.getObjectId());
            return false;
        }
        parseDefaultValue(dto);
        Map<String, String> metaMap = routeRuleProxy.getMetaMap() == null ? Maps.newHashMap() : routeRuleProxy.getMetaMap();
        metaMap.put("DubboMeta", isDelete ? StringUtils.EMPTY : JSON.toJSONString(dto));
        routeRuleProxy.setMetaMap(metaMap);
        return BaseConst.ERROR_RESULT != routeRuleProxyService.update(routeRuleProxy);
    }

    public Object parseDefaultValue(String typeString, Object value){
        if (value == null && ClassTypeUtil.PrimitiveTypeEnum.isPrimitiveType(typeString)){
            return ClassTypeUtil.PrimitiveTypeEnum.getDefaultValueByName(typeString);
        }
        return value;
    }

    public void parseDefaultValue(DubboBindingDto.DubboParam param){
        Object o = parseDefaultValue(param.getValue(), param.getDefaultValue());
        param.setDefaultValue(o);
    }

    @Override
    public void parseDefaultValue(DubboBindingDto dto){
        List<DubboBindingDto.DubboParam> params = dto.getParams();
        if (!CollectionUtils.isEmpty(params)){
            params.stream().forEach(this::parseDefaultValue);
        }
    }
}
