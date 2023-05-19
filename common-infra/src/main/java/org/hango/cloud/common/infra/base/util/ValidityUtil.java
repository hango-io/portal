package org.hango.cloud.common.infra.base.util;

/**
 * @Author zhufengwei
 * @Date 2023/1/6
 */
public class ValidityUtil {
    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 65536;

    public static boolean vaildPort(Integer port){
        return port > MIN_PORT && port < MAX_PORT;
    }
}
