package org.hango.cloud.common.infra.plugin.meta;

public enum Operation {
    /**
     * 路由插件创建动作
     */
    CREATE("create route plugin"),
    /**
     * 路由插件更新动作
     */
    UPDATE("update route plugin"),
    /**
     * 路由插件删除动作
     */
    DELETE("delete route plugin");

    private final String action;

    Operation(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}