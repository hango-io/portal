package org.hango.cloud.common.infra.domain.enums;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2022/11/4
 */
@SuppressWarnings("java:S115")
public enum DomainStatusEnum {
    /**
     * 被管理的域名
     */
    Managed( "管理中"),

    /**
     * 只用于关联，不进行管理，例如gateway api、ingress的域名
     */
    RelevanceOnly( "只用于关联");

    private String desc;
    DomainStatusEnum(String desc){
        this.desc = desc;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static DomainStatusEnum getByName(String status){
        return Stream.of(values()).filter(o -> o.name().equals(status)).findFirst().orElse(null);
    }

}
