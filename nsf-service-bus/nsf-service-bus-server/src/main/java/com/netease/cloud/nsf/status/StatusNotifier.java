package com.netease.cloud.nsf.status;

/**
 * 用来更新Status的接口，可以提供默认的ValueGenerator，按一定的规则更新status。也可自定义status value或ValueGenerator
 * @author wupenghuai@corp.netease.com
 * @date 2020/5/6
 **/
public interface StatusNotifier {
    void notifyStatus(String key);

    void notifyStatus(String key, String value);

    void notifyStatus(String key, ValueGenerator generator);

    @FunctionalInterface
    interface ValueGenerator {
        String generate(String key);
    }
}
