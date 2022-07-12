package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import java.util.List;

public class ApiHeadersDto {
    @JSONField(name = "ApiId")
    private long id;
    @JSONField(name = "Headers")
    @Valid
    private List<ApiHeaderBasicDto> apiHeaderBasicDtoList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ApiHeaderBasicDto> getApiHeaderBasicDtoList() {
        return apiHeaderBasicDtoList;
    }

    public void setApiHeaderBasicDtoList(List<ApiHeaderBasicDto> apiHeaderBasicDtoList) {
        this.apiHeaderBasicDtoList = apiHeaderBasicDtoList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
