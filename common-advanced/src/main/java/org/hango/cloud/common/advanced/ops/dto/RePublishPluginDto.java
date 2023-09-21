package org.hango.cloud.common.advanced.ops.dto;

import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/6/19 10:39
 **/
public class RePublishPluginDto {
    @JSONField(name = "VirtualGwId")
    @Min(value = 1, message = "Min VirtualGwId Error")
    private long virtualGwId;

    @JSONField(name = "PluginType")
    private String pluginType;

    @JSONField(name = "BindingObjectType")
    private String bindingObjectType;

    @JSONField(name = "BindingObjectIdList")
    private List<String> bindingObjectIdList;

    public long getVirtualGwId() {
        return virtualGwId;
    }

    public void setVirtualGwId(long virtualGwId) {
        this.virtualGwId = virtualGwId;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getBindingObjectType() {
        return bindingObjectType;
    }

    public void setBindingObjectType(String bindingObjectType) {
        this.bindingObjectType = bindingObjectType;
    }

    public List<String> getBindingObjectIdList() {
        return bindingObjectIdList;
    }

    public void setBindingObjectIdList(List<String> bindingObjectIdList) {
        this.bindingObjectIdList = bindingObjectIdList;
    }
}
