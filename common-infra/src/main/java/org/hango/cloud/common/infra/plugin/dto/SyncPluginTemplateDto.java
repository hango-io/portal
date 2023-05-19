package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class SyncPluginTemplateDto {
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "PluginBindingInfoIds")
    @NotEmpty
    private List<Long> pluginBindingInfoIds;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Long> getPluginBindingInfoIds() {
        return pluginBindingInfoIds;
    }

    public void setPluginBindingInfoIds(List<Long> pluginBindingInfoIds) {
        this.pluginBindingInfoIds = pluginBindingInfoIds;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
