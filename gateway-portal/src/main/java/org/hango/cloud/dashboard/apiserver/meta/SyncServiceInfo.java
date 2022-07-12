package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/4/23
 */
public class SyncServiceInfo extends ServiceInfo {


    /**
     * 同步前服务状态
     * <p>
     * 0 - 新服务 1 - 覆盖的服务， 2 -冲突的服务
     */
    private Integer preSyncStatus;

    public SyncServiceInfo() {
    }

    public SyncServiceInfo(ServiceInfo serviceInfo, Integer preSyncStatus) {
        this.setId(serviceInfo.getId());
        this.setCreateDate(serviceInfo.getCreateDate());
        this.setModifyDate(serviceInfo.getModifyDate());
        this.setDisplayName(serviceInfo.getDisplayName());
        this.setServiceName(serviceInfo.getServiceName());
        this.setContacts(serviceInfo.getContacts());
        this.setDescription(serviceInfo.getDescription());
        this.setStatus(serviceInfo.getStatus());
        this.setPublishedCount(serviceInfo.getPublishedCount());
        this.setHealthInterfacePath(serviceInfo.getHealthInterfacePath());
        this.setServiceType(serviceInfo.getServiceType());
        this.setWsdlUrl(serviceInfo.getWsdlUrl());
        this.setProjectId(serviceInfo.getProjectId());
        this.setSyncStatus(serviceInfo.getSyncStatus());
        this.setExtServiceId(serviceInfo.getExtServiceId());
        this.preSyncStatus = preSyncStatus;
    }

    public SyncServiceInfo(ServiceInfo serviceInfo) {
        this.setId(serviceInfo.getId());
        this.setCreateDate(serviceInfo.getCreateDate());
        this.setModifyDate(serviceInfo.getModifyDate());
        this.setDisplayName(serviceInfo.getDisplayName());
        this.setServiceName(serviceInfo.getServiceName());
        this.setContacts(serviceInfo.getContacts());
        this.setDescription(serviceInfo.getDescription());
        this.setStatus(serviceInfo.getStatus());
        this.setPublishedCount(serviceInfo.getPublishedCount());
        this.setHealthInterfacePath(serviceInfo.getHealthInterfacePath());
        this.setServiceType(serviceInfo.getServiceType());
        this.setWsdlUrl(serviceInfo.getWsdlUrl());
        this.setProjectId(serviceInfo.getProjectId());
        this.setSyncStatus(serviceInfo.getSyncStatus());
        this.setExtServiceId(serviceInfo.getExtServiceId());
    }

    public Integer getPreSyncStatus() {
        return preSyncStatus;
    }

    public void setPreSyncStatus(Integer preSyncStatus) {
        this.preSyncStatus = preSyncStatus;
    }


    public enum PreSyncStatusEnum {
        CONFLICT_SERVICE(2, "Conflict")
        /**
         * 冲突服务
         */
        ,
        CONVERT_SERVICE(1, "Convert")
        /**
         * 覆盖服务
         */
        ,
        NEW_SERVICE(0, "New")
        /**
         * 新服务
         */
        ;
        private String name;

        private Integer preSyncStatus;


        PreSyncStatusEnum(Integer preSyncStatus, String name) {
            this.preSyncStatus = preSyncStatus;
            this.name = name;
        }

        public static String getName(Integer preSyncStatus) {
            for (PreSyncStatusEnum preSyncStatusEnum : PreSyncStatusEnum.values()) {
                if (preSyncStatusEnum.getPreSyncStatus().equals(preSyncStatus)) {
                    return preSyncStatusEnum.name;
                }
            }
            return StringUtils.EMPTY;
        }

        public static PreSyncStatusEnum get(Integer preSyncStatus) {
            for (PreSyncStatusEnum preSyncStatusEnum : PreSyncStatusEnum.values()) {
                if (preSyncStatusEnum.getPreSyncStatus().equals(preSyncStatus)) {
                    return preSyncStatusEnum;
                }
            }
            return null;
        }

        public Integer getPreSyncStatus() {
            return preSyncStatus;
        }

        public String getName() {
            return name;
        }
    }

}
