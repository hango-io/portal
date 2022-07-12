package org.hango.cloud.dashboard.apiserver.dto.authmanage;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.dto.ApiInfoBasicDto;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * 授权管理相关dto
 */
public class AuthInfoDto {
    @JSONField(name = "ServiceId")
    @NotEmpty
    private long serviceId;

    @JSONField(name = "ServiceName")
    private String displayName;
    /**
     * 增加返回服务标识，
     */
    @JSONField(name = "ServiceTag")
    private String serviceName;

    @JSONField(name = "ApiAuthInfo")
    @NotEmpty
    private List<ApiInfoBasicDto> apiAuthInfo;
    @JSONField(name = "GwId")
    @NotEmpty
    private long gwId;
    @JSONField(name = "GwName")
    private String gwName;
    @NotEmpty
    @JSONField(name = "GwUniId")
    private String gwUniId;

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
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

    public List<ApiInfoBasicDto> getApiAuthInfo() {
        return apiAuthInfo;
    }

    public void setApiAuthInfo(List<ApiInfoBasicDto> apiAuthInfo) {
        this.apiAuthInfo = apiAuthInfo;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }

    public String getGwName() {
        return gwName;
    }

    public void setGwName(String gwName) {
        this.gwName = gwName;
    }

    public String getGwUniId() {
        return gwUniId;
    }

    public void setGwUniId(String gwUniId) {
        this.gwUniId = gwUniId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
