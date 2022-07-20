package org.hango.cloud.dashboard.apiserver.dto;

/**
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/9/13 下午8:15.
 */
public class GatewayInfoDto {

    private long id;

    private String gwEnv;

    private String gwAddr;

    public GatewayInfoDto(long id, String gwEnv, String gwAddr) {
        this.id = id;
        this.gwEnv = gwEnv;
        this.gwAddr = gwAddr;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGwEnv() {
        return gwEnv;
    }

    public void setGwEnv(String gwEnv) {
        this.gwEnv = gwEnv;
    }

    public String getGwAddr() {
        return gwAddr;
    }

    public void setGwAddr(String gwAddr) {
        this.gwAddr = gwAddr;
    }
}
