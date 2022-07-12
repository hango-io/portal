package org.hango.cloud.gdashboard.api.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2018/11/27
 */
public enum DubboType {

    DubboInterface,
    DubboMethod,
    DubboVersion,
    DubboGroup,
    DubboParam,
    DubboResponse;

    DubboType() {
    }

    public static String getDubboType(String type) {
        for (DubboType dubboType : DubboType.values()) {
            if (dubboType.name().equals(type)) {
                return dubboType.name();
            }
        }
        return null;
    }
}
