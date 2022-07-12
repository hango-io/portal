package com.netease.cloud.nsf.util;

import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * 类型转换工具类
 */
public final class ConversionUtils {
    private static final ConversionService conversionService = new DefaultConversionService();

    private ConversionUtils() {
    }

    /**
     * Return {@code true} if objects of {@code sourceType} can be converted to the {@code targetType}.
     * <p>If this method returns {@code true}, it means {@link #convert(Object, Class)} is capable
     * of converting an instance of {@code sourceType} to {@code targetType}.
     * <p>Special note on collections, arrays, and maps types:
     * For conversion between collection, array, and map types, this method will return {@code true}
     * even though a convert invocation may still generate a {@link ConversionException} if the
     * underlying elements are not convertible. Callers are expected to handle this exceptional case
     * when working with collections and maps.
     *
     * @param sourceType the source type to convert from (may be {@code null} if source is {@code null})
     * @param targetType the target type to convert to (required)
     * @return {@code true} if a conversion can be performed, {@code false} if not
     * @throws IllegalArgumentException if {@code targetType} is {@code null}
     */
    public static boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }

    /**
     * Convert the given {@code source} to the specified {@code targetType}.
     *
     * @param source     the source object to convert (may be {@code null})
     * @param targetType the target type to convert to (required)
     * @return the converted object, an instance of targetType
     * @throws ConversionException      if a conversion exception occurred
     * @throws IllegalArgumentException if targetType is {@code null}
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        return conversionService.convert(source,targetType);
    }
}
