package org.hango.cloud.dashboard.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClassMethodExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ClassMethodExecutor.class);
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_IS = "is";
    private Class<?> clazz;
    private Set<String> excludes;
    private Set<String> includes;

    public ClassMethodExecutor(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void executeAllMethod() {
        executeAllMethod(true, true);
    }

    public void executeAllReadMethod() {
        executeAllMethod(true, false);
    }

    public void executeAllWriteMethod() {
        executeAllMethod(false, true);
    }

    private void executeAllMethod(boolean executeReadMethod, boolean executeWriteMethod) {
        try {
            List<Method> methods = new ArrayList<Method>();
            if (executeReadMethod) {
                methods.addAll(getReadMethod());
            }
            if (executeWriteMethod) {
                methods.addAll(getWriteMethod());
            }

            for (Method method : methods) {
                Class<?>[] params = method.getParameterTypes();
                List<Object> values = new ArrayList<Object>();
                for (Class<?> param : params) {
                    values.add(DefaultValue.getDefaultValue(param));
                }
                method.setAccessible(true);
                logger.info("execute method [{}]", clazz.getName() + "." + method.getName());
                method.invoke(clazz.newInstance(), values.toArray());
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private List<Method> getReadMethod() {
        List<Method> readMethods = new ArrayList<Method>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (isReadMethod(methodName)) {
                readMethods.add(method);
            }
        }
        return readMethods;
    }

    private List<Method> getWriteMethod() {
        List<Method> readMethods = new ArrayList<Method>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (!isReadMethod(methodName)) {
                readMethods.add(method);
            }
        }
        return readMethods;
    }

    private boolean isReadMethod(String methodName) {
        if (methodName.startsWith(PREFIX_GET) || methodName.startsWith(PREFIX_IS)) {
            return true;
        }
        return false;
    }

}
