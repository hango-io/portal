package org.hango.cloud.dashboard.envoy.web.dto.auth;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * AuthPermissionList 查询授权列表Dto
 */
public class AuthPermissionListDto {
    /**
     * totalCount
     */
    @JSONField(name = "TotalCount")
    private long totalCount;

    @JSONField(name = "AuthPermissionList")
    private List<AuthPermissionDto> authPermissionDtoList;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<AuthPermissionDto> getAuthPermissionDtoList() {
        return authPermissionDtoList;
    }

    public void setAuthPermissionDtoList(List<AuthPermissionDto> authPermissionDtoList) {
        this.authPermissionDtoList = authPermissionDtoList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
