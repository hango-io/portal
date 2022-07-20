package org.hango.cloud.gdashboard.api.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hango.cloud.gdashboard.api.util.Const;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * 添加新的数据模型的DTO
 *
 * @Author: Wang Dacheng(wangdacheng)
 * @Date: 创建时间: 2018/1/29 下午5:47.
 */
public class CreateApiModelDto implements Serializable {

    private static final long serialVersionUID = -7118900278147739854L;

    @JSONField(name = "ModelId")
    private long id;

    /**
     * 模型名称
     */
    @JSONField(name = "ModelName")
    @NotNull
    @Pattern(regexp = Const.REGEX_MODEL_NAME)
    private String modelName;

    /**
     * 服务id
     */
    @JSONField(name = "ServiceId")
    @NotNull
    private long serviceId;

    /**
     * 服务名称
     */
    @JSONField(name = "ServiceName")
    private String displayName;

    /**
     * 模型具体参数信息
     */
    @JSONField(name = "Params")
    @Valid
    private List<ApiParamDto> params;

    /**
     * 数据模型描述
     */
    @JSONField(name = "Description")
    @Pattern(regexp = Const.REGEX_DESCRIPTION)
    private String description;

    @JSONField(name = "CreateDate")
    private long createDate;

    @JSONField(name = "ModifyDate")
    private long modifyDate;

    /**
     * swagger同步状态 0：本地，1：同步，2：失步
     */
    @JSONField(name = "SwaggerSync")
    private int swaggerSync;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public List<ApiParamDto> getParams() {
        return params;
    }

    public void setParams(List<ApiParamDto> params) {
        this.params = params;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getSwaggerSync() {
        return swaggerSync;
    }

    public void setSwaggerSync(int swaggerSync) {
        this.swaggerSync = swaggerSync;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
