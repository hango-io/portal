package com.netease.cloud.nsf.step;

import com.netease.cloud.nsf.util.ConversionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/7/22
 **/
public class Property {
    private Map<String, Object> property;

    public Property() {
        this.property = new LinkedHashMap<>();
    }

    public Map<String, Object> getProperty() {
        return property;
    }

    public void setProperty(Map<String, Object> property) {
        this.property = property;
    }

    public void add(String key, Object object) {
        this.property.put(key, object);
    }

    public Object get(String key) {
        return get(key, Object.class, false);
    }

    public <T> T get(String key, Class<T> tClass) {
        return get(key, tClass, false);
    }

    public <T> T strictGet(String key, Class<T> tClass) {
        return get(key, tClass, true);
    }

    public <T> T getOrDefault(String key, Class<T> tClass, T defaultValue) {
        T value = get(key, tClass);
        if (Objects.nonNull(value)) {
            return value;
        }
        return defaultValue;
    }

    private <T> T get(String key, Class<T> tClass, boolean strict) {
        if (Objects.isNull(key) || Objects.isNull(tClass)) {
            throw new RuntimeException("argument [key] or [class] could not be null");
        }
        Object object = property.get(key);
        if (Objects.isNull(object)) {
            if (strict) {
                throw new RuntimeException(String.format("the value for property [%s] could not be found", key));
            } else {
                return null;
            }
        }
        if (tClass.isAssignableFrom(object.getClass())) {
            return tClass.cast(object);
        }
        if (ConversionUtils.canConvert(object.getClass(), tClass)) {
            return ConversionUtils.convert(object, tClass);
        }
        throw new RuntimeException(String.format("object [%s] could not cast to class[%s]", object, tClass));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property1 = (Property) o;
        return Objects.equals(property, property1.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property);
    }
}
