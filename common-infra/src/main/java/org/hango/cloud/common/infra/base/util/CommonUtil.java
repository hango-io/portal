package org.hango.cloud.common.infra.base.util;

import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: Wang Dacheng(wangdacheng@corp.netease.com)
 * @Date: 创建时间: 2018/2/8 下午3:11.
 */
public class CommonUtil {
    /**
     * IPV4格式 1~255.0~255.0~255.0~255
     */
    private static final String IPV4_BASIC_PATTERN_STRING = "(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}" +
            "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}" +
            "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";

    private static final Pattern IPV4_PATTERN = Pattern.compile("^" + IPV4_BASIC_PATTERN_STRING + "$");

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);


    /**
     * 去除字符串末尾指定字符
     *
     * @param remove
     * @param origin
     * @return
     */
    public static String removeEnd(String remove, String origin) {
        if (StringUtils.isAnyBlank(remove, origin)) {
            return origin;
        }
        if (!StringUtils.endsWith(origin, remove)) {
            return origin;
        }
        return removeEnd(remove, origin.substring(0, origin.length() - remove.length()));
    }


    /**
     * 获取IP
     *
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(xFor) && BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = xFor.indexOf(",");
            if (index != -1) {
                return xFor.substring(0, index);
            } else {
                return xFor;
            }
        }
        xFor = xip;
        if (StringUtils.isNotEmpty(xFor) && !BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            return xFor;
        }
        if (StringUtils.isBlank(xFor) || BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(xFor) || BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(xFor) || BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(xFor) || BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            xFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(xFor) || BaseConst.UNKNOWN.equalsIgnoreCase(xFor)) {
            xFor = request.getRemoteAddr();
        }
        return xFor;
    }

    /**
     * 检查IP、IP网段格式是否正确
     *
     * @param ipAddress IP地址或网段
     * @return true:格式正确 false:格式错误
     */
    public static boolean ipCheck(String ipAddress) {
        if (ipAddress.indexOf(BaseConst.SYMBOL_SLASH) > 0) {
            try {

                String[] addressAndMask = StringUtils.split(ipAddress, BaseConst.SYMBOL_SLASH);
                ipAddress = addressAndMask[0];
                int nMaskBits = Integer.parseInt(addressAndMask[1]);
                if (nMaskBits < 0 || nMaskBits > 32) {
                    return false;
                }
                return IPV4_PATTERN.matcher(ipAddress).matches();
            } catch (Exception e) {
                logger.info("解析IP地址格式时发生异常!", e);
                return false;
            }
        }
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>(BaseConst.DEFAULT_MAP_SIZE);
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 将输入字符串根据符号分割，从而获取到Long类型集合
     *
     * @param string    待分割字符串
     * @param separator 分隔符
     * @return 分割的Long类型元素集合
     */
    public static List<Long> splitStringToLongList(String string, String separator) {
        if (StringUtils.isEmpty(string)) {
            return Collections.EMPTY_LIST;
        } else {
            return Arrays.stream(string.split(separator))
                    .filter(item -> !StringUtils.isEmpty(item))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
    }

    public static ErrorCode checkOffsetAndLimit(long offset, long limit) {
        if (offset < 0) {
            return CommonErrorCode.invalidParameter(String.valueOf(offset), "Offset");
        } else if (limit < 0 || limit > 1000) {
            return CommonErrorCode.invalidParameter(String.valueOf(limit), "Limit");
        }
        return CommonErrorCode.SUCCESS;
    }

    public static Boolean equalIgnoreSeq(List<String> list1, List<String> list2){
        if (list1 == null){
            list1 = new ArrayList<>();
        }
        if (list2 == null){
            list2 = new ArrayList<>();
        }
        if (list1.size() != list2.size()){
            return Boolean.FALSE;
        }
        list2.sort(Comparator.comparing(String::hashCode));
        list1.sort(Comparator.comparing(String::hashCode));
        return list2.toString().equals(list1.toString());
    }
}
