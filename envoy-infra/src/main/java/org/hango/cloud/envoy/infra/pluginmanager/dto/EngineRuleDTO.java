package org.hango.cloud.envoy.infra.pluginmanager.dto;

/**
 * @ClassName EngineRuleDTO
 * @Description 网关路由多活引擎规则
 * @Author xianyanglin
 * @Date 2023/5/31 16:23
 */
public class EngineRuleDTO {
    private String name;
    private String filename;
    private Object config;
    private String operate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
