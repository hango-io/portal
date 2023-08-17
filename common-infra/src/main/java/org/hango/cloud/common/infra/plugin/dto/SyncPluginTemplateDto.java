package org.hango.cloud.common.infra.plugin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class SyncPluginTemplateDto {
    @JSONField(name = "Id")
    private long id;

    @JSONField(name = "PluginBindingInfoIds")
    @NotEmpty
    private List<Long> pluginBindingInfoIds;
}
