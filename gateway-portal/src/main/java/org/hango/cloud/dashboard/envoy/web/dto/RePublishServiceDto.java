package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 服务重新发布Dto
 *
 * @author hzchenzhongyang 2020-01-19
 */
public class RePublishServiceDto {
    @JSONField(name = "RePublishAllService")
    private boolean rePublishAllService = true;

    @JSONField(name = "ServiceIdList")
    private List<Long> serviceIdList;

    @JSONField(name = "GwId")
    @Min(1)
    private long gwId;


    public boolean getRePublishAllService() {
        return rePublishAllService;
    }

    public void setRePublishAllService(boolean rePublishAllService) {
        this.rePublishAllService = rePublishAllService;
    }

    public List<Long> getServiceIdList() {
        return serviceIdList;
    }

    public void setServiceIdList(List<Long> serviceIdList) {
        this.serviceIdList = serviceIdList;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
