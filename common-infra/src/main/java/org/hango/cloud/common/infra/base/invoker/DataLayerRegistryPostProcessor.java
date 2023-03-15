//package org.hango.cloud.common-infra.infra.base.invoker;
//
//import com.google.common-infra.collect.Maps;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.math.NumberUtils;
//import org.hango.cloud.common-infra.infra.base.annotation.LoadOrder;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.config.BeanDefinition;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.beans.factory.support.BeanDefinitionRegistry;
//import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
//import org.springframework.beans.factory.support.DefaultListableBeanFactory;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//
///**
// * @author zhangbj
// * @version 1.0
// * @Type
// * @Desc 数据层重复bean加载处理逻辑，配合{@link LoadOrder}使用
// * @date 2022/4/20
// */
//@Component
//public class DataLayerRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
//
//    /**
//     * 处理数据bean加载顺序， 设置{@link LoadOrder#order()}值最小的bean 为 primary {@link Primary}
//     * 其他bean可通过{@link Qualifier} 或 {@link Resource}引入使用
//     *
//     * @param registry
//     * @throws BeansException
//     */
//    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        DefaultListableBeanFactory defaultRegistry = (DefaultListableBeanFactory) registry;
//        String[] beanNamesForAnnotation = defaultRegistry.getBeanNamesForAnnotation(LoadOrder.class);
//        if (ArrayUtils.isEmpty(beanNamesForAnnotation)) {
//            return;
//        }
//        Map<Class<?>, TreeMap<Integer, BeanDefinition>> definitionMap = Maps.newHashMap();
//        try {
//            for (String name : beanNamesForAnnotation) {
//                BeanDefinition beanDefinition = defaultRegistry.getBeanDefinition(name);
//                String beanClassName = beanDefinition.getBeanClassName();
//                Class<?> clazz = Class.forName(beanClassName);
//                LoadOrder loadOrder = clazz.getAnnotation(LoadOrder.class);
//                Class<?>[] classes = loadOrder.implFor();
//                for (Class<?> ac : classes) {
//                    TreeMap<Integer, BeanDefinition> beanMap = definitionMap.getOrDefault(ac, Maps.newTreeMap());
//                    beanMap.put(loadOrder.order(), beanDefinition);
//                    definitionMap.putIfAbsent(ac, beanMap);
//                }
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        for (TreeMap<Integer, BeanDefinition> value : definitionMap.values()) {
//            Map.Entry<Integer, BeanDefinition> first = value.firstEntry();
//            if (NumberUtils.INTEGER_ONE.equals(value.size())) {
//                continue;
//            }
//            first.getValue().setPrimary(true);
//        }
//    }
//
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//
//    }
//}
