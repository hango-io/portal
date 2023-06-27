package org.hango.cloud.envoy.advanced.metric.service;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2023/6/1
 */
@FunctionalInterface
public interface MetricFunction<T,R> {

    R apply(String address,T metric);
}
