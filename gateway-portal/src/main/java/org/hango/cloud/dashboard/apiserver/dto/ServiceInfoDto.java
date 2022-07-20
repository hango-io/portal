package org.hango.cloud.dashboard.apiserver.dto;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/9/13 下午8:29.
 */
public class ServiceInfoDto {

    private long id;
    private String serviceName;
    private String displayName;

    public ServiceInfoDto(long id, String serviceName, String displayName) {
        this.id = id;
        this.serviceName = serviceName;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
