package org.hango.cloud.dashboard.apiserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/4/8 13:53
 **/
@Component
public class ClassTypeUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassTypeUtil.class);

    private static final List<String> MAP_CLASS = Arrays.asList(Map.class.getName(), HashMap.class.getName(),
            TreeMap.class.getName(), LinkedHashMap.class.getName());

    private static final List<String> SET_CLASS = Arrays.asList(Set.class.getName(), HashSet.class.getName(),
            TreeSet.class.getName(), LinkedHashSet.class.getName());

    private static final List<String> LIST_CLASS = Arrays.asList(List.class.getName(), ArrayList.class.getName(),
            LinkedList.class.getName());

    private static final List<String> WRAPPER_CLASS = Arrays.asList(Integer.class.getName(), Boolean.class.getName(), Long.class.getName(),
            Short.class.getName(), Double.class.getName(), Float.class.getName(), Character.class.getName(), Byte.class.getName(), String.class.getName());


    public enum PrimitiveTypeEnum {

        BOOLEAN("boolean", boolean.class, false),
        CHAR("char", char.class, "\u0000"),
        BYTE("byte", byte.class, 0),
        SHORT("short", short.class, 0),
        INT("int", int.class, 0),
        LONG("long", long.class, 0),
        FLOAT("float", float.class, 0.0f),
        DOUBLE("double", double.class, 0.0d);

        /**
         * 类名
         */
        private String className;
        /**
         * java类
         */
        private Class<?> javaClass;

        /**
         * 对应Prometheus类型
         */
        private Object defaultValue;


        PrimitiveTypeEnum(String className, Class<?> javaClass, Object defaultValue) {
            this.className = className;
            this.javaClass = javaClass;
            this.defaultValue = defaultValue;
        }

        public static boolean isPrimitiveType(String javaType){
            return Stream.of(values()).map(PrimitiveTypeEnum::getClassName).anyMatch(o -> o.equals(javaType));
        }

        public static PrimitiveTypeEnum getByName(String className){
            return Stream.of(values())
                    .filter(o -> o.getClassName().equals(className))
                    .findFirst()
                    .orElse(null);
        }


        public static Object getDefaultValueByName(String className){
            PrimitiveTypeEnum matchEnum = getByName(className);
            return matchEnum == null ? null : matchEnum.getDefaultValue();
        }

        public static Class<?> getClassByName(String className){
            PrimitiveTypeEnum matchEnum = getByName(className);
            return matchEnum == null ? null : matchEnum.getJavaClass();
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Class<?> getJavaClass() {
            return javaClass;
        }

        public void setJavaClass(Class<?> javaClass) {
            this.javaClass = javaClass;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }
    }


    public static boolean isPrimitive(String typeString){
        return PrimitiveTypeEnum.isPrimitiveType(typeString);
    }

    public static boolean isWrapperClass(String typeString){
        return WRAPPER_CLASS.contains(typeString);
    }

    public static boolean isCollectionClass(String typeString) {
        return SET_CLASS.contains(typeString) || LIST_CLASS.contains(typeString);
    }

    public static boolean isMapClass(String typeString) {
        return MAP_CLASS.contains(typeString);
    }

    public static boolean isBooleanClass(String typeString){
        return Arrays.asList(boolean.class.getName(), Boolean.class.getName()).contains(typeString);
    }

    public static boolean isNumberClass(String typeString){
        return Arrays.asList(
                byte.class.getName(),
                short.class.getName(),
                int.class.getName(),
                long.class.getName(),
                float.class.getName(),
                double.class.getName(),
                Integer.class.getName(),
                Short.class.getName(),
                Double.class.getName(),
                Float.class.getName(),
                Byte.class.getName(),
                Long.class.getName()
        ).contains(typeString);
    }

    public static boolean isStringClass(String typeString){
        return Arrays.asList(
                char.class.getName(),
                String.class.getName(),
                Character.class.getName()
        ).contains(typeString);
    }


    public static Class<?> getClassForName(String typeString) throws Exception{
        if (isPrimitive(typeString)){
            return PrimitiveTypeEnum.getClassByName(typeString);
        }
        return Class.forName(typeString);
    }


}
