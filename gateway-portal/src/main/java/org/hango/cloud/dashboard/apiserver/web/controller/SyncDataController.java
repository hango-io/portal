package org.hango.cloud.dashboard.apiserver.web.controller;

import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.dashboard.apiserver.meta.SyncServiceInfo;
import org.hango.cloud.dashboard.apiserver.meta.errorcode.CommonErrorCode;
import org.hango.cloud.dashboard.apiserver.service.ISyncDataService;
import org.hango.cloud.dashboard.apiserver.util.Const;
import org.hango.cloud.dashboard.apiserver.web.holder.ProjectTraceHolder;
import org.hango.cloud.dashboard.common.distributedlock.MethodReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/12/28
 */
@RestController
@RequestMapping(value = Const.G_DASHBOARD_PREFIX, params = "Version=2018-08-09")
public class SyncDataController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(SyncDataController.class);


    @Autowired
    private ISyncDataService syncDataService;


    @MethodReentrantLock
    @GetMapping(params = "Action=SyncData")
    public String syncData() {
        long proId = ProjectTraceHolder.getProId();
        logger.info("Get Sync Data Info , ProId = {}", proId);
        logger.info("Start Sync Service Data Info ");
        List<SyncServiceInfo> syncServiceInfoList = syncDataService.getSyncListByProjectId(proId);
        syncDataService.syncServiceInfo(syncServiceInfoList);
        List<String> failedServiceList = syncServiceInfoList.stream().filter(syncServiceInfo ->
                        SyncServiceInfo.PreSyncStatusEnum.getName(syncServiceInfo.getPreSyncStatus()).equals(SyncServiceInfo.PreSyncStatusEnum.CONFLICT_SERVICE.getName()))
                .collect(Collectors.mapping(SyncServiceInfo::getDisplayName, Collectors.toList()));
        logger.info("End Sync Service Data Info ");
        logger.info("Start Sync Api Data Info ");
        Integer apiCount = syncDataService.syncApiInfo(proId);
        Map<String, Object> result = new HashMap<>();
        result.put("SuccessServiceCount", syncServiceInfoList.size() - failedServiceList.size());
        result.put("SuccessApiCount", apiCount);
        result.put("FailedServiceCount", failedServiceList.size());
        result.put("FailedServiceList", failedServiceList);
        return apiReturnSuccess(result);
    }

    @MethodReentrantLock
    @PostMapping(params = "Action=SyncDataOptional")
    public String syncDataOptional(@RequestBody List<String> serviceList) {
        if (CollectionUtils.isEmpty(serviceList)) {
            return apiReturn(CommonErrorCode.InvalidBodyFormat);
        }
        //同步选中服务
        long proId = ProjectTraceHolder.getProId();
        logger.info("Get Optional Sync Data Info , ProId = {}", proId);
        List<SyncServiceInfo> allServiceInfo = syncDataService.getSyncListByProjectId(proId);
        List<SyncServiceInfo> syncServiceInfoList = allServiceInfo.stream().filter(s -> serviceList.contains(s.getDisplayName())).collect(Collectors.toList());
        logger.info("Start Optional Sync Service Data Info ");
        syncDataService.syncServiceInfo(syncServiceInfoList);
        List<String> failedServiceList = syncServiceInfoList.stream().filter(syncServiceInfo ->
                        SyncServiceInfo.PreSyncStatusEnum.getName(syncServiceInfo.getPreSyncStatus()).equals(SyncServiceInfo.PreSyncStatusEnum.CONFLICT_SERVICE.getName()))
                .collect(Collectors.mapping(SyncServiceInfo::getDisplayName, Collectors.toList()));
        logger.info("End Optional Sync Service Data Info ");
        logger.info("Start Optional Sync Api Data Info ");
        Integer apiCount = syncDataService.syncApiInfo(syncServiceInfoList);
        Map<String, Object> result = new HashMap<>();
        result.put("SuccessServiceCount", syncServiceInfoList.size() - failedServiceList.size());
        result.put("SuccessApiCount", apiCount);
        result.put("FailedServiceCount", failedServiceList.size());
        result.put("FailedServiceList", failedServiceList);
        //默认处理 NSF 删除的已失步服务
        logger.info("Start Sync Service Which SyncStatus is 2 For Sync Lost Status");
        List<SyncServiceInfo> syncLostServices = allServiceInfo.stream().filter(s -> s.getSyncStatus() == 2 && !serviceList.contains(s.getDisplayName()))
                .collect(Collectors.toList());
        syncDataService.syncServiceInfo(syncLostServices);
        syncDataService.syncApiInfo(syncLostServices);
        return apiReturnSuccess(result);
    }

    @GetMapping(params = "Action=GetSyncStatistics")
    public String getSyncInfo() {
        long proId = ProjectTraceHolder.getProId();
        logger.info("Get Sync Data Info ,ProId = {}", proId);
        List<SyncServiceInfo> syncListByProjectId = syncDataService.getSyncListByProjectId(proId);
        Map<String, List<String>> serviceSyncStatistics = syncDataService.getServiceSyncStatistics(syncListByProjectId);
        Map<String, Object> result = new HashMap<>();
        result.put(TOTAL_COUNT, CollectionUtils.size(syncListByProjectId));
        result.put(RESULT, serviceSyncStatistics);
        return apiReturnSuccess(result);
    }


}

