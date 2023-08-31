package org.hango.cloud.envoy.advanced.manager.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import org.hango.cloud.envoy.infra.serviceproxy.dto.ResultDTO;

/**
 * @Author zhufengwei
 * @Date 2023/4/18
 */
@Getter
@Setter
public class RepublishResult {

    @JSONField(name = "VirtualGwName")
    private String vgName;

    @JSONField(name = "Errmsg")
    private String errmsg;

    @JSONField(name = "Service")
    private ResultDTO service;

    @JSONField(name = "Plugin")
    private ResultDTO plugin;

    RepublishResult(){
    }

    private RepublishResult(String vgName, String errmsg, ResultDTO service, ResultDTO plugin){
        this.errmsg = errmsg;
        this.vgName = vgName;
        this.service = service;
        this.plugin = plugin;
    }


    public static RepublishResult of(String vgName){
        return new RepublishResult(vgName, null,null, null);
    }

    public static RepublishResult ofError(String vgName, String errMsg){
        return new RepublishResult(vgName, errMsg, null, null);
    }
}
