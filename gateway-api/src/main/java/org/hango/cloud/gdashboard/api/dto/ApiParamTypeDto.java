package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class ApiParamTypeDto {
    @JSONField(name = "ParamTypeId")
    private long id;
    @JSONField(name = "ParamType")
    private String paramType;
    @JSONField(name = "ModelId")
    private long modelId;

    @JSONField(name = "ParamValue")
    private CreateApiModelDto createApiModelDto;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public long getModelId() {
        return modelId;
    }

    public void setModelId(long modelId) {
        this.modelId = modelId;
    }

    public CreateApiModelDto getCreateApiModelDto() {
        return createApiModelDto;
    }

    public void setCreateApiModelDto(CreateApiModelDto createApiModelDto) {
        this.createApiModelDto = createApiModelDto;
    }
}
