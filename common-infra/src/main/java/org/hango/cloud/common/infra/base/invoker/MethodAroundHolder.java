package org.hango.cloud.common.infra.base.invoker;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 方法入参、返回存储器
 * @date 2022/8/18
 */
public class MethodAroundHolder {

    private static Logger logger = LoggerFactory.getLogger(MethodAroundHolder.class);

    private static ThreadLocal<MutablePair<Object[], Object>> methodInvokerInfo = new ThreadLocal<>();

    private static ThreadLocal<Integer> paramIndex = ThreadLocal.withInitial(() -> NumberUtils.INTEGER_ZERO);

    public static Pair get() {
        MutablePair<Object[], Object> pair = methodInvokerInfo.get();
        return pair;
    }

    public static Object[] getParams() {
        Pair<Object[], Object> pair = get();
        if (pair == null) {
            return null;
        }

        return pair.getLeft();
    }

    /**
     * 设置方法入参
     *
     * @param params
     */
    public static void setParams(Object[] params) {
        MutablePair<Object[], Object> pair = methodInvokerInfo.get();
        if (pair == null) {
            pair = new MutablePair<Object[], Object>();
        }
        pair.setLeft(params);
        methodInvokerInfo.set(pair);
    }

    /**
     * 设置方法入参
     *
     * @param param
     * @param index
     */
    public static void rewriteParam(Object param, int index) {
        MutablePair<Object[], Object> pair = methodInvokerInfo.get();
        if (pair == null) {
            return;
        }
        Object[] left = pair.getLeft();
        if (left == null) {
            return;
        }
        if (index >= left.length) {
            logger.warn("invalid index ,out of bound");
            return;
        }
        left[index] = param;
    }

    public static Object getParam() {
        return getParam(0);
    }

    public static <Type> Type getNextParam(Class<Type> paramType) {
        Object nextParam = getNextParam();
        if (nextParam == null) {
            return null;
        }
        return paramType.cast(nextParam);
    }

    public static Object getNextParam() {
        Integer index = paramIndex.get();
        Object param = getParam(index);
        index++;
        paramIndex.set(index);
        return param;
    }

    public static Object getParam(int index) {
        Pair<Object[], Object> pair = get();
        if (pair == null) {
            return null;
        }
        Object[] left = pair.getLeft();
        if (left == null) {
            return null;
        }
        if (index >= left.length) {
            logger.warn("invalid index ,out of bound");
            return null;
        }
        return left[index];
    }

    public static Object getReturn() {
        Pair pair = get();
        if (pair == null) {
            return null;
        }
        return pair.getRight();
    }

    /**
     * 设置方法返回
     *
     * @param returns
     */
    public static void setReturn(Object returns) {
        MutablePair pair = methodInvokerInfo.get();
        if (pair == null) {
            pair = new MutablePair<>();
        }
        pair.setRight(returns);
        methodInvokerInfo.set(pair);
    }

    public static void remove() {
        paramIndex.set(NumberUtils.INTEGER_ZERO);
        methodInvokerInfo.remove();
    }
}
