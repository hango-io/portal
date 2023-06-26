package org.hango.cloud.envoy.infra.base.task;

/**
 * @Author zhufengwei
 * @Date 2023/1/16
 */

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.hango.cloud.common.infra.base.dto.DataCorrectResultDTO;
import org.hango.cloud.common.infra.base.dto.ResourceCheckResultDTO;
import org.hango.cloud.common.infra.base.dto.ResourceDTO;
import org.hango.cloud.common.infra.base.exception.DistributedLockTimeOutException;
import org.hango.cloud.common.infra.base.meta.HttpClientResponse;
import org.hango.cloud.common.infra.base.meta.ResourceEnum;
import org.hango.cloud.common.infra.base.util.HttpClientUtil;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo;
import org.hango.cloud.common.infra.plugin.service.IPluginInfoService;
import org.hango.cloud.common.infra.route.dao.IRouteDao;
import org.hango.cloud.common.infra.route.pojo.RoutePO;
import org.hango.cloud.common.infra.route.pojo.RouteQuery;
import org.hango.cloud.common.infra.serviceproxy.convert.ServiceProxyConvert;
import org.hango.cloud.common.infra.serviceproxy.dto.ServiceProxyDto;
import org.hango.cloud.common.infra.serviceproxy.meta.ServiceProxyQuery;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.hango.cloud.common.infra.virtualgateway.dto.PermissionScopeDto;
import org.hango.cloud.common.infra.virtualgateway.dto.VirtualGatewayDto;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayInfoService;
import org.hango.cloud.common.infra.virtualgateway.service.IVirtualGatewayProjectService;
import org.hango.cloud.envoy.infra.base.meta.EnvoyConst;
import org.hango.cloud.envoy.infra.base.service.MultiClusterApiPlaneService;
import org.hango.cloud.envoy.infra.serviceproxy.service.IEnvoyServiceProxyService;
import org.hango.cloud.gdashboard.api.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hango.cloud.common.infra.plugin.meta.PluginBindingInfo.*;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/31 21:22
 **/
@Slf4j
@Component
public class MultiClusterTask {

    private static final Logger logger = LoggerFactory.getLogger(MultiClusterTask.class);

    @Autowired
    private MultiClusterApiPlaneService multiClusterApiPlaneService;

    @Autowired
    private IGatewayService gatewayService;

    @Autowired
    private IVirtualGatewayInfoService virtualGatewayInfoService;

    @Autowired
    private IRouteDao routeRuleProxyDao;

    @Autowired
    private IServiceProxyService serviceProxyService;

    @Autowired
    private IEnvoyServiceProxyService envoyServiceProxyService;

    @Autowired
    MultiClusterTask multiClusterTask;

    @Autowired
    private IPluginInfoService pluginInfoService;

    @Autowired
    private IVirtualGatewayProjectService virtualGatewayProjectService;

    @Value("${multicluster.startCheckTask:false}")
    private boolean startCheckTask;

    @Value("${multicluster.GwClusterNames:#{null}}")
    private String gwClusterNames;

    @Scheduled(cron = "${multicluster.checkTaskCron:0 0 */1 * * ?}")
    public void dataCheckTask() {
        if (!startCheckTask){
            return;
        }
        try {
            multiClusterTask.startDataCheck();
        } catch (DistributedLockTimeOutException e) {
            logger.info("重复校验");
        } catch (Exception e){
            logger.error("执行定时校验任务异常", e);
        }
    }

    public void startDataCheck() {
        logger.info("============================");
        logger.info("多集群校验 | 开始执行校验任务");

        List<? extends VirtualGatewayDto> gatewayDtoList = findVirtualGatewayInfo();
        for (VirtualGatewayDto virtualGatewayDto : gatewayDtoList) {
            logger.info("多集群校验 | 开始校验虚拟网关:{}", virtualGatewayDto.getName());
            Map<String, List<ResourceDTO>> resourceMap = describeResourceInfo(virtualGatewayDto);
            //调用api-plane进行校验
            Map<String, List<ResourceCheckResultDTO>> checkResult = performDataCheck(virtualGatewayDto, resourceMap);
            //处理校验结果
            boolean needCorrect = handleCheckResult(checkResult);
            if (!needCorrect){
                return;
            }
            logger.info("多集群补偿 | 开始执行补偿任务（不删除资源）");
            Map<String, DataCorrectResultDTO> correctResult = dataCorrection(checkResult);
            //处理补偿结果
            handleCorrectResult(correctResult);
        }

    }


    public Map<String, List<ResourceCheckResultDTO>> performDataCheck(VirtualGatewayDto virtualGatewayDto, Map<String, List<ResourceDTO>> resource) {
        Map<String, Object> params = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        params.put("Action", "DataCheck");
        params.put("Version", "2022-09-15");

        Map<String, Object> body = new HashMap<>(Const.DEFAULT_MAP_SIZE);
        body.put("Gateway", virtualGatewayDto.getGwClusterName() + "-" + virtualGatewayDto.getCode());
        body.put("Resource", resource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientResponse response;
        try {
            response = HttpClientUtil.postRequest(virtualGatewayDto.getConfAddr() + "/util/resource", JSONObject.toJSONString(body), params, headers, EnvoyConst.MODULE_API_PLANE);
        } catch (Exception e) {
            log.error("调用api-plane发布接口异常", e);
            return null;
        }
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            log.error("调用api-plane发布服务接口失败，返回http status code非2xx，httpStatusCoed:{},errMsg:{}", response.getStatusCode(), response.getResponseBody());
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject((response.getResponseBody()));
        return jsonObject.getObject("Result", new TypeReference<Map<String, List<ResourceCheckResultDTO>>>(){});
    }

    private void handleCorrectResult(Map<String, DataCorrectResultDTO>correctResult){
        if (CollectionUtils.isEmpty(correctResult)){
            return;
        }
        for (Map.Entry<String, DataCorrectResultDTO> entry : correctResult.entrySet()) {
            doHandleCorrectResult(entry.getValue(), entry.getKey());
        }
    }

    private void doHandleCorrectResult(DataCorrectResultDTO result, String kind){
        if (result == null){
            return;
        }
        List<Long> successList = result.getSuccessList() == null ? new ArrayList<>() : result.getSuccessList();
        List<Long> failedList = result.getFaildList() == null ? new ArrayList<>() : result.getFaildList();
        logger.error("多集群补偿 | {}补偿结果", kind);
        logger.error("重新发布数：{}, 发布成功：{}， 发布失败:{}", result.getTotalCount(), successList, failedList);

    }

    private Map<String, DataCorrectResultDTO> dataCorrection(Map<String, List<ResourceCheckResultDTO>> param) {
        Map<String, DataCorrectResultDTO> result = new HashMap<>();
        for (Map.Entry<String, List<ResourceCheckResultDTO>> entry : param.entrySet()) {
            /**
             * 通过重新发布操作更新配置
             * 特别说明：补偿任务不执行删除CR的操作，只进行告警
             */
            List<Long> ids = entry.getValue().stream()
                    .filter(o -> o.getDbResourceInfo() != null)
                    .map(ResourceCheckResultDTO::getResourceId)
                    .collect(Collectors.toList());
            List<Long> failedIds = multiClusterApiPlaneService.rePublishResource(ids, entry.getKey());
            result.put(entry.getKey(), DataCorrectResultDTO.ofTotalAndFailed(ids, failedIds));
        }
        return result;
    }

    public List<? extends VirtualGatewayDto> findVirtualGatewayInfo(){
        if (StringUtils.hasText(gwClusterNames)){
            List<String> gwClusterNameList = Stream.of(gwClusterNames.split(","))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            List<Long> gwIds = gatewayService.findAll().stream().filter(o -> gwClusterNameList.contains(o.getGwClusterName())).map(GatewayDto::getId).collect(Collectors.toList());
            return virtualGatewayInfoService.getVirtualGatewayList(gwIds);
        }
        return virtualGatewayInfoService.findAll();
    }



    private boolean handleCheckResult(Map<String, List<ResourceCheckResultDTO>> checkResult){
        if (CollectionUtils.isEmpty(checkResult)){
            logger.info("多集群校验 | 校验结果：未发现数据不一致");
            return false;
        }
        boolean needCorrect = false;
        for (Map.Entry<String, List<ResourceCheckResultDTO>> entry : checkResult.entrySet()) {
            boolean handleResult = doHandleCheckResult(entry.getValue(), entry.getKey());
            needCorrect = needCorrect || handleResult;
        }
        return needCorrect;
    }

    private boolean doHandleCheckResult(List<ResourceCheckResultDTO> result, String kind){
        if (CollectionUtils.isEmpty(result)){
            return false;
        }
        List<String> needDeleteCustomResource = new ArrayList<>();
        List<ResourceCheckResultDTO> needUpdateCustomResource = new ArrayList<>();
        for (ResourceCheckResultDTO resourceCheckDTO : result) {
            if (StringUtils.isEmpty(resourceCheckDTO.getDbResourceInfo())){
                needDeleteCustomResource.add(resourceCheckDTO.getResourceName());
            }else {
                needUpdateCustomResource.add(resourceCheckDTO);
            }
        }
        logger.error("多集群校验 | {}校验结果", kind);
        if (!CollectionUtils.isEmpty(needDeleteCustomResource)){
            logger.error("多集群校验 | 存在需要删除的资源：");
            logger.error("{}", JSONObject.toJSONString(needDeleteCustomResource));
        }
        if (!CollectionUtils.isEmpty(needUpdateCustomResource)){
            logger.error("多集群校验 | 存在需要更新的资源：");
            logger.error("{}", JSONObject.toJSONString(needUpdateCustomResource));
        }
        return !CollectionUtils.isEmpty(needUpdateCustomResource);
    }

    public Map<String, List<ResourceDTO>> describeResourceInfo(VirtualGatewayDto vgDto) {
        Long vgId = vgDto.getId();
        String code = vgDto.getCode();
        String gwClusterName = vgDto.getGwClusterName();
        //查询服务
        Map<String, Object> params = Maps.newHashMap();
        List<ServiceProxyDto> serviceProxy = serviceProxyService.getServiceProxy(ServiceProxyQuery.builder().virtualGwId(vgId).projectId(NumberUtils.LONG_ZERO).build());
        List<ResourceDTO> serviceResources = serviceProxy.stream().map(o -> convertService(o, gwClusterName, code)).collect(Collectors.toList());

        //查询路由
        RouteQuery query = RouteQuery.builder().virtualGwId(vgId).build();
        List<RoutePO> routeRuleProxyInfos = routeRuleProxyDao.getRouteList(query);
        List<ResourceDTO> routeResources = routeRuleProxyInfos.stream().map(o -> convertRoute(o, gwClusterName, code)).collect(Collectors.toList());

        //查询插件
        List<PluginBindingInfo> pluginBindingInfos = pluginInfoService.getEnablePluginBindingList(vgId);
        //全局/host插件转换
        List<ResourceDTO> globalPluginResource = pluginBindingInfos.stream()
                .filter(o -> Arrays.asList(BINDING_OBJECT_TYPE_GLOBAL, BINDING_OBJECT_TYPE_HOST).contains(o.getBindingObjectType()))
                .map(o -> convertEnvoyPlugin(o, gwClusterName, code))
                .collect(Collectors.toList());

        //路由插件转换
        Map<String, PluginBindingInfo> routePluginMap = pluginBindingInfos.stream()
                .filter(o -> BINDING_OBJECT_TYPE_ROUTE_RULE.equals(o.getBindingObjectType()))
                .collect(Collectors.toMap(PluginBindingInfo::getBindingObjectId, Function.identity(), (p1, p2) -> p1.getVersion() > p2.getVersion() ? p1 : p2)
                );
        List<ResourceDTO> routePluginResource = new ArrayList<>(routePluginMap.values()).stream()
                .map(o -> convertEnvoyPlugin(o, gwClusterName, code))
                .collect(Collectors.toList());

        //结果处理
        Map<String, List<ResourceDTO>> res = new HashMap<>();
        if (!CollectionUtils.isEmpty(serviceResources)){
            res.put(ResourceEnum.Service.getKind(), serviceResources);
        }
        if (!CollectionUtils.isEmpty(routeResources)){
            res.put(ResourceEnum.Route.getKind(), routeResources);
        }
        List<ResourceDTO> pluginResource = Stream.of(routePluginResource, globalPluginResource).flatMap(Collection::stream).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pluginResource)){
            res.put(ResourceEnum.Plugin.getKind(), pluginResource);
        }
        return res;
    }


    public ResourceDTO convertRoute(RoutePO routePO, String gwClusterName, String vgCode){
        String name = "qz" + "-" + routePO.getId() + "-" + gwClusterName + "-" + vgCode;
        return ResourceDTO.of(routePO.getVersion(), routePO.getId(), name);
    }

    public ResourceDTO convertService(ServiceProxyDto serviceProxyDto, String gwClusterName, String vgCode){
        String name = ServiceProxyConvert.getCode(serviceProxyDto).toLowerCase(Locale.ROOT) + "-" + gwClusterName + "-" + vgCode;
        return ResourceDTO.of(serviceProxyDto.getVersion(), serviceProxyDto.getId(), name);
    }

    private ResourceDTO convertEnvoyPlugin(PluginBindingInfo pluginInfo, String gwClusterName, String vgCode){
        String name;
        String bindingObjectId = pluginInfo.getBindingObjectId();
        String bindingObjectType = pluginInfo.getBindingObjectType();
        if (BINDING_OBJECT_TYPE_ROUTE_RULE.equals(bindingObjectType)){
            name = bindingObjectId + "-" + gwClusterName + "-" + vgCode;
        }else if (BINDING_OBJECT_TYPE_GLOBAL.equals(bindingObjectType)){
            PermissionScopeDto project = virtualGatewayProjectService.getProjectScope(Long.parseLong(bindingObjectId));
            name = project.getPermissionScopeEnName() + "-" + bindingObjectId + vgCode+ pluginInfo.getPluginType();
        }else {
            name = bindingObjectType + "-" + bindingObjectId;
        }
        return ResourceDTO.of(pluginInfo.getVersion(), pluginInfo.getId(), name);
    }
}
