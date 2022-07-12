package org.hango.cloud.dashboard.apiserver.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/8/18
 */
public enum RegistryCenterEnum {

    /**
     * Eureka注册中心方式发布的服务，对应的host拼接为{appName}.eureka，且格式为小写
     */
    Eureka("Eureka", "%s.eureka"),
    Zookeeper("Zookeeper", "%s.dubbo"),
    Nacos("Nacos", "%s.nacos"),
    /**
     * Consul注册中心方式发布的服务，对应的host拼接为{appName}.service.consul.{consul别名}
     * consul别名，用于区分不同consul应用
     */
    Consul("Consul", "%s.service.consul.%s"),
    Kubernetes("Kubernetes", "%s");

    private String type;

    private String suffix;


    RegistryCenterEnum(String type, String suffix) {
        this.type = type;
        this.suffix = suffix;
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
}
