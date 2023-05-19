package org.hango.cloud.common.infra.plugin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.mapper.DomainInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.domain.meta.DomainInfo;
import org.hango.cloud.common.infra.domain.service.IDomainInfoService;
import org.hango.cloud.common.infra.plugin.dao.IPluginBindingInfoDao;
import org.hango.cloud.common.infra.plugin.dto.CopyGlobalPluginDto;
import org.hango.cloud.common.infra.plugin.dto.PluginBindingDto;
import org.hango.cloud.common.infra.plugin.dto.PluginTemplateDto;
import org.hango.cloud.common.infra.plugin.meta.BindingPluginDto;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginInfo;
import org.hango.cloud.common.infra.plugin.meta.PluginTemplateInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.plugin.service.IPluginTemplateService;
import org.hango.cloud.common.infra.route.dto.RouteDto;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.route.service.IRouteService;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.meta.BaseConst.SOAP_JSON_TRANSCODER_PLUGIN;

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
    private IPluginTemplateService pluginTemplateService;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IDomainInfoService domainInfoService;

    @Autowired
    private DomainInfoMapper domainInfoMapper;


    @Override
    public PluginBindingDto get(long id) {
        return toView(pluginBindingInfoDao.get(id));
    }

    @Override
    public ErrorCode checkDescribePlugin(long virtualGwId) {
        if (0 < virtualGwId) {
            VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(virtualGwId);
            if (null == virtualGateway) {
                logger.error("插件流程查询指定的网关不存在! virtualGwId:{}", virtualGwId);
                return CommonErrorCode.NO_SUCH_GATEWAY;
            }
        }
        // 获取插件getPluginInfo可以不传gwId，默认值为0，此处返回成功（后面会处理所有网关的场景）
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
        ErrorCode gwErrorCode = checkDescribePlugin(copyGlobalPluginDto.getVirtualGwId());
        if (!gwErrorCode.equals(CommonErrorCode.SUCCESS)) {
            return gwErrorCode;
        }
        // 检查插件是否存在
        PluginBindingDto pluginBindingDto = get(copyGlobalPluginDto.getPluginId());
        if (pluginBindingDto == null) {
            logger.error("校验拷贝全局插件流程，插件不存在! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.NO_SUCH_PLUGIN_BINDING;
        }
        if (!pluginBindingDto.getBindingObjectType().equals(PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL)) {
            logger.error("校验拷贝全局插件流程，指定插件不是全局类型插件! pluginId:{}", copyGlobalPluginDto.getPluginId());
            return CommonErrorCode.ILLEGAL_PLUGIN_TYPE;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public PluginInfo getPluginInfoFromDataPlane(String pluginType) {
        return null;
    }


    @Override
    @SuppressWarnings("java:S3776")
    public ErrorCode checkCreateParam(PluginBindingDto pluginBindingDto) {
        BindingPluginDto bindingPluginDto = BindingPluginDto.createBindingPluginFromDto(pluginBindingDto);
        long projectId = ProjectTraceHolder.getProId();
        long templateId = pluginBindingDto.getTemplateId();
        VirtualGatewayDto virtualGateway = virtualGatewayInfoService.get(bindingPluginDto.getVirtualGwId());
        if (null == virtualGateway) {
            logger.info("绑定插件时指定的网关id不存在！ virtualGwId：{}", bindingPluginDto.getVirtualGwId());
            return CommonErrorCode.NO_SUCH_GATEWAY;
        }
        if (PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingPluginDto.getBindingObjectType())) {
            RouteDto routeDto = routeService.getRoute(bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
            if (null == routeDto) {
                logger.info("路由规则尚未发布到指定网关，不允许绑定插件! virtualGwId:{}, routeRuleId:{}", bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
                return CommonErrorCode.ROUTE_RULE_NOT_PUBLISHED;
            }
        } else if (PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(bindingPluginDto.getBindingObjectType())) {
            pluginBindingDto.setBindingObjectId(String.valueOf(projectId));
            bindingPluginDto.setBindingObjectId(projectId);
            // TODO 若后续改为项目级，则需要增加校验网关与项目的关联关系
            List<String> hosts = domainInfoService.getHosts(bindingPluginDto.getBindingObjectId(), bindingPluginDto.getVirtualGwId());
            if (CollectionUtils.isEmpty(hosts)) {
                logger.info("绑定全局插件时指定的域名不存在! virtualGwId:{}, projectId:{}", bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
                return CommonErrorCode.PROJECT_NOT_ASSOCIATED_GATEWAY;
            }
        }else if (PluginBindingInfo.BINDING_OBJECT_TYPE_HOST.equals(bindingPluginDto.getBindingObjectType())){
            DomainInfo domainInfoPO = domainInfoMapper.selectById(bindingPluginDto.getBindingObjectId());
            if (domainInfoPO == null) {
                logger.info("绑定域名插件时指定的域名不存在! virtualGwId:{}}", bindingPluginDto.getVirtualGwId());
                return CommonErrorCode.invalidParameter("域名不存在");
            }
        } else {
            ServiceProxyDto serviceProxyDto = serviceProxyService.get(bindingPluginDto.getBindingObjectId());
            if (null == serviceProxyDto) {
                logger.info("服务尚未发布到指定网关，不允许绑定插件！ virtualGwId:{}, serviceId:{}", bindingPluginDto.getVirtualGwId(), bindingPluginDto.getBindingObjectId());
            }
        }

        if (StringUtils.isBlank(bindingPluginDto.getPluginType())) {
            logger.info("绑定插件时参数 pluginType 为空!");
            return CommonErrorCode.MissingParameter("PluginType");
        }

        PluginBindingInfo bindingInfo = getBindingInfo(bindingPluginDto);
        if (null != bindingInfo) {
            logger.info("已绑定该插件，不允许重复绑定");
            return CommonErrorCode.CANNOT_DUPLICATE_BINDING;
        }
        if (0 < templateId) {
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(templateId);
            if (null == pluginTemplateDto) {
                logger.info("指定插件模板不存在! templateId:{}", templateId);
                return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
            }
            if (!bindingPluginDto.getPluginType().equals(pluginTemplateDto.getPluginType())) {
                logger.info("插件模板与插件类型不匹配! pluginType:{}, templatePluginType:{}", bindingPluginDto.getPluginType(), pluginTemplateDto.getPluginType());
                return CommonErrorCode.NO_SUCH_PLUGIN_TEMPLATE;
            }
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public PluginBindingInfo getBindingInfo(BindingPluginDto bindingPluginDto) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, bindingPluginDto.getVirtualGwId());
        params.put(BaseConst.BINDING_OBJECT_ID, bindingPluginDto.getBindingObjectId());
        params.put("bindingObjectType", bindingPluginDto.getBindingObjectType());
        params.put("pluginType", bindingPluginDto.getPluginType());
        List<PluginBindingInfo> pluginBindingInfoList = ((List) pluginBindingInfoDao.getRecordsByField(params));
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? null : pluginBindingInfoList.get(0);
    }

    @Override
    public List<String> getInnerPlugins() {
        List<String> innerPluginList = new ArrayList<>();
        // 内部使用插件，不对外暴露，新增内部插件继续添加
        innerPluginList.add(SOAP_JSON_TRANSCODER_PLUGIN);
        return innerPluginList;
    }

    @Override
    public List<PluginBindingInfo> getBindingInfoList(PluginBindingDto bindingPluginDto) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, bindingPluginDto.getVirtualGwId());
        params.put(BaseConst.BINDING_OBJECT_ID, bindingPluginDto.getBindingObjectId());
        params.put(BaseConst.BINDING_OBJECT_TYPE, bindingPluginDto.getBindingObjectType());
        return pluginBindingInfoDao.getRecordsByField(params);
    }

    @Override
    public List<PluginBindingInfo> getBindingInfoList(BindingPluginDto bindingPluginDto) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, bindingPluginDto.getVirtualGwId());
        params.put(BaseConst.BINDING_OBJECT_ID, bindingPluginDto.getBindingObjectId());
        params.put(BaseConst.BINDING_OBJECT_TYPE, bindingPluginDto.getBindingObjectType());
        return pluginBindingInfoDao.getRecordsByField(params);
    }

    @Override
    public boolean copyGlobalPluginToGatewayByVirtualGwId(CopyGlobalPluginDto copyGlobalPlugin) {
        // 拷贝全局插件的目标网关ID
        Long virtualGwId = copyGlobalPlugin.getVirtualGwId();
        // 拷贝全局插件的源插件ID
        Long pluginId = copyGlobalPlugin.getPluginId();
        PluginBindingInfo pluginBindingInfo = pluginBindingInfoDao.get(pluginId);
        pluginBindingInfo.setVirtualGwId(virtualGwId);
        pluginBindingInfo.setCreateTime(System.currentTimeMillis());
        pluginBindingInfo.setUpdateTime(System.currentTimeMillis());

        // 查询目标网关下相同类型的全局插件（项目级）
        BindingPluginDto bindingPlugin = BindingPluginDto.createBindingPluginFromPluginBindingInfo(pluginBindingInfo);
        bindingPlugin.setBindingObjectType(BaseConst.PLUGIN_TYPE_GLOBAL);
        bindingPlugin.setVirtualGwId(virtualGwId);
        List<PluginBindingInfo> sameTypePlugins = getPluginBindingListByVirtualGwIdAndTypeAndProjectId(bindingPlugin, copyGlobalPlugin.getProjectId());

        logger.info("[copyGlobalPlugin] bindingPlugin:{},virtualGwId:{}", bindingPlugin, virtualGwId);

        if (CollectionUtils.isEmpty(sameTypePlugins)) {
            // 网关没有相同类型的全局插件（项目级）
            long newGlobalPluginId = pluginBindingInfoDao.add(pluginBindingInfo);
            pluginBindingInfo.setId(newGlobalPluginId);
        } else {
            // 网关下有相同类型的全局插件（项目级）
            // 全局插件同一种类型只能有一个存在，因此取第一个元素
            PluginBindingInfo oldPlugin = sameTypePlugins.get(0);
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
                pluginBindingInfo.setBindingStatus(PluginBindingInfo.BINDING_STATUS_ENABLE);
            } else {
                pluginBindingInfo.setBindingStatus(PluginBindingInfo.BINDING_STATUS_DISABLE);
            }
        }
    }

    @Override
    public List<PluginBindingInfo> getPluginBindingListByVirtualGwIdAndTypeAndProjectId(BindingPluginDto bindingPlugin, Long projectId) {
        Map<String, Object> params = new HashMap<>(4);
        params.put(BaseConst.VIRTUAL_GW_ID, bindingPlugin.getVirtualGwId());
        params.put("pluginType", bindingPlugin.getPluginType());
        params.put(BaseConst.BINDING_OBJECT_TYPE, bindingPlugin.getBindingObjectType());
        params.put("projectId", projectId);
        return pluginBindingInfoDao.getRecordsByField(params);
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
        PluginBindingInfo bindingInfo = new PluginBindingInfo();
        bindingInfo.setProjectId(ProjectTraceHolder.getProId());
        bindingInfo.setVirtualGwId(pluginBindingDto.getVirtualGwId());
        VirtualGatewayDto virtualGatewayDto = virtualGatewayInfoService.get(pluginBindingDto.getVirtualGwId());
        bindingInfo.setGwType(virtualGatewayDto.getGwType());
        bindingInfo.setUpdateTime(System.currentTimeMillis());
        bindingInfo.setCreateTime(System.currentTimeMillis());
        bindingInfo.setPluginConfiguration(pluginBindingDto.getPluginConfiguration());
        if (0 < pluginBindingDto.getTemplateId()) {
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(pluginBindingDto.getTemplateId());
            if (null == pluginTemplateDto) {
                return BaseConst.ERROR_RESULT;
            }
            bindingInfo.setTemplateId(pluginTemplateDto.getId());
            bindingInfo.setTemplateVersion(pluginTemplateDto.getTemplateVersion());
            bindingInfo.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
        }
        bindingInfo.setBindingObjectType(pluginBindingDto.getBindingObjectType());
        bindingInfo.setBindingObjectId(String.valueOf(pluginBindingDto.getBindingObjectId()));
        bindingInfo.setPluginType(pluginBindingDto.getPluginType());
        bindingInfo.setPluginName(pluginBindingDto.getPluginName());
        bindingInfo.setBindingStatus(PluginBindingInfo.BINDING_STATUS_ENABLE);
        return pluginBindingInfoDao.add(bindingInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long update(PluginBindingDto pluginBindingDto) {
        pluginBindingDto.setUpdateTime(System.currentTimeMillis());
        pluginBindingDto.setUpdateTime(System.currentTimeMillis());
        if (0 < pluginBindingDto.getTemplateId()) {
            PluginTemplateDto pluginTemplateDto = pluginTemplateService.get(pluginBindingDto.getTemplateId());
            pluginBindingDto.setPluginConfiguration(pluginTemplateDto.getPluginConfiguration());
            pluginBindingDto.setTemplateId(pluginTemplateDto.getId());
            if (pluginBindingDto.getTemplateVersion() > 0) {
                pluginBindingDto.setTemplateVersion(pluginTemplateDto.getTemplateVersion());
            } else {
                pluginBindingDto.setTemplateVersion(pluginTemplateDto.getTemplateVersion());
            }
        } else {
            pluginBindingDto.setTemplateId(0);
            pluginBindingDto.setTemplateVersion(0);
        }
        return pluginBindingInfoDao.update(toMeta(pluginBindingDto));

    }

    @Override
    public void delete(PluginBindingDto bindingInfo) {
        pluginBindingInfoDao.delete(toMeta(bindingInfo));
    }

    @Override
    public List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId, String bindingObjectId, String bindingObjectType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        params.put(BaseConst.BINDING_OBJECT_ID, bindingObjectId);
        params.put(BaseConst.BINDING_OBJECT_TYPE, bindingObjectType);
        params.put("bindingStatus", PluginBindingInfo.BINDING_STATUS_ENABLE);
        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList;
    }

    @Override
    public List<PluginBindingInfo> getEnablePluginBindingList(long virtualGwId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        params.put("bindingStatus", PluginBindingInfo.BINDING_STATUS_ENABLE);
        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList;
    }

    @Override
    public List<PluginBindingDto> getPluginBindingList(long virtualGwId, String bindingObjectId, String bindingObjectType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(BaseConst.VIRTUAL_GW_ID, virtualGwId);
        params.put(BaseConst.BINDING_OBJECT_ID, bindingObjectId);
        params.put(BaseConst.BINDING_OBJECT_TYPE, bindingObjectType);
        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getRecordsByField(params);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Lists.newArrayList() : pluginBindingInfoList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public ErrorCode checkUpdateParam(PluginBindingDto pluginBindingDto) {
        if (0 < pluginBindingDto.getTemplateId()) {
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
    public long getBindingPluginCount(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, List<String> excludedPluginTypeList) {
        return pluginBindingInfoDao.getBindingPluginCount(projectId, virtualGwId, bindingObjectId, bindingObjectType, pattern, excludedPluginTypeList);
    }

    @Override
    public long getBindingPluginCountExcludedInnerPlugins(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern) {
        // 查询数量需去除不对外展示的内部插件
        List<String> excludedPluginTypeList = getInnerPlugins();
        return getBindingPluginCount(virtualGwId, projectId, bindingObjectId, bindingObjectType, pattern, excludedPluginTypeList);
    }

    @Override
    public List<PluginBindingDto> getBindingPluginListOutSide(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue) {
        List<PluginBindingDto> bindingPluginList = getBindingPluginList(virtualGwId, projectId, bindingObjectId, bindingObjectType, pattern, offset, limit, sortKey, sortValue);
        if (CollectionUtils.isEmpty(bindingPluginList)) {
            return bindingPluginList;
        }
        // 查询插件列表，排除内部不对外展示的插件
        List<PluginBindingDto> pluginBindingDtoList = bindingPluginList.stream().filter(item -> !isInnerPlugin(item)).collect(Collectors.toList());
        fillDtoFiled(pluginBindingDtoList);
        return pluginBindingDtoList;
    }

    @Override
    public List<PluginBindingDto> getBindingPluginList(long virtualGwId, long projectId, String bindingObjectId, String bindingObjectType, String pattern, long offset, long limit, String sortKey, String sortValue) {
        List<PluginBindingInfo> pluginBindingInfoList = pluginBindingInfoDao.getBindingPluginList(projectId, virtualGwId, bindingObjectId, bindingObjectType, pattern, offset, limit, sortKey, sortValue);
        return CollectionUtils.isEmpty(pluginBindingInfoList) ? Collections.emptyList() : pluginBindingInfoList.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public long deletePluginList(long virtualGwId, String bindingObjectId, String bindingObjectType) {
        return pluginBindingInfoDao.batchDeleteBindingInfo(getPluginBindingList(virtualGwId, bindingObjectId, bindingObjectType));
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


    private void fillDtoFiled(List<PluginBindingDto> pluginBindingDtoList) {
        if (CollectionUtils.isEmpty(pluginBindingDtoList)) {
            return;
        }

        Set<Long> templateIdSet = pluginBindingDtoList.stream().map(PluginBindingDto::getTemplateId).collect(Collectors.toSet());
        Set<Long> vgIdSet = pluginBindingDtoList.stream().map(PluginBindingDto::getVirtualGwId).collect(Collectors.toSet());
        Set<Long> serviceIdSet = pluginBindingDtoList.stream().filter(item -> PluginBindingInfo.BINDING_OBJECT_TYPE_SERVICE.equals(item.getBindingObjectType())).map(item -> Long.valueOf(item.getBindingObjectId())).collect(Collectors.toSet());
        Set<Long> routeRuleIdSet = pluginBindingDtoList.stream().filter(item -> PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(item.getBindingObjectType())).map(item -> Long.valueOf(item.getBindingObjectId())).collect(Collectors.toSet());
        Set<Long> domainIdSet = pluginBindingDtoList.stream().filter(item -> PluginBindingInfo.BINDING_OBJECT_TYPE_HOST.equals(item.getBindingObjectType())).map(item -> Long.valueOf(item.getBindingObjectId())).collect(Collectors.toSet());
        Map<Long, VirtualGatewayDto> gatewayInfoMap =virtualGatewayInfoService.getByIds(Lists.newArrayList(vgIdSet)).stream().collect(Collectors.toMap(VirtualGatewayDto::getId, item -> item));
        //Map<Long, ServiceDto> serviceInfoMap = serviceInfoService.getServiceDtoList(Lists.newArrayList(serviceIdSet)).stream().collect(Collectors.toMap(ServiceDto::getId, item -> item));
        RouteQuery query = RouteQuery.builder().routeIds(Lists.newArrayList(routeRuleIdSet)).build();
        Map<Long, RouteDto> routeRuleInfoMap = routeService.getRouteList(query).stream().collect(Collectors.toMap(RouteDto::getId, item -> item));
        Map<Long, PluginTemplateDto> templateInfoMap = pluginTemplateService.batchGet(Lists.newArrayList(templateIdSet)).stream().collect(Collectors.toMap(PluginTemplateDto::getId, item -> item));
        Map<Long, DomainInfo> domainInfoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(domainIdSet)){
            domainInfoMap = domainInfoMapper.selectBatchIds(domainIdSet).stream().collect(Collectors.toMap(DomainInfo::getId, item -> item));
        }
        for (PluginBindingDto item : pluginBindingDtoList) {
            //// TODO: 2023/3/22 插件依赖服务元数据需要改造， 目前先暂时注释代码，参数传null
            fillDtoFiiled(gatewayInfoMap, null, routeRuleInfoMap, templateInfoMap, domainInfoMap, item);

        }
    }

    private static void fillDtoFiiled(Map<Long, VirtualGatewayDto> gatewayInfoMap, Map<Long, ServiceProxyDto> serviceInfoMap, Map<Long, RouteDto> routeRuleInfoMap, Map<Long, PluginTemplateDto> templateInfoMap, Map<Long, DomainInfo> domainMap, PluginBindingDto item) {
        VirtualGatewayDto virtualGateway = gatewayInfoMap.get(item.getVirtualGwId());
        item.setGwName(null == virtualGateway ? StringUtils.EMPTY : virtualGateway.getName());
        if (PluginBindingInfo.BINDING_OBJECT_TYPE_ROUTE_RULE.equals(item.getBindingObjectType())) {
            RouteDto routeDto = routeRuleInfoMap.get(Long.valueOf(item.getBindingObjectId()));
            item.setBindingObjectName(null == routeDto ? StringUtils.EMPTY : routeDto.getName());
        } else if (PluginBindingInfo.BINDING_OBJECT_TYPE_GLOBAL.equals(item.getBindingObjectType())) {
            item.setBindingObjectName(null == virtualGateway ? StringUtils.EMPTY : virtualGateway.getName());
        } else if (PluginBindingInfo.BINDING_OBJECT_TYPE_HOST.equals(item.getBindingObjectType())) {
            DomainInfo domainInfoPO = domainMap.get(Long.valueOf(item.getBindingObjectId()));
            item.setBindingObjectName(null == domainInfoPO ? StringUtils.EMPTY : domainInfoPO.getHost());
        }
        //// TODO: 2023/3/22 插件依赖服务元数据需要改造， 目前先暂时注释代码，并将入参中的ServiceDto 改为 ServiceProxyDto
        /*else {
            ServiceDto serviceInfo = serviceInfoMap.get(Long.valueOf(item.getBindingObjectId()));
            item.setBindingObjectName(null == serviceInfo ? StringUtils.EMPTY : serviceInfo.getDisplayName());
        }*/
        PluginTemplateDto pluginTemplateDto = templateInfoMap.get(item.getTemplateId());
        if (0 == item.getTemplateId() || null == pluginTemplateDto || item.getTemplateVersion() == pluginTemplateDto.getTemplateVersion()) {
            item.setTemplateStatus(PluginTemplateInfo.STATUS_NO_NEED_SYNC);
        } else {
            item.setTemplateStatus(PluginTemplateInfo.STATUS_NEED_SYNC);
        }
    }

    @Override
    public List<PluginBindingDto> getBindingListByTemplateId(long templateId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("templateId", templateId);
        List<PluginBindingInfo> pluginBindingInfos = pluginBindingInfoDao.getRecordsByField(params);
        if (CollectionUtils.isEmpty(pluginBindingInfos)) {
            return Collections.emptyList();
        }
        List<PluginBindingDto> pluginBindingDtoList = pluginBindingInfos.stream().map(this::toView).collect(Collectors.toList());
        fillDtoFiled(pluginBindingDtoList);
        return pluginBindingDtoList;
    }

    @Override
    public boolean batchDissociateTemplate(List<Long> bindingInfoList) {
        pluginBindingInfoDao.batchDissociateTemplate(bindingInfoList);
        return true;
    }

    @Override
    public List<PluginBindingDto> batchGetById(List<Long> bindingInfoIdList) {
        List<PluginBindingInfo> pluginBindingInfos = pluginBindingInfoDao.batchGetById(bindingInfoIdList);
        if (CollectionUtils.isEmpty(pluginBindingInfos)) {
            return Collections.emptyList();
        }
        return pluginBindingInfos.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public boolean isInnerPlugin(PluginBindingDto pluginBindingDto) {
        for (String innerPlugin : getInnerPlugins()) {
            if (innerPlugin.equals(pluginBindingDto.getPluginType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PluginBindingDto toView(PluginBindingInfo pluginBindingInfo) {
        if (pluginBindingInfo == null){
            return null;
        }
        PluginBindingDto pluginBindingDto = new PluginBindingDto();
        BeanUtils.copyProperties(pluginBindingInfo, pluginBindingDto);
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
