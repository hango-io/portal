package org.hango.cloud.common.infra.serviceregistry.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/8/18
 */
@SuppressWarnings("java:S115")
public enum RegistryCenterEnum {

    /**
     * Eureka注册中心方式发布的服务，对应的host拼接为{appName}.eureka，且格式为小写
     */
    Eureka("Eureka", "%s.eureka", 2),
    Zookeeper("Zookeeper", "%s.dubbo", 3),
    Nacos("Nacos", "%s.nacos", 4),
    /**
     * Consul注册中心方式发布的服务，对应的host拼接为{appName}.service.consul.{consul别名}
     * consul别名，用于区分不同consul应用
     */
    Consul("Consul", "%s.service.consul.%s", 1),
    Kubernetes("Kubernetes", "%s", 0),
    ;

    private String type;

    private String suffix;

    private int order;

    RegistryCenterEnum(String type, String suffix, int order) {
        this.type = type;
        this.suffix = suffix;
        this.order = order;
    }

    public static RegistryCenterEnum get(String type) {
        for (RegistryCenterEnum value : RegistryCenterEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getOrder() {
        return order;
    }
}
