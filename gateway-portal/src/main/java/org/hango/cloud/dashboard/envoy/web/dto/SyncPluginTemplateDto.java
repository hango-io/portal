package org.hango.cloud.dashboard.envoy.web.dto;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class SyncPluginTemplateDto {
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "PluginBindingInfoIds")
    @Size(min = 1)
    @NotNull
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
