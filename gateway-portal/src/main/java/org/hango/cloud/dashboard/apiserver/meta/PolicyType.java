package org.hango.cloud.dashboard.apiserver.meta;


import org.hango.cloud.dashboard.apiserver.util.Const;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/9/10
 */
public enum PolicyType {
    ShuntPolicy("ShuntPolicy", "分流策略", new String[]{Const.OBJECT_TYPE_SERVICE}),
    TrafficControlPolicy("TrafficControlPolicy", "限流策略", new String[]{Const.OBJECT_TYPE_API}),
    WhiteListPolicy("WhiteListPolicy", "黑白名单策略", new String[]{Const.OBJECT_TYPE_GLOBAL, Const.OBJECT_TYPE_SERVICE, Const.OBJECT_TYPE_API});


    private String type;

    private String desc;

    private String[] target;

    PolicyType(String type, String desc, String[] target) {
        this.type = type;
        this.desc = desc;
        this.target = target;
    }

    public static PolicyType get(String type) {
        for (PolicyType value : PolicyType.values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String[] getTarget() {
        return target;
    }

}
