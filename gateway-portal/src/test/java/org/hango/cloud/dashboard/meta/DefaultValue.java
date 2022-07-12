package org.hango.cloud.dashboard.meta;

import com.alibaba.fastjson.JSON;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultValue {
    private static final Map<Class<?>, Object> DEFAULTS;

    static {
        // Only add to this map via put(Map, Class<T>, T)
        Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
        put(map, boolean.class, false);
        put(map, char.class, '\0');
        put(map, byte.class, (byte) 0);
        put(map, short.class, (short) 0);
        put(map, int.class, 0);
        put(map, long.class, 0L);
        put(map, float.class, 0f);
        put(map, double.class, 0d);
        put(map, String.class, "0");
        DEFAULTS = Collections.unmodifiableMap(map);
    }

    private DefaultValue() {
    }

    private static <T> void put(Map<Class<?>, Object> map, Class<T> type, T value) {
        map.put(type, value);
    }

    /**
     * Returns the default value of {@code type} as defined by JLS --- {@code 0} for numbers, {@code false} for {@code boolean} and {@code '\0'} for
     * {@code char}. For non-primitive types and {@code void}, null is returned.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> type) {
        if (null == type) {
            return null;
        }
        if (type.isPrimitive() || String.class.equals(type)) {
            return (T) DEFAULTS.get(type);
        }
        if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return (T) JSON.parseObject("[]", type);
        } else {
            return (T) JSON.parseObject("{}", type);
        }
    }

}
