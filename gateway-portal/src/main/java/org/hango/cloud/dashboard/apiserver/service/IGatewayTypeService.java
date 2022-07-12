package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.meta.GatewayEnum;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 获取网关类型，注: 所有与发布，网关类型相关的接口定义必须实现该方法
 * @date 2021/11/18
 */
@FunctionalInterface
public interface IGatewayTypeService {

    /**
     * 由于网关控制面存在多套引擎的管理能力，每套服务引擎中处理逻辑可能存在不一致的情况
     * 为了方便后续代码维护，处理不同引擎实现逻辑的实现类应该根据其类型分为不同的实现类
     * 其中共用逻辑可
     *
     * @return
     */
    GatewayEnum getGatewayType();


    /**
     * 通过网关类型动态获取需要执行的实现类
     *
     * @param applicationContext 上下文
     * @param gatewayEnum        网关类型枚举
     * @param clazz              实现类的超类
     * @param <T>                实现类必须实现{@link IGatewayTypeService}
     * @return
     */
    default <T extends IGatewayTypeService> T getBean(ApplicationContext applicationContext, GatewayEnum gatewayEnum, Class<T> clazz) {
        if (gatewayEnum == null) {
            throw new RuntimeException("为找到对应的网关类型");
        }
        Map<String, T> beans = applicationContext.getBeansOfType(clazz);

        if (CollectionUtils.isEmpty(beans)) {
            throw new RuntimeException("为找到对应的实现类");
        }
        for (T value : beans.values()) {
            if (gatewayEnum.equals(value.getGatewayType())) {
                return value;
            }
        }
        throw new RuntimeException("为找到对应的实现类");
    }

}
