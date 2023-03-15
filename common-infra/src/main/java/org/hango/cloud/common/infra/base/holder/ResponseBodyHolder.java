package org.hango.cloud.common.infra.base.holder;

import org.hango.cloud.common.infra.base.meta.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Id: ResponseBodyHolder.java, v 1.0 2018年09月06日 11:29
 */
public class ResponseBodyHolder {

    public static final Logger logger = LoggerFactory.getLogger(ResponseBodyHolder.class);

    public static final String RESTFUL = "restful";

    public static final String ACTION = "action";

    private static ThreadLocal<Pair<String, String>> responseThreadLocal = new ThreadLocal<>();

    public static Pair<String, String> getAndRemove() {
        Pair<String, String> responseBody = responseThreadLocal.get();
        logger.info("Method Invoke getAndRemove() , ThreadLocal Value {} Would Be Remove", responseBody);
        responseThreadLocal.remove();
        return responseBody;
    }

    public static void set(Pair<String, String> responseBody) {
        responseThreadLocal.set(responseBody);
    }

    public static Pair<String, String> get() {
        return responseThreadLocal.get();
    }

}
