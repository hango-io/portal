package org.hango.cloud.common.infra.base.config;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hango.cloud.common.infra.base.annotation.JSONPolymorphism;
import org.hango.cloud.common.infra.base.annotation.KeyDiscern;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.reflections.Reflections;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc JSON多态解析器，负责选定多态Class，注册于{@link JSONParserConfig}
 * @date 2022/4/13
 */
public class JSONPolymorphismDeserializer extends JavaBeanDeserializer {

    private static final Map<? extends Class<?>, List<Class<?>>> clazzMap;
    private static final Map<? extends Class<?>, Set<String>> keyDiscernMap = Maps.newHashMap();

    static {
        Reflections reflections = new Reflections(BaseConst.HANGO_PREFIX);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(JSONPolymorphism.class);
        clazzMap = typesAnnotatedWith.stream().sorted(Comparator.comparing(c -> c.getAnnotation(JSONPolymorphism.class).order()))
                .collect(Collectors.groupingBy(Class::getSuperclass, Collectors.toList()));
        for (Class<?> clazz : typesAnnotatedWith) {
            JSONPolymorphism annotation = clazz.getAnnotation(JSONPolymorphism.class);
            String[] keyDiscernArray = annotation.keyDiscern();
            Field[] declaredFields = clazz.getDeclaredFields();
            Set<String> keyDiscerns = Arrays.stream(declaredFields).filter(f -> f.isAnnotationPresent(KeyDiscern.class))
                    .map(f -> f.isAnnotationPresent(JSONField.class) ? f.getAnnotation(JSONField.class).name() : f.getName()).collect(Collectors.toSet());
            keyDiscerns.addAll(Arrays.asList(keyDiscernArray));
            if (CollectionUtils.isEmpty(keyDiscerns)) {
                continue;
            }
            Set<String> discernKeySet = keyDiscernMap.getOrDefault(clazz, Sets.newHashSet());
            discernKeySet.addAll(keyDiscerns);
        }
    }

    public JSONPolymorphismDeserializer(ParserConfig config, Class<?> clazz, Type type) {
        super(config, getClazz(clazz), getType(type));

    }


    private static Class<?> getClazz(Class<?> clazz) {
        Annotation clazzAn = clazz.getAnnotation(JSONPolymorphism.class);
        if (clazzAn == null) {
            return clazz;
        }
        List<Class<?>> classes = clazzMap.get(clazz);
        if (CollectionUtils.isEmpty(classes)) {
            return clazz;
        }
        clazz = Iterables.getFirst(classes, clazz);
        return clazz;
    }

    private static Type getType(Type type) {
        if (!(type instanceof Class<?>)) {
            return type;
        }
        return getClazz(((Class) type));
    }
}
