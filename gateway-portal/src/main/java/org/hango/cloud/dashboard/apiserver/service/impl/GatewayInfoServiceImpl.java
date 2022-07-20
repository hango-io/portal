package org.hango.cloud.dashboard.apiserver.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.dashboard.apiserver.dao.GatewayInfoDao;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayAddrConfigInfo;
import org.hango.cloud.dashboard.apiserver.dto.gatewaydto.GatewayDto;
import org.hango.cloud.dashboard.apiserver.meta.GatewayInfo;
import org.hango.cloud.dashboard.apiserver.meta.PluginInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.ErrorCode;
import org.hango.cloud.dashboard.apiserver.meta.gateway.PermissionScopeDto;
import org.hango.cloud.dashboard.apiserver.service.IGatewayInfoService;
import org.hango.cloud.dashboard.apiserver.service.IGatewayProjectService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.apiserver.web.holder.UserPermissionHolder;
import org.hango.cloud.dashboard.envoy.meta.EnvoyVirtualHostInfo;
import org.hango.cloud.dashboard.envoy.service.IEnvoyGatewayService;
import org.hango.cloud.dashboard.envoy.service.impl.EnvoyGatewayServiceImpl;
import org.hango.cloud.dashboard.envoy.web.dto.EnvoyVirtualHostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/17 下午5:27.
 */
@Service
public class GatewayInfoServiceImpl implements IGatewayInfoService {
    private static final Logger logger = LoggerFactory.getLogger(GatewayInfoServiceImpl.class);

    @Autowired
    private GatewayInfoDao gatewayInfoDao;
    @Autowired
    private IEnvoyGatewayService gatewayService;

    @Override
    public GatewayInfo get(long id) {
        try {
            return gatewayInfoDao.get(id);
        } catch (Exception e) {
            logger.error("查询网关出错，错误信息为 {} , 网关Id ={} ", e.getMessage(), id);
            e.printStackTrace();
        }
        logger.info("未找到对应的网关, 网关Id ={} ", id);
        return null;
    }

    @Override
    public GatewayInfo getGatewayByName(String gwName) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwName", gwName);
        List<GatewayInfo> gatewayInfoList = gatewayInfoDao.getRecordsByField(params);
        if (gatewayInfoList.size() == 0) {
            return null;
        } else {
            return gatewayInfoList.get(0);
        }
    }

    @Override
    public GatewayInfo getGatewayInfoByGwClusterName(String gwClusterName) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwClusterName", gwClusterName);
        List<GatewayInfo> gatewayInfoList = gatewayInfoDao.getRecordsByField(params);
        if (gatewayInfoList.size() == 0) {
            return null;
        } else {
            return gatewayInfoList.get(0);
        }
    }

    @Override
    public boolean updateGwInfo(GatewayInfo gatewayInfo, boolean updateProjectId) {
        if (gatewayInfo == null) {
            return false;
        }
        gatewayInfo.setModifyDate(System.currentTimeMillis());

        GatewayInfo gatewayInfoInDb = get(gatewayInfo.getId());
        //envoy网关更新auth，envId
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType())) {
            gatewayInfo.setEnvId(gatewayInfoInDb.getEnvId());
            gatewayInfo.setAuthAddr(gatewayInfoInDb.getAuthAddr());
        }
        // Envoy网关在更新virtual host时修改projectId
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfo.getGwType()) && !updateProjectId) {
            gatewayInfo.setProjectId(gatewayInfoInDb.getProjectId());
        }
        if (!CollectionUtils.isEmpty(gatewayInfo.getVirtualHostList())){
            //插入Virtual Host 异常后，直接报错
            gatewayService.updateVirtualHostList(gatewayInfo.getId(),gatewayInfo.getVirtualHostList());
        }

        return 1 == gatewayInfoDao.update(gatewayInfo);
    }

    @Override
    public boolean update(GatewayInfo gatewayInfo) {
        return 1 == gatewayInfoDao.update(gatewayInfo);
    }

    @Override
    public boolean updateGwInfoForHealth(GatewayInfo gatewayInfo) {
        if (gatewayInfo == null) {
            return false;
        }
        return 1 == gatewayInfoDao.update(gatewayInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(long id, List<PluginInfo> pluginInfoList) {
        GatewayInfo gatewayInfoInDb = gatewayInfoDao.get(id);
        //envoy类型网关，直接删除网关
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInfoInDb.getGwType())) {
            gatewayInfoDao.delete(id);
            return true;
        }
        return true;
    }

    @Override
    public List<GatewayInfo> findAll() {
        return gatewayInfoDao.findAll();
    }

    @Override
    public List<GatewayInfo> findGatewayByLimit(String pattern, long offset, long limit) {
        return gatewayInfoDao.getGatewayInfoByLimit(pattern, offset, limit);
    }


    @Override
    public List<GatewayInfo> findGatwayByProjectIdAndLimit(String pattern, long offset, long limit, long projectId) {
        return gatewayInfoDao.getGatewayInfoByProjectIdAndLimit(pattern, offset, limit, projectId);
    }


    @Override
    public long getGatewayCount(String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            return gatewayInfoDao.getGatewayInfoCountsByPattern(pattern);
        }
        return gatewayInfoDao.getCountByFields(new HashMap<String, Object>());
    }

    @Override
    public long getGatewayCountByProjectId(String pattern, long projectId) {
        List<GatewayInfo> gatewayInfos = gatewayInfoDao.getGatewayInfoByProjectId(pattern, projectId);
        if (gatewayInfos != null) {
            return gatewayInfos.size();
        } else {
            return 0;
        }
    }

    /**
     * 当前项目下是否存在相同的网关名称
     *
     * @param gwName
     * @return
     */
    @Override
    public boolean isExistGwInstance(String gwName) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwName", gwName);
        return gatewayInfoDao.getCountByFields(params) == 0 ? false : true;
    }

    @Override
    public boolean isGwExists(long gwId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", gwId);
        return gatewayInfoDao.getCountByFields(params) == 0 ? false : true;
    }

    @Override
    public ErrorCode checkGwIdParam(String gwId) {
        if (StringUtils.isBlank(gwId)) {
            logger.info("请求GwId为空");
            return CommonErrorCode.MissingParameter("GwId");
        }
        if (!isGwExists(NumberUtils.toLong(gwId))) {
            logger.info("请求GwId不存在，gwId:{]", gwId);
            return CommonErrorCode.InvalidParameterValue(gwId, "GwId");
        }
        return CommonErrorCode.Success;
    }

    /**
     * 根据项目id查询所属项目下的网关实例
     *
     * @param projectId
     * @return
     */
    @Override
    public List<GatewayInfo> getGwEnvByProjectId(Long projectId) {
        return gatewayInfoDao.getGatewayInfoByProjectId(null, projectId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addGatewayByMetaDto(GatewayDto gatewayDto) {
        GatewayInfo gatewayInfo = GatewayDto.toMeta(gatewayDto);
        gatewayInfo.setCreateDate(System.currentTimeMillis());
        gatewayInfo.setModifyDate(System.currentTimeMillis());
        long gwId = gatewayInfoDao.add(gatewayInfo);
        if (!CollectionUtils.isEmpty(gatewayInfo.getVirtualHostList())){
            //插入Virtual Host 异常后，直接报错
            for (EnvoyVirtualHostInfo envoyVirtualHostInfo : gatewayInfo.getVirtualHostList()) {
                envoyVirtualHostInfo.setGwId(gwId);
                gatewayService.createVirtualHost(envoyVirtualHostInfo);
            }
        }
        return gwId;
    }

    @Override
    public List<GatewayInfo> getGwByEnvId(String envId) {
        Map<String, Object> params = new HashMap<>();
        params.put("envId", envId);
        return gatewayInfoDao.getRecordsByField(params);
    }

    @Override
    public GatewayInfo getGwByUniId(String gwUniId) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwUniId", gwUniId);
        List<GatewayInfo> recordsByField = gatewayInfoDao.getRecordsByField(params);
        return (recordsByField != null && recordsByField.size() > 0) ? recordsByField.get(0) : null;
    }

    @Override
    public ErrorCode checkAddParam(GatewayDto gatewayDto) {
        if (isExistGwInstance(gatewayDto.getGwName())) {
            return CommonErrorCode.GwNameAlreadyExist;
        }
        return checkCommonParam(gatewayDto);
    }

    @Override
    public ErrorCode checkCommonDataParam(GatewayDto gatewayDto) {
        //参数校验
        GatewayAddrConfigInfo gatewayAddrConfigInfo = gatewayDto.getGatewayAddrConfigInfo();
        if (gatewayAddrConfigInfo != null && StringUtils.isBlank(gatewayAddrConfigInfo.getAuditDbConfig())) {
            return CommonErrorCode.MissingDataSource;
        }

        if (gatewayAddrConfigInfo != null && StringUtils.isBlank(gatewayAddrConfigInfo.getMetricUrl())) {
            return CommonErrorCode.MissingParameter("MetricUrl");
        }
        return CommonErrorCode.Success;
    }

    private ErrorCode checkCommonParam(GatewayDto gatewayDto) {
        //envoy网关
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayDto.getGwType())) {
            if (StringUtils.isBlank(gatewayDto.getApiPlaneAddr())) {
                logger.info("创建/修改envoy网关，api-plane地址为空");
                return CommonErrorCode.MissingParameter("ApiPlaneAddr");
            }
            if (StringUtils.isBlank(gatewayDto.getGwClusterName())) {
                logger.info("创建/修改envoy网关，gw-cluster地址为空");
                return CommonErrorCode.MissingParameter("GwClusterName");
            }
            GatewayInfo sameClusterGateway = getGatewayByClusterName(gatewayDto.getGwClusterName());
            if (null != sameClusterGateway && sameClusterGateway.getId() != gatewayDto.getId()) {
                logger.info("创建/修改envoy网关，gw-cluster地址已存在");
                return CommonErrorCode.SameNameGatewayClusterExists;
            }
        }
        return CommonErrorCode.Success;
    }


    public GatewayInfo getGatewayByClusterName(String clusterName) {
        Map<String, Object> params = new HashMap<>();
        params.put("gwClusterName", clusterName);
        List<GatewayInfo> recordsByField = gatewayInfoDao.getRecordsByField(params);
        return (CollectionUtils.isEmpty(recordsByField)) ? null : recordsByField.get(0);
    }

    @Override
    public ErrorCode checkUpdateParam(GatewayDto gatewayDto) {
        GatewayInfo gatewayInDb = gatewayInfoDao.get(gatewayDto.getId());
        if (gatewayInDb == null) {
            return CommonErrorCode.NoSuchGateway;
        }
        if (Const.ENVOY_GATEWAY_TYPE.equals(gatewayInDb.getGwType())) {
            GatewayAddrConfigInfo gatewayAddrConfigInfo = gatewayDto.getGatewayAddrConfigInfo()
                    == null ? new GatewayAddrConfigInfo() : gatewayDto.getGatewayAddrConfigInfo();
            gatewayAddrConfigInfo.setAuditDatasourceSwitch(gatewayInDb.getAuditDatasourceSwitch());
            gatewayAddrConfigInfo.setAuditDbConfig(gatewayInDb.getAuditDbConfig());
            gatewayAddrConfigInfo.setMetricUrl(gatewayInDb.getPromAddr());
            gatewayDto.setGatewayAddrConfigInfo(gatewayAddrConfigInfo);
        }
        GatewayInfo gatewayInfo = getGatewayByName(gatewayDto.getGwName());
        if (gatewayInfo != null && gatewayInfo.getId() != gatewayDto.getId()) {
            logger.info("网关名称已经存在,gwName:{]", gatewayDto.getGwName());
            return CommonErrorCode.GwNameAlreadyExist;
        }
        return checkCommonParam(gatewayDto);
    }

    @Override
    public List<Long> getGwIdListByNameFuzzy(String gwName, long projectId) {
        List<Long> gwIdList = gatewayInfoDao.getGwIdListByNameFuzzy(gwName, projectId);
        return CollectionUtils.isEmpty(gwIdList) ? Lists.newArrayList() : gwIdList;
    }

    @Override
    public List<GatewayInfo> getGatewayInfoList(List<Long> gwIdList) {
        if (CollectionUtils.isEmpty(gwIdList)) {
            return Lists.newArrayList();
        }

        List<GatewayInfo> gatewayInfoList = gatewayInfoDao.getGatewayInfoList(gwIdList);
        return CollectionUtils.isEmpty(gatewayInfoList) ? Lists.newArrayList() : gatewayInfoList;
    }

    @Override
    public Map<String, List<GatewayInfo>> distinguishGatewayRelationshipByProjectIdAndGwId(List<GatewayInfo> associatedGateways, Long projectId, Long gwId) {
        if (CollectionUtils.isEmpty(associatedGateways)) {
            logger.error("distinguishGatewayRelationshipByProjectIdAndGwId gatewayList is empty.");
            return null;
        }
        Map<String, List<GatewayInfo>> gatewayMap = new HashMap<>(2);
        List<GatewayInfo> selfGateway = new ArrayList<>(1);
        List<GatewayInfo> otherAssociatedGateways = new ArrayList<>(associatedGateways.size() - 1);

        // 排除项目类型为project的目标网关
        List<EnvoyVirtualHostInfo> projectTypeVhList = gatewayService.getProjectTypeVhByProjectId(projectId);
        for (EnvoyVirtualHostInfo projectTypeVh : projectTypeVhList) {
            associatedGateways.removeIf(gatewayInfo -> gatewayInfo.getId() == projectTypeVh.getGwId());
        }

        // 排除源网关本身
        for (GatewayInfo associatedGateway : associatedGateways) {
            if (associatedGateway.getId() == gwId) {
                selfGateway.add(associatedGateway);
            } else {
                otherAssociatedGateways.add(associatedGateway);
            }
        }
        gatewayMap.put(Const.SELF_ASSOCIATED_GATEWAYS, selfGateway);
        gatewayMap.put(Const.OTHER_ASSOCIATED_GATEWAYS, otherAssociatedGateways);
        return gatewayMap;
    }


    @Override
    public List<GatewayInfo> getGatewayListByConditions(String pattern, long offset, long limit, long tenantId, long projectId) {
        List<GatewayInfo> gatewayInfoList = new ArrayList<>();
        //查询某一租户项目下的网关
        if (tenantId > 0L && projectId > 0L) {
            gatewayInfoList = findGatwayByProjectIdAndLimit(pattern, offset, limit, projectId);
        }
        return gatewayInfoList;
    }

    @Override
    public List<GatewayInfo> getGatewayByProjectIds(String pattern, List<Long> projectIds) {
        List<GatewayInfo> gatewayInfoList = new ArrayList<>();
        for (Long projectId : projectIds) {
            if (projectId != null) {
                List<GatewayInfo> gatewayInfos = gatewayInfoDao.getGatewayInfoByProjectId(pattern, projectId);
                gatewayInfoList.addAll(gatewayInfos);
            }
        }
        //去重
        gatewayInfoList = gatewayInfoList.stream()
                .collect(Collectors.collectingAndThen(Collectors
                        .toCollection(() -> new TreeSet<>(Comparator.comparing(GatewayInfo::getProjectId))), ArrayList::new));

        return gatewayInfoList;
    }
}