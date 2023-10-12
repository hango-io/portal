package org.hango.cloud.common.infra.plugin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.convert.PluginInfoConvertService;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingQueryDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.enums.AuthPluginTypeEnum;
import org.hango.cloud.common.infra.plugin.enums.BindingObjectTypeEnum;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfoQuery;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.*;

/**
 * @author xin li
 * @date 2022/9/7 18:23
 */

/**
 * 插件Service层实现类
 *
 * @author hzchenzhongyang 2019-10-23
 */
@Service
public class PluginServiceInfoImpl implements IPluginInfoService {
    private static final Logger logger = LoggerFactory.getLogger(PluginServiceInfoImpl.class);

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;
    @Autowired
    private IRouteService routeService;
    @Autowired
    private IPluginBindingInfoDao pluginBindingInfoDao;

    @Autowired
    private PluginInfoConvertService pluginInfoConvertService;

    @Autowired
    private IPluginTemplateService pluginTemplateService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private DomainInfoMapper domainInfoMapper;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;


    @Override
    public PluginBindingDto get(long id) {
        return toView(pluginBindingInfoDao.get(id));
    }

    @Override
    public ErrorCode checkDescribePlugin(long virtualGwId) {
        return checkDescribePlugin(virtualGwId, false);
    }

    @Override
    public ErrorCode checkDescribePlugin(long virtualGwId, boolean copyPlugin) {
        if (virtualGwId <= 0) {
            // 获取插件getPluginInfo可以不传gwId，默认值为0，此处返回成功（后面会处理所有网关的场景）
            return CommonErrorCode.SUCCESS;
        }
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
        if (null == virtualGateway) {
            logger.error("插件流程查询指定的网关不存在! virtualGwId:{}", virtualGwId);
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        if (CollectionUtils.isEmpty(virtualGateway.getDomainInfos())){
            if (copyPlugin) {
                logger.error("复制插件时，目前网关未绑定域名，不允许复制, vgId:{}", virtualGwId);
                return CommonErrorCode.DESTINATION_GW_NOT_ASSOCIATED_DOMAIN;
            } else {
                logger.error("绑定插件时，目前网关未绑定域名，不允许复制, vgId:{}", virtualGwId);
                return CommonErrorCode.CURRENT_GW_NOT_ASSOCIATED_DOMAIN;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkUnbindParam(long pluginBindingInfoId) {
        PluginBindingInfo pluginBindingInfo = pluginBindingInfoDao.get(pluginBindingInfoId);
        if (pluginBindingInfo == null) {
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkCopyGlobalPluginToGateway(CopyGlobalPluginDto copyGlobalPluginDto) {
        // 检查目标网关是否存在
        ErrorCode gwErrorCode = checkDescribePlugin(copyGlobalPluginDto.getVirtualGwId(), true);
        if (!gwErrorCode.equals(CommonErrorCode.SUCCESS)) {
            return gwErrorCode;
        }
        // 检查插件是否存在
        PluginBindingDto pluginBindingDto = get(copyGlobalPluginDto.getPluginId());
        if (pluginBindingDto == null) {
            logger.error("校验拷贝全局插件流程，插件不存在! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        if (!pluginBindingDto.getBindingObjectType().equals(BindingObjectTypeEnum.GLOBAL.getValue())) {
            logger.error("校验拷贝全局插件流程，指定插件不是全局类型插件! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.ILLEGAL_PLUGIN_TYPE;
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    @SuppressWarnings("java:S3776")
    public ErrorCode checkCreateParam(PluginBindingDto pluginBindingDto) {
        long projectId = ProjectTraceHolder.getProId();
        pluginBindingDto.setProjectId(projectId);

        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(pluginBindingDto.getVirtualGwId());
        if (null == virtualGateway) {
            logger.info("绑定插件时指定的网关id不存在！ virtualGwId：{}", pluginBindingDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        //校验绑定类型
        ErrorCode errorCode = checkBindingObjectType(pluginBindingDto);
        if (!errorCode.equals(CommonErrorCode.SUCCESS)) {
            return errorCode;
        }

        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .virtualGwId(pluginBindingDto.getVirtualGwId())
                .pluginType(Collections.singletonList(pluginBindingDto.getPluginType()))
                .bindingObjectId(String.valueOf(pluginBindingDto.getBindingObjectId()))
                .bindingObjectType(pluginBindingDto.getBindingObjectType())
                .build();
        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getPluginBindingInfoList(query);
        if (!CollectionUtils.isEmpty(pluginBindingInfoList)) {
            logger.info("已绑定该插件，不允许重复绑定");
            return CommonErrorCode.CANNOT_DUPLICATE_BINDING;
        }

        if (AuthPluginTypeEnum.isAuthPlugin(pluginBindingDto.getPluginType())) {
            query.setPluginType(AuthPluginTypeEnum.getPluginTypeList());
            List<PluginBindingInfo> authPluginList = pluginBindingInfoDao.getPluginBindingInfoList(query);
            if (!CollectionUtils.isEmpty(authPluginList)) {
                logger.info("认证类型插件只能绑定一种，不能重复绑定");
                return CommonErrorCode.CANNOT_DUPLICATE_BINDING_AUTH_PLUGIN;
            }
        }
        Long templateId = pluginBindingDto.getTemplateId();
        if (templateId != null && 0 < templateId) {
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(templateId);
            if (null == pluginTemplateDto) {
                logger.info("指定插件模板不存在! templateId:{}", templateId);
                return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
            }
            if (!pluginBindingDto.getPluginType().equals(pluginTemplateDto.getPluginType())) {
                logger.info("插件模板与插件类型不匹配! pluginType:{}, templatePluginType:{}", pluginBindingDto.getPluginType(), pluginTemplateDto.getPluginType());
                return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    private ErrorCode checkBindingObjectType(PluginBindingDto pluginBindingDto){
        String bindingObjectType = pluginBindingDto.getBindingObjectType();
        long bindingObjectId = Long.parseLong(pluginBindingDto.getBindingObjectId());
        Long virtualGwId = pluginBindingDto.getVirtualGwId();
        BindingObjectTypeEnum bindingObjectTypeEnum = BindingObjectTypeEnum.getByValue(bindingObjectType);
        if (bindingObjectTypeEnum == null) {
            logger.info("绑定插件时指定的绑定对象类型不存在！ bindingObjectType:{}", bindingObjectType);
            return CommonErrorCode.invalidParameter("无效的插件绑定对象类型");
        }
        switch (bindingObjectTypeEnum){
            case ROUTE:
                RouteDto routeDto = routeService.getRoute(virtualGwId, bindingObjectId);
                if (null == routeDto) {
                    logger.info("路由规则尚未发布到指定网关，不允许绑定插件! virtualGwId:{}, routeRuleId:{}", virtualGwId, bindingObjectId);
                    return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
                }
                break;
            case GLOBAL:
                // TODO 若后续改为项目级，则需要增加校验网关与项目的关联关系
                List<String> hosts = domainInfoService.getHosts(bindingObjectId, virtualGwId);
                if (CollectionUtils.isEmpty(hosts)) {
                    logger.info("绑定全局插件时指定的域名不存在! virtualGwId:{}, projectId:{}", virtualGwId, bindingObjectId);
                    return CommonErrorCode.CURRENT_GW_NOT_ASSOCIATED_DOMAIN;
                }
                break;
            case HOST:
                DomainInfo domainInfoPO = domainInfoMapper.selectById(bindingObjectId);
                if (domainInfoPO == null) {
                    logger.info("绑定域名插件时指定的域名不存在! virtualGwId:{}}", virtualGwId);
                    return CommonErrorCode.invalidParameter("域名不存在");
                }
                break;
            case GATEWAY:
                VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(bindingObjectId);
                if (null == virtualGatewayDto) {
                    logger.info("绑定插件时指定的网关不存在！ virtualGwId：{}", virtualGwId);
                    return CommonErrorCode.NO_SUCH_GATEWAY;
                }
                Long templateId = pluginBindingDto.getTemplateId();
                if (templateId != null && templateId > 0){
                    logger.info("网关级插件不允许绑定插件模板! pluginBindingDto:{}", JSONObject.toJSONString(pluginBindingDto));
                    return CommonErrorCode.invalidParameter("网关级插件不允许绑定模板");
                }
                break;
            default:
                return CommonErrorCode.invalidParameter("无效的插件绑定对象类型");
        }
        return CommonErrorCode.SUCCESS;
    }


    @Override
    public boolean copyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPlugin) {
        // 拷贝全局插件的目标网关ID
        Long virtualGwId = copyGlobalPlugin.getVirtualGwId();
        // 拷贝全局插件的源插件ID
        Long pluginId = copyGlobalPlugin.getPluginId();
        PluginBindingInfo pluginBindingInfo = pluginBindingInfoDao.get(pluginId);
        pluginBindingInfo.setVirtualGwId(virtualGwId);

        // 查询目标网关下相同类型的全局插件（项目级）
        BindingPluginDto bindingPlugin = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginBindingInfo);
        bindingPlugin.setBindingObjectType(BindingObjectTypeEnum.GLOBAL.getValue());
        bindingPlugin.setVirtualGwId(virtualGwId);
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .virtualGwId(bindingPlugin.getVirtualGwId())
                .pluginType(Collections.singletonList(bindingPlugin.getPluginType()))
                .bindingObjectType(bindingPlugin.getBindingObjectType())
                .projectId(copyGlobalPlugin.getProjectId())
                .build();
        List<PluginBindingDto> sameTypePlugins = getBindingPluginInfoList(query);
        logger.info("[copyGlobalPlugin] bindingPlugin:{},virtualGwId:{}", bindingPlugin, virtualGwId);
        if (CollectionUtils.isEmpty(sameTypePlugins)) {
            // 网关没有相同类型的全局插件（项目级）
            pluginBindingInfo.setId(null);
            pluginBindingInfoDao.add(pluginBindingInfo);
        } else {
            // 网关下有相同类型的全局插件（项目级）
            // 全局插件同一种类型只能有一个存在，因此取第一个元素
            PluginBindingDto oldPlugin = sameTypePlugins.get(0);
            pluginBindingInfo.setId(oldPlugin.getId());
            // 根据插件状态的策略设置新插件的启用状态
            prepareForBindingStatus(copyGlobalPlugin, pluginBindingInfo);
            pluginBindingInfoDao.update(pluginBindingInfo);
        }
        return true;
    }

    /**
     * 配置插件的启用决策，根据前台策IsEnable决定
     *
     * @param copyGlobalPlugin  前台输入的拷贝插件流程对象（前台期望状态）
     * @param pluginBindingInfo 新插件配置对象（插件启用状态）
     */
    private void prepareForBindingStatus(CopyGlobalPluginDto copyGlobalPlugin, PluginBindingInfo pluginBindingInfo) {
        // enable为空代表前端没有传值，标识按照默认策略（源插件状态）；否则设置成前台期望状态
        if (copyGlobalPlugin.getIsEnable() != null) {
            if (copyGlobalPlugin.getIsEnable()) {
                pluginBindingInfo.setBindingStatus(ENABLE_STATE);
            } else {
                pluginBindingInfo.setBindingStatus(DISABLE_STATE);
            }
        }
    }


    @Override
    public ErrorCode checkDescribeBindingPlugins(String bindingObjectId, String bindingObjectType) {
        if (StringUtils.isNotBlank(bindingObjectId) && StringUtils.isBlank(bindingObjectType)) {
            logger.info("查询绑定插件列表时，参数BindingObjectType为空! bindingObjectId:{}", bindingObjectId);
            return CommonErrorCode.MissingParameter(BaseConst.BINDING_OBJECT_TYPE);
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long create(PluginBindingDto pluginBindingDto) {
        PluginBindingInfo pluginBindingInfo = pluginInfoConvertService.trans(pluginBindingDto);
        int id = pluginBindingInfoDao.add(pluginBindingInfo);
        pluginBindingDto.setId(pluginBindingInfo.getId());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long update(PluginBindingDto pluginBindingDto) {
        pluginInfoConvertService.fillPluginInfo(pluginBindingDto);
        return pluginBindingInfoDao.update(toMeta(pluginBindingDto));

    }

    @Override
    public void delete(PluginBindingDto bindingInfo) {
        pluginBindingInfoDao.delete(toMeta(bindingInfo));
    }

    @Override
    public List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId, Long bindingObjectId, String bindingObjectType) {
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .virtualGwId(virtualGwId)
                .bindingObjectId(String.valueOf(bindingObjectId))
                .bindingObjectType(bindingObjectType)
                .bindingStatus(ENABLE_STATE)
                .build();

        return pluginBindingInfoDao.getPluginBindingInfoList(query);
    }

    @Override
    public List<PluginBindingDto> getPluginBindingList(long virtualGwId, Long bindingObjectId, String bindingObjectType) {
        PluginBindingInfoQuery query = PluginBindingInfoQuery.builder()
                .virtualGwId(virtualGwId)
                .bindingObjectId(String.valueOf(bindingObjectId))
                .bindingObjectType(bindingObjectType)
                .build();

        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getPluginBindingInfoList(query);
        if (CollectionUtils.isEmpty(pluginBindingInfoList)) {
            return Collections.emptyList();
        }
        return pluginBindingInfoList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkUpdateParam(PluginBindingDto pluginBindingDto) {
        if (pluginBindingDto.getTemplateId() != null && 0 < pluginBindingDto.getTemplateId()) {
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(pluginBindingDto.getTemplateId());
            if (null == pluginTemplateDto) {
                logger.info("更新插件配置时，指定的模板不存在! templateId:{}", pluginBindingDto.getTemplateId());
                return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
            }
        } else {
            if (StringUtils.isBlank(pluginBindingDto.getPluginConfiguration())) {
                logger.info("更新插件配置时，参数PluginConfiguration缺失!");
                return CommonErrorCode.MissingParameter("PluginConfiguration");
            }
        }
        PluginBindingDto pluginBindingInfo = get(pluginBindingDto.getId());
        if (null == pluginBindingInfo) {
            logger.info("更新插件配置时，指定的绑定关系不存在! pluginBindingInfoId:{}", pluginBindingDto.getId());
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public Page<PluginBindingInfo> getOutsideBindingPluginPage(PluginBindingQueryDto queryDto) {
        PluginBindingInfoQuery query = PluginInfoConvertService.trans(queryDto);
        // 内部使用插件，不对外暴露，新增内部插件继续添加;
        query.setExcludedPluginType(Collections.singletonList(SOAP_JSON_TRANSCODER_PLUGIN));
        return pluginBindingInfoDao.getPluginBindingInfoPage(query);
    }



    @Override
    public ErrorCode checkUpdatePluginBindingStatus(long pluginBindingInfoId, String bindingStatus) {
        PluginBindingDto pluginBindingDto = get(pluginBindingInfoId);
        if (null == pluginBindingDto) {
            logger.error("修改插件绑定关系状态时，指定插件绑定关系不存在! pluginBindinginfoId:{}", pluginBindingInfoId);
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        return CommonErrorCode.SUCCESS;
    }

    private void fillPluginBindingInfo(PluginBindingDto pluginBindingDto) {
        if(pluginBindingDto == null){
            return;
        }
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginBindingDto.getVirtualGwId());
        if (virtualGatewayDto != null){
            pluginBindingDto.setVirtualGwName(virtualGatewayDto.getName());
            pluginBindingDto.setVirtualGwType(virtualGatewayDto.getType());
        }
        BindingObjectTypeEnum bindingObjectTypeEnum = BindingObjectTypeEnum.getByValue(pluginBindingDto.getBindingObjectType());
        if (bindingObjectTypeEnum == null) {
            return;
        }
        Long virtualGwId = pluginBindingDto.getVirtualGwId();
        Long bindingObjectId = Long.valueOf(pluginBindingDto.getBindingObjectId());
        switch (bindingObjectTypeEnum){
            case ROUTE:
                RouteDto routeDto = routeService.getRoute(virtualGwId, bindingObjectId);
                pluginBindingDto.setBindingObjectName(routeDto == null ? StringUtils.EMPTY : routeDto.getName());
                break;
            case GLOBAL:
                PermissionScopeDto projectScope = virtualGatewayProjectService.getProjectScope(pluginBindingDto.getProjectId());
                if (projectScope != null){
                    pluginBindingDto.setBindingObjectName(projectScope.getPermissionScopeEnName());
                }
                break;
            case GATEWAY:
                pluginBindingDto.setBindingObjectName(virtualGatewayDto == null ? StringUtils.EMPTY : virtualGatewayDto.getName());
                break;
            case HOST:
                DomainInfo domainInfoPO = domainInfoMapper.selectById(bindingObjectId);
                pluginBindingDto.setBindingObjectName(domainInfoPO == null ? StringUtils.EMPTY : domainInfoPO.getHost());
                break;
            default:
                break;
        }
    }

    @Override
    public List<PluginBindingDto> getBindingPluginInfoList(PluginBindingInfoQuery query) {
        List<PluginBindingInfo> pluginBindingInfos = pluginBindingInfoDao.getPluginBindingInfoList(query);
        return pluginBindingInfos.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public PluginBindingDto toView(PluginBindingInfo pluginBindingInfo) {
        if (pluginBindingInfo == null){
            return null;
        }
        PluginBindingDto pluginBindingDto = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingInfo, pluginBindingDto);
        fillPluginBindingInfo(pluginBindingDto);
        return pluginBindingDto;
    }

    @Override
    public PluginBindingInfo toMeta(PluginBindingDto pluginBindingDto) {
        if (pluginBindingDto == null){
            return null;
        }
        PluginBindingInfo pluginBindingInfo = new PluginBindingInfo();
        BeanUtils.copyProperties(pluginBindingDto, pluginBindingInfo);
        return pluginBindingInfo;
    }
}
