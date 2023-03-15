package org.hango.cloud.common.infra.base.meta;


import java.util.stream.Stream;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/29 9:57
 **/
@SuppressWarnings("java:S115")
public enum ResourceEnum {
    Service("DestinationRule"),
    Route("VirtualService"),
    Plugin("EnvoyPlugin");

    private String kind;
    ResourceEnum(String kind){
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public static ResourceEnum getByName(String name){
        return Stream.of(values()).filter(o -> o.name().equals(name)).findFirst().orElse(null);
    }


    public static ResourceEnum getByKind(String kind){
        return Stream.of(values()).filter(o -> o.getKind().equals(kind)).findFirst().orElse(null);
    }
}

