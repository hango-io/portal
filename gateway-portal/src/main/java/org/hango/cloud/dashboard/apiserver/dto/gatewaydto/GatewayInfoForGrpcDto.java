package org.hango.cloud.dashboard.apiserver.dto.gatewaydto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author TC_WANG
 * @date 2018/9/13
 */
public class GatewayInfoForGrpcDto {

    /**
     * 网关id
     */
    @JSONField(name = "GwId")
    private long id;

    /**
     * 网关名称
     */
    @JSONField(name = "GwName")
    private String gwName;

    public GatewayInfoForGrpcDto(long id, String gwName) {
        this.id = id;
        this.gwName = gwName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }
}

