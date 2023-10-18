package org.hango.cloud.envoy.infra.plugin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginUpdateDto;
import org.hango.cloud.common.infra.plugin.dto.UpdatePluginStatusDto;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.util.EnvoyCommonUtil;
import org.hango.cloud.envoy.infra.plugin.dao.ICustomPluginInfoDao;
import org.hango.cloud.envoy.infra.plugin.dto.*;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfo;
import org.hango.cloud.envoy.infra.plugin.meta.CustomPluginInfoQuery;
import org.hango.cloud.envoy.infra.plugin.meta.PluginStatusEnum;
import org.hango.cloud.envoy.infra.plugin.meta.SchemaInfo;
import org.hango.cloud.envoy.infra.plugin.rpc.CustomPluginRpcService;
import org.hango.cloud.envoy.infra.plugin.service.CustomPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.service.IEnvoyPluginInfoService;
import org.hango.cloud.envoy.infra.plugin.util.Trans;
import org.hango.cloud.envoy.infra.pluginmanager.service.IPluginManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.FILE;
import static org.hango.cloud.envoy.infra.base.meta.EnvoyConst.OCI;

/**
 * 自定义插件service层实现类
 *
 * @author xianyanglin
 * @date 2023/7/1 16:15
 */
@Service
public class CustomPluginServiceInfoImpl implements CustomPluginInfoService {
    private static final Logger logger = LoggerFactory.getLogger(CustomPluginServiceInfoImpl.class);
    @Autowired
    private CustomPluginRpcService customPluginRpcService;
    @Autowired
    private ICustomPluginInfoDao customPluginInfoDao;

    @Autowired
    private IPluginBindingInfoDao pluginBindingInfoDao;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IGatewayService iGatewayService;

    @Autowired
    IPluginManagerService iPluginManagerService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayService;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Autowired
    private IRouteService routeService;

    @Autowired
    private IEnvoyPluginInfoService iEnvoyPluginInfoService;

    private static final List<String> VALID_VIRTUAL_GATEWAY = Arrays.asList("ApiGateway", "NetworkProxy", "LoadBalance");



    @Override
    public ErrorCode checkPluginImportParameter(CustomPluginInfoDto customPluginInfoDto) {
        String[] pluginScopes = customPluginInfoDto.getPluginScope().split(",");
        for (String scopes : pluginScopes) {
            if (!BindingObjectTypeEnum.getCustomPluginScope().contains(scopes)) {
                return CommonErrorCode.invalidParameter("插件作用域错误");
            }
        }
        ErrorCode errorCode = checkPluginSchema(customPluginInfoDto.getSchemaContent());
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }
        //校验插件内容
        errorCode = checkPluginContent(customPluginInfoDto.getSourceType(),customPluginInfoDto.getSourceUrl(), customPluginInfoDto.getSourceContent());
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }

        //校验插件名称是否重复
        errorCode = checkPluginType(customPluginInfoDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return errorCode;
        }

        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode checkPluginSchema(String schema){
        if (!StringUtils.hasText(schema)){
            return CommonErrorCode.invalidParameter("schema不能为空");
        }
        SchemaInfo schemaInfo;
        try {
            schemaInfo = JSONObject.parseObject(schema, SchemaInfo.class);
        }catch (Exception e){
            logger.error("schema格式错误, schema:{}",schema, e);
            return CommonErrorCode.invalidParameter("schema格式错误");
        }
        List<JSONObject> layouts = schemaInfo.getLayouts();
        if (CollectionUtils.isEmpty(layouts)){
            return CommonErrorCode.invalidParameter("layouts不能为空");
        }
        for (JSONObject layout : layouts) {
            if (!layout.containsKey("key")){
                return CommonErrorCode.invalidParameter("key 不能为空");
            }
            if (!layout.containsKey("type")){
                return CommonErrorCode.invalidParameter("type 不能为空");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode checkPluginType(CustomPluginInfoDto customPluginInfoDto){
        CustomPluginInfoQuery query = CustomPluginInfoQuery.builder().pluginType(customPluginInfoDto.getPluginType()).build();
        List<CustomPluginInfo> customPluginInfoList = customPluginInfoDao.getCustomPluginInfoList(query);
        if (!CollectionUtils.isEmpty(customPluginInfoList)){
            return CommonErrorCode.invalidParameter("插件名称已存在");
        }
        //自定义插件名称不能与系统插件重复
        List<VirtualGatewayDto> virtualGateways = virtualGatewayService.findAll().stream().filter(o -> VALID_VIRTUAL_GATEWAY.contains(o.getType())).collect(Collectors.toList());
        //查询系统插件
        Set<String> pluginTypes = virtualGateways.stream()
                .filter(virtualGateway -> EnvoyConst.ENVOY_GATEWAY_TYPE.equals(virtualGateway.getGwType()))
                .map(virtualGateway -> iEnvoyPluginInfoService.getSystemPluginInfos(virtualGateway))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .map(PluginInfo::getPluginType)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(pluginTypes)){
            if (pluginTypes.contains(customPluginInfoDto.getPluginType())){
                return CommonErrorCode.invalidParameter("自定义插件名称不能与系统插件重复");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode checkPluginContent(String sourceType, String sourceUrl, MultipartFile sourceContent) {
        if (OCI.equals(sourceType) && !StringUtils.hasText(sourceUrl)){
            return CommonErrorCode.invalidParameter("OCI源码地址不能为空");
        }
        if (FILE.equals(sourceType)){
            if (sourceContent == null || sourceContent.isEmpty()) {
                return CommonErrorCode.invalidParameter("代码文件为空");
            }
            String content = EnvoyCommonUtil.file2Str(sourceContent);
            if (!StringUtils.hasText(content)){
                return CommonErrorCode.invalidParameter("代码文件格式错误");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUpdateCustomPlugin(PluginUpdateDto pluginUpdateDto) {
        long id = pluginUpdateDto.getId();
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(id);
        if (customPluginInfo == null){
            return CommonErrorCode.invalidParameter("自定义插件不存在");
        }
        if (PluginStatusEnum.ONLINE.getStatus().equals(customPluginInfo.getPluginStatus())){
            return CommonErrorCode.invalidParameter("自定义插件已上架，不允许修改");
        }
        String[] pluginScopes = pluginUpdateDto.getPluginScope().split(",");
        for (String scopes : pluginScopes) {
            if (!BindingObjectTypeEnum.getCustomPluginScope().contains(scopes)) {
                return CommonErrorCode.invalidParameter("插件作用域错误");
            }
        }
        return checkPluginContent(pluginUpdateDto.getSourceType(), pluginUpdateDto.getSourceUrl(), pluginUpdateDto.getSourceContent());
    }

    @Override
    public ErrorCode checkUpdatePluginStatus(UpdatePluginStatusDto updatePluginStatusDto) {
        long id = updatePluginStatusDto.getId();
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(id);
        if (customPluginInfo == null){
            return CommonErrorCode.invalidParameter("自定义插件不存在");
        }
        String pluginStatus = updatePluginStatusDto.getPluginStatus();
        if (customPluginInfo.getPluginStatus().equals(pluginStatus)){
            return CommonErrorCode.invalidParameter("插件状态已{}，请刷新页面", updatePluginStatusDto.getPluginStatus());
        }
        //下架插件校验
        if (PluginStatusEnum.OFFLINE.getStatus().equals(pluginStatus)){
            PluginBindingInfoQuery query = PluginBindingInfoQuery.builder().pluginType(Collections.singletonList(customPluginInfo.getPluginType())).build();
            List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getPluginBindingInfoList(query);
            if (!CollectionUtils.isEmpty(pluginBindingInfoList)){
                return CommonErrorCode.invalidParameter("插件已绑定网关，不允许下架");
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeletePlugin(Long id) {
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(id);
        if (customPluginInfo == null) {
            return CommonErrorCode.invalidParameter("插件不存在，请刷新页面");
        }
        if(PluginStatusEnum.ONLINE.getStatus().equals(customPluginInfo.getPluginStatus())){
            return CommonErrorCode.invalidParameter("插件已上架，不允许删除");
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public Long pluginImport(CustomPluginInfoDto customPluginInfoDto) {
        CustomPluginInfo customPluginInfo = Trans.customPluginDto2MetaInfo(customPluginInfoDto);
        customPluginInfo.setPluginStatus(PluginStatusEnum.OFFLINE.getStatus());
        customPluginInfoDao.add(customPluginInfo);
        return customPluginInfo.getId();
    }



    @Override
    public Integer pluginUpdate(PluginUpdateDto pluginUpdateDto) {
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(pluginUpdateDto.getId());
        Trans.merge(customPluginInfo, pluginUpdateDto);
        return customPluginInfoDao.update(customPluginInfo);
    }

    @Override
    public ErrorCode updatePluginStatus(UpdatePluginStatusDto updatePluginStatusDto) {
        List<VirtualGatewayDto> allVirtualGateway = virtualGatewayService.findAll().stream().filter(virtualGatewayDto -> VALID_VIRTUAL_GATEWAY.contains(virtualGatewayDto.getType())).collect(Collectors.toList());
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(updatePluginStatusDto.getId());
        customPluginInfo.setPluginStatus(updatePluginStatusDto.getPluginStatus());
        //更新配置
        ErrorCode errorCode = handlePluginStatus(customPluginInfo, allVirtualGateway, PluginStatusEnum.fromType(customPluginInfo.getPluginStatus()));
        if (!CommonErrorCode.SUCCESS.equals(errorCode)){
            return errorCode;
        }
        //更新db数据
        customPluginInfoDao.update(customPluginInfo);
        return CommonErrorCode.SUCCESS;
    }


    private ErrorCode handlePluginStatus(CustomPluginInfo customPluginInfo, List<VirtualGatewayDto> virtualGatewayDtos, PluginStatusEnum pluginStatusEnum) {
        //文件处理
        if (FILE.equals(customPluginInfo.getSourceType())){
            ErrorCode errorCode = handleFile(customPluginInfo, pluginStatusEnum);
            if (!CommonErrorCode.SUCCESS.equals(errorCode)){
                return errorCode;
            }
        }
        //plm item配置更新
        for (VirtualGatewayDto virtualGatewayDto : virtualGatewayDtos) {
            Boolean result = iPluginManagerService.updateCustomPluginStatus(virtualGatewayDto, customPluginInfo);
            if (Boolean.FALSE.equals(result)){
                logger.error("update plugin manager failed, gateway:{}", virtualGatewayDto.getGwClusterName());
                return CommonErrorCode.INTERNAL_SERVER_ERROR;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode handleFile(CustomPluginInfo customPluginInfo, PluginStatusEnum pluginStatusEnum){
        List<? extends GatewayDto> gatewayDtos = iGatewayService.findAll();
        for (GatewayDto gateway : gatewayDtos) {
            String pluginNmae = Trans.getPluginNmae(customPluginInfo.getPluginType(), customPluginInfo.getLanguage());
            Boolean planeResult;
            if (PluginStatusEnum.ONLINE.equals(pluginStatusEnum)){
                planeResult = customPluginRpcService.addPluginCodeFile(pluginNmae, customPluginInfo.getPluginContent(), gateway);
            }else {
                planeResult = customPluginRpcService.deletePluginCodeFile(pluginNmae, gateway);
            }
            if (Boolean.FALSE.equals(planeResult)) {
                return CommonErrorCode.INTERNAL_SERVER_ERROR;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode deletePlugin(Long id) {
        CustomPluginInfo customPluginInfo = CustomPluginInfo.builder().id(id).build();
        customPluginInfoDao.delete(customPluginInfo);
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public DescribeCustomPluginDto describeCustomPluginInfo(Long pluginId) {
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(pluginId);
        return Trans.customPluginInfo2Dto(customPluginInfo);
    }
    @Override
    public Page<DescribeCustomPluginDto> getCustomPluginList(CustomPluginQueryDto queryDto) {
        CustomPluginInfoQuery query = CustomPluginInfoQuery.builder()
                .pluginType(queryDto.getPluginType())
                .pluginCategory(queryDto.getPluginCategory())
                .build();
        query.setLimit(queryDto.getLimit());
        query.setOffset(query.getOffset());
        Page<CustomPluginInfo> customPluginInfoPage = customPluginInfoDao.getCustomPluginInfoPage(query);
        Page<DescribeCustomPluginDto> result = new Page<>();
        result.setTotal(customPluginInfoPage.getTotal());
        result.setRecords(customPluginInfoPage.getRecords().stream().map(Trans::customPluginInfo2Dto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public CustomPluginInfo getCustomPlugin(String pluginType) {
        CustomPluginInfoQuery query = CustomPluginInfoQuery.builder()
                .pluginType(pluginType)
                .build();
        List<CustomPluginInfo> customPluginInfoList = customPluginInfoDao.getCustomPluginInfoList(query);
        return CollectionUtils.isEmpty(customPluginInfoList) ? null : customPluginInfoList.get(0);
    }

    @Override
    public Page<CustomPluginInstanceDto> getCustomPluginInstancePage(CustomPluginInstanceListQueryDto customPluginInstanceListQueryDto) {
        Long pluginId = customPluginInstanceListQueryDto.getPluginId();
        CustomPluginInfo customPluginInfo = customPluginInfoDao.get(pluginId);
        if (customPluginInfo == null) {
            logger.error("pluginId:{} not exist", pluginId);
            return Page.of(0,0);
        }

        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .pluginType(Collections.singletonList(customPluginInfo.getPluginType()))
                .build();
        query.setLimit(customPluginInstanceListQueryDto.getLimit());
        query.setOffset(customPluginInstanceListQueryDto.getOffset());
        Page<PluginBindingInfo> page = pluginBindingInfoDao.getPluginBindingInfoPage(query);
        if (page.getTotal() == 0){
            return Page.of(0,0);
        }
        List<PluginBindingInfo> pluginBindingInfos = page.getRecords();
        List<CustomPluginInstanceDto> customPluginInstanceDtos = pluginBindingInfos.stream()
                .map(pluginInfoService::toView)
                .map(this::buildCustomPluginInstanceDto)
                .collect(Collectors.toList());
        Page<CustomPluginInstanceDto> pageRes = Page.of(page.getCurrent(), page.getTotal());
        pageRes.setRecords(customPluginInstanceDtos);
        return pageRes;

    }

    private CustomPluginInstanceDto buildCustomPluginInstanceDto(PluginBindingDto pluginBindingInfo){
        CustomPluginInstanceDto customPluginInstanceDto = CustomPluginInstanceDto.builder().build();
        customPluginInstanceDto.setId(pluginBindingInfo.getId());
        customPluginInstanceDto.setBindingObjectId(pluginBindingInfo.getBindingObjectId());
        customPluginInstanceDto.setBindingObjectType(pluginBindingInfo.getBindingObjectType());
        customPluginInstanceDto.setBindingObjectName(pluginBindingInfo.getBindingObjectName());
        customPluginInstanceDto.setVirtualGwName(pluginBindingInfo.getVirtualGwName());
        customPluginInstanceDto.setUpdateTime(pluginBindingInfo.getUpdateTime());
        customPluginInstanceDto.setBindingStatus(pluginBindingInfo.getBindingStatus());
        customPluginInstanceDto.setVirtualGwId(pluginBindingInfo.getVirtualGwId());
        //网关级插件不展示项目
        if (!BindingObjectTypeEnum.GATEWAY.getValue().equals(pluginBindingInfo.getBindingObjectType())){
            PermissionScopeDto projectScope = virtualGatewayProjectService.getProjectScope(pluginBindingInfo.getProjectId());
            customPluginInstanceDto.setProject(projectScope ==null ? "-" : projectScope.getPermissionScopeName());
        }
        return customPluginInstanceDto;
    }

}
