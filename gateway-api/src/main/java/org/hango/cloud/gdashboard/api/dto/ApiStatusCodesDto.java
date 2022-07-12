package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import java.util.List;

public class ApiStatusCodesDto {
    @JSONField(name = "ApiId")
    private long id;
    @JSONField(name = "ResponseStatusCode")
    @Valid
    private List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtoList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ApiStatusCodeBasicDto> getApiStatusCodeBasicDtoList() {
        return apiStatusCodeBasicDtoList;
    }

    public void setApiStatusCodeBasicDtoList(List<ApiStatusCodeBasicDto> apiStatusCodeBasicDtoList) {
        this.apiStatusCodeBasicDtoList = apiStatusCodeBasicDtoList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
