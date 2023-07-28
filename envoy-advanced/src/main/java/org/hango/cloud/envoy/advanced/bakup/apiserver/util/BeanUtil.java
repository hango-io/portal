package org.hango.cloud.envoy.advanced.bakup.apiserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

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
}
