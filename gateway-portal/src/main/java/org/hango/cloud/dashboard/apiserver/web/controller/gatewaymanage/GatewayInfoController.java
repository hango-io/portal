package org.hango.cloud.dashboard.apiserver.web.controller.gatewaymanage;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.dashboard.apiserver.aop.Audit;
import org.hango.cloud.dashboard.apiserver.dto.auditdto.ResourceDataDto;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.EasyGatewayDto;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCodeEnum;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.service.apigateway.IGetInfoFromGatewayService;
import org.hango.cloud.dashboard.apiserver.util.AuditResourceHolder;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.controller.AbstractController;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.IEnvoyIstioGatewayService;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyGatewaySettingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Modified hanjiahao
 * 网关基本管理，包括创建网关、删除网关、查询网关基本信息
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = {"Version=2018-08-09"})
@Validated
public class GatewayInfoController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(GatewayInfoController.class);

    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private IGetInfoFromGatewayService getInfoFromGatewayService;
    @Autowired
    private IEnvoyGatewayService envoyGatewayService;
    @Autowired
    private IEnvoyIstioGatewayService envoyIstioGatewayService;



    @MethodReentrantLock
    @RequestMapping(params = {"Action=CreateGateway"}, method = RequestMethod.POST)
    @Audit(eventName = "CreateGwInfo", description = "创建网关")
    public Object addGwInfo(@Validated @RequestBody GatewayDto gatewayDto) {
        //操作审计记录资源名称
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_GATEWAY, null, gatewayDto.getGwName());
        AuditResourceHolder.set(resource);
        //创建网关参数校验
        ErrorCode errorCode = gatewayInfoService.checkAddParam(gatewayDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayDto.getGwType())) {
            boolean result = envoyIstioGatewayService.updateGatewaySetting(new EnvoyGatewaySettingDto(), GatewayDto.toMeta(gatewayDto));
            if (!result) {
                logger.warn("调用ApiPlane创建网关失败,请检查网络连接");
                return apiReturn(CommonErrorCode.UpdateToGwFailure);
            }
        }
        long gwId = gatewayInfoService.addGatewayByMetaDto(gatewayDto);
        resource.setResourceId(gwId);
        return apiReturnSuccess(gwId);

    }

    /**
     * 查询所有环境信息列表
     */
    @RequestMapping(params = {"Action=DescribeGatewayList"}, method = RequestMethod.GET)
    public Object getAllGwInfoList(@RequestParam(value = "Pattern", required = false) String pattern,
                                   @Min(0) @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                   @Min(1) @Max(1000) @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit) {
        long gatewayCount = gatewayInfoService.getGatewayCountByProjectId(pattern, ProjectTraceHolder.getProId());
        List<GatewayInfo> gatewayInfoList = gatewayInfoService.findGatwayByProjectIdAndLimit(pattern, offset, limit, ProjectTraceHolder.getProId());
        gatewayInfoList.forEach(item -> {
            if (Const.ENVOY_GATEWAY_TYPE.equals(item.getGwType())) {
                List<EnvoyVirtualHostInfo> virtualHostInfoList = envoyGatewayService.getVirtualHostListByGwId(item.getId());
                item.setVirtualHostList(virtualHostInfoList);
            }
        });
        List<GatewayDto> gatewayDtos = gatewayInfoList.stream().map(GatewayDto::fromMeta).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("GatewayCount", gatewayCount);
        result.put("GatewayInfos", gatewayDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 网关管理页面查找所有网关信息
     * 由于网关管理需要区分全局或者项目下网关，需要提供单独的接口进行获取
     * 网关管理觉得可以查看所有的网关，其他的角色只能获得本项目下的网关
     */
    @RequestMapping(params = {"Action=DescribeGatewayListForGatewayTab"}, method = RequestMethod.GET)
    public Object getAllGwInfoListForGatewayTab(@RequestParam(value = "Pattern", required = false) String pattern,
                                                @Min(0) @RequestParam(value = "Offset", required = false, defaultValue = "0") long offset,
                                                @Min(1) @Max(1000) @RequestParam(value = "Limit", required = false, defaultValue = "20") long limit,
                                                @RequestParam(value = "TenantId", required = false, defaultValue = "0") long tenantId,
                                                @RequestParam(value = "ProjectId", required = false, defaultValue = "0") long projectId) {
        logger.info("根据条件查询网关信息 pattern:{}, offset:{}, limit:{}, tenantId:{},projectId:{}", pattern, offset, limit, tenantId, projectId);
        List<GatewayInfo> gatewayInfoList;
        Map<String, Object> result = new HashMap<>();
        gatewayInfoList = gatewayInfoService.getGatewayListByConditions(pattern, 0L, 1000L, tenantId, projectId);
        List<GatewayDto> gatewayDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(gatewayInfoList)) {
            gatewayInfoList.forEach(item -> {
                if (Const.ENVOY_GATEWAY_TYPE.equals(item.getGwType())) {
                    List<EnvoyVirtualHostInfo> virtualHostInfoList = envoyGatewayService.getVirtualHostListByGwId(item.getId());
                    item.setVirtualHostList(virtualHostInfoList);
                }
            });
            gatewayDtos = gatewayInfoList.stream()
                    .sorted(Comparator.comparing(GatewayInfo::getId))
                    .skip(offset)
                    .limit(limit)
                    .map(GatewayDto::fromMeta)
                    .collect(Collectors.toList());
        }
        result.put("GatewayCount", CollectionUtils.isEmpty(gatewayDtos) ? 0 : gatewayDtos.size());
        result.put("GatewayInfos", gatewayDtos);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * 删除网关
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=DeleteGateway"}, method = RequestMethod.GET)
    @Audit(eventName = "DeleteGwInfo", description = "删除网关")
    public Object deleteGwInfo(@RequestParam(value = "GwId") long id) {
        //操作审计记录资源名称
        GatewayInfo gatewayInfo = gatewayInfoService.get(id);
        if (gatewayInfo == null) {
            return apiReturn(CommonErrorCode.Success);
        }
        ResourceDataDto resource = new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_GATEWAY, id, gatewayInfo == null ? null : gatewayInfo.getGwName());
        AuditResourceHolder.set(resource);
        //TODO envoy网关删除已发布信息判断，已经具有发布信息，不允许删除
        return apiReturn(CommonErrorCode.Success);
    }


    /**
     * 查询gateway
     */
    @RequestMapping(params = {"Action=DescribeGatewayById"}, method = RequestMethod.GET)
    public Object getGwInfoById(@RequestParam(value = "GwId") long id) {

        GatewayInfo gatewayInfo = gatewayInfoService.get(id);
        if (gatewayInfo == null) {
            logger.info("不存在gwId下的网关，gwId:{}", id);
            return apiReturn(CommonErrorCode.NoSuchGwId);
        }
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            List<EnvoyVirtualHostInfo> virtualHostInfoList = envoyGatewayService.getVirtualHostListByGwId(gatewayInfo.getId());
            gatewayInfo.setVirtualHostList(virtualHostInfoList);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("GatewayInfo", GatewayDto.fromMeta(gatewayInfo));
        return apiReturn(CommonErrorCode.Success, result);
    }


    /**
     * @param gatewayDto
     * @return
     */
    @MethodReentrantLock
    @RequestMapping(params = {"Action=UpdateGateway"}, method = RequestMethod.POST)
    @Audit(eventName = "UpdateGwInfo", description = "修改环境信息")
    public Object updateGwInfo(@Validated @RequestBody GatewayDto gatewayDto) {
        logger.info("修改网关信息,gatewayInfo:", gatewayDto.toString());

        //操作审计记录资源名称
        AuditResourceHolder.set(new ResourceDataDto(Const.AUDIT_RESOURCE_TYPE_GATEWAY, gatewayDto.getId(), gatewayDto.getGwName()));
        //同步网关真实配置信息并进行参数校验

        ErrorCode errorCode = gatewayInfoService.checkUpdateParam(gatewayDto);
        if (!ErrorCodeEnum.Success.getCode().equals(errorCode.getCode())) {
            return apiReturn(errorCode);
        }

        if (!gatewayInfoService.updateGwInfo(GatewayDto.toMeta(gatewayDto), false)) {
            logger.info("更新网关失败");
            return apiReturn(CommonErrorCode.InternalServerError);
        }
        return apiReturn(CommonErrorCode.Success);
    }

    @RequestMapping(params = {"Action=CheckGatewayAuth"}, method = RequestMethod.GET)
    public Object checkFromGatewayInstance(@RequestParam(value = "GwId") long gwId) {
        //参数校验
        if (!gatewayInfoService.isGwExists(gwId)) {
            ErrorCode errorCode = CommonErrorCode.InvalidParameterValueGwId(String.valueOf(gwId));
            return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        boolean flag = getInfoFromGatewayService.checkAuthConfig(gatewayInfo.getGwAddr());
        Map<String, Object> result = new HashMap<>();
        result.put("CheckFlag", flag);
        return apiReturn(CommonErrorCode.Success, result);
    }

    @RequestMapping(params = {"Action=DescribeTenantAndProject"}, method = RequestMethod.GET)
    public Object describeTenantAndProject(@RequestParam(value = "GwId") long gwId) {
        //参数校验
        if (!gatewayInfoService.isGwExists(gwId)) {
            ErrorCode errorCode = CommonErrorCode.InvalidParameterValueGwId(String.valueOf(gwId));
            return apiReturn(errorCode.getStatusCode(), errorCode.getCode(), errorCode.getMessage(), null);
        }
        GatewayInfo gatewayInfo = gatewayInfoService.get(gwId);
        List<Long> gatewayProjects = Lists.newArrayList();
        if (StringUtils.isNotEmpty(gatewayInfo.getProjectId())) {
            gatewayProjects = gatewayInfo.getProjectIdList();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("GatewayProject", gatewayProjects);
        return apiReturn(CommonErrorCode.Success, result);
    }

    /**
     * For 轻舟License
     *
     * @return
     */
    @RequestMapping(params = {"Action=DescribeGatewayInfoCount"}, method = RequestMethod.GET)
    public Object getGatewayInfoCount() {
        List<GatewayInfo> gatewayInfoList = gatewayInfoService.findAll();
        Set<String> gatewayInfoAddrSet = new HashSet<>();
        for (GatewayInfo gatewayInfo : gatewayInfoList) {
            gatewayInfoAddrSet.add(gatewayInfo.getGwAddr());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Count", gatewayInfoAddrSet.size());
        return apiReturn(200, null, null, result);
    }


    @GetMapping(params = {"Action=DescribeGatewaysByProjectId"})
    public String getGatewaysByProjectId(@RequestParam(value = "ProjectId") Long projectId,
                                         @RequestParam(value = "GwId") Long gwId) {
        logger.info("根据projectID查询网关列表, projectId:{}", projectId);
        // 查询与projectID和网关之间的关联关系
        List<GatewayInfo> gatewayInfoList = gatewayInfoService.findGatwayByProjectIdAndLimit(null, 0L, 10000L, projectId);
        // 进一步区分在相关联的网关中排除指定gwID的集合
        Map<String, List<GatewayInfo>> gatewayMap
                = gatewayInfoService.distinguishGatewayRelationshipByProjectIdAndGwId(gatewayInfoList, projectId, gwId);
        Map<String, Object> result = new HashMap<>(2);
        if (CollectionUtils.isEmpty(gatewayInfoList)) {
            result.put(Const.OTHER_ASSOCIATED_GATEWAYS, Collections.EMPTY_LIST);
            result.put(Const.SELF_ASSOCIATED_GATEWAYS, Collections.EMPTY_LIST);
        } else {
            gatewayMap.forEach((key, value) -> {
                List<EasyGatewayDto> gatewayDtoList = value.stream().map(EasyGatewayDto::fromMeta).collect(Collectors.toList());
                result.put(key, gatewayDtoList);
            });
        }
        return apiReturn(200, null, null, result);
    }
}
