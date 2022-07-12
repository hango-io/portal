package org.hango.cloud.dashboard.apiserver.dto.grpcdto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * 下线protobuf dto
 *
 * @author TC_WANG
 * @date 2019/7/2
 */
public class OfflineServiceProtobufDto implements Serializable {

    @JSONField(name = "ServiceName")
    private String serviceName;

    /**
     * pb对应的名称
     */
    @JSONField(name = "PbName")
    private String pbName;

    /**
     * 需要下线的API列表
     */
    @JSONField(name = "OfflineApiIdList")
    private List<Long> offlineApiIdList;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPbName() {
        return pbName;
    }

    public void setPbName(String pbName) {
        this.pbName = pbName;
    }

    public List<Long> getOfflineApiIdList() {
        return offlineApiIdList;
    }

    public void setOfflineApiIdList(List<Long> offlineApiIdList) {
        this.offlineApiIdList = offlineApiIdList;
    }
}

