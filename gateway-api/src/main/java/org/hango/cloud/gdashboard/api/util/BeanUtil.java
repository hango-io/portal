package org.hango.cloud.gdashboard.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BeanUtil {
    private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

    public static <T, U> U copy(T t, Class<U> type) {

        U target = null;
        if (t == null) {
            return target;
        }

        try {
            target = type.newInstance();
        } catch (Exception e) {
            logger.warn("create new instance for {} failed", type);
        }

        BeanUtils.copyProperties(t, target);
        return (U) target;
    }

    public static <T, U> List<U> copyList(List<T> ts, Class<U> type) {

        List<U> us = new ArrayList<>();
        if (CollectionUtils.isEmpty(ts)) {
            return us;
        }

        try {
            for (T t : ts) {
                U u = type.newInstance();
                BeanUtils.copyProperties(t, u);
                us.add(u);
            }
        } catch (Exception e) {
            logger.warn("create new instance for {} failed", type);
        }
        return us;
    }

    /**
     * 替换集合中Bean的单个属性值
     *
     * @param beans
     * @param property
     * @param value
     * @param <T>
     * @param <V>
     */
    public static <T, V> void replaceValueWithBeans(List<T> beans, String property, V value) {
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        try {
            Class clazz = beans.get(0).getClass();
            Field field = clazz.getDeclaredField(property);
            Type type = field.getType();
            //判断属性是否为原始类型
            Class parameterClass = null;
            if (((Class) type).isPrimitive()) {
                switch (((Class) type).getName()) {
                    case "boolean":
                        parameterClass = boolean.class;
                        break;
                    case "byte":
                        parameterClass = byte.class;
                        break;
                    case "short":
                        parameterClass = short.class;
                        break;
                    case "char":
                        parameterClass = char.class;
                        break;
                    case "int":
                        parameterClass = int.class;
                        break;
                    case "float":
                        parameterClass = float.class;
                        break;
                    case "long":
                        parameterClass = long.class;
                        break;
                    case "double":
                        parameterClass = double.class;
                        break;
                    default:
                        parameterClass = Object.class;
                }
            }
            if (null == parameterClass) {
                parameterClass = Class.forName(((Class) type).getName());
            }
            Method setMethod = clazz.getMethod("set" + property.substring(0, 1).toUpperCase() + property.substring(1), parameterClass);
            for (T t : beans) {
                setMethod.invoke(t, value);
            }
        } catch (NoSuchFieldException e) {
            logger.error("replaceValueWithBeans error, property = {} , value = {}, error = {}", property, value, e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.error("replaceValueWithBeans error, property = {} , value = {}, error = {}", property, value, e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("replaceValueWithBeans error, property = {} , value = {}, error = {}", property, value, e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("replaceValueWithBeans error, property = {} , value = {}, error = {}", property, value, e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("replaceValueWithBeans error, property = {} , value = {}, error = {}", property, value, e.getMessage());
        }
    }
}
