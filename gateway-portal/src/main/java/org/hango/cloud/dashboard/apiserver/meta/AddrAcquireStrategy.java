package org.hango.cloud.dashboard.apiserver.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 获取后端服务地址方式枚举
 * @date 2019/1/10
 */
public enum AddrAcquireStrategy {
    /**
     * 注册中心拉取
     */
    RegistryCenter,
    /**
     * 手动输入
     */
    CustomInput;

    AddrAcquireStrategy() {
    }

    public static String getAcquireStrategy(String strategy) {
        for (AddrAcquireStrategy value : AddrAcquireStrategy.values()) {
            if (value.name().equals(strategy)) {
                return value.name();
            }
        }
        return null;
    }
}
