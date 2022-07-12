package org.hango.cloud.dashboard.apiserver.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/8/29 下午2:48.
 */
public class PublishedServiceInfoForSkiffDto {

    @JSONField(name = "ServiceName")
    private String serviceName;

    @JSONField(name = "GwList")
    private List<String> gwList;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getGwList() {
        return gwList;
    }

    public void setGwList(List<String> gwList) {
        this.gwList = gwList;
    }
}
