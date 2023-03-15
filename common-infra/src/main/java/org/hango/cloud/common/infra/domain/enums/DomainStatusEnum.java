package org.hango.cloud.common.infra.domain.enums;

import java.util.stream.Stream;

/**
 * @Author zhufengwei
 * @Date 2022/11/4
 */
@SuppressWarnings("java:S115")
public enum DomainStatusEnum {

    /**
     * 域名未被使用
     */
    NotUse("未使用"),

    /**
     * 域名未被使用，需要进行配置刷新
     */
    NotActive("待生效"),

    /**
     * 配置刷新后变成已生效状态
     */
    Active("已生效"),
    /**
     * 执行域名删除操作后变为待下线状态，刷新配置后才进行删除操作
     */
    WaitDelete( "待下线"),

    /**
     * 只用于关联，不进行管理，例如gateway api、ingress的域名
     */
    RelevanceOnly( "只用于关联");;

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

    public static boolean enable(String status){
        return WaitDelete.name().equals(status) || Active.name().equals(status);
    }

    public static boolean needRefresh(String status){
        return NotActive.name().equals(status) || WaitDelete.name().equals(status);
    }

    public static DomainStatusEnum getByName(String status){
        return Stream.of(values()).filter(o -> o.name().equals(status)).findFirst().orElse(null);
    }

}
