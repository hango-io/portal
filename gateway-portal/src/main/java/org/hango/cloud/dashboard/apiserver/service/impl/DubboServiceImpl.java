package org.hango.cloud.dashboard.apiserver.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.IDubboDao;
import org.hango.cloud.dashboard.apiserver.dto.DubboInfoDto;
import org.hango.cloud.dashboard.apiserver.dto.DubboMetaDto;
import org.hango.cloud.dashboard.apiserver.meta.DubboInfo;
import org.hango.cloud.dashboard.apiserver.meta.RegistryCenterEnum;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.service.IDubboMetaService;
import org.hango.cloud.dashboard.apiserver.service.IDubboService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleInfoService;
import org.hango.cloud.dashboard.apiserver.service.IRouteRuleProxyService;
import org.hango.cloud.dashboard.apiserver.service.IServiceProxyService;
import org.hango.cloud.dashboard.apiserver.util.ClassTypeUtil;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.util.ZkClientUtils;
import org.hango.cloud.dashboard.envoy.dao.IRouteRuleProxyDao;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleInfo;
import org.hango.cloud.dashboard.envoy.meta.RouteRuleProxyInfo;
import org.hango.cloud.dashboard.envoy.meta.ServiceProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.dashboard.apiserver.util.Const.POSITION_COOKIE;
import static org.hango.cloud.dashboard.apiserver.util.Const.POSITION_HEADER;

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

    @Autowired
    private IRouteRuleProxyDao routeRuleProxyDao;

    @Autowired
    private IDubboMetaService dubboMetaService;

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
        DubboInfoDto dubboInfoDto = DubboInfoDto.toDto(getDubboInfo(objectId, objectType));
        //默认值json格式转换为json string以适配前端api
        if (dubboInfoDto != null && dubboInfoDto.getParams() != null){
            for (DubboInfoDto.DubboParam param : dubboInfoDto.getParams()) {
                param.setDefaultValue(JSONObject.toJSONString(param.getDefaultValue()));
            }
        }
        return dubboInfoDto;
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
    public void processMethodWorks(DubboInfoDto dto) {
        if (dto == null){
            return;
        }
        //获取已发布路由信息
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Id", dto.getObjectId());
        List<RouteRuleProxyInfo> routeRuleProxyInfos = routeRuleProxyDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(routeRuleProxyInfos)){
            logger.error("未找到已发布路由信息，查询条件为 routeRuleProxyId:{}", dto.getObjectId());
            return;
        }
        RouteRuleProxyInfo proxyInfo = routeRuleProxyInfos.get(0);
        //获取dubbo meta信息
        List<DubboMetaDto> dubboMetaDtos = dubboMetaService.findByCondition(proxyInfo.getGwId(), dto.getInterfaceName(), dto.getGroup(), dto.getVersion());
        //判断方法是否失效
        for (DubboMetaDto dubboMetaDto : dubboMetaDtos) {
            if (dto.getMethod().equals(dubboMetaDto.getMethod()) && equalParams(dto.getParams(), dubboMetaDto.getParams())){
                dto.setMethodWorks(true);
                return;
            }
        }
        dto.setMethodWorks(false);
    }

    private boolean equalParams(List<DubboInfoDto.DubboParam> dubboParams, List<String> params){
        List<String> dubboParamInfo = CollectionUtils.isEmpty(dubboParams) ? new ArrayList<>() : dubboParams.stream().map(DubboInfoDto.DubboParam::getValue).collect(Collectors.toList());
        List<String> paramInfo = CollectionUtils.isEmpty(params) ? new ArrayList<>() : params;
        return dubboParamInfo.equals(paramInfo);
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
        //自定义参数校验
        if (dto.getCustomParamMapping()){
            ErrorCode errorCode = checkCustomParamMapping(dto);
            if (CommonErrorCode.Success != errorCode){
                return errorCode;
            }
        }
        String backendService = serviceProxy.getBackendService();
        String[] meta = ZkClientUtils.splitIgv(backendService);
        dto.setInterfaceName(meta[0]);
        dto.setGroup(meta[1]);
        dto.setVersion(meta[2]);
        return CommonErrorCode.Success;
    }

    //自定义参数映射校验
    private ErrorCode checkCustomParamMapping(DubboInfoDto dto){
        List<DubboInfoDto.DubboParam> params = dto.getParams();
        for (DubboInfoDto.DubboParam param : params) {
            //参数名不能为空
            if (StringUtils.isBlank(param.getKey())){
                logger.error("自定义参数映射不允许参数名为空 : {}", JSON.toJSONString(dto));
                return CommonErrorCode.ParameterNull;
            }
            //参数类型不能为空
            if (StringUtils.isBlank(param.getValue())){
                logger.error("自定义参数映射不允许参数类型为空 : {}", JSON.toJSONString(dto));
                return CommonErrorCode.ParameterNull;
            }
            //默认值类型校验
            if (param.getDefaultValue() != null){
                ErrorCode errorCode = checkDefaultValue(param.getValue(), param.getDefaultValue());
                if (!CommonErrorCode.Success.equals(errorCode)){
                    return errorCode;
                }
            }
            //泛型校验
            Boolean checkResult = checkGenericInfo(param.getGenericInfo());
            if (!checkResult){
                logger.error("无效的泛型配置 : {}", JSON.toJSONString(dto));
                return CommonErrorCode.GenericInfoInvalid;
            }
        }
        //隐式参数校验
        if (!checkDubboAttachment(dto.getDubboAttachment())){
            return CommonErrorCode.DubboAttachmentConfigInvaild;
        }
        return CommonErrorCode.Success;
    }

    private Boolean checkDubboAttachment(List<DubboInfoDto.DubboAttachmentDto> dubboAttachmentDtos){
        if (CollectionUtils.isEmpty(dubboAttachmentDtos)){
            return Boolean.TRUE;
        }
        for (DubboInfoDto.DubboAttachmentDto dubboAttachmentDto : dubboAttachmentDtos) {
            String paramPosition = dubboAttachmentDto.getParamPosition();
            if (StringUtils.isBlank(paramPosition)){
                logger.error("隐式参数来源不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }
            if (!POSITION_COOKIE.equalsIgnoreCase(paramPosition) && !POSITION_HEADER.equalsIgnoreCase(paramPosition)){
                logger.error("隐式参数来源错误 : {}", paramPosition);
                return Boolean.FALSE;
            }
            if (StringUtils.isBlank(dubboAttachmentDto.getClientParamName())){
                logger.error("参数名不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }

            if (StringUtils.isBlank(dubboAttachmentDto.getServerParamName())){
                logger.error("隐式参数名不能为空 : {}", JSON.toJSONString(dubboAttachmentDto));
                return Boolean.FALSE;
            }
        }
        long clientCount = dubboAttachmentDtos.stream().map(o -> o.getParamPosition() + o.getClientParamName()).distinct().count();
        if (clientCount != dubboAttachmentDtos.size()){
            logger.error("参数名重复 : {}", JSON.toJSONString(dubboAttachmentDtos));

            return Boolean.FALSE;
        }
        long serverCount = dubboAttachmentDtos.stream().map(DubboInfoDto.DubboAttachmentDto::getServerParamName).distinct().count();
        if (serverCount != dubboAttachmentDtos.size()){
            logger.error("隐式参数名重复 : {}", JSON.toJSONString(dubboAttachmentDtos));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private ErrorCode checkDefaultValue(String javaType, Object defaultValue){
        if (!ClassTypeUtil.isMapClass(javaType) && !ClassTypeUtil.isCollectionClass(javaType) && !ClassTypeUtil.isPrimitive(javaType) && !ClassTypeUtil.isWrapperClass(javaType)){
            logger.error("默认值类型不支持 : {}", javaType);
            return CommonErrorCode.UnSupportedDefaultValueType;
        }
        //boolean类型校验
        if (ClassTypeUtil.isBooleanClass(javaType) && !(defaultValue instanceof Boolean)){
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        //数值类型校验
        if (ClassTypeUtil.isNumberClass(javaType) && !(defaultValue instanceof Number)){
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        //字符串类型校验
        if (ClassTypeUtil.isStringClass(javaType) && !(defaultValue instanceof String)){
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        //集合类型校验
        if (ClassTypeUtil.isCollectionClass(javaType) && !(defaultValue instanceof Collection)){
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        //字典类型校验
        if (ClassTypeUtil.isMapClass(javaType) && !(defaultValue instanceof Map)){
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        //兜底校验
        String str = JSONObject.toJSONString(defaultValue);
        try {
            JSONObject.parseObject(str, ClassTypeUtil.getClassForName(javaType));
        } catch (Exception e) {
            logger.error("默认值配置错误，type:{}, defaultValue:{}", javaType, str);
            return CommonErrorCode.DefaultValueConfigInvaild;
        }
        return CommonErrorCode.Success;
    }

    public Boolean checkGenericInfo(String genericInfo){
        if (StringUtils.isBlank(genericInfo)){
            return Boolean.TRUE;
        }
        String[] genericConfigs = genericInfo.split(",");
        for (String genericConfig : genericConfigs) {
            if (StringUtils.isBlank(genericConfig)) {
                return Boolean.FALSE;
            }
            if (!genericConfig.startsWith(".")){
                return Boolean.FALSE;
            }
            String[] split = genericConfig.split(":");
            if (split.length != 2){
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
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
        parseDefaultValue(dto);
        RouteRuleInfo routeRule = envoyRouteRuleInfoService.getRouteRuleInfoById(routeRuleProxy.getRouteRuleId());
        if (routeRule == null) {
            logger.info("发布失败，未找到路由，路由ID为 {}", routeRuleProxy.getRouteRuleId());
            return false;
        }
        Map<String, String> metaMap = routeRuleProxy.getMetaMap() == null ? Maps.newHashMap() : routeRuleProxy.getMetaMap();
        metaMap.put("DubboMeta", isDelete ? StringUtils.EMPTY : JSON.toJSONString(dto));
        routeRuleProxy.setMetaMap(metaMap);
        return Const.ERROR_RESULT != envoyRouteRuleProxyService.updateEnvoyRouteRuleProxy(routeRuleProxy);
    }

    public Object parseDefaultValue(String typeString, Object value){
        if (value == null && ClassTypeUtil.PrimitiveTypeEnum.isPrimitiveType(typeString)){
            return ClassTypeUtil.PrimitiveTypeEnum.getDefaultValueByName(typeString);
        }
        return value;
    }

    public void parseDefaultValue(DubboInfoDto.DubboParam param){
        Object o = parseDefaultValue(param.getValue(), param.getDefaultValue());
        param.setDefaultValue(o);
    }

    @Override
    public void parseDefaultValue(DubboInfoDto dto){
        List<DubboInfoDto.DubboParam> params = dto.getParams();
        if (!CollectionUtils.isEmpty(params)){
            params.stream().forEach(this::parseDefaultValue);
        }
    }
}
