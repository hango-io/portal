package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

/**
 * API json dto，提供至前端，生成request、response param
 *
 * @author hanjiahao
 */
public class ApiBodyJsonDto {
    @JSONField(name = "ApiId")
    private long id;
    @JSONField(name = "Type")
    @NotEmpty
    private String type;
    @JSONField(name = "Param")
    private Map<String, Object> params;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
