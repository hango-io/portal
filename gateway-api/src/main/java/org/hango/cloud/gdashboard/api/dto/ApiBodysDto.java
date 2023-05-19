package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;
import java.util.List;

public class ApiBodysDto {
    @JSONField(name = "ApiId")
    private long id;

    @JSONField(name = "ApiBodies")
    @Valid
    private List<ApiBodyBasicDto> apiBodyBasicDtoList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ApiBodyBasicDto> getApiBodyBasicDtoList() {
        return apiBodyBasicDtoList;
    }

    public void setApiBodyBasicDtoList(List<ApiBodyBasicDto> apiBodyBasicDtoList) {
        this.apiBodyBasicDtoList = apiBodyBasicDtoList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
