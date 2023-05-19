package org.hango.cloud.common.infra.base.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/4/27
 */
public class ActionPair {
    /**
     * action , used to build request query param. key as {@link BaseConst#ACTION}
     */
    private String action;

    /**
     * version , used to build request query param. key as {@link BaseConst#VERSION}
     */
    private String version;

    public ActionPair(String action, String version) {
        this.action = action;
        this.version = version;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
