package org.hango.cloud.common.infra.serviceproxy.dto;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/9/7 11:19
 **/
public class LocalitySettingDto {
    @JSONField(name = "Enable")
    private Boolean enable;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
